package com.example.gitcat_events;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import android.content.Intent;

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
    private ImageButton btnFilterEvents;
    private TextView textView2; // Upcoming Events header

    private FirebaseFirestore db;

    // Filter state
    private Calendar filterStartDate;
    private Calendar filterEndDate;
    private Set<String> filterInterests;

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
        btnFilterEvents = view.findViewById(R.id.btnFilterEvents);
        textView2 = view.findViewById(R.id.textView2);

        // Initialize lists
        enteredEvents = new ArrayList<>();
        upcomingEvents = new ArrayList<>();
        pendingInvitations = new ArrayList<>();
        
        // Initialize filter state
        filterInterests = new HashSet<>();

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

            getParentFragmentManager().setFragmentResultListener("detail_closed", this, (key, bundle) -> {
                Event deletedEvent = (Event) bundle.getSerializable("deletedEvent");
                if (deletedEvent != null) {
                    for (Iterator<Event> iterator = upcomingEvents.iterator(); iterator.hasNext();) {
                        Event e = iterator.next();
                        if (e.getDocumentId().equals(deletedEvent.getDocumentId())) {
                            iterator.remove();
                            break;
                        }
                    }
                    upcomingEventsAdapter.notifyDataSetChanged();
                }
            });

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

            getParentFragmentManager().setFragmentResultListener("detail_closed", this, (key, bundle) -> {
                Event deletedEvent = (Event) bundle.getSerializable("deletedEvent");
                if (deletedEvent != null) {
                    for (Iterator<Event> iterator = enteredEvents.iterator(); iterator.hasNext();) {
                        Event e = iterator.next();
                        if (e.getDocumentId().equals(deletedEvent.getDocumentId())) {
                            iterator.remove();
                            break;
                        }
                    }
                    enteredEventsAdapter.notifyDataSetChanged();
                }
            });

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

        // Set up filter button click listener
        if (btnFilterEvents != null) {
            btnFilterEvents.setOnClickListener(v -> showFilterBottomSheet());
        }


        ImageButton btnScanQR = view.findViewById(R.id.imageButton2);
        if (btnScanQR != null) {
            btnScanQR.setOnClickListener(v->{
                Intent intent = new Intent(getActivity(), QRScannerActivity.class);
                startActivity(intent);
            });
        }

        // Update filter indicator on view creation
        updateFilterIndicator();

        // Load events after view is fully created
        // Use post to ensure view is attached to window
        view.post(() -> {
            if (getView() != null && isAdded()) {
                loadEvents();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload events when returning to fragment
        if (getView() != null && enteredEvents != null && upcomingEvents != null && pendingInvitations != null) {
            loadEvents();
        }
    }

    private void loadEvents() {
        if (getView() == null) {
            Log.w(TAG, "Fragment view is null, skipping loadEvents");
            return;
        }
        
        String deviceId = getOrCreateDeviceId();

        // Load pending invitations first (highest priority)
        loadPendingInvitations(deviceId);
        
        // Load all upcoming events
        loadUpcomingEvents();

        // Load events user has entered (accepted invitations)
        loadEnteredEvents(deviceId);
    }
    
    private void loadPendingInvitations(String deviceId) {
        if (getView() == null) {
            Log.w(TAG, "Fragment view is null, skipping loadPendingInvitations");
            return;
        }
        
        if (pendingInvitations == null) {
            pendingInvitations = new ArrayList<>();
        }
        pendingInvitations.clear();
        
        // Find all events where user has a pending invitation
        db.collectionGroup("invitation_list")
                .whereEqualTo("userDeviceId", deviceId)
                .get()
                .addOnSuccessListener(invitationSnapshot -> {
                    if (getView() == null) {
                        Log.w(TAG, "Fragment view destroyed during loadPendingInvitations");
                        return;
                    }
                    
                    if (invitationSnapshot == null || invitationSnapshot.isEmpty()) {
                        updatePendingInvitationsUI();
                        return;
                    }
                    
                    // Collect event IDs with pending invitations
                    Set<String> invitationEventIds = new HashSet<>();
                    try {
                        for (QueryDocumentSnapshot doc : invitationSnapshot) {
                            String status = doc.getString("status");
                            String eventId = doc.getString("eventId");
                            // Only show events with "pending" status
                            if ("pending".equals(status) && eventId != null) {
                                invitationEventIds.add(eventId);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing invitation snapshot", e);
                        updatePendingInvitationsUI();
                        return;
                    }
                    
                    if (invitationEventIds.isEmpty()) {
                        updatePendingInvitationsUI();
                        return;
                    }
                    
                    // Load event details for each invitation
                    final int[] completed = {0};
                    final int total = invitationEventIds.size();
                    
                    if (pendingInvitations == null) {
                        pendingInvitations = new ArrayList<>();
                    }
                    
                    for (String eventId : invitationEventIds) {
                        db.collection("events").document(eventId)
                                .get()
                                .addOnSuccessListener(eventDoc -> {
                                    if (getView() == null) {
                                        return;
                                    }
                                    
                                    if (eventDoc != null && eventDoc.exists()) {
                                        try {
                                            Event event = parseEvent(eventDoc);
                                            if (event != null && pendingInvitations != null) {
                                                // Check for duplicates before adding
                                                boolean isDuplicate = false;
                                                for (Event existingEvent : pendingInvitations) {
                                                    if (existingEvent != null && existingEvent.getDocumentId() != null &&
                                                            existingEvent.getDocumentId().equals(event.getDocumentId())) {
                                                        isDuplicate = true;
                                                        break;
                                                    }
                                                }
                                                if (!isDuplicate) {
                                                    pendingInvitations.add(event);
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error parsing invitation event", e);
                                        }
                                    }
                                    
                                    completed[0]++;
                                    if (completed[0] == total && getView() != null) {
                                        // Sort by event date
                                        try {
                                            if (pendingInvitations != null) {
                                                pendingInvitations.sort((e1, e2) -> {
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
                                        updatePendingInvitationsUI();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error loading invitation event: " + eventId, e);
                                    completed[0]++;
                                    if (completed[0] == total && getView() != null) {
                                        updatePendingInvitationsUI();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading pending invitations", e);
                    if (getView() != null) {
                        updatePendingInvitationsUI();
                    }
                });
    }
    
    private void updatePendingInvitationsUI() {
        if (getView() == null) {
            Log.w(TAG, "Fragment view is null, skipping updatePendingInvitationsUI");
            return;
        }
        
        try {
            if (pendingInvitations == null) {
                pendingInvitations = new ArrayList<>();
            }
            
            if (pendingInvitations.isEmpty()) {
                if (pendingInvitationsContainer != null) {
                    pendingInvitationsContainer.setVisibility(View.GONE);
                }
            } else {
                if (pendingInvitationsContainer != null) {
                    pendingInvitationsContainer.setVisibility(View.VISIBLE);
                }
                if (pendingInvitationsEmpty != null) {
                    pendingInvitationsEmpty.setVisibility(View.GONE);
                }
                if (pendingInvitationsList != null) {
                    pendingInvitationsList.setVisibility(View.VISIBLE);
                }
                if (pendingInvitationsAdapter != null) {
                    pendingInvitationsAdapter.notifyDataSetChanged();
                    if (pendingInvitationsList != null) {
                        setListViewHeightBasedOnChildren(pendingInvitationsList);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating pending invitations UI", e);
        }
    }

    private void loadUpcomingEvents() {
        if (getView() == null) {
            Log.w(TAG, "Fragment view is null, skipping loadUpcomingEvents");
            return;
        }
        
        String currentDeviceId = getOrCreateDeviceId();
        Log.d(TAG, "Loading upcoming events for device: " + currentDeviceId);

        // Load all events sorted by event date
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getView() == null) {
                        Log.w(TAG, "Fragment view destroyed during load, aborting");
                        return;
                    }
                    
                    Log.d(TAG, "Loaded " + (queryDocumentSnapshots != null ? queryDocumentSnapshots.size() : 0) + " events from Firestore");
                    upcomingEvents.clear();

                    // Separate tracking: waitlist events should still show in upcoming events
                    // Only filter out events where user is invited or accepted
                    Set<String> waitlistEventIds = new HashSet<>(); // Keep these visible
                    Set<String> invitedEventIds = new HashSet<>(); // Filter these out
                    Set<String> acceptedEventIds = new HashSet<>(); // Filter these out
                    
                    // Use counters to track completion of all async operations
                    final int[] completedQueries = {0};
                    final int totalQueries = 3;
                    
                    // Helper to check if all queries are done and filter
                    Runnable checkAndFilter = () -> {
                        synchronized (this) {
                            completedQueries[0]++;
                            Log.d(TAG, "Query completed: " + completedQueries[0] + "/" + totalQueries + 
                                      " - Waitlist: " + waitlistEventIds.size() + 
                                      ", Invited: " + invitedEventIds.size() + 
                                      ", Accepted: " + acceptedEventIds.size());
                            if (completedQueries[0] == totalQueries) {
                                if (getView() != null) {
                                    Log.d(TAG, "All queries complete, filtering events. Total events: " + queryDocumentSnapshots.size());
                                    filterUpcomingEvents(queryDocumentSnapshots, currentDeviceId, waitlistEventIds, invitedEventIds, acceptedEventIds);
                                } else {
                                    Log.w(TAG, "View is null when trying to filter events");
                                }
                            }
                        }
                    };
                    
                    // Check waitlist (keep these visible in upcoming events)
                    db.collectionGroup("waitlist")
                            .whereEqualTo("userDeviceId", currentDeviceId)
                            .get()
                            .addOnSuccessListener(waitlistSnapshot -> {
                                try {
                                    for (QueryDocumentSnapshot doc : waitlistSnapshot) {
                                        String eventId = doc.getString("eventId");
                                        if (eventId != null) {
                                            waitlistEventIds.add(eventId);
                                        }
                                    }
                                    Log.d(TAG, "Found " + waitlistEventIds.size() + " events on waitlist");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing waitlist snapshot", e);
                                }
                                checkAndFilter.run();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading waitlist", e);
                                checkAndFilter.run();
                            });
                    
                    // Check invitation_list (filter these out from upcoming)
                    db.collectionGroup("invitation_list")
                            .whereEqualTo("userDeviceId", currentDeviceId)
                            .get()
                            .addOnSuccessListener(invitationSnapshot -> {
                                try {
                                    for (QueryDocumentSnapshot doc : invitationSnapshot) {
                                        String eventId = doc.getString("eventId");
                                        if (eventId != null) {
                                            invitedEventIds.add(eventId);
                                        }
                                    }
                                    Log.d(TAG, "Found " + invitedEventIds.size() + " events with invitations");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing invitation_list snapshot", e);
                                }
                                checkAndFilter.run();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading invitation_list", e);
                                checkAndFilter.run();
                            });
                    
                    // Check acceptedList (filter these out from upcoming)
                    db.collectionGroup("acceptedList")
                            .whereEqualTo("userDeviceId", currentDeviceId)
                            .get()
                            .addOnSuccessListener(acceptedSnapshot -> {
                                try {
                                    for (QueryDocumentSnapshot doc : acceptedSnapshot) {
                                        String eventId = doc.getString("eventId");
                                        if (eventId != null) {
                                            acceptedEventIds.add(eventId);
                                        }
                                    }
                                    Log.d(TAG, "Found " + acceptedEventIds.size() + " accepted events");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing acceptedList snapshot", e);
                                }
                                checkAndFilter.run();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading acceptedList", e);
                                checkAndFilter.run();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading upcoming events", e);
                    if (getView() != null && getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                        updateUpcomingEventsUI();
                    }
                });
    }
    
    private void filterUpcomingEvents(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots, 
                                      String currentDeviceId, 
                                      Set<String> waitlistEventIds,
                                      Set<String> invitedEventIds,
                                      Set<String> acceptedEventIds) {
        if (getView() == null) {
            Log.w(TAG, "Fragment view is null, skipping filterUpcomingEvents");
            return;
        }
        
        if (upcomingEvents == null) {
            upcomingEvents = new ArrayList<>();
        }
        upcomingEvents.clear();
        
        if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
            Log.d(TAG, "No events to filter - query result is empty");
            updateUpcomingEventsUI();
            return;
        }
        
        Log.d(TAG, "Filtering " + queryDocumentSnapshots.size() + " events. Current device: " + currentDeviceId + 
                  ", Waitlist: " + waitlistEventIds.size() + 
                  ", Invited: " + invitedEventIds.size() + 
                  ", Accepted: " + acceptedEventIds.size());

        // Prepare filter dates ONCE before the loop starts (performance optimization)
        Calendar startNormalized = null;
        if (filterStartDate != null) {
            startNormalized = (Calendar) filterStartDate.clone();
            startNormalized.set(Calendar.HOUR_OF_DAY, 0);
            startNormalized.set(Calendar.MINUTE, 0);
            startNormalized.set(Calendar.SECOND, 0);
            startNormalized.set(Calendar.MILLISECOND, 0);
        }

        Calendar endNormalized = null;
        if (filterEndDate != null) {
            endNormalized = (Calendar) filterEndDate.clone();
            endNormalized.set(Calendar.HOUR_OF_DAY, 0);
            endNormalized.set(Calendar.MINUTE, 0);
            endNormalized.set(Calendar.SECOND, 0);
            endNormalized.set(Calendar.MILLISECOND, 0);
        }
        
        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
            try {
                Event event = parseEvent(document);
                
                // Skip if event couldn't be parsed
                if (event == null) {
                    Log.w(TAG, "Skipping event with null parse result: " + document.getId());
                    continue;
                }
                
                String eventId = event.getDocumentId();
                if (eventId == null || eventId.isEmpty()) {
                    Log.w(TAG, "Skipping event with null or empty document ID: " + document.getId());
                    continue;
                }

                // Don't show events organized by this user
                // commented out for debugging
//                if (event.getOrganizerDeviceId() != null &&
//                        event.getOrganizerDeviceId().equals(currentDeviceId)) {
//                    continue;
//                }

                // Filter logic:
                // - Show events user is on waitlist for (they can see their status)
                // - Don't show events where user has been invited (show in pending invitations instead)
                // - Don't show events where user has accepted (show in entered events instead)
                // - Don't show events organized by this user
                
                if (invitedEventIds.contains(eventId)) {
                    // User has a pending invitation - don't show in upcoming (it's in pending invitations)
                    Log.d(TAG, "Filtering out event with invitation: " + event.getName());
                    continue;
                }
                
                if (acceptedEventIds.contains(eventId)) {
                    // User has accepted - don't show in upcoming (it's in entered events)
                    Log.d(TAG, "Filtering out accepted event: " + event.getName());
                    continue;
                }
                
                // Note: Events on waitlist are kept visible (waitlistEventIds.contains(eventId) is OK)
                if (waitlistEventIds.contains(eventId)) {
                    Log.d(TAG, "Keeping waitlist event visible: " + event.getName());
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
                String regStartInfo = "null";
                if (event.getRegistrationStartDate() != null) {
                    try {
                        Calendar regStartNormalized = (Calendar) event.getRegistrationStartDate().clone();
                        regStartNormalized.set(Calendar.HOUR_OF_DAY, 0);
                        regStartNormalized.set(Calendar.MINUTE, 0);
                        regStartNormalized.set(Calendar.SECOND, 0);
                        regStartNormalized.set(Calendar.MILLISECOND, 0);
                        // Registration has started if now >= registrationStartDate
                        registrationStarted = !nowNormalized.before(regStartNormalized);
                        regStartInfo = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(regStartNormalized.getTime());
                    } catch (Exception e) {
                        Log.e(TAG, "Error comparing registration start date for event: " + eventId, e);
                        registrationStarted = true; // Default to showing if error
                    }
                }
                
                boolean registrationOpen = true;
                String raffleDateInfo = "null";
                if (event.getRaffleDate() != null) {
                    try {
                        Calendar raffleNormalized = (Calendar) event.getRaffleDate().clone();
                        raffleNormalized.set(Calendar.HOUR_OF_DAY, 0);
                        raffleNormalized.set(Calendar.MINUTE, 0);
                        raffleNormalized.set(Calendar.SECOND, 0);
                        raffleNormalized.set(Calendar.MILLISECOND, 0);
                        // Registration is open if now <= raffleDate (registration closes at end of raffleDate)
                        // So we allow events where now is before or equal to raffleDate
                        registrationOpen = !nowNormalized.after(raffleNormalized);
                        raffleDateInfo = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(raffleNormalized.getTime());
                    } catch (Exception e) {
                        Log.e(TAG, "Error comparing raffle date for event: " + eventId, e);
                        registrationOpen = true; // Default to showing if error
                    }
                }
                
                String nowInfo = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(nowNormalized.getTime());
                Log.d(TAG, "Event: " + event.getName() + " - Now: " + nowInfo + ", RegStart: " + regStartInfo + ", Raffle: " + raffleDateInfo + 
                          ", Started: " + registrationStarted + ", Open: " + registrationOpen);

                // For debugging: Log all events before filtering to see what we have
                Log.d(TAG, "Event details - Name: " + event.getName() + ", Organizer: " + event.getOrganizerDeviceId() + 
                          ", Is organizer? " + (event.getOrganizerDeviceId() != null && event.getOrganizerDeviceId().equals(currentDeviceId)) +
                          ", On waitlist? " + waitlistEventIds.contains(eventId) +
                          ", Invited? " + invitedEventIds.contains(eventId) +
                          ", Accepted? " + acceptedEventIds.contains(eventId));

                if (registrationStarted && registrationOpen) {
                    // Apply date range filter if set
                    boolean passesDateFilter = true;
                    if (event.getEventDate() != null) {
                        try {
                            // Normalize event date to start of day for comparison
                            Calendar eventDateNormalized = (Calendar) event.getEventDate().clone();
                            eventDateNormalized.set(Calendar.HOUR_OF_DAY, 0);
                            eventDateNormalized.set(Calendar.MINUTE, 0);
                            eventDateNormalized.set(Calendar.SECOND, 0);
                            eventDateNormalized.set(Calendar.MILLISECOND, 0);

                            // Check start date filter (using pre-normalized filter date)
                            if (startNormalized != null) {
                                if (eventDateNormalized.before(startNormalized)) {
                                    passesDateFilter = false;
                                    Log.d(TAG, "Event filtered out by start date: " + event.getName());
                                }
                            }

                            // Check end date filter (using pre-normalized filter date)
                            if (endNormalized != null && passesDateFilter) {
                                if (eventDateNormalized.after(endNormalized)) {
                                    passesDateFilter = false;
                                    Log.d(TAG, "Event filtered out by end date: " + event.getName());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error applying date filter for event: " + eventId, e);
                            // Default to passing filter if error occurs
                            passesDateFilter = true;
                        }
                    } else {
                        // If event has no date and filters are set, exclude it
                        // (In reality, events should always have dates, but we handle this defensively)
                        if (startNormalized != null || endNormalized != null) {
                            passesDateFilter = false;
                            Log.d(TAG, "Event filtered out - no event date: " + event.getName());
                        }
                    }

                    // Apply interest filter if set (AND logic - event must have ALL selected interests)
                    boolean passesInterestFilter = true;
                    if (filterInterests != null && !filterInterests.isEmpty()) {
                        List<String> eventTypes = event.getEventTypes();
                        if (eventTypes == null || eventTypes.isEmpty()) {
                            // Event has no types, filter it out when interests are selected
                            passesInterestFilter = false;
                            Log.d(TAG, "Event filtered out - no event types: " + event.getName());
                        } else {
                            // Event must have ALL selected interests (AND logic)
                            // Check if eventTypes contains all filterInterests
                            passesInterestFilter = eventTypes.containsAll(filterInterests);
                            if (!passesInterestFilter) {
                                Log.d(TAG, "Event filtered out by interests: " + event.getName() + 
                                          " - Event types: " + eventTypes + ", Filter: " + filterInterests);
                            }
                        }
                    }

                    if (passesDateFilter && passesInterestFilter) {
                        upcomingEvents.add(event);
                        Log.d(TAG, "✓ Added event to upcoming: " + event.getName() + " (ID: " + eventId + ")");
                    } else {
                        if (!passesDateFilter) {
                            Log.d(TAG, "✗ Event filtered out by date range: " + event.getName());
                        }
                        if (!passesInterestFilter) {
                            Log.d(TAG, "✗ Event filtered out by interests: " + event.getName());
                        }
                    }
                } else {
                    Log.d(TAG, "✗ Event filtered out: " + event.getName() + " - registrationStarted: " + registrationStarted + ", registrationOpen: " + registrationOpen);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error filtering event: " + (document != null ? document.getId() : "unknown"), e);
                // Continue processing other events even if one fails
            }
        }

        Log.d(TAG, "After filtering: " + upcomingEvents.size() + " upcoming events");

        // Sort by event date (events without dates go to the end)
        try {
            upcomingEvents.sort((e1, e2) -> {
                try {
                    if (e1 == null || e2 == null) return 0;
                    Calendar d1 = e1.getEventDate();
                    Calendar d2 = e2.getEventDate();
                    
                    // Both have no date - treat as equal
                    if (d1 == null && d2 == null) return 0;
                    // e1 has no date - put it at the end
                    if (d1 == null) return 1;
                    // e2 has no date - put it at the end
                    if (d2 == null) return -1;
                    // Both have dates - normal sort
                    return d1.compareTo(d2);
                } catch (Exception e) {
                    Log.e(TAG, "Error sorting events", e);
                    return 0;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error sorting upcoming events list", e);
        }

        updateUpcomingEventsUI();
    }

    private void showFilterBottomSheet() {
        EventFilterBottomSheet filterSheet = EventFilterBottomSheet.newInstance(new EventFilterBottomSheet.FilterCallback() {
            @Override
            public void onFilterApplied(Calendar startDate, Calendar endDate, Set<String> selectedInterests) {
                filterStartDate = startDate != null ? (Calendar) startDate.clone() : null;
                filterEndDate = endDate != null ? (Calendar) endDate.clone() : null;
                filterInterests = selectedInterests != null ? new HashSet<>(selectedInterests) : new HashSet<>();
                updateFilterIndicator();
                // Reload events with new filter
                loadUpcomingEvents();
            }

            @Override
            public void onFiltersCleared() {
                filterStartDate = null;
                filterEndDate = null;
                filterInterests = new HashSet<>();
                updateFilterIndicator();
                // Reload events without filter
                loadUpcomingEvents();
            }
        });

        // Set initial dates if filters are already active
        if (filterStartDate != null || filterEndDate != null) {
            filterSheet.setInitialDates(filterStartDate, filterEndDate);
        }
        
        // Set initial interests if filters are already active
        if (filterInterests != null && !filterInterests.isEmpty()) {
            filterSheet.setInitialInterests(filterInterests);
        }

        filterSheet.show(getParentFragmentManager(), "EventFilterBottomSheet");
    }

    private void updateFilterIndicator() {
        if (getView() == null || textView2 == null || getContext() == null) return;

        boolean hasActiveFilters = filterStartDate != null || filterEndDate != null || 
                                   (filterInterests != null && !filterInterests.isEmpty());
        if (hasActiveFilters) {
            // Show visual indicator that filters are active
            textView2.setText("Upcoming Events (Filtered)");
            textView2.setTextColor(ContextCompat.getColor(requireContext(), R.color.filter_active_orange));
        } else {
            // Reset to normal
            textView2.setText("Upcoming Events");
            textView2.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_default_blue));
        }
    }

    private void loadEnteredEvents(String deviceId) {
        if (getView() == null) {
            Log.w(TAG, "Fragment view is null, skipping loadEnteredEvents");
            return;
        }
        
        if (enteredEvents == null) {
            enteredEvents = new ArrayList<>();
        }
        enteredEvents.clear();
        Set<String> eventIds = new HashSet<>();

        // Only show events where user has accepted invitation (in acceptedList)
        // Waitlisted events should NOT appear in "Entered Events"
        db.collectionGroup("acceptedList")
                .whereEqualTo("userDeviceId", deviceId)
                .get()
                .addOnSuccessListener(acceptedSnapshot -> {
                    if (getView() == null) {
                        Log.w(TAG, "Fragment view destroyed during loadEnteredEvents");
                        return;
                    }
                    
                    try {
                        // Collect all event IDs from accepted list
                        if (acceptedSnapshot != null) {
                            for (QueryDocumentSnapshot doc : acceptedSnapshot) {
                                String eventId = doc.getString("eventId");
                                if (eventId != null) {
                                    eventIds.add(eventId);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing accepted events snapshot", e);
                    }

                    loadEventDetails(eventIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading accepted events", e);
                    // Even if query fails, try to load with empty set
                    if (getView() != null) {
                        loadEventDetails(eventIds);
                    }
                });
    }

    private void loadEventDetails(Set<String> eventIds) {
        if (getView() == null) {
            Log.w(TAG, "Fragment view is null, skipping loadEventDetails");
            return;
        }
        
        if (eventIds == null || eventIds.isEmpty()) {
            updateEnteredEventsUI();
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
                        if (getView() == null) {
                            return;
                        }
                        
                        if (eventDoc != null && eventDoc.exists()) {
                            try {
                                Event event = parseEvent(eventDoc);
                                if (event != null && enteredEvents != null) {
                                    // Check for duplicates before adding
                                    boolean isDuplicate = false;
                                    for (Event existingEvent : enteredEvents) {
                                        if (existingEvent != null && existingEvent.getDocumentId() != null &&
                                                existingEvent.getDocumentId().equals(event.getDocumentId())) {
                                            isDuplicate = true;
                                            break;
                                        }
                                    }
                                    if (!isDuplicate) {
                                        enteredEvents.add(event);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing entered event", e);
                            }
                        }

                        completed[0]++;
                        if (completed[0] == total && getView() != null) {
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
                            updateEnteredEventsUI();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading event: " + eventId, e);
                        completed[0]++;
                        if (completed[0] == total && getView() != null) {
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
            event.setQrCodeUrl(document.getString("qrCodeUrl"));

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

            // Read eventTypes from Firestore (List<String>)
            @SuppressWarnings("unchecked")
            List<Object> eventTypesObj = (List<Object>) document.get("eventTypes");
            if (eventTypesObj != null) {
                List<String> eventTypes = new ArrayList<>();
                for (Object obj : eventTypesObj) {
                    if (obj instanceof String) {
                        eventTypes.add((String) obj);
                    }
                }
                event.setEventTypes(eventTypes.isEmpty() ? null : eventTypes);
            } else {
                event.setEventTypes(null);
            }

            return event;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing event document: " + document.getId(), e);
            return null;
        }
    }

    private void updateUpcomingEventsUI() {
        if (getView() == null) {
            Log.w(TAG, "Fragment view is null, skipping updateUpcomingEventsUI");
            return;
        }
        
        try {
            if (upcomingEvents == null) {
                upcomingEvents = new ArrayList<>();
            }
            
            if (upcomingEvents.isEmpty()) {
                if (upcomingEventsEmpty != null) {
                    upcomingEventsEmpty.setVisibility(View.VISIBLE);
                }
                if (upcomingEventsList != null) {
                    upcomingEventsList.setVisibility(View.GONE);
                }
            } else {
                if (upcomingEventsEmpty != null) {
                    upcomingEventsEmpty.setVisibility(View.GONE);
                }
                if (upcomingEventsList != null) {
                    upcomingEventsList.setVisibility(View.VISIBLE);
                }
                if (upcomingEventsAdapter != null) {
                    upcomingEventsAdapter.notifyDataSetChanged();
                    if (upcomingEventsList != null) {
                        setListViewHeightBasedOnChildren(upcomingEventsList);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating upcoming events UI", e);
        }
    }

    private void updateEnteredEventsUI() {
        if (getView() == null) {
            Log.w(TAG, "Fragment view is null, skipping updateEnteredEventsUI");
            return;
        }
        
        try {
            if (enteredEvents == null) {
                enteredEvents = new ArrayList<>();
            }
            
            if (enteredEvents.isEmpty()) {
                if (enteredEventsEmpty != null) {
                    enteredEventsEmpty.setVisibility(View.VISIBLE);
                }
                if (enteredEventsList != null) {
                    enteredEventsList.setVisibility(View.GONE);
                }
            } else {
                if (enteredEventsEmpty != null) {
                    enteredEventsEmpty.setVisibility(View.GONE);
                }
                if (enteredEventsList != null) {
                    enteredEventsList.setVisibility(View.VISIBLE);
                }
                if (enteredEventsAdapter != null) {
                    enteredEventsAdapter.notifyDataSetChanged();
                    if (enteredEventsList != null) {
                        setListViewHeightBasedOnChildren(enteredEventsList);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating entered events UI", e);
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