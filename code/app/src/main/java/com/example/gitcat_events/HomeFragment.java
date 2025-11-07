package com.example.gitcat_events;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.features.event.ui.EventArrayAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final String PREFS = "app_prefs";

    private ArrayList<Event> upcomingEvents;
    private ArrayList<Event> enteredEvents;
    private ArrayList<Event> pendingInvitations;

    private EventArrayAdapter upcomingEventsAdapter;
    private EventArrayAdapter enteredEventsAdapter;
    private EventArrayAdapter pendingInvitationsAdapter;

    private ListView enteredEventsList;
    private ListView upcomingEventsList;
    private ListView pendingInvitationsList;
    private TextView upcomingEventsEmpty;
    private TextView enteredEventsEmpty;
    private TextView pendingInvitationsEmpty;
    private LinearLayout pendingInvitationsContainer;

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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        enteredEventsList = view.findViewById(R.id.enteredEventsList);
        upcomingEventsList = view.findViewById(R.id.upcomingEventsList);
        pendingInvitationsList = view.findViewById(R.id.pendingInvitationsList);
        upcomingEventsEmpty = view.findViewById(R.id.upcomingEventsEmpty);
        enteredEventsEmpty = view.findViewById(R.id.EnteredEventsEmpty);
        pendingInvitationsEmpty = view.findViewById(R.id.pendingInvitationsEmpty);
        pendingInvitationsContainer = view.findViewById(R.id.PendingInvitationsContainer);

        // Initialize lists
        enteredEvents = new ArrayList<>();
        upcomingEvents = new ArrayList<>();
        pendingInvitations = new ArrayList<>();

        // Initialize adapters
        enteredEventsAdapter = new EventArrayAdapter(getContext(), enteredEvents);
        upcomingEventsAdapter = new EventArrayAdapter(getContext(), upcomingEvents);
        pendingInvitationsAdapter = new EventArrayAdapter(getContext(), pendingInvitations);

        // Set adapters
        enteredEventsList.setAdapter(enteredEventsAdapter);
        upcomingEventsList.setAdapter(upcomingEventsAdapter);
        pendingInvitationsList.setAdapter(pendingInvitationsAdapter);

        // Set up click listeners
        upcomingEventsList.setOnItemClickListener((parent, tmpView, position, id) -> {
            Event selectedEvent = upcomingEvents.get(position);
            EventDetails detailFragment = EventDetails.newInstance(selectedEvent);

            getParentFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out,
                            android.R.anim.fade_in, android.R.anim.fade_out)
                    .add(R.id.frameLayout, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        enteredEventsList.setOnItemClickListener((parent, tmpView, position, id) -> {
            Event selectedEvent = enteredEvents.get(position);
            EventDetails detailFragment = EventDetails.newInstance(selectedEvent);

            getParentFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out,
                            android.R.anim.fade_in, android.R.anim.fade_out)
                    .add(R.id.frameLayout, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        pendingInvitationsList.setOnItemClickListener((parent, tmpView, position, id) -> {
            Event selectedEvent = pendingInvitations.get(position);
            EventDetails detailFragment = EventDetails.newInstance(selectedEvent);

            getParentFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out,
                            android.R.anim.fade_in, android.R.anim.fade_out)
                    .add(R.id.frameLayout, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Load events
        loadEvents();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload events when returning to fragment
        if (enteredEvents != null && upcomingEvents != null && pendingInvitations != null) {
            loadEvents();
        }
    }

    private void loadEvents() {
        String deviceId = getOrCreateDeviceId();

        // Load pending invitations first (highest priority)
        loadPendingInvitations(deviceId);
        
        // Load all upcoming events
        loadUpcomingEvents();

        // Load events user has entered (accepted invitations)
        loadEnteredEvents(deviceId);
    }
    
    private void loadPendingInvitations(String deviceId) {
        pendingInvitations.clear();
        
        // Find all events where user has a pending invitation
        db.collectionGroup("invitation_list")
                .whereEqualTo("userDeviceId", deviceId)
                .get()
                .addOnSuccessListener(invitationSnapshot -> {
                    if (invitationSnapshot.isEmpty()) {
                        updatePendingInvitationsUI();
                        return;
                    }
                    
                    // Collect event IDs with pending invitations
                    Set<String> invitationEventIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : invitationSnapshot) {
                        String status = doc.getString("status");
                        String eventId = doc.getString("eventId");
                        // Only show events with "pending" status
                        if ("pending".equals(status) && eventId != null) {
                            invitationEventIds.add(eventId);
                        }
                    }
                    
                    if (invitationEventIds.isEmpty()) {
                        updatePendingInvitationsUI();
                        return;
                    }
                    
                    // Load event details for each invitation
                    final int[] completed = {0};
                    final int total = invitationEventIds.size();
                    
                    for (String eventId : invitationEventIds) {
                        db.collection("events").document(eventId)
                                .get()
                                .addOnSuccessListener(eventDoc -> {
                                    if (eventDoc.exists()) {
                                        try {
                                            Event event = parseEvent(eventDoc);
                                            pendingInvitations.add(event);
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error parsing invitation event", e);
                                        }
                                    }
                                    
                                    completed[0]++;
                                    if (completed[0] == total) {
                                        // Sort by event date
                                        pendingInvitations.sort((e1, e2) -> {
                                            if (e1.getEventDate() == null || e2.getEventDate() == null) return 0;
                                            return e1.getEventDate().compareTo(e2.getEventDate());
                                        });
                                        updatePendingInvitationsUI();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error loading invitation event: " + eventId, e);
                                    completed[0]++;
                                    if (completed[0] == total) {
                                        updatePendingInvitationsUI();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading pending invitations", e);
                    updatePendingInvitationsUI();
                });
    }
    
    private void updatePendingInvitationsUI() {
        if (pendingInvitations.isEmpty()) {
            pendingInvitationsContainer.setVisibility(View.GONE);
        } else {
            pendingInvitationsContainer.setVisibility(View.VISIBLE);
            pendingInvitationsEmpty.setVisibility(View.GONE);
            pendingInvitationsList.setVisibility(View.VISIBLE);
            pendingInvitationsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnChildren(pendingInvitationsList);
        }
    }

    private void loadUpcomingEvents() {
        String currentDeviceId = getOrCreateDeviceId();

        // Load all events sorted by event date
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    upcomingEvents.clear();

                    // First, collect all event IDs where user is already involved
                    Set<String> involvedEventIds = new HashSet<>();
                    
                    // Check waitlist
                    db.collectionGroup("waitlist")
                            .whereEqualTo("userDeviceId", currentDeviceId)
                            .get()
                            .addOnSuccessListener(waitlistSnapshot -> {
                                for (QueryDocumentSnapshot doc : waitlistSnapshot) {
                                    String eventId = doc.getString("eventId");
                                    if (eventId != null) {
                                        involvedEventIds.add(eventId);
                                    }
                                }
                                
                                // Check invitation_list
                                db.collectionGroup("invitation_list")
                                        .whereEqualTo("userDeviceId", currentDeviceId)
                                        .get()
                                        .addOnSuccessListener(invitationSnapshot -> {
                                            for (QueryDocumentSnapshot doc : invitationSnapshot) {
                                                String eventId = doc.getString("eventId");
                                                if (eventId != null) {
                                                    involvedEventIds.add(eventId);
                                                }
                                            }
                                            
                                            // Check acceptedList
                                            db.collectionGroup("acceptedList")
                                                    .whereEqualTo("userDeviceId", currentDeviceId)
                                                    .get()
                                                    .addOnSuccessListener(acceptedSnapshot -> {
                                                        for (QueryDocumentSnapshot doc : acceptedSnapshot) {
                                                            String eventId = doc.getString("eventId");
                                                            if (eventId != null) {
                                                                involvedEventIds.add(eventId);
                                                            }
                                                        }
                                                        
                                                        // Now filter events
                                                        filterUpcomingEvents(queryDocumentSnapshots, currentDeviceId, involvedEventIds);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Error loading acceptedList", e);
                                                        // Continue with filtering even if this fails
                                                        filterUpcomingEvents(queryDocumentSnapshots, currentDeviceId, involvedEventIds);
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error loading invitation_list", e);
                                            // Continue with filtering even if this fails
                                            filterUpcomingEvents(queryDocumentSnapshots, currentDeviceId, involvedEventIds);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading waitlist", e);
                                // Continue with filtering even if this fails
                                filterUpcomingEvents(queryDocumentSnapshots, currentDeviceId, involvedEventIds);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading upcoming events", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void filterUpcomingEvents(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots, 
                                      String currentDeviceId, Set<String> involvedEventIds) {
        upcomingEvents.clear();
        
        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
            try {
                Event event = parseEvent(document);
                String eventId = event.getDocumentId();

                // Skip if event couldn't be parsed
                if (eventId == null) {
                    Log.w(TAG, "Skipping event with null document ID");
                    continue;
                }

                // Don't show events organized by this user
                if (event.getOrganizerDeviceId() != null &&
                        event.getOrganizerDeviceId().equals(currentDeviceId)) {
                    continue;
                }

                // Don't show events where user is already involved (waitlist, invited, or accepted)
                if (involvedEventIds.contains(eventId)) {
                    continue;
                }

                // Check if registration is currently open
                // Registration is open if: registrationStartDate <= now <= raffleDate
                Calendar now = Calendar.getInstance();
                
                // Normalize now to start of day for date-only comparison
                Calendar nowNormalized = (Calendar) now.clone();
                nowNormalized.set(Calendar.HOUR_OF_DAY, 0);
                nowNormalized.set(Calendar.MINUTE, 0);
                nowNormalized.set(Calendar.SECOND, 0);
                nowNormalized.set(Calendar.MILLISECOND, 0);
                
                boolean registrationStarted = true;
                if (event.getRegistrationStartDate() != null) {
                    Calendar regStartNormalized = (Calendar) event.getRegistrationStartDate().clone();
                    regStartNormalized.set(Calendar.HOUR_OF_DAY, 0);
                    regStartNormalized.set(Calendar.MINUTE, 0);
                    regStartNormalized.set(Calendar.SECOND, 0);
                    regStartNormalized.set(Calendar.MILLISECOND, 0);
                    // Registration has started if now >= registrationStartDate
                    registrationStarted = !nowNormalized.before(regStartNormalized);
                }
                
                boolean registrationOpen = true;
                if (event.getRaffleDate() != null) {
                    Calendar raffleNormalized = (Calendar) event.getRaffleDate().clone();
                    raffleNormalized.set(Calendar.HOUR_OF_DAY, 0);
                    raffleNormalized.set(Calendar.MINUTE, 0);
                    raffleNormalized.set(Calendar.SECOND, 0);
                    raffleNormalized.set(Calendar.MILLISECOND, 0);
                    // Registration is open if now <= raffleDate (registration closes at end of raffleDate)
                    // So we allow events where now is before or equal to raffleDate
                    registrationOpen = !nowNormalized.after(raffleNormalized);
                }

                if (registrationStarted && registrationOpen) {
                    upcomingEvents.add(event);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing event: " + document.getId(), e);
                // Continue processing other events even if one fails
            }
        }

        // Sort by event date
        upcomingEvents.sort((e1, e2) -> {
            if (e1.getEventDate() == null || e2.getEventDate() == null) return 0;
            return e1.getEventDate().compareTo(e2.getEventDate());
        });

        updateUpcomingEventsUI();
    }

    private void loadEnteredEvents(String deviceId) {
        enteredEvents.clear();
        Set<String> eventIds = new HashSet<>();

        // Only show events where user has accepted invitation (in acceptedList)
        // Waitlisted events should NOT appear in "Entered Events"
        db.collectionGroup("acceptedList")
                .whereEqualTo("userDeviceId", deviceId)
                .get()
                .addOnSuccessListener(acceptedSnapshot -> {
                    // Collect all event IDs from accepted list
                    for (QueryDocumentSnapshot doc : acceptedSnapshot) {
                        String eventId = doc.getString("eventId");
                        if (eventId != null) {
                            eventIds.add(eventId);
                        }
                    }

                    loadEventDetails(eventIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading accepted events", e);
                    // Even if query fails, try to load with empty set
                    loadEventDetails(eventIds);
                });
    }

    private void loadEventDetails(Set<String> eventIds) {
        if (eventIds.isEmpty()) {
            updateEnteredEventsUI();
            return;
        }

        final int[] completed = {0};
        final int total = eventIds.size();
        enteredEvents.clear();

        for (String eventId : eventIds) {
            db.collection("events").document(eventId)
                    .get()
                    .addOnSuccessListener(eventDoc -> {
                        if (eventDoc.exists()) {
                            try {
                                Event event = parseEvent(eventDoc);
                                enteredEvents.add(event);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing entered event", e);
                            }
                        }

                        completed[0]++;
                        if (completed[0] == total) {
                            // Sort by event date
                            enteredEvents.sort((e1, e2) -> {
                                if (e1.getEventDate() == null || e2.getEventDate() == null) return 0;
                                return e1.getEventDate().compareTo(e2.getEventDate());
                            });
                            updateEnteredEventsUI();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading event: " + eventId, e);
                        completed[0]++;
                        if (completed[0] == total) {
                            updateEnteredEventsUI();
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

        SharedPreferences sp = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String deviceId = sp.getString("device_id", null);

        if (deviceId == null) {
            try {
                deviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                if (deviceId == null || deviceId.isEmpty()) {
                    deviceId = UUID.randomUUID().toString();
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to get Android ID", e);
                deviceId = UUID.randomUUID().toString();
            }

            // Save the device ID for future use
            sp.edit().putString("device_id", deviceId).apply();
        }

        return deviceId;
    }

    private static void setListViewHeightBasedOnChildren(ListView listView) {
        if (listView == null) return;

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