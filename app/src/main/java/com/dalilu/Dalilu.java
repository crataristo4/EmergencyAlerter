package com.dalilu;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class Dalilu extends Application {
    private HttpProxyCacheServer proxy;
    public static Context context;

    public static HttpProxyCacheServer getProxy(Context context) {
        Dalilu app = (Dalilu) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    public static Context getDaliluAppContext() {
        return Dalilu.context;

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        Dalilu.context = getApplicationContext();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().getReference().keepSynced(true);

    }

}
