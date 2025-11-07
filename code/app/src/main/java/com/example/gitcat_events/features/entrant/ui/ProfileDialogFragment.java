package com.example.gitcat_events.features.entrant.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.example.gitcat_events.R;
import com.example.gitcat_events.core.model.Profile;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class ProfileDialogFragment extends DialogFragment {

    public interface OnSaveProfileListener {
        void onSaveProfile(Profile profile);
    }

    private static final String TAG = "ProfileDialogFragment";
    
    private @Nullable Profile existingProfile;
    private @Nullable Uri selectedImageUri;
    private ImageView ivDialogProfilePicture;
    private FirebaseStorage storage;
    private String uploadedImageUrl = null;

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
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        
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
                uploadedImageUrl = existingProfile.getProfilePictureUrl();
                Glide.with(this)
                    .load(existingProfile.getProfilePictureUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .circleCrop()
                    .into(ivDialogProfilePicture);
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
            
            // If user selected a new image, upload it first
            if (selectedImageUri != null) {
                // Show progress dialog
                ProgressDialog progressDialog = new ProgressDialog(requireContext());
                progressDialog.setMessage("Uploading profile picture...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                
                uploadImageToFirebase(selectedImageUri, new ImageUploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        progressDialog.dismiss();
                        // Create profile with uploaded image URL
                        Profile p = new Profile(name, email, phone);
                        p.setProfilePictureUrl(imageUrl);
                        
                        // Preserve device ID if it exists
                        if (existingProfile != null && existingProfile.getDeviceId() != null) {
                            p.setDeviceId(existingProfile.getDeviceId());
                        }
                        
                        saveProfile(p);
                        dlg.dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Image upload failed: " + error, Toast.LENGTH_LONG).show();
                        // Continue with save anyway, without image
                        Profile p = new Profile(name, email, phone);
                        if (existingProfile != null && existingProfile.getProfilePictureUrl() != null) {
                            p.setProfilePictureUrl(existingProfile.getProfilePictureUrl());
                        }
                        if (existingProfile != null && existingProfile.getDeviceId() != null) {
                            p.setDeviceId(existingProfile.getDeviceId());
                        }
                        saveProfile(p);
                        dlg.dismiss();
                    }
                });
            } else {
                // No new image selected, just save profile
                Profile p = new Profile(name, email, phone);
                
                // Keep existing profile picture URL if no new image selected
                if (uploadedImageUrl != null) {
                    p.setProfilePictureUrl(uploadedImageUrl);
                } else if (existingProfile != null && existingProfile.getProfilePictureUrl() != null) {
                    p.setProfilePictureUrl(existingProfile.getProfilePictureUrl());
                }
                
                // Preserve device ID if it exists
                if (existingProfile != null && existingProfile.getDeviceId() != null) {
                    p.setDeviceId(existingProfile.getDeviceId());
                }
                
                saveProfile(p);
                dlg.dismiss();
            }
        });
    }

    public static ProfileDialogFragment newInstance(@Nullable Profile existing) {
        ProfileDialogFragment f = new ProfileDialogFragment();
        Bundle b = new Bundle();
        if (existing != null) b.putSerializable("profile", existing);
        f.setArguments(b);
        return f;
    }
    
    /**
     * Upload image to Firebase Storage and get download URL
     */
    private void uploadImageToFirebase(Uri imageUri, ImageUploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure("No image selected");
            return;
        }
        
        // Create a unique filename for the image
        String filename = "profile_pictures/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = storage.getReference().child(filename);
        
        // Upload the file
        storageRef.putFile(imageUri)
            .addOnSuccessListener(taskSnapshot -> {
                // Get the download URL
                storageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Log.d(TAG, "Image uploaded successfully: " + uri.toString());
                        callback.onSuccess(uri.toString());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get download URL", e);
                        callback.onFailure(e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Image upload failed", e);
                callback.onFailure(e.getMessage());
            });
    }
    
    /**
     * Save profile by calling the listener
     */
    private void saveProfile(Profile profile) {
        OnSaveProfileListener host = null;
        if (getParentFragment() instanceof OnSaveProfileListener) {
            host = (OnSaveProfileListener) getParentFragment();
        } else if (getActivity() instanceof OnSaveProfileListener) {
            host = (OnSaveProfileListener) getActivity();
        }
        if (host != null) {
            host.onSaveProfile(profile);
        }
    }
    
    /**
     * Callback interface for image upload
     */
    private interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }
}
