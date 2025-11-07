package com.example.gitcat_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.features.event.ui.EventArrayAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final String PREFS = "app_prefs";
    
    private ArrayList<Event> upcomingEvents;
    private ArrayList<Event> enteredEvents;

    private FirebaseFirestore db;

    private EventArrayAdapter upcomingEventsAdapter;
    private EventArrayAdapter enteredEventsAdapter;

    private ListView enteredEventsList;
    private ListView upcomingEventsList;
    private TextView enteredEventsEmpty;
    private TextView upcomingEventsEmpty;
    
    private FirebaseFirestore db;

    public HomeFragment() {
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
        db = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        enteredEventsList = view.findViewById(R.id.enteredEventsList);
        upcomingEventsList = view.findViewById(R.id.upcomingEventsList);
        enteredEvents = new ArrayList<>();
        upcomingEvents = new ArrayList<>();

        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        System.out.println(name);

                        String description = doc.getString("description");
                        Long capacityLong = doc.getLong("capacity");
                        int capacity = capacityLong != null ? capacityLong.intValue() : 0;

                        Long maxWaitLong = doc.getLong("maxWaitListSize");
                        Integer maxWaitListSize = maxWaitLong != null ? maxWaitLong.intValue() : null;

                        String poster = doc.getString("poster");
                        Boolean geoLocationRequired = doc.getBoolean("geoLocationRequired");

                        // Convert Firestore timestamps to Calendar
                        Timestamp raffleTs = doc.getTimestamp("raffleDate");
                        Timestamp eventTs = doc.getTimestamp("eventDate");

                        Calendar raffleDate = Calendar.getInstance();
                        Calendar eventDate = Calendar.getInstance();

                        if (raffleTs != null) raffleDate.setTime(raffleTs.toDate());
                        if (eventTs != null) eventDate.setTime(eventTs.toDate());

                        Event event = new Event(
                                name,
                                description,
                                capacity,
                                maxWaitListSize,
                                poster,
                                raffleDate,
                                eventDate,
                                geoLocationRequired
                        );

                        upcomingEvents.add(event);
                    }
                    upcomingEventsAdapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(upcomingEventsList);
                    updatePlaceholderText(view);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading events", e);
                });

        // Initialize adapters
        enteredEventsAdapter = new EventArrayAdapter(getContext(), enteredEvents);
        upcomingEventsAdapter = new EventArrayAdapter(getContext(), upcomingEvents);


        // show placeholders if there is no events
        updatePlaceholderText(view);

        enteredEventsList.setAdapter(enteredEventsAdapter);
        upcomingEventsList.setAdapter(upcomingEventsAdapter);

        setListViewHeightBasedOnChildren(enteredEventsList);
        setListViewHeightBasedOnChildren(upcomingEventsList);

        upcomingEventsList.setOnItemClickListener((parent, tmpView, position, id) -> {
            Event selectedEvent = upcomingEvents.get(position);
            System.out.println(selectedEvent);
            EventDetails detailFragment = EventDetails.newInstance(selectedEvent);

            getParentFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out,
                            android.R.anim.fade_in, android.R.anim.fade_out)
                    .add(R.id.frameLayout, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });


        return view;
    }

    public void updatePlaceholderText(View view){
        if(enteredEvents.size() == 0){
            view.findViewById(R.id.EnteredEventsEmpty).setVisibility(View.VISIBLE);;
        } else {
            view.findViewById(R.id.EnteredEventsEmpty).setVisibility(View.GONE);;
        }

        if(upcomingEvents.size() == 0){
            view.findViewById(R.id.upcomingEventsEmpty).setVisibility(View.VISIBLE);;
        } else {
            view.findViewById(R.id.upcomingEventsEmpty).setVisibility(View.GONE);;
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = (130 * listAdapter.getCount());
        float dpHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, totalHeight, listView.getContext().getResources().getDisplayMetrics());

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = (int) dpHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


}