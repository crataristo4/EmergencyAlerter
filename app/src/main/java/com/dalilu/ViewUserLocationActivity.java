package com.dalilu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.dalilu.databinding.ActivityViewUserLocationBinding;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.GetTimeAgo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class ViewUserLocationActivity extends FragmentActivity implements OnMapReadyCallback {
    ActivityViewUserLocationBinding activityViewUserLocationBinding;
    TextView txtName, txtTime, txtKnownName;
    String name, location, photoUrl;
    long timeStamp;
    double lat, lng;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityViewUserLocationBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_user_location);
        txtKnownName = activityViewUserLocationBinding.txtLocation;
        txtName = activityViewUserLocationBinding.txtName;
        txtTime = activityViewUserLocationBinding.txtTime;

        //Reference of the MapFragment
        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.map_fragment);
        //Get the GoogleMap object
        Objects.requireNonNull(fragment).getMapAsync(this);

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
                    .error(getDrawable(R.drawable.boy))
                    .into(activityViewUserLocationBinding.imageView);

        }



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LatLng latLng = new LatLng(lat, lng);
        map.clear();
        map.addMarker(new MarkerOptions()
                .title(name + "'s location")
                .snippet("at " + location)
                .position(latLng)).showInfoWindow();
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);

    }

}