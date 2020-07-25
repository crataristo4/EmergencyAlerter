package com.dalilu;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.dalilu.services.FirebaseManager;
import com.dalilu.services.LocationService;
import com.dalilu.services.PointOfInterest;
import com.dalilu.services.PointOfInterestService;
import com.dalilu.ui.PointOfInterestActivity;
import com.dalilu.ui.fragments.DetailsFragment;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    //Map object
    private GoogleMap mMap = null;
    //Permission request code for COARSE and FINE location permissions
    private int PERMISSION_REQUEST_LOCATION = 1337;
    //The marker of the user's position
    private Marker userMarker = null;
    //The marker used for the user's location
    private BitmapDescriptor mUserMarkerGraphic;
    //User's location
    private LatLng userLocation = null;
    //First location of the user after the app has been launched
    private LatLng firstLocation = null;
    //Map of markers mapped to points of interest
    private Map<Marker, PointOfInterest> mMarkerMap = new HashMap<>();
    public BroadcastReceiver broadcastReceiverPois = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            PointOfInterest poi = intent.getParcelableExtra("poi");

            String action = intent.getStringExtra("action");


            if (action.equals("add")) {
                //Add the marker to the map and put it in the hashmap
                Marker mark = mMap.addMarker(new MarkerOptions().position(poi.coordinatesToLatLng()).title(poi.getName()).visible(false));
                mMarkerMap.put(mark, poi);
                checkProximity(mark);
            } else {

                Iterator<Map.Entry<Marker, PointOfInterest>> it = mMarkerMap.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry<Marker, PointOfInterest> e = it.next();
                    if (e.getValue().equals(poi)) {
                        e.getKey().remove();
                        it.remove();
                        break;
                    }
                }
            }
        }
    };
    //Service that gets the location of the user
    private Intent locationService;
    //Service that gets the points of interest around the user
    private Intent poiService;
    //Visual representation of the area that the user can discover points of interest
    private Circle userProximity;
    //The fragment that is used in landscape mode to show the points of interest
    private DetailsFragment detailsFragment;
    //Boolean flag
    private boolean queryForPois = true;
    private Context cont = this;
    //BroadcastReceiver that get's location updates from LocationService
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //New location
            LatLng loc = intent.getParcelableExtra("coordinates");
            //If this is the first time we get a location
            if (userLocation == null) {
                firstLocation = loc;
            }
            userLocation = loc;

            //If the marker is not empty (app just launched)
            float[] distance = new float[1];

            Location.distanceBetween(userLocation.latitude, userLocation.longitude, firstLocation.latitude, firstLocation.longitude, distance);
            //If the distance is greater that 70-100km (roughly 1.0 in lng)
            if (distance[0] >= 70000.0) {
                stopService(poiService);
                //Relaunch the service with the new location of the user
                poiService.putExtra("userPosition", userLocation);
                startService(poiService);
            }

            //If we haven't started the service
            if (queryForPois) {
                poiService = new Intent(context, PointOfInterestService.class);
                poiService.putExtra("userPosition", userLocation);
                startService(poiService);
                queryForPois = false;
            }

            //Draw the marker and circle on user's position
            drawUser(userLocation);
        }
    };

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the location if there is one
        if (userLocation != null) {
            outState.putParcelable("userLocation", userLocation);
        }
        if (firstLocation != null) {
            outState.putParcelable("firstLocation", firstLocation);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queryForPois = true;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Fragment startingFragment = new DetailsFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.details_fragment, startingFragment).commit();
        }

        //Retrieve user's location from saved instance
        LatLng tempLoc = null;
        LatLng tempFirstLoc = null;
        if (savedInstanceState != null) {
            tempLoc = savedInstanceState.getParcelable("userLocation");
            tempFirstLoc = savedInstanceState.getParcelable("firstLocation");
        }

        if (tempLoc != null) {
            userLocation = tempLoc;
        }

        if (tempFirstLoc != null) {
            firstLocation = tempFirstLoc;
        }

        setContentView(R.layout.activity_maps);


        //get the fragment
        detailsFragment = (DetailsFragment) getSupportFragmentManager().findFragmentById(R.id.details_fragment);

        //Create a bitmap of the user's marker from drawable vector graphic
        Bitmap userMarkerStyle = BitmapFactory.decodeResource(this.getResources(), R.drawable.blue_user_marker);
        Bitmap resizedMarker = Bitmap.createScaledBitmap(userMarkerStyle, 32, 32, false);
        mUserMarkerGraphic = BitmapDescriptorFactory.fromBitmap(resizedMarker);

        FloatingActionButton fab = findViewById(R.id.captureFab);
        fab.setOnClickListener(view -> {
            if (userLocation == null) {
                DisplayViewUI.displayToast(view.getContext(), "unavailable");
                return;
            }
            DisplayViewUI.displayToast(view.getContext(), "hello");
            //Intent to create a new point of interest
            Intent newPoiIntent = new Intent(view.getContext(), PointOfInterestActivity.class);
            newPoiIntent.putExtra("location", userLocation);
            startActivity(newPoiIntent);
        });

        FloatingActionButton recenterPosition = findViewById(R.id.recenter_fab);
        recenterPosition.setOnClickListener(view -> {
            if (userLocation == null) {
                return;
            }
            //Recenter the camera on the user
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
        });


        //Maps
        //Reference of the MapFragment
        MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        //Get the GoogleMap object
        mf.getMapAsync(this);


        //Register the BroadcastReceiver so that we can receive location updates from LocationService
        registerReceiver(broadcastReceiver, new IntentFilter(LocationService.BROADCAST));

        //Register the BroadcastReceiver so that we can receive location updates from PointOfInterestService
        registerReceiver(broadcastReceiverPois, new IntentFilter(PointOfInterestService.BROADCAST_POI));

    }

    @Override
    public void onMapLoaded() {
        //If we have saved a previous location
        if (userLocation != null) {
            //Draw it
            drawUser(userLocation);
        }

        //Enable the compass on the top left (resets rotation of the camera)
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnInfoWindowLongClickListener(marker -> {
            if (marker.equals(userMarker)) {
                return;
            }
            if (mMarkerMap.get(marker) != null && mMarkerMap.get(marker).getUser().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])) {
                FirebaseManager.getInstance().deletePOI(mMarkerMap.get(marker));
            } else {
                Toast.makeText(cont, "Cannot delete other user's markers", Toast.LENGTH_SHORT).show();
            }
        });


        mMap.setOnInfoWindowClickListener(marker -> {

            if (marker.equals(userMarker)) {
                return;
            }
            //Else get the corresponding POI from the hashmap
            PointOfInterest poi = mMarkerMap.get(marker);

            //Save the POI in a bundle and pass it to a new fragment
            Bundle bundle = new Bundle();
            bundle.putParcelable("poi", poi);
            Fragment fragment = new DetailsFragment();
            fragment.setArguments(bundle);

            //If the orientation is landscape
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //replace the empty/temporary fragment with the one that has the POI
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.details_fragment, fragment);
                fragmentTransaction.commit();
            } else {
                //If we are in portrait launch a new activity with the POI
                Intent detailsIntent = new Intent(MapsActivity.this, PointOfInterestDetailsActivity.class);
                detailsIntent.putExtra("poi", poi);
                startActivity(detailsIntent);
            }
        });
        //Set the click listener for the markers
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //If the user marker is clicked show the info window
                marker.showInfoWindow();
                return true;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Get the GoogleMap since MapActivity implements the listener
        //Set the mMap to the googleMap
        mMap = googleMap;
        //Start the onMapLoaded() callback
        mMap.setOnMapLoadedCallback(this);

        //Request user's permission for location (coarse and fine)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        } else {
            startLocationRequest();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            //Permission granted
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Start request for location updates
                startLocationRequest();
            }
        }
    }

    public void startLocationRequest() {
        //Start an intent to the LocationService in order to receive location updates
        locationService = new Intent(this, LocationService.class);
        startService(locationService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Unregister the receivers
        unregisterReceiver(broadcastReceiverPois);
        unregisterReceiver(broadcastReceiver);

        //And stop the currently running services
        if (locationService != null) {
            stopService(locationService);
        }
        if (poiService != null) {
            stopService(poiService);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Stop the application. We don't want to return to the login screen.
        this.finishAffinity();
    }


    //Check whether the markers in the hashmap are within distance
    public void checkProximity() {
        float[] results = new float[1];
        for (Marker m : mMarkerMap.keySet()) {
            LatLng mLoc = m.getPosition();
            Location.distanceBetween(mLoc.latitude, mLoc.longitude, userLocation.latitude, userLocation.longitude, results);

            if (results[0] <= 500) {
                m.setVisible(true);
            } else {
                m.setVisible(false);
            }

        }
    }

    //Check whether a marker is withing the "circle" of the user
    public void checkProximity(Marker mark) {
        float[] results = new float[1];
        LatLng mLoc = mark.getPosition();
        //Get the distance between the two
        Location.distanceBetween(mLoc.latitude, mLoc.longitude, userLocation.latitude, userLocation.longitude, results);

        if (results[0] <= 500) {
            mark.setVisible(true);
        } else {
            mark.setVisible(false);
        }
    }


    //Draw the user and circle on the map
    public void drawUser(LatLng location) {
        //Replace it with a new one in the updated location
        if (userMarker == null) {
            userMarker = mMap.addMarker(new MarkerOptions().position(location).title("User's Location").icon(mUserMarkerGraphic));
        } else {
            userMarker.setPosition(userLocation);
        }

        if (userProximity == null) {
            userProximity = mMap.addCircle(new CircleOptions()
                    .strokeColor(Color.BLACK)
                    .strokeWidth(1)
                    .fillColor(getResources().getColor(R.color.translucent_yellow))
                    .center(userLocation)
                    .radius(500));
        } else {
            userProximity.setCenter(userLocation);
        }
        checkProximity();
    }

}
