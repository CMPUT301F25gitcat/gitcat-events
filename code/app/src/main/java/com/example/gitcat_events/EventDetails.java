package com.example.gitcat_events;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

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
    private boolean isOrganizer = false;
    private boolean hasInvitation = false;
    
    private ImageView ivEventPoster;
    private TextView tvEventName, tvEventDate, tvEventSpots, tvEventDesc;
    private TextView tvWaitingListCount, tvStatusMessage, tvSelectionCriteria;
    private Button btnJoinWaitingList, btnBack, btnRunRaffle, btnViewWaitingList, btnViewInvitedEntrants;
    private Button btnAcceptInvitation, btnDeclineInvitation;
    private android.view.ViewGroup invitationButtons;

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
        btnBack = view.findViewById(R.id.eventDetailsBackBtn);
        btnAcceptInvitation = view.findViewById(R.id.btnAcceptInvitation);
        btnDeclineInvitation = view.findViewById(R.id.btnDeclineInvitation);
        invitationButtons = view.findViewById(R.id.invitationButtons);

        // Display event details
        displayEvent();
        
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
        
        // Set up invitation response buttons
        btnAcceptInvitation.setOnClickListener(v -> acceptInvitation());
        btnDeclineInvitation.setOnClickListener(v -> declineInvitation());

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
    }
    
    private void checkUserStatus() {
        if (event == null || event.getDocumentId() == null) return;
        
        String deviceId = getOrCreateDeviceId();
        
        // Check if user is the organizer
        if (event.getOrganizerDeviceId() != null && 
                event.getOrganizerDeviceId().equals(deviceId)) {
            isOrganizer = true;
            btnJoinWaitingList.setVisibility(View.GONE);
            invitationButtons.setVisibility(View.GONE);
            btnRunRaffle.setVisibility(View.VISIBLE);
            btnViewWaitingList.setVisibility(View.VISIBLE);
            btnViewInvitedEntrants.setVisibility(View.VISIBLE);
            
            // Show organizer status
            showOrganizerStatus();
            return;
        }
        
        // Not organizer, hide organizer buttons
        btnRunRaffle.setVisibility(View.GONE);
        btnViewWaitingList.setVisibility(View.GONE);
        btnViewInvitedEntrants.setVisibility(View.GONE);
        
        // Check if user has a pending invitation
        checkInvitationStatus(deviceId);
        
        // Check waitlist status
        db.collection("events").document(event.getDocumentId())
                .collection("waitlist")
                .document(deviceId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error checking waitlist status", error);
                        return;
                    }
                    
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        isOnWaitlist = true;
                        if (!hasInvitation) {
                            updateButtonForWaitlistStatus();
                        }
                    } else {
                        isOnWaitlist = false;
                        if (!hasInvitation) {
                            updateButtonForWaitlistStatus();
                        }
                    }
                });
    }
    
    private void checkInvitationStatus(String deviceId) {
        db.collection("events").document(event.getDocumentId())
                .collection("invitation_list")
                .document(deviceId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error checking invitation status", error);
                        return;
                    }
                    
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
        btnJoinWaitingList.setVisibility(View.GONE);
        invitationButtons.setVisibility(View.VISIBLE);
        if (tvStatusMessage != null) {
            tvStatusMessage.setVisibility(View.VISIBLE);
            tvStatusMessage.setText("🎉 Congratulations! You've been selected for this event!");
            tvStatusMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }
    }
    
    private void hideInvitationButtons() {
        invitationButtons.setVisibility(View.GONE);
        if (!hasInvitation) {
            btnJoinWaitingList.setVisibility(View.VISIBLE);
            updateButtonForWaitlistStatus();
        }
    }
    
    private void updateButtonForWaitlistStatus() {
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
        android.content.Intent intent = new android.content.Intent(requireContext(), WaitlistViewActivity.class);
        intent.putExtra("eventId", event.getDocumentId());
        intent.putExtra("eventName", event.getName());
        startActivity(intent);
    }
    
    private void viewInvitedEntrants() {
        if (event == null || event.getDocumentId() == null) return;
        
        // Navigate to InvitationListViewActivity
        android.content.Intent intent = new android.content.Intent(requireContext(), InvitationListViewActivity.class);
        intent.putExtra("eventId", event.getDocumentId());
        intent.putExtra("eventName", event.getName());
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
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
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
                    java.util.List<String> waitlistUserIds = new java.util.ArrayList<>();
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
                    java.util.Collections.shuffle(waitlistUserIds);
                    java.util.List<String> selectedUsers = waitlistUserIds.subList(0, numToSelect);
                    
                    // Move selected users to invitation list
                    moveToInvitationList(selectedUsers, waitlistSize, numToSelect);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error running raffle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void moveToInvitationList(java.util.List<String> selectedUsers, int totalWaitlist, int numSelected) {
        if (event == null || event.getDocumentId() == null) return;
        
        // Get current draw round number
        db.collection("events").document(event.getDocumentId())
                .get()
                .addOnSuccessListener(doc -> {
                    Long currentRound = doc.getLong("drawRound");
                    int drawRound = (currentRound != null ? currentRound.intValue() : 0) + 1;
                    
                    // Batch write to move users to invitation_list
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    
                    for (String userId : selectedUsers) {
                        // Add to invitation_list (pending acceptance)
                        com.google.firebase.firestore.DocumentReference invitationRef = 
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
                        com.google.firebase.firestore.DocumentReference waitlistRef = 
                            db.collection("events").document(event.getDocumentId())
                                .collection("waitlist").document(userId);
                        batch.delete(waitlistRef);
                    }
                    
                    // Update draw round in event document
                    com.google.firebase.firestore.DocumentReference eventRef = 
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
                                    new android.os.Handler().postDelayed(() -> {
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
                        com.google.firebase.firestore.WriteBatch batch = db.batch();
                        
                        // Add to acceptedList
                        com.google.firebase.firestore.DocumentReference acceptedRef = 
                            db.collection("events").document(event.getDocumentId())
                                .collection("acceptedList").document(deviceId);
                        batch.set(acceptedRef, acceptedData);
                        
                        // Remove from invitation_list
                        com.google.firebase.firestore.DocumentReference invitationRef = 
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
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Decline Invitation?")
                .setMessage("Are you sure you want to decline this invitation? This spot will be offered to someone else.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Decline", (dialog, which) -> performDecline())
                .show();
    }
    
    private void performDecline() {
        String deviceId = getOrCreateDeviceId();
        
        // Update status to declined in invitation_list, then remove
        db.collection("events").document(event.getDocumentId())
                .collection("invitation_list")
                .document(deviceId)
                .update("status", "declined", "declinedAt", System.currentTimeMillis())
                .addOnSuccessListener(v -> {
                    // Remove from invitation_list after marking as declined
                    db.collection("events").document(event.getDocumentId())
                            .collection("invitation_list")
                            .document(deviceId)
                            .delete()
                            .addOnSuccessListener(v2 -> {
                                showSuccess("Invitation declined. Spot will be offered to another participant.");
                                hasInvitation = false;
                                hideInvitationButtons();
                                
                                // Organizer will see updated status automatically when they view their event
                            })
                            .addOnFailureListener(e -> {
                                showError("Failed to process decline. Please try again.");
                            });
                })
                .addOnFailureListener(e -> {
                    showError("Failed to decline invitation. Please try again.");
                    Log.e(TAG, "Error declining invitation", e);
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
        if (waitlistListener != null) {
            waitlistListener.remove();
        }
    }
}
