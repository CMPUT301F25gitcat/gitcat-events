package com.example.gitcat_events;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

import java.util.ArrayList;
import java.util.Calendar;
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

        Event eventOne = new Event("Event 1", "This is a description", 1,10, "poster1", Calendar.getInstance(), Calendar.getInstance(), Boolean.FALSE);
        Event eventTwo = new Event("Event 2", "This is a description", 1, 10, "poster2", Calendar.getInstance(), Calendar.getInstance(), Boolean.FALSE);

        enteredEvents = new ArrayList<>();
        upcomingEvents = new ArrayList<>();
//        enteredEvents.add(eventTwo);
//        enteredEvents.add(eventTwo);
//        enteredEvents.add(eventTwo);
        upcomingEvents.add(eventOne);
        upcomingEvents.add(eventOne);
        upcomingEvents.add(eventOne);
        upcomingEvents.add(eventOne);

        enteredEventsAdapter = new EventArrayAdapter(getContext(), enteredEvents);
        upcomingEventsAdapter = new EventArrayAdapter(getContext(), upcomingEvents);


        // show placeholders if there is no events
        if(enteredEvents.size() == 0){
            view.findViewById(R.id.EnteredEventsEmpty).setVisibility(View.VISIBLE);;
        }

        if(upcomingEvents.size() == 0){
            view.findViewById(R.id.upcomingEventsEmpty).setVisibility(View.VISIBLE);;
        }

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