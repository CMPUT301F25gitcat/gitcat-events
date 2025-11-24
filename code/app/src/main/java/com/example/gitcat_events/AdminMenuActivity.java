package com.example.gitcat_events;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.gitcat_events.core.model.Profile;
import com.example.gitcat_events.core.model.Image;
import com.example.gitcat_events.features.admin.ImageArrayAdapter;
import com.example.gitcat_events.features.entrant.ui.ProfileArrayAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;

public class AdminMenuActivity extends AppCompatActivity {

    private ListView notifsList, profileList, imageList;

    private ArrayList<Profile> allProfiles;
    private ProfileArrayAdapter profileAdapter;

    private ArrayList<Image> allImages;
    private ImageArrayAdapter imageAdapter;

    private TabLayout tabLayout;

    private FirebaseFirestore db;

//    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);

        db = FirebaseFirestore.getInstance();

        // set up profile list
        allProfiles = new ArrayList<>();
        profileList = findViewById(R.id.adminProfilesList);
        profileAdapter = new ProfileArrayAdapter(this, allProfiles);
        profileList.setAdapter(profileAdapter);

        profileList.setOnItemClickListener((parent, view, position, id) -> {
            Profile selected = allProfiles.get(position);
            String name = selected.getName();
            if(name == null) name = selected.getDeviceId();
            showProfileDeletePopup(selected, name);
        });

        // set up image list
        allImages = new ArrayList<>();
        imageList = findViewById(R.id.adminImagesList);
        imageAdapter = new ImageArrayAdapter(this, allImages);
        imageList.setAdapter(imageAdapter);

        imageList.setOnItemClickListener((parent, view, position, id) -> {
            Image selected = allImages.get(position);
            showImageDeletePopup(selected);
        });

        notifsList = findViewById(R.id.adminNotifsList);
        imageList = findViewById(R.id.adminImagesList);

        Button backBtn = findViewById(R.id.backToProfileBtn);
        backBtn.setOnClickListener(v -> finish());

        // set tab behavior
        tabLayout = findViewById(R.id.adminTabLayout);
        tabLayout.addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    System.out.println(position);

                    if(position == 1){
                        profileList.setVisibility(View.VISIBLE);
                        loadProfiles();
                    } else if(position == 2){
                        imageList.setVisibility(View.VISIBLE);
                        loadImages();
                    }

