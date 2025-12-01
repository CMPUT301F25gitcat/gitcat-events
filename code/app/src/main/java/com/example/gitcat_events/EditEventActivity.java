package com.example.gitcat_events;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gitcat_events.core.model.Event;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for editing existing events
 */
public class EditEventActivity extends AppCompatActivity {

    private static final String TAG = "EditEventActivity";

    private FirebaseFirestore db;
    private ImageView ivEventPoster;
    private EditText etEventName, etEventDescription, etCapacity, etMaxWaitlist, etSelectionCriteria;
    private Button btnSelectPoster, btnSelectRegistrationStartDate, btnSelectEventDate, btnSelectRaffleDate, btnSelectEventType, btnUpdateEvent;
    private TextView tvRegistrationStartDateDisplay, tvEventDateDisplay, tvRaffleDateDisplay, tvEventTypeDisplay;
    private SwitchMaterial switchGeoLocation;
    private ImageButton btnBack;

    private Uri selectedPosterUri;
    private String existingPosterBase64; // Keep existing poster if not changed
    private Calendar selectedRegistrationStartDate;
    private Calendar selectedEventDate;
    private Calendar selectedRaffleDate;
    private List<String> selectedEventTypes;
    
    private String eventId;
    private Event currentEvent;

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
        setContentView(R.layout.activity_edit_event);

        db = FirebaseFirestore.getInstance();

        // Get event ID from intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        ivEventPoster = findViewById(R.id.ivEventPoster);
        etEventName = findViewById(R.id.etEventName);
        etEventDescription = findViewById(R.id.etEventDescription);
        etCapacity = findViewById(R.id.etCapacity);
        etMaxWaitlist = findViewById(R.id.etMaxWaitlist);
        etSelectionCriteria = findViewById(R.id.etSelectionCriteria);
        btnSelectPoster = findViewById(R.id.btnSelectPoster);
        btnSelectRegistrationStartDate = findViewById(R.id.btnSelectRegistrationStartDate);
        btnSelectEventDate = findViewById(R.id.btnSelectEventDate);
        btnSelectRaffleDate = findViewById(R.id.btnSelectRaffleDate);
        btnSelectEventType = findViewById(R.id.btnSelectEventType);
        btnUpdateEvent = findViewById(R.id.btnUpdateEvent);
        tvRegistrationStartDateDisplay = findViewById(R.id.tvRegistrationStartDateDisplay);
        tvEventDateDisplay = findViewById(R.id.tvEventDateDisplay);
        tvRaffleDateDisplay = findViewById(R.id.tvRaffleDateDisplay);
        tvEventTypeDisplay = findViewById(R.id.tvEventTypeDisplay);
        switchGeoLocation = findViewById(R.id.switchGeoLocation);

        // Set up click listeners
        btnBack.setOnClickListener(v -> onBackPressed());
        btnSelectPoster.setOnClickListener(v -> selectPoster());
        btnSelectRegistrationStartDate.setOnClickListener(v -> selectRegistrationStartDate());
        btnSelectEventDate.setOnClickListener(v -> selectEventDate());
        btnSelectRaffleDate.setOnClickListener(v -> selectRaffleDate());
        btnSelectEventType.setOnClickListener(v -> selectEventType());
        btnUpdateEvent.setOnClickListener(v -> confirmUpdateEvent());

