package com.example.gitcat_events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gitcat_events.core.model.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/**
 * This fragment displays events created by the current user and provides functionality to create new events via a floating action button.
 */
public class CreateFragment extends Fragment {

    private static final String TAG = "CreateFragment";
    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id";

    private FirebaseFirestore db;
    private RecyclerView recyclerViewEvents;
    private LinearLayout emptyStateContainer;
    private FloatingActionButton fabCreateEvent;
    private EventsAdapter eventsAdapter;
    private List<Event> eventsList = new ArrayList<>();

    public CreateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerViewEvents = view.findViewById(R.id.recyclerViewEvents);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        fabCreateEvent = view.findViewById(R.id.fabCreateEvent);

        // Setup RecyclerView
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsAdapter = new EventsAdapter(eventsList);
        recyclerViewEvents.setAdapter(eventsAdapter);

        // Setup FAB
        fabCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateEventActivity.class);
            startActivity(intent);
        });

        // Load user's events
        loadUserEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload events when returning to this fragment
        loadUserEvents();
    }
    /**
     * Loads and displays events created by the current user from Firestore
     * Shows empty state if no events are found
     */
    private void loadUserEvents() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String profileIdStr = prefs.getString(KEY_PROFILE_ID, null);
        
        if (profileIdStr == null) {
            Toast.makeText(getContext(), "Error: No user profile found", Toast.LENGTH_SHORT).show();
            return;
        }

        int organizerId = Integer.parseInt(profileIdStr);

        db.collection("events")
                .whereEqualTo("organizer", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventsList.clear();
                    
                    // Use a Set to track document IDs and prevent duplicates
                    java.util.Set<String> seenDocumentIds = new java.util.HashSet<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String docId = document.getId();
                            
                            // Skip if we've already seen this document ID
                            if (seenDocumentIds.contains(docId)) {
                                Log.w(TAG, "Duplicate event document ID found: " + docId + ", skipping");
                                continue;
                            }
                            seenDocumentIds.add(docId);
                            
                            Event event = new Event();
                            event.setName(document.getString("name"));
                            event.setDescription(document.getString("description"));
                            
                            Long capacity = document.getLong("capacity");
                            event.setCapacity(capacity != null ? capacity.intValue() : 0);
                            
                            Long maxWaitlist = document.getLong("maxWaitListSize");
                            event.setMaxWaitListSize(maxWaitlist != null ? maxWaitlist.intValue() : null);
                            
                            event.setPoster(document.getString("poster"));
                            
                            Long organizer = document.getLong("organizer");
                            event.setOrganizer(organizer != null ? organizer.intValue() : 0);
                            
                            // Set document ID and organizer device ID
                            event.setDocumentId(docId);
                            event.setOrganizerDeviceId(document.getString("organizerDeviceId"));
                            event.setQrCodeUrl(document.getString("qrCodeUrl"));
                            
                            Boolean geoLocation = document.getBoolean("geoLocationRequired");
                            event.setGeoLocationRequired(geoLocation != null ? geoLocation : false);
                            
                            // Convert Date to Calendar
                            Date registrationStartDate = document.getDate("registrationStartDate");
                            if (registrationStartDate != null) {
                                Calendar regStartCal = Calendar.getInstance();
                                regStartCal.setTime(registrationStartDate);
                                event.setRegistrationStartDate(regStartCal);
                            }
                            
                            Date eventDate = document.getDate("eventDate");
                            if (eventDate != null) {
                                Calendar eventCal = Calendar.getInstance();
                                eventCal.setTime(eventDate);
                                event.setEventDate(eventCal);
                            }
                            
                            Date raffleDate = document.getDate("raffleDate");
                            if (raffleDate != null) {
                                Calendar raffleCal = Calendar.getInstance();
                                raffleCal.setTime(raffleDate);
                                event.setRaffleDate(raffleCal);
                            }
                            
                            eventsList.add(event);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing event: " + document.getId(), e);
                        }
                    }

                    // Sort by event date (most recent first)
                    eventsList.sort((e1, e2) -> {
                        if (e1.getEventDate() == null || e2.getEventDate() == null) return 0;
                        return e2.getEventDate().compareTo(e1.getEventDate());
                    });

                    // Update UI
                    if (eventsList.isEmpty()) {
                        emptyStateContainer.setVisibility(View.VISIBLE);
                        recyclerViewEvents.setVisibility(View.GONE);
                    } else {
                        emptyStateContainer.setVisibility(View.GONE);
                        recyclerViewEvents.setVisibility(View.VISIBLE);
                    }
                    
                    eventsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(getContext(), "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // RecyclerView Adapter
    /**
     * RecyclerView adapter for displaying created events in a list
     */
    private class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
        private List<Event> events;

        EventsAdapter(List<Event> events) {
            this.events = events;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_created_event, parent, false);
            return new EventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            Event event = events.get(position);
            holder.bind(event);
        }

        @Override
        public int getItemCount() {
            return events.size();
        }
        /**
         * ViewHolder for displaying individual event items in the RecyclerView
         */
        class EventViewHolder extends RecyclerView.ViewHolder {
            ImageView ivEventThumbnail;
            TextView tvEventName, tvEventDescription, tvEventDate, tvEventCapacity;
            ImageButton btnEditEvent;

            EventViewHolder(@NonNull View itemView) {
                super(itemView);
                ivEventThumbnail = itemView.findViewById(R.id.ivEventThumbnail);
                tvEventName = itemView.findViewById(R.id.tvEventName);
                tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
                tvEventDate = itemView.findViewById(R.id.tvEventDate);
                tvEventCapacity = itemView.findViewById(R.id.tvEventCapacity);
                btnEditEvent = itemView.findViewById(R.id.btnEditEvent);
            }
            /**
             * Binds event data to the ViewHolder views and sets up click listeners
             * @param event
             * the event to bind to the view
             */
            void bind(Event event) {
                tvEventName.setText(event.getName());
                tvEventDescription.setText(event.getDescription());
                tvEventCapacity.setText("Cap: " + event.getCapacity());

                // Format event date
                if (event.getEventDate() != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    tvEventDate.setText(dateFormat.format(event.getEventDate().getTime()));
                } else {
                    tvEventDate.setText("Date TBD");
                }

                // Load poster image (Base64)
                if (event.getPoster() != null && !event.getPoster().isEmpty()) {
                    loadBase64Image(event.getPoster(), ivEventThumbnail);
                } else {
                    ivEventThumbnail.setImageResource(R.drawable.ic_launcher_foreground);
                }

                // Click listener for event item - navigate to event details
                itemView.setOnClickListener(v -> {
                    // Navigate to EventDetails fragment to show organizer buttons
                    EventDetails detailFragment = EventDetails.newInstance(event);
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out,
                                        android.R.anim.fade_in, android.R.anim.fade_out)
                                .add(R.id.frameLayout, detailFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                });

                // Click listener for edit button - navigate to edit event
                btnEditEvent.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), EditEventActivity.class);
                    intent.putExtra("eventId", event.getDocumentId());
                    startActivity(intent);
                });
            }
        }
    }
    /**
     * Loads and displays a Base64 encoded image in an ImageView
     * @param base64String
     * the Base64 encoded image string
     * @param imageView
     * the ImageView to display the image in
     */
    private void loadBase64Image(String base64String, ImageView imageView) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode Base64 image", e);
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
}
