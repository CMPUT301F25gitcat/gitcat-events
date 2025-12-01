package com.example.gitcat_events;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/*
  Activity to display the QR code for an event
  Allows users to view , share and save QR codes
 */
public class QRCodeDisplayActivity extends AppCompatActivity {
    private static final String TAG = "QRCodeDisplayActivity";

    private ImageView ivQRCode;
    private TextView tvEventName;
    private TextView tvQRCodeUrl;
    private Button btnShareQRCode;
    private Button btnSaveQRCode;
    private Button btnViewEvent;
    private ImageButton btnBack;

    private String eventId;
    private String eventName;
    private String qrCodeUrl;
    private Bitmap qrCodeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_display);

        // now wwe have to get data from the intents 
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        qrCodeUrl = getIntent().getStringExtra("qrCodeUrl");

        if (eventId == null || eventName == null || qrCodeUrl == null) {
          Toast.makeText(this, "Error: No event data provided", Toast.LENGTH_SHORT).show();
          finish();
          return;
        }

        //Intialize the views respectively for the button , imageview and textview respectively. 
        btnBack = findViewById(R.id.btnBack);
        ivQRCode = findViewById(R.id.ivQRCode);
        tvEventName = findViewById(R.id.tvEventName);
        tvQRCodeUrl = findViewById(R.id.tvQRCodeUrl);
        btnShareQRCode = findViewById(R.id.btnShareQRCode);
        btnSaveQRCode = findViewById(R.id.btnSaveQRCode);
        btnViewEvent = findViewById(R.id.btnViewEvent);

        //setting up th eventname 
        if (eventName != null) {
          tvEventName.setText(eventName);
        } else {
          tvEventName.setText("Event QR Code");
        }

        //setting up the QR code url 
        tvQRCodeUrl.setText(qrCodeUrl);

        //then we go ahead and generate teh QR code 
        generateQRCode();

        //setting up thee click listeners fro the buttons 
        btnBack.setOnClickListener(v->finish());
        btnShareQRCode.setOnClickListener(v->shareQRCode());
        btnSaveQRCode.setOnClickListener(v->saveQRCodeToGallery());
        btnViewEvent.setOnClickListener(v->viewEventDetails());
    }


    /**
     * method to gnerate the QR code 
     * bitmap from a url and display it in the image view 
     */

    private void generateQRCode() {
      try {
        QRCodeWriter writer = new QRCodeWriter(); // creating a QR code writer object 
        BitMatrix bitMatrix = writer.encode(qrCodeUrl, BarcodeFormat.QR_CODE, 512, 512);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();

        qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x<width; x++) {
          for (int y = 0; y<height; y++) {
            qrCodeBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
          }
        }

        ivQRCode.setImageBitmap(qrCodeBitmap);
      } catch (WriterException e) {
        Log.e(TAG, "Error generating QR code", e);
        Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
      }
    }

    /**
     * 
     * shares the QR code image to other users 
     */
    private void shareQRCode() {
      if (qrCodeBitmap == null) {
        Toast.makeText(this, "QR code not available to share", Toast.LENGTH_SHORT).show();
        return;
      }

      try {
        //save 
        File cachePath = new File(getCacheDir(), "qr_codes");
        cachePath.mkdirs();

        File file = new File(cachePath, "qr_code_" + eventId + ".png");
        FileOutputStream stream = new FileOutputStream(file);
        qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();

        //getting uri via a file provider 
        Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

        // creating the share intent 
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share QR Code for " + (eventName != null ? eventName : "Event"));

        shareIntent.putExtra(Intent.EXTRA_TEXT, "Scan this QR code to access the event: " + qrCodeUrl);

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share QR code via "));
      } catch (IOException e) {
        Log.e(TAG, "Error sharing QR code", e);
        Toast.makeText(this, "Failed to share QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
      }
    }




    /**
     * saves the QR code image to the gallery 
     */
    private void saveQRCodeToGallery() {
      if (qrCodeBitmap == null) {
          Toast.makeText(this, "QR code not available", Toast.LENGTH_SHORT).show();
          return;
      }

      try {
        //creating a file name for the QR code image 
          String fileName = "QRCode_" + (eventName != null ? eventName.replaceAll("[^a-zA-Z0-9]", "_") : "Event") + "_" + eventId + ".png";
          
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //for android 10 and above, we use the MediaStore to save the QR code image 
              ContentValues contentValues = new ContentValues();
              contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
              contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
              contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GitCatEvents");

              Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

              if (uri != null) {
                  try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                      if (outputStream != null) {
                          qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                          outputStream.flush();
                          Toast.makeText(this, "QR code saved to Gallery", Toast.LENGTH_LONG).show();
                      }
                  }
              } else {
                  Toast.makeText(this, "Failed to save QR code", Toast.LENGTH_SHORT).show();
              }
          } else {
              File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
              File gitcatDir = new File(picturesDir, "GitCatEvents");
              if (!gitcatDir.exists()) {
                  boolean created = gitcatDir.mkdirs();
                  if (!created) {
                      Log.w(TAG, "Failed to create GitCatEvents directory");
                      Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
                      return;
                  }
              }

              File imageFile = new File(gitcatDir, fileName);
              FileOutputStream outputStream = new FileOutputStream(imageFile);
              qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
              outputStream.flush();
              outputStream.close();

              //notifying the media store to scan the file 
              Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
              Uri fileUri = Uri.fromFile(imageFile);
              mediaScanIntent.setData(fileUri);
              sendBroadcast(mediaScanIntent);
              //showing a toast message to the user 
              Toast.makeText(this, "QR code saved to Gallery: " + fileName, Toast.LENGTH_LONG).show();
          }
      } catch (IOException e) {
          Log.e(TAG, "Error saving QR code to gallery", e);
          Toast.makeText(this, "Failed to save QR code: " + e.getMessage(), Toast.LENGTH_LONG).show();
      }
  }



    /**
     * opens the event details activity basically to see the event details 
     * 
     */

    private void viewEventDetails() {
      Intent intent = new Intent(this, EventDetailsActivity.class);
      intent.putExtra("eventId", eventId);
      startActivity(intent);
    }
}