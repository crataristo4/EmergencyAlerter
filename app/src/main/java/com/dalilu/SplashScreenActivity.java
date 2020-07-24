package com.dalilu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.dalilu.databinding.ActivitySplashScreenBinding;
import com.dalilu.ui.auth.PhoneAuthActivity;


public class SplashScreenActivity extends AppCompatActivity {
    ActivitySplashScreenBinding activitySplashScreenBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //overridePendingTransition(R.anim.fadein, R.anim.explode);
        super.onCreate(savedInstanceState);

        activitySplashScreenBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen);
        activitySplashScreenBinding.txtAppName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.from_top));
        // activitySplashScreenBinding.txtAppName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.explode));

        new Handler().postDelayed(() -> {
            //Opens the Welcome Screen Activity once the time elapses
            startActivity(new Intent(SplashScreenActivity.this, PhoneAuthActivity.class));
            finish();
        }, 5000);

    }
}