package com.example.gitcat_events;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.features.event.ui.EventArrayAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventHistoryActivity extends AppCompatActivity {
    private static final String TAG = "EventHistoryActivity";

    // UI Components
    private ListView entriesList;
    private TextView entriesEmpty;
    private ImageButton btnBack;

    // Data & Firebase
    private EventArrayAdapter adapter;
    private ArrayList<Event> enteredEvents;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);

        db = FirebaseFirestore.getInstance();

        // Initializing
        entriesList = findViewById(R.id.entriesList);
        entriesEmpty = findViewById(R.id.entriesEmpty);
        btnBack = findViewById(R.id.btnBack);

        enteredEvents = new ArrayList<>();
        adapter = new EventArrayAdapter(this, enteredEvents);
        entriesList.setAdapter(adapter);

        //  get deviceId from intent
        String deviceId = getIntent().getStringExtra("deviceId");

        if (deviceId != null) {
            loadAllEntries(deviceId);
        } else {
            Log.e(TAG, "No Device ID passed to activity");
            Toast.makeText(this, "Error: User ID missing", Toast.LENGTH_SHORT).show();
        }

        // back button
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadAllEntries(String deviceId) {

        // get invited, accepted, waitlisted, and canceled events
        Task<QuerySnapshot> taskInvites = db.collectionGroup("invitation_list")
                .whereEqualTo("userDeviceId", deviceId).get();

        Task<QuerySnapshot> taskAccepted = db.collectionGroup("acceptedList")
                .whereEqualTo("userDeviceId", deviceId).get();

        Task<QuerySnapshot> taskWaitlist = db.collectionGroup("waitlist")
                .whereEqualTo("userDeviceId", deviceId).get();

        /*currently unimplemented: canceled_events requires a COLLECTION_GROUP_ASC index
        Task<QuerySnapshot> taskCancelled = db.collectionGroup("cancelled_list")
                .whereEqualTo("userDeviceId", deviceId).get();*/

        // Wait for ALL tasks to complete
        Tasks.whenAllSuccess(taskInvites, taskAccepted, taskWaitlist)
                .addOnSuccessListener(results -> {


                    if (isDestroyed() || isFinishing()) return;

                    Set<String> uniqueEventIds = new HashSet<>();

                    // 'results' is a list containing the result of each task in order
                    List<QuerySnapshot> snapshots = (List<QuerySnapshot>) (List<?>) results;

                    for (QuerySnapshot snapshot : snapshots) {
                        if (snapshot != null) {
                            for (QueryDocumentSnapshot doc : snapshot) {
                                String eventId = doc.getString("eventId");
                                if (eventId != null) {
                                    uniqueEventIds.add(eventId);
                                }
                            }
                        }
                    }

                    Log.d(TAG, "Total unique events found: " + uniqueEventIds.size());

                    if (uniqueEventIds.isEmpty()) {
                        updateUIState(); // Show empty state
                    } else {
                        loadEventDetails(uniqueEventIds);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying lists", e);
                    Toast.makeText(EventHistoryActivity.this, "Failed to load lists. Check Logs for Index link.", Toast.LENGTH_LONG).show();
                });
    }

    private void loadEventDetails(Set<String> eventIds) {
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        // Create a fetch task for every single event ID found
        for (String id : eventIds) {
            tasks.add(db.collection("events").document(id).get());
        }

        // Wait for all individual event fetches to complete
        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(objects -> {
                    if (isDestroyed() || isFinishing()) return;

                    enteredEvents.clear();

                    // Convert results
                    List<DocumentSnapshot> snapshots = (List<DocumentSnapshot>) (List<?>) objects;

                    for (DocumentSnapshot doc : snapshots) {
                        if (doc.exists()) {
                            Event event = parseEvent(doc);
                            if (event != null) {
                                enteredEvents.add(event);
                            }
                        }
                    }

                    // Sort events (Newest Date first)
                    if (!enteredEvents.isEmpty()) {
                        enteredEvents.sort((e1, e2) -> {
                            if (e1.getEventDate() == null || e2.getEventDate() == null) return 0;
                            return e2.getEventDate().compareTo(e1.getEventDate());
                        });
                    }

                    updateUIState();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading event details", e);
                });
    }

    private void updateUIState() {
        if (enteredEvents.isEmpty()) {
            entriesEmpty.setVisibility(View.VISIBLE);
            entriesList.setVisibility(View.GONE);
        } else {
            entriesEmpty.setVisibility(View.GONE);
            entriesList.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private Event parseEvent(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        try {
            Event event = new Event();
            event.setDocumentId(document.getId());
            event.setName(document.getString("name"));
            event.setDescription(document.getString("description"));

            Long capacity = document.getLong("capacity");
            event.setCapacity(capacity != null ? capacity.intValue() : 0);

            Long maxWaitlist = document.getLong("maxWaitListSize");
            event.setMaxWaitListSize(maxWaitlist != null ? maxWaitlist.intValue() : null);

            event.setPoster(document.getString("poster"));
            event.setOrganizerDeviceId(document.getString("organizerDeviceId"));
            event.setSelectionCriteria(document.getString("selectionCriteria"));

            Boolean geoLocation = document.getBoolean("geoLocationRequired");
            event.setGeoLocationRequired(geoLocation != null ? geoLocation : false);

            Date registrationStartDate = document.getDate("registrationStartDate");
            if (registrationStartDate != null) {
                Calendar regStartCal = Calendar.getInstance();
                regStartCal.setTime(registrationStartDate);
                event.setRegistrationStartDate(regStartCal);
            }
            
            Date eventDate = document.getDate("eventDate");
            if (eventDate != null) {
                Calendar evtCal = Calendar.getInstance();
                evtCal.setTime(eventDate);
                event.setEventDate(evtCal);
            } else {
                // Fallback to current date if missing
                event.setEventDate(Calendar.getInstance());
            }

            return event;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing event", e);
            return null;
        }
    }
}
