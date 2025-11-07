package com.example.gitcat_events;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.features.event.ui.EventArrayAdapter;

import java.util.ArrayList;
import java.util.Date;

public class HomeFragment extends Fragment {
    private ArrayList<Event> upcomingEvents;
    private ArrayList<Event> enteredEvents;


    private EventArrayAdapter upcomingEventsAdapter;
    private EventArrayAdapter enteredEventsAdapter;

    private ListView enteredEventsList;
    private ListView upcomingEventsList;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        enteredEventsList = view.findViewById(R.id.enteredEventsList);
        upcomingEventsList = view.findViewById(R.id.upcomingEventsList);

//        Event(String name, String description, int capacity, @Nullable Integer maxWaitListSize, String poster, Date
//        raffleDate, Date eventDate)
        Event eventOne = new Event(
                "Event 1",
                "This is a description",
                10,
                        null,
                "blah",
                        new Date(),
                        new Date()
                );
//        Event eventTwo = new Event("Event 2", "This is a description", "Oct 20, 2025");

        enteredEvents = new ArrayList<>();
        upcomingEvents = new ArrayList<>();
        enteredEvents.add(eventOne);
        upcomingEvents.add(eventOne);
        upcomingEvents.add(eventOne);



        enteredEventsAdapter = new EventArrayAdapter(getContext(), enteredEvents);
        upcomingEventsAdapter = new EventArrayAdapter(getContext(), upcomingEvents);

        enteredEventsList.setAdapter(enteredEventsAdapter);
        upcomingEventsList.setAdapter(upcomingEventsAdapter);

        return view;
    }
}