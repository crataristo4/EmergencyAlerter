package com.dalilu.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.R;
import com.dalilu.adapters.HomeRecyclerAdapter;
import com.dalilu.databinding.FragmentHomeBinding;
import com.dalilu.model.AlertItems;
import com.dalilu.utils.AppConstants;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private static final String KEY = "key";
    private static final String TAG = "HomeFragment";
    private static final int INITIAL_LOAD = 50;
    private Bundle mBundleState;
    private FragmentHomeBinding fragmentHomeBinding;
    private RecyclerView recyclerView;
    private HomeRecyclerAdapter adapter;
    private final ArrayList<AlertItems> arrayList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private Parcelable mState;
    private DocumentSnapshot mLastResult;
    ProgressBar pbHomeLoading;
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;
    private final CollectionReference collectionReference = FirebaseFirestore
            .getInstance()
            .collection("Alerts");
    ListenerRegistration registration;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        return fragmentHomeBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        requireActivity().runOnUiThread(this::fetchData);

    }


    private void initViews() {

        recyclerView = fragmentHomeBinding.recyclerViewHome;
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new HomeRecyclerAdapter(arrayList, getContext());
        recyclerView.setAdapter(adapter);


    }

    private void fetchData() {

        // Create a query against the collection.
        Query query = collectionReference.orderBy("timeStamp", Query.Direction.DESCENDING).limit(INITIAL_LOAD);

        registration = query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            // arrayList.clear();
            assert queryDocumentSnapshots != null;
            for (QueryDocumentSnapshot ds : queryDocumentSnapshots) {

                AlertItems alertItems = ds.toObject(AlertItems.class);
                //get data from model
                String address = alertItems.getAddress();
                String userName = alertItems.getUserName();
                String phoneNumber = alertItems.getPhoneNumber();
                long timeStamp = alertItems.getTimeStamp();
                String userPhotoUrl = alertItems.getUserPhotoUrl();
                String url = alertItems.getUrl();

                String id = ds.getId();
                String dateReported = alertItems.getDateReported();
//group data by images
                if (ds.getData().containsKey("image")) {


                    arrayList.add(new AlertItems(AppConstants.IMAGE_TYPE,
                            userName,
                            userPhotoUrl,
                            url,
                            timeStamp,
                            address,
                            id, dateReported));

                }
                //group data by Videos
                else if (ds.getData().containsKey("video")) {

                    arrayList.add(new AlertItems(AppConstants.VIDEO_TYPE,
                            id,
                            userName,
                            url,
                            userPhotoUrl,
                            timeStamp,
                            address
                    ));
                }

            }

            adapter.notifyDataSetChanged();

        });


    }

    @Override
    public void onStop() {
        super.onStop();
        //activityItemAdapter.stopListening();
        registration.remove();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBundleState = new Bundle();
        mState = layoutManager.onSaveInstanceState();
        mBundleState.putParcelable(KEY, mState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBundleState != null) {

            new Handler().postDelayed(() -> {

                mState = mBundleState.getParcelable(KEY);
                layoutManager.onRestoreInstanceState(mState);
            }, 50);
        }

        recyclerView.setLayoutManager(layoutManager);
    }


}