        // Load existing event data
        loadEventData();
    }

    private void loadEventData() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading event...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressDialog.dismiss();
                    
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Event document exists, parsing manually (not using toObject)");
                        try {
                            // CRITICAL: Do NOT use toObject() - it can't convert Timestamp to Calendar
                            // Use manual parsing instead
                            currentEvent = parseEvent(documentSnapshot);
                            if (currentEvent != null) {
                                Log.d(TAG, "Event parsed successfully: " + currentEvent.getName());
                                currentEvent.setDocumentId(eventId);
                                populateForm();
                            } else {
                                Log.e(TAG, "parseEvent returned null");
                                Toast.makeText(this, "Failed to parse event data", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing event", e);
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to parse event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Error loading event", e);
                    Toast.makeText(this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
    
    private Event parseEvent(com.google.firebase.firestore.DocumentSnapshot document) {
        Log.d(TAG, "parseEvent called for document: " + (document != null ? document.getId() : "null"));
        if (document == null || !document.exists()) {
            Log.w(TAG, "Document is null or doesn't exist");
            return null;
        }
        
        try {
            Event event = new Event();
            event.setDocumentId(document.getId());
            String eventName = document.getString("name");
            event.setName(eventName);
            Log.d(TAG, "Parsing event: " + eventName);
            event.setDescription(document.getString("description"));

            Long capacity = document.getLong("capacity");
            event.setCapacity(capacity != null ? capacity.intValue() : 0);

            Long maxWaitlist = document.getLong("maxWaitListSize");
            event.setMaxWaitListSize(maxWaitlist != null ? maxWaitlist.intValue() : null);

            event.setPoster(document.getString("poster"));
            event.setOrganizerDeviceId(document.getString("organizerDeviceId"));
            event.setSelectionCriteria(document.getString("selectionCriteria"));

            Boolean geoLocation = document.getBoolean("geoLocationRequired");
            event.setGeoLocationRequired(geoLocation != null ? geoLocation : false);

            // Convert Date/Timestamp to Calendar
            // Handle registrationStartDate
            try {
                java.util.Date registrationStartDate = document.getDate("registrationStartDate");
                if (registrationStartDate != null) {
                    Calendar regStartCal = Calendar.getInstance();
                    regStartCal.setTime(registrationStartDate);
                    event.setRegistrationStartDate(regStartCal);
                } else {
                    event.setRegistrationStartDate(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing registrationStartDate", e);
                event.setRegistrationStartDate(null);
            }

            // Handle eventDate
            try {
                java.util.Date eventDate = document.getDate("eventDate");
                if (eventDate != null) {
                    Calendar eventCal = Calendar.getInstance();
                    eventCal.setTime(eventDate);
                    event.setEventDate(eventCal);
                } else {
                    event.setEventDate(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing eventDate", e);
                event.setEventDate(null);
            }

            // Handle raffleDate
            try {
                java.util.Date raffleDate = document.getDate("raffleDate");
                if (raffleDate != null) {
                    Calendar raffleCal = Calendar.getInstance();
                    raffleCal.setTime(raffleDate);
                    event.setRaffleDate(raffleCal);
                } else {
                    event.setRaffleDate(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing raffleDate", e);
                event.setRaffleDate(null);
            }

            // Read eventTypes from Firestore (List<String>)
            try {
                @SuppressWarnings("unchecked")
                List<Object> eventTypesObj = (List<Object>) document.get("eventTypes");
                if (eventTypesObj != null) {
                    List<String> eventTypes = new ArrayList<>();
                    for (Object obj : eventTypesObj) {
                        if (obj instanceof String) {
                            eventTypes.add((String) obj);
                        }
                    }
                    event.setEventTypes(eventTypes.isEmpty() ? null : eventTypes);
                } else {
                    event.setEventTypes(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing eventTypes", e);
                event.setEventTypes(null);
            }

            return event;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing event document: " + document.getId(), e);
            return null;
        }
    }

    private void populateForm() {
        if (currentEvent == null) return;

        // Populate text fields
        etEventName.setText(currentEvent.getName());
        etEventDescription.setText(currentEvent.getDescription());
        etCapacity.setText(String.valueOf(currentEvent.getCapacity()));
        
        if (currentEvent.getMaxWaitListSize() != null) {
            etMaxWaitlist.setText(String.valueOf(currentEvent.getMaxWaitListSize()));
        }
        
        if (currentEvent.getSelectionCriteria() != null && etSelectionCriteria != null) {
            etSelectionCriteria.setText(currentEvent.getSelectionCriteria());
        }

        switchGeoLocation.setChecked(currentEvent.getGeoLocationRequired() != null && currentEvent.getGeoLocationRequired());

        // Populate dates
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        if (currentEvent.getRegistrationStartDate() != null) {
            selectedRegistrationStartDate = currentEvent.getRegistrationStartDate();
            tvRegistrationStartDateDisplay.setText(sdf.format(selectedRegistrationStartDate.getTime()));
        }
        
        if (currentEvent.getEventDate() != null) {
            selectedEventDate = currentEvent.getEventDate();
            tvEventDateDisplay.setText(sdf.format(selectedEventDate.getTime()));
        }
        
        if (currentEvent.getRaffleDate() != null) {
            selectedRaffleDate = currentEvent.getRaffleDate();
            tvRaffleDateDisplay.setText(sdf.format(selectedRaffleDate.getTime()));
        }

        // Populate event types
        if (currentEvent.getEventTypes() != null && !currentEvent.getEventTypes().isEmpty()) {
            selectedEventTypes = new ArrayList<>(currentEvent.getEventTypes());
            updateEventTypeDisplay();
        }

        // Load existing poster
        if (currentEvent.getPoster() != null && !currentEvent.getPoster().isEmpty()) {
            existingPosterBase64 = currentEvent.getPoster();
            loadBase64Image(existingPosterBase64, ivEventPoster);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("Are you sure you want to discard your changes?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Discard", (dialog, which) -> {
                    super.onBackPressed();
                    finish();
                })
                .show();
    }

    private void selectPoster() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        posterPickerLauncher.launch(intent);
    }

    private void selectRegistrationStartDate() {
        Calendar calendar = selectedRegistrationStartDate != null ? selectedRegistrationStartDate : Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedRegistrationStartDate = Calendar.getInstance();
                    selectedRegistrationStartDate.set(year, month, dayOfMonth);
                    tvRegistrationStartDateDisplay.setText(
                            String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
                    );
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void selectEventDate() {
        Calendar calendar = selectedEventDate != null ? selectedEventDate : Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedEventDate = Calendar.getInstance();
                    selectedEventDate.set(year, month, dayOfMonth);
                    tvEventDateDisplay.setText(
                            String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
                    );
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void selectRaffleDate() {
        Calendar calendar = selectedRaffleDate != null ? selectedRaffleDate : Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedRaffleDate = Calendar.getInstance();
                    selectedRaffleDate.set(year, month, dayOfMonth);
                    tvRaffleDateDisplay.setText(
                            String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
                    );
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void selectEventType() {
        EventTypeSelectionBottomSheet typeSheet = EventTypeSelectionBottomSheet.newInstance(selectedTypes -> {
            this.selectedEventTypes = selectedTypes;
            updateEventTypeDisplay();
        });

        // Set initial types if already selected
        if (selectedEventTypes != null && !selectedEventTypes.isEmpty()) {
            typeSheet.setInitialTypes(selectedEventTypes);
        }

        typeSheet.show(getSupportFragmentManager(), "EventTypeSelectionBottomSheet");
    }

    private void updateEventTypeDisplay() {
        if (selectedEventTypes == null || selectedEventTypes.isEmpty()) {
            tvEventTypeDisplay.setText("Not selected");
        } else {
            String typesText = String.join(", ", selectedEventTypes);
            tvEventTypeDisplay.setText(typesText);
        }
    }

    private void confirmUpdateEvent() {
        new AlertDialog.Builder(this)
                .setTitle("Update Event?")
                .setMessage("Are you sure you want to save these changes?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Update", (dialog, which) -> updateEvent())
                .show();
    }

    private void updateEvent() {
        // Disable button to prevent duplicate submissions
        btnUpdateEvent.setEnabled(false);

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
        if (selectedEventTypes == null || selectedEventTypes.isEmpty()) {
            Toast.makeText(this, "Please select at least one event type", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (!ok) {
            btnUpdateEvent.setEnabled(true);
            return;
        }

        int capacity = Integer.parseInt(capacityStr);
        Integer maxWaitlist = maxWaitlistStr.isEmpty() ? null : Integer.parseInt(maxWaitlistStr);

        // Validate date order: registrationStart <= raffleDate <= eventDate
        // Normalize dates to start of day for comparison (ignore time)
        Calendar regStartNormalized = null;
        Calendar raffleNormalized = null;
        Calendar eventNormalized = null;
        
        if (selectedRegistrationStartDate != null) {
            regStartNormalized = (Calendar) selectedRegistrationStartDate.clone();
            regStartNormalized.set(Calendar.HOUR_OF_DAY, 0);
            regStartNormalized.set(Calendar.MINUTE, 0);
            regStartNormalized.set(Calendar.SECOND, 0);
            regStartNormalized.set(Calendar.MILLISECOND, 0);
        }
        
        if (selectedRaffleDate != null) {
            raffleNormalized = (Calendar) selectedRaffleDate.clone();
            raffleNormalized.set(Calendar.HOUR_OF_DAY, 0);
            raffleNormalized.set(Calendar.MINUTE, 0);
            raffleNormalized.set(Calendar.SECOND, 0);
            raffleNormalized.set(Calendar.MILLISECOND, 0);
        }
        
        if (selectedEventDate != null) {
            eventNormalized = (Calendar) selectedEventDate.clone();
            eventNormalized.set(Calendar.HOUR_OF_DAY, 0);
            eventNormalized.set(Calendar.MINUTE, 0);
            eventNormalized.set(Calendar.SECOND, 0);
            eventNormalized.set(Calendar.MILLISECOND, 0);
        }
        
        // Check: registrationStart <= raffleDate (allows equality)
        if (regStartNormalized != null && raffleNormalized != null && 
            regStartNormalized.after(raffleNormalized)) {
            Toast.makeText(this, "Registration start date must be on or before final registration date", Toast.LENGTH_LONG).show();
            btnUpdateEvent.setEnabled(true);
            return;
        }
        
        // Check: raffleDate <= eventDate (allows equality)
        if (raffleNormalized != null && eventNormalized != null && 
            raffleNormalized.after(eventNormalized)) {
            Toast.makeText(this, "Final registration date must be on or before event date", Toast.LENGTH_LONG).show();
            btnUpdateEvent.setEnabled(true);
            return;
        }

        // Validate capacity vs waitlist
        if (maxWaitlist != null && capacity > maxWaitlist) {
            Toast.makeText(this, "Event capacity cannot exceed max waitlist size", Toast.LENGTH_LONG).show();
            etCapacity.setError("Capacity too large");
            btnUpdateEvent.setEnabled(true);
            return;
        }

        boolean geoLocationRequired = switchGeoLocation.isChecked();
        String selectionCriteria = etSelectionCriteria.getText().toString().trim();

        if (selectionCriteria.isEmpty()) {
            selectionCriteria = "Random selection from all registered participants. All entrants have an equal chance of being selected.";
        }

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating event...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Handle poster update
        String posterBase64 = existingPosterBase64; // Keep existing by default
        if (selectedPosterUri != null) {
            // New poster selected, convert to Base64
            try {
                posterBase64 = convertImageToBase64(selectedPosterUri);
            } catch (Exception e) {
                Log.e(TAG, "Error converting poster to Base64", e);
                Toast.makeText(this, "Failed to process poster image. Keeping existing poster.", Toast.LENGTH_SHORT).show();
            }
        }

        // Prepare update data
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("description", description);
        updates.put("capacity", capacity);
        updates.put("maxWaitListSize", maxWaitlist);
        updates.put("registrationStartDate", selectedRegistrationStartDate.getTime());
        updates.put("eventDate", selectedEventDate.getTime());
        updates.put("raffleDate", selectedRaffleDate.getTime());
        updates.put("geoLocationRequired", geoLocationRequired);
        updates.put("poster", posterBase64);
        updates.put("selectionCriteria", selectionCriteria);
        updates.put("eventTypes", selectedEventTypes != null ? selectedEventTypes : new ArrayList<>());

        // Update in Firestore
        db.collection("events").document(eventId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Failed to update event", e);
                    Toast.makeText(this, "Failed to update event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnUpdateEvent.setEnabled(true);
                });
    }

    private String convertImageToBase64(Uri imageUri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        if (inputStream == null) throw new Exception("Failed to open input stream");

        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        if (originalBitmap == null) throw new Exception("Failed to decode image");

        // Resize image to max 800x800 while maintaining aspect ratio
        int maxDimension = 800;
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
        originalBitmap.recycle();

        // Compress to JPEG with quality 85
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
        resizedBitmap.recycle();

        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void loadBase64Image(String base64String, ImageView imageView) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode Base64 image", e);
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
}

