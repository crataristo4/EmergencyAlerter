package com.dalilu;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dalilu.adapters.CommentsAdapter;
import com.dalilu.model.Message;
import com.dalilu.utils.DisplayViewUI;
import com.dalilu.utils.FileUtils;
import com.dalilu.utils.GetTimeAgo;
import com.dalilu.utils.PermissionUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
    private static final String[] WRITE_EXTERNAL_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] RECORD_AUDIO_PERMISSION = new String[]{Manifest.permission.RECORD_AUDIO};
    private static final int RECORD_AUDIO = 3;
    String randomId;
    String name = MainActivity.userName;
    String dateTime;
    String userId = MainActivity.userId;
    private MediaRecorder mediaRecorder;
    private boolean recording;
    private String recordPath;
    private String recordUrl;
    private ImageView btnRecord;
    private ProgressDialog pd;
    private StorageReference audioFilePath, filePath;

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
        audioFilePath = FirebaseStorage.getInstance().getReference().child("audio");
        filePath = audioFilePath.child(UUID.randomUUID().toString());

        ConstraintLayout activity_comment = findViewById(R.id.activity_comment);
        ImageView emojiButton = findViewById(R.id.emoticonButton);
        emojiconEditText = findViewById(R.id.emoticonEditTxt);
        EmojIconActions emojIconActions = new EmojIconActions(getApplicationContext(), activity_comment, emojiButton, emojiconEditText);
        emojIconActions.ShowEmojicon();

        recording = false;
        btnRecord = findViewById(R.id.imgRecordAudio);
        btnRecord.setOnClickListener(v -> voiceRecordingAction());
        pd = DisplayViewUI.displayProgress(this, getResources().getString(R.string.uploadingPleaseWait));

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

        randomId = databaseReference.push().getKey();
        assert randomId != null;

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        commentList = new ArrayList<>();

        adapter = new CommentsAdapter(commentList);
        recyclerView.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM HH:mm", Locale.ENGLISH);
        dateTime = simpleDateFormat.format(calendar.getTime());


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

        if (!postComment.trim().isEmpty()) {
            HashMap<String, Object> comments = new HashMap<>();
            comments.put("message", postComment);
            comments.put("userName", name);
            comments.put("messageDateTime", dateTime);
            comments.put("type", "text");

            databaseReference.child(randomId).setValue(comments);

            emojiconEditText.getText().clear();

        } else if (postComment.trim().isEmpty()) {
            emojiconEditText.setError("Cannot send empty message");
        }


    }

    private void initializeMediaRecord() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
    }


    private void voiceRecordingAction() {

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!PermissionUtils.hasAudioRecordPermission(this)) {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_PERMISSION[0], RECORD_AUDIO_PERMISSION[0]}, RECORD_AUDIO);
            }
        }

        if (!isAndroidVersionNew || PermissionUtils.hasAudioRecordPermission(this)
                || PermissionUtils.hasWritePermission(this)) {

            if (!recording) {
                DisplayViewUI.displayToast(CommentsActivity.this, getString(R.string.stdVoiceRecordind));
                emojiconEditText.setHint(R.string.rcAud);
                emojiconEditText.setHintTextColor(getResources().getColor(R.color.colorRed));
                emojiconEditText.setEnabled(false);
                btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_black_24px));


                initializeMediaRecord();
                startRecordingAudio();
            } else {
                // DisplayViewUI.displayToast(CommentsActivity.this, getString(R.string.stpdVoiceRecording));
                emojiconEditText.setHint(getResources().getString(R.string.type_your_comments_here));
                emojiconEditText.setHintTextColor(getResources().getColor(R.color.black));
                emojiconEditText.setEnabled(true);
                btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_keyboard_voice_24));
                stopRecordingAudio();
                uploadAudioRecording(Uri.fromFile(new File(recordPath)), "audio");
            }
            recording = !recording;
        }
    }

    private void uploadAudioRecording(Uri uri, String type) {
        pd.show();
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:MM a");
        String dateReported = dateFormat.format(Calendar.getInstance().getTime());

        //upload audio to server
        filePath.putFile(uri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                pd.dismiss();

            }
            return filePath.getDownloadUrl();

        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {


                Uri downLoadUri = task.getResult();
                assert downLoadUri != null;
                String url = downLoadUri.toString();

                Map<String, Object> uploadAudio = new HashMap<>();
                uploadAudio.put("userName", name);
                uploadAudio.put("url", url);
                uploadAudio.put("type", type);
                uploadAudio.put("timeStamp", GetTimeAgo.getTimeInMillis());
                uploadAudio.put("messageDateTime", dateTime);

                databaseReference.child(randomId).setValue(uploadAudio).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            pd.dismiss();
                            DisplayViewUI.displayToast(CommentsActivity.this, getString(R.string.successFull));
                        } else {
                            pd.dismiss();
                            DisplayViewUI.displayToast(CommentsActivity.this, Objects.requireNonNull(task.getException()).getMessage());


                        }
                    }
                });


            } else {
                pd.dismiss();
                DisplayViewUI.displayToast(CommentsActivity.this, Objects.requireNonNull(task.getException()).getMessage());

            }

        });


    }

    private void startRecordingAudio() {
        File audioFile = FileUtils.createFileWithExtension("3gpp");
        recordUrl = null;
        recordPath = Objects.requireNonNull(audioFile).getAbsolutePath();
        mediaRecorder.setOutputFile(recordPath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingAudio() {

        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();

            mediaRecorder = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                voiceRecordingAction();
            } else {
                DisplayViewUI.displayToast(this, "Audio recording DENIED!!!");

            }
        }
    }


    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

}