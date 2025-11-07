package com.example.gitcat_events;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
 * Activity to display the accepted/enrolled entrants for an event (organizer only)
 * Shows all entrants who accepted their invitation and are confirmed for the event
 */
public class AcceptedEntrantsViewActivity extends AppCompatActivity {

    private static final String TAG = "AcceptedEntrantsView";
    
    private FirebaseFirestore db;
    private RecyclerView rvAcceptedList;
    private TextView tvAcceptedListEmpty;
    private TextView tvAcceptedListCount;
    private TextView tvCapacityInfo;
    private android.widget.ImageButton btnBack;
    
    private String eventId;
    private String eventName;
    private int eventCapacity;
    private AcceptedListAdapter adapter;
    private List<AcceptedEntryDisplay> acceptedEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_entrants_view);

        db = FirebaseFirestore.getInstance();

        // Get event ID, name, and capacity from intent
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        eventCapacity = getIntent().getIntExtra("eventCapacity", 0);
        
        if (eventId == null) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        rvAcceptedList = findViewById(R.id.rvAcceptedList);
        tvAcceptedListEmpty = findViewById(R.id.tvAcceptedListEmpty);
        tvAcceptedListCount = findViewById(R.id.tvAcceptedListCount);
        tvCapacityInfo = findViewById(R.id.tvCapacityInfo);
        TextView tvEventName = findViewById(R.id.tvEventName);

        // Set event name
        if (eventName != null) {
            tvEventName.setText(eventName + " - Enrolled Entrants");
        } else {
            tvEventName.setText("Enrolled Entrants");
        }

        // Setup RecyclerView
        acceptedEntries = new ArrayList<>();
        adapter = new AcceptedListAdapter(acceptedEntries);
        rvAcceptedList.setLayoutManager(new LinearLayoutManager(this));
        rvAcceptedList.setAdapter(adapter);

        // Setup back button
        btnBack.setOnClickListener(v -> finish());

        // Load accepted list
        loadAcceptedList();
    }

    private void loadAcceptedList() {
        db.collection("events").document(eventId)
                .collection("acceptedList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    acceptedEntries.clear();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    // Fetch profile information for each accepted entry
                    int totalEntries = queryDocumentSnapshots.size();
                    final int[] processedEntries = {0};

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userDeviceId = document.getString("userDeviceId");
                        String status = document.getString("status");
                        Long drawRound = document.getLong("drawRound");
                        Long timestamp = document.getLong("timestamp");
                        Long acceptedAt = document.getLong("acceptedAt");
                        
                        if (userDeviceId != null) {
                            // Fetch user profile
                            fetchUserProfile(userDeviceId, status, drawRound, timestamp, acceptedAt, totalEntries, processedEntries);
                        } else {
                            processedEntries[0]++;
                            if (processedEntries[0] == totalEntries) {
                                updateUI();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading accepted list", e);
                    Toast.makeText(this, "Failed to load enrolled entrants: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void fetchUserProfile(String deviceId, String status, Long drawRound, Long timestamp, Long acceptedAt,
                                   int totalEntries, int[] processedEntries) {
        db.collection("profiles")
                .whereEqualTo("deviceId", deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    AcceptedEntryDisplay entry = new AcceptedEntryDisplay();
                    entry.deviceId = deviceId;
                    entry.status = status != null ? status : "accepted";
                    entry.drawRound = drawRound != null ? drawRound.intValue() : 1;
                    entry.invitedTimestamp = timestamp;
                    entry.acceptedTimestamp = acceptedAt;

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

                    acceptedEntries.add(entry);
                    processedEntries[0]++;

                    if (processedEntries[0] == totalEntries) {
                        // Sort by accepted timestamp (most recent first)
                        acceptedEntries.sort((e1, e2) -> {
                            if (e1.acceptedTimestamp == null || e2.acceptedTimestamp == null) {
                                // Fall back to draw round if no accepted timestamp
                                return Integer.compare(e2.drawRound, e1.drawRound);
                            }
                            return Long.compare(e2.acceptedTimestamp, e1.acceptedTimestamp);
                        });
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching profile for device: " + deviceId, e);
                    // Add entry without profile info
                    AcceptedEntryDisplay entry = new AcceptedEntryDisplay();
                    entry.deviceId = deviceId;
                    entry.status = status != null ? status : "accepted";
                    entry.drawRound = drawRound != null ? drawRound.intValue() : 1;
                    entry.invitedTimestamp = timestamp;
                    entry.acceptedTimestamp = acceptedAt;
                    entry.name = "Unknown User";
                    acceptedEntries.add(entry);
                    
                    processedEntries[0]++;
                    if (processedEntries[0] == totalEntries) {
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (acceptedEntries.isEmpty()) {
            showEmptyState();
        } else {
            tvAcceptedListEmpty.setVisibility(View.GONE);
            rvAcceptedList.setVisibility(View.VISIBLE);
            
            int enrolledCount = acceptedEntries.size();
            tvAcceptedListCount.setText("Total Enrolled: " + enrolledCount);
            
            // Show capacity info
            if (eventCapacity > 0) {
                int spotsRemaining = eventCapacity - enrolledCount;
                String capacityText;
                if (spotsRemaining > 0) {
                    capacityText = "Capacity: " + enrolledCount + " / " + eventCapacity + 
                                  " (" + spotsRemaining + " spot" + (spotsRemaining != 1 ? "s" : "") + " remaining)";
                } else if (spotsRemaining == 0) {
                    capacityText = "✅ Event at full capacity: " + enrolledCount + " / " + eventCapacity;
                } else {
                    capacityText = "⚠️ Over capacity: " + enrolledCount + " / " + eventCapacity;
                }
                tvCapacityInfo.setText(capacityText);
                tvCapacityInfo.setVisibility(View.VISIBLE);
            } else {
                tvCapacityInfo.setVisibility(View.GONE);
            }
            
            adapter.notifyDataSetChanged();
        }
    }

    private void showEmptyState() {
        tvAcceptedListEmpty.setVisibility(View.VISIBLE);
        rvAcceptedList.setVisibility(View.GONE);
        tvAcceptedListCount.setText("Total Enrolled: 0");
        tvCapacityInfo.setVisibility(View.GONE);
    }

    // Data class for displaying accepted entries
    private static class AcceptedEntryDisplay {
        String deviceId;
        String name;
        String email;
        String phone;
        String status;
        int drawRound;
        Long invitedTimestamp;
        Long acceptedTimestamp;
    }

    // RecyclerView Adapter
    private class AcceptedListAdapter extends RecyclerView.Adapter<AcceptedListAdapter.ViewHolder> {
        private List<AcceptedEntryDisplay> entries;

        AcceptedListAdapter(List<AcceptedEntryDisplay> entries) {
            this.entries = entries;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_accepted_entry, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AcceptedEntryDisplay entry = entries.get(position);
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
            TextView tvAcceptedDate;
            TextView tvStatusBadge;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPosition = itemView.findViewById(R.id.tvPosition);
                tvName = itemView.findViewById(R.id.tvName);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvDrawRound = itemView.findViewById(R.id.tvDrawRound);
                tvInvitedDate = itemView.findViewById(R.id.tvInvitedDate);
                tvAcceptedDate = itemView.findViewById(R.id.tvAcceptedDate);
                tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            }

            void bind(AcceptedEntryDisplay entry, int position) {
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
                
                // Accepted timestamp
                if (entry.acceptedTimestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                    String dateStr = sdf.format(new Date(entry.acceptedTimestamp));
                    tvAcceptedDate.setText("✓ Accepted: " + dateStr);
                    tvAcceptedDate.setVisibility(View.VISIBLE);
                    tvAcceptedDate.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    tvAcceptedDate.setVisibility(View.GONE);
                }
                
                // Status badge
                tvStatusBadge.setText("✅ CONFIRMED");
                tvStatusBadge.setVisibility(View.VISIBLE);
            }
        }
    }
}

