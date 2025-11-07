package com.example.gitcat_events;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gitcat_events.core.model.Event;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateFragment extends Fragment {

    private static final String TAG = "CreateFragment";
    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id";

    private FirebaseFirestore db;
    private ImageView ivEventPoster;
    private EditText etEventName, etEventDescription, etCapacity, etMaxWaitlist;
    private Button btnSelectPoster, btnSelectEventDate, btnSelectRaffleDate, btnCreateEvent;
    private TextView tvEventDateDisplay, tvRaffleDateDisplay;
    private SwitchMaterial switchGeoLocation;

    private Uri selectedPosterUri;
    private Calendar selectedEventDate;
    private Calendar selectedRaffleDate;

    // Activity result launcher for image selection
    private final ActivityResultLauncher<Intent> posterPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedPosterUri = result.getData().getData();
                    if (ivEventPoster != null && selectedPosterUri != null) {
                        ivEventPoster.setImageURI(selectedPosterUri);
                    }
                }
            });

    public CreateFragment() {
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
        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        ivEventPoster = view.findViewById(R.id.ivEventPoster);
        etEventName = view.findViewById(R.id.etEventName);
        etEventDescription = view.findViewById(R.id.etEventDescription);
        etCapacity = view.findViewById(R.id.etCapacity);
        etMaxWaitlist = view.findViewById(R.id.etMaxWaitlist);
        btnSelectPoster = view.findViewById(R.id.btnSelectPoster);
        btnSelectEventDate = view.findViewById(R.id.btnSelectEventDate);
        btnSelectRaffleDate = view.findViewById(R.id.btnSelectRaffleDate);
        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
        tvEventDateDisplay = view.findViewById(R.id.tvEventDateDisplay);
        tvRaffleDateDisplay = view.findViewById(R.id.tvRaffleDateDisplay);
        switchGeoLocation = view.findViewById(R.id.switchGeoLocation);

        // Set up click listeners
        btnSelectPoster.setOnClickListener(v -> selectPoster());
        btnSelectEventDate.setOnClickListener(v -> selectEventDate());
        btnSelectRaffleDate.setOnClickListener(v -> selectRaffleDate());
        btnCreateEvent.setOnClickListener(v -> createEvent());
    }

    private void selectPoster() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        posterPickerLauncher.launch(intent);
    }

    private void selectEventDate() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedEventDate = Calendar.getInstance();
                    selectedEventDate.set(year, month, dayOfMonth);
                    tvEventDateDisplay.setText(
                            String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                    );
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void selectRaffleDate() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedRaffleDate = Calendar.getInstance();
                    selectedRaffleDate.set(year, month, dayOfMonth);
                    tvRaffleDateDisplay.setText(
                            String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                    );
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void createEvent() {
        // Validate inputs
        String name = etEventName.getText().toString().trim();
        String description = etEventDescription.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String maxWaitlistStr = etMaxWaitlist.getText().toString().trim();

        boolean ok = true;
        if (name.isEmpty()) {
            etEventName.setError("Event name required");
            ok = false;
        }
        if (description.isEmpty()) {
            etEventDescription.setError("Description required");
            ok = false;
        }
        if (capacityStr.isEmpty()) {
            etCapacity.setError("Capacity required");
            ok = false;
        }
        if (selectedEventDate == null) {
            Toast.makeText(getContext(), "Please select an event date", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (selectedRaffleDate == null) {
            Toast.makeText(getContext(), "Please select a raffle date", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (!ok) return;

        int capacity = Integer.parseInt(capacityStr);
        Integer maxWaitlist = maxWaitlistStr.isEmpty() ? null : Integer.parseInt(maxWaitlistStr);
        boolean geoLocationRequired = switchGeoLocation.isChecked();

        // Get organizer ID (current user's profile ID)
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, getContext().MODE_PRIVATE);
        String profileIdStr = prefs.getString(KEY_PROFILE_ID, null);
        if (profileIdStr == null) {
            Toast.makeText(getContext(), "Error: No user profile found", Toast.LENGTH_SHORT).show();
            return;
        }
        int organizerId = Integer.parseInt(profileIdStr);

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Creating event...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Convert poster image to Base64 if selected
        String base64Poster = null;
        if (selectedPosterUri != null) {
            try {
                base64Poster = convertImageToBase64(selectedPosterUri);
            } catch (Exception e) {
                Log.e(TAG, "Error converting poster to Base64", e);
                Toast.makeText(getContext(), "Failed to process poster image. Creating event without poster.", Toast.LENGTH_SHORT).show();
            }
        }

        // Create event object
        Event newEvent = new Event(
                name,
                description,
                capacity,
                maxWaitlist,
                base64Poster,
                selectedRaffleDate,
                selectedEventDate,
                geoLocationRequired
        );
        newEvent.setOrganizer(organizerId);

        // Save to Firestore with auto-incrementing ID
        saveEventToFirestore(newEvent, progressDialog);
    }

    private void saveEventToFirestore(Event event, ProgressDialog progressDialog) {
        db.runTransaction(transaction -> {
            DocumentReference counterRef = db.collection("meta").document("events_counter");
            DocumentSnapshot snap = transaction.get(counterRef);

            long next;
            boolean existed = snap.exists();
            if (existed) {
                Long val = snap.getLong("next");
                next = (val != null) ? val : 0L;
            } else {
                next = 0L; // first event gets ID 0
            }

            String docId = String.valueOf(next);
            DocumentReference eventRef = db.collection("events").document(docId);

            // Prepare event data
            Map<String, Object> data = new HashMap<>();
            data.put("name", event.getName());
            data.put("description", event.getDescription());
            data.put("capacity", event.getCapacity());
            data.put("maxWaitListSize", event.getMaxWaitListSize());
            data.put("eventDate", event.getEventDate().getTime());
            data.put("raffleDate", event.getRaffleDate().getTime());
            data.put("geoLocationRequired", event.getGeoLocationRequired());
            data.put("poster", event.getPoster());
            data.put("organizer", event.getOrganizer());
            data.put("eventId", next);

            transaction.set(eventRef, data);

            // Increment counter
            if (existed) {
                transaction.update(counterRef, "next", next + 1L);
            } else {
                Map<String, Object> counterInit = new HashMap<>();
                counterInit.put("next", next + 1L);
                transaction.set(counterRef, counterInit);
            }

            return docId;
        }).addOnSuccessListener(eventId -> {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Event created successfully! Event ID: " + eventId, Toast.LENGTH_LONG).show();
            
            // Clear form
            clearForm();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Log.e(TAG, "Failed to create event", e);
            Toast.makeText(getContext(), "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private String convertImageToBase64(Uri imageUri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
        if (inputStream == null) throw new Exception("Failed to open input stream");

        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        if (originalBitmap == null) throw new Exception("Failed to decode bitmap");

        // Resize to max 800px for event posters (bigger than profile pics)
        Bitmap resizedBitmap = resizeBitmap(originalBitmap, 800);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.d(TAG, "Poster converted to Base64. Size: " + (base64Image.length() / 1024) + "KB");
        return base64Image;
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = Math.min(
                (float) maxSize / width,
                (float) maxSize / height
        );

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void clearForm() {
        etEventName.setText("");
        etEventDescription.setText("");
        etCapacity.setText("");
        etMaxWaitlist.setText("");
        tvEventDateDisplay.setText("Not selected");
        tvRaffleDateDisplay.setText("Not selected");
        switchGeoLocation.setChecked(false);
        selectedPosterUri = null;
        selectedEventDate = null;
        selectedRaffleDate = null;
        ivEventPoster.setImageResource(R.drawable.ic_launcher_foreground);
    }
}
