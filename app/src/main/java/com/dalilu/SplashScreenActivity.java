package com.dalilu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.dalilu.databinding.ActivitySplashScreenBinding;
import com.dalilu.ui.auth.PhoneAuthActivity;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class SplashScreenActivity extends AppCompatActivity {
    Intent intent;
    private ActivitySplashScreenBinding activitySplashScreenBinding;
    private DatabaseReference usersDbRef, usersDetails;
    private CollectionReference usersCollectionRef;
    private String uid, phoneNumber, userName, userPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //overridePendingTransition(R.anim.fadein, R.anim.explode);
        super.onCreate(savedInstanceState);


        activitySplashScreenBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen);
        // activitySplashScreenBinding.txtAppName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.from_top));
        activitySplashScreenBinding.txtAppName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadein));


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        new Handler().postDelayed(() -> {

            if (firebaseUser != null) {

                uid = firebaseUser.getUid();
                phoneNumber = firebaseUser.getPhoneNumber();

                Log.i("Id: ", uid);

                // usersDbRef = FirebaseDatabase.getInstance().getReference().child("Users");
                usersCollectionRef = FirebaseFirestore.getInstance().collection("Users");

                usersCollectionRef.get().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        if (!task.getResult().getDocuments().isEmpty()) {
                            DocumentReference usersDocDbRef = usersCollectionRef.document(uid);

                            usersDocDbRef.get().addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()) {
                                    DocumentSnapshot document = task1.getResult();
                                    if (document != null && document.exists()) {
                                        //Log.d("userName", Objects.requireNonNull(document.getString("userName")));
                                        //Log.d("phone", Objects.requireNonNull(document.getString("phoneNumber")));
                                        //Log.d("photo", Objects.requireNonNull(document.getString("userPhotoUrl")));

                                        userPhotoUrl = Objects.requireNonNull(document.getString("userPhotoUrl"));
                                        userName = Objects.requireNonNull(document.getString("userName"));
                                        phoneNumber = Objects.requireNonNull(document.getString("phoneNumber"));

                                        intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                                        intent.putExtra(AppConstants.UID, uid);
                                        intent.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
                                        intent.putExtra(AppConstants.USER_NAME, userName);
                                        intent.putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl);

                                    } else {

                                        intent = new Intent(SplashScreenActivity.this, FinishAccountSetupActivity.class);
                                        intent.putExtra(AppConstants.UID, uid);
                                        intent.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);


                                    }

                                    startActivity(intent);
                                    SplashScreenActivity.this.finishAffinity();

                                } else {

                                    DisplayViewUI.displayToast(SplashScreenActivity.this, task1.getException().getMessage());


                                }

                            });


                        }
                    }

                });


            } else {
                //Opens the Phone Auth Activity once the time elapses
                startActivity(new Intent(SplashScreenActivity.this, PhoneAuthActivity.class));
                finish();
            }

        }, 2000);


    }
}