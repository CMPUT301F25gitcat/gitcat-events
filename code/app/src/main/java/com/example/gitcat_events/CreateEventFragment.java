package com.example.gitcat_events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gitcat_events.core.model.Profile;
import com.example.gitcat_events.features.entrant.ui.ProfileDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class CreateEventFragment extends Fragment {//implements CreateEventDialogFragment.OnCreateEventListener {
    private String name;
    private String description;
    private int capacity;
    private Integer maxWaitListSize;
    private Date eventDate;
    private Date raffleDate;
    private String poster;
    private Boolean geoLocationRequired;
    private int organizer;
    private static final String TAG = "CreateEventFragment";
    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id";
    private FirebaseFirestore db;
    private ImageView imgPoster;
    private Button btnEditPoster, btnEventTime, btnRaffleTime, btnCreateEvent, btnCancel;
    private TextInputEditText inpEventName, inpDesc, inpSlotsAvailable;
    private Profile currentProfile;

    public static CreateEventFragment newInstance() {
        return new CreateEventFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //initialise views:
        imgPoster=view.findViewById(R.id.create_event_poster_preview);
        btnEditPoster=view.findViewById(R.id.create_event_edit_poster_button);
        btnEventTime=view.findViewById(R.id.select_event_time_button);
        btnRaffleTime=view.findViewById(R.id.select_raffle_time_button);
        btnCreateEvent=view.findViewById(R.id.create_event_create_button);
        btnCancel=view.findViewById(R.id.create_event_cancel_button);
        inpEventName=view.findViewById(R.id.event_name_text_input);
        inpDesc=view.findViewById(R.id.description_text_input);
        inpSlotsAvailable=view.findViewById(R.id.slots_available_text_input);
    }
}
