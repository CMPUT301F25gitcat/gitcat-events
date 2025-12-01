package com.example.gitcat_events;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Map view for organizers: shows clusters of where enrolled (accepted) entrants joined from.
 * Uses only entries in events/{eventId}/acceptedList that have latitude/longitude.
 * Implemented with a WebView + Leaflet marker clustering (no Google Maps SDK).
 */
public class AcceptedEntrantsMapActivity extends AppCompatActivity {

    private static final String TAG = "AcceptedEntrantsMap";

    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    private WebView webView;
    private MaterialCardView statusCard;
    private TextView tvStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_entrants_map);

        db = FirebaseFirestore.getInstance();

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        if (eventId == null) {
            Toast.makeText(this, "No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (eventName != null) {
            toolbar.setTitle(eventName + " - Map");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        webView = findViewById(R.id.webViewMap);
        statusCard = findViewById(R.id.statusCard);
        tvStatus = findViewById(R.id.tvStatus);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Once the HTML is loaded, send the markers
                loadAcceptedEntrantLocations();
            }
        });

        // Load local HTML that uses Leaflet + markercluster
        webView.loadUrl("file:///android_asset/accepted_map.html");
    }

    private void loadAcceptedEntrantLocations() {
        db.collection("events")
                .document(eventId)
                .collection("acceptedList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    JSONArray markers = new JSONArray();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");

                        if (lat == null || lng == null) {
                            // Only show markers when geo is actually recorded
                            continue;
                        }

                        String deviceId = doc.getString("userDeviceId");
                        String title = deviceId != null ? deviceId : "Entrant";
                        String snippet = buildSnippetForEntry(doc);

                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("lat", lat);
                            obj.put("lng", lng);
                            obj.put("title", title);
                            obj.put("snippet", snippet);
                            markers.put(obj);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error building marker JSON", e);
                        }
                    }

                    if (markers.length() == 0) {
                        showStatusMessage("No locations recorded for enrolled entrants yet.");
                        // Inform JS as well
                        evaluateJs("window.showMarkers([]);");
                        return;
                    }

                    hideStatusMessage();
                    String json = markers.toString();
                    evaluateJs("window.showMarkers(" + json + ");");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load accepted entrants locations", e);
                    showStatusMessage("Failed to load locations: " + e.getMessage());
                    evaluateJs("window.showMarkers([]);");
                });
    }

    private String buildSnippetForEntry(DocumentSnapshot doc) {
        Long ts = doc.getLong("acceptedAt");
        if (ts == null) {
            ts = doc.getLong("timestamp");
        }
        if (ts == null) return "Enrolled entrant";

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault());
        String dateStr = sdf.format(new java.util.Date(ts));
        return "Accepted: " + dateStr;
    }

    private void evaluateJs(String script) {
        try {
            webView.post(() -> webView.evaluateJavascript(script, null));
        } catch (Exception e) {
            Log.e(TAG, "Error evaluating JS", e);
        }
    }

    private void showStatusMessage(String message) {
        if (statusCard != null && tvStatus != null) {
            tvStatus.setText(message);
            statusCard.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void hideStatusMessage() {
        if (statusCard != null) statusCard.setVisibility(View.GONE);
    }
}

