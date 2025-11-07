package com.example.gitcat_events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gitcat_events.core.model.Profile;
import com.example.gitcat_events.features.entrant.ui.ProfileActivity;
import com.example.gitcat_events.features.entrant.ui.ProfileDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment to display user profile information
 */
public class ProfileFragment extends Fragment implements ProfileDialogFragment.OnSaveProfileListener {

    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id";

    private FirebaseFirestore db;
    private TextView tvFragmentName, tvFragmentEmail, tvFragmentPhone;
    private ImageView ivFragmentProfilePicture;
    private Button btnFragmentViewFullProfile, btnFragmentDeleteProfile;
    private Profile currentProfile;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvFragmentName = view.findViewById(R.id.tvFragmentName);
        tvFragmentEmail = view.findViewById(R.id.tvFragmentEmail);
        tvFragmentPhone = view.findViewById(R.id.tvFragmentPhone);
        ivFragmentProfilePicture = view.findViewById(R.id.ivFragmentProfilePicture);
        btnFragmentViewFullProfile = view.findViewById(R.id.btnFragmentViewFullProfile);
        btnFragmentDeleteProfile = view.findViewById(R.id.btnFragmentDeleteProfile);

        // Setup button to open edit dialog directly
        btnFragmentViewFullProfile.setOnClickListener(v -> {
            if (currentProfile != null) {
                ProfileDialogFragment.newInstance(currentProfile)
                        .show(getChildFragmentManager(), "editProfile");
            } else {
                ProfileDialogFragment.newInstance(null)
                        .show(getChildFragmentManager(), "createProfile");
            }
        });
        
        // Setup delete button
        btnFragmentDeleteProfile.setOnClickListener(v -> confirmAndDelete());

        // Load profile data
        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh profile data when fragment becomes visible
        loadProfile();
    }

    private void loadProfile() {
        String profileId = getSavedDocId();
        if (profileId == null) {
            // No profile yet, show default/empty state
            tvFragmentName.setText("No profile yet");
            tvFragmentEmail.setText("—");
            tvFragmentPhone.setText("—");
            ivFragmentProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }

        // Fetch profile from Firestore
        db.collection("profiles").document(profileId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Profile profile = documentSnapshot.toObject(Profile.class);
                    if (profile != null && isAdded()) {
                        currentProfile = profile;
                        renderProfile(profile);
                    } else if (isAdded()) {
                        currentProfile = null;
                        tvFragmentName.setText("Profile not found");
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        currentProfile = null;
                        Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderProfile(Profile profile) {
        tvFragmentName.setText(profile.getName());
        tvFragmentEmail.setText(profile.getEmail());
        String phone = profile.getPhone();
        tvFragmentPhone.setText((phone == null || phone.trim().isEmpty()) ? "—" : phone);

        // Load profile picture with Glide
        if (profile.getProfilePictureUrl() != null && !profile.getProfilePictureUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(profile.getProfilePictureUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(ivFragmentProfilePicture);
        } else {
            ivFragmentProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private @Nullable String getSavedDocId() {
        if (getActivity() == null) return null;
        SharedPreferences sp = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(KEY_PROFILE_ID, null);
    }
    
    private void confirmAndDelete() {
        String id = getSavedDocId();
        if (id == null) {
            Toast.makeText(getContext(), "No profile to delete.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (getContext() == null) return;
        
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete profile?")
                .setMessage("This will permanently remove your profile from the database.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteProfileById(id))
                .show();
    }
    
    private void deleteProfileById(String id) {
        db.collection("profiles").document(id).delete()
                .addOnSuccessListener(v -> {
                    if (getContext() == null) return;
                    
                    // Clear local state
                    SharedPreferences sp = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                    sp.edit().remove(KEY_PROFILE_ID).apply();
                    
                    // Clear UI
                    tvFragmentName.setText("No profile yet");
                    tvFragmentEmail.setText("—");
                    tvFragmentPhone.setText("—");
                    ivFragmentProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                    
                    Toast.makeText(getContext(), "Profile deleted successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    @Override
    public void onSaveProfile(Profile profile) {
        if (getContext() == null) return;
        
        // Ensure device ID is set
        if (profile.getDeviceId() == null || profile.getDeviceId().isEmpty()) {
            profile.setDeviceId(getOrCreateDeviceId());
        }
        
        String existingId = getSavedDocId();
        if (existingId == null) {
            // Create new profile with sequential numeric id via transaction
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
                        renderProfile(profile);
                        Toast.makeText(getContext(), "Profile saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
    
    private void createProfileWithAutoId(Profile profile) {
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentReference counterRef = db.collection("meta").document("profiles_counter");
            com.google.firebase.firestore.DocumentSnapshot snap = transaction.get(counterRef);

            long next;
            boolean existed = snap.exists();
            if (existed) {
                Long val = snap.getLong("next");
                next = (val != null) ? val : 0L;
            } else {
                next = 0L;
            }

            String docId = String.valueOf(next);
            com.google.firebase.firestore.DocumentReference profileRef = db.collection("profiles").document(docId);

            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("name", profile.getName());
            data.put("email", profile.getEmail());
            data.put("phone", profile.getPhone());
            data.put("deviceId", profile.getDeviceId());
            data.put("profilePictureUrl", profile.getProfilePictureUrl());
            data.put("uid", next);
            transaction.set(profileRef, data);

            if (existed) {
                transaction.update(counterRef, "next", next + 1L);
            } else {
                java.util.Map<String, Object> counterInit = new java.util.HashMap<>();
                counterInit.put("next", next + 1L);
                transaction.set(counterRef, counterInit);
            }

            return docId;
        }).addOnSuccessListener(assignedId -> {
            if (getContext() == null) return;
            saveDocId(assignedId);
            currentProfile = profile;
            renderProfile(profile);
            Toast.makeText(getContext(), "Created user #" + assignedId, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Create failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void saveDocId(String id) {
        if (getActivity() == null) return;
        getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_PROFILE_ID, id).apply();
    }
    
    private String getOrCreateDeviceId() {
        if (getActivity() == null) return java.util.UUID.randomUUID().toString();
        
        SharedPreferences sp = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String deviceId = sp.getString("device_id", null);
        
        if (deviceId == null) {
            try {
                deviceId = android.provider.Settings.Secure.getString(
                    getActivity().getContentResolver(), 
                    android.provider.Settings.Secure.ANDROID_ID
                );
            } catch (Exception e) {
                deviceId = null;
            }
            
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = java.util.UUID.randomUUID().toString();
            }
            
            sp.edit().putString("device_id", deviceId).apply();
        }
        
        return deviceId;
    }
}