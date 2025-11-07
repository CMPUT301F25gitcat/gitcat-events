package com.example.gitcat_events;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.gitcat_events.core.model.Notif;
import com.example.gitcat_events.features.event.ui.NotifArrayAdapter;

import java.util.ArrayList;
import java.util.Date;

public class NotifsFragment extends Fragment {

    private ArrayList<Notif> notifList;
    private NotifArrayAdapter notifAdapter;

    private ListView notifListView;

    public NotifsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifs, container, false);
        notifListView = view.findViewById(R.id.notifsList);

        Notif notif1 = new Notif("Notif1", "You have won the lottery", new Date());
        Notif notif2 = new Notif("Notif2", "Notifications test", new Date());

        notifList = new ArrayList<>();
        notifList.add(notif1);
        notifList.add(notif2);

        notifAdapter = new NotifArrayAdapter(getContext(), notifList);

        notifListView.setAdapter(notifAdapter);

        return view;
    }
}