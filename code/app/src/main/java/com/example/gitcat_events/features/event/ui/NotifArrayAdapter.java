package com.example.gitcat_events.features.event.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.gitcat_events.R;
import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.core.model.Notif;

import java.util.ArrayList;
import java.util.List;


/**
 * this is a custom array adapter for displaying notifications in a list view.
 * 
 * @author andriy 
 * @see ArrayAdapter
 * @see Notif
 * @see Context
 * @see LayoutInflater
 * @see View
 * @see ViewGroup
 * @see TextView
 * @see ArrayList
 * 
 */
public class NotifArrayAdapter extends ArrayAdapter<Notif> {
    public NotifArrayAdapter(@NonNull Context context, @NonNull ArrayList<Notif> notifs) {
        super(context, 0, notifs);
    }
    /**
     * 
     * this for getting a view for the notification item in the list view.
     * @param position the position of the item in the list view 
     * @param convertView the convert view for the item in the list view 
     * @param parent this is the parent view group for the item in the list view 
     * @return the view for the notification item in the list view 
     */
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.notification_item, parent, false);
        } else {
            view = convertView;
        }

        Notif notif = getItem(position);

        TextView notifTitle = view.findViewById(R.id.notifTitle);
        TextView notifDescription = view.findViewById(R.id.notifDescription);
        TextView notifDate = view.findViewById((R.id.notifDate));

        notifTitle.setText(notif.getTitle());
        notifDescription.setText(notif.getDescription());
        notifDate.setText(notif.getDate().toString());

        return view;
    }
}
