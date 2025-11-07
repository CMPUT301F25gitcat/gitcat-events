package com.example.gitcat_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
<<<<<<< HEAD
import android.provider.Settings;
=======
>>>>>>> 707c31c (chore: fixing rebase mishap)
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

<<<<<<< HEAD
=======
import com.bumptech.glide.Glide;
>>>>>>> 707c31c (chore: fixing rebase mishap)
import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.core.model.WaitListEntry;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
<<<<<<< HEAD
import java.util.Date;
=======
>>>>>>> 707c31c (chore: fixing rebase mishap)
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailsActivity";
    private static final String PREFS = "app_prefs";
    
    private FirebaseFirestore db;
    private ImageView ivEventDetailsPoster;
    private TextView tvEventDetailsName, tvEventDetailsDescription, tvEventDetailsDate;
    private TextView tvEventDetailsRaffleDate, tvEventDetailsCapacity, tvWaitingListCount;
    private TextView tvEventDetailsGeolocation, tvStatusMessage;
    private Button btnJoinWaitingList, btnBackToHome;
    
    private String eventId;
    private Event currentEvent;
    private ListenerRegistration waitlistListener;
    private boolean isOnWaitlist = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

<<<<<<< HEAD
        db = FirebaseFirestore.getInstance();

        // Get event ID from intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
=======
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get event ID from Intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
>>>>>>> 707c31c (chore: fixing rebase mishap)
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        ivEventDetailsPoster = findViewById(R.id.ivEventDetailsPoster);
        tvEventDetailsName = findViewById(R.id.tvEventDetailsName);
        tvEventDetailsDescription = findViewById(R.id.tvEventDetailsDescription);
        tvEventDetailsDate = findViewById(R.id.tvEventDetailsDate);
        tvEventDetailsRaffleDate = findViewById(R.id.tvEventDetailsRaffleDate);
        tvEventDetailsCapacity = findViewById(R.id.tvEventDetailsCapacity);
        tvWaitingListCount = findViewById(R.id.tvWaitingListCount);
        tvEventDetailsGeolocation = findViewById(R.id.tvEventDetailsGeolocation);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        btnJoinWaitingList = findViewById(R.id.btnJoinWaitingList);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        // Load event details
        loadEventDetails();
        
        // Set up real-time waiting list count listener
        setupWaitlistListener();

        // Set up join/leave button
        btnJoinWaitingList.setOnClickListener(v -> handleWaitlistAction());
        
        // Set up back button
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        
        // Check if user is already on the waiting list
        checkWaitlistStatus();
    }

    private void loadEventDetails() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
<<<<<<< HEAD
                        parseAndDisplayEvent(documentSnapshot);
=======
                        currentEvent = parseEvent(documentSnapshot);
                        if (currentEvent != null) {
                            currentEvent.setDocumentId(documentSnapshot.getId());
                            displayEvent(currentEvent);
                        } else {
                            Toast.makeText(this, "Error parsing event data", Toast.LENGTH_SHORT).show();
                            finish();
                        }
>>>>>>> 707c31c (chore: fixing rebase mishap)
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
<<<<<<< HEAD
                    Toast.makeText(this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
=======
                    Toast.makeText(this, "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
>>>>>>> 707c31c (chore: fixing rebase mishap)
                    finish();
                });
    }

