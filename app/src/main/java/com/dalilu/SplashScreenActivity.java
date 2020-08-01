package com.dalilu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.dalilu.databinding.ActivitySplashScreenBinding;
import com.dalilu.ui.auth.PhoneAuthActivity;
import com.dalilu.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SplashScreenActivity extends AppCompatActivity {
    Intent intent;
    private ActivitySplashScreenBinding activitySplashScreenBinding;
    private DatabaseReference usersDbRef, usersDetails;
    private String uid, phoneNumber, firstName, lastName, userPhotoUrl;

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

                usersDbRef = FirebaseDatabase.getInstance().getReference().child("Users");

                usersDbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists() && snapshot.hasChild(uid)) {

                            // usersDetails = usersDbRef.child(uid);
                            usersDbRef.child(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    userPhotoUrl = (String) snapshot.child("userPhotoUrl").getValue();
                                    firstName = (String) snapshot.child("firstName").getValue();
                                    lastName = (String) snapshot.child("lastName").getValue();


                                    intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                                    intent.putExtra(AppConstants.UID, uid);
                                    intent.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
                                    intent.putExtra(AppConstants.FIRST_NAME, firstName);
                                    intent.putExtra(AppConstants.LAST_NAME, lastName);
                                    intent.putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    SplashScreenActivity.this.finishAffinity();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        } else if (!snapshot.hasChild(uid)) {

                            intent = new Intent(SplashScreenActivity.this, FinishAccountSetupActivity.class);
                            intent.putExtra(AppConstants.UID, uid);
                            intent.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            SplashScreenActivity.this.finishAffinity();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            } else {
                //Opens the Phone Auth Activity once the time elapses
                startActivity(new Intent(SplashScreenActivity.this, PhoneAuthActivity.class));
                finish();
            }

        }, 3000);


    }
}