package com.dalilu.ui.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.MainActivity;
import com.dalilu.R;
import com.dalilu.adapters.RequestAdapter;
import com.dalilu.databinding.FragmentContactsBinding;
import com.dalilu.model.RequestModel;
import com.dalilu.utils.DisplayViewUI;
import com.dalilu.utils.GetTimeAgo;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";
    private static final int INITIAL_LOAD = 15;
    ProgressBar progressBar;
    private FragmentContactsBinding fragmentContactsBinding;
    RecyclerView rv;
    private CollectionReference usersDbReF;
    private DatabaseReference locationDbRef, friendsDbRef;
    private RequestAdapter adapter;
    String receiverName;
    String receiverPhotoUrl;
    String receiverId;
    String receiverPhoneNumber;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentContactsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false);
        return fragmentContactsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //current users details
        String senderId = MainActivity.userId;
        Log.i(TAG, "onViewCreated: " + senderId);
        String name = MainActivity.userName;
        String photo = MainActivity.userPhotoUrl;
        String yourLocation = MainActivity.knownName;

        ProgressDialog loading = DisplayViewUI.displayProgress(requireActivity(), getString(R.string.addingUser));


        rv = fragmentContactsBinding.contactsRecyclerView;
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(requireActivity()));

        progressBar = fragmentContactsBinding.progressLoading;
        usersDbReF = FirebaseFirestore.getInstance().collection("Users");
        locationDbRef = FirebaseDatabase.getInstance().getReference("Locations");
        friendsDbRef = FirebaseDatabase.getInstance().getReference("Friends");

        Query query = friendsDbRef.child(senderId).orderByKey();

        FirebaseRecyclerOptions<RequestModel> options =
                new FirebaseRecyclerOptions.Builder<RequestModel>().setQuery(query,
                        RequestModel.class).build();

        adapter = new RequestAdapter(options);
        rv.setAdapter(adapter);

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
                        MessageFormat.format(getString(R.string.qst), getUserName), getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
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

                    adapter.setOnItemClickListener(//send location
//Send location details to user
//get users location
//..location received from another user ..//
//..location sent to ..(user who sent  will view this) //
                            this::onClick);


                } else {
                    progressBar.setVisibility(View.GONE);

                }


                return true;
            }
        });


        //adding users...

        adapter.setOnItemClickListener((view12, position) -> {

            loading.show();

            receiverId = adapter.getItem(position).getId();
            receiverName = adapter.getItem(position).getUserName();
            receiverPhotoUrl = adapter.getItem(position).getUserPhotoUrl();
            receiverPhoneNumber = adapter.getItem(position).getPhoneNumber();

            Log.i(TAG, "onViewCreated: " + receiverId);


            //sender
            Map<String, Object> updateResponse = new HashMap<>();
            // to.put("id", receiverId);
            updateResponse.put("response", "friends");

            friendsDbRef.child(receiverId).child(senderId).updateChildren(updateResponse).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    friendsDbRef.child(senderId).child(receiverId).updateChildren(updateResponse).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                loading.dismiss();
                            }

                        }
                    });

                } else {
                    DisplayViewUI.displayToast(requireContext(), Objects.requireNonNull(task.getException()).getMessage());
                }

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