<<<<<<< HEAD
    private void parseAndDisplayEvent(DocumentSnapshot document) {
        currentEvent = new Event();
        currentEvent.setName(document.getString("name"));
        currentEvent.setDescription(document.getString("description"));
        
        Long capacity = document.getLong("capacity");
        currentEvent.setCapacity(capacity != null ? capacity.intValue() : 0);
        
        Long maxWaitlist = document.getLong("maxWaitListSize");
        currentEvent.setMaxWaitListSize(maxWaitlist != null ? maxWaitlist.intValue() : null);
        
        currentEvent.setPoster(document.getString("poster"));
        currentEvent.setOrganizerDeviceId(document.getString("organizerDeviceId"));
        
        Boolean geoLocation = document.getBoolean("geoLocationRequired");
        currentEvent.setGeoLocationRequired(geoLocation != null ? geoLocation : false);
        
        // Convert Date to Calendar
        Date eventDate = document.getDate("eventDate");
        if (eventDate != null) {
            Calendar eventCal = Calendar.getInstance();
            eventCal.setTime(eventDate);
            currentEvent.setEventDate(eventCal);
        }
        
        Date raffleDate = document.getDate("raffleDate");
        if (raffleDate != null) {
            Calendar raffleCal = Calendar.getInstance();
            raffleCal.setTime(raffleDate);
            currentEvent.setRaffleDate(raffleCal);
        }
        
        // Display event details
        displayEvent();
    }

    private void displayEvent() {
        tvEventDetailsName.setText(currentEvent.getName());
        tvEventDetailsDescription.setText(currentEvent.getDescription());
        tvEventDetailsCapacity.setText(String.valueOf(currentEvent.getCapacity()));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        
        if (currentEvent.getEventDate() != null) {
            tvEventDetailsDate.setText(dateFormat.format(currentEvent.getEventDate().getTime()));
        }
        
        if (currentEvent.getRaffleDate() != null) {
            tvEventDetailsRaffleDate.setText(dateFormat.format(currentEvent.getRaffleDate().getTime()));
        }
        
        tvEventDetailsGeolocation.setText(currentEvent.getGeoLocationRequired() ? "Required" : "Not required");
        
        // Load poster
        if (currentEvent.getPoster() != null && !currentEvent.getPoster().isEmpty()) {
            loadBase64Image(currentEvent.getPoster(), ivEventDetailsPoster);
=======
    private Event parseEvent(DocumentSnapshot doc) {
        try {
            Event event = new Event();
            event.setName(doc.getString("name"));
            event.setDescription(doc.getString("description"));
            event.setCapacity(doc.getLong("capacity") != null ? doc.getLong("capacity").intValue() : 0);
            
            Long maxWaitlistSize = doc.getLong("maxWaitListSize");
            event.setMaxWaitListSize(maxWaitlistSize != null ? maxWaitlistSize.intValue() : null);
            
            event.setPoster(doc.getString("poster"));
            event.setOrganizerDeviceId(doc.getString("organizerDeviceId"));
            
            // Parse dates
            com.google.firebase.Timestamp eventTimestamp = doc.getTimestamp("eventDate");
            if (eventTimestamp != null) {
                Calendar eventCal = Calendar.getInstance();
                eventCal.setTime(eventTimestamp.toDate());
                event.setEventDate(eventCal);
            }
            
            com.google.firebase.Timestamp raffleTimestamp = doc.getTimestamp("raffleDate");
            if (raffleTimestamp != null) {
                Calendar raffleCal = Calendar.getInstance();
                raffleCal.setTime(raffleTimestamp.toDate());
                event.setRaffleDate(raffleCal);
            }
            
            Boolean geoRequired = doc.getBoolean("geoLocationRequired");
            event.setGeoLocationRequired(geoRequired != null ? geoRequired : false);
            
            return event;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing event", e);
            return null;
        }
    }

    private void displayEvent(Event event) {
        tvEventDetailsName.setText(event.getName());
        tvEventDetailsDescription.setText(event.getDescription() != null ? event.getDescription() : "No description available");
        
        // Format and display dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        if (event.getEventDate() != null) {
            tvEventDetailsDate.setText("Event Date: " + dateFormat.format(event.getEventDate().getTime()));
        }
        if (event.getRaffleDate() != null) {
            tvEventDetailsRaffleDate.setText("Registration Deadline: " + dateFormat.format(event.getRaffleDate().getTime()));
        }
        
        tvEventDetailsCapacity.setText("Capacity: " + event.getCapacity());
        tvEventDetailsGeolocation.setText("Geolocation Required: " + (event.getGeoLocationRequired() ? "Yes" : "No"));
        
        // Load poster image
        if (event.getPoster() != null && !event.getPoster().isEmpty()) {
            loadBase64Image(event.getPoster(), ivEventDetailsPoster);
>>>>>>> 707c31c (chore: fixing rebase mishap)
        } else {
            ivEventDetailsPoster.setImageResource(R.drawable.ic_launcher_foreground);
        }
        
        // Check if user is the organizer
        String currentDeviceId = getOrCreateDeviceId();
        if (currentEvent.getOrganizerDeviceId() != null && 
                currentEvent.getOrganizerDeviceId().equals(currentDeviceId)) {
            // User is the organizer, disable join button
            btnJoinWaitingList.setEnabled(false);
            btnJoinWaitingList.setText("You're the Organizer");
            tvStatusMessage.setVisibility(android.view.View.VISIBLE);
            tvStatusMessage.setText("You're the organizer of this event");
            tvStatusMessage.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }
    
    private void checkWaitlistStatus() {
        String deviceId = getOrCreateDeviceId();
        
        db.collection("events").document(eventId)
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
        if (currentEvent != null && currentEvent.getOrganizerDeviceId() != null && 
                currentEvent.getOrganizerDeviceId().equals(getOrCreateDeviceId())) {
            return;
        }
        
        if (isOnWaitlist) {
            btnJoinWaitingList.setText("Leave Waiting List");
            btnJoinWaitingList.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
            tvStatusMessage.setVisibility(android.view.View.VISIBLE);
            tvStatusMessage.setText("You're on the waiting list");
            tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            btnJoinWaitingList.setText("Join Waiting List");
            btnJoinWaitingList.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
            tvStatusMessage.setVisibility(android.view.View.GONE);
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
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Leave Waiting List?")
                .setMessage("Are you sure you want to leave the waiting list for this event?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Leave", (dialog, which) -> leaveWaitingList())
                .show();
    }
    
    private void leaveWaitingList() {
        String deviceId = getOrCreateDeviceId();
        
        db.collection("events").document(eventId)
                .collection("waitlist")
                .document(deviceId)
                .delete()
                .addOnSuccessListener(v -> {
                    showSuccess("Successfully left the waiting list");
                    isOnWaitlist = false;
                    updateButtonForWaitlistStatus();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to leave: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupWaitlistListener() {
        waitlistListener = db.collection("events").document(eventId)
                .collection("waitlist")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to waitlist", error);
                        return;
                    }
                    
                    if (querySnapshot != null) {
                        int count = querySnapshot.size();
<<<<<<< HEAD
                        tvWaitingListCount.setText(String.valueOf(count));
                        
                        // Update max waitlist display if set
                        if (currentEvent != null && currentEvent.getMaxWaitListSize() != null) {
                            tvWaitingListCount.setText(count + " / " + currentEvent.getMaxWaitListSize());
                        }
=======
                        tvWaitingListCount.setText("Waiting List: " + count + 
                                (currentEvent != null && currentEvent.getMaxWaitListSize() != null ? 
                                "/" + currentEvent.getMaxWaitListSize() : ""));
>>>>>>> 707c31c (chore: fixing rebase mishap)
                    }
                });
    }

    private void joinWaitingList() {
<<<<<<< HEAD
        if (currentEvent == null) {
            Toast.makeText(this, "Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String deviceId = getOrCreateDeviceId();
        
        // Check if user is the organizer
=======
        // Check if user is the organizer (double check)
        String deviceId = getOrCreateDeviceId();
>>>>>>> 707c31c (chore: fixing rebase mishap)
        if (currentEvent.getOrganizerDeviceId() != null && 
                currentEvent.getOrganizerDeviceId().equals(deviceId)) {
            showError("You cannot join your own event.");
            return;
        }
<<<<<<< HEAD

        // Check if registration is still open (before raffle date)
        Calendar now = Calendar.getInstance();
        if (currentEvent.getRaffleDate() != null && now.after(currentEvent.getRaffleDate())) {
            showError("Registration is closed. The deadline has passed.");
            return;
        }

        // Check if already joined
        db.collection("events").document(eventId)
                .collection("waitlist")
                .whereEqualTo("userDeviceId", deviceId)
                .get()
                .addOnSuccessListener(userQuerySnapshot -> {
                    if (!userQuerySnapshot.isEmpty()) {
                        showError("You're already on the waiting list!");
                        return;
                    }

                    // Check waitlist capacity (get total count)
                    db.collection("events").document(eventId)
                            .collection("waitlist")
                            .get()
                            .addOnSuccessListener(totalQuerySnapshot -> {
                                int currentWaitlistSize = totalQuerySnapshot.size();
                                if (currentEvent.getMaxWaitListSize() != null 
                                        && currentWaitlistSize >= currentEvent.getMaxWaitListSize()) {
                                    showError("Waiting list is full!");
                                    return;
                                }

                                // All checks passed, add to waitlist
                                addToWaitlist(deviceId);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error checking capacity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
=======
        
        // Check if registration is still open
        if (currentEvent.getRaffleDate() != null) {
            Calendar now = Calendar.getInstance();
            if (now.after(currentEvent.getRaffleDate())) {
                showError("Registration has closed for this event.");
                return;
            }
        }
        
        // Check if already on waitlist (shouldn't happen due to real-time check, but just in case)
        db.collection("events").document(eventId)
                .collection("waitlist")
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        showError("You're already on the waiting list.");
                        return;
                    }
                    
                    // Check if waitlist is full (if max size is set)
                    if (currentEvent.getMaxWaitListSize() != null) {
                        checkWaitlistCapacityAndJoin(deviceId);
                    } else {
                        addToWaitlist(deviceId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkWaitlistCapacityAndJoin(String deviceId) {
        db.collection("events").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int currentSize = querySnapshot.size();
                    if (currentEvent.getMaxWaitListSize() != null && 
                            currentSize >= currentEvent.getMaxWaitListSize()) {
                        showError("Waiting list is full (capacity: " + currentEvent.getMaxWaitListSize() + ")");
                    } else {
                        addToWaitlist(deviceId);
                    }
>>>>>>> 707c31c (chore: fixing rebase mishap)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addToWaitlist(String deviceId) {
        WaitListEntry entry = new WaitListEntry(eventId, deviceId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", entry.getEventId());
        data.put("userDeviceId", entry.getUserDeviceId());
        data.put("timestamp", System.currentTimeMillis());

        db.collection("events").document(eventId)
                .collection("waitlist")
                .document(deviceId) // Use deviceId as document ID to prevent duplicates
                .set(data)
                .addOnSuccessListener(v -> {
                    showSuccess("Successfully joined the waiting list!");
                    isOnWaitlist = true;
                    updateButtonForWaitlistStatus();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showError(String message) {
        tvStatusMessage.setText(message);
        tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        tvStatusMessage.setVisibility(android.view.View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        tvStatusMessage.setText(message);
        tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        tvStatusMessage.setVisibility(android.view.View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void loadBase64Image(String base64String, ImageView imageView) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
<<<<<<< HEAD
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode Base64 image", e);
=======
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding Base64 image", e);
>>>>>>> 707c31c (chore: fixing rebase mishap)
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private String getOrCreateDeviceId() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String deviceId = sp.getString("device_id", null);
<<<<<<< HEAD

        if (deviceId == null) {
            try {
                deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
                Log.w(TAG, "Failed to get Android ID", e);
            }

            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
            }
=======
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
>>>>>>> 707c31c (chore: fixing rebase mishap)
            sp.edit().putString("device_id", deviceId).apply();
        }
        return deviceId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister listener to prevent memory leaks
        if (waitlistListener != null) {
            waitlistListener.remove();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Navigate back to MainActivity (Home)
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}

