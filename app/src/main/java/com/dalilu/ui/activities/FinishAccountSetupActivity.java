package com.dalilu.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dalilu.R;
import com.dalilu.databinding.ActivityFinishAccountSetupBinding;
import com.dalilu.ui.bottomsheets.WelcomeNoticeBottomSheet;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.dalilu.utils.GetTimeAgo;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
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
        ActivityFinishAccountSetupBinding activityFinishAccountSetupBinding = DataBindingUtil.setContentView(this, R.layout.activity_finish_account_setup);

        imgUserPhoto = activityFinishAccountSetupBinding.imgUploadPhoto;
        txtUserName = activityFinishAccountSetupBinding.txtUserName;

        Intent getUserData = getIntent();
        if (getUserData != null) {
            uid = getUserData.getStringExtra(AppConstants.UID);
            phoneNumber = getUserData.getStringExtra(AppConstants.PHONE_NUMBER);
        }

        activityFinishAccountSetupBinding.btnSave.setOnClickListener(view -> {
            DisplayViewUI.displayAlertDialogMsg(this,
                    getString(R.string.ntice),
                    MessageFormat.format(getString(R.string.bfr) +
                                    getString(R.string.tkn) +
                                    getString(R.string.rcd) +
                                    getString(R.string.orUse),
                            Objects.requireNonNull(txtUserName.getEditText()).getText()),
                    getString(R.string.cancel),
                    getString(R.string.proCeed),
                    (dialogInterface, i) -> {

                        if (i == -1) {
                            try {
                                uploadFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else if (i == -2) {
                            dialogInterface.dismiss();
                        }

                    });

        });

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


    private void uploadFile() throws IOException {
        String userName = Objects.requireNonNull(txtUserName.getEditText()).getText().toString();
        //validations for user name
        if (userName.trim().isEmpty()) {
            int numberOfLetters = 6;
            userName = DisplayViewUI.getAlphaNumericString(numberOfLetters);

            txtUserName.getEditText().setText(userName);

        }

        Bitmap bitmap;

        //push to db
        if (uri != null) {

            ProgressDialog progressDialog = DisplayViewUI.displayProgress(this, getString(R.string.saveDetails));
            progressDialog.show();

            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);

            byte[] fileInBytes = byteArrayOutputStream.toByteArray();

            final StorageReference fileReference = mStorageReference.child(uid);
            String finalUserName = userName;
            fileReference.putBytes(fileInBytes).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    DisplayViewUI.displayToast(FinishAccountSetupActivity.this, Objects.requireNonNull(task.getException()).getMessage());

                }
                return fileReference.getDownloadUrl();

            }).addOnSuccessListener(uri -> {
                progressDialog.dismiss();

                Uri downLoadUri = Uri.parse(uri.toString());
                assert downLoadUri != null;
                getImageUri = downLoadUri.toString();

                Map<String, Object> accountInfo = new HashMap<>();
                accountInfo.put("userPhotoUrl", getImageUri);
                accountInfo.put("userName", finalUserName);
                accountInfo.put("phoneNumber", phoneNumber);
                accountInfo.put("userId", uid);
                accountInfo.put("timeStamp", GetTimeAgo.getTimeInMillis());

                usersCollection.document(uid).set(accountInfo);


                DisplayViewUI.displayToast(FinishAccountSetupActivity.this, getString(R.string.successFull));
                Intent intent = new Intent(FinishAccountSetupActivity.this, MainActivity.class);
                intent.putExtra(AppConstants.UID, uid);
                intent.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
                intent.putExtra(AppConstants.USER_NAME, finalUserName);
                intent.putExtra(AppConstants.USER_PHOTO_URL, getImageUri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                this.finishAffinity();


            }).addOnFailureListener(this, e -> {
                progressDialog.dismiss();
                DisplayViewUI.displayToast(FinishAccountSetupActivity.this, Objects.requireNonNull(e.getMessage()));

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