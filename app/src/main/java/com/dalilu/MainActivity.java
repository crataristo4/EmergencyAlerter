package com.dalilu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dalilu.databinding.ActivityMainBinding;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    public static String userName, userPhotoUrl;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;
    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;
    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    private static Object mContext;
    private ActivityMainBinding activityMainBinding;

    /**
     * Time when the location was updated represented as a String.
     */
    public static String mLastUpdateTime, knownName, state, country, phoneNumber, userId;
    public static double latitude, longitude;
    private Geocoder geocoder;
    private List<Address> addressList;
    private DatabaseReference locationDbRef;
    private CollectionReference alertsCollectionReference;


    public static Context getAppContext() {
        return (Context) mContext;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mContext = getApplicationContext();
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());



        createLocationCallback();
        createLocationRequest();
        requestPermissions();


        initViews();

    }

    private void initViews() {

        BottomNavigationView navView = activityMainBinding.navView;
        Menu menu = navView.getMenu();
        MenuItem menuItemHome = menu.findItem(R.id.navigation_home);
        MenuItem menuItemFriends = menu.findItem(R.id.navigation_contacts);
        MenuItem menuItemNotification = menu.findItem(R.id.navigation_alerts);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_alerts, R.id.navigation_home, R.id.navigation_contacts)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setOnNavigationItemReselectedListener(item -> {

        });


        BadgeDrawable badgeDrawableHome = navView.getOrCreateBadge(menuItemHome.getItemId());
        BadgeDrawable badgeDrawableNotification = navView.getOrCreateBadge(menuItemNotification.getItemId());
        BadgeDrawable badgeDrawableFriends = navView.getOrCreateBadge(menuItemFriends.getItemId());

        activityMainBinding.searchContact.setOnClickListener(view -> startActivity(new Intent(view.getContext(), SearchContactActivity.class)));

        activityMainBinding.logOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(view.getContext(), SplashScreenActivity.class));

        });

        activityMainBinding.contacts.setOnClickListener(view -> {

            startActivity(new Intent(view.getContext(), ContactsActivity.class));

        });

        activityMainBinding.editProfile.setOnClickListener(view -> startActivity(new Intent(view.getContext(), EditProfileActivity.class)));

        Intent getUserDetailsIntent = getIntent();
        if (getUserDetailsIntent != null) {

            userName = getUserDetailsIntent.getStringExtra(AppConstants.USER_NAME);
            userPhotoUrl = getUserDetailsIntent.getStringExtra(AppConstants.USER_PHOTO_URL);
            userId = getUserDetailsIntent.getStringExtra(AppConstants.UID);
            phoneNumber = getUserDetailsIntent.getStringExtra(AppConstants.PHONE_NUMBER);

            locationDbRef = FirebaseDatabase.getInstance().getReference().child("Locations").child(userId);
            locationDbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists() && snapshot.hasChildren()) {

                        int numberOfItems = (int) snapshot.getChildrenCount();

                        if (numberOfItems > 0) {

                            badgeDrawableNotification.setNumber(numberOfItems);


                        }


                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    DisplayViewUI.displayToast(MainActivity.this, error.getMessage());

                }
            });

            alertsCollectionReference = FirebaseFirestore.getInstance().collection("Alerts");
            alertsCollectionReference.get().addOnCompleteListener(task -> {

                if (task.getResult().size() > 0)
                    badgeDrawableHome.setNumber(task.getResult().size());

            });

        }

        activityMainBinding.report.setOnClickListener(v -> {
            Intent reportIntent = new Intent(v.getContext(), ReportActivity.class);
            reportIntent.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
            reportIntent.putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl);
            reportIntent.putExtra(AppConstants.USER_NAME, userName);
            reportIntent.putExtra(AppConstants.STATE, state);
            reportIntent.putExtra(AppConstants.COUNTRY, country);
            reportIntent.putExtra(AppConstants.KNOWN_LOCATION, knownName);

            startActivity(reportIntent);

        });


    }


    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(AppConstants.KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        AppConstants.KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(AppConstants.KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(AppConstants.KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(AppConstants.KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(AppConstants.KEY_LAST_UPDATED_TIME_STRING);
            }
            // updateUI();
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(AppConstants.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(AppConstants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            Log.i(TAG, String.format(Locale.ENGLISH, "%s: %f", "lat",
                    mCurrentLocation.getLatitude()));
            Log.i(TAG, String.format(Locale.ENGLISH, "%s: %f", "lng",
                    mCurrentLocation.getLongitude()));
            Log.i(TAG, String.format(Locale.ENGLISH, "%s: %s",
                    "last update", mLastUpdateTime));

            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();


            try {
                addressList = geocoder.getFromLocation(latitude, longitude, 1);

                if (addressList != null) {
                    String address = addressList.get(0).getAddressLine(0);
                    state = addressList.get(0).getAdminArea();
                    country = addressList.get(0).getCountryName();
                    knownName = addressList.get(0).getFeatureName();


                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == AppConstants.REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else {

                requestPermissions();
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
               /* DisplayViewUI.displayAlertDialog(MainActivity.this, "OOPS", getString(R.string.permission_denied_explanation),
                        getString(R.string.settings),
                        (dialogInterface, i) -> {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        });*/

            }
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            DisplayViewUI.displayAlertDialog(MainActivity.this,
                    getString(R.string.grantPerm),
                    getString(R.string.permission_rationale), getString(R.string.ok), (dialogInterface, i) -> {
// Request permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                AppConstants.REQUEST_PERMISSIONS_REQUEST_CODE);
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    AppConstants.REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, locationSettingsResponse -> {
                    Log.i(TAG, "All location settings are satisfied.");

                    //noinspection MissingPermission
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback, Looper.myLooper());

                    //updateUI();
                })
                .addOnFailureListener(this, e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings ");

                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(MainActivity.this, AppConstants.REQUEST_CHECK_SETTINGS);

                            } catch (IntentSender.SendIntentException sie) {
                                Log.i(TAG, "PendingIntent unable to execute request.");
                            }

                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = getString(R.string.errMsg) + getString(R.string.fixIn);
                            Log.e(TAG, errorMessage);
                            DisplayViewUI.displayToast(getAppContext(), errorMessage);
                            mRequestingLocationUpdates = false;
                    }

                    //updateUI();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermissions()) {
            buildLocationSettingsRequest();
            startLocationUpdates();
        }

    }


}