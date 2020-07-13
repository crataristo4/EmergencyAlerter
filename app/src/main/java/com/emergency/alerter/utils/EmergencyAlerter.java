package com.emergency.alerter.utils;

import android.app.Application;
import android.content.Context;

public class EmergencyAlerter extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LanguageManager.setLocale(base));

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
