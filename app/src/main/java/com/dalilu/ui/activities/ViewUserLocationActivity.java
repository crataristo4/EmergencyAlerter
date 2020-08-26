package com.dalilu.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.dalilu.R;
import com.dalilu.databinding.ActivityViewUserLocationBinding;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.GetTimeAgo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class ViewUserLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    ActivityViewUserLocationBinding activityViewUserLocationBinding;
    TextView txtName, txtTime, txtKnownName;
    String name, location, photoUrl;
    long timeStamp;
    double lat, lng;
    private float mapZoomLevel;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityViewUserLocationBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_user_location);
        txtKnownName = activityViewUserLocationBinding.txtLocation;
        txtName = activityViewUserLocationBinding.txtName;
        txtTime = activityViewUserLocationBinding.txtTime;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        Intent intent = getIntent();
        if (intent != null) {

            name = intent.getStringExtra(AppConstants.USER_NAME);
            location = intent.getStringExtra(AppConstants.KNOWN_LOCATION);
            photoUrl = intent.getStringExtra(AppConstants.USER_PHOTO_URL);
            timeStamp = intent.getLongExtra(AppConstants.TIMESTAMP, 0);
            lat = intent.getDoubleExtra(AppConstants.LATITUDE, 0);
            lng = intent.getDoubleExtra(AppConstants.LONGITUDE, 0);

            activityViewUserLocationBinding.txtName.setText(name);
            activityViewUserLocationBinding.txtLocation.setText(location);
            activityViewUserLocationBinding.txtTime.setText(GetTimeAgo.getTimeAgo(timeStamp));

            Glide.with(this)
                    .load(photoUrl)
                    .error(ContextCompat.getDrawable(this, R.drawable.boy))
                    .into(activityViewUserLocationBinding.imageView);

            mapZoomLevel = 13;
        }


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.custommap));
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(ViewUserLocationActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermission();
                return;
            }

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setZoomGesturesEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.getUiSettings().setRotateGesturesEnabled(true);


            LatLng latLng = new LatLng(lat, lng);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mapZoomLevel));

            map.setOnCameraMoveListener(() -> mapZoomLevel = map.getCameraPosition().zoom);
            googleMap.clear();
            if (name.equals("You")) {
                map.addMarker(new MarkerOptions()
                        .title("Your: " + "current location")
                        .snippet(location)
                        .position(latLng)).showInfoWindow();
            } else {
                map.addMarker(new MarkerOptions()
                        .title(name + "'s location")
                        .snippet("at " + location)
                        .position(latLng)).showInfoWindow();
            }


        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        buildGoogleApiClient();

        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                AppConstants.REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermission();
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
      /*  LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//
        try {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 30f));

        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

}