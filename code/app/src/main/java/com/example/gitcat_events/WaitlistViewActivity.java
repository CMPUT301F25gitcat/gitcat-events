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

import com.example.gitcat_events.core.model.Profile;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity to display the waiting list for an event (organizer only)
 */
public class WaitlistViewActivity extends AppCompatActivity {

    private static final String TAG = "WaitlistViewActivity";
    
    private FirebaseFirestore db;
    private RecyclerView rvWaitlist;
    private TextView tvWaitlistEmpty;
    private TextView tvWaitlistCount;
    private ImageButton btnBack;
    
    private String eventId;
    private String eventName;
    private WaitlistAdapter adapter;
    private List<WaitlistEntryDisplay> waitlistEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_view);

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
        rvWaitlist = findViewById(R.id.rvWaitlist);
        tvWaitlistEmpty = findViewById(R.id.tvWaitlistEmpty);
        tvWaitlistCount = findViewById(R.id.tvWaitlistCount);
        TextView tvEventName = findViewById(R.id.tvEventName);

        // Set event name
        if (eventName != null) {
            tvEventName.setText(eventName + " - Waiting List");
        } else {
            tvEventName.setText("Event Waiting List");
        }

        // Setup RecyclerView
        waitlistEntries = new ArrayList<>();
        adapter = new WaitlistAdapter(waitlistEntries);
        rvWaitlist.setLayoutManager(new LinearLayoutManager(this));
        rvWaitlist.setAdapter(adapter);

        // Setup back button
        btnBack.setOnClickListener(v -> finish());

        // Load waiting list
        loadWaitlist();
    }

    private void loadWaitlist() {
        db.collection("events").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    waitlistEntries.clear();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    // Fetch profile information for each waiting list entry
                    int totalEntries = queryDocumentSnapshots.size();
                    final int[] processedEntries = {0};

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userDeviceId = document.getString("userDeviceId");
                        Long timestamp = document.getLong("timestamp");
                        
                        if (userDeviceId != null) {
                            // Fetch user profile
                            fetchUserProfile(userDeviceId, timestamp, totalEntries, processedEntries);
                        } else {
                            processedEntries[0]++;
                            if (processedEntries[0] == totalEntries) {
                                updateUI();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading waitlist", e);
                    Toast.makeText(this, "Failed to load waiting list: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void fetchUserProfile(String deviceId, Long timestamp, int totalEntries, int[] processedEntries) {
        db.collection("profiles")
                .whereEqualTo("deviceId", deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    WaitlistEntryDisplay entry = new WaitlistEntryDisplay();
                    entry.deviceId = deviceId;
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

                    waitlistEntries.add(entry);
                    processedEntries[0]++;

                    if (processedEntries[0] == totalEntries) {
                        // Sort by timestamp (earliest first)
                        waitlistEntries.sort((e1, e2) -> {
                            if (e1.timestamp == null || e2.timestamp == null) return 0;
                            return Long.compare(e1.timestamp, e2.timestamp);
                        });
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching profile for device: " + deviceId, e);
                    // Add entry without profile info
                    WaitlistEntryDisplay entry = new WaitlistEntryDisplay();
                    entry.deviceId = deviceId;
                    entry.timestamp = timestamp;
                    entry.name = "Unknown User";
                    waitlistEntries.add(entry);
                    
                    processedEntries[0]++;
                    if (processedEntries[0] == totalEntries) {
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (waitlistEntries.isEmpty()) {
            showEmptyState();
        } else {
            tvWaitlistEmpty.setVisibility(View.GONE);
            rvWaitlist.setVisibility(View.VISIBLE);
            tvWaitlistCount.setText("Total Entrants: " + waitlistEntries.size());
            adapter.notifyDataSetChanged();
        }
    }

    private void showEmptyState() {
        tvWaitlistEmpty.setVisibility(View.VISIBLE);
        rvWaitlist.setVisibility(View.GONE);
        tvWaitlistCount.setText("Total Entrants: 0");
    }

    // Data class for displaying waitlist entries
    private static class WaitlistEntryDisplay {
        String deviceId;
        String name;
        String email;
        String phone;
        Long timestamp;
    }

    // RecyclerView Adapter
    private class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.ViewHolder> {
        private List<WaitlistEntryDisplay> entries;

        WaitlistAdapter(List<WaitlistEntryDisplay> entries) {
            this.entries = entries;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_waitlist_entry, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WaitlistEntryDisplay entry = entries.get(position);
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
            TextView tvJoinedDate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPosition = itemView.findViewById(R.id.tvPosition);
                tvName = itemView.findViewById(R.id.tvName);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvJoinedDate = itemView.findViewById(R.id.tvJoinedDate);
            }

            void bind(WaitlistEntryDisplay entry, int position) {
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
                
                if (entry.timestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                    String dateStr = sdf.format(new Date(entry.timestamp));
                    tvJoinedDate.setText("Joined: " + dateStr);
                    tvJoinedDate.setVisibility(View.VISIBLE);
                } else {
                    tvJoinedDate.setVisibility(View.GONE);
                }
            }
        }
    }
}

