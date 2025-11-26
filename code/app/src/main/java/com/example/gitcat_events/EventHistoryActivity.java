package com.example.gitcat_events;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.features.event.ui.EventArrayAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EventHistoryActivity extends AppCompatActivity {
    private static final String TAG = "HomeFragment";
    private static final String PREFS = "app_prefs";
    private EventArrayAdapter enteredEventsAdapter;
    private ListView entriesList;
    private TextView entriesEmpty;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private ArrayList<Event> enteredEvents;
    private String deviceID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);

        db = FirebaseFirestore.getInstance();

        entriesList = findViewById(R.id.entriesList);
        entriesEmpty = findViewById(R.id.entriesEmpty);
        btnBack = findViewById(R.id.btnBack);

        //set up entries list
        enteredEvents = new ArrayList<>();
        enteredEventsAdapter = new EventArrayAdapter(getApplicationContext(), enteredEvents);

        //show entries
        //view.post(() -> {
            //if (findViewById(android.R.id.content).getRootView() != null && isAdded()) {
                loadEvents();
            //}
        //});


        //close activity
        btnBack.setOnClickListener(v -> onBackPressed());
    }


    private void loadEvents() {
        if (findViewById(android.R.id.content).getRootView() == null) {
            Log.w(TAG, "Fragment view is null, skipping loadEvents");
            return;
        }

        String deviceId = getIntent().getStringExtra("deviceId");

        // Load events user has entered (accepted invitations)
        loadAllEntries(deviceId);
    }
    
    private void loadAllEntries(String deviceId) {
        if (findViewById(android.R.id.content).getRootView() == null) {
            Log.w(TAG, "Fragment view is null, skipping loadAllEntries");
            return;
        }

        if (enteredEvents == null) {
            enteredEvents = new ArrayList<>();
        }
        
        enteredEvents.clear();

        Task invited = db.collectionGroup("invitation_list").whereEqualTo("userDeviceId", deviceId).get();
        Task accepted = db.collectionGroup("acceptedList").whereEqualTo("userDeviceId", deviceId).get();
        Task cancelled = db.collectionGroup("cancelled_list").whereEqualTo("userDeviceId", deviceId).get();
        Task waitlisted = db.collectionGroup("waitlist").whereEqualTo("userDeviceId", deviceId).get();

        Task getHistory = Tasks.whenAllSuccess(invited, accepted, cancelled, waitlisted)
                .addOnSuccessListener(historySnapshot -> {
                    if (findViewById(android.R.id.content).getRootView() == null) {
                        Log.w(TAG, "Activity view destroyed during loadPendingInvitations");
                        return;
                    }

                    if (historySnapshot == null || historySnapshot.isEmpty()) {
                        updateAllEntriesUI();
                        return;
                    }

                    // Collect event IDs
                    Set<String> EventIds = new HashSet<>();
                    try {
                        for (Object doc : historySnapshot) {
                            System.out.println("HEY LOOK HERE!!!!");
                            System.out.println(doc.toString());
                            System.out.println(doc.getClass());
                            //String status = doc.getString("status");
                            /*String eventId = doc.getString("eventId");
                            // Only show events with "pending" status
                            if (eventId != null) {
                                EventIds.add(eventId);
                            }*/
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing history snapshot", e);
                        updateAllEntriesUI();
                        return;
                    }

                    if (EventIds.isEmpty()) {
                        updateAllEntriesUI();
                        return;
                    }

                    // Load event details for each event
                    final int[] completed = {0};
                    final int total = EventIds.size();

                    if (enteredEvents == null) {
                        enteredEvents = new ArrayList<>();
                    }

                    for (String eventId : EventIds) {
                        db.collection("events").document(eventId)
                                .get()
                                .addOnSuccessListener(eventDoc -> {
                                    if (findViewById(android.R.id.content).getRootView() == null) {
                                        return;
                                    }

                                    if (eventDoc != null && eventDoc.exists()) {
                                        try {
                                            Event event = parseEvent(eventDoc);
                                            if (event != null && enteredEvents != null) {
                                                enteredEvents.add(event);
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error parsing invitation event", e);
                                        }
                                    }

                                    completed[0]++;
                                    if (completed[0] == total && findViewById(android.R.id.content).getRootView() != null) {
                                        // Sort by event date
                                        try {
                                            if (enteredEvents != null) {
                                                enteredEvents.sort((e1, e2) -> {
                                                    try {
                                                        if (e1 == null || e2 == null) return 0;
                                                        if (e1.getEventDate() == null || e2.getEventDate() == null) return 0;
                                                        return e1.getEventDate().compareTo(e2.getEventDate());
                                                    } catch (Exception e) {
                                                        Log.e(TAG, "Error sorting pending invitations", e);
                                                        return 0;
                                                    }
                                                });
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error sorting pending invitations list", e);
                                        }
                                        updateAllEntriesUI();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error loading invitation event: " + eventId, e);
                                    completed[0]++;
                                    if (completed[0] == total && findViewById(android.R.id.content).getRootView() != null) {
                                        updateAllEntriesUI();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading pending invitations", e);
                    if (findViewById(android.R.id.content).getRootView() != null) {
                        updateAllEntriesUI();
                    }
                });
    }

    private void loadEventDetails(Set<String> eventIds) {
        if (findViewById(android.R.id.content).getRootView() == null) {
            Log.w(TAG, "Fragment view is null, skipping loadEventDetails");
            return;
        }

        if (eventIds == null || eventIds.isEmpty()) {
            updateAllEntriesUI();
            return;
        }

        final int[] completed = {0};
        final int total = eventIds.size();

        if (enteredEvents == null) {
            enteredEvents = new ArrayList<>();
        }
        enteredEvents.clear();

        for (String eventId : eventIds) {
            db.collection("events").document(eventId)
                    .get()
                    .addOnSuccessListener(eventDoc -> {
                        if (findViewById(android.R.id.content).getRootView() == null) {
                            return;
                        }

                        if (eventDoc != null && eventDoc.exists()) {
                            try {
                                Event event = parseEvent(eventDoc);
                                if (event != null && enteredEvents != null) {
                                    enteredEvents.add(event);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing entered event", e);
                            }
                        }

                        completed[0]++;
                        if (completed[0] == total && findViewById(android.R.id.content).getRootView() != null) {
                            // Sort by event date
                            try {
                                if (enteredEvents != null) {
                                    enteredEvents.sort((e1, e2) -> {
                                        try {
                                            if (e1 == null || e2 == null) return 0;
                                            if (e1.getEventDate() == null || e2.getEventDate() == null) return 0;
                                            return e1.getEventDate().compareTo(e2.getEventDate());
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error sorting entered events", e);
                                            return 0;
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error sorting entered events list", e);
                            }
                            updateAllEntriesUI();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading event: " + eventId, e);
                        completed[0]++;
                        if (completed[0] == total && findViewById(android.R.id.content).getRootView() != null) {
                            updateAllEntriesUI();
                        }
                    });
        }
    }

    private Event parseEvent(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            Log.w(TAG, "Attempted to parse null or non-existent document");
            return null;
        }

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

            // Convert Date to Calendar with null checks
            Date registrationStartDate = document.getDate("registrationStartDate");
            if (registrationStartDate != null) {
                Calendar regStartCal = Calendar.getInstance();
                regStartCal.setTime(registrationStartDate);
                event.setRegistrationStartDate(regStartCal);
            } else {
                event.setRegistrationStartDate(null);
            }

            Date eventDate = document.getDate("eventDate");
            if (eventDate != null) {
                Calendar eventCal = Calendar.getInstance();
                eventCal.setTime(eventDate);
                event.setEventDate(eventCal);
            } else {
                event.setEventDate(null);
            }

            Date raffleDate = document.getDate("raffleDate");
            if (raffleDate != null) {
                Calendar raffleCal = Calendar.getInstance();
                raffleCal.setTime(raffleDate);
                event.setRaffleDate(raffleCal);
            } else {
                event.setRaffleDate(null);
            }

            return event;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing event document: " + document.getId(), e);
            return null;
        }
    }

    private void updateAllEntriesUI() {
        if (findViewById(android.R.id.content).getRootView() == null) {
            Log.w(TAG, "Activity view is null, skipping updateEnteredEventsUI");
            return;
        }

        try {
            if (enteredEvents == null) {
                enteredEvents = new ArrayList<>();
            }

            if (enteredEvents.isEmpty()) {
                if (entriesEmpty != null) {
                    entriesEmpty.setVisibility(View.VISIBLE);
                }
                if (entriesList != null) {
                    entriesList.setVisibility(View.GONE);
                }
            } else {
                if (entriesEmpty != null) {
                    entriesEmpty.setVisibility(View.GONE);
                }
                if (entriesList != null) {
                    entriesList.setVisibility(View.VISIBLE);
                }
                if (enteredEventsAdapter != null) {
                    enteredEventsAdapter.notifyDataSetChanged();
                    /*if (entriesList != null) {
                        setListViewHeightBasedOnChildren(entriesList);
                    }*/
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating event history UI", e);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
