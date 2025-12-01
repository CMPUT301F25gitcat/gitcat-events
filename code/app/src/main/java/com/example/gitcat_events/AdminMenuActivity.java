package com.example.gitcat_events;

import android.app.Notification;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gitcat_events.core.model.Notif;
import com.example.gitcat_events.core.model.Profile;
import com.example.gitcat_events.core.model.Image;
import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.features.admin.ImageArrayAdapter;
import com.example.gitcat_events.features.entrant.ui.ProfileArrayAdapter;
import com.example.gitcat_events.features.event.ui.NotifArrayAdapter;
import com.example.gitcat_events.features.event.ui.EventArrayAdapter;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminMenuActivity extends AppCompatActivity {

    private ListView notifsList, profileList, imageList, eventsList;

    private TextView loadingText;
    private EditText searchEventsEditText;

    private ArrayList<Profile> allProfiles;
    private ProfileArrayAdapter profileAdapter;

    private ArrayList<Image> allImages;
    private ImageArrayAdapter imageAdapter;

    private ArrayList<Notif> allNotifs;
    private NotifArrayAdapter notifAdapter;

    private ArrayList<Event> allEvents;
    private ArrayList<Event> filteredEvents;
    private EventArrayAdapter eventAdapter;

    private TabLayout tabLayout;

    private FirebaseFirestore db;

    //    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);

        loadingText = findViewById(R.id.adminLoadingText);

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

        // set up notif list
        allNotifs = new ArrayList<>();
        notifsList = findViewById(R.id.adminNotifsList);
        notifAdapter = new NotifArrayAdapter(this, allNotifs);
        notifsList.setAdapter(notifAdapter);

        notifsList.setOnItemClickListener((parent, view, position, id) -> {
            Notif selected = allNotifs.get(position);
            showNotifDeletePopup(selected);
        });

        // set up events list
        allEvents = new ArrayList<>();
        filteredEvents = new ArrayList<>();
        eventsList = findViewById(R.id.adminEventsList);
        eventAdapter = new EventArrayAdapter(this, filteredEvents);
        eventsList.setAdapter(eventAdapter);

        eventsList.setOnItemClickListener((parent, view, position, id) -> {
            Event selected = filteredEvents.get(position);
            String eventName = selected.getName();
            if (eventName == null) eventName = "Event " + selected.getDocumentId();
            showEventDeletePopup(selected, eventName);
        });

        // set up search field
        searchEventsEditText = findViewById(R.id.adminEventsSearch);
        if (searchEventsEditText != null) {
            searchEventsEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEventsByName(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        Button backBtn = findViewById(R.id.backToProfileBtn);
        backBtn.setOnClickListener(v -> finish());

        loadNotifications();

        // set tab behavior
        tabLayout = findViewById(R.id.adminTabLayout);
        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        int position = tab.getPosition();

                        loadingText.setVisibility(View.VISIBLE);

                        if(position == 0){
                            notifsList.setVisibility(View.VISIBLE);
                            profileList.setVisibility(View.GONE);
                            imageList.setVisibility(View.GONE);
                            eventsList.setVisibility(View.GONE);
                            if (searchEventsEditText != null) searchEventsEditText.setVisibility(View.GONE);
                            loadNotifications();
                        } else if(position == 1){
                            profileList.setVisibility(View.VISIBLE);
                            notifsList.setVisibility(View.GONE);
                            imageList.setVisibility(View.GONE);
                            eventsList.setVisibility(View.GONE);
                            if (searchEventsEditText != null) searchEventsEditText.setVisibility(View.GONE);
                            loadProfiles();
                        } else if(position == 2){
                            imageList.setVisibility(View.VISIBLE);
                            notifsList.setVisibility(View.GONE);
                            profileList.setVisibility(View.GONE);
                            eventsList.setVisibility(View.GONE);
                            if (searchEventsEditText != null) searchEventsEditText.setVisibility(View.GONE);
                            loadImages();
                        } else if(position == 3){
                            eventsList.setVisibility(View.VISIBLE);
                            notifsList.setVisibility(View.GONE);
                            profileList.setVisibility(View.GONE);
                            imageList.setVisibility(View.GONE);
                            if (searchEventsEditText != null) searchEventsEditText.setVisibility(View.VISIBLE);
                            loadEvents();
                        }

                        // handle change
                    }

                    @Override public void onTabUnselected(TabLayout.Tab tab) {
                        int position = tab.getPosition();
                        if(position == 0){
                            notifsList.setVisibility(View.GONE);
                        } else if(position == 1){
                            profileList.setVisibility(View.GONE);
                        } else if(position == 2){
                            imageList.setVisibility(View.GONE);
                        } else if(position == 3){
                            eventsList.setVisibility(View.GONE);
                            if (searchEventsEditText != null) searchEventsEditText.setVisibility(View.GONE);
                        }
                    }

                    @Override public void onTabReselected(TabLayout.Tab tab) {}
                }
        );

    }

    private void loadNotifications(){
        if(!allNotifs.isEmpty()){
            loadingText.setVisibility(View.GONE);
            return;
        }

        db.collection("notifications")
                .get()
                .addOnSuccessListener(notificationsSnapshot -> {
                    List<DocumentSnapshot> allNotifications = notificationsSnapshot.getDocuments();

                    for (DocumentSnapshot notification : allNotifications) {
                        String id = notification.getId();
                        String title = notification.getString("title");
                        String description = notification.getString("description");
                        Date timestamp = notification.getDate("timestamp");

                        Notif newNotif = new Notif(title, description, timestamp);
                        newNotif.setDocumentId(id);

                        if (newNotif.getDate() == null) {
                            continue;
                        }

                        allNotifs.add(newNotif);
                    }
                    Collections.sort(allNotifs, (a, b) -> b.getDate().compareTo(a.getDate()));

                    notifAdapter.notifyDataSetChanged();
                    loadingText.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("Notifications", "Error getting notifications: " + e.getMessage());
                });
    }


    private void loadProfiles() {
        if(!allProfiles.isEmpty()){
            loadingText.setVisibility(View.GONE);
            return;
        }

        db.collection("profiles")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        Profile p = doc.toObject(Profile.class);
                        allProfiles.add(p);
                    }
                    profileAdapter.notifyDataSetChanged();
                    loadingText.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    // handle error
                });
    }

    private void loadImages() {
        if(!allImages.isEmpty()) {
            loadingText.setVisibility(View.GONE);
            return;
        }

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

                                if (imageAdapter != null) {
                                    imageAdapter.notifyDataSetChanged();
                                    loadingText.setVisibility(View.GONE);
                                }

                            }).addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to load event posters: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );

                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile images: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void showNotifDeletePopup(Notif notif) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Manage Notification")
                .setMessage("Choose an action for this notification:\n")
                .setPositiveButton("Delete Notification", (dialog, which) -> deleteNotification(notif))
                .setNeutralButton("Cancel", null)
                .show();
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

    private void deleteNotification(Notif notif) {
        if (notif.getDocumentId() == null) {
            Log.e("Notifications", "Cannot delete notification: missing document ID");
            return;
        }

        db.collection("notifications")
                .document(notif.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    allNotifs.remove(notif);

                    Collections.sort(allNotifs, (a, b) ->
                            b.getDate().compareTo(a.getDate())
                    );

                    notifAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("Notifications", "Error deleting notification: " + e.getMessage()));
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

    private void loadEvents() {
        if (!allEvents.isEmpty()) {
            filterEventsByName(searchEventsEditText != null ? searchEventsEditText.getText().toString() : "");
            loadingText.setVisibility(View.GONE);
            return;
        }

        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allEvents.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Event event = parseEvent(doc);
                        if (event != null) {
                            allEvents.add(event);
                        }
                    }
                    // Sort by event name
                    Collections.sort(allEvents, (e1, e2) -> {
                        String name1 = e1.getName() != null ? e1.getName() : "";
                        String name2 = e2.getName() != null ? e2.getName() : "";
                        return name1.compareToIgnoreCase(name2);
                    });
                    // Apply current filter
                    filterEventsByName(searchEventsEditText != null ? searchEventsEditText.getText().toString() : "");
                    loadingText.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loadingText.setVisibility(View.GONE);
                });
    }

    private void filterEventsByName(String searchQuery) {
        filteredEvents.clear();
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String queryLower = searchQuery.toLowerCase().trim();
            for (Event event : allEvents) {
                String eventName = event.getName();
                if (eventName != null && eventName.toLowerCase().contains(queryLower)) {
                    filteredEvents.add(event);
                }
            }
        }
        eventAdapter.notifyDataSetChanged();
    }

    private Event parseEvent(DocumentSnapshot document) {
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
            event.setQrCodeUrl(document.getString("qrCodeUrl"));

            Boolean geoLocation = document.getBoolean("geoLocationRequired");
            event.setGeoLocationRequired(geoLocation != null ? geoLocation : false);

            // Convert Date to Calendar
            java.util.Date registrationStartDate = document.getDate("registrationStartDate");
            if (registrationStartDate != null) {
                java.util.Calendar regStartCal = java.util.Calendar.getInstance();
                regStartCal.setTime(registrationStartDate);
                event.setRegistrationStartDate(regStartCal);
            }

            java.util.Date eventDate = document.getDate("eventDate");
            if (eventDate != null) {
                java.util.Calendar eventCal = java.util.Calendar.getInstance();
                eventCal.setTime(eventDate);
                event.setEventDate(eventCal);
            }

            java.util.Date raffleDate = document.getDate("raffleDate");
            if (raffleDate != null) {
                java.util.Calendar raffleCal = java.util.Calendar.getInstance();
                raffleCal.setTime(raffleDate);
                event.setRaffleDate(raffleCal);
            }

            // Read eventTypes
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
            }

            return event;
        } catch (Exception e) {
            Log.e("AdminMenu", "Error parsing event: " + document.getId(), e);
            return null;
        }
    }

    private void showEventDeletePopup(Event event, String eventName) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Event?")
                .setMessage("Are you sure you want to delete:\n" + eventName + "\n\nThis will also delete all associated waitlist entries, invitations, and accepted entries.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent(Event event) {
        if (event == null || event.getDocumentId() == null) {
            Toast.makeText(this, "Cannot delete: invalid event", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventId = event.getDocumentId();
        Toast.makeText(this, "Deleting event and all associated data...", Toast.LENGTH_SHORT).show();

        // Delete all subcollections first, then the event document
        deleteSubcollection(eventId, "waitlist", () ->
            deleteSubcollection(eventId, "invitation_list", () ->
                deleteSubcollection(eventId, "acceptedList", () ->
                    deleteSubcollection(eventId, "cancelled_list", () ->
                        // Finally delete the event document
                        db.collection("events").document(eventId)
                                .delete()
                                .addOnSuccessListener(v -> {
                                    allEvents.remove(event);
                                    filterEventsByName(searchEventsEditText != null ? searchEventsEditText.getText().toString() : "");
                                    Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                })
                    )
                )
            )
        );
    }

    private void deleteSubcollection(String eventId, String collectionName, Runnable onComplete) {
        db.collection("events").document(eventId)
                .collection(collectionName)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(v -> onComplete.run())
                            .addOnFailureListener(e -> {
                                Log.e("AdminMenu", "Failed to delete " + collectionName + ": " + e.getMessage());
                                // Continue anyway
                                onComplete.run();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminMenu", "Failed to load " + collectionName + ": " + e.getMessage());
                    // Continue anyway
                    onComplete.run();
                });
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