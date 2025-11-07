package com.example.gitcat_events;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity to display the invitation list for an event (organizer only)
 * Shows all entrants who have been selected/invited but haven't responded yet
 */
public class InvitationListViewActivity extends AppCompatActivity {

    private static final String TAG = "InvitationListView";
    
    private FirebaseFirestore db;
    private RecyclerView rvInvitationList;
    private TextView tvInvitationListEmpty;
    private TextView tvInvitationListCount;
    private ImageButton btnBack;
    
    private String eventId;
    private String eventName;
    private InvitationListAdapter adapter;
    private List<InvitationEntryDisplay> invitationEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_list_view);

        db = FirebaseFirestore.getInstance();

        // Get event ID and name from intent
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        
        if (eventId == null) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        rvInvitationList = findViewById(R.id.rvInvitationList);
        tvInvitationListEmpty = findViewById(R.id.tvInvitationListEmpty);
        tvInvitationListCount = findViewById(R.id.tvInvitationListCount);
        TextView tvEventName = findViewById(R.id.tvEventName);

        // Set event name
        if (eventName != null) {
            tvEventName.setText(eventName + " - Invited Entrants");
        } else {
            tvEventName.setText("Event Invitation List");
        }

        // Setup RecyclerView
        invitationEntries = new ArrayList<>();
        adapter = new InvitationListAdapter(invitationEntries);
        rvInvitationList.setLayoutManager(new LinearLayoutManager(this));
        rvInvitationList.setAdapter(adapter);

        // Setup back button
        btnBack.setOnClickListener(v -> finish());

        // Load invitation list
        loadInvitationList();
    }

    private void loadInvitationList() {
        db.collection("events").document(eventId)
                .collection("invitation_list")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    invitationEntries.clear();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    // Fetch profile information for each invitation list entry
                    int totalEntries = queryDocumentSnapshots.size();
                    final int[] processedEntries = {0};

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userDeviceId = document.getString("userDeviceId");
                        String status = document.getString("status");
                        Long drawRound = document.getLong("drawRound");
                        Long timestamp = document.getLong("timestamp");
                        
                        if (userDeviceId != null) {
                            // Fetch user profile
                            fetchUserProfile(userDeviceId, status, drawRound, timestamp, totalEntries, processedEntries);
                        } else {
                            processedEntries[0]++;
                            if (processedEntries[0] == totalEntries) {
                                updateUI();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading invitation list", e);
                    Toast.makeText(this, "Failed to load invitation list: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void fetchUserProfile(String deviceId, String status, Long drawRound, Long timestamp, 
                                   int totalEntries, int[] processedEntries) {
        db.collection("profiles")
                .whereEqualTo("deviceId", deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    InvitationEntryDisplay entry = new InvitationEntryDisplay();
                    entry.deviceId = deviceId;
                    entry.status = status != null ? status : "pending";
                    entry.drawRound = drawRound != null ? drawRound.intValue() : 1;
                    entry.timestamp = timestamp;

                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot profileDoc = querySnapshot.getDocuments().get(0);
                        entry.name = profileDoc.getString("name");
                        entry.email = profileDoc.getString("email");
                        entry.phone = profileDoc.getString("phone");
                        
                        // Handle null/empty names
                        if (entry.name == null || entry.name.trim().isEmpty()) {
                            entry.name = "Anonymous User";
                        }
                    } else {
                        // No profile found, use device ID
                        entry.name = "User (No Profile)";
                        entry.email = null;
                        entry.phone = null;
                    }

                    invitationEntries.add(entry);
                    processedEntries[0]++;

                    if (processedEntries[0] == totalEntries) {
                        // Sort by draw round, then by timestamp (most recent first)
                        invitationEntries.sort((e1, e2) -> {
                            int roundCompare = Integer.compare(e2.drawRound, e1.drawRound);
                            if (roundCompare != 0) return roundCompare;
                            if (e1.timestamp == null || e2.timestamp == null) return 0;
                            return Long.compare(e2.timestamp, e1.timestamp);
                        });
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching profile for device: " + deviceId, e);
                    // Add entry without profile info
                    InvitationEntryDisplay entry = new InvitationEntryDisplay();
                    entry.deviceId = deviceId;
                    entry.status = status != null ? status : "pending";
                    entry.drawRound = drawRound != null ? drawRound.intValue() : 1;
                    entry.timestamp = timestamp;
                    entry.name = "Unknown User";
                    invitationEntries.add(entry);
                    
                    processedEntries[0]++;
                    if (processedEntries[0] == totalEntries) {
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (invitationEntries.isEmpty()) {
            showEmptyState();
        } else {
            tvInvitationListEmpty.setVisibility(View.GONE);
            rvInvitationList.setVisibility(View.VISIBLE);
            
            // Count pending vs responded
            long pendingCount = invitationEntries.stream().filter(e -> "pending".equals(e.status)).count();
            tvInvitationListCount.setText("Total Invited: " + invitationEntries.size() + 
                    " (Pending: " + pendingCount + ")");
            
            adapter.notifyDataSetChanged();
        }
    }

    private void showEmptyState() {
        tvInvitationListEmpty.setVisibility(View.VISIBLE);
        rvInvitationList.setVisibility(View.GONE);
        tvInvitationListCount.setText("Total Invited: 0");
    }
    
    private void showCancelConfirmation(InvitationEntryDisplay entry) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancel Invitation?")
                .setMessage("Cancel invitation for " + entry.name + "?\n\nThis will remove them from the invited list and free up a spot for a replacement draw.")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelInvitation(entry))
                .show();
    }
    
    private void cancelInvitation(InvitationEntryDisplay entry) {
        if (eventId == null || entry.deviceId == null) return;
        
        // Show loading
        Toast.makeText(this, "Cancelling invitation...", Toast.LENGTH_SHORT).show();
        
        // Get the invitation data first, then move it to cancelled_list
        db.collection("events").document(eventId)
                .collection("invitation_list")
                .document(entry.deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Map<String, Object> invitationData = doc.getData();
                        
                        // Create cancelled entry data
                        Map<String, Object> cancelledData = new HashMap<>(invitationData);
                        cancelledData.put("status", "cancelled_by_organizer");
                        cancelledData.put("cancelledAt", System.currentTimeMillis());
                        
                        // Move to cancelled_list collection
                        db.collection("events").document(eventId)
                                .collection("cancelled_list")
                                .document(entry.deviceId)
                                .set(cancelledData)
                                .addOnSuccessListener(v -> {
                                    // Now delete from invitation_list
                                    db.collection("events").document(eventId)
                                            .collection("invitation_list")
                                            .document(entry.deviceId)
                                            .delete()
                                            .addOnSuccessListener(v2 -> {
                                                Toast.makeText(this, "Invitation cancelled successfully", Toast.LENGTH_SHORT).show();
                                                // Reload the list
                                                loadInvitationList();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error removing from invitation list", e);
                                                Toast.makeText(this, "Failed to cancel invitation", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding to cancelled list", e);
                                    Toast.makeText(this, "Failed to cancel invitation", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Invitation not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading invitation", e);
                    Toast.makeText(this, "Failed to cancel invitation", Toast.LENGTH_SHORT).show();
                });
    }

    // Data class for displaying invitation entries
    private static class InvitationEntryDisplay {
        String deviceId;
        String name;
        String email;
        String phone;
        String status; // "pending", "accepted", "declined"
        int drawRound;
        Long timestamp;
    }

    // RecyclerView Adapter
    private class InvitationListAdapter extends RecyclerView.Adapter<InvitationListAdapter.ViewHolder> {
        private List<InvitationEntryDisplay> entries;

        InvitationListAdapter(List<InvitationEntryDisplay> entries) {
            this.entries = entries;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_invitation_entry, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            InvitationEntryDisplay entry = entries.get(position);
            holder.bind(entry, position + 1);
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPosition;
            TextView tvName;
            TextView tvEmail;
            TextView tvPhone;
            TextView tvStatus;
            TextView tvDrawRound;
            TextView tvInvitedDate;
            Button btnCancelInvitation;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPosition = itemView.findViewById(R.id.tvPosition);
                tvName = itemView.findViewById(R.id.tvName);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvDrawRound = itemView.findViewById(R.id.tvDrawRound);
                tvInvitedDate = itemView.findViewById(R.id.tvInvitedDate);
                btnCancelInvitation = itemView.findViewById(R.id.btnCancelInvitation);
            }

            void bind(InvitationEntryDisplay entry, int position) {
                tvPosition.setText("#" + position);
                tvName.setText(entry.name != null ? entry.name : "Anonymous User");
                
                if (entry.email != null && !entry.email.isEmpty()) {
                    tvEmail.setText(entry.email);
                    tvEmail.setVisibility(View.VISIBLE);
                } else {
                    tvEmail.setVisibility(View.GONE);
                }
                
                if (entry.phone != null && !entry.phone.isEmpty()) {
                    tvPhone.setText(entry.phone);
                    tvPhone.setVisibility(View.VISIBLE);
                } else {
                    tvPhone.setVisibility(View.GONE);
                }
                
                // Status indicator
                if ("pending".equals(entry.status)) {
                    tvStatus.setText("⏳ Pending Response");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    // Show cancel button only for pending invitations
                    btnCancelInvitation.setVisibility(View.VISIBLE);
                    btnCancelInvitation.setOnClickListener(v -> showCancelConfirmation(entry));
                } else if ("accepted".equals(entry.status)) {
                    tvStatus.setText("✓ Accepted");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    btnCancelInvitation.setVisibility(View.GONE);
                } else if ("declined".equals(entry.status)) {
                    tvStatus.setText("✗ Declined");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    btnCancelInvitation.setVisibility(View.GONE);
                } else {
                    tvStatus.setText("Unknown");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    btnCancelInvitation.setVisibility(View.GONE);
                }
                
                // Draw round
                tvDrawRound.setText("Draw Round: " + entry.drawRound);
                
                if (entry.timestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                    String dateStr = sdf.format(new Date(entry.timestamp));
                    tvInvitedDate.setText("Invited: " + dateStr);
                    tvInvitedDate.setVisibility(View.VISIBLE);
                } else {
                    tvInvitedDate.setVisibility(View.GONE);
                }
            }
        }
    }
}

