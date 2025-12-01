package com.example.gitcat_events.features.admin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gitcat_events.R;
import com.example.gitcat_events.core.model.Image;

import java.util.ArrayList;

public class ImageArrayAdapter extends ArrayAdapter<Image> {
    public ImageArrayAdapter(Context context, ArrayList<Image> data) {
        super(context, 0, data);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView != null ? convertView
                : LayoutInflater.from(getContext()).inflate(R.layout.image_list_item, parent, false);

        Image tempImg = getItem(position);
        ImageView imageView = view.findViewById(R.id.adminListImage);

        TextView origin = view.findViewById(R.id.adminImageOrigin);
        TextView name = view.findViewById(R.id.adminImageName);
        String originType = tempImg.getOrigin();
        if(originType.equals("Event")){
            origin.setText("Event Poster");
        } else if(originType.equals("Profile")){
            origin.setText("Profile Photo");
        }
        name.setText(tempImg.getOwnerName());

        // Load image asynchronously
        loadBase64Image(tempImg.getUrl(), imageView);

        return view;
    }

    /**
     * Load Base64 image into ImageView
     */
    private void loadBase64Image(String base64String, ImageView imageView) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
//            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
}
