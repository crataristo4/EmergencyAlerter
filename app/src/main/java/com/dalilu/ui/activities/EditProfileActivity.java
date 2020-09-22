package com.dalilu.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dalilu.R;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.Bitmap.CompressFormat;

public class EditProfileActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    private CircleImageView imgUserPhoto;
    private StorageReference mStorageReference;
    private CollectionReference usersCollection;
    private String uid;
    private String getImageUri;
    private String phoneNumber;
    private String userPhotoUrl;
    private static final String TAG = "EditProfileActivity";
    private Uri uri;
    private TextInputLayout txtUserName;
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.dalilu.databinding.ActivityEditProfileBinding activityEditProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);
        setSupportActionBar(activityEditProfileBinding.toolBarCompleteProfile);

        imgUserPhoto = activityEditProfileBinding.imgUploadPhoto;
        txtUserName = activityEditProfileBinding.txtUserName;

        Intent getUserDetailsIntent = getIntent();
        if (getUserDetailsIntent != null) {
            userName = getUserDetailsIntent.getStringExtra(AppConstants.USER_NAME);
            userPhotoUrl = getUserDetailsIntent.getStringExtra(AppConstants.USER_PHOTO_URL);
            uid = getUserDetailsIntent.getStringExtra(AppConstants.UID);
            phoneNumber = getUserDetailsIntent.getStringExtra(AppConstants.PHONE_NUMBER);

            Objects.requireNonNull(txtUserName.getEditText()).setText(userName);
            Objects.requireNonNull(activityEditProfileBinding.txtPhone.getEditText()).setText(phoneNumber);

            runOnUiThread(() -> Glide.with(EditProfileActivity.this)
                    .load(userPhotoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.photo)
                    .into(imgUserPhoto));

        }


        progressDialog = DisplayViewUI.displayProgress(this, getString(R.string.saveDetails));

        activityEditProfileBinding.btnSave.setOnClickListener(this::updateName);

        activityEditProfileBinding.fabUploadPhoto.setOnClickListener(view -> DisplayViewUI.openGallery(EditProfileActivity.this));
        imgUserPhoto.setOnClickListener(view -> DisplayViewUI.openGallery(EditProfileActivity.this));

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

                Glide.with(EditProfileActivity.this)
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgUserPhoto);

                try {
                    uploadFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                String error = result.getError().getMessage();
                DisplayViewUI.displayToast(EditProfileActivity.this, error);
            }
        }
    }

    private void updateName(View view) {
        userName = Objects.requireNonNull(txtUserName.getEditText()).getText().toString();
        //  progressDialog.show();
        //validations for user name
        if (!userName.trim().isEmpty()) {
            runOnUiThread(() -> {
                DisplayViewUI.displayToast(EditProfileActivity.this, getString(R.string.successFull));
                Map<String, Object> accountInfo = new HashMap<>();
                accountInfo.put("userName", userName);
                usersCollection.document(uid).update(accountInfo);
            });


            //  onUpdateDone();


        }
    }


    void onUpdateDone() {
        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
        intent.putExtra(AppConstants.UID, uid);
        intent.putExtra(AppConstants.USER_NAME, userName);
        intent.putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl);
        intent.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
        startActivity(intent);
        EditProfileActivity.this.finishAffinity();

    }


    private void uploadFile() throws IOException {
        progressDialog.show();

        Bitmap bitmap;
        //push to db
        if (uri != null) {

            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 75, byteArrayOutputStream);

            byte[] fileInBytes = byteArrayOutputStream.toByteArray();

            final StorageReference fileReference = mStorageReference.child(uid);
            fileReference.putBytes(fileInBytes).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    DisplayViewUI.displayToast(EditProfileActivity.this, Objects.requireNonNull(task.getException()).getMessage());

                }
                return fileReference.getDownloadUrl();

            }).addOnSuccessListener(uri -> {
                progressDialog.dismiss();

                Uri downLoadUri = Uri.parse(uri.toString());
                assert downLoadUri != null;
                getImageUri = downLoadUri.toString();

                Map<String, Object> accountInfo = new HashMap<>();
                accountInfo.put("userPhotoUrl", getImageUri);

                usersCollection.document(uid).update(accountInfo);

                DisplayViewUI.displayToast(EditProfileActivity.this, getString(R.string.successFull));
                //  onUpdateDone();


            }).addOnFailureListener(this, e -> {
                progressDialog.dismiss();
                DisplayViewUI.displayToast(EditProfileActivity.this, Objects.requireNonNull(e.getMessage()));

            });


            //  file path for the itemImage
            // final StorageReference fileReference = mStorageReference.child(uid + "." + uri.getLastPathSegment());

          /*  fileReference.putFile(uri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    DisplayViewUI.displayToast(EditProfileActivity.this, Objects.requireNonNull(task.getException()).getMessage());

                }
                return fileReference.getDownloadUrl();

            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    Uri downLoadUri = task.getResult();
                    assert downLoadUri != null;
                    getImageUri = downLoadUri.toString();

                    Map<String, Object> accountInfo = new HashMap<>();
                    accountInfo.put("userPhotoUrl", getImageUri);


                    usersCollection.document(uid).update(accountInfo).addOnCompleteListener(task1 -> {

                        if (task1.isSuccessful()) {
                            progressDialog.dismiss();
                            DisplayViewUI.displayToast(EditProfileActivity.this, getString(R.string.successFull));
                            Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            EditProfileActivity.this.finishAffinity();

                        } else {
                            progressDialog.dismiss();
                            DisplayViewUI.displayToast(EditProfileActivity.this, Objects.requireNonNull(task1.getException()).getMessage());

                        }

                    });

                } else {
                    progressDialog.dismiss();
                    DisplayViewUI.displayToast(EditProfileActivity.this, Objects.requireNonNull(task.getException()).getMessage());

                }

            });*/
        } else {
            DisplayViewUI.displayToast(EditProfileActivity.this, getString(R.string.plsSlct));
        }
    }

    @Override
    public void onBackPressed() {
        gotoMain();
    }

    void gotoMain() {
        startActivity(new Intent(this, MainActivity.class)
                .putExtra(AppConstants.PHONE_NUMBER, phoneNumber)
                .putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl)
                .putExtra(AppConstants.USER_NAME, userName)
                .putExtra(AppConstants.UID, uid)
        );
        finishAffinity();
    }
}