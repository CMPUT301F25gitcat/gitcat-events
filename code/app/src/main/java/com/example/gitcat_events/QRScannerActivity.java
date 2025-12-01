package com.example.gitcat_events;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.net.URI;
import java.net.URISyntaxException;



/**
 * Activity for scannign QR codes to navigate to events
 */

public class QRScannerActivity extends AppCompatActivity {
    private static final String TAG = "QRScannerActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);

        integrator.setPrompt("scan a QR code to view the event details");

        integrator.setCameraId(0); // to use the back camera basically 

        integrator.setBeepEnabled(true);

        integrator.setBarcodeImageEnabled(false);

        integrator.setOrientationLocked(true); // to lock the orientation correctly 

        integrator.setCaptureActivity(PortraitCaptureActivity.class);

        integrator.initiateScan();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String scannedContent = result.getContents();
                handleScannedQRCode(scannedContent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            finish();
        }
    }


    private void handleScannedQRCode(String scannedContent) {
        try {
            URI uri = new URI(scannedContent);

            if ("gitcatevents".equals(uri.getScheme()) && "event".equals(uri.getHost())) {
                String path = uri.getPath();
                if (path != null && path.startsWith("/")) {
                    String eventId = path.substring(1);
                    if (!eventId.isEmpty()) {
                        navigateToEvent(eventId);
                        return;
                    }
                }
            }

            Toast.makeText(this, "Invalid QR code format. Expected event QR code.", Toast.LENGTH_LONG).show();
            finish();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Error parsing QR code URL: " + scannedContent, e);
            Toast.makeText(this, "Invalid QR code format: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    /**
     * navigates to the event details activity once we have scanned or gotten the event url from QR code . 
     * @param eventId
     */
    private void navigateToEvent(String eventId) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("eventId", eventId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}