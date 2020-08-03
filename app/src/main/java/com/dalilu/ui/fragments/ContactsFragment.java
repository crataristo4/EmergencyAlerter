package com.dalilu.ui.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
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
import com.dalilu.adapters.ContactsAdapter;
import com.dalilu.databinding.FragmentContactsBinding;
import com.dalilu.model.Users;
import com.dalilu.utils.DisplayViewUI;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
    private DatabaseReference locationDbRef;
    private CollectionReference usersDbReF;
    private ContactsAdapter adapter;

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
        String name = MainActivity.userName;
        String photo = MainActivity.userPhotoUrl;


        progressBar = fragmentContactsBinding.progressLoading;
        usersDbReF = FirebaseFirestore.getInstance().collection("Users");
        locationDbRef = FirebaseDatabase.getInstance().getReference("Locations");


        // Query query = usersDbReF.orderByChild("senderId").equalTo(senderId);
       /* Query query = usersDbReF.orderBy("userName", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Users> options =
                new FirestoreRecyclerOptions.Builder<Users>().setQuery(query,
                        Users.class).build();

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ContactsAdapter(options);
        rv.setAdapter(adapter);*/


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


                                    //get users location
                                    String yourLocation = MainActivity.knownName;

                                    String sharedLocation = name + " " + getString(R.string.shLoc) + " : " + yourLocation + getString(R.string.withU) + dateSent;
                                    String locationReceived = getString(R.string.urLoc) + yourLocation + getString(R.string.isShared) + getUserName + getString(R.string.on) + dateSent;

                                    //..location received from another user ..//
                                    Map<String, Object> fromUser = new HashMap<>();
                                    /*fromUser.put("locationName", yourLocation);
                                    fromUser.put("date", dateSent);
                                    fromUser.put("from", name);
                                    fromUser.put("to", getUserName);
                                    fromUser.put("userPhoto", photo);*/
                                    fromUser.put("location", sharedLocation);

                                    //..location sent to ..(user who sent  will view this) //
                                    Map<String, Object> toReceiver = new HashMap<>();
                                    /*toReceiver.put("locationName", yourLocation);
                                    toReceiver.put("date", dateSent);
                                    toReceiver.put("to", getUserName);
                                    toReceiver.put("from", name);
                                    toReceiver.put("userPhoto", photo);*/
                                    toReceiver.put("location", locationReceived);


                                    String locationDbId = locationDbRef.push().getKey();
                                    assert locationDbId != null;
                                    locationDbRef.child(senderId).child(locationDbId).setValue(fromUser).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            progressBar.dismiss();
                                            DisplayViewUI.displayToast(requireActivity(), getString(R.string.successFull));

                                            locationDbRef.child(getUserId).child(locationDbId).setValue(toReceiver);


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
                    RecyclerView rv = fragmentContactsBinding.contactsRecyclerView;
                    rv.setHasFixedSize(true);


                    rv.setLayoutManager(new LinearLayoutManager(requireActivity()));


                    progressBar.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(() -> progressBar.setVisibility(View.GONE), 3000);


                    requireActivity().runOnUiThread(() -> {
                        Query query = usersDbReF.orderBy("userName").whereEqualTo("userName", s);

                        FirestoreRecyclerOptions<Users> options =
                                new FirestoreRecyclerOptions.Builder<Users>().setQuery(query,
                                        Users.class).build();


                        adapter = new ContactsAdapter(options);
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


    }

    @Override
    public void onStart() {
        super.onStart();
//        adapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        //  adapter.stopListening();
    }
}