package com.example.gitcat_events;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private ImageButton btnBack;
    private Button btnExportCsv;
    
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
        btnExportCsv = findViewById(R.id.btnExportCsv);
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
        
        // Setup export CSV button
        btnExportCsv.setOnClickListener(v -> exportToCsv());

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
            
            btnExportCsv.setEnabled(true);
            adapter.notifyDataSetChanged();
        }
    }

    private void showEmptyState() {
        tvAcceptedListEmpty.setVisibility(View.VISIBLE);
        rvAcceptedList.setVisibility(View.GONE);
        tvAcceptedListCount.setText("Total Enrolled: 0");
        tvCapacityInfo.setVisibility(View.GONE);
        btnExportCsv.setEnabled(false);
    }
    
    private void exportToCsv() {
        if (acceptedEntries == null || acceptedEntries.isEmpty()) {
            Toast.makeText(this, "No enrolled entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Create CSV content
            StringBuilder csvContent = new StringBuilder();
            
            // Add header
            csvContent.append("Position,Name,Email,Phone,Draw Round,Invited Date,Accepted Date\n");
            
            // Add data rows
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            for (int i = 0; i < acceptedEntries.size(); i++) {
                AcceptedEntryDisplay entry = acceptedEntries.get(i);
                
                // Escape CSV fields (wrap in quotes and escape existing quotes)
                String name = escapeCsvField(entry.name != null ? entry.name : "");
                String email = escapeCsvField(entry.email != null ? entry.email : "");
                String phone = escapeCsvField(entry.phone != null ? entry.phone : "");
                String invitedDate = entry.invitedTimestamp != null ? 
                        sdf.format(new Date(entry.invitedTimestamp)) : "";
                String acceptedDate = entry.acceptedTimestamp != null ? 
                        sdf.format(new Date(entry.acceptedTimestamp)) : "";
                
                csvContent.append(String.format("%d,%s,%s,%s,%d,%s,%s\n",
                        i + 1,
                        name,
                        email,
                        phone,
                        entry.drawRound,
                        invitedDate,
                        acceptedDate
                ));
            }
            
            // Create file name with event name and timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String fileName = String.format("%s_enrolled_entrants_%s.csv",
                    sanitizeFileName(eventName != null ? eventName : "Event"),
                    timestamp);
            
            // Save file to cache directory (no permissions needed)
            File cacheDir = getCacheDir();
            File csvFile = new File(cacheDir, fileName);
            
            FileWriter writer = new FileWriter(csvFile);
            writer.write(csvContent.toString());
            writer.close();
            
            // Share the file
            shareFile(csvFile);
            
        } catch (IOException e) {
            Log.e(TAG, "Error creating CSV file", e);
            Toast.makeText(this, "Failed to export CSV: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
        }
    }
    
    private String escapeCsvField(String field) {
        if (field == null) return "\"\"";
        // Wrap in quotes and escape existing quotes by doubling them
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }
    
    private String sanitizeFileName(String name) {
        // Remove or replace characters that are invalid in file names
        return name.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    }
    
    private void shareFile(File file) {
        try {
            // Use FileProvider to share the file
            Uri fileUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );
            
            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, 
                    String.format("%s - Enrolled Entrants", eventName != null ? eventName : "Event"));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Show share dialog
            startActivity(Intent.createChooser(shareIntent, "Export CSV via..."));
            
            Toast.makeText(this, "CSV file ready to share", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error sharing file", e);
            Toast.makeText(this, "Failed to share file: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
        }
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
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

