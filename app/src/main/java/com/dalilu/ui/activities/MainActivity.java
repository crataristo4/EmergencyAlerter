package com.dalilu.ui.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dalilu.R;
import com.dalilu.databinding.ActivityMainBinding;
import com.dalilu.services.LocationUpdatesService;
import com.dalilu.services.Utils;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MainActivity";
    public static String userName, userPhotoUrl;
    private ActivityMainBinding activityMainBinding;
    private CollectionReference alertsCollectionReference, locationCollectionDbRef;


    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    /**
     * Time when the location was updated represented as a String.
     */
    public static String knownName, state, country, phoneNumber, userId;
    public static double latitude, longitude;
    public static String address;
    public static Geocoder geocoder;
    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        myReceiver = new MyReceiver();
        geocoder = new Geocoder(this, Locale.getDefault());

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        alertsCollectionReference = FirebaseFirestore.getInstance().collection("Alerts");
        locationCollectionDbRef = FirebaseFirestore.getInstance().collection("Locations");

        Intent getUserDetailsIntent = getIntent();
        if (getUserDetailsIntent != null) {
            userName = getUserDetailsIntent.getStringExtra(AppConstants.USER_NAME);
            userPhotoUrl = getUserDetailsIntent.getStringExtra(AppConstants.USER_PHOTO_URL);
            userId = getUserDetailsIntent.getStringExtra(AppConstants.UID);
            phoneNumber = getUserDetailsIntent.getStringExtra(AppConstants.PHONE_NUMBER);


        }

        initViews();

    }

    private void initViews() {
       // faBsMenu = activityMainBinding.fabsMenu;
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
        badgeDrawableNotification.setBackgroundColor(getResources().getColor(R.color.amber));
        BadgeDrawable badgeDrawableFriends = navView.getOrCreateBadge(menuItemFriends.getItemId());
        badgeDrawableFriends.setBackgroundColor(getResources().getColor(R.color.black));


        locationCollectionDbRef.document(userId).collection(userId).get().addOnCompleteListener(task -> {
            int numberOfItems = task.getResult().size();
            if (numberOfItems > 0)
                badgeDrawableNotification.setNumber(numberOfItems);
        });

        runOnUiThread(() -> alertsCollectionReference
                .whereEqualTo("isSolved", false)
                .get().addOnCompleteListener(task -> {

                    if (task.getResult().size() > 0)
                        badgeDrawableHome.setNumber(task.getResult().size());

                }));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_report:
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                }
                // Restore the state of the buttons when the activity (re)launches.
                setButtonsState(Utils.requestingLocationUpdates(this));

                // Bind to the service. If the service is in foreground mode, this signals to the service
                // that since this activity is in the foreground, the service can exit foreground mode.
                bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                        Context.BIND_AUTO_CREATE);


                DisplayViewUI.displayAlertDialogMsg(this, "Make a report", "Are you sure you want to make a report?",
                        "No", "Yes", (dialogInterface, i) -> {

                            if (i == -2) {
                                dialogInterface.dismiss();
                            } else {
                                ProgressDialog loading = DisplayViewUI.displayProgress(MainActivity.this, "");
                                loading.show();
                                new Handler().postDelayed(() -> {
                                    loading.dismiss();
                                    myIntent(ReportActivity.class);

                                }, 3000);
                            }
                        });
                break;
            case R.id.menu_search:
                startActivity(new Intent(this, SearchContactActivity.class));
                break;
            case R.id.menu_edit_profile:
                myIntent(EditProfileActivity.class);

                break;

            case R.id.menu_logout:
                DisplayViewUI.displayAlertDialog(this,
                        getString(R.string.logOut), getString(R.string.xcvv),
                        getString(R.string.logMeOut), getString(R.string.cancel),
                        (dialogInterface, i) -> {
                            if (i == -1) {

                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(this, SplashScreenActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                finish();
                            } else if (i == -2) {
                                dialogInterface.dismiss();
                            }


                        });

                break;


        }


        return true;
    }

    void myIntent(@NonNull Class ctx) {

        Intent intent = new Intent(this, ctx);
        intent.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
        intent.putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl);
        intent.putExtra(AppConstants.USER_NAME, userName);
        intent.putExtra(AppConstants.UID, userId);

        startActivity(intent);
        finish();


    }


    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this));


        if (!checkPermissions()) {
            requestPermissions();
        } else {
//            mService.requestLocationUpdates();
        }

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));


    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();

    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mService.requestLocationUpdates();

            } else {

                requestPermissions();


            }
        }
    }


    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
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
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    void getAddress(@NonNull Location mLocation) {

        try {
            List<Address> addressList = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);

            if (addressList != null) {
                address = addressList.get(0).getAddressLine(0);
                state = addressList.get(0).getAdminArea();
                country = addressList.get(0).getCountryName();
                knownName = addressList.get(0).getFeatureName();
                latitude = mLocation.getLatitude();
                longitude = mLocation.getLongitude();

                /*Toast.makeText(this, "Address--" + address
                                + " state--" + state + " country--" + country + " known name--" + knownName
                                + " Lat--" + latitude + " lng--" + longitude
                        , Toast.LENGTH_LONG).show();*/

                Log.i(TAG, String.format(Locale.ENGLISH, "%s: %f", "lat",
                        mLocation.getLatitude()));
                Log.i(TAG, String.format(Locale.ENGLISH, "%s: %f", "lng",
                        mLocation.getLongitude()));
                Log.i(TAG, String.format(Locale.ENGLISH, "%s: %s",
                        "Known Name", knownName));

                Log.i(TAG, String.format(Locale.ENGLISH, "%s: %s",
                        "address ", address));
                Log.i(TAG, String.format(Locale.ENGLISH, "%s: %s",
                        "state ", state));
                Log.i(TAG, String.format(Locale.ENGLISH, "%s: %s",
                        "country ", country));


            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {

            Log.i(TAG, "true: ");
        } else {

            Log.i(TAG, "false: ");
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                runOnUiThread(() -> {

                    getAddress(location);

                });
               /* Toast.makeText(MainActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();*/
            }
        }
    }


}