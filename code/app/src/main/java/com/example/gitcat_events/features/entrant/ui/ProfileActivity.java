package com.example.gitcat_events.features.entrant.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gitcat_events.R;
import com.example.gitcat_events.core.model.Profile;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity
        implements ProfileDialogFragment.OnSaveProfileListener {

    private FirebaseFirestore db;
    String TAG = "FirestoreSmoke";
    private TextView tvName, tvEmail, tvPhone;

    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id"; // we'll store the numeric id as a String

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
        Button btnEdit = findViewById(R.id.btnEdit);

        loadProfile();

        btnEdit.setOnClickListener(v ->
                ProfileDialogFragment.newInstance(currentProfile)
                        .show(getSupportFragmentManager(), "editProfile"));
        Button btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> confirmAndDelete());
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
    }

    /** Called when dialog presses Save */
    @Override
    public void onSaveProfile(Profile profile) {
        String existingId = getSavedDocId();
        if (existingId == null) {
            // Create new profile with sequential numeric id (0,1,2,...) via transaction
            createProfileWithAutoId(profile);
        } else {
            // Update existing
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
                    Toast.makeText(this, "Profile deleted.", Toast.LENGTH_SHORT).show();

                    // Prompt to create a new one (optional)
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
}