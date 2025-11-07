package com.example.gitcat_events;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.core.model.WaitListEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EventDetails extends Fragment {

    private static final String TAG = "EventDetailsFragment";
    private static final String PREFS = "app_prefs";
    
    private Event event;
    private FirebaseFirestore db;
    private ListenerRegistration waitlistListener;
    private boolean isOnWaitlist = false;
    
    private ImageView ivEventPoster;
    private TextView tvEventName, tvEventDate, tvEventSpots, tvEventDesc;
    private TextView tvWaitingListCount, tvStatusMessage;
    private Button btnJoinWaitingList, btnBack;

    public EventDetails() {}

    public static EventDetails newInstance(Event event) {
        EventDetails fragment = new EventDetails();
        Bundle args = new Bundle();
        args.putSerializable("event", event); // Event must implement Serializable
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        // Initialize views
        ivEventPoster = view.findViewById(R.id.ivEventPoster);
        tvEventName = view.findViewById(R.id.eventDetailsName);
        tvEventDate = view.findViewById(R.id.eventDetailsDate);
        tvEventSpots = view.findViewById(R.id.eventDetailsSpots);
        tvEventDesc = view.findViewById(R.id.eventDetailsDesc);
        tvWaitingListCount = view.findViewById(R.id.eventDetailsWaitinglist);
        tvStatusMessage = view.findViewById(R.id.tvStatusMessage);
        btnJoinWaitingList = view.findViewById(R.id.btnJoinWaitingList);
        btnBack = view.findViewById(R.id.eventDetailsBackBtn);

        // Display event details
        displayEvent();
        
        // Set up real-time waiting list count listener
        setupWaitlistListener();
        
        // Check if user is already on the waiting list
        checkWaitlistStatus();

        // Set up join/leave button
        btnJoinWaitingList.setOnClickListener(v -> handleWaitlistAction());

        // Set up back button
        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.slide_in_left,
                            android.R.anim.fade_out
                    )
                    .remove(EventDetails.this)
                    .commit();
        });

        return view;
    }

    private void displayEvent() {
        if (event == null) return;
        
        tvEventName.setText(event.getName());
        tvEventDesc.setText(event.getDescription() != null ? event.getDescription() : "No description");
        
        // Format and display dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        if (event.getEventDate() != null) {
            tvEventDate.setText("Event: " + dateFormat.format(event.getEventDate().getTime()));
        }
        
        tvEventSpots.setText("Capacity: " + event.getCapacity());
        
        // Load poster image
        if (event.getPoster() != null && !event.getPoster().isEmpty()) {
            loadBase64Image(event.getPoster(), ivEventPoster);
        }
        
        // Check if user is the organizer
        String currentDeviceId = getOrCreateDeviceId();
        if (event.getOrganizerDeviceId() != null && 
                event.getOrganizerDeviceId().equals(currentDeviceId)) {
            // User is the organizer, disable join button
            btnJoinWaitingList.setEnabled(false);
            btnJoinWaitingList.setText("You're the Organizer");
            if (tvStatusMessage != null) {
                tvStatusMessage.setVisibility(View.VISIBLE);
                tvStatusMessage.setText("You're the organizer of this event");
                tvStatusMessage.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        }
    }
    
    private void checkWaitlistStatus() {
        if (event == null || event.getDocumentId() == null) return;
        
        String deviceId = getOrCreateDeviceId();
        
        db.collection("events").document(event.getDocumentId())
                .collection("waitlist")
                .document(deviceId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error checking waitlist status", error);
                        return;
                    }
                    
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // User is on the waiting list
                        isOnWaitlist = true;
                        updateButtonForWaitlistStatus();
                    } else {
                        // User is not on the waiting list
                        isOnWaitlist = false;
                        updateButtonForWaitlistStatus();
                    }
                });
    }
    
    private void updateButtonForWaitlistStatus() {
        // Don't update if user is the organizer
        if (event != null && event.getOrganizerDeviceId() != null && 
                event.getOrganizerDeviceId().equals(getOrCreateDeviceId())) {
            return;
        }
        
        if (isOnWaitlist) {
            btnJoinWaitingList.setText("Leave Waiting List");
            btnJoinWaitingList.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
            if (tvStatusMessage != null) {
                tvStatusMessage.setVisibility(View.VISIBLE);
                tvStatusMessage.setText("You're on the waiting list");
                tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        } else {
            btnJoinWaitingList.setText("Join Waiting List");
            btnJoinWaitingList.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
            if (tvStatusMessage != null) {
                tvStatusMessage.setVisibility(View.GONE);
            }
        }
    }
    
    private void handleWaitlistAction() {
        if (isOnWaitlist) {
            confirmLeaveWaitlist();
        } else {
            joinWaitingList();
        }
    }
    
    private void confirmLeaveWaitlist() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Leave Waiting List?")
                .setMessage("Are you sure you want to leave the waiting list for this event?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Leave", (dialog, which) -> leaveWaitingList())
                .show();
    }
    
    private void leaveWaitingList() {
        if (event == null || event.getDocumentId() == null) return;
        
        String deviceId = getOrCreateDeviceId();
        
        db.collection("events").document(event.getDocumentId())
                .collection("waitlist")
                .document(deviceId)
                .delete()
                .addOnSuccessListener(v -> {
                    showSuccess("Successfully left the waiting list");
                    isOnWaitlist = false;
                    updateButtonForWaitlistStatus();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to leave: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupWaitlistListener() {
        if (event == null || event.getDocumentId() == null) return;
        
        waitlistListener = db.collection("events").document(event.getDocumentId())
                .collection("waitlist")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to waitlist", error);
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        int count = querySnapshot.size();
                        if (event.getMaxWaitListSize() != null) {
                            tvWaitingListCount.setText("Waiting List: " + count + " / " + event.getMaxWaitListSize());
                        } else {
                            tvWaitingListCount.setText("Waiting List: " + count);
                        }
                    }
                });
    }

    private void joinWaitingList() {
        if (event == null || event.getDocumentId() == null) return;
        
        // Check if user is the organizer (double check)
        String deviceId = getOrCreateDeviceId();
        if (event.getOrganizerDeviceId() != null && 
                event.getOrganizerDeviceId().equals(deviceId)) {
            showError("You cannot join your own event.");
            return;
        }
        
        // Check if registration is still open
        if (event.getRaffleDate() != null) {
            Calendar now = Calendar.getInstance();
            if (now.after(event.getRaffleDate())) {
                showError("Registration has closed for this event.");
                return;
            }
        }
        
        // Check if already on waitlist
        db.collection("events").document(event.getDocumentId())
                .collection("waitlist")
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        showError("You're already on the waiting list.");
                        return;
                    }
                    
                    // Check if waitlist is full (if max size is set)
                    if (event.getMaxWaitListSize() != null) {
                        checkWaitlistCapacityAndJoin(deviceId);
                    } else {
                        addToWaitlist(deviceId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error checking waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkWaitlistCapacityAndJoin(String deviceId) {
        if (event == null || event.getDocumentId() == null) return;
        
        db.collection("events").document(event.getDocumentId())
                .collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int currentSize = querySnapshot.size();
                    if (event.getMaxWaitListSize() != null && 
                            currentSize >= event.getMaxWaitListSize()) {
                        showError("Waiting list is full (capacity: " + event.getMaxWaitListSize() + ")");
                    } else {
                        addToWaitlist(deviceId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error checking capacity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addToWaitlist(String deviceId) {
        if (event == null || event.getDocumentId() == null) return;
        
        WaitListEntry entry = new WaitListEntry(event.getDocumentId(), deviceId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", entry.getEventId());
        data.put("userDeviceId", entry.getUserDeviceId());
        data.put("timestamp", System.currentTimeMillis());

        db.collection("events").document(event.getDocumentId())
                .collection("waitlist")
                .document(deviceId)
                .set(data)
                .addOnSuccessListener(v -> {
                    showSuccess("Successfully joined the waiting list!");
                    isOnWaitlist = true;
                    updateButtonForWaitlistStatus();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showError(String message) {
        if (tvStatusMessage != null) {
            tvStatusMessage.setText(message);
            tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            tvStatusMessage.setVisibility(View.VISIBLE);
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        if (tvStatusMessage != null) {
            tvStatusMessage.setText(message);
            tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvStatusMessage.setVisibility(View.VISIBLE);
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loadBase64Image(String base64String, ImageView imageView) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error decoding Base64 image", e);
        }
    }

    private String getOrCreateDeviceId() {
        SharedPreferences sp = requireActivity().getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE);
        String deviceId = sp.getString("device_id", null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            sp.edit().putString("device_id", deviceId).apply();
        }
        return deviceId;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister listener to prevent memory leaks
        if (waitlistListener != null) {
            waitlistListener.remove();
        }
    }
}
