package com.dalilu.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.dalilu.R;
import com.dalilu.databinding.ActivityReportBinding;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.CameraUtils;
import com.dalilu.utils.DisplayViewUI;
import com.dalilu.utils.GetTimeAgo;
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

import static android.provider.MediaStore.Images.Media.getBitmap;

public class ReportActivity extends BaseActivity {

    private static final String TAG = "ReportActivity";
    private static String imageStoragePath;
    ActivityReportBinding activityReportBinding;
    private StorageReference filePath;
    private Uri uri = null;
    private ProgressDialog pd;
    private CollectionReference alertCollectionReference;
    // key to store image path in savedInstance state
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";
    private double latitude, longitude;
    private ImageView imgPhoto;
    private VideoView videoView;
    private Button btnUpload;
    // Bitmap sampling size
    public static final int BITMAP_SAMPLE_SIZE = 8;
    private String knownName, state, address, country, phoneNumber, userId, userName, userPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityReportBinding = DataBindingUtil.setContentView(this, R.layout.activity_report);
        setSupportActionBar(activityReportBinding.toolBarReport);

        Intent getUserDetailsIntent = getIntent();
        if (getUserDetailsIntent != null) {
            userName = getUserDetailsIntent.getStringExtra(AppConstants.USER_NAME);
            userPhotoUrl = getUserDetailsIntent.getStringExtra(AppConstants.USER_PHOTO_URL);
            userId = getUserDetailsIntent.getStringExtra(AppConstants.UID);
            phoneNumber = getUserDetailsIntent.getStringExtra(AppConstants.PHONE_NUMBER);

            address = BaseActivity.address;
            state = BaseActivity.state;
            country = BaseActivity.country;
            knownName = BaseActivity.knownName;
            latitude = BaseActivity.latitude;
            longitude = BaseActivity.longitude;

            Log.i(TAG, "---: " + userName + phoneNumber + userPhotoUrl + latitude + longitude + address + "--" + state + "__" + knownName);

        }


        updateAddress();


        StorageReference imageStorageRef = FirebaseStorage.getInstance().getReference().child("alerts");
        filePath = imageStorageRef.child(UUID.randomUUID().toString());
        alertCollectionReference = FirebaseFirestore.getInstance().collection("Alerts");
        imgPhoto = activityReportBinding.imgAlertPhoto;
        videoView = activityReportBinding.videoView;
        btnUpload = activityReportBinding.btnUpload;

