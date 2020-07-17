package com.emergency.alerter.ui.bottomsheets;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.emergency.alerter.MainActivity;
import com.emergency.alerter.R;
import com.emergency.alerter.databinding.PopUpAlerterBottomSheetBinding;
import com.emergency.alerter.utils.AppConstants;
import com.emergency.alerter.utils.DisplayViewUI;
import com.emergency.alerter.utils.GetTimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PopUpAlerter extends BottomSheetDialogFragment {
    PopUpAlerterBottomSheetBinding popUpAlerterBottomSheetBinding;
    private Uri imageUri = null;
    private StorageReference imageStorageRef;
    private DatabaseReference dbRef;



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
                    DisplayViewUI.displayToast(getActivity(),"opening camera");
                    dismiss();
                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (captureIntent.resolveActivity(requireActivity().getPackageManager()) != null){

                        startActivityForResult(captureIntent, AppConstants.CAMERA_REQUEST_CODE);
                    }
                    break;
                case 1:

                    DisplayViewUI.displayToast(getActivity(),"recording video");
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
    }
}
