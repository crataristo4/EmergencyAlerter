package com.dalilu.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dalilu.R;
import com.dalilu.databinding.ActivityMainBinding;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import jahirfiquitiva.libs.fabsmenu.FABsMenu;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    public static String userName, userPhotoUrl;
    private ActivityMainBinding activityMainBinding;
    private CollectionReference alertsCollectionReference, locationCollectionDbRef;
    FABsMenu faBsMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

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
        faBsMenu = activityMainBinding.fabsMenu;
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

        activityMainBinding.searchContact.setOnClickListener(view -> startActivity(new Intent(view.getContext(), SearchContactActivity.class)));

        activityMainBinding.logOut.setOnClickListener(view -> DisplayViewUI.displayAlertDialog(view.getContext(),
                getString(R.string.logOut), getString(R.string.xcvv),
                getString(R.string.logMeOut), getString(R.string.cancel),
                (dialogInterface, i) -> {
                    if (i == -1) {

                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(view.getContext(), SplashScreenActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    } else if (i == -2) {
                        dialogInterface.dismiss();
                    }


                }));

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

        activityMainBinding.report.setOnClickListener(v -> myIntent(ReportActivity.class));

        activityMainBinding.editProfile.setOnClickListener(view -> myIntent(EditProfileActivity.class));


    }


    void myIntent(@NonNull Class ctx) {

        Intent intent = new Intent(this, ctx);
        intent.putExtra(AppConstants.PHONE_NUMBER, phoneNumber);
        intent.putExtra(AppConstants.USER_PHOTO_URL, userPhotoUrl);
        intent.putExtra(AppConstants.USER_NAME, userName);
        intent.putExtra(AppConstants.UID, userId);
      /*  intent.putExtra(AppConstants.STATE, BaseActivity.state);
        intent.putExtra(AppConstants.COUNTRY, BaseActivity.country);
        intent.putExtra(AppConstants.KNOWN_LOCATION, BaseActivity.knownName);
        intent.putExtra(AppConstants.LATITUDE, BaseActivity.latitude);
        intent.putExtra(AppConstants.LONGITUDE, BaseActivity.longitude);*/

        startActivity(intent);
        finish();


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (faBsMenu.isExpanded()) {
            faBsMenu.collapse();
        }


    }


}