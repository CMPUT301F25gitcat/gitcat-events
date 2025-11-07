package com.example.gitcat_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.features.event.ui.EventArrayAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final String PREFS = "app_prefs";
    
    private ArrayList<Event> upcomingEvents;
    private ArrayList<Event> enteredEvents;

    private EventArrayAdapter upcomingEventsAdapter;
    private EventArrayAdapter enteredEventsAdapter;

    private ListView enteredEventsList;
    private ListView upcomingEventsList;
    private TextView enteredEventsEmpty;
    private TextView upcomingEventsEmpty;
    
    private FirebaseFirestore db;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        enteredEventsList = view.findViewById(R.id.enteredEventsList);
        upcomingEventsList = view.findViewById(R.id.upcomingEventsList);
        enteredEventsEmpty = view.findViewById(R.id.EnteredEventsEmpty);
        upcomingEventsEmpty = view.findViewById(R.id.upcomingEventsEmpty);

        // Initialize lists
        enteredEvents = new ArrayList<>();
        upcomingEvents = new ArrayList<>();

        // Initialize adapters
        enteredEventsAdapter = new EventArrayAdapter(getContext(), enteredEvents);
        upcomingEventsAdapter = new EventArrayAdapter(getContext(), upcomingEvents);


        // show placeholders if there is no events
        if(enteredEvents.size() == 0){
            view.findViewById(R.id.EnteredEventsEmpty).setVisibility(View.VISIBLE);;
        }

        if(upcomingEvents.size() == 0){
            view.findViewById(R.id.upcomingEventsEmpty).setVisibility(View.VISIBLE);;
        }

        enteredEventsList.setAdapter(enteredEventsAdapter);
        upcomingEventsList.setAdapter(upcomingEventsAdapter);

        // Set up click listeners
        upcomingEventsList.setOnItemClickListener((parent, tmpView, position, id) -> {
            Event selectedEvent = upcomingEvents.get(position);
            Intent intent = new Intent(getContext(), EventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getDocumentId());
            startActivity(intent);
        });
        
        enteredEventsList.setOnItemClickListener((parent, tmpView, position, id) -> {
            Event selectedEvent = enteredEvents.get(position);
            Intent intent = new Intent(getContext(), EventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getDocumentId());
            startActivity(intent);
        });

        // Load events
        loadEvents();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload events when returning to fragment
        if (enteredEvents != null && upcomingEvents != null) {
            loadEvents();
        }
    }
    
    private void loadEvents() {
        String deviceId = getOrCreateDeviceId();
        
        // Load all upcoming events
        loadUpcomingEvents();
        
        // Load events user has entered (waitlisted)
        loadEnteredEvents(deviceId);
    }
    
    private void loadUpcomingEvents() {
        String currentDeviceId = getOrCreateDeviceId();
        
        // Load all events sorted by event date
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    upcomingEvents.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Event event = parseEvent(document);
                            
                            // Don't show events organized by this user
                            if (event.getOrganizerDeviceId() != null && 
                                    event.getOrganizerDeviceId().equals(currentDeviceId)) {
                                continue;
                            }
                            
                            // Only show events where registration is still open
                            Calendar now = Calendar.getInstance();
                            if (event.getRaffleDate() != null && now.before(event.getRaffleDate())) {
                                upcomingEvents.add(event);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing event: " + document.getId(), e);
                        }
                    }
                    
                    // Sort by event date
                    upcomingEvents.sort((e1, e2) -> {
                        if (e1.getEventDate() == null || e2.getEventDate() == null) return 0;
                        return e1.getEventDate().compareTo(e2.getEventDate());
                    });
                    
                    updateUpcomingEventsUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading upcoming events", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void loadEnteredEvents(String deviceId) {
        // Get all waitlist entries for this user
        db.collectionGroup("waitlist")
                .whereEqualTo("userDeviceId", deviceId)
                .get()
                .addOnSuccessListener(waitlistSnapshot -> {
                    enteredEvents.clear();
                    
                    // Get unique event IDs
                    ArrayList<String> eventIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : waitlistSnapshot) {
                        String eventId = doc.getString("eventId");
                        if (eventId != null && !eventIds.contains(eventId)) {
                            eventIds.add(eventId);
                        }
                    }
                    
                    // Load each event
                    if (eventIds.isEmpty()) {
                        updateEnteredEventsUI();
                        return;
                    }
                    
                    for (String eventId : eventIds) {
                        db.collection("events").document(eventId)
                                .get()
                                .addOnSuccessListener(eventDoc -> {
                                    if (eventDoc.exists()) {
                                        try {
                                            Event event = parseEvent(eventDoc);
                                            enteredEvents.add(event);
                                            
                                            // Sort by event date
                                            enteredEvents.sort((e1, e2) -> {
                                                if (e1.getEventDate() == null || e2.getEventDate() == null) return 0;
                                                return e1.getEventDate().compareTo(e2.getEventDate());
                                            });
                                            
                                            updateEnteredEventsUI();
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error parsing entered event", e);
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading entered events", e);
                });
    }
    
    private Event parseEvent(com.google.firebase.firestore.DocumentSnapshot document) {
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
        
        Boolean geoLocation = document.getBoolean("geoLocationRequired");
        event.setGeoLocationRequired(geoLocation != null ? geoLocation : false);
        
        // Convert Date to Calendar
        Date eventDate = document.getDate("eventDate");
        if (eventDate != null) {
            Calendar eventCal = Calendar.getInstance();
            eventCal.setTime(eventDate);
            event.setEventDate(eventCal);
        }
        
        Date raffleDate = document.getDate("raffleDate");
        if (raffleDate != null) {
            Calendar raffleCal = Calendar.getInstance();
            raffleCal.setTime(raffleDate);
            event.setRaffleDate(raffleCal);
        }
        
        return event;
    }
    
    private void updateUpcomingEventsUI() {
        if (upcomingEvents.isEmpty()) {
            upcomingEventsEmpty.setVisibility(View.VISIBLE);
            upcomingEventsList.setVisibility(View.GONE);
        } else {
            upcomingEventsEmpty.setVisibility(View.GONE);
            upcomingEventsList.setVisibility(View.VISIBLE);
            upcomingEventsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnChildren(upcomingEventsList);
        }
    }
    
    private void updateEnteredEventsUI() {
        if (enteredEvents.isEmpty()) {
            enteredEventsEmpty.setVisibility(View.VISIBLE);
            enteredEventsList.setVisibility(View.GONE);
        } else {
            enteredEventsEmpty.setVisibility(View.GONE);
            enteredEventsList.setVisibility(View.VISIBLE);
            enteredEventsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnChildren(enteredEventsList);
        }
    }
    
    private String getOrCreateDeviceId() {
        if (getContext() == null) return UUID.randomUUID().toString();
        
        SharedPreferences sp = getContext().getSharedPreferences(PREFS, getContext().MODE_PRIVATE);
        String deviceId = sp.getString("device_id", null);

        if (deviceId == null) {
            try {
                deviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
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

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = (130 * listAdapter.getCount());
        float dpHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, totalHeight, listView.getContext().getResources().getDisplayMetrics());

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = (int) dpHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


}