package com.example.gitcat_events;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gitcat_events.core.model.Event;


public class EventDetails extends Fragment {

    private Event event;

    public EventDetails() {}

    public static EventDetails newInstance(Event event) {
        EventDetails fragment = new EventDetails();
        Bundle args = new Bundle();
        args.putSerializable("event", event); // Event must implement Serializable
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        ((TextView)view.findViewById(R.id.eventDetailsName)).setText(event.getName());
        ((TextView)view.findViewById(R.id.eventDetailsDate)).setText(event.getName());
        ((TextView)view.findViewById(R.id.eventDetailsSpots)).setText(String.valueOf(event.getName()));
        ((TextView)view.findViewById(R.id.eventDetailsDesc)).setText(event.getDescription());

        ((TextView)view.findViewById(R.id.eventDetailsEnterDate)).setText(event.getName());

        view.findViewById(R.id.eventDetailsBackBtn).setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.slide_in_left,  // enter animation for fragment below
                            android.R.anim.fade_out // exit animation for this fragment
                    )
                    .remove(EventDetails.this)
                    .commit();
        });


        return view;
    }
}