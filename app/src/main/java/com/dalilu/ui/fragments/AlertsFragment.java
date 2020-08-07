package com.dalilu.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.MainActivity;
import com.dalilu.R;
import com.dalilu.adapters.LocationSharingAdapter;
import com.dalilu.databinding.FragmentAlertsBinding;
import com.dalilu.model.ShareLocation;
import com.dalilu.utils.DisplayViewUI;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nex3z.notificationbadge.NotificationBadge;

import java.text.MessageFormat;


public class AlertsFragment extends Fragment {

    FragmentAlertsBinding fragmentAlertsBinding;
    private DatabaseReference locationDbRef;
    private LocationSharingAdapter adapter;
    NotificationBadge notificationBadge;
    RelativeLayout relativeLayout;
    RecyclerView rv;
    TextView txtNotificationCount;
    ProgressBar pbLoading;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentAlertsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_alerts, container, false);

        return fragmentAlertsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();

        loadData();


    }

    void initViews() {
        txtNotificationCount = fragmentAlertsBinding.txtNotifCount;
        relativeLayout = fragmentAlertsBinding.badgeLayout;
        notificationBadge = fragmentAlertsBinding.notifBadge;
        notificationBadge.setAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.blinking_text));
        pbLoading = fragmentAlertsBinding.pbLoading;

    }

    private void loadData() {
        rv = fragmentAlertsBinding.alertRecyclerView;
        rv.setHasFixedSize(true);

        new Handler().postDelayed(() -> {

            pbLoading.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);

        }, 2000);

        locationDbRef = FirebaseDatabase.getInstance().getReference().child("Locations").child(MainActivity.userId);
        Query query = locationDbRef.orderByChild("timeStamp");

        locationDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.hasChildren()) {

                    txtNotificationCount.setVisibility(View.VISIBLE);
                    //notificationBadge.setVisibility(View.VISIBLE);

                    int numberOfItems = (int) snapshot.getChildrenCount();

                    if (numberOfItems == 0) {

                        txtNotificationCount.setVisibility(View.VISIBLE);
                        txtNotificationCount.setText(MessageFormat.format(getString(R.string.noNotifs), numberOfItems));
                        // notificationBadge.setText(String.valueOf(0));
                        // notificationBadge.setAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.blinking_text));


                    } else {
                        //  badgeLayout.setVisibility(View.VISIBLE);
                        // notificationBadge.setVisibility(View.VISIBLE);
                        //  notificationBadge.setAnimation(AnimationUtils.loadAnimation(requireContext(),R.anim.blinking_text));

                        //  txtNotificationCount.setText(MessageFormat.format(getString(R.string.notifs),numberOfItems));
                        notificationBadge.setText(String.valueOf(numberOfItems));
                        txtNotificationCount.setVisibility(View.GONE);
                        relativeLayout.setVisibility(View.VISIBLE);


                    }


                } else if (!snapshot.exists()) {
                    relativeLayout.setVisibility(View.GONE);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DisplayViewUI.displayToast(requireActivity(), error.getMessage());

            }
        });

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