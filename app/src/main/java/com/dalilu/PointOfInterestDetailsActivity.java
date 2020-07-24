package com.dalilu;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.dalilu.services.PointOfInterest;
import com.dalilu.ui.fragments.DetailsFragment;

public class PointOfInterestDetailsActivity extends AppCompatActivity {

    private ImageView imageView;
    private PointOfInterest poi;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //Get the fragment from the saved stated if possible
        if (savedInstanceState != null) {
            fragment = getSupportFragmentManager().getFragment(savedInstanceState, "fragment");
            fragmentTransaction.replace(R.id.details_fragment, fragment);
        } else {
            //Make a new one if there is no saved fragment
            PointOfInterest poi = getIntent().getParcelableExtra("poi");
            Log.d("details", String.valueOf(poi == null));
            Bundle bundle = new Bundle();
            bundle.putParcelable("poi", poi);
            fragment = new DetailsFragment();
            fragment.setArguments(bundle);
            fragmentTransaction.add(R.id.details_fragment, fragment);
        }
        fragmentTransaction.commit();


        setContentView(R.layout.activity_point_of_interest_details);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment
        getSupportFragmentManager().putFragment(outState, "fragment", fragment);
    }
}
