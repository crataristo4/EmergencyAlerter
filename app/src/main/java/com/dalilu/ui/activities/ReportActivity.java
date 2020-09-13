package com.dalilu.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.dalilu.R;
import com.dalilu.databinding.ActivityReportBinding;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.CameraUtils;
import com.dalilu.utils.DisplayViewUI;
import com.dalilu.utils.GetTimeAgo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ReportActivity extends AppCompatActivity {

    private static final String TAG = "ReportActivity";
    private static String imageStoragePath;
    ActivityReportBinding activityReportBinding;
    private StorageReference filePath;
    private Uri uri = null;
    private ProgressDialog pd;
    private CollectionReference alertCollectionReference;
    private String knownName, state, country, phoneNumber, userId, userName, userPhotoUrl;
    private double latitude, longitude;
    private ImageView imgPhoto;
    private VideoView videoView;
    private Button btnUpload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityReportBinding = DataBindingUtil.setContentView(this, R.layout.activity_report);
        setSupportActionBar(activityReportBinding.toolBarReport);


        StorageReference imageStorageRef = FirebaseStorage.getInstance().getReference().child("alerts");
        filePath = imageStorageRef.child(UUID.randomUUID().toString());
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("alerts");
        alertCollectionReference = FirebaseFirestore.getInstance().collection("Alerts");
        imgPhoto = activityReportBinding.imgAlertPhoto;
        videoView = activityReportBinding.videoView;
        btnUpload = activityReportBinding.btnUpload;

        Intent getUserDetailsIntent = getIntent();
        if (getUserDetailsIntent != null) {
            userName = getUserDetailsIntent.getStringExtra(AppConstants.USER_NAME);
            userPhotoUrl = getUserDetailsIntent.getStringExtra(AppConstants.USER_PHOTO_URL);
            userId = getUserDetailsIntent.getStringExtra(AppConstants.UID);
            phoneNumber = getUserDetailsIntent.getStringExtra(AppConstants.PHONE_NUMBER);
            state = BaseActivity.state;
            country = BaseActivity.country;
            knownName = BaseActivity.state;
            latitude = BaseActivity.latitude;
            longitude = BaseActivity.longitude;

        }

        activityReportBinding.fabCamera.setOnClickListener(v -> {
            if (CameraUtils.checkPermissions(v.getContext())) {
                if (String.valueOf(latitude) == null && String.valueOf(longitude) == null) {

                    DisplayViewUI.displayToast(v.getContext(), "Sorry you cant report now.Try again");

                } else {
                    captureImage();

                }
            } else {
                requestCameraPermission(AppConstants.MEDIA_TYPE_IMAGE);
            }
        });


        activityReportBinding.fabVideo.setOnClickListener(v -> {
            if (CameraUtils.checkPermissions(v.getContext())) {
                captureVideo();
            } else {
                requestCameraPermission(AppConstants.MEDIA_TYPE_VIDEO);
            }
        });


    }


    /**
     * Capturing Camera Image will launch camera app requested image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(AppConstants.MEDIA_TYPE_IMAGE);
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(ReportActivity.this, file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, AppConstants.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Launching camera app to record video
     */
    private void captureVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(AppConstants.MEDIA_TYPE_VIDEO);
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(ReportActivity.this, file);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
        intent.putExtra(String.valueOf(MediaRecorder.VideoEncoder.MPEG_4_SP), 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file

        // start the video capture Intent
        startActivityForResult(intent, AppConstants.CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Requesting permissions using Dexter library
     */
    private void requestCameraPermission(final int type) {

        Dexter.withContext(ReportActivity.this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            if (type == AppConstants.MEDIA_TYPE_IMAGE) {
                                // capture picture
                                captureImage();
                            } else {
                                captureVideo();
                            }

                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            showPermissionsAlert();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * Alert dialog to navigate to app settings
     * to enable necessary permissions
     */
    private void showPermissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
        builder.setTitle(R.string.perRequired)
                .setMessage(R.string.camPerm)
                .setPositiveButton(R.string.goToSettings, (dialog, which) -> CameraUtils.openSettings(ReportActivity.this))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                }).show();
    }

    private void uploadToServer(Uri imageUri, String type) throws IOException {
        pd.show();
        StringBuilder address = new StringBuilder();
        address.append(knownName).append(",").append(state).append(",").append(country);
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:MM a");
        String dateReported = dateFormat.format(Calendar.getInstance().getTime());

        //compress image
        Bitmap bitmap;
        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);

        byte[] fileInBytes = byteArrayOutputStream.toByteArray();
        //upload photo to server
        filePath.putBytes(fileInBytes).continueWithTask(task -> {

            if (!task.isSuccessful()) {
                pd.dismiss();

            }
            return filePath.getDownloadUrl();

        }).addOnSuccessListener(this, o -> {
            pd.dismiss();

            Uri downLoadUri = Uri.parse(o.toString());
            assert downLoadUri != null;
            String url = downLoadUri.toString();

            Map<String, Object> alertItems = new HashMap<>();
            alertItems.put("userName", userName);
            alertItems.put("userPhotoUrl", userPhotoUrl);
            alertItems.put("url", url);
            alertItems.put(type, type);
            alertItems.put("coordinates", new GeoPoint(latitude, longitude));
            alertItems.put("address", address.toString());
            alertItems.put("userId", userId);
            alertItems.put("phoneNumber", phoneNumber);
            alertItems.put("timeStamp", GetTimeAgo.getTimeInMillis());
            alertItems.put("dateReported", dateReported);

            alertCollectionReference.add(alertItems);
            String id = MainActivity.userId;

            DisplayViewUI.displayToast(ReportActivity.this, getString(R.string.reportSuccess));

            startActivity(new Intent(ReportActivity.this, MainActivity.class)
                    .putExtra(AppConstants.UID, id)
                    .putExtra(AppConstants.USER_NAME, userName)
                    .putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl)
                    .putExtra(AppConstants.PHONE_NUMBER, phoneNumber)


            );
            finish();


        }).addOnFailureListener(this, e -> {
            pd.dismiss();
            DisplayViewUI.displayToast(this, Objects.requireNonNull(e.getMessage()));

        });
        


       /* filePath.putFile(imageUri).continueWithTask(task -> {


        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {



                //fire store cloud store
                alertCollectionReference.add(alertItems).addOnCompleteListener(task2 -> {

                    if (task2.isSuccessful()) {
                        String id = MainActivity.userId;

                        pd.dismiss();
                        DisplayViewUI.displayToast(ReportActivity.this, getString(R.string.reportSuccess));

                        startActivity(new Intent(ReportActivity.this, MainActivity.class)
                                .putExtra(AppConstants.UID, id)
                                .putExtra(AppConstants.USER_NAME, userName)
                                .putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl)
                                .putExtra(AppConstants.PHONE_NUMBER, phoneNumber)


                        );
                        finish();


                    } else {
                        pd.dismiss();
                        DisplayViewUI.displayToast(this, Objects.requireNonNull(task2.getException()).getMessage());

                    }

                });

            } else {
                pd.dismiss();
                DisplayViewUI.displayToast(ReportActivity.this, Objects.requireNonNull(task.getException()).getMessage());

            }

        });
*/

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConstants.CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

//todo change the name of the images uploaded to the server

                // Refreshing the gallery
                CameraUtils.refreshGallery(ReportActivity.this, imageStoragePath);
                //CameraUtils.optimizeBitmap(10,imageStoragePath);
                uri = CameraUtils.getOutputMediaFileUri(ReportActivity.this, new File(imageStoragePath));
                imgPhoto.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                Glide.with(ReportActivity.this).load(uri).into(activityReportBinding.imgAlertPhoto);


                btnUpload.setOnClickListener(view -> {
                    //display loading
                    pd = DisplayViewUI.displayProgress(ReportActivity.this, getString(R.string.uploadingImage));

//upload to server
                    try {
                        uploadToServer(uri, "image");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


            } else if (resultCode == Activity.RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(ReportActivity.this,
                        R.string.captureCanceled, Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(ReportActivity.this,
                        R.string.failedToCapture, Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == AppConstants.CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(ReportActivity.this, imageStoragePath);

                uri = CameraUtils.getOutputMediaFileUri(ReportActivity.this, new File(imageStoragePath));

                videoView.setVisibility(View.VISIBLE);
                imgPhoto.setVisibility(View.GONE);
                videoView.setVideoPath(imageStoragePath);
                videoView.start();

                btnUpload.setOnClickListener(view -> {
                    //display loading
                    pd = DisplayViewUI.displayProgress(ReportActivity.this, getString(R.string.uploadingVideo));

                    //upload to server
                    try {
                        uploadToServer(uri, "video");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });


            } else if (resultCode == Activity.RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(ReportActivity.this,
                        R.string.vidRecCanceled, Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(ReportActivity.this,
                        R.string.sorryVidFailed, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}