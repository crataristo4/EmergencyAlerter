package com.dalilu.ui.bottomsheets;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.dalilu.R;
import com.dalilu.databinding.UserlocationBinding;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.GetTimeAgo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

public class UserLocation extends BottomSheetDialogFragment implements OnMapReadyCallback {

    UserlocationBinding userlocationBinding;
    String name, location, photoUrl;
    long timeStamp;
    double lat, lng;
    private float mapZoomLevel;
    private GoogleMap map;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        userlocationBinding = DataBindingUtil.inflate(inflater, R.layout.userlocation, container, false);

        return userlocationBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        Bundle intent = getArguments();
        if (intent != null) {

            name = intent.getString(AppConstants.USER_NAME);
            location = intent.getString(AppConstants.KNOWN_LOCATION);
            photoUrl = intent.getString(AppConstants.USER_PHOTO_URL);
            timeStamp = intent.getLong(AppConstants.TIMESTAMP, 0);
            lat = intent.getDouble(AppConstants.LATITUDE, 0);
            lng = intent.getDouble(AppConstants.LONGITUDE, 0);

            userlocationBinding.txtName.setText(name);
            userlocationBinding.txtLocation.setText(location);
            userlocationBinding.txtTime.setText(GetTimeAgo.getTimeAgo(timeStamp));

            Glide.with(this)
                    .load(photoUrl)
                    .error(ContextCompat.getDrawable(requireContext(), R.drawable.boy))
                    .into(userlocationBinding.imageView);

            mapZoomLevel = 13;
        }

        userlocationBinding.btnCancel.setOnClickListener(view1 -> dismiss());


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            requireContext(), R.raw.custommap));

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            // map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setZoomGesturesEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.getUiSettings().setRotateGesturesEnabled(true);


            LatLng latLng = new LatLng(lat, lng);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mapZoomLevel));

            map.setOnCameraMoveListener(() -> mapZoomLevel = map.getCameraPosition().zoom);
            //  googleMap.clear();
            if (name.equals("You")) {
                map.addMarker(new MarkerOptions()
                        .title("Your last location shared")
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


    }
}
