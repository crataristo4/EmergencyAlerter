package com.dalilu;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dalilu.adapters.CommentsAdapter;
import com.dalilu.model.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class CommentsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private String getAlertItemId, getAlertPhotoUrl, getAddress, getDatePosted;
    private LinearLayoutManager layoutManager;
    private CommentsAdapter adapter;
    private DatabaseReference databaseReference;
    private EmojiconEditText emojiconEditText;
    private ArrayList<Message> commentList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Toolbar commentToolBar = findViewById(R.id.commentsToolBar);
        setSupportActionBar(commentToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        initViews();
        loadData();

    }

    void initViews() {
        Intent getCommentsIntent = getIntent();
        if (getCommentsIntent != null) {

            getAlertItemId = getCommentsIntent.getStringExtra("alertItemId");
            getAddress = getCommentsIntent.getStringExtra("address");
            getDatePosted = getCommentsIntent.getStringExtra("datePosted");
            getAlertPhotoUrl = getCommentsIntent.getStringExtra("alertPhotoUrl");
        }

        ConstraintLayout activity_comment = findViewById(R.id.activity_comment);
        ImageView emojiButton = findViewById(R.id.emoticonButton);
        emojiconEditText = findViewById(R.id.emoticonEditTxt);
        EmojIconActions emojIconActions = new EmojIconActions(getApplicationContext(), activity_comment, emojiButton, emojiconEditText);
        emojIconActions.ShowEmojicon();


        TextView txtItemDes = findViewById(R.id.txtItemDescription);
        TextView txtDatePosted = findViewById(R.id.txtDatePosted);
        ImageView imgItemImage = findViewById(R.id.imgItemImage);

        txtItemDes.setText(MessageFormat.format("{0}{1}", getString(R.string.loc), getAddress));
        txtDatePosted.setText(MessageFormat.format("{0}{1}", getString(R.string.dt), getDatePosted));
        Glide.with(this)
                .load(getAlertPhotoUrl).thumbnail(0.5f)
                .centerCrop()
                .into(imgItemImage);

        findViewById(R.id.btnComment).setOnClickListener(v -> {
            addComment();
        });

        databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("Comments").child(getAlertItemId);
        databaseReference.keepSynced(true);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        commentList = new ArrayList<>();

        adapter = new CommentsAdapter(commentList);
        recyclerView.setAdapter(adapter);


    }

    private void loadData() {
        Query query = databaseReference.orderByChild("timeStamp");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        Message commentsList = ds.getValue(Message.class);

                        commentList.add(commentsList);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void addComment() {
        String postComment = emojiconEditText.getText().toString();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM HH:mm", Locale.ENGLISH);
        String name = MainActivity.fullName;

        String dateTime = simpleDateFormat.format(calendar.getTime());
        if (!postComment.trim().isEmpty()) {
            HashMap<String, Object> comments = new HashMap<>();
            comments.put("message", postComment);
            comments.put("senderName", name);
            comments.put("messageDateTime", dateTime);

            String randomId = databaseReference.push().getKey();
            assert randomId != null;
            databaseReference.child(randomId).setValue(comments);

            emojiconEditText.getText().clear();

        } else if (postComment.trim().isEmpty()) {
            emojiconEditText.setError("Cannot send empty message");
        }


    }


    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

}