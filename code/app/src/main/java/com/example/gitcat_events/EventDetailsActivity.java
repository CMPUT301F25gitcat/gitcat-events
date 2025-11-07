package com.example.gitcat_events;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.core.model.WaitListEntry;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private Button btnJoinWaitingList;
    
    private String eventId;
    private Event currentEvent;
    private ListenerRegistration waitlistListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db = FirebaseFirestore.getInstance();

        // Get event ID from intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
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

        // Load event details
        loadEventDetails();
        
        // Set up real-time waiting list count listener
        setupWaitlistListener();

        // Set up join button
        btnJoinWaitingList.setOnClickListener(v -> joinWaitingList());
    }

    private void loadEventDetails() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        parseAndDisplayEvent(documentSnapshot);
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

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
            showError("You cannot join your own event.");
        }
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
                        tvWaitingListCount.setText(String.valueOf(count));
                        
                        // Update max waitlist display if set
                        if (currentEvent != null && currentEvent.getMaxWaitListSize() != null) {
                            tvWaitingListCount.setText(count + " / " + currentEvent.getMaxWaitListSize());
                        }
                    }
                });
    }

    private void joinWaitingList() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String deviceId = getOrCreateDeviceId();
        
        // Check if user is the organizer
        if (currentEvent.getOrganizerDeviceId() != null && 
                currentEvent.getOrganizerDeviceId().equals(deviceId)) {
            showError("You cannot join your own event.");
            return;
        }

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
                    btnJoinWaitingList.setEnabled(false);
                    btnJoinWaitingList.setText("Joined Waiting List");
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
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode Base64 image", e);
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private String getOrCreateDeviceId() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String deviceId = sp.getString("device_id", null);

        if (deviceId == null) {
            try {
                deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
                Log.w(TAG, "Failed to get Android ID", e);
            }

            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
            }
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
}

