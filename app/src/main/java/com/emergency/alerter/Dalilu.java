package com.emergency.alerter;

import android.app.Application;
import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;
import com.emergency.alerter.utils.LanguageManager;

public class Dalilu extends Application {
    private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        Dalilu app = (Dalilu) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LanguageManager.setLocale(base));

    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);

    }

}