        activityReportBinding.fabCamera.setOnClickListener(v -> {
            if (CameraUtils.checkPermissions(v.getContext())) {

                captureImage();

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

        // restoring storage image path from saved instance state
        // otherwise the path will be null on device rotation
        restoreFromBundle(savedInstanceState);


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


    void updateAddress() {

        if (address == null && knownName == null && state == null && country == null) {
            try {
                List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

                if (addressList != null) {
                    address = addressList.get(0).getAddressLine(0);
                    state = addressList.get(0).getAdminArea();
                    country = addressList.get(0).getCountryName();
                    knownName = addressList.get(0).getFeatureName();

                    Log.i(TAG, "Location: " + address + " --" + state + " --" + country + " --" + knownName);


                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void uploadToServer(Uri imageUri, String type) throws IOException {
        pd.show();
        StringBuilder addressBuilder = new StringBuilder();
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:MM a");
        String dateReported = dateFormat.format(Calendar.getInstance().getTime());

        updateAddress();

        addressBuilder.append(knownName).append(",").append(state).append(",").append(country);


        if (type.equals("image")) {

            //compress image
            Bitmap bitmap;
            bitmap = getBitmap(getContentResolver(), imageUri);
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
                alertItems.put("address", addressBuilder.toString());
                alertItems.put("userId", userId);
                alertItems.put("phoneNumber", phoneNumber);
                alertItems.put("timeStamp", GetTimeAgo.getTimeInMillis());
                alertItems.put("dateReported", dateReported);
                alertItems.put("isSolved", false);


                alertCollectionReference.add(alertItems);
                String id = MainActivity.userId;
                Log.i(TAG, "uploadToServer: " + addressBuilder.toString());

                startActivity(new Intent(ReportActivity.this, MainActivity.class)
                        .putExtra(AppConstants.PHONE_NUMBER, phoneNumber)
                        .putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl)
                        .putExtra(AppConstants.USER_NAME, userName)
                        .putExtra(AppConstants.UID, userId));
                finish();


            }).addOnFailureListener(this, e -> {
                pd.dismiss();
                DisplayViewUI.displayToast(this, Objects.requireNonNull(e.getMessage()));

            });


        } else if (type.equals("video")) {
            filePath.putFile(imageUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    pd.dismiss();

                }

                return filePath.getDownloadUrl();

            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    Uri downLoadUri = task.getResult();
                    assert downLoadUri != null;
                    String url = downLoadUri.toString();

                    Map<String, Object> alertItems = new HashMap<>();
                    alertItems.put("userName", userName);
                    alertItems.put("userPhotoUrl", userPhotoUrl);
                    alertItems.put("url", url);
                    alertItems.put(type, type);
                    alertItems.put("coordinates", new GeoPoint(latitude, longitude));
                    alertItems.put("address", addressBuilder.toString());
                    alertItems.put("userId", userId);
                    alertItems.put("phoneNumber", phoneNumber);
                    alertItems.put("timeStamp", GetTimeAgo.getTimeInMillis());
                    alertItems.put("dateReported", dateReported);
                    alertItems.put("isSolved", false);


                    //fire store cloud store
                    alertCollectionReference.add(alertItems).addOnCompleteListener(task2 -> {

                        if (task2.isSuccessful()) {

                            pd.dismiss();
                            DisplayViewUI.displayToast(ReportActivity.this, getString(R.string.reportSuccess));

                            startActivity(new Intent(ReportActivity.this, MainActivity.class)
                                    .putExtra(AppConstants.PHONE_NUMBER, phoneNumber)
                                    .putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl)
                                    .putExtra(AppConstants.USER_NAME, userName)
                                    .putExtra(AppConstants.UID, userId));
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

        }


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
                previewCapturedImage();

                btnUpload.setOnClickListener(view -> {
                    //display loading
                    pd = DisplayViewUI.displayProgress(ReportActivity.this, getString(R.string.uploadingImage));
                    if (address == null && knownName == null && state == null && country == null) {
                        updateAddress();
                    } else {
                        //upload to server
                        try {
                            uploadToServer(uri, "image");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

                previewVideo();

                btnUpload.setOnClickListener(view -> {
                    //display loading
                    pd = DisplayViewUI.displayProgress(ReportActivity.this, getString(R.string.uploadingVideo));
                    if (address == null && knownName == null && state == null && country == null) {
                        updateAddress();
                    } else {
                        //upload to server
                        try {
                            uploadToServer(uri, "video");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

    /**
     * Display image from gallery
     */
    private void previewCapturedImage() {
        try {
            // hide video preview
            imgPhoto.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);

            Bitmap bitmap = CameraUtils.optimizeBitmap(BITMAP_SAMPLE_SIZE, imageStoragePath);

            imgPhoto.setImageBitmap(bitmap);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displaying video in VideoView
     */
    private void previewVideo() {
        try {
            videoView.setVisibility(View.VISIBLE);
            imgPhoto.setVisibility(View.GONE);
            videoView.setVideoPath(imageStoragePath);
            videoView.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Restoring store image path from saved instance state
     */
    private void restoreFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_IMAGE_STORAGE_PATH)) {
                imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
                if (!TextUtils.isEmpty(imageStoragePath)) {
                    if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + AppConstants.IMAGE_EXTENSION)) {
                        previewCapturedImage();
                    } else if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + AppConstants.VIDEO_EXTENSION)) {
                        previewVideo();
                    }
                }
            }
        }
    }


    /**
     * Saving stored image path to saved instance state
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putString(KEY_IMAGE_STORAGE_PATH, imageStoragePath);
    }

    /**
     * Restoring image path from saved instance state
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
    }

}