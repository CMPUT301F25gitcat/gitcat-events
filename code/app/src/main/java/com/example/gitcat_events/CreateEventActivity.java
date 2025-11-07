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
/**
 * This activity allows users to create new events with details such as name, description, capacity, dates, and poster image. Events are saved to Firestore with auto-incremented IDs.
 */
public class CreateEventActivity extends AppCompatActivity {

    private static final String TAG = "CreateEventActivity";
    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id";

    private FirebaseFirestore db;
    private ImageView ivEventPoster;
    private EditText etEventName, etEventDescription, etCapacity, etMaxWaitlist, etSelectionCriteria;
    private Button btnSelectPoster, btnSelectEventDate, btnSelectRaffleDate, btnCreateEvent;
    private TextView tvEventDateDisplay, tvRaffleDateDisplay;
    private SwitchMaterial switchGeoLocation;
    private android.widget.ImageButton btnBack;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        ivEventPoster = findViewById(R.id.ivEventPoster);
        etEventName = findViewById(R.id.etEventName);
        etEventDescription = findViewById(R.id.etEventDescription);
        etCapacity = findViewById(R.id.etCapacity);
        etMaxWaitlist = findViewById(R.id.etMaxWaitlist);
        etSelectionCriteria = findViewById(R.id.etSelectionCriteria);
        btnSelectPoster = findViewById(R.id.btnSelectPoster);
        btnSelectEventDate = findViewById(R.id.btnSelectEventDate);
        btnSelectRaffleDate = findViewById(R.id.btnSelectRaffleDate);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        tvEventDateDisplay = findViewById(R.id.tvEventDateDisplay);
        tvRaffleDateDisplay = findViewById(R.id.tvRaffleDateDisplay);
        switchGeoLocation = findViewById(R.id.switchGeoLocation);

        // Set up click listeners
        btnBack.setOnClickListener(v -> onBackPressed());
        btnSelectPoster.setOnClickListener(v -> selectPoster());
        btnSelectEventDate.setOnClickListener(v -> selectEventDate());
        btnSelectRaffleDate.setOnClickListener(v -> selectRaffleDate());
        btnCreateEvent.setOnClickListener(v -> createEvent());
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Opens an image picker to select a poster for the event
     */
    private void selectPoster() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        posterPickerLauncher.launch(intent);
    }
    /**
     * Shows a date picker dialog to select the event date
     */
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
    /**
     * Shows a date picker dialog to select the raffle date (final registration date)
     */
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
    /**
     * Validates input fields and creates a new event in Firestore
     * Disables the create button during processing to prevent duplicate submissions
     */
    private void createEvent() {
        // Disable button to prevent duplicate submissions
        btnCreateEvent.setEnabled(false);
        
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
            Toast.makeText(this, "Please select an event date", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (selectedRaffleDate == null) {
            Toast.makeText(this, "Please select a final registration date", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (!ok) {
            // Re-enable button if validation fails
            btnCreateEvent.setEnabled(true);
            return;
        }

        int capacity = Integer.parseInt(capacityStr);
        Integer maxWaitlist = maxWaitlistStr.isEmpty() ? null : Integer.parseInt(maxWaitlistStr);
        boolean geoLocationRequired = switchGeoLocation.isChecked();
        String selectionCriteria = etSelectionCriteria.getText().toString().trim();
        
        // If no criteria provided, use default
        if (selectionCriteria.isEmpty()) {
            selectionCriteria = "Random selection from all registered participants. All entrants have an equal chance of being selected.";
        }

        // Get organizer device ID (permanent identifier)
        String organizerDeviceId = getOrCreateDeviceId();

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
                selectedRaffleDate,
                selectedEventDate,
                geoLocationRequired
        );
        newEvent.setOrganizerDeviceId(organizerDeviceId);
        newEvent.setSelectionCriteria(selectionCriteria);

        // Save to Firestore with auto-incrementing ID
        saveEventToFirestore(newEvent, progressDialog);
    }
    /**
     * Saves the event to Firestore using a transaction to ensure atomic counter increment
     * @param event
     * the event to save
     * @param progressDialog
     * the progress dialog to show during saving
     */
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
            data.put("organizerDeviceId", event.getOrganizerDeviceId());
            data.put("selectionCriteria", event.getSelectionCriteria());
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
            
            // Return to Create fragment (button stays disabled since we're leaving)
            finish();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Log.e(TAG, "Failed to create event", e);
            Toast.makeText(this, "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show();
            
            // Re-enable button on failure so user can try again
            btnCreateEvent.setEnabled(true);
        });
    }
    /**
     * Converts the selected image URI to a Base64 encoded string for storage
     * @param imageUri
     * the URI of the image to convert
     * @return
     * returns the Base64 encoded string of the image
     * @throws Exception
     * if the image cannot be processed
     */
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
    /**
     * Resizes a bitmap to fit within the specified maximum size while maintaining aspect ratio
     * @param bitmap
     * the original bitmap to resize
     * @param maxSize
     * the maximum width or height for the resized image
     * @return
     * returns the resized bitmap
     */
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
    /**
     * Gets the device ID from shared preferences or creates a new one if it doesn't exist
     * @return
     * returns the unique device identifier
     */
    private String getOrCreateDeviceId() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String deviceId = sp.getString("device_id", null);

        if (deviceId == null) {
            try {
                deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
                Log.w(TAG, "Failed to get Android ID", e);
            }

            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = java.util.UUID.randomUUID().toString();
            }
            sp.edit().putString("device_id", deviceId).apply();
        }
        return deviceId;
    }
}

