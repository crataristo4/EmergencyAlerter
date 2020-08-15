package com.dalilu.ui.fragments;

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
import com.dalilu.adapters.LocationSharingAdapter;
import com.dalilu.databinding.FragmentNotificationsBinding;
import com.dalilu.model.ShareLocation;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class NotificationsFragment extends Fragment {

    FragmentNotificationsBinding fragmentNotificationsBinding;
    private DatabaseReference locationDbRef;
    private LocationSharingAdapter adapter;
    RecyclerView rv;
    Query query;


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

        rv = fragmentNotificationsBinding.alertRecyclerView;
        rv.setHasFixedSize(true);

        locationDbRef = FirebaseDatabase.getInstance().getReference().child("Locations").child(MainActivity.userId);
        query = locationDbRef.orderByChild("timeStamp");
    }

    private void loadData() {

        FirebaseRecyclerOptions<ShareLocation> options =
                new FirebaseRecyclerOptions.Builder<ShareLocation>().setQuery(query,
                        ShareLocation.class).build();

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new LocationSharingAdapter(options);
        rv.setAdapter(adapter);
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