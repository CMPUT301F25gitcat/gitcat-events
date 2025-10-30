package com.example.gitcat_events.features.entrant.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate content view
        View v = getLayoutInflater().inflate(R.layout.dialog_profile, null);
        EditText etName  = v.findViewById(R.id.etName);
        EditText etEmail = v.findViewById(R.id.etEmail);
        EditText etPhone = v.findViewById(R.id.etPhone);

        // Prefill if editing
        Bundle args = getArguments();
        if (args != null && args.getSerializable("profile") instanceof Profile) {
            existingProfile = (Profile) args.getSerializable("profile");
        }
        if (existingProfile != null) {
            etName.setText(existingProfile.getName());
            etEmail.setText(existingProfile.getEmail());
            if (existingProfile.getPhone() != null) etPhone.setText(existingProfile.getPhone());
        }

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
            Profile p = new Profile(name, email, phone);

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
