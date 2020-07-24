package com.dalilu.services;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;


public class LocationAsyncTask extends AsyncTask<LocationManager, Void, Void>
        implements LocationListener {

    private static final int POLL_TIME = 2048;
    //Location Provider
    private String locationProvider = LocationManager.GPS_PROVIDER;
    //Reference to the service that launched the async task. Used so that `placeMarker()` can be called.
    @SuppressLint("StaticFieldLeak")
    private LocationService activity;
    private LatLng lastKnownPosition;

    private boolean runAsyncTask = true;
    private boolean fetchForUpdate = true;


    public LocationAsyncTask(LocationService activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(LocationManager... locationManagers) {
        Looper.myLooper().prepare();
        LocationManager mLocationManager = locationManagers[0];

        LocationListener locationListener = this;

        try {
            mLocationManager.requestLocationUpdates(locationProvider, POLL_TIME, 0, locationListener, Looper.myLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        Looper.myLooper().loop();

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "location changed to - lat:"
                + location.getLatitude() + " lng: " + location.getLongitude());

        //Update lastKnownLocation based on the LocationManager
        lastKnownPosition = new LatLng(location.getLatitude(), location.getLongitude());

        activity.placeMarker(lastKnownPosition);
        Log.d("onLocationChanged", Looper.myLooper().toString());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
