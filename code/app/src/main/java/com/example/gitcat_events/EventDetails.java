package com.example.gitcat_events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.core.model.WaitListEntry;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class EventDetails extends Fragment {

    private static final String TAG = "EventDetailsFragment";
    private static final String PREFS = "app_prefs";

    private Event event;
    private FirebaseFirestore db;
    private ListenerRegistration waitlistListener;
    private ListenerRegistration eventListener; // Real-time event update listener
    private boolean isOnWaitlist = false;
    private boolean isOrganizer = false;
    private boolean hasInvitation = false;
    
    private ImageView ivEventPoster;
    private TextView tvEventName, tvEventDate, tvEventSpots, tvEventDesc;
    private TextView tvWaitingListCount, tvStatusMessage, tvSelectionCriteria;
    private Button btnJoinWaitingList, btnRunRaffle, btnViewWaitingList, btnViewInvitedEntrants, btnViewEnrolledEntrants, btnViewCancelledEntrants, btnEditEvent, deleteEventBtn;
    private ImageButton btnBack;
    private Button btnAcceptInvitation, btnDeclineInvitation;
    private ViewGroup invitationButtons;

    public EventDetails() {}

    public static EventDetails newInstance(Event event) {
        EventDetails fragment = new EventDetails();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
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
        tvSelectionCriteria = view.findViewById(R.id.tvSelectionCriteria);
        btnJoinWaitingList = view.findViewById(R.id.btnJoinWaitingList);
        btnRunRaffle = view.findViewById(R.id.btnRunRaffle);
        btnViewWaitingList = view.findViewById(R.id.btnViewWaitingList);
        btnViewInvitedEntrants = view.findViewById(R.id.btnViewInvitedEntrants);
        btnViewEnrolledEntrants = view.findViewById(R.id.btnViewEnrolledEntrants);
        btnViewCancelledEntrants = view.findViewById(R.id.btnViewCancelledEntrants);
        btnEditEvent = view.findViewById(R.id.btnEditEvent);
        btnBack = view.findViewById(R.id.eventDetailsBackBtn);
        btnAcceptInvitation = view.findViewById(R.id.btnAcceptInvitation);
        btnDeclineInvitation = view.findViewById(R.id.btnDeclineInvitation);
        invitationButtons = view.findViewById(R.id.invitationButtons);
        deleteEventBtn = view.findViewById(R.id.adminDeleteEvent);

        // Display event details
        displayEvent();
        
        // Set up real-time event update listener (to prevent crashes when event is edited)
        setupEventListener();
        
        // Set up real-time waiting list count listener
        setupWaitlistListener();
        
        // Check if user is organizer or on waitlist
        checkUserStatus();

        // Set up join/leave button
        btnJoinWaitingList.setOnClickListener(v -> handleWaitlistAction());
        
        // Set up raffle button (only visible to organizer)
        btnRunRaffle.setOnClickListener(v -> runRaffle());
        
        // Set up view waiting list button (only visible to organizer)
        btnViewWaitingList.setOnClickListener(v -> viewWaitingList());
        
        // Set up view invited entrants button (only visible to organizer)
        btnViewInvitedEntrants.setOnClickListener(v -> viewInvitedEntrants());
        
        // Set up view enrolled entrants button (only visible to organizer)
        btnViewEnrolledEntrants.setOnClickListener(v -> viewEnrolledEntrants());
        
        // Set up view cancelled entrants button (only visible to organizer)
        btnViewCancelledEntrants.setOnClickListener(v -> viewCancelledEntrants());
        
        // Set up edit event button (only visible to organizer)
        btnEditEvent.setOnClickListener(v -> editEvent());

        // Set up invitation response buttons
        btnAcceptInvitation.setOnClickListener(v -> acceptInvitation());
        btnDeclineInvitation.setOnClickListener(v -> declineInvitation());

        // Set up back button
        btnBack.setOnClickListener(v -> {
            // Remove this fragment and go back
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.slide_in_left,
                            android.R.anim.fade_out
                    )
                    .remove(EventDetails.this)
                    .commit();
            
            // Notify HomeFragment to refresh (it will reload in onResume)
        });

        // set up delete btn
        if(getIsAdmin(getContext())){
            deleteEventBtn.setVisibility(View.VISIBLE);
            deleteEventBtn.setOnClickListener(v -> {
                deleteEvent(Integer.parseInt(event.getDocumentId()));
            });
        }


        return view;
    }

    private boolean getIsAdmin(Context context) {
        final String KEY_IS_ADMIN = "is_admin";
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_ADMIN, false);
    }

    private void deleteEvent(int eventId) {
        db.collection("events")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(getContext(), "No event found with eventId: " + eventId, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(v -> {

                                    // notify home page so they can reload event list
                                Bundle result = new Bundle();
                                result.putSerializable("deletedEvent", event);
                                getParentFragmentManager().setFragmentResult("detail_closed", result);

                                Toast.makeText(getContext(), "Event deleted: " + eventId, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to delete event: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error querying event: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }




    private void displayEvent() {
        if (event == null || getView() == null) return;
        
        try {
            tvEventName.setText(event.getName() != null ? event.getName() : "Unnamed Event");
            tvEventDesc.setText(event.getDescription() != null ? event.getDescription() : "No description");
            
            // Format and display dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            if (event.getEventDate() != null) {
                tvEventDate.setText("Event: " + dateFormat.format(event.getEventDate().getTime()));
            } else {
                tvEventDate.setText("Event: Date TBD");
            }
            
            tvEventSpots.setText("Capacity: " + event.getCapacity());
            
            // Display selection criteria
            if (event.getSelectionCriteria() != null && !event.getSelectionCriteria().isEmpty()) {
                tvSelectionCriteria.setText(event.getSelectionCriteria());
            } else {
                tvSelectionCriteria.setText("Random selection from all registered participants. All entrants have an equal chance of being selected.");
            }
            
            // Load poster image
            if (event.getPoster() != null && !event.getPoster().isEmpty()) {
                loadBase64Image(event.getPoster(), ivEventPoster);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying event", e);
        }
    }
    
    private void checkUserStatus() {
        if (event == null || event.getDocumentId() == null || getView() == null) return;
        
        try {
            String deviceId = getOrCreateDeviceId();
            Log.d(TAG, "Checking user status - Device ID: " + deviceId + ", Event organizer: " + event.getOrganizerDeviceId());
            
            // Check if user is the organizer
            if (event.getOrganizerDeviceId() != null && 
                    event.getOrganizerDeviceId().equals(deviceId)) {
                isOrganizer = true;
                Log.d(TAG, "User is organizer - showing organizer buttons");
                if (btnJoinWaitingList != null) {
                    btnJoinWaitingList.setVisibility(View.GONE);
                }
                if (invitationButtons != null) {
                    invitationButtons.setVisibility(View.GONE);
                }
                // Show all organizer buttons
                if (btnRunRaffle != null) {
                    btnRunRaffle.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Run Raffle button visible: " + (btnRunRaffle.getVisibility() == View.VISIBLE));
                }
                if (btnViewWaitingList != null) {
                    btnViewWaitingList.setVisibility(View.VISIBLE);
                    Log.d(TAG, "View Waiting List button visible: " + (btnViewWaitingList.getVisibility() == View.VISIBLE));
                }
                if (btnViewInvitedEntrants != null) {
                    btnViewInvitedEntrants.setVisibility(View.VISIBLE);
                    Log.d(TAG, "View Invited Entrants button visible: " + (btnViewInvitedEntrants.getVisibility() == View.VISIBLE));
                }
                if (btnViewEnrolledEntrants != null) {
                    btnViewEnrolledEntrants.setVisibility(View.VISIBLE);
                    Log.d(TAG, "View Enrolled Entrants button visible: " + (btnViewEnrolledEntrants.getVisibility() == View.VISIBLE));
                }
                if (btnViewCancelledEntrants != null) {
                    btnViewCancelledEntrants.setVisibility(View.VISIBLE);
                    Log.d(TAG, "View Cancelled Entrants button visible: " + (btnViewCancelledEntrants.getVisibility() == View.VISIBLE));
                }
                if (btnEditEvent != null) {
                    btnEditEvent.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Edit Event button visible: " + (btnEditEvent.getVisibility() == View.VISIBLE));
                }
                
                // Show organizer status
                showOrganizerStatus();
                return;
            }
            
            // Not organizer, hide organizer buttons
            isOrganizer = false;
            if (btnRunRaffle != null) btnRunRaffle.setVisibility(View.GONE);
            if (btnViewWaitingList != null) btnViewWaitingList.setVisibility(View.GONE);
            if (btnViewInvitedEntrants != null) btnViewInvitedEntrants.setVisibility(View.GONE);
            if (btnViewEnrolledEntrants != null) btnViewEnrolledEntrants.setVisibility(View.GONE);
            if (btnViewCancelledEntrants != null) btnViewCancelledEntrants.setVisibility(View.GONE);
            if (btnEditEvent != null) btnEditEvent.setVisibility(View.GONE);
            
            // Show join button by default (will be hidden if user has invitation)
            if (btnJoinWaitingList != null) btnJoinWaitingList.setVisibility(View.VISIBLE);
            
            // Check if user has a pending invitation
            checkInvitationStatus(deviceId);
            
            // Check waitlist status with real-time listener
            db.collection("events").document(event.getDocumentId())
                    .collection("waitlist")
                    .document(deviceId)
                    .addSnapshotListener((documentSnapshot, error) -> {
                        if (error != null) {
                            Log.e(TAG, "Error checking waitlist status", error);
                            return;
                        }
                        
                        if (getView() == null) return; // Fragment view detached
                        
                        // Update waitlist status immediately
                        boolean wasOnWaitlist = isOnWaitlist;
                        isOnWaitlist = (documentSnapshot != null && documentSnapshot.exists());
                        
                        // Update button if status changed or if no invitation
                        if (wasOnWaitlist != isOnWaitlist || !hasInvitation) {
                            updateButtonForWaitlistStatus();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in checkUserStatus", e);
        }
    }
    
    private void checkInvitationStatus(String deviceId) {
        if (event == null || event.getDocumentId() == null) return;
        
        db.collection("events").document(event.getDocumentId())
                .collection("invitation_list")
                .document(deviceId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error checking invitation status", error);
                        return;
                    }
                    
                    if (getView() == null) return; // Fragment view detached
                    
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        if ("pending".equals(status)) {
                            hasInvitation = true;
                            showInvitationButtons();
                        } else {
                            hasInvitation = false;
                            hideInvitationButtons();
                        }
                    } else {
                        hasInvitation = false;
                        hideInvitationButtons();
                    }
                });
    }
    
    private void showInvitationButtons() {
        if (getView() == null) return;
        
        try {
            if (btnJoinWaitingList != null) btnJoinWaitingList.setVisibility(View.GONE);
            if (invitationButtons != null) invitationButtons.setVisibility(View.VISIBLE);
            if (tvStatusMessage != null) {
                tvStatusMessage.setVisibility(View.VISIBLE);
                tvStatusMessage.setText("🎉 Congratulations! You've been selected for this event!");
                tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing invitation buttons", e);
        }
    }
    
    private void hideInvitationButtons() {
        if (getView() == null) return;
        
        try {
            if (invitationButtons != null) invitationButtons.setVisibility(View.GONE);
            if (!hasInvitation && btnJoinWaitingList != null) {
                btnJoinWaitingList.setVisibility(View.VISIBLE);
                updateButtonForWaitlistStatus();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding invitation buttons", e);
        }
    }
    
    private void updateButtonForWaitlistStatus() {
        if (getView() == null || btnJoinWaitingList == null) return;
        
        try {
            // Simple toggle: Join or Leave based on waitlist status
            if (isOnWaitlist) {
                btnJoinWaitingList.setText("Leave Waiting List");
                btnJoinWaitingList.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_dark));
                if (tvStatusMessage != null) {
                    tvStatusMessage.setVisibility(View.VISIBLE);
                    tvStatusMessage.setText("You're on the waiting list");
                    tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }
            } else {
                btnJoinWaitingList.setText("Join Waiting List");
                btnJoinWaitingList.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_dark));
                if (tvStatusMessage != null) {
                    tvStatusMessage.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating button for waitlist status", e);
        }
    }
    
    private void handleWaitlistAction() {
        // Simple toggle: if on waitlist, leave; if not, join
        if (isOnWaitlist) {
            leaveWaitingList();
        } else {
            joinWaitingList();
        }
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

    private void setupEventListener() {
        if (event == null || event.getDocumentId() == null) return;
        
        // Listen for real-time updates to the event document
        // This prevents crashes when someone edits the event while another user is viewing it
        eventListener = db.collection("events").document(event.getDocumentId())
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to event updates", error);
                        return;
                    }
                    
                    if (documentSnapshot != null && documentSnapshot.exists() && getView() != null) {
                        try {
                            // Parse and update the event
                            Event updatedEvent = parseEvent(documentSnapshot);
                            if (updatedEvent != null) {
                                event = updatedEvent;
                                // Update the UI with new event data
                                displayEvent();
                                // Re-check user status in case organizer changed or other updates
                                checkUserStatus();
                                Log.d(TAG, "Event updated from Firestore");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing updated event", e);
                        }
                    } else if (documentSnapshot != null && !documentSnapshot.exists()) {
                        // Event was deleted
                        if (getView() != null) {
                            Toast.makeText(requireContext(), "This event has been deleted", Toast.LENGTH_LONG).show();
                            // Go back to home
                            requireActivity().getSupportFragmentManager().popBackStack();
                        }
                    }
                });
    }
    
    private Event parseEvent(com.google.firebase.firestore.DocumentSnapshot document) {
        if (document == null || !document.exists()) {
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

            // Convert Date to Calendar
            java.util.Date registrationStartDate = document.getDate("registrationStartDate");
            if (registrationStartDate != null) {
                Calendar regStartCal = Calendar.getInstance();
                regStartCal.setTime(registrationStartDate);
                event.setRegistrationStartDate(regStartCal);
            }

            java.util.Date eventDate = document.getDate("eventDate");
            if (eventDate != null) {
                Calendar eventCal = Calendar.getInstance();
                eventCal.setTime(eventDate);
                event.setEventDate(eventCal);
            }

            java.util.Date raffleDate = document.getDate("raffleDate");
            if (raffleDate != null) {
                Calendar raffleCal = Calendar.getInstance();
                raffleCal.setTime(raffleDate);
                event.setRaffleDate(raffleCal);
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

    private void setupWaitlistListener() {
        if (event == null || event.getDocumentId() == null) return;
        
        waitlistListener = db.collection("events").document(event.getDocumentId())
                .collection("waitlist")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to waitlist", error);
                        return;
                    }
                    
                    if (querySnapshot != null && event != null) {
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
        
        String deviceId = getOrCreateDeviceId();
        
        // Check if registration window is open
        Calendar now = Calendar.getInstance();
        
        // Check if registration has started
        if (event.getRegistrationStartDate() != null && now.before(event.getRegistrationStartDate())) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String startDate = sdf.format(event.getRegistrationStartDate().getTime());
            showError("Registration opens on " + startDate);
            return;
        }
        
        // Check if registration has closed
        if (event.getRaffleDate() != null && now.after(event.getRaffleDate())) {
            showError("Registration has closed for this event.");
            return;
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

    private void runRaffle() {
        if (event == null || event.getDocumentId() == null) return;
        
        // Calculate how many spots are available (for replacement draws)
        calculateAvailableSpots();
    }
    
    private void viewWaitingList() {
        if (event == null || event.getDocumentId() == null) return;
        
        // Navigate to WaitlistViewActivity
        Intent intent = new Intent(requireContext(), WaitlistViewActivity.class);
        intent.putExtra("eventId", event.getDocumentId());
        intent.putExtra("eventName", event.getName());
        startActivity(intent);
    }
    
    private void viewInvitedEntrants() {
        if (event == null || event.getDocumentId() == null) return;
        
        // Navigate to InvitationListViewActivity
        Intent intent = new Intent(requireContext(), InvitationListViewActivity.class);
        intent.putExtra("eventId", event.getDocumentId());
        intent.putExtra("eventName", event.getName());
        startActivity(intent);
    }
    
    private void viewEnrolledEntrants() {
        if (event == null || event.getDocumentId() == null) return;
        
        // Navigate to AcceptedEntrantsViewActivity
        Intent intent = new Intent(requireContext(), AcceptedEntrantsViewActivity.class);
        intent.putExtra("eventId", event.getDocumentId());
        intent.putExtra("eventName", event.getName());
        intent.putExtra("eventCapacity", event.getCapacity());
        startActivity(intent);
    }
    
    private void viewCancelledEntrants() {
        if (event == null || event.getDocumentId() == null) return;
        
        // Navigate to CancelledEntrantsViewActivity
        Intent intent = new Intent(requireContext(), CancelledEntrantsViewActivity.class);
        intent.putExtra("eventId", event.getDocumentId());
        intent.putExtra("eventName", event.getName());
        startActivity(intent);
    }
    
    private void editEvent() {
        if (event == null || event.getDocumentId() == null) return;
        
        // Navigate to EditEventActivity
        Intent intent = new Intent(requireContext(), EditEventActivity.class);
        intent.putExtra("eventId", event.getDocumentId());
        startActivity(intent);
    }
    
    private void calculateAvailableSpots() {
        String eventId = event.getDocumentId();
        final int[] acceptedCount = {0};
        final int[] pendingInvitations = {0};
        final int[] queriesCompleted = {0};
        
        // Count accepted participants
        db.collection("events").document(eventId)
                .collection("acceptedList")
                .get()
                .addOnSuccessListener(snapshot -> {
                    acceptedCount[0] = snapshot.size();
                    queriesCompleted[0]++;
                    if (queriesCompleted[0] == 2) {
                        showRaffleConfirmation(acceptedCount[0], pendingInvitations[0]);
                    }
                });
        
        // Count pending invitations
        db.collection("events").document(eventId)
                .collection("invitation_list")
                .get()
                .addOnSuccessListener(snapshot -> {
                    pendingInvitations[0] = snapshot.size();
                    queriesCompleted[0]++;
                    if (queriesCompleted[0] == 2) {
                        showRaffleConfirmation(acceptedCount[0], pendingInvitations[0]);
                    }
                });
    }
    
    private void showRaffleConfirmation(int acceptedCount, int pendingInvitations) {
        int capacity = event.getCapacity();
        int occupiedSpots = acceptedCount + pendingInvitations;
        int availableSpots = capacity - occupiedSpots;
        
        String message;
        if (occupiedSpots == 0) {
            // Initial draw
            message = "This will randomly select up to " + capacity + " participants from the waiting list.\n\n" +
                      "Capacity: " + capacity + "\n" +
                      "Available spots: " + capacity + "\n\n" +
                      "Continue?";
        } else if (availableSpots > 0) {
            // Replacement draw
            message = "REPLACEMENT DRAW\n\n" +
                      "Capacity: " + capacity + "\n" +
                      "Accepted: " + acceptedCount + "\n" +
                      "Pending invitations: " + pendingInvitations + "\n" +
                      "Available spots: " + availableSpots + "\n\n" +
                      "This will select " + availableSpots + " replacement(s) from the waiting list.\n\n" +
                      "Continue?";
        } else {
            // No spots available
            showError("No available spots!\n\n" +
                     "Capacity: " + capacity + "\n" +
                     "Accepted: " + acceptedCount + "\n" +
                     "Pending: " + pendingInvitations);
            return;
        }
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Run Raffle?")
                .setMessage(message)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Run Raffle", (dialog, which) -> performRaffleSelection(availableSpots))
                .show();
    }

    private void performRaffleSelection(int spotsToFill) {
        if (event == null || event.getDocumentId() == null) return;
        
        // Get all waitlist entries
        db.collection("events").document(event.getDocumentId())
                .collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        showError("No one is on the waiting list!");
                        return;
                    }
                    
                    // Get list of all user device IDs on waitlist
                    List<String> waitlistUserIds = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        waitlistUserIds.add(doc.getId());
                    });
                    
                    int waitlistSize = waitlistUserIds.size();
                    
                    // Select up to the number of available spots
                    int numToSelect = Math.min(spotsToFill, waitlistSize);
                    
                    if (numToSelect == 0) {
                        showError("No one on the waiting list to select!");
                        return;
                    }
                    
                    // Randomly shuffle and select
                    Collections.shuffle(waitlistUserIds);
                    List<String> selectedUsers = waitlistUserIds.subList(0, numToSelect);
                    List<String> notSelectedUsers = waitlistUserIds.subList(numToSelect, waitlistSize);

                    // Notify those who weren't selected
                    notifyNotSelected(notSelectedUsers);

                    // Move selected users to invitation list
                    moveToInvitationList(selectedUsers, waitlistSize, numToSelect);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error running raffle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void notifyNotSelected(List<String> users){
        if (users.isEmpty()){
            return;
        }
        CollectionReference notifRef = db.collection("notifications");
        DocumentReference countRef = db.collection("notifications").document("count");
        final int usersCount = users.size();
        final AtomicInteger foundUsers = new AtomicInteger(0);
        ArrayList<DocumentReference> notifiedUsers = new ArrayList<>();
        for (String user : users){
            db.collection("profiles").whereEqualTo("deviceId", user).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                if (!queryDocumentSnapshots1.isEmpty()) {
                    DocumentSnapshot profile = queryDocumentSnapshots1.getDocuments().get(0);
                    if (!profile.contains("hasNotificationsEnabled")){
                        profile.getReference().update("hasNotificationsEnabled", "true");
                        notifiedUsers.add(profile.getReference());
                    } else if (profile.getString("hasNotificationsEnabled").equals("true")) {
                        notifiedUsers.add(profile.getReference());
                    }
                }
                int found = foundUsers.incrementAndGet();
                if (found == usersCount) {
                    db.runTransaction(transaction -> {
                        DocumentSnapshot snapshot = transaction.get(countRef);
                        Long count = snapshot.getLong("count");
                        if (count == null) {
                            count = 0L;
                            Map<String, Integer> newCount = new HashMap<>();
                            newCount.put("count", 0);
                            transaction.set(countRef, newCount);
                        }
                        int notifCount = Math.toIntExact(count);
                        for (DocumentReference notifiedUser : notifiedUsers) {
                            Map<String, Object> notif = new HashMap<>();
                            notif.put("deviceId", notifiedUser.getId());
                            notif.put("title", "You were not invited to ".concat(event.getName()).concat("!"));
                            notif.put("description", "You did not win the lottery for this event. Try joining more events.");
                            notif.put("timestamp", FieldValue.serverTimestamp());
                            transaction.set(notifRef.document(String.valueOf(notifCount)), notif);
                            notifCount++;
                        }
                        transaction.update(countRef, "count", notifCount);
                        return notifCount;
                    });
                }
            });
        }
    }
    private void moveToInvitationList(List<String> selectedUsers, int totalWaitlist, int numSelected) {
        if (event == null || event.getDocumentId() == null) return;
        
        // Get current draw round number
        db.collection("events").document(event.getDocumentId())
                .get()
                .addOnSuccessListener(doc -> {
                    Long currentRound = doc.getLong("drawRound");
                    int drawRound = (currentRound != null ? currentRound.intValue() : 0) + 1;
                    
                    // Batch write to move users to invitation_list
                    WriteBatch batch = db.batch();
                    
                    for (String userId : selectedUsers) {
                        // Add to invitation_list (pending acceptance)
                        DocumentReference invitationRef = 
                            db.collection("events").document(event.getDocumentId())
                                .collection("invitation_list").document(userId);
                        
                        Map<String, Object> invitationData = new HashMap<>();
                        invitationData.put("eventId", event.getDocumentId());
                        invitationData.put("userDeviceId", userId);
                        invitationData.put("timestamp", System.currentTimeMillis());
                        invitationData.put("drawRound", drawRound);
                        invitationData.put("status", "pending"); // pending, accepted, declined
                        
                        batch.set(invitationRef, invitationData);
                        
                        // Remove from waitlist
                        DocumentReference waitlistRef = 
                            db.collection("events").document(event.getDocumentId())
                                .collection("waitlist").document(userId);
                        batch.delete(waitlistRef);
                    }
                    
                    // Update draw round in event document
                    DocumentReference eventRef = 
                        db.collection("events").document(event.getDocumentId());
                    batch.update(eventRef, "drawRound", drawRound);
                    batch.update(eventRef, "lastDrawTimestamp", System.currentTimeMillis());
                    
                    // Commit batch
                    batch.commit()
                            .addOnSuccessListener(v -> {
                                String message = "Raffle complete! Selected " + numSelected + " out of " + totalWaitlist + " participants. Invitations sent!";
                                showSuccess(message);
                                
                                // Record in draw history
                                recordDrawHistory(drawRound, numSelected, totalWaitlist);
                                
                                // Refresh organizer status if organizer
                                if (isOrganizer) {
                                    // Slight delay to allow Firestore to update
                                    new Handler().postDelayed(() -> {
                                        showOrganizerStatus();
                                    }, 1000);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Error completing raffle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void recordDrawHistory(int drawRound, int numSelected, int totalWaitlist) {
        if (event == null || event.getDocumentId() == null) return;
        
        Map<String, Object> historyData = new HashMap<>();
        historyData.put("eventId", event.getDocumentId());
        historyData.put("drawRound", drawRound);
        historyData.put("numSelected", numSelected);
        historyData.put("totalWaitlist", totalWaitlist);
        historyData.put("timestamp", System.currentTimeMillis());
        historyData.put("organizerDeviceId", event.getOrganizerDeviceId());
        historyData.put("drawType", drawRound == 1 ? "Initial Draw" : "Replacement Draw");
        
        db.collection("events").document(event.getDocumentId())
                .collection("drawHistory")
                .add(historyData)
                .addOnSuccessListener(doc -> {
                    Log.d(TAG, "Draw history recorded: round " + drawRound + " (" + (drawRound == 1 ? "Initial" : "Replacement") + ")");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to record draw history", e);
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
    
    private void showOrganizerStatus() {
        if (event == null || event.getDocumentId() == null) return;
        
        String eventId = event.getDocumentId();
        final int[] acceptedCount = {0};
        final int[] pendingCount = {0};
        final int[] queriesCompleted = {0};
        
        // Count accepted participants
        db.collection("events").document(eventId)
                .collection("acceptedList")
                .get()
                .addOnSuccessListener(snapshot -> {
                    acceptedCount[0] = snapshot.size();
                    queriesCompleted[0]++;
                    if (queriesCompleted[0] == 2) {
                        displayOrganizerStatus(acceptedCount[0], pendingCount[0]);
                    }
                });
        
        // Count pending invitations
        db.collection("events").document(eventId)
                .collection("invitation_list")
                .get()
                .addOnSuccessListener(snapshot -> {
                    pendingCount[0] = snapshot.size();
                    queriesCompleted[0]++;
                    if (queriesCompleted[0] == 2) {
                        displayOrganizerStatus(acceptedCount[0], pendingCount[0]);
                    }
                });
    }
    
    private void displayOrganizerStatus(int acceptedCount, int pendingCount) {
        int capacity = event.getCapacity();
        int availableSpots = capacity - acceptedCount - pendingCount;
        
        String statusText = "📊 ORGANIZER VIEW\n" +
                           "Capacity: " + capacity + " | " +
                           "Accepted: " + acceptedCount + " | " +
                           "Pending: " + pendingCount + " | " +
                           "Available: " + availableSpots;
        
        if (availableSpots > 0 && pendingCount == 0) {
            statusText += "\n\n💡 You can run a " + (acceptedCount == 0 ? "raffle" : "replacement draw") + " to fill " + availableSpots + " spot(s).";
        } else if (availableSpots > 0 && pendingCount > 0) {
            statusText += "\n\n⏳ Wait for pending invitations to be accepted/declined before running another draw.";
        } else if (availableSpots == 0) {
            statusText += "\n\n✅ Event is at full capacity!";
        }
        
        if (tvStatusMessage != null) {
            tvStatusMessage.setText(statusText);
            tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            tvStatusMessage.setVisibility(View.VISIBLE);
        }
    }

    private void acceptInvitation() {
        if (event == null || event.getDocumentId() == null) return;
        
        String deviceId = getOrCreateDeviceId();
        
        // Move from invitation_list to acceptedList
        db.collection("events").document(event.getDocumentId())
                .collection("invitation_list")
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Map<String, Object> invitationData = doc.getData();
                        
                        // Create accepted list entry
                        Map<String, Object> acceptedData = new HashMap<>(invitationData);
                        acceptedData.put("status", "accepted");
                        acceptedData.put("acceptedAt", System.currentTimeMillis());
                        
                        // Batch operation: add to acceptedList and remove from invitation_list
                        WriteBatch batch = db.batch();
                        
                        // Add to acceptedList
                        DocumentReference acceptedRef = 
                            db.collection("events").document(event.getDocumentId())
                                .collection("acceptedList").document(deviceId);
                        batch.set(acceptedRef, acceptedData);
                        
                        // Remove from invitation_list
                        DocumentReference invitationRef = 
                            db.collection("events").document(event.getDocumentId())
                                .collection("invitation_list").document(deviceId);
                        batch.delete(invitationRef);
                        
                        // Commit batch
                        batch.commit()
                                .addOnSuccessListener(v -> {
                                    showSuccess("✓ Invitation accepted! You're registered for this event.");
                                    hasInvitation = false;
                                    hideInvitationButtons();
                                })
                                .addOnFailureListener(e -> {
                                    showError("Failed to accept invitation. Please try again.");
                                    Log.e(TAG, "Error accepting invitation", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    showError("Error loading invitation. Please try again.");
                    Log.e(TAG, "Error loading invitation", e);
                });
    }
    
    private void declineInvitation() {
        if (event == null || event.getDocumentId() == null) return;
        
        // Confirm decline
        new AlertDialog.Builder(requireContext())
                .setTitle("Decline Invitation?")
                .setMessage("Are you sure you want to decline this invitation? This spot will be offered to someone else.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Decline", (dialog, which) -> performDecline())
                .show();
    }
    
    private void performDecline() {
        String deviceId = getOrCreateDeviceId();
        String eventDocId = event.getDocumentId();
        
        // Get the invitation data first
        db.collection("events").document(eventDocId)
                .collection("invitation_list")
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Map<String, Object> invitationData = doc.getData();
                        
                        // Create cancelled entry data
                        Map<String, Object> cancelledData = new HashMap<>(invitationData);
                        cancelledData.put("status", "declined");
                        cancelledData.put("declinedAt", System.currentTimeMillis());
                        
                        // Move to cancelled_list collection
                        db.collection("events").document(eventDocId)
                                .collection("cancelled_list")
                                .document(deviceId)
                                .set(cancelledData)
                                .addOnSuccessListener(v -> {
                                    // Now delete from invitation_list
                                    db.collection("events").document(eventDocId)
                                            .collection("invitation_list")
                                            .document(deviceId)
                                            .delete()
                                            .addOnSuccessListener(v2 -> {
                                                showSuccess("Invitation declined. Spot will be offered to another participant.");
                                                hasInvitation = false;
                                                hideInvitationButtons();
                                            })
                                            .addOnFailureListener(e -> {
                                                showError("Failed to process decline. Please try again.");
                                                Log.e(TAG, "Error removing from invitation list", e);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    showError("Failed to decline invitation. Please try again.");
                                    Log.e(TAG, "Error adding to cancelled list", e);
                                });
                    } else {
                        showError("Invitation not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    showError("Failed to decline invitation. Please try again.");
                    Log.e(TAG, "Error loading invitation", e);
                });
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
        SharedPreferences sp = requireActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
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
        if (waitlistListener != null) {
            waitlistListener.remove();
            waitlistListener = null;
        }
        if (eventListener != null) {
            eventListener.remove();
            eventListener = null;
        }
    }
}
