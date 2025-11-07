package com.example.gitcat_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gitcat_events.core.model.Profile;
import com.example.gitcat_events.features.entrant.ui.ProfileArrayAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WaitingListActivity extends AppCompatActivity {
    private static final String TAG = "WaitingListActivity";
    private static final String PREFS = "app_prefs";
    
    private FirebaseFirestore db;
    private ListView listViewEntrants;
    private TextView tvEmpty;
    private TextView tvTitle;
    private Button btnBack;
    private ProfileArrayAdapter adapter;
    private List<Profile> entrantsList;
    private String eventId;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);
        
        db = FirebaseFirestore.getInstance();
        entrantsList = new ArrayList<>();
        
        
        Intent intent = getIntent(); // getting the event id and the name from the intent. 
        eventId = intent.getStringExtra("eventId");
        eventName = intent.getStringExtra("eventName");
        
        if (eventId == null) {
            Toast.makeText(this, "Error: Event ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        //Intializing the views 
        listViewEntrants = findViewById(R.id.listViewEntrants);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);
        
        // setting the title of =n an activity. 
        if (eventName != null && !eventName.isEmpty()) {
            tvTitle.setText("Waiting List: " + eventName);
        } else {
            tvTitle.setText("Waiting List Entrants");
        }
        
        // Intializing the adapter for the list view of the entrants .
        adapter = new ProfileArrayAdapter(this, entrantsList);
        listViewEntrants.setAdapter(adapter);
        
        // Back button stuff 
        btnBack.setOnClickListener(v -> finish());
        
        //Loading the waiting list entrants.
        loadWaitingListEntrants();
    }
    
    private void loadWaitingListEntrants() {
        if (eventId == null) return;
        
        // Show loading
        tvEmpty.setText("Loading...");
        tvEmpty.setVisibility(View.VISIBLE);
        listViewEntrants.setVisibility(View.GONE);
        
        // Get all waitlist entries for this event
        db.collection("events").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        tvEmpty.setText("No entrants on the waiting list yet.");
                        tvEmpty.setVisibility(View.VISIBLE);
                        listViewEntrants.setVisibility(View.GONE);
                        return;
                    }
                    
                    //collecting all the device ids from waitlist entries. 
                    List<String> deviceIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String deviceId = doc.getString("userDeviceId");
                        if (deviceId != null && !deviceId.isEmpty()) {
                            deviceIds.add(deviceId);
                        }
                    }
                    
                    if (deviceIds.isEmpty()) {
                        tvEmpty.setText("No valid entrants found on the waiting list.");
                        tvEmpty.setVisibility(View.VISIBLE);
                        listViewEntrants.setVisibility(View.GONE);
                        return;
                    }
                    
                    // loading the profiles for each device id. 
                    loadProfilesForDeviceIds(deviceIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading waitlist", e);
                    Toast.makeText(this, "Error loading waiting list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvEmpty.setText("Error loading waiting list.");
                    tvEmpty.setVisibility(View.VISIBLE);
                    listViewEntrants.setVisibility(View.GONE);
                });
    }
    
    private void loadProfilesForDeviceIds(List<String> deviceIds) {
        entrantsList.clear();
        final int[] completed = {0};
        final int total = deviceIds.size();
        
        if (total == 0) {
            tvEmpty.setText("No entrants on the waiting list.");
            tvEmpty.setVisibility(View.VISIBLE);
            listViewEntrants.setVisibility(View.GONE);
            return;
        }
        
        // quertying the profiles collection for each device id. 
        for (String deviceId : deviceIds) {
            db.collection("profiles")
                    .whereEqualTo("deviceId", deviceId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // if we get the profile 
                            QueryDocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            Profile profile = doc.toObject(Profile.class);
                            if (profile != null) {
                                entrantsList.add(profile);
                            } else {
                                // creating a minimal skeleton profile with just the device id if we dont have the req details. 
                                Profile minimalProfile = new Profile(deviceId);
                                entrantsList.add(minimalProfile);
                            }
                        } else {
                            // if we dont have the profile, creating a minimal skeleton profile with just the device id. 
                            Profile minimalProfile = new Profile(deviceId);
                            entrantsList.add(minimalProfile);
                        }
                        
                        completed[0]++;
                        if (completed[0] == total) {
                            // once all the profiles are loaded, updating the ui. 
                            updateUI();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading profile for deviceId: " + deviceId, e);
                        // creating a minimal skeleton profile with just the device id if we encounter an error. 
                        Profile minimalProfile = new Profile(deviceId);
                        entrantsList.add(minimalProfile);
                        
                        completed[0]++;
                        if (completed[0] == total) {
                            updateUI();
                        }
                    });
        }
    }
    
    private void updateUI() {
        if (entrantsList.isEmpty()) {
            tvEmpty.setText("No entrants on the waiting list.");
            tvEmpty.setVisibility(View.VISIBLE);
            listViewEntrants.setVisibility(View.GONE);
        } else {
            // sort by name if possible , otherwise do it by device ID. 
            entrantsList.sort((p1, p2) -> {
                String name1 = p1.getName() != null ? p1.getName() : p1.getDeviceId();
                String name2 = p2.getName() != null ? p2.getName() : p2.getDeviceId();
                return name1.compareToIgnoreCase(name2);
            });
            
            adapter.notifyDataSetChanged();
            tvEmpty.setVisibility(View.GONE);
            listViewEntrants.setVisibility(View.VISIBLE);
        }
    }
}
