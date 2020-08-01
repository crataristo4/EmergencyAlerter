package com.dalilu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dalilu.databinding.ActivityFinishAccountSetupBinding;
import com.dalilu.ui.bottomsheets.WelcomeNoticeBottomSheet;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FinishAccountSetupActivity extends AppCompatActivity {
    private ActivityFinishAccountSetupBinding activityFinishAccountSetupBinding;
    private CircleImageView imgUserPhoto;
    private StorageReference mStorageReference;
    private DatabaseReference usersDbRef;
    private String uid, getImageUri, phoneNumber;
    private Uri uri;
    private TextInputLayout txtFirstName, txtLastName, txtAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityFinishAccountSetupBinding = DataBindingUtil.setContentView(this, R.layout.activity_finish_account_setup);

        imgUserPhoto = activityFinishAccountSetupBinding.imgUploadPhoto;
        txtFirstName = activityFinishAccountSetupBinding.txtFirstName;
        txtLastName = activityFinishAccountSetupBinding.txtLastName;
        txtAbout = activityFinishAccountSetupBinding.txtAbout;
        Intent getUserData = getIntent();
        if (getUserData != null) {
            uid = getUserData.getStringExtra(AppConstants.UID);
            phoneNumber = getUserData.getStringExtra(AppConstants.PHONE_NUMBER);
        }

        activityFinishAccountSetupBinding.btnSave.setOnClickListener(view -> uploadFile());

//select or capture photo
        activityFinishAccountSetupBinding.fabUploadPhoto.setOnClickListener(view -> DisplayViewUI.openGallery(FinishAccountSetupActivity.this));
        imgUserPhoto.setOnClickListener(view -> DisplayViewUI.openGallery(FinishAccountSetupActivity.this));

//database reference
        usersDbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        //storage reference
        mStorageReference = FirebaseStorage.getInstance().getReference("user photos");

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == Activity.RESULT_OK) {
                assert result != null;
                uri = result.getUri();

                Glide.with(FinishAccountSetupActivity.this)
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgUserPhoto);

                //uploadFile();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                String error = result.getError().getMessage();
                DisplayViewUI.displayToast(FinishAccountSetupActivity.this, error);
            }
        }
    }


    private void uploadFile() {
        String fName = Objects.requireNonNull(txtFirstName.getEditText()).getText().toString();
        String lName = Objects.requireNonNull(txtLastName.getEditText()).getText().toString();
        String about = Objects.requireNonNull(txtAbout.getEditText()).getText().toString();


        //validations for about
        if (about.trim().isEmpty()) {
            txtAbout.setErrorEnabled(true);
            txtAbout.setError(getString(R.string.abtReq));
        } else {
            txtAbout.setErrorEnabled(false);
        }

        //validations for first name
        if (fName.trim().isEmpty()) {
            txtFirstName.setErrorEnabled(true);
            txtFirstName.setError(getString(R.string.fNameReq));
        } else {
            txtFirstName.setErrorEnabled(false);
        }

        //validations for last name
        if (lName.trim().isEmpty()) {
            txtLastName.setErrorEnabled(true);
            txtLastName.setError(getString(R.string.lNameReq));
        } else {
            txtLastName.setErrorEnabled(false);
        }


        //push to db
        if (uri != null && !fName.trim().isEmpty() && !lName.trim().isEmpty() && !about.trim().isEmpty()) {

            ProgressDialog progressDialog = DisplayViewUI.displayProgress(this, getString(R.string.saveDetails));
            progressDialog.show();

            //  file path for the itemImage
            final StorageReference fileReference = mStorageReference.child(uid + "." + uri.getLastPathSegment());

            fileReference.putFile(uri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    DisplayViewUI.displayToast(FinishAccountSetupActivity.this, Objects.requireNonNull(task.getException()).getMessage());

                }
                return fileReference.getDownloadUrl();

            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    Uri downLoadUri = task.getResult();
                    assert downLoadUri != null;
                    getImageUri = downLoadUri.toString();

                    Map<String, Object> accountInfo = new HashMap<>();
                    accountInfo.put("userPhotoUrl", getImageUri);
                    accountInfo.put("firstName", fName);
                    accountInfo.put("lastName", lName);
                    accountInfo.put("about", about);
                    accountInfo.put("phoneNumber", phoneNumber);

                    usersDbRef.setValue(accountInfo).addOnCompleteListener(task12 -> {
                        if (task12.isSuccessful()) {
                            progressDialog.dismiss();
                            DisplayViewUI.displayToast(FinishAccountSetupActivity.this, getString(R.string.successFull));
                            Intent intent = new Intent(FinishAccountSetupActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            FinishAccountSetupActivity.this.finishAffinity();


                        } else {
                            progressDialog.dismiss();
                            DisplayViewUI.displayToast(FinishAccountSetupActivity.this, Objects.requireNonNull(task12.getException()).getMessage());

                        }
                    });


                } else {
                    progressDialog.dismiss();
                    DisplayViewUI.displayToast(FinishAccountSetupActivity.this, Objects.requireNonNull(task.getException()).getMessage());

                }

            });
        }
    }

    private void checkDisplayAlertDialog() {
        SharedPreferences pref = getSharedPreferences(AppConstants.PREFS, 0);
        boolean alertShown = pref.getBoolean(AppConstants.IS_DIALOG_SHOWN, false);

        if (!alertShown) {
            new Handler().postDelayed(() -> {

                WelcomeNoticeBottomSheet welcomeNoticeBottomSheet = new WelcomeNoticeBottomSheet();
                welcomeNoticeBottomSheet.setCancelable(false);
                welcomeNoticeBottomSheet.show(getSupportFragmentManager(), "welcome");

            }, 500);

            SharedPreferences.Editor edit = pref.edit();
            edit.putBoolean(AppConstants.IS_DIALOG_SHOWN, true);
            edit.apply();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {

            checkDisplayAlertDialog();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}