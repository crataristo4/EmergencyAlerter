package com.dalilu;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.adapters.ContactsAdapter;
import com.dalilu.databinding.ActivitySearchContactBinding;
import com.dalilu.model.RequestModel;
import com.dalilu.ui.bottomsheets.PopUpAlerter;
import com.dalilu.utils.AppConstants;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class SearchContactActivity extends AppCompatActivity {
    private ActivitySearchContactBinding activitySearchContactBinding;
    private CollectionReference usersDbReF;
    private ContactsAdapter adapter;
    ProgressBar progressBar;
    Button btnAdd;
    DatabaseReference requestDbRef;
    String receiverName;
    String receiverPhotoUrl;
    String receiverId;
    String receiverPhoneNumber;
    private long mLastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usersDbReF = FirebaseFirestore.getInstance().collection("Users");
        requestDbRef = FirebaseDatabase.getInstance().getReference("Friends");

        activitySearchContactBinding = DataBindingUtil.setContentView(this, R.layout.activity_search_contact);
        progressBar = activitySearchContactBinding.progressLoading;
        activitySearchContactBinding.imgBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        RecyclerView rv = activitySearchContactBinding.recyclerViewContacts;
        rv.setHasFixedSize(true);

        activitySearchContactBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!s.isEmpty()) {
                    progressBar.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> progressBar.setVisibility(View.GONE), 3000);

                    runOnUiThread(() -> {
                        Query query = usersDbReF.orderBy("userName").whereEqualTo("userName", s);
                        FirestoreRecyclerOptions<RequestModel> options =
                                new FirestoreRecyclerOptions.Builder<RequestModel>().setQuery(query,
                                        RequestModel.class).build();

                        rv.setLayoutManager(new LinearLayoutManager(SearchContactActivity.this));

                        adapter = new ContactsAdapter(options);
                        adapter.notifyDataSetChanged();
                        adapter.startListening();
                        rv.setAdapter(adapter);
                    });

                } else {
                    progressBar.setVisibility(View.GONE);

                }


                adapter.setOnItemClickListener((v, position) -> {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }

                    receiverId = adapter.getItem(position).getUserId();
                    receiverName = adapter.getItem(position).getUserName();
                    receiverPhotoUrl = adapter.getItem(position).getUserPhotoUrl();
                    receiverPhoneNumber = adapter.getItem(position).getPhoneNumber();

                    mLastClickTime = SystemClock.elapsedRealtime();
                    Bundle bundleFriendDetails = new Bundle();
                    bundleFriendDetails.putString(AppConstants.USER_NAME, receiverName);
                    bundleFriendDetails.putString(AppConstants.UID, receiverId);
                    bundleFriendDetails.putString(AppConstants.USER_PHOTO_URL, receiverPhotoUrl);
                    bundleFriendDetails.putString(AppConstants.PHONE_NUMBER, receiverPhoneNumber);

                    PopUpAlerter popUpAlerter = new PopUpAlerter();
                    popUpAlerter.setCancelable(false);
                    popUpAlerter.setArguments(bundleFriendDetails);
                    popUpAlerter.show(getSupportFragmentManager(), AppConstants.SEND_REQUEST_TAG);


                });

                return true;
            }
        });


    }


    @Override
    public void onStart() {
        super.onStart();
        //  adapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        // adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}