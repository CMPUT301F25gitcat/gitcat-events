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

public class ProfileArrayAdapter extends ArrayAdapter<Profile> {
    private final LayoutInflater inflater;

    public ProfileArrayAdapter(@NonNull Context context, @NonNull List<Profile> data) {
        super(context, 0, data);
        this.inflater = LayoutInflater.from(context);
    }

    static class ViewHolder {
        TextView name;
        TextView email;
        TextView phone;
    }

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

            // phone is optional: hide the row if null/blank
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