                    // handle change
                }

                @Override public void onTabUnselected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    if(position == 0){
                        notifsList.setVisibility(View.GONE);
                    } else if(position == 1){
                        profileList.setVisibility(View.GONE);
                    } else {
                        imageList.setVisibility(View.GONE);
                    }
                }

                @Override public void onTabReselected(TabLayout.Tab tab) {}
            }
        );

    }

    private void loadProfiles() {
        if(!allProfiles.isEmpty()) return;

        db.collection("profiles")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        Profile p = doc.toObject(Profile.class);
                        allProfiles.add(p);
                    }
                    profileAdapter.notifyDataSetChanged();
                    System.out.println(allProfiles);
                })
                .addOnFailureListener(e -> {
                    // handle error
                });
    }

    private void loadImages() {
        if(!allImages.isEmpty()) return;

        db.collection("profiles")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        String url = doc.getString("profilePictureUrl");
                        String deviceId = doc.getString("deviceId");
                        String name = doc.getString("name");
                        if (url != null && !url.isEmpty()) {
                            Image temp = new Image(url, "Profile", deviceId, name);
                            allImages.add(temp);
                        }
                    }

                    // 2. Load event poster images after profiles
                    db.collection("events")
                            .get()
                            .addOnSuccessListener(eventSnapshot -> {
                                for (DocumentSnapshot eventDoc : eventSnapshot) {
                                    String posterUrl = eventDoc.getString("poster");
                                    String eventId = Long.toString(eventDoc.getLong("eventId"));
                                    String eventName = eventDoc.getString("name");
                                    if (posterUrl != null && !posterUrl.isEmpty()) {
                                        Image temp = new Image(posterUrl, "Event", eventId, eventName);
                                        allImages.add(temp);
                                    }
                                }

                                // Notify your adapter if using one
                                if (imageAdapter != null) {
                                    imageAdapter.notifyDataSetChanged();
                                }

                            }).addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to load event posters: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );

                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile images: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void showProfileDeletePopup(Profile profile, String name) {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Manage Profile")
                .setMessage("Choose an action for:\n" + name)
                .setPositiveButton("Delete Entire Profile", (dialog, which) -> deleteProfile(profile))
                .setNegativeButton("Delete as Organizer", (dialog, which) -> deleteAsOrganizer(profile))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void showImageDeletePopup(Image image) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Manage Image")
                .setPositiveButton("Delete Image", (dialog, which) -> deleteImage(image))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void deleteAsOrganizer(Profile p) {
        String deviceId = p.getDeviceId();
        db.collection("events")
                .whereEqualTo("organizerDeviceId", deviceId)
                .get()
                .addOnSuccessListener(eventSnapshot -> {

                    WriteBatch batch = db.batch();

                    int totalEvents = eventSnapshot.size();
                    final int[] processed = {0};

                    if (totalEvents == 0) {
                        return;
                    }

                    for (DocumentSnapshot eventDoc : eventSnapshot) {
                        String eventId = eventDoc.getId();

                        db.collection("events")
                                .document(eventId)
                                .collection("waitlist")
                                .get()
                                .addOnSuccessListener(waitlistSnapshot -> {

                                    for (DocumentSnapshot w : waitlistSnapshot) {
                                        batch.delete(w.getReference());
                                    }

                                    processed[0]++;

                                    if (processed[0] == totalEvents) {
                                        for (DocumentSnapshot e : eventSnapshot) {
                                            batch.delete(e.getReference());
                                        }

                                        batch.commit()
                                                .addOnFailureListener(err ->
                                                        Toast.makeText(this, "Failed deleting events: " + err.getMessage(), Toast.LENGTH_LONG).show());
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to query events: " + e.getMessage(), Toast.LENGTH_LONG).show());

    }

    private void deleteProfile(Profile p) {
        deleteOrganizedEvents(p,
                () -> deleteUserWaitlistEntries(p,
                        () -> deleteProfileDocument(p)
                )
        );
    }

    private void deleteOrganizedEvents(Profile profile, Runnable onComplete) {
        String deviceId = profile.getDeviceId();

        db.collection("events")
                .whereEqualTo("organizerDeviceId", deviceId)
                .get()
                .addOnSuccessListener(eventSnapshot -> {

                    WriteBatch batch = db.batch();

                    int totalEvents = eventSnapshot.size();
                    final int[] processed = {0};

                    if (totalEvents == 0) {
                        onComplete.run();
                        return;
                    }

                    for (DocumentSnapshot eventDoc : eventSnapshot) {
                        String eventId = eventDoc.getId();

                        db.collection("events")
                                .document(eventId)
                                .collection("waitlist")
                                .get()
                                .addOnSuccessListener(waitlistSnapshot -> {

                                    for (DocumentSnapshot w : waitlistSnapshot) {
                                        batch.delete(w.getReference());
                                    }

                                    processed[0]++;

                                    if (processed[0] == totalEvents) {
                                        for (DocumentSnapshot e : eventSnapshot) {
                                            batch.delete(e.getReference());
                                        }

                                        batch.commit()
                                                .addOnSuccessListener(v ->
                                                        onComplete.run()
                                                )
                                                .addOnFailureListener(err ->
                                                        Toast.makeText(this, "Failed deleting events: " + err.getMessage(), Toast.LENGTH_LONG).show());
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to query events: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void deleteUserWaitlistEntries(Profile p, Runnable onComplete) {
        String deviceId = p.getDeviceId();
        // Delete all waitlist entries by this user (in events they didn't organize)
        db.collectionGroup("waitlist")
                .whereEqualTo("userDeviceId", deviceId)
                .get()
                .addOnSuccessListener(waitlistSnapshot -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : waitlistSnapshot) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit().addOnSuccessListener(v -> {
                        // Finally, delete the profile
                        onComplete.run();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete waitlist entries: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to find waitlist entries: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteProfileDocument(Profile p) {
        String deviceId = p.getDeviceId();
        db.collection("profiles")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(this, "No profile found for deviceId", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot doc : query) {
                        doc.getReference().delete();
                    }

                    allProfiles.remove(p);
                    profileAdapter.notifyDataSetChanged();

                    Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void deleteImage(Image img) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String origin = img.getOrigin();
        String originId = img.getOriginId();

        if ("Profile".equals(origin)) {
            // Clear profile picture for this profile
            db.collection("profiles")
                    .whereEqualTo("deviceId", originId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot.isEmpty()) {
                            Log.d("Firebase", "No profile found for deviceId: " + originId);
                            return;
                        }
                        WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : querySnapshot) {
                            batch.update(doc.getReference(), "profilePictureUrl", null);
                        }
                        batch.commit()
                                .addOnSuccessListener(v -> {
                                    Log.d("Firebase", "Profile image deleted for deviceId: " + originId);
                                    allImages.remove(img);
                                    imageAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> Log.e("Firebase", "Failed to delete profile image: " + e.getMessage()));
                    })
                    .addOnFailureListener(e -> Log.e("Firebase", "Failed to query profile: " + e.getMessage()));

        } else if ("Event".equals(origin)) {
            // Clear poster for this event
            try {
                int eventIdInt = Integer.parseInt(originId); // convert originId to integer
                db.collection("events")
                        .whereEqualTo("eventId", eventIdInt)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot.isEmpty()) {
                                Log.d("Firebase", "No event found for eventId: " + originId);
                                return;
                            }
                            WriteBatch batch = db.batch();
                            for (DocumentSnapshot doc : querySnapshot) {
                                batch.update(doc.getReference(), "poster", null);
                            }
                            batch.commit()
                                    .addOnSuccessListener(v -> {
                                        Log.d("Firebase", "Event poster deleted for eventId: " + originId);
                                        allImages.remove(img);
                                        imageAdapter.notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> Log.e("Firebase", "Failed to delete event poster: " + e.getMessage()));
                        })
                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to query event: " + e.getMessage()));
            } catch (NumberFormatException e) {
                Log.e("Firebase", "Invalid eventId: " + originId);
            }

        } else {
            Log.w("Firebase", "Unknown origin type: " + origin);
        }
    }





}

