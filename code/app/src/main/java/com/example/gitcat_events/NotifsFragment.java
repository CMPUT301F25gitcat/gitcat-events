package com.example.gitcat_events;

import static android.view.View.INVISIBLE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.gitcat_events.core.model.Notif;
import com.example.gitcat_events.features.event.ui.NotifArrayAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NotifsFragment extends Fragment {

    private ArrayList<Notif> notifList;
    private NotifArrayAdapter notifAdapter;
    private FirebaseFirestore db;
    private static final String PREFS = "app_prefs";

    private ListView notifListView;

    public NotifsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_notifs, container, false);
        notifListView = view.findViewById(R.id.notifsList);

        Notif notif1 = new Notif("Notif1", "You have won the lottery", new Date());
        Notif notif2 = new Notif("Notif2", "Notifications test", new Date());
//        DocumentReference count=db.collection("events").document(eventId).collection("acceptedList").document("count");
        notifList = new ArrayList<>();

        String userId = getOrCreateDeviceId();
//        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
//           for (QueryDocumentSnapshot event : queryDocumentSnapshots){
//               db.collection("events").document(event.getId()).collection("acceptedList").get().addOnSuccessListener(queryDocumentSnapshots1 -> {
//                   for (QueryDocumentSnapshot profile : queryDocumentSnapshots1){
//                       if (profile.getId().equals(userId)){
//                           db.collection("events").document(event.getId()).collection("acceptedList").document("notifId").collection("notifItems").get().addOnSuccessListener(queryDocumentSnapshots2 -> {
//                               for (QueryDocumentSnapshot notif : queryDocumentSnapshots2){
//                                   Notif newNotif = new Notif(notif.get("title").toString(), notif.get("description").toString(), new Date());
//                                   notifList.add(newNotif);
//                               }
//                           });
//                       }
//                   }
//
//               });
//           }
//
//        });

        notifList = new ArrayList<>();
        notifAdapter = new NotifArrayAdapter(getContext(), notifList);
        notifListView.setAdapter(notifAdapter);
        for (String list : new String[]{"acceptedList", "waitlist","cancelled_list"}) {

            db.collection("events").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                final int totalEvents = queryDocumentSnapshots.size();
                                final int[] completedEvents = {0};
                                for (QueryDocumentSnapshot event : queryDocumentSnapshots) {
                                    String eventId = event.getId();
                                    db.collection("events")
                                            .document(eventId)
                                            .collection(list)
                                            .get()
                                            .addOnSuccessListener(acceptedListSnapshot -> {
                                                        boolean userFound = false;
                                                        for (QueryDocumentSnapshot profile : acceptedListSnapshot) {
                                                            if (profile.getId().equals(userId)) {
                                                                userFound = true;
                                                                break;
                                                            }
                                                        }
                                                        if (userFound) {
                                                            db.collection("events")
                                                                    .document(eventId)
                                                                    .collection(list)
                                                                    .document("notifID")
                                                                    .collection("notifItems")
                                                                    .get()
                                                                    .addOnSuccessListener(notifSnapshot -> {
                                                                                for (QueryDocumentSnapshot notif : notifSnapshot) {
                                                                                    String title = notif.getString("title");
                                                                                    String description = notif.getString("description");
                                                                                    Date timestamp = notif.getDate("timestamp");

                                                                                    Notif newNotif = new Notif(title, description, timestamp);
                                                                                    notifList.add(newNotif);
                                                                                    Collections.sort(notifList, new Comparator<Notif>() {
                                                                                        @Override
                                                                                        public int compare(Notif notif, Notif t1) {
                                                                                            return t1.getDate().compareTo(notif.getDate());
                                                                                        }
                                                                                    });
                                                                                    notifAdapter.notifyDataSetChanged();
                                                                                }
                                                                            }
                                                                    );

                                                        }
                                                    }
                                            );
                                }

                            }
                    );


        }
        return view;
    }

    private String getOrCreateDeviceId() {
        if (getActivity() == null) return UUID.randomUUID().toString();

        SharedPreferences sp = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String deviceId = sp.getString("device_id", null);

        if (deviceId == null) {
            try {
                deviceId = Settings.Secure.getString(
                        getActivity().getContentResolver(),
                        Settings.Secure.ANDROID_ID
                );
            } catch (Exception e) {
                deviceId = null;
            }

            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
            }

            sp.edit().putString("device_id", deviceId).apply();
        }

        return deviceId;
    }

}