package com.example.gitcat_events.features.entrant.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileDialogFragment extends DialogFragment {

    public interface OnSaveProfileListener {
        void onSaveProfile(Profile profile);
    }

    private static final String TAG = "ProfileDialogFragment";
    
    private @Nullable Profile existingProfile;
    private @Nullable Uri selectedImageUri;
    private ImageView ivDialogProfilePicture;
    private String base64Image = null;

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
                base64Image = existingProfile.getProfilePictureUrl();
                loadBase64Image(base64Image, ivDialogProfilePicture);
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

            String nameRaw  = etName.getText().toString().trim();
            String emailRaw = etEmail.getText().toString().trim();
            String phoneRaw = etPhone.getText().toString().trim();

            // Validate email format if provided
            if (!emailRaw.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailRaw).matches()) {
                etEmail.setError("Please enter a valid email");
                return; // keep dialog open
            }

            // All fields are optional
            String name = nameRaw.isEmpty() ? null : nameRaw;
            String email = emailRaw.isEmpty() ? null : emailRaw;
            String phone = phoneRaw.isEmpty() ? null : phoneRaw;
            
            // If user selected a new image, convert to Base64
            if (selectedImageUri != null) {
                // Show progress dialog
                ProgressDialog progressDialog = new ProgressDialog(requireContext());
                progressDialog.setMessage("Processing image...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                
                // Convert image to Base64 in background
                new Thread(() -> {
                    try {
                        String imageBase64 = convertImageToBase64(selectedImageUri);
                        
                        // Update UI on main thread
                        requireActivity().runOnUiThread(() -> {
                            progressDialog.dismiss();
                            
                            if (imageBase64 != null) {
                                // Create profile with Base64 image
                                String deviceId = (existingProfile != null && existingProfile.getDeviceId() != null) 
                                        ? existingProfile.getDeviceId() 
                                        : getOrCreateDeviceId();
                                Profile p = new Profile(name, email, phone, deviceId, imageBase64);
                                
                                saveProfile(p);
                                dlg.dismiss();
                                Toast.makeText(requireContext(), "Profile picture saved!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to process image. Saving without picture.", Toast.LENGTH_SHORT).show();
                                // Save without image
                                String deviceId = (existingProfile != null && existingProfile.getDeviceId() != null) 
                                        ? existingProfile.getDeviceId() 
                                        : getOrCreateDeviceId();
                                Profile p = new Profile(name, email, phone, deviceId, null);
                                saveProfile(p);
                                dlg.dismiss();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error converting image", e);
                        requireActivity().runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(), "Image error. Saving without picture.", Toast.LENGTH_SHORT).show();
                            String deviceId = (existingProfile != null && existingProfile.getDeviceId() != null) 
                                    ? existingProfile.getDeviceId() 
                                    : getOrCreateDeviceId();
                            Profile p = new Profile(name, email, phone, deviceId, null);
                            saveProfile(p);
                            dlg.dismiss();
                        });
                    }
                }).start();
            } else {
                // No new image selected, just save profile
                String deviceId = (existingProfile != null && existingProfile.getDeviceId() != null) 
                        ? existingProfile.getDeviceId() 
                        : getOrCreateDeviceId();
                
                // Keep existing profile picture if no new image selected
                String profilePicture = base64Image;
                if (profilePicture == null && existingProfile != null) {
                    profilePicture = existingProfile.getProfilePictureUrl();
                }
                
                Profile p = new Profile(name, email, phone, deviceId, profilePicture);
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
     * Convert image URI to Base64 string
     */
    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream");
                return null;
            }
            
            // Decode image to bitmap
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap");
                return null;
            }
            
            // Resize bitmap to reduce size (max 500px on longest side)
            Bitmap resizedBitmap = resizeBitmap(originalBitmap, 500);
            
            // Convert to Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
            
            Log.d(TAG, "Image converted to Base64. Size: " + (base64Image.length() / 1024) + "KB");
            
            // Firestore has a 1MB limit per document, warn if close
            if (base64Image.length() > 800000) {
                Log.w(TAG, "Warning: Image size is large (" + (base64Image.length() / 1024) + "KB). May hit Firestore limit.");
            }
            
            return base64Image;
            
        } catch (Exception e) {
            Log.e(TAG, "Error converting image to Base64", e);
            return null;
        }
    }
    
    /**
     * Resize bitmap to fit within maxSize while maintaining aspect ratio
     */
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        float ratio = Math.min(
            (float) maxSize / width,
            (float) maxSize / height
        );
        
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    /**
     * Load Base64 image into ImageView
     */
    private void loadBase64Image(String base64String, ImageView imageView) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading Base64 image", e);
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
    
    private String getOrCreateDeviceId() {
        if (getContext() == null) return java.util.UUID.randomUUID().toString();
        
        android.content.SharedPreferences sp = getContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
        String deviceId = sp.getString("device_id", null);

        if (deviceId == null) {
            try {
                deviceId = android.provider.Settings.Secure.getString(getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
                Log.w(TAG, "Failed to get Android ID", e);
            }

            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = java.util.UUID.randomUUID().toString();
            }
            sp.edit().putString("device_id", deviceId).apply();
        }
        return deviceId;
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
}
