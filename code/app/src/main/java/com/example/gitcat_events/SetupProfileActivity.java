package com.example.gitcat_events;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gitcat_events.core.model.Profile;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetupProfileActivity extends AppCompatActivity {

    private static final String TAG = "SetupProfileActivity";
    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id";
    private static final String KEY_DEVICE_ID = "device_id";

    private FirebaseFirestore db;
    private ImageView ivSetupProfilePicture;
    private TextInputEditText etSetupName, etSetupEmail, etSetupPhone;
    private Button btnSelectImage, btnCreateProfile;
    
    private Uri selectedImageUri;
    private String base64Image = null;

    // Activity result launcher for image selection
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        ivSetupProfilePicture.setImageURI(selectedImageUri);
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        ivSetupProfilePicture = findViewById(R.id.ivSetupProfilePicture);
        etSetupName = findViewById(R.id.etSetupName);
        etSetupEmail = findViewById(R.id.etSetupEmail);
        etSetupPhone = findViewById(R.id.etSetupPhone);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnCreateProfile = findViewById(R.id.btnCreateProfile);

        // Setup image picker
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Setup create profile button
        btnCreateProfile.setOnClickListener(v -> createProfile());
    }

    private void createProfile() {
        // Get input values
        String name = etSetupName.getText().toString().trim();
        String email = etSetupEmail.getText().toString().trim();
        String phone = etSetupPhone.getText().toString().trim();

        // Validate inputs
        boolean isValid = true;
        
        if (name.isEmpty()) {
            etSetupName.setError("Name is required");
            isValid = false;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etSetupEmail.setError("Valid email is required");
            isValid = false;
        }

        if (!isValid) return;

        // Show progress
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating your profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Process image if selected
        if (selectedImageUri != null) {
            new Thread(() -> {
                try {
                    String imageBase64 = convertImageToBase64(selectedImageUri);
                    runOnUiThread(() -> saveProfileToFirestore(name, email, phone.isEmpty() ? null : phone, imageBase64, progressDialog));
                } catch (Exception e) {
                    Log.e(TAG, "Error processing image", e);
                    runOnUiThread(() -> saveProfileToFirestore(name, email, phone.isEmpty() ? null : phone, null, progressDialog));
                }
            }).start();
        } else {
            saveProfileToFirestore(name, email, phone.isEmpty() ? null : phone, null, progressDialog);
        }
    }

    private void saveProfileToFirestore(String name, String email, String phone, String imageBase64, ProgressDialog progressDialog) {
        db.runTransaction(transaction -> {
            DocumentReference counterRef = db.collection("meta").document("profiles_counter");
            DocumentSnapshot snap = transaction.get(counterRef);

            long next;
            boolean existed = snap.exists();
            if (existed) {
                Long val = snap.getLong("next");
                next = (val != null) ? val : 0L;
            } else {
                next = 0L;
            }

            String docId = String.valueOf(next);
            DocumentReference profileRef = db.collection("profiles").document(docId);

            // Create profile data
            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("email", email);
            data.put("phone", phone);
            data.put("deviceId", getOrCreateDeviceId());
            data.put("profilePictureUrl", imageBase64);
            data.put("uid", next);
            transaction.set(profileRef, data);

            // Bump counter
            if (existed) {
                transaction.update(counterRef, "next", next + 1L);
            } else {
                Map<String, Object> counterInit = new HashMap<>();
                counterInit.put("next", next + 1L);
                transaction.set(counterRef, counterInit);
            }

            return docId;
        }).addOnSuccessListener(assignedId -> {
            progressDialog.dismiss();
            
            // Save profile ID
            getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_PROFILE_ID, assignedId)
                    .apply();
            
            Toast.makeText(this, "Profile created successfully! Welcome!", Toast.LENGTH_LONG).show();
            
            // Redirect to home page
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment", "home");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Log.e(TAG, "Failed to create profile", e);
            Toast.makeText(this, "Failed to create profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
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

            // Resize bitmap
            Bitmap resizedBitmap = resizeBitmap(originalBitmap, 500);

            // Convert to Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.d(TAG, "Image converted to Base64. Size: " + (base64Image.length() / 1024) + "KB");

            return base64Image;

        } catch (Exception e) {
            Log.e(TAG, "Error converting image to Base64", e);
            return null;
        }
    }

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

    private String getOrCreateDeviceId() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String deviceId = sp.getString(KEY_DEVICE_ID, null);

        if (deviceId == null) {
            try {
                deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
                deviceId = null;
            }

            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
            }

            sp.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        }

        return deviceId;
    }

    @Override
    public void onBackPressed() {
        // Prevent going back - user must create profile
        Toast.makeText(this, "Please create your profile to continue", Toast.LENGTH_SHORT).show();
    }
}

