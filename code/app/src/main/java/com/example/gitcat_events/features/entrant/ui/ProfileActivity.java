package com.example.gitcat_events.features.entrant.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gitcat_events.CreateFragment;
import com.example.gitcat_events.HomeFragment;
import com.example.gitcat_events.MainActivity;
import com.example.gitcat_events.NotifsFragment;
import com.example.gitcat_events.ProfileFragment;
import com.example.gitcat_events.R;
import com.example.gitcat_events.core.model.Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity
        implements ProfileDialogFragment.OnSaveProfileListener {

    private FirebaseFirestore db;
    String TAG = "FirestoreSmoke";
    private TextView tvName, tvEmail, tvPhone;
    private ImageView ivProfilePicture;

    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id"; // we'll store the numeric id as a String
    private static final String KEY_DEVICE_ID = "device_id"; // unique device identifier

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseFirestore.setLoggingEnabled(true);
        db = FirebaseFirestore.getInstance();
        Map<String, Object> doc = new HashMap<>();
        doc.put("ts", System.currentTimeMillis());
        doc.put("ok", true);

        db.collection("health").document("ping").set(doc)
                .addOnSuccessListener(v -> Log.d(TAG, "WRITE OK"))
                .addOnFailureListener(e -> Log.e(TAG, "WRITE FAIL", e));

        db.collection("health").document("ping").get()
                .addOnSuccessListener(s -> Log.d(TAG, "READ OK exists=" + s.exists()))
                .addOnFailureListener(e -> Log.e(TAG, "READ FAIL", e));

        tvName  = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        Button btnEdit = findViewById(R.id.btnEdit);

        loadProfile();

        btnEdit.setOnClickListener(v ->
                ProfileDialogFragment.newInstance(currentProfile)
                        .show(getSupportFragmentManager(), "editProfile"));
        Button btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> confirmAndDelete());
        
        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.profile); // Highlight profile tab
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.home) {
                // Go back to MainActivity with Home fragment
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment", "home");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.notifs) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment", "notifs");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.create) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment", "create");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.profile) {
                // Already on profile, do nothing
                return true;
            }
            
            return false;
        });
    }

    private Profile currentProfile;

    /** Load existing profile using saved numeric id (if any). If none, prompt to create. */
    private void loadProfile() {
        String id = getSavedDocId();
        if (id == null) {
            // no profile yet — open dialog to create
            ProfileDialogFragment.newInstance(null)
                    .show(getSupportFragmentManager(), "createProfile");
            return;
        }

        db.collection("profiles").document(id).get()
                .addOnSuccessListener(snap -> {
                    currentProfile = snap.toObject(Profile.class);
                    if (currentProfile != null) {
                        render(currentProfile);
                    } else {
                        // doc missing? treat as create
                        ProfileDialogFragment.newInstance(null)
                                .show(getSupportFragmentManager(), "createProfile");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void render(Profile p) {
        tvName.setText(p.getName());
        tvEmail.setText(p.getEmail());
        String ph = p.getPhone();
        tvPhone.setText((ph == null || ph.trim().isEmpty()) ? "—" : ph);
        
        // Load profile picture from Base64 string
        if (p.getProfilePictureUrl() != null && !p.getProfilePictureUrl().isEmpty()) {
            loadBase64Image(p.getProfilePictureUrl(), ivProfilePicture);
        } else {
            ivProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    /** Called when dialog presses Save */
    @Override
    public void onSaveProfile(Profile profile) {
        // Ensure device ID is set
        if (profile.getDeviceId() == null || profile.getDeviceId().isEmpty()) {
            profile.setDeviceId(getOrCreateDeviceId());
        }
        
        String existingId = getSavedDocId();
        if (existingId == null) {
            // Create new profile with sequential numeric id (0,1,2,...) via transaction
            createProfileWithAutoId(profile);
        } else {
            // Update existing - preserve deviceId and profilePictureUrl if not changed
            if (currentProfile != null) {
                if (profile.getDeviceId() == null) {
                    profile.setDeviceId(currentProfile.getDeviceId());
                }
                if (profile.getProfilePictureUrl() == null) {
                    profile.setProfilePictureUrl(currentProfile.getProfilePictureUrl());
                }
            }
            db.collection("profiles").document(existingId).set(profile)
                    .addOnSuccessListener(v -> {
                        currentProfile = profile;
                        render(profile);
                        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    /** Transaction: read meta/profiles_counter.next (default 0), use it as id, write profile, bump next */
    private void createProfileWithAutoId(Profile profile) {
        db.runTransaction(transaction -> {
            DocumentReference counterRef = db.collection("meta").document("profiles_counter");
            DocumentSnapshot snap = transaction.get(counterRef);

            long next;
            boolean existed = snap.exists();
            if (existed) {
                Long val = snap.getLong("next");
                next = (val != null) ? val : 0L;
            } else {
                next = 0L; // first user gets 0
            }

            String docId = String.valueOf(next);
            DocumentReference profileRef = db.collection("profiles").document(docId);

            // write profile (you can also include "uid": next if you want)
            Map<String, Object> data = new HashMap<>();
            data.put("name", profile.getName());
            data.put("email", profile.getEmail());
            data.put("phone", profile.getPhone()); // may be null
            data.put("deviceId", profile.getDeviceId()); // device identifier
            data.put("profilePictureUrl", profile.getProfilePictureUrl()); // profile picture URL
            data.put("uid", next);
            transaction.set(profileRef, data);

            // bump counter
            if (existed) {
                transaction.update(counterRef, "next", next + 1L);
            } else {
                Map<String, Object> counterInit = new HashMap<>();
                counterInit.put("next", next + 1L); // we consumed 0
                transaction.set(counterRef, counterInit);
            }

            return docId;
        }).addOnSuccessListener(assignedId -> {
            saveDocId(assignedId);                 // persist "0", "1", ...
            currentProfile = profile;              // <- use what you saved
            render(profile);
            Toast.makeText(this, "Created user #" + assignedId, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Create failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // --- tiny prefs helpers ---

    private @Nullable String getSavedDocId() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        return sp.getString(KEY_PROFILE_ID, null);
    }
    private void confirmAndDelete() {
        String id = getSavedDocId();
        if (id == null) {
            Toast.makeText(this, "No profile to delete.", Toast.LENGTH_SHORT).show();
            return;
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete profile?")
                .setMessage("This will remove your profile from the database on this app.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> deleteProfileById(id))
                .show();
    }

    private void deleteProfileById(@NonNull String id) {
        db.collection("profiles").document(id).delete()
                .addOnSuccessListener(v -> {
                    // clear local state
                    saveDocId(null);        // clear prefs key
                    currentProfile = null;
                    // clear UI
                    tvName.setText("—");
                    tvEmail.setText("—");
                    tvPhone.setText("—");
                    ivProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                    Toast.makeText(this, "Profile deleted successfully.", Toast.LENGTH_SHORT).show();

                    // Immediately show profile creation dialog
                    ProfileDialogFragment.newInstance(null)
                            .show(getSupportFragmentManager(), "createProfile");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private void saveDocId(String id) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_PROFILE_ID, id).apply();
    }
    
    /**
     * Get or create a unique device identifier.
     * First tries Android ID, falls back to UUID stored in SharedPreferences.
     */
    private String getOrCreateDeviceId() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String deviceId = sp.getString(KEY_DEVICE_ID, null);
        
        if (deviceId == null) {
            // Try to get Android ID (unique per device and app installation)
            try {
                deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
                Log.w(TAG, "Failed to get Android ID", e);
            }
            
            // Fallback to UUID if Android ID is not available
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
            }
            
            // Save for future use
            sp.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        }
        
        return deviceId;
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
}