package com.example.gitcat_events.features.entrant.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gitcat_events.R;
import com.example.gitcat_events.core.model.Profile;

import java.util.List;

/**
 * Custom ArrayAdapter for displaying Profile objects in a ListView.
 * <p>
 * This adapter displays profile information including name, email, and phone number.
 * The phone number field is conditionally displayed - it will be hidden if the phone
 * number is null or empty. Uses the ViewHolder pattern for efficient view recycling.
 * </p>
 * 
 * @author Ryan Chattopadhyay
 * @see Profile
 * @see ArrayAdapter
 */
public class ProfileArrayAdapter extends ArrayAdapter<Profile> {
    private final LayoutInflater inflater;

    /**
     * construing the profileArray Adapter for user profiles to display in a list view.
     * 
     * @param context the context which is used in inflating the layout views.
     * @param data the list of user profiles to display.
     */
    public ProfileArrayAdapter(@NonNull Context context, @NonNull List<Profile> data) {
        super(context, 0, data);
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * viewholder class to cache view references for efficient recycling.
     * 
     * This static inner class holds references to the TextView widgets used to
     * display profile information, avoiding repeated findViewById calls.
     * 
     */
    static class ViewHolder {
        /** TextView displaying the profile name */
        TextView name;
        /** TextView displaying the profile email */
        TextView email;
        /** TextView displaying the profile phone number (may be hidden if empty) */
        TextView phone;
    }

    /**
     * Gets a View that displays the Profile data at the specified position.
     * 
     * 
     * This method implements view recycles if convertView is provided, it will be reused; otherwise, a new view will be
     * inflated from the item_profile layout. T
     * 
     * The phone number TextView will be
     * hidden if the profile's phone number is null or empty.
     * 
     * @param position the poistion of the item to display.
     * @param convertView old view to reuse if possible.
     * @param parent the parent which this view will eventually be attached to.
     * @return a View corresponding to the data at the specified position
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder h;
        View v = convertView;

        if (v == null) {
            v = inflater.inflate(R.layout.item_profile, parent, false);
            h = new ViewHolder();
            h.name  = v.findViewById(R.id.textName);
            h.email = v.findViewById(R.id.textEmail);
            h.phone = v.findViewById(R.id.textPhone);
            v.setTag(h);
        } else {
            h = (ViewHolder) v.getTag();
        }

        Profile p = getItem(position);
        if (p != null) {
            h.name.setText(p.getName());
            h.email.setText(p.getEmail());

            // here the phone number is optional. 
            String ph = p.getPhone();
            if (ph == null || ph.trim().isEmpty()) {
                h.phone.setVisibility(View.GONE);
            } else {
                h.phone.setVisibility(View.VISIBLE);
                h.phone.setText(ph);
            }
        }

        return v;
    }
}
