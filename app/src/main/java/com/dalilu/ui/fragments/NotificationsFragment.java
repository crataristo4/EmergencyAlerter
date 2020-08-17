package com.dalilu.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.MainActivity;
import com.dalilu.R;
import com.dalilu.ViewUserLocationActivity;
import com.dalilu.adapters.LocationSharingAdapter;
import com.dalilu.databinding.FragmentNotificationsBinding;
import com.dalilu.model.ShareLocation;
import com.dalilu.utils.AppConstants;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class NotificationsFragment extends Fragment {

    FragmentNotificationsBinding fragmentNotificationsBinding;
    private DatabaseReference locationDbRef;
    String id;
    private LocationSharingAdapter adapter;
    RecyclerView rv;
    Query query;
    private CollectionReference mCollectionReference;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentNotificationsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_notifications, container, false);

        return fragmentNotificationsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        loadData();


    }

    void initViews() {
        id = MainActivity.userId;
        rv = fragmentNotificationsBinding.alertRecyclerView;
        rv.setHasFixedSize(true);

        //locationDbRef = FirebaseDatabase.getInstance().getReference().child("Locations").child(MainActivity.userId);
        mCollectionReference = FirebaseFirestore.getInstance().collection("Locations");


    }

    private void loadData() {

       /* FirebaseRecyclerOptions<ShareLocation> options =
                new FirebaseRecyclerOptions.Builder<ShareLocation>().setQuery(query,
                        ShareLocation.class).build();*/

        requireActivity().runOnUiThread(() -> {
            query = mCollectionReference.document(id).collection(id).orderBy("timeStamp");
            FirestoreRecyclerOptions<ShareLocation> options =
                    new FirestoreRecyclerOptions.Builder<ShareLocation>().setQuery(query,
                            ShareLocation.class).build();

            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new LocationSharingAdapter(options);
            rv.setAdapter(adapter);

            adapter.setOnLocationItemClick((view, position) -> {
                String name = adapter.getItem(position).getUserName();
                long timeStamp = adapter.getItem(position).getTimeStamp();
                String knownLocation = adapter.getItem(position).getKnownName();
                String userPhoto = adapter.getItem(position).getPhoto();
                double lat = adapter.getItem(position).getLatitude();
                double lng = adapter.getItem(position).getLongitude();

                Intent viewUserData = new Intent(requireContext(), ViewUserLocationActivity.class);
                viewUserData.putExtra(AppConstants.USER_NAME, name);
                viewUserData.putExtra(AppConstants.KNOWN_LOCATION, knownLocation);
                viewUserData.putExtra(AppConstants.USER_PHOTO_URL, userPhoto);
                viewUserData.putExtra(AppConstants.TIMESTAMP, timeStamp);
                viewUserData.putExtra(AppConstants.LATITUDE, lat);
                viewUserData.putExtra(AppConstants.LONGITUDE, lng);

                startActivity(viewUserData);
            });


        });

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}