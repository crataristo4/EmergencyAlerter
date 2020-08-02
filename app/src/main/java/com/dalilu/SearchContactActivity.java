package com.dalilu;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.adapters.ContactsAdapter;
import com.dalilu.databinding.ActivitySearchContactBinding;
import com.dalilu.model.RequestModel;
import com.dalilu.model.Users;
import com.dalilu.utils.DisplayViewUI;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class SearchContactActivity extends AppCompatActivity {
    private ActivitySearchContactBinding activitySearchContactBinding;
    private CollectionReference usersDbReF;
    private ContactsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySearchContactBinding = DataBindingUtil.setContentView(this, R.layout.activity_search_contact);

        RecyclerView rv = activitySearchContactBinding.recyclerViewContacts;
        rv.setHasFixedSize(true);
        usersDbReF = FirebaseFirestore.getInstance().collection("Users");

        activitySearchContactBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {


                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!s.isEmpty()) {

                    Query query = usersDbReF.orderBy("userName").whereEqualTo("userName", s);
                    FirestoreRecyclerOptions<Users> options =
                            new FirestoreRecyclerOptions.Builder<Users>().setQuery(query,
                                    Users.class).build();

                    rv.setLayoutManager(new LinearLayoutManager(SearchContactActivity.this));


                    adapter = new ContactsAdapter(options);
                    adapter.notifyDataSetChanged();
                    adapter.startListening();
                    rv.setAdapter(adapter);
                }

                adapter.setOnItemClickListener((v, position) -> {

                    DatabaseReference requestDbRef = FirebaseDatabase.getInstance().getReference("Friends");
                    String requestId = requestDbRef.push().getKey();
                    assert requestId != null;

                    //receiver details
                    String receiverName = adapter.getItem(position).getUserName();
                    String receiverPhotoUrl = adapter.getItem(position).getUserPhotoUrl();
                    String receiverId = adapter.getItem(position).getUserId();

                    //sender details
                    String senderName = MainActivity.userName;
                    String senderId = MainActivity.userId;
                    String senderPhotoUrl = MainActivity.userPhotoUrl;

                    String response = "pending";

                    ProgressDialog pd = DisplayViewUI.displayProgress(SearchContactActivity.this, getString(R.string.addingUser));


                    Log.i("Receiver : ", "name: " + receiverName + " url " + receiverPhotoUrl + " id: " + receiverId);
                    Log.i("Sender : ", "name: " + senderName + " url " + senderPhotoUrl + " id: " + senderId);

                    RequestModel sendRequest = new RequestModel(senderId, receiverId, response, senderPhotoUrl, senderName, receiverName, receiverPhotoUrl);
                    // child(receiverId).child(senderId)
                    requestDbRef.child(receiverId).child(senderId).setValue(sendRequest).addOnCompleteListener(task -> {
                        pd.show();
                        new Handler().postDelayed(() -> {
                            if (task.isSuccessful()) {
                                pd.dismiss();
                                DisplayViewUI.displayToast(SearchContactActivity.this, getString(R.string.successFull));
                                finish();

                            } else {
                                pd.dismiss();
                                DisplayViewUI.displayToast(SearchContactActivity.this, Objects.requireNonNull(task.getException()).getMessage());

                            }
                        }, 3000);

                    });

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
}