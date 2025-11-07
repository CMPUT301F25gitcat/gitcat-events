package com.example.gitcat_events;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.gitcat_events.core.model.Profile;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class CreateEventFragment extends Fragment {
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

}
