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
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment to display user profile information
 */
public class ProfileFragment extends Fragment {

    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id";

    private FirebaseFirestore db;
    private TextView tvFragmentName, tvFragmentEmail, tvFragmentPhone;
    private ImageView ivFragmentProfilePicture;
    private Button btnFragmentViewFullProfile;

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

        // Setup button to open full profile activity
        btnFragmentViewFullProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

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
                        renderProfile(profile);
                    } else if (isAdded()) {
                        tvFragmentName.setText("Profile not found");
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
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
}