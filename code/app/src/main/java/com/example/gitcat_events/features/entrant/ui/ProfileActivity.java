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
import com.example.gitcat_events.SetupProfileActivity;
import com.example.gitcat_events.core.model.Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Activity for displaying and managing a user's profile info.
 * Allows users to view , edit and delete their profile information.
 * profile creation is handled by incrementing user ID Identification . 
 * 
 * @author Ryan Chattopadhyay
 * 
 * @see ProfileDialogFragment
 * @see Profile
 * @see ProfileFragment
 * @see SetupProfileActivity
 * @see MainActivity
 * @see HomeFragment
 * @see NotifsFragment
 * @see CreateFragment
 */


public class ProfileActivity extends AppCompatActivity
        implements ProfileDialogFragment.OnSaveProfileListener {

    /**Firebase integration stuff for database operations. */
    private FirebaseFirestore db;
    /**Tagging for logging and debugging purposes. */
    String TAG = "FirestoreSmoke";
    /**TextView displaying the user's name, email and phone number. */
    private TextView tvName, tvEmail, tvPhone;
    /**ImageView displaying the user's profile picture. */
    private ImageView ivProfilePicture;

    /**Shared prefrences name for storing the app preferences. */
    private static final String PREFS = "app_prefs";
    /**Key for stoing the profile document id in the shared preferences. */
    private static final String KEY_PROFILE_ID = "profile_doc_id"; // key for stoing the profile document id in shared prefs 
    private static final String KEY_DEVICE_ID = "device_id"; // unique device identifier. 

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //intializing firebase firestore and perform health check operations. 
        FirebaseFirestore.setLoggingEnabled(true);
        db = FirebaseFirestore.getInstance();
        Map<String, Object> doc = new HashMap<>();
        doc.put("ts", System.currentTimeMillis());
        doc.put("ok", true);
        

        //Health check allowing us to check if read and write operations are working with the database.
        db.collection("health").document("ping").set(doc)
                .addOnSuccessListener(v -> Log.d(TAG, "WRITE OK"))
                .addOnFailureListener(e -> Log.e(TAG, "WRITE FAIL", e));

        db.collection("health").document("ping").get()
                .addOnSuccessListener(s -> Log.d(TAG, "READ OK exists=" + s.exists()))
                .addOnFailureListener(e -> Log.e(TAG, "READ FAIL", e));
        
        //Intialize the UI components. 
        tvName  = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        Button btnEdit = findViewById(R.id.btnEdit);
        //Load the existing profile if it exists. orchecks for creation. 
        loadProfile();
        //set the edit button to open the profile dialog fragment.
        btnEdit.setOnClickListener(v ->
                ProfileDialogFragment.newInstance(currentProfile)
                        .show(getSupportFragmentManager(), "editProfile"));

        //set the delete button to confirm and delete the profile. 
        Button btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> confirmAndDelete());
        
        // Setup bottom navigation to navigate between the different fragments. 
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.profile); // Highlight profile tab
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.home) {
                // to navigate to the main activity with home fragment.
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment", "home");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.notifs) {
                // Navigate to MainAcitvity with a notifs fragment.
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment", "notifs");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.create) {
                // Navigate to MainAcitvity with a create fragment.
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

    /** Load the existing profile using the saved document id from shared prefs. 
     * if no profile is found , prompts the user to create a new profile via dialog. 
     * 
     * If the profile document is missing , prompts the user to create a new profile via dialog. 
     * 
     * . */
    private void loadProfile() {
        String id = getSavedDocId();
        if (id == null) {
            // No profile found , open dialog to create a new profile.
            ProfileDialogFragment.newInstance(null)
                    .show(getSupportFragmentManager(), "createProfile");
            return;
        }

        // Fetch profile from firestore using that saved document id. 
        db.collection("profiles").document(id).get()
                .addOnSuccessListener(snap -> {
                    currentProfile = snap.toObject(Profile.class);
                    if (currentProfile != null) {
                        render(currentProfile);
                    } else {
                        // Document missing or corrupted , treat it as an entirely new profile. 
                        ProfileDialogFragment.newInstance(null)
                                .show(getSupportFragmentManager(), "createProfile");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Renders the profile data to the UI components.
     * @param p the profile object to render. 
     */

    private void render(Profile p) {
        tvName.setText(p.getName());
        tvEmail.setText(p.getEmail());
        String ph = p.getPhone();
        //Display if the phone number is null or empty. 
        tvPhone.setText((ph == null || ph.trim().isEmpty()) ? "—" : ph);
        
        // Loading the profile pic to render from a base64 string. 
        if (p.getProfilePictureUrl() != null && !p.getProfilePictureUrl().isEmpty()) {
            loadBase64Image(p.getProfilePictureUrl(), ivProfilePicture);
        } else {
            // use default launcher icon if no profile pic is available. 
            ivProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    /** Called when dialog presses Save button to save the profile from the dialog fragment.
     * 
     * Handles both creating new profiles and updating existing profiles.
     * 
     * New profiles: Users transaction to assign sequential numeric IDs (0,1,2,...)
     * for Exsisting profiles : preserves the device id and profile picture url if not changed. 
     * 
     * 
     * 
     * @param profile
    */
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
            // update existing profile and preserve the deviceId and profilePictureUrl if not changed. 
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

    /** Transaction: 
     * read meta/profiles_counter.next (default 0), use it as id, 
     * write profile, bump next counter by 1. 
     * 
     * @param profile which is the profile object to create with a sequential numeric id. 
     */
    private void createProfileWithAutoId(Profile profile) {
        db.runTransaction(transaction -> {
            // Reference to the counter document in the meta collection. 
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

            
            //write profile data to the firestore database. 
            Map<String, Object> data = new HashMap<>();
            data.put("name", profile.getName());
            data.put("email", profile.getEmail());
            data.put("phone", profile.getPhone()); // could be null
            data.put("deviceId", profile.getDeviceId()); // device identifier for the phone which the users uses. 
            data.put("profilePictureUrl", profile.getProfilePictureUrl()); // profile picture URL which is a base64 string.
            data.put("uid", next);
            transaction.set(profileRef, data);

            // counter to track bumping the next profile id. 
            if (existed) {
                transaction.update(counterRef, "next", next + 1L);
            } else {
                Map<String, Object> counterInit = new HashMap<>();
                counterInit.put("next", next + 1L); // we consumed 0, we need to bump the counter by 1. 
                transaction.set(counterRef, counterInit);
            }

            return docId;
        }).addOnSuccessListener(assignedId -> {
            saveDocId(assignedId);                 
            currentProfile = profile;              
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
                    // clear local state UI components. 
                    saveDocId(null);        // clear prefs key
                    currentProfile = null;
                    // clear UI components. 
                    tvName.setText("—");
                    tvEmail.setText("—");
                    tvPhone.setText("—"); 
                    ivProfilePicture.setImageResource(R.drawable.ic_launcher_foreground); // default profile pic. 
                    Toast.makeText(this, "Profile deleted successfully.", Toast.LENGTH_SHORT).show();

                    // Redirect to setup page to create a new profile. 
                    Intent intent = new Intent(this, SetupProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private void saveDocId(String id) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_PROFILE_ID, id).apply();
    }
    
    /**
     * Get or create Unique Device Identifier.
     * First tries Android ID, falls back to UUID stored in SharedPreferences.
     * 
     * @return the unique device identifier. 
     */
    private String getOrCreateDeviceId() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String deviceId = sp.getString(KEY_DEVICE_ID, null);
        
        if (deviceId == null) {
            // Try to get android id which is unique per device and app installation.
            try {
                deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
                Log.w(TAG, "Failed to get Android ID", e);
            }
            
            // fallback to UUID if android id is not available.
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
            }
            
            // saving for future use in the shared prefs for the device id. 
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