package com.dalilu.ui.fragments;

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
    private static final int INITIAL_LOAD = 15;
    private Bundle mBundleState;
    private FragmentHomeBinding fragmentHomeBinding;
    private RecyclerView recyclerView;
    private HomeRecyclerAdapter adapter;
    private ArrayList<AlertItems> arrayList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private Parcelable mState;
    private ListenerRegistration registration;
    private DocumentSnapshot mLastResult;
    ProgressBar pbHomeLoading;
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;
    private CollectionReference collectionReference = FirebaseFirestore
            .getInstance()
            .collection("Alerts");


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        return fragmentHomeBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        loadActivityData();
        requireActivity().runOnUiThread(this::fetchData);

    }


    private void loadActivityData() {
        recyclerView = fragmentHomeBinding.recyclerViewHome;
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        pbHomeLoading = fragmentHomeBinding.pbHomeLoading;

        new Handler().postDelayed(() -> {

            pbHomeLoading.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

        }, 2000);

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
                            userName, userPhotoUrl, url, timeStamp, address, id, dateReported));

                }
                //group data by Videos
                else if (ds.getData().containsKey("video")) {


                    arrayList.add(new AlertItems(AppConstants.VIDEO_TYPE,
                            userName,
                            url,
                            userPhotoUrl,
                            timeStamp,
                            address
                    ));
                }

                //group data by audios
               /* else if (ds.getData().containsKey("audios")) {
                    arrayList.add(new AlertItems(AppConstants.VIDEO_TYPE,
                            itemImage,
                            itemDescription,
                            userName,
                            userPhoto,
                            timeStamp,
                            id
                    ));
                }*/
            }

            adapter.notifyDataSetChanged();

            //get the last visible item
//            mLastResult = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);


          /*  //load more
            RecyclerView.OnScrollListener  listener = new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                        isScrolling = true;
                    }

                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    int visibleCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();

                    if (isScrolling && (firstVisibleItem + visibleCount == totalItemCount) && !isLastItemReached){

                        isScrolling = false;
                        Query queryNext =collectionReference.orderBy("timeStamp", Query.Direction.DESCENDING)
                                .startAfter(mLastResult).limit(INITIAL_LOAD);

                        registration =    queryNext.addSnapshotListener((queryDocumentSnapshots1, e1) -> {

                            if (e1 != null) {
                                Log.w(TAG, "Listen failed.", e1);
                                return;
                            }
                              arrayList.clear();
                            assert queryDocumentSnapshots1 != null;
                            for (QueryDocumentSnapshot ds : queryDocumentSnapshots1) {


                                ActivityItemModel itemModel = ds.toObject(ActivityItemModel.class);
                                //get data from model
                                String userName = itemModel.getUserName();
                                String userPhoto = itemModel.getUserPhoto();
                                String itemDescription = itemModel.getItemDescription();
                                String status = itemModel.getStatus();
                                String itemImage = itemModel.getItemImage();
                                long timeStamp = itemModel.getTimeStamp();
                                String id = ds.getId();
//group data by status
                                if (ds.getData().containsKey("status")) {
                                    Log.i(TAG, "status: " + ds.getData().get("status"));

                                    arrayList.add(new ActivityItemModel(ActivityItemModel.TEXT_TYPE,
                                            status,
                                            userName,
                                            userPhoto,
                                            timeStamp,
                                            id));

                                }
                                //group data by item description
                                else if (ds.getData().containsKey("itemDescription")) {
                                    arrayList.add(new ActivityItemModel(ActivityItemModel.IMAGE_TYPE,
                                            itemImage,
                                            itemDescription,
                                            userName,
                                            userPhoto,
                                            timeStamp,
                                            id
                                    ));
                                }
                            }


                            adapter.notifyDataSetChanged();
                            //get the last visible item
                            mLastResult = queryDocumentSnapshots1.getDocuments().get(queryDocumentSnapshots1.size() -1);

                            if (queryDocumentSnapshots1.getDocuments().size() < INITIAL_LOAD){

                                isLastItemReached = true;
                            }

                        });


                    }

                }
            };

            recyclerView.addOnScrollListener(listener);

*/
        });

       /* //get all items from fire store
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot ds : Objects.requireNonNull(task.getResult())) {
                    Log.i(TAG, "onComplete: " + ds.getId() + " " + ds.getData());

                    ActivityItemModel itemModel = ds.toObject(ActivityItemModel.class);

                    //group data by status
                    if (ds.getData().containsKey("status")) {
                        Log.i(TAG, "status: " + ds.getData().get("status"));

                        arrayList.add(new ActivityItemModel(ActivityItemModel.TEXT_TYPE,
                                itemModel.getStatus(),
                                itemModel.getUserName(),
                                itemModel.getUserPhoto(),
                                itemModel.getTimeStamp()));

                    }
                    //group data by item description
                    else if (ds.getData().containsKey("itemDescription")) {
                        Log.i(TAG, "itemDescription: " + ds.getData().get("itemDescription"));

                        arrayList.add(new ActivityItemModel(ActivityItemModel.IMAGE_TYPE,
                                itemModel.getItemImage(),
                                itemModel.getItemDescription(),
                                itemModel.getUserName(),
                                itemModel.getUserPhoto(),
                                itemModel.getTimeStamp()));
                    }

                }

                adapter.notifyDataSetChanged();

            }

        });*/
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