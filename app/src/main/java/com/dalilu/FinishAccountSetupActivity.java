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
import com.dalilu.ui.bottomsheets.WelcomeNoticeBottomSheet;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FinishAccountSetupActivity extends AppCompatActivity {
    private CircleImageView imgUserPhoto;
    private StorageReference mStorageReference;
    private CollectionReference usersCollection;
    private String uid, getImageUri, phoneNumber;
    private Uri uri;
    private TextInputLayout txtUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.dalilu.databinding.ActivityFinishAccountSetupBinding activityFinishAccountSetupBinding = DataBindingUtil.setContentView(this, R.layout.activity_finish_account_setup);

        imgUserPhoto = activityFinishAccountSetupBinding.imgUploadPhoto;
        txtUserName = activityFinishAccountSetupBinding.txtUserName;

        Intent getUserData = getIntent();
        if (getUserData != null) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            assert user != null;
            uid = user.getUid();

            uid = getUserData.getStringExtra(AppConstants.UID);
            phoneNumber = getUserData.getStringExtra(AppConstants.PHONE_NUMBER);
        }

        activityFinishAccountSetupBinding.btnSave.setOnClickListener(view -> uploadFile());

//select or capture photo
        activityFinishAccountSetupBinding.fabUploadPhoto.setOnClickListener(view -> DisplayViewUI.openGallery(FinishAccountSetupActivity.this));
        imgUserPhoto.setOnClickListener(view -> DisplayViewUI.openGallery(FinishAccountSetupActivity.this));

//database reference
        usersCollection = FirebaseFirestore.getInstance().collection("Users");
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
        String userName = Objects.requireNonNull(txtUserName.getEditText()).getText().toString();
        //validations for user name
        if (userName.trim().isEmpty()) {
            int numberOfLetters = 6;
            userName = DisplayViewUI.getAlphaNumericString(numberOfLetters);

            txtUserName.getEditText().setText(userName);

        }


        //push to db
        if (uri != null) {

            ProgressDialog progressDialog = DisplayViewUI.displayProgress(this, getString(R.string.saveDetails));
            progressDialog.show();

            //  file path for the itemImage
            final StorageReference fileReference = mStorageReference.child(uid + "." + uri.getLastPathSegment());

            String finalUserName = userName;
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
                    accountInfo.put("userName", finalUserName);
                    accountInfo.put("phoneNumber", phoneNumber);
                    accountInfo.put("userId", uid);


                    usersCollection.document(uid).set(accountInfo).addOnCompleteListener(task1 -> {

                        if (task1.isSuccessful()) {
                            progressDialog.dismiss();
                            DisplayViewUI.displayToast(FinishAccountSetupActivity.this, getString(R.string.successFull));
                            Intent intent = new Intent(FinishAccountSetupActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            FinishAccountSetupActivity.this.finishAffinity();

                        } else {
                            progressDialog.dismiss();
                            DisplayViewUI.displayToast(FinishAccountSetupActivity.this, Objects.requireNonNull(task1.getException()).getMessage());

                        }

                    });

/*
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
*/


                } else {
                    progressDialog.dismiss();
                    DisplayViewUI.displayToast(FinishAccountSetupActivity.this, Objects.requireNonNull(task.getException()).getMessage());

                }

            });
        } else {
            DisplayViewUI.displayToast(FinishAccountSetupActivity.this, getString(R.string.plsSlct));
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
        this.finishAffinity();
    }
}