package com.dalilu.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.dalilu.R;
import com.dalilu.databinding.ActivitySplashScreenBinding;
import com.dalilu.ui.auth.RegisterPhoneNumberActivity;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class SplashScreenActivity extends AppCompatActivity {
    Intent intent;
    private CollectionReference usersCollectionRef;
    private String uid, phoneNumber, userName, userPhotoUrl;
    private long timeStamp;
    private static final String TAG = "SplashScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        ActivitySplashScreenBinding activitySplashScreenBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen);
        activitySplashScreenBinding.txtAppName.startAnimation(AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.fadein));

        runOnUiThread(this::startSplash);

    }


    void startSplash() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        new Handler().postDelayed(() -> {

            if (firebaseUser != null) {

                uid = firebaseUser.getUid();
                phoneNumber = firebaseUser.getPhoneNumber();

                usersCollectionRef = FirebaseFirestore.getInstance().collection("Users");

                usersCollectionRef.get().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        if (!task.getResult().getDocuments().isEmpty()) {
                            DocumentReference usersDocDbRef = usersCollectionRef.document(uid);

                            usersDocDbRef.get().addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()) {
                                    DocumentSnapshot document = task1.getResult();
                                    if (document != null && document.exists()) {

                                        userPhotoUrl = Objects.requireNonNull(document.getString("userPhotoUrl"));
                                        userName = Objects.requireNonNull(document.getString("userName"));
                                        phoneNumber = Objects.requireNonNull(document.getString("phoneNumber"));
                                        timeStamp = (long) Objects.requireNonNull(document.get("timeStamp"));

                                        intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                                        intent.putExtra(AppConstants.UID, uid);
                                        intent.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
                                        intent.putExtra(AppConstants.USER_NAME, userName);
                                        intent.putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl);
                                        intent.putExtra(AppConstants.TIMESTAMP, timeStamp);

                                        String days = String.valueOf(TimeUnit.MILLISECONDS.toDays(timeStamp));

                                        Log.i(TAG, "days--" + days);
                                        Log.i(TAG, "timestamp--" + timeStamp);

                                    } else {

                                        intent = new Intent(SplashScreenActivity.this, FinishAccountSetupActivity.class);
                                        intent.putExtra(AppConstants.UID, uid);
                                        intent.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);


                                    }

                                    startActivity(intent);
                                    SplashScreenActivity.this.finishAffinity();

                                } else {

                                    DisplayViewUI.displayToast(SplashScreenActivity.this, Objects.requireNonNull(task1.getException()).getMessage());


                                }

                            });


                        }
                    }

                });


            } else {
                //Opens the Phone Auth Activity once the time elapses
                startActivity(new Intent(SplashScreenActivity.this, RegisterPhoneNumberActivity.class));
                finish();
            }

        }, 3000);
    }
}