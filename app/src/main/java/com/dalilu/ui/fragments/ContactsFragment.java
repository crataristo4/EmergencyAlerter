package com.dalilu.ui.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.dalilu.MainActivity;
import com.dalilu.R;
import com.dalilu.adapters.FriendRequestAdapter;
import com.dalilu.databinding.FragmentContactsBinding;
import com.dalilu.model.RequestModel;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.dalilu.utils.GetTimeAgo;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";
    private static final int INITIAL_LOAD = 15;
    ProgressBar progressBar;
    private FragmentContactsBinding fragmentContactsBinding;
    RecyclerView rv;
    private CollectionReference friendsCollectionReference;
    private DatabaseReference locationDbRef, friendsDbRef;
    // private RequestAdapter adapter;
    private FriendRequestAdapter adapter;
    String receiverName;
    String receiverPhotoUrl;
    String receiverId;
    String receiverPhoneNumber;
    String id;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentContactsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false);
        return fragmentContactsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        // requireActivity().runOnUiThread(this::loadData);
        loadData();

        adapter.setOnItemClickListener((view1, position) -> {

            requireActivity().runOnUiThread(() -> {
                SharedPreferences pref = requireActivity().getSharedPreferences(AppConstants.PREFS, 0);

                ProgressDialog progressBar = DisplayViewUI.displayProgress(requireActivity(), getString(R.string.XCC));
                //send location
                //Send location details to user
                @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:MM a");
                String dateSent = dateFormat.format(Calendar.getInstance().getTime());

                String getUserName = adapter.getItem(position).getName();
                String getUserId = adapter.getItem(position).getId();
                String getUserPhoto = adapter.getItem(position).getPhoto();
                String name = MainActivity.userName, photo = MainActivity.userPhotoUrl, yourLocation = MainActivity.knownName, senderId = MainActivity.userId;

                DisplayViewUI.displayAlertDialog(requireActivity(),
                        getString(R.string.sndloc),
                        MessageFormat.format(getString(R.string.qst), getUserName), getString(R.string.yes), getString(R.string.no), (dialogInterface, i) -> {
                            if (i == -1) {
                                // progressBar.show();
                                String sharedLocation = name + " " + getString(R.string.shLoc) + " " + getString(R.string.withU);
                                String locationReceived = getString(R.string.urLoc) + " " + getString(R.string.isShared) + " " + getUserName;

                                //get location coordinates
                                String latitude = Double.toString(MainActivity.latitude);
                                String longitude = Double.toString(MainActivity.longitude);
                                String url = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + "&z=15";

                                //..location received from another user ..//
                                Map<String, Object> fromUser = new HashMap<>();
                                fromUser.put("location", locationReceived);
                                fromUser.put("knownName", yourLocation);
                                fromUser.put("url", url);
                                fromUser.put("date", dateSent);
                                fromUser.put("userName", name);
                                fromUser.put("photo", getUserPhoto);
                                fromUser.put("time", GetTimeAgo.getTimeInMillis());

                                //..location sent to ..(user who sent  will view this) //
                                Map<String, Object> toReceiver = new HashMap<>();
                                toReceiver.put("location", sharedLocation);
                                toReceiver.put("knownName", yourLocation);
                                toReceiver.put("url", url);
                                toReceiver.put("date", dateSent);
                                toReceiver.put("userName", getUserName);
                                toReceiver.put("time", GetTimeAgo.getTimeInMillis());
                                toReceiver.put("photo", photo);


                                String locationDbId = locationDbRef.push().getKey();
                                assert locationDbId != null;
                                locationDbRef.child(senderId).child(locationDbId).setValue(fromUser);
                                locationDbRef.child(getUserId).child(locationDbId).setValue(toReceiver);
                                DisplayViewUI.displayToast(requireActivity(), getString(R.string.successFull));

                                SharedPreferences.Editor shareLocationEditor = pref.edit();
                                shareLocationEditor.putBoolean(AppConstants.IS_LOCATION_SHARED, true);
                                shareLocationEditor.putString(AppConstants.USER_NAME, receiverName);
                                shareLocationEditor.putString(AppConstants.UID, receiverId);
                                shareLocationEditor.apply();


/*
                      locationDbRef.child(senderId).child(locationDbId).setValue(fromUser).addOnCompleteListener(task -> {
                          if (task.isSuccessful()) {
                              progressBar.dismiss();
                              DisplayViewUI.displayToast(requireActivity(), getString(R.string.successFull));
                              locationDbRef.child(getUserId).child(locationDbId).setValue(toReceiver);

                              SharedPreferences.Editor shareLocationEditor = pref.edit();
                              shareLocationEditor.putBoolean(AppConstants.IS_LOCATION_SHARED, true);
                              shareLocationEditor.apply();


                          } else {
                              progressBar.dismiss();
                              DisplayViewUI.displayToast(requireActivity(), Objects.requireNonNull(task.getException()).getMessage());

                          }
                      });
*/


                            } else if (i == -2) {
                                dialogInterface.dismiss();


                            }
                        });


            });

        });
        //loadContactsFromPhone();

/*

        fragmentContactsBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private void onClick(View view1, int position) {
                ProgressDialog progressBar = DisplayViewUI.displayProgress(requireActivity(), getString(R.string.XCC));
                //send location
                //Send location details to user
                @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:MM a");
                String dateSent = dateFormat.format(Calendar.getInstance().getTime());

                String getUserName = adapter.getItem(position).getUserName();
                String getUserId = adapter.getItem(position).getUserId();
                String getUserPhoto = adapter.getItem(position).getUserPhotoUrl();

                DisplayViewUI.displayAlertDialog(requireActivity(),
                        getString(R.string.sndloc),
                        MessageFormat.format(getString(R.string.qst), getUserName), getString(R.string.yes), getString(R.string.no), (dialogInterface, i) -> {
                            if (i == -1) {
                                progressBar.show();
                                String sharedLocation = name + " " + getString(R.string.shLoc) + " " + getString(R.string.withU);
                                String locationReceived = getString(R.string.urLoc) + " " + getString(R.string.isShared) + " " + getUserName;

                                //get location coordinates
                                String latitude = Double.toString(MainActivity.latitude);
                                String longitude = Double.toString(MainActivity.longitude);
                                String url = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + "&z=15";

                                //..location received from another user ..//
                                Map<String, Object> fromUser = new HashMap<>();
                                fromUser.put("location", locationReceived);
                                fromUser.put("knownName", yourLocation);
                                fromUser.put("url", url);
                                fromUser.put("date", dateSent);
                                fromUser.put("userName", name);
                                fromUser.put("photo", getUserPhoto);
                                fromUser.put("time", GetTimeAgo.getTimeInMillis());

                                //..location sent to ..(user who sent  will view this) //
                                Map<String, Object> toReceiver = new HashMap<>();
                                toReceiver.put("location", sharedLocation);
                                toReceiver.put("knownName", yourLocation);
                                toReceiver.put("url", url);
                                toReceiver.put("date", dateSent);
                                toReceiver.put("userName", getUserName);
                                toReceiver.put("time", GetTimeAgo.getTimeInMillis());
                                toReceiver.put("photo", photo);


                                String locationDbId = locationDbRef.push().getKey();
                                assert locationDbId != null;
                                locationDbRef.child(senderId).child(locationDbId).setValue(fromUser).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        progressBar.dismiss();
                                        DisplayViewUI.displayToast(requireActivity(), getString(R.string.successFull));
                                        locationDbRef.child(getUserId).child(locationDbId).setValue(toReceiver);


                                    } else {
                                        progressBar.dismiss();
                                        DisplayViewUI.displayToast(requireActivity(), Objects.requireNonNull(task.getException()).getMessage());

                                    }
                                });


                            } else if (i == -2) {
                                dialogInterface.dismiss();


                            }
                        });
            }

            @Override
            public boolean onQueryTextSubmit(String s) {


                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!s.isEmpty()) {


                    progressBar.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(() -> progressBar.setVisibility(View.GONE), 3000);


                    requireActivity().runOnUiThread(() -> {
                        Query query = friendsDbRef.orderByChild("name");

                        FirebaseRecyclerOptions<RequestModel> options =
                                new FirebaseRecyclerOptions.Builder<RequestModel>().setQuery(query,
                                        RequestModel.class).build();


                        adapter = new RequestAdapter(options);
                        adapter.notifyDataSetChanged();
                        adapter.startListening();
                        rv.setAdapter(adapter);

                    });

                }


                return true;
            }
        });

*/


    }

    void initViews() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mCurrentUser = mAuth.getCurrentUser();
        assert mCurrentUser != null;
        id = mCurrentUser.getUid();


        rv = fragmentContactsBinding.contactsRecyclerView;
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(requireActivity()));

        progressBar = fragmentContactsBinding.progressLoading;
        friendsCollectionReference = FirebaseFirestore.getInstance().collection("Friends");
        locationDbRef = FirebaseDatabase.getInstance().getReference("Locations");
        friendsDbRef = FirebaseDatabase.getInstance().getReference("Friends");


    }

    private void loadData() {

        requireActivity().runOnUiThread(() -> {
            Query query = friendsCollectionReference.document(id).collection(id).orderBy("name", Query.Direction.ASCENDING);
            FirestoreRecyclerOptions<RequestModel> options =
                    new FirestoreRecyclerOptions.Builder<RequestModel>().setQuery(query,
                            RequestModel.class).build();

            adapter = new FriendRequestAdapter(options);
            rv.setAdapter(adapter);

           /* adapter.setOnItemClickListener((view1, position) -> {
                receiverId = adapter.getItem(position).getId();
                receiverName = adapter.getItem(position).getUserName();
                receiverPhotoUrl = adapter.getItem(position).getUserPhotoUrl();
                receiverPhoneNumber = adapter.getItem(position).getPhoneNumber();

                //todo send location to user


            });*/
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