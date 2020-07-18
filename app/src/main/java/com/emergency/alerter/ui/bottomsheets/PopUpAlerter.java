package com.emergency.alerter.ui.bottomsheets;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.emergency.alerter.R;
import com.emergency.alerter.databinding.PopUpAlerterBottomSheetBinding;
import com.emergency.alerter.utils.AppConstants;
import com.emergency.alerter.utils.CameraUtils;
import com.emergency.alerter.utils.DisplayViewUI;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PopUpAlerter extends BottomSheetDialogFragment {
    PopUpAlerterBottomSheetBinding popUpAlerterBottomSheetBinding;
    private Uri imageUri = null;
    private StorageReference imageStorageRef;
    private DatabaseReference dbRef;
    private static String imageStoragePath;



    /**
     * Capturing Camera Image will launch camera app requested image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(AppConstants.MEDIA_TYPE_IMAGE);
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(requireActivity(), file);

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

        Uri fileUri = CameraUtils.getOutputMediaFileUri(requireActivity(), file);

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
        Dexter.withActivity(requireActivity())
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Permissions required!")
                .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
                .setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CameraUtils.openSettings(requireContext());
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        popUpAlerterBottomSheetBinding = DataBindingUtil.inflate(inflater, R.layout.pop_up_alerter_bottom_sheet, container, false);

        return popUpAlerterBottomSheetBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();



    }

    private void initViews() {
//alert actions
        String[] alertActions = new String[]{getResources().getString(R.string.capture)
                , getResources().getString(R.string.recordVideo)
                , getResources().getString(R.string.recordAudio)};
//alert icons
        int[] alertIcons = new int[]{R.drawable.ic_photo_camera
                , R.drawable.ic_baseline_videocam_24
                , R.drawable.ic_baseline_record_voice_over_24};

        imageStorageRef = FirebaseStorage.getInstance().getReference();
        dbRef = FirebaseDatabase.getInstance().getReference().child("alerts");


        //row to store items
        List<HashMap<String, String>> hashMaps = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("icons", Integer.toString(alertIcons[i]));
            hm.put("actions", alertActions[i]);
            hashMaps.add(hm);


        }

        //keys for hash map
        String[] from = {"icons", "actions"};

        //ids of views
        int[] to = {R.id.imgAlertIcon
                , R.id.txtAlertAction};

        ListView alertListItems = popUpAlerterBottomSheetBinding.alertList;

        SimpleAdapter adapter = new SimpleAdapter(requireContext(), hashMaps, R.layout.layout_alert_items, from, to);

        alertListItems.setAdapter(adapter);
        alertListItems.setOnItemClickListener((parent, view1, position, id) -> {

            switch (position) {
                case 0:
                    /*DisplayViewUI.displayToast(getActivity(),"opening camera");
                    dismiss();
                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (captureIntent.resolveActivity(requireActivity().getPackageManager()) != null){

                        startActivityForResult(captureIntent, AppConstants.CAMERA_REQUEST_CODE);
                    }*/
                    if (CameraUtils.checkPermissions(requireContext())) {
                        captureImage();
                    } else {
                        requestCameraPermission(AppConstants.MEDIA_TYPE_IMAGE);
                    }
                    break;
                case 1:

                    if (CameraUtils.checkPermissions(requireContext())) {
                        captureVideo();
                    } else {
                        requestCameraPermission(AppConstants.MEDIA_TYPE_IMAGE);
                    }
                    dismiss();
                    break;

                case 2:
                    DisplayViewUI.displayToast(getActivity(),"recording audio");
                    dismiss();

                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + position);
            }


        });

        popUpAlerterBottomSheetBinding.btnCancel.setOnClickListener(v -> dismiss());

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        requireActivity();
        if (requestCode == AppConstants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            imageUri = data != null ? data.getData() : null;

          ProgressDialog pd =  DisplayViewUI.displayProgress(requireActivity(),"uploading");
            pd.show();
            StorageReference filePath = imageStorageRef.child("alertImages").child(Objects.requireNonNull(imageUri.getLastPathSegment()));

            filePath.putFile(imageUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    pd.dismiss();

dismiss();
                }
                return filePath.getDownloadUrl();

            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    Uri downLoadUri = task.getResult();
                    assert downLoadUri != null;
               String     getImageUploadUri = downLoadUri.toString();
                    Log.i( "Url: ", getImageUploadUri + " image path " + imageUri.getLastPathSegment());

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
                    DisplayViewUI.displayToast(requireContext(), Objects.requireNonNull(task.getException()).getMessage());

                }

            });


        }

        if (requestCode == AppConstants.CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(requireContext(), imageStoragePath);

                // successfully captured the image
                // display it in image view
                //previewCapturedImage();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(requireContext(),
                        "Capturing image cancelled", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(requireContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == AppConstants.CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(requireContext(), imageStoragePath);

                // video successfully recorded
                // preview the recorded video
                //previewVideo();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(requireContext(),
                        " video recording cancelled", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(requireContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
