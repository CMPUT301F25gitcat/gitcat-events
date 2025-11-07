package com.example.gitcat_events;

import android.annotation.SuppressLint;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Base64;
import android.util.Log;
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

public class CreateEventActivity extends AppCompatActivity {

    private static final String TAG = "CreateEventActivity";
    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id";

    private FirebaseFirestore db;
    private ImageView ivEventPoster;
    private EditText etEventName, etEventDescription, etCapacity, etMaxWaitlist, etSelectionCriteria;
    private Button btnSelectPoster, btnSelectRegistrationStartDate, btnSelectEventDate, btnSelectRaffleDate, btnCreateEvent;
    private TextView tvRegistrationStartDateDisplay, tvEventDateDisplay, tvRaffleDateDisplay;
    private SwitchMaterial switchGeoLocation;

    private Uri selectedPosterUri;
    private Calendar selectedRegistrationStartDate;
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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        ivEventPoster = findViewById(R.id.ivEventPoster);
        etEventName = findViewById(R.id.etEventName);
        etEventDescription = findViewById(R.id.etEventDescription);
        etCapacity = findViewById(R.id.etCapacity);
        etMaxWaitlist = findViewById(R.id.etMaxWaitlist);
        btnSelectPoster = findViewById(R.id.btnSelectPoster);
        btnSelectRegistrationStartDate = findViewById(R.id.btnSelectRegistrationStartDate);
        btnSelectEventDate = findViewById(R.id.btnSelectEventDate);
        btnSelectRaffleDate = findViewById(R.id.btnSelectRaffleDate);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        tvRegistrationStartDateDisplay = findViewById(R.id.tvRegistrationStartDateDisplay);
        tvEventDateDisplay = findViewById(R.id.tvEventDateDisplay);
        tvRaffleDateDisplay = findViewById(R.id.tvRaffleDateDisplay);
        switchGeoLocation = findViewById(R.id.switchGeoLocation);

        // Set up click listeners
        btnSelectPoster.setOnClickListener(v -> selectPoster());
        btnSelectRegistrationStartDate.setOnClickListener(v -> selectRegistrationStartDate());
        btnSelectEventDate.setOnClickListener(v -> selectEventDate());
        btnSelectRaffleDate.setOnClickListener(v -> selectRaffleDate());
        btnCreateEvent.setOnClickListener(v -> createEvent());
    }

    private void selectPoster() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        posterPickerLauncher.launch(intent);
    }

    private void selectRegistrationStartDate() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedRegistrationStartDate = Calendar.getInstance();
                    selectedRegistrationStartDate.set(year, month, dayOfMonth);
                    tvRegistrationStartDateDisplay.setText(
                            String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                    );
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void selectEventDate() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
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
                this,
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
        if (selectedRegistrationStartDate == null) {
            Toast.makeText(this, "Please select a registration start date", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (selectedRaffleDate == null) {
            Toast.makeText(this, "Please select a final registration date", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (selectedEventDate == null) {
            Toast.makeText(this, "Please select an event date", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (!ok) {
            // Re-enable button if validation fails
            btnCreateEvent.setEnabled(true);
            return;
        }

        int capacity = Integer.parseInt(capacityStr);
        Integer maxWaitlist = maxWaitlistStr.isEmpty() ? null : Integer.parseInt(maxWaitlistStr);

        // Validate date order: registrationStart <= raffleDate <= eventDate
        if (selectedRegistrationStartDate != null && selectedRaffleDate != null && 
            selectedRegistrationStartDate.after(selectedRaffleDate)) {
            Toast.makeText(this, "Registration start date must be before or equal to registration end date", Toast.LENGTH_LONG).show();
            btnCreateEvent.setEnabled(true);
            return;
        }
        if (selectedRaffleDate != null && selectedEventDate != null && 
            selectedRaffleDate.after(selectedEventDate)) {
            Toast.makeText(this, "Registration end date must be before or equal to event date", Toast.LENGTH_LONG).show();
            btnCreateEvent.setEnabled(true);
            return;
        }

        // Validate capacity vs waitlist
        if (maxWaitlist != null && capacity > maxWaitlist) {
            Toast.makeText(this, "Event capacity cannot exceed max waitlist size", Toast.LENGTH_LONG).show();
            etCapacity.setError("Capacity too large");
            btnCreateEvent.setEnabled(true);
            return;
        }
        if (!ok) {
            // Re-enable button if validation fails
            btnCreateEvent.setEnabled(true);
            return;
        }

        boolean geoLocationRequired = switchGeoLocation.isChecked();

        // Get organizer ID (current user's profile ID)
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String profileIdStr = prefs.getString(KEY_PROFILE_ID, null);
        if (profileIdStr == null) {
            Toast.makeText(this, "Error: No user profile found", Toast.LENGTH_SHORT).show();
            return;
        }
        int organizerId = Integer.parseInt(profileIdStr);

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
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
                Toast.makeText(this, "Failed to process poster image. Creating event without poster.", Toast.LENGTH_SHORT).show();
            }
        }

        // Create event object
        Event newEvent = new Event(
                name,
                description,
                capacity,
                maxWaitlist,
                base64Poster,
                selectedRegistrationStartDate,
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
            data.put("registrationStartDate", event.getRegistrationStartDate().getTime());
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
            Toast.makeText(this, "Event created successfully!", Toast.LENGTH_LONG).show();
            
            // Return to Create fragment
            finish();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Log.e(TAG, "Failed to create event", e);
            Toast.makeText(this, "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private String convertImageToBase64(Uri imageUri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
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
}

