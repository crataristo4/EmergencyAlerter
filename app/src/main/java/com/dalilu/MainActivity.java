package com.dalilu;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dalilu.databinding.ActivityMainBinding;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.CameraUtils;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class MainActivity extends BaseActivity {
    public static FirebaseUser firebaseUser;
    public static FirebaseAuth mAuth;
    private static String imageStoragePath;
    private static Object mContext;
    private ActivityMainBinding activityMainBinding;
    private StorageReference imageStorageRef, filePath;
    private Uri uri = null;
    private DatabaseReference dbRef;
    private ProgressDialog pd;
    private String uid;

    public static Context getAppContext() {
        return (Context) mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mContext = getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
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

        navView.setOnNavigationItemReselectedListener(item -> {

        });

        imageStorageRef = FirebaseStorage.getInstance().getReference().child("alerts");
        dbRef = FirebaseDatabase.getInstance().getReference().child("alerts");
        filePath = imageStorageRef.child(UUID.randomUUID().toString());

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


        if (requestCode == AppConstants.CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

//todo change the name of the images uploaded to the server

                // Refreshing the gallery
                CameraUtils.refreshGallery(MainActivity.this, imageStoragePath);

                uri = CameraUtils.getOutputMediaFileUri(MainActivity.this, new File(imageStoragePath));
                uploadToServer(uri);


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
                uri = CameraUtils.getOutputMediaFileUri(MainActivity.this, new File(imageStoragePath));

                uploadToServer(uri);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(MainActivity.this,
                        R.string.vidRecCanceled, Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(MainActivity.this,
                        R.string.sorryVidFailed, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void uploadToServer(Uri imageUri) {
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
                DisplayViewUI.displayToast(MainActivity.this, getString(R.string.reportSuccess));

                Uri downLoadUri = task.getResult();
                assert downLoadUri != null;
                String url = downLoadUri.toString();
                Log.i("Url: ", url);

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

    @Override
    protected void onStart() {
        super.onStart();

       /* try {
            assert firebaseUser != null;

            if (mAuth.getCurrentUser() == null) {
                SendUserToLoginActivity();
            } else {
                uid = firebaseUser.getUid();
                //checkDisplayAlertDialog();
                //retrieveServiceType();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}