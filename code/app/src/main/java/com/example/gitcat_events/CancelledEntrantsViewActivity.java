package com.example.gitcat_events;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import java.util.Locale;

/**
 * Activity to display the cancelled/declined entrants for an event (organizer only)
 * Shows all entrants who were invited but declined the invitation
 */
public class CancelledEntrantsViewActivity extends AppCompatActivity {

    private static final String TAG = "CancelledEntrantsView";
    
    private FirebaseFirestore db;
    private RecyclerView rvCancelledList;
    private TextView tvCancelledListEmpty;
    private TextView tvCancelledListCount;
    private ImageButton btnBack;
    
    private String eventId;
    private String eventName;
    private CancelledListAdapter adapter;
    private List<CancelledEntryDisplay> cancelledEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancelled_entrants_view);

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
        rvCancelledList = findViewById(R.id.rvCancelledList);
        tvCancelledListEmpty = findViewById(R.id.tvCancelledListEmpty);
        tvCancelledListCount = findViewById(R.id.tvCancelledListCount);
        TextView tvEventName = findViewById(R.id.tvEventName);

        // Set event name
        if (eventName != null) {
            tvEventName.setText(eventName + " - Cancelled Entrants");
        } else {
            tvEventName.setText("Cancelled Entrants");
        }

        // Setup RecyclerView
        cancelledEntries = new ArrayList<>();
        adapter = new CancelledListAdapter(cancelledEntries);
        rvCancelledList.setLayoutManager(new LinearLayoutManager(this));
        rvCancelledList.setAdapter(adapter);

        // Setup back button
        btnBack.setOnClickListener(v -> finish());

        // Load cancelled list
        loadCancelledList();
    }

    private void loadCancelledList() {
        db.collection("events").document(eventId)
                .collection("cancelled_list")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cancelledEntries.clear();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    // Fetch profile information for each cancelled entry
                    int totalEntries = queryDocumentSnapshots.size();
                    final int[] processedEntries = {0};

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userDeviceId = document.getString("userDeviceId");
                        String status = document.getString("status");
                        Long drawRound = document.getLong("drawRound");
                        Long timestamp = document.getLong("timestamp");
                        Long declinedAt = document.getLong("declinedAt");
                        Long cancelledAt = document.getLong("cancelledAt");
                        
                        // Use cancelledAt if available, otherwise declinedAt
                        Long cancellationTime = cancelledAt != null ? cancelledAt : declinedAt;
                        
                        if (userDeviceId != null) {
                            // Fetch user profile
                            fetchUserProfile(userDeviceId, status, drawRound, timestamp, cancellationTime, totalEntries, processedEntries);
                        } else {
                            processedEntries[0]++;
                            if (processedEntries[0] == totalEntries) {
                                updateUI();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cancelled list", e);
                    Toast.makeText(this, "Failed to load cancelled entrants: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void fetchUserProfile(String deviceId, String status, Long drawRound, Long timestamp, Long cancellationTime,
                                   int totalEntries, int[] processedEntries) {
        db.collection("profiles")
                .whereEqualTo("deviceId", deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    CancelledEntryDisplay entry = new CancelledEntryDisplay();
                    entry.deviceId = deviceId;
                    entry.status = status != null ? status : "declined";
                    entry.drawRound = drawRound != null ? drawRound.intValue() : 1;
                    entry.invitedTimestamp = timestamp;
                    entry.declinedTimestamp = cancellationTime;

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

                    cancelledEntries.add(entry);
                    processedEntries[0]++;

                    if (processedEntries[0] == totalEntries) {
                        // Sort by declined timestamp (most recent first)
                        cancelledEntries.sort((e1, e2) -> {
                            if (e1.declinedTimestamp == null || e2.declinedTimestamp == null) {
                                // Fall back to draw round if no declined timestamp
                                return Integer.compare(e2.drawRound, e1.drawRound);
                            }
                            return Long.compare(e2.declinedTimestamp, e1.declinedTimestamp);
                        });
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching profile for device: " + deviceId, e);
                    // Add entry without profile info
                    CancelledEntryDisplay entry = new CancelledEntryDisplay();
                    entry.deviceId = deviceId;
                    entry.status = status != null ? status : "declined";
                    entry.drawRound = drawRound != null ? drawRound.intValue() : 1;
                    entry.invitedTimestamp = timestamp;
                    entry.declinedTimestamp = cancellationTime;
                    entry.name = "Unknown User";
                    cancelledEntries.add(entry);
                    
                    processedEntries[0]++;
                    if (processedEntries[0] == totalEntries) {
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (cancelledEntries.isEmpty()) {
            showEmptyState();
        } else {
            tvCancelledListEmpty.setVisibility(View.GONE);
            rvCancelledList.setVisibility(View.VISIBLE);
            tvCancelledListCount.setText("Total Cancelled: " + cancelledEntries.size());
            adapter.notifyDataSetChanged();
        }
    }

    private void showEmptyState() {
        tvCancelledListEmpty.setVisibility(View.VISIBLE);
        rvCancelledList.setVisibility(View.GONE);
        tvCancelledListCount.setText("Total Cancelled: 0");
    }

    // Data class for displaying cancelled entries
    private static class CancelledEntryDisplay {
        String deviceId;
        String name;
        String email;
        String phone;
        String status; // "declined" or "cancelled_by_organizer"
        int drawRound;
        Long invitedTimestamp;
        Long declinedTimestamp;
    }

    // RecyclerView Adapter
    private class CancelledListAdapter extends RecyclerView.Adapter<CancelledListAdapter.ViewHolder> {
        private List<CancelledEntryDisplay> entries;

        CancelledListAdapter(List<CancelledEntryDisplay> entries) {
            this.entries = entries;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_cancelled_entry, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CancelledEntryDisplay entry = entries.get(position);
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
            TextView tvDrawRound;
            TextView tvInvitedDate;
            TextView tvDeclinedDate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPosition = itemView.findViewById(R.id.tvPosition);
                tvName = itemView.findViewById(R.id.tvName);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvDrawRound = itemView.findViewById(R.id.tvDrawRound);
                tvInvitedDate = itemView.findViewById(R.id.tvInvitedDate);
                tvDeclinedDate = itemView.findViewById(R.id.tvDeclinedDate);
            }

            void bind(CancelledEntryDisplay entry, int position) {
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
                
                // Draw round
                tvDrawRound.setText("Draw Round: " + entry.drawRound);
                
                // Invited timestamp
                if (entry.invitedTimestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                    String dateStr = sdf.format(new Date(entry.invitedTimestamp));
                    tvInvitedDate.setText("Invited: " + dateStr);
                    tvInvitedDate.setVisibility(View.VISIBLE);
                } else {
                    tvInvitedDate.setVisibility(View.GONE);
                }
                
                // Declined/Cancelled timestamp
                if (entry.declinedTimestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                    String dateStr = sdf.format(new Date(entry.declinedTimestamp));
                    
                    if ("cancelled_by_organizer".equals(entry.status)) {
                        tvDeclinedDate.setText("🚫 Cancelled by Organizer: " + dateStr);
                        tvDeclinedDate.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    } else {
                        tvDeclinedDate.setText("❌ Declined by User: " + dateStr);
                        tvDeclinedDate.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                    tvDeclinedDate.setVisibility(View.VISIBLE);
                } else {
                    tvDeclinedDate.setVisibility(View.GONE);
                }
            }
        }
    }
}

