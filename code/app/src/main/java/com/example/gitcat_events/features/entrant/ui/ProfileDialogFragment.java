package com.example.gitcat_events.features.entrant.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.gitcat_events.R;
import com.example.gitcat_events.core.model.Profile;

public class ProfileDialogFragment extends DialogFragment {

    public interface OnSaveProfileListener {
        void onSaveProfile(Profile profile);
    }

    private @Nullable Profile existingProfile;
    private @Nullable Uri selectedImageUri;
    private ImageView ivDialogProfilePicture;

    // Activity result launcher for image selection
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (ivDialogProfilePicture != null && selectedImageUri != null) {
                        ivDialogProfilePicture.setImageURI(selectedImageUri);
                    }
                }
            });

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate content view
        View v = getLayoutInflater().inflate(R.layout.dialog_profile, null);
        EditText etName  = v.findViewById(R.id.etName);
        EditText etEmail = v.findViewById(R.id.etEmail);
        EditText etPhone = v.findViewById(R.id.etPhone);
        ivDialogProfilePicture = v.findViewById(R.id.ivDialogProfilePicture);
        Button btnSelectProfilePicture = v.findViewById(R.id.btnSelectProfilePicture);

        // Prefill if editing
        Bundle args = getArguments();
        if (args != null && args.getSerializable("profile") instanceof Profile) {
            existingProfile = (Profile) args.getSerializable("profile");
        }
        if (existingProfile != null) {
            etName.setText(existingProfile.getName());
            etEmail.setText(existingProfile.getEmail());
            if (existingProfile.getPhone() != null) etPhone.setText(existingProfile.getPhone());
            
            // Load existing profile picture if available
            if (existingProfile.getProfilePictureUrl() != null && !existingProfile.getProfilePictureUrl().isEmpty()) {
                // TODO: Load image from URL using Glide or Picasso
                ivDialogProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }

        // Setup image picker button
        btnSelectProfilePicture.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // NOTE: setPositiveButton(null) so we can attach a custom click in onStart()
        return new AlertDialog.Builder(requireContext())
                .setTitle(existingProfile == null ? "Create profile" : "Edit profile")
                .setView(v)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null) // custom handler in onStart()
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dlg = (AlertDialog) getDialog();
        if (dlg == null) return;

        // Grab views from the dialog
        final EditText etName  = dlg.findViewById(R.id.etName);
        final EditText etEmail = dlg.findViewById(R.id.etEmail);
        final EditText etPhone = dlg.findViewById(R.id.etPhone);

        // Attach custom Save handler (prevents auto-dismiss on validation errors)
        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (etName == null || etEmail == null || etPhone == null) {
                dlg.dismiss(); // defensive (shouldn't happen)
                return;
            }

            String name  = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phoneRaw = etPhone.getText().toString().trim();

            boolean ok = true;
            if (name.isEmpty()) { etName.setError("Name required"); ok = false; }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Valid email required"); ok = false;
            }
            if (!ok) return; // keep dialog open

            String phone = phoneRaw.isEmpty() ? null : phoneRaw; // optional
            
            // Create profile with device ID and picture URL
            Profile p = new Profile(name, email, phone);
            
            // If user selected a new image, convert URI to string (for now)
            // In production, you'd upload to Firebase Storage and get the download URL
            if (selectedImageUri != null) {
                p.setProfilePictureUrl(selectedImageUri.toString());
                Toast.makeText(requireContext(), "Note: Image upload to Firebase Storage not implemented yet", Toast.LENGTH_SHORT).show();
            } else if (existingProfile != null && existingProfile.getProfilePictureUrl() != null) {
                // Keep existing profile picture URL if no new image selected
                p.setProfilePictureUrl(existingProfile.getProfilePictureUrl());
            }
            
            // Preserve device ID if it exists
            if (existingProfile != null && existingProfile.getDeviceId() != null) {
                p.setDeviceId(existingProfile.getDeviceId());
            }

            OnSaveProfileListener host = null;
            if (getParentFragment() instanceof OnSaveProfileListener) {
                host = (OnSaveProfileListener) getParentFragment();
            } else if (getActivity() instanceof OnSaveProfileListener) {
                host = (OnSaveProfileListener) getActivity();
            }
            if (host != null) host.onSaveProfile(p);

            dlg.dismiss(); // only dismiss after successful validation/callback
        });
    }

    public static ProfileDialogFragment newInstance(@Nullable Profile existing) {
        ProfileDialogFragment f = new ProfileDialogFragment();
        Bundle b = new Bundle();
        if (existing != null) b.putSerializable("profile", existing);
        f.setArguments(b);
        return f;
    }
}
