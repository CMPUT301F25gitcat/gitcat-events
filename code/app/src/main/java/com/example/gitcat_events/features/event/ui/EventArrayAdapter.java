package com.example.gitcat_events.features.event.ui;

import com.example.gitcat_events.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gitcat_events.core.model.Event;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;

/**
 * custom array adapter for displaying Event objects in a ListView.
 * 
 * @author finlay soehn 
 * @see Event
 * @see ArrayAdapter
 */

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

        ImageView poster = view.findViewById(R.id.posterThumbnail);
        if (event.getPoster() != null && !event.getPoster().isEmpty()) {
            loadBase64Image(event.getPoster(), poster);
        } else {
            poster.setImageResource(R.drawable.ic_launcher_foreground);
        }

        return view;
    }

    /**
     * loading base64 Image into ImageView. 
     * 
     * this method is used to load a base64 string into an ImageView.
     * @param base64String
     * @param imageView
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
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
}
