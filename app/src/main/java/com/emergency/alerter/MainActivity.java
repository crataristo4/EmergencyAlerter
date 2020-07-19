package com.emergency.alerter;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.emergency.alerter.databinding.ActivityMainBinding;
import com.emergency.alerter.utils.AppConstants;
import com.emergency.alerter.utils.CameraUtils;
import com.emergency.alerter.utils.DisplayViewUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;
    private StorageReference imageStorageRef,filePath;
    private DatabaseReference dbRef;
    private static String imageStoragePath;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initViews();

    }

    private void initViews() {
        BottomNavigationView navView = activityMainBinding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_alerts, R.id.navigation_contacts)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);


        imageStorageRef = FirebaseStorage.getInstance().getReference().child("alerts");
        dbRef = FirebaseDatabase.getInstance().getReference().child("alerts");


        activityMainBinding.capture.setOnClickListener(v -> {
            if (CameraUtils.checkPermissions(v.getContext())) {
                captureImage();
            } else {
                requestCameraPermission(AppConstants.MEDIA_TYPE_IMAGE);
            }
        });

        activityMainBinding.recordVideo.setOnClickListener(v -> {
            if (CameraUtils.checkPermissions(v.getContext())) {
                captureVideo();
            } else {
                requestCameraPermission(AppConstants.MEDIA_TYPE_IMAGE);
            }
        });

        activityMainBinding.recordAudio.setOnClickListener(v -> {

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

        Uri fileUri = CameraUtils.getOutputMediaFileUri(MainActivity.this, file);

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

        Uri fileUri = CameraUtils.getOutputMediaFileUri(MainActivity.this, file);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file

        // start the video capture Intent
        startActivityForResult(intent, AppConstants.CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Requesting permissions using Dexter library
     */
    private void requestCameraPermission(final int type) {
        Dexter.withActivity(MainActivity.this)
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
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.perRequired)
                .setMessage(R.string.camPerm)
                .setPositiveButton(R.string.goToSettings, (dialog, which) -> CameraUtils.openSettings(MainActivity.this))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                }).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConstants.CAMERA_CAPTURE_IMAGE_REQUEST_CODE ) {
            if (resultCode == Activity.RESULT_OK) {


//todo change the name of the images uploaded to the server

                // Refreshing the gallery
                CameraUtils.refreshGallery(MainActivity.this, imageStoragePath);

                 filePath = imageStorageRef.child(UUID.randomUUID().toString());

                Uri imageUri = CameraUtils.getOutputMediaFileUri(MainActivity.this, new File(imageStoragePath));

                uploadImageToServer(imageUri);


            } else if (resultCode == Activity.RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(MainActivity.this,
                        R.string.captureCanceled, Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(MainActivity.this,
                        R.string.failedToCapture, Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == AppConstants.CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(MainActivity.this, imageStoragePath);

                // video successfully recorded
                // preview the recorded video
                //previewVideo();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(MainActivity.this,
                        " video recording cancelled", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(MainActivity.this,
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void uploadImageToServer(Uri imageUri) {
        //display loading
        pd = DisplayViewUI.displayProgress(MainActivity.this, getString(R.string.uploadingPleaseWait));
        pd.show();

        //upload photo to server
        filePath.putFile(imageUri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                pd.dismiss();

            }
            return filePath.getDownloadUrl();

        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pd.dismiss();
                DisplayViewUI.displayToast(MainActivity.this, "success");

                Uri downLoadUri = task.getResult();
                assert downLoadUri != null;
                String getImageUploadUri = downLoadUri.toString();
                Log.i("Url: ", getImageUploadUri );

                String documentId = UUID.randomUUID().toString();

                //fire store cloud store
                    /*dbReference.add(itemsMap).addOnCompleteListener(task2 -> {

                        if (task2.isSuccessful()) {
                            serviceTypeDbRef.child(uid).child(randomUID).setValue(itemsMap);

                            progressDialog.dismiss();
                            DisplayViewUI.displayToast(this, "Successful");

                            startActivity(new Intent(AddDesignOrStyleActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();


                        } else {
                            pd.dismiss();
                            DisplayViewUI.displayToast(this, Objects.requireNonNull(task2.getException()).getMessage());

                        }

                    });
*/
            } else {
                pd.dismiss();
                DisplayViewUI.displayToast(MainActivity.this, Objects.requireNonNull(task.getException()).getMessage());

            }

        });


    }

}