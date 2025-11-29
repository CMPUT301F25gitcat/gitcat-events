package com.example.gitcat_events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gitcat_events.core.model.Notif;
import com.example.gitcat_events.core.model.Profile;
import com.example.gitcat_events.features.entrant.ui.ProfileActivity;
import com.example.gitcat_events.features.entrant.ui.ProfileDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fragment to display user profile information
 */
public class ProfileFragment extends Fragment implements ProfileDialogFragment.OnSaveProfileListener {

    private static final String TAG = "ProfileFragment";
    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id";
    private static final String KEY_IS_ADMIN = "is_admin";

    private FirebaseFirestore db;
    private TextView tvFragmentName, tvFragmentEmail, tvFragmentPhone;
    private ImageView ivFragmentProfilePicture;
    private Button btnFragmentViewFullProfile, btnFragmentDeleteProfile, adminBtn;
    private Profile currentProfile;
    private Button notificationsToggle;
    private CompoundButton.OnCheckedChangeListener notificationsToggleListener;

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
        adminBtn = view.findViewById(R.id.adminMenuBtn);
        notificationsToggle = view.findViewById(R.id.notificationToggleButton);
        String deviceId = getOrCreateDeviceId();
        db.collection("profiles").whereEqualTo("deviceId", deviceId).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
            if (!queryDocumentSnapshots1.isEmpty()) {
                String uid = queryDocumentSnapshots1.getDocuments().get(0).getId();
                DocumentReference profileRef = db.collection("profiles").document(uid);
                db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(profileRef);
                    if (!snapshot.contains("hasNotificationsEnabled")) {
                        profileRef.update("hasNotificationsEnabled", "true");
                        notificationsToggle.setText("Toggle Notifications (On)");
                    } else if (snapshot.getString("hasNotificationsEnabled").equals("true")){
                        notificationsToggle.setText("Toggle Notifications (On)");
                    } else{
                        notificationsToggle.setText("Toggle Notifications (Off)");
                    }
                    return notificationsToggle;
                });
            }});
        notificationsToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("profiles").whereEqualTo("deviceId", deviceId).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    if (!queryDocumentSnapshots1.isEmpty()) {
                        String uid = queryDocumentSnapshots1.getDocuments().get(0).getId();
                        DocumentReference profileRef = db.collection("profiles").document(uid);

                        String currentText = notificationsToggle.getText().toString();
                        if (currentText.contains("(On)")) {
                            profileRef.update("hasNotificationsEnabled", "false");
                            notificationsToggle.setText("Toggle Notifications (Off)");
                        } else {
                            profileRef.update("hasNotificationsEnabled", "true");
                            notificationsToggle.setText("Toggle Notifications (On)");
                        }
                    }
                });
            }
        });






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
            // No profile yet, redirect to setup page
            Intent intent = new Intent(getActivity(), SetupProfileActivity.class);
            startActivity(intent);
            return;
        }

        // Fetch profile from Firestore
        db.collection("profiles").document(profileId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Profile profile = documentSnapshot.toObject(Profile.class);
                    if (profile != null && isAdded()) {
                        currentProfile = profile;
                        saveIsAdmin(currentProfile.isAdmin());
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
        // Handle optional name
        String name = profile.getName();
        tvFragmentName.setText((name == null || name.trim().isEmpty()) ? "Anonymous User" : name);
        
        // Handle optional email
        String email = profile.getEmail();
        tvFragmentEmail.setText((email == null || email.trim().isEmpty()) ? "No email provided" : email);
        
        // Handle optional phone
        String phone = profile.getPhone();
        tvFragmentPhone.setText((phone == null || phone.trim().isEmpty()) ? "—" : phone);

        // Load profile picture from Base64
        if (profile.getProfilePictureUrl() != null && !profile.getProfilePictureUrl().isEmpty()) {
            loadBase64Image(profile.getProfilePictureUrl(), ivFragmentProfilePicture);
        } else {
            ivFragmentProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // show admin menu button if user is an admin
        if(profile.isAdmin()){
            adminBtn.setVisibility(View.VISIBLE);
            adminBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AdminMenuActivity.class);
                startActivity(intent);
            });
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
        
        new AlertDialog.Builder(getContext())
                .setTitle("Delete profile?")
                .setMessage("This will permanently remove:\n• Your profile\n• All your created events\n• All waitlist entries for those events\n• Your entries in other events")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteProfileById(id))
                .show();
    }
    
    private void deleteProfileById(String id) {
        // Delete profile with cascade: events → waitlist entries for those events, and waitlist entries by this user
        String deviceId = getOrCreateDeviceId();
        
        // Step 1: Delete all events created by this device AND their waitlist entries
        db.collection("events")
                .whereEqualTo("organizerDeviceId", deviceId)
                .get()
                .addOnSuccessListener(eventSnapshot -> {
                    WriteBatch batch = db.batch();
                    
                    // For each event, delete its waitlist subcollection entries first
                    int eventsCount = eventSnapshot.size();
                    final int[] processedEvents = {0};
                    
                    if (eventsCount == 0) {
                        // No events to delete, move to step 2
                        deleteUserWaitlistEntries(id, deviceId);
                        return;
                    }
                    
                    for (QueryDocumentSnapshot eventDoc : eventSnapshot) {
                        String eventId = eventDoc.getId();
                        
                        // Delete waitlist entries for this event
                        db.collection("events").document(eventId)
                                .collection("waitlist")
                                .get()
                                .addOnSuccessListener(waitlistSnapshot -> {
                                    for (QueryDocumentSnapshot waitlistDoc : waitlistSnapshot) {
                                        batch.delete(waitlistDoc.getReference());
                                    }
                                    
                                    processedEvents[0]++;
                                    
                                    // When all events processed, delete the events themselves
                                    if (processedEvents[0] == eventsCount) {
                                        for (QueryDocumentSnapshot doc : eventSnapshot) {
                                            batch.delete(doc.getReference());
                                        }
                                        
                                        batch.commit().addOnSuccessListener(v -> {
                                            // Step 2: Delete user's waitlist entries in OTHER events
                                            deleteUserWaitlistEntries(id, deviceId);
                                        }).addOnFailureListener(e -> {
                                            if (getContext() != null) {
                                                Toast.makeText(getContext(), "Failed to delete events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to find events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void deleteUserWaitlistEntries(String profileId, String deviceId) {
        // Delete all waitlist entries by this user (in events they didn't organize)
        db.collectionGroup("waitlist")
                .whereEqualTo("userDeviceId", deviceId)
                .get()
                .addOnSuccessListener(waitlistSnapshot -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : waitlistSnapshot) {
                        batch.delete(doc.getReference());
                    }
                    
                    batch.commit().addOnSuccessListener(v -> {
                        // Finally, delete the profile
                        deleteProfileDocument(profileId);
                    }).addOnFailureListener(e -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to delete waitlist entries: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to find waitlist entries: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void deleteProfileDocument(String id) {
        db.collection("profiles").document(id).delete()
                .addOnSuccessListener(v -> {
                    if (getContext() == null || getActivity() == null) return;
                    
                    // Clear local state
                    SharedPreferences sp = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                    sp.edit().remove(KEY_PROFILE_ID).apply();
                    
                    // Clear current profile
                    currentProfile = null;
                    
                    Toast.makeText(getContext(), "Profile, events, and waitlist entries deleted successfully.", Toast.LENGTH_SHORT).show();
                    
                    // Redirect to setup page and clear activity stack
                    Intent intent = new Intent(getActivity(), SetupProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    
                    // Finish the current activity
                    getActivity().finish();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to delete profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

            Map<String, Object> data = new HashMap<>();
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
                Map<String, Object> counterInit = new HashMap<>();
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
        if (getActivity() == null) return UUID.randomUUID().toString();
        
        SharedPreferences sp = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String deviceId = sp.getString("device_id", null);
        
        if (deviceId == null) {
            try {
                deviceId = Settings.Secure.getString(
                    getActivity().getContentResolver(), 
                    Settings.Secure.ANDROID_ID
                );
            } catch (Exception e) {
                deviceId = null;
            }
            
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
            }
            
            sp.edit().putString("device_id", deviceId).apply();
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

    private void saveIsAdmin(Boolean isAdmin){
        requireContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_IS_ADMIN, isAdmin)
                .apply();
    }
}