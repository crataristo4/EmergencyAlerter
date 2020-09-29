package com.dalilu.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dalilu.R;
import com.dalilu.databinding.ActivityEditProfileBinding;
import com.dalilu.model.AlertItems;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.dalilu.utils.GetTimeAgo;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.Bitmap.CompressFormat;

public class EditProfileActivity extends BaseActivity {
    ProgressDialog progressDialog;
    private CircleImageView imgUserPhoto;
    private StorageReference mStorageReference;
    long timeInThirtyDays;
    private CollectionReference usersCollection, alertsCollection;
    private String getImageUri;
    private String phoneNumber;
    private String userPhotoUrl;
    private String uid, alertUserId, postId;
    private static final String TAG = "Edit-Profile";
    private Uri uri;
    private TextInputLayout txtUserName;
    String userName;
    private long timeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEditProfileBinding activityEditProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);
        setSupportActionBar(activityEditProfileBinding.toolBarCompleteProfile);


        imgUserPhoto = activityEditProfileBinding.imgUploadPhoto;
        txtUserName = activityEditProfileBinding.txtUserName;

        userName = BaseActivity.userName;
        userPhotoUrl = BaseActivity.userPhotoUrl;
        uid = BaseActivity.uid;
        phoneNumber = BaseActivity.phoneNumber;
        timeStamp = BaseActivity.timeStamp;

        Objects.requireNonNull(txtUserName.getEditText()).setText(userName);
        Objects.requireNonNull(activityEditProfileBinding.txtPhone.getEditText()).setText(phoneNumber);

        runOnUiThread(() -> Glide.with(EditProfileActivity.this)
                .load(userPhotoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.photo)
                .into(imgUserPhoto));


        progressDialog = DisplayViewUI.displayProgress(this, getString(R.string.saveDetails));

        activityEditProfileBinding.fabUploadPhoto.setOnClickListener(view -> DisplayViewUI.openGallery(EditProfileActivity.this));
        imgUserPhoto.setOnClickListener(view -> DisplayViewUI.openGallery(EditProfileActivity.this));

        //users
        usersCollection = FirebaseFirestore.getInstance().collection("Users");
        //alerts
        alertsCollection = FirebaseFirestore.getInstance().collection("Alerts");
        //storage reference
        mStorageReference = FirebaseStorage.getInstance().getReference("user photos");

        timeInThirtyDays = 2592000000L;
        //check and disable button for updating user profile
        String timeIn30days = String.valueOf(TimeUnit.MILLISECONDS.toDays(timeInThirtyDays));
        String timeUpdated = GetTimeAgo.getTimeAgo(timeStamp);

        assert timeUpdated != null;
        String[] split = timeUpdated.split("\\s");
        int splitDay = Integer.parseInt(split[0]);

        if (splitDay < Integer.parseInt(timeIn30days)) {
            activityEditProfileBinding.btnSave.setEnabled(false);
            Objects.requireNonNull(txtUserName.getEditText()).setError(getString(R.string.updateTime));
        } else {
            //user can update profile
            activityEditProfileBinding.btnSave.setOnClickListener(this::updateName);

        }

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
        Map<String, Object> accountInfo = new HashMap<>();
        accountInfo.put("userName", userName);
        accountInfo.put("timeStamp", GetTimeAgo.getTimeInMillis());

        //validations for user name
        if (!userName.trim().isEmpty()) {
            runOnUiThread(() -> {

                alertsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {

                    assert queryDocumentSnapshots != null;
                    for (QueryDocumentSnapshot ds : queryDocumentSnapshots) {

                        AlertItems alertItems = ds.toObject(AlertItems.class);
                        postId = ds.getId();
                        alertUserId = alertItems.getUserId();

                        //user has made a post
                        //compare id and update the name of the user who made the post
                        if (uid.equals(alertUserId)) {

                            alertsCollection.document(postId).update(accountInfo);
                            usersCollection.document(uid).update(accountInfo);

                            Log.i(TAG, "user name updated: " + userName + " on post id" + ds.getId());


                        } else {

                            //user has not made any post
                            //update only users details
                            usersCollection.document(uid).update(accountInfo);

                        }


                    }


                });


                DisplayViewUI.displayToast(EditProfileActivity.this, getString(R.string.successFull));

            });

        }
    }

    private void uploadFile() throws IOException {
        progressDialog.show();
        Map<String, Object> accountInfo = new HashMap<>();
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
                accountInfo.put("userPhotoUrl", getImageUri);

                alertsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {

                    assert queryDocumentSnapshots != null;
                    for (QueryDocumentSnapshot ds : queryDocumentSnapshots) {

                        AlertItems alertItems = ds.toObject(AlertItems.class);
                        postId = ds.getId();
                        alertUserId = alertItems.getUserId();

                        //user has made a post
                        //compare id and update the name of the user who made the post
                        if (uid.equals(alertUserId)) {

                            alertsCollection.document(postId).update(accountInfo);
                            usersCollection.document(uid).update(accountInfo);

                            Log.i(TAG, "user photo updated: " + getImageUri + " on post id" + ds.getId());


                        } else {

                            //user has not made any post
                            //update only users details
                            usersCollection.document(uid).update(accountInfo);
                            Log.i(TAG, "user photo updated: " + getImageUri);


                        }


                    }


                });
                //  usersCollection.document(uid).update(accountInfo);

                DisplayViewUI.displayToast(EditProfileActivity.this, getString(R.string.successFull));

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

    private void checkIfUserIdExist() {
        runOnUiThread(() -> alertsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {

            assert queryDocumentSnapshots != null;
            for (QueryDocumentSnapshot ds : queryDocumentSnapshots) {

                AlertItems alertItems = ds.toObject(AlertItems.class);
                postId = ds.getId();
                alertUserId = alertItems.getUserId();

                Log.i(TAG, "user id: " + alertUserId);

                if (uid.equals(alertUserId)) {

                    Map<String, Object> accountInfo = new HashMap<>();
                    accountInfo.put("userName", userName);
                    alertsCollection.document(postId).update(accountInfo);

                }


            }


        }));
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
                .putExtra(AppConstants.TIMESTAMP, timeStamp)
        );
        finishAffinity();
    }
}