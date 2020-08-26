package com.dalilu.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.dalilu.R;
import com.dalilu.ui.activities.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocationShareService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public final int uniqueId = 654321;
    GoogleApiClient client;
    LocationRequest request;
    LatLng latLngCurrent;
    CollectionReference locationCollectionRef;
    String uid;

    NotificationCompat.Builder notification;

    public LocationShareService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.


        throw new UnsupportedOperationException("Not yet implemented");


    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationCollectionRef = FirebaseFirestore.getInstance().collection("Locations");
        uid = MainActivity.userId;
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(false);
        notification.setOngoing(true);


        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        new LocationRequest();
        request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(500);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);

        notification.setSmallIcon(R.drawable.ic_location);
        notification.setTicker("Notification.");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Dalilu App");
        notification.setContentText("You are sharing your location.!");
        notification.setDefaults(Notification.DEFAULT_SOUND);


        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        notification.setContentIntent(pendingIntent);

        // Build the nofification

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        nm.notify(uniqueId, notification.build());


        // display notification
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        latLngCurrent = new LatLng(location.getLatitude(), location.getLongitude());

        shareLocation();


    }


    public void shareLocation() {
        // TODO: 8/21/2020  constantly update users location shared
    }


    @Override
    public void onDestroy() {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        client.disconnect();


        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(uniqueId);


    }
}
