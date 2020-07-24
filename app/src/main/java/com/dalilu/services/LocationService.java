package com.dalilu.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;

import com.google.android.gms.maps.model.LatLng;

public class LocationService extends Service {

    //Broadcast string for broadcasting the location back to `MapActivity`
    public static final String BROADCAST = "com.alex.service";
    //Minimum time for `LocationManager` to fetch an updated location
    private static final int POLL_TIME = 2024;
    //LocationManager, fetches the user's location
    private LocationManager locationManager;

    public LocationService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Get the location from the SystemServices
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            //Create an asynctask to get the location
            LocationAsyncTask locationAsyncTask = new LocationAsyncTask(LocationService.this);
            //Pass the location manager to the async task
            locationAsyncTask.execute(locationManager);
        } catch (SecurityException e) {
            //e.g. permission has not been given by the user.
            e.printStackTrace();
        }
        //Start the service as STICKY. Service won't stopped until it is explicitly stopped.
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void placeMarker(LatLng coordinates) {
        //Start an intent to broadcast the new location back to the MapActivity
        Intent intent = new Intent(BROADCAST);
        //Pass the coordinates to the intent
        intent.putExtra("coordinates", coordinates);

        //Broadcast the intent to MapActivity
        sendBroadcast(intent);
    }
}
