package com.example.gitcat_events.features.event.ui;

import com.example.gitcat_events.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gitcat_events.core.model.Event;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;


public class EventArrayAdapter extends ArrayAdapter<Event> {
    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.event_list_item, parent, false);
        } else {
            view = convertView;
        }

        Event event = getItem(position);

        TextView eventName = view.findViewById(R.id.eventCardTitleText);
        TextView eventDescription = view.findViewById(R.id.eventCardSubtitleText);
        TextView eventDate = view.findViewById((R.id.eventCardDateText));


        eventName.setText(event.getName());
        eventDescription.setText(event.getDescription());

        Calendar calendar = event.getEventDate();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        eventDate.setText(formatter.format(calendar.getTime()));

        return view;
    }
}
