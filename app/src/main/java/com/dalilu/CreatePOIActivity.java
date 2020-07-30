package com.dalilu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dalilu.services.FirebaseManager;
import com.dalilu.services.PointOfInterest;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;

public class CreatePOIActivity extends AppCompatActivity {

    //Request code
    private static final int REQUEST_IMAGE_CAPTURE = 256;
    //Imageview of the picture
    private ImageView cameraPhotoView;
    //Image encoded in a string
    private String encodedImage;
    //The edit text
    private EditText nameEditText;
    //The picture that was captured by the user in a bitmap
    private Bitmap picture;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the data on the activity if they have been set by the user
        if (encodedImage != null) {
            outState.putString("encodedImage", encodedImage);
        }
        if (nameEditText != null && nameEditText.getText().toString() != null && !nameEditText.getText().toString().isEmpty()) {
            outState.putString("editText", nameEditText.getText().toString());
        }
        if (picture != null) {
            outState.putParcelable("picture", picture);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poi);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        LatLng location = intent.getParcelableExtra("location");
        final double lat = location.latitude;
        final double lng = location.longitude;

        nameEditText = findViewById(R.id.name_edit_text);
        Button submitPoiButton = findViewById(R.id.submit_poi_button);
        Button takePhotoButton = findViewById(R.id.take_photo_button);
        cameraPhotoView = findViewById(R.id.photo_imageview);

        //Retrieve the saved data if there are set
        if (savedInstanceState != null) {
            String savedEncodedImage = savedInstanceState.getString("encodedImage");
            String savedEditTextString = savedInstanceState.getString("editText");
            Bitmap savedPicture = savedInstanceState.getParcelable("picture");

            if (savedEncodedImage != null) {
                encodedImage = savedEncodedImage;
            }
            if (savedEditTextString != null) {
                nameEditText.setText(savedEditTextString);
            }
            if (savedPicture != null) {
                cameraPhotoView.setImageBitmap(savedPicture);
                picture = savedPicture;
            }
        }

        submitPoiButton.setOnClickListener(view -> {
            //Toast to display wrong input
            if (nameEditText.getText().toString() == null || nameEditText.getText().toString().isEmpty() || encodedImage == null || encodedImage.isEmpty()) {
                Toast errorToast = Toast.makeText(view.getContext(), "Name or image cannot be empty", Toast.LENGTH_SHORT);
                return;
            }
            //Create a new point of interest
            PointOfInterest pointOfInterest = new PointOfInterest(nameEditText.getText().toString(), lat, lng);
            //Create the poi and write the image in a separate firebase reference
            FirebaseManager.getInstance().createPOI(pointOfInterest);
            FirebaseManager.getInstance().writePoiImage(pointOfInterest, encodedImage);
            finish();
        });

        takePhotoButton.setOnClickListener(view -> launchTakePictureIntent());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Get the captured image
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap capturedImage = (Bitmap) extras.get("data");
            picture = capturedImage;
            cameraPhotoView.setImageBitmap(capturedImage);
            encodedImage = encodeBitmap(capturedImage);
        }
    }

    private void launchTakePictureIntent() {
        //Start the intent to take a picture
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Only if the intent has not been launched already
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //Encode the bitmap to Base64
    private String encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}
