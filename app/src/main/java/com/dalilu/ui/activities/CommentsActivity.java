package com.dalilu.ui.activities;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.R;
import com.dalilu.adapters.CommentsAdapter;
import com.dalilu.model.Message;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.dalilu.utils.FileUtils;
import com.dalilu.utils.GetTimeAgo;
import com.dalilu.utils.PermissionUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
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
    private String id, getAlertPhotoUrl, getAddress, getDatePosted;
    private CommentsAdapter adapter;
    private DatabaseReference databaseReference;
    private EmojiconEditText emojiconEditText;
    private ArrayList<Message> commentList;
    private static final String[] WRITE_EXTERNAL_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] RECORD_AUDIO_PERMISSION = new String[]{Manifest.permission.RECORD_AUDIO};
    private static final int RECORD_AUDIO = 3;
    String randomId;
    final String name = MainActivity.userName;
    String dateTime;
    String userId = MainActivity.userId;
    private MediaRecorder mediaRecorder;
    private boolean recording;
    private String recordPath;
    private ImageView btnRecord;
    private ProgressDialog pd;
    private StorageReference filePath;
    private static final String TAG = "CommentsActivity";
    private ListenerRegistration registration;
    private DocumentSnapshot mLastResult;
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;
    private CollectionReference commentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar commentToolBar = findViewById(R.id.commentsToolBar);
        setSupportActionBar(commentToolBar);

        initViews();
        runOnUiThread(this::fetchCommentsData);
        // fetchCommentsData();


    }

    private void fetchCommentsData() {

        // Create a query against the collection.
        Query query = commentsRef.orderBy("timeStamp", Query.Direction.DESCENDING);

        registration = query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            commentList.clear();
            assert queryDocumentSnapshots != null;
            for (QueryDocumentSnapshot ds : queryDocumentSnapshots) {

                Message messageList = ds.toObject(Message.class);
                String name = messageList.getUserName();
                String dateTime = messageList.getMessageDateTime();
                long timeStamp = messageList.getTimeStamp();
                String message = messageList.getMessage();
                String audioUrl = messageList.getUrl();
                String id = ds.getId();

                //group data by text
                if (ds.getData().containsKey("text")) {

                    commentList.add(new Message(AppConstants.TEXT_TYPE, name, timeStamp, id, message));

                }
                //group data by audio
                else if (ds.getData().containsKey("audio")) {

                    commentList.add(new Message(AppConstants.AUDIO_TYPE, name, timeStamp, audioUrl));

                }


            }

            adapter.notifyDataSetChanged();


        });

    }

    void initViews() {
        Intent getCommentsIntent = getIntent();
        if (getCommentsIntent != null) {

            id = getCommentsIntent.getStringExtra("id");
            getAddress = getCommentsIntent.getStringExtra("address");
            getDatePosted = getCommentsIntent.getStringExtra("datePosted");

        }

        commentsRef = FirebaseFirestore.getInstance().collection("Comments").document(id).collection(id);


        StorageReference audioFilePath = FirebaseStorage.getInstance().getReference().child("audio");
        filePath = audioFilePath.child(UUID.randomUUID().toString());

        ConstraintLayout activity_comment = findViewById(R.id.activity_comment);
        ImageView emojiButton = findViewById(R.id.emoticonButton);
        emojiconEditText = findViewById(R.id.emoticonEditTxt);
        EmojIconActions emojIconActions = new EmojIconActions(getApplicationContext(), activity_comment, emojiButton, emojiconEditText);
        emojIconActions.ShowEmojicon();

        recording = false;
        btnRecord = findViewById(R.id.imgRecordAudio);
        btnRecord.setOnClickListener(v -> {
            try {
                voiceRecordingAction();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        pd = DisplayViewUI.displayProgress(this, getResources().getString(R.string.uploadingPleaseWait));


        findViewById(R.id.btnComment).setOnClickListener(v -> addComment());


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM HH:mm", Locale.ENGLISH);
        dateTime = simpleDateFormat.format(calendar.getTime());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        commentList = new ArrayList<>();

        adapter = new CommentsAdapter(commentList, CommentsActivity.this);
        recyclerView.setAdapter(adapter);


    }

    private void initializeMediaRecord() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
    }

    private void voiceRecordingAction() throws IOException {

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
                btnRecord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop_black_24px));


                initializeMediaRecord();
                startRecordingAudio();
            } else {
                // DisplayViewUI.displayToast(CommentsActivity.this, getString(R.string.stpdVoiceRecording));
                emojiconEditText.setHint(getResources().getString(R.string.type_your_comments_here));
                emojiconEditText.setHintTextColor(getResources().getColor(R.color.black));
                emojiconEditText.setEnabled(true);
                btnRecord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_keyboard_voice_24));
                stopRecordingAudio();
                uploadAudioRecording(Uri.fromFile(new File(recordPath)));
            }
            recording = !recording;
        }
    }

    private void addComment() {
        String postComment = emojiconEditText.getText().toString();

        if (!postComment.trim().isEmpty()) {
            HashMap<String, Object> comments = new HashMap<>();
            comments.put("message", postComment);
            comments.put("userName", name);
            comments.put("messageDateTime", dateTime);
            comments.put("timeStamp", GetTimeAgo.getTimeInMillis());
            comments.put("text", "text");

            commentsRef.add(comments);

            emojiconEditText.getText().clear();

        } else if (postComment.trim().isEmpty()) {
            emojiconEditText.setError("Cannot send empty message");
        }


    }

    private void uploadAudioRecording(Uri uri) throws IOException {
        runOnUiThread(() -> {

            pd.show();
            //upload audio to server
            filePath.putFile(uri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    pd.dismiss();

                }
                return filePath.getDownloadUrl();

            }).addOnSuccessListener(CommentsActivity.this, uri1 -> {
                        pd.dismiss();
                        DisplayViewUI.displayToast(CommentsActivity.this, getString(R.string.successFull));

                        Uri downLoadUri = Uri.parse(uri1.toString());
                        assert downLoadUri != null;
                        String url = downLoadUri.toString();

                        Map<String, Object> uploadAudio = new HashMap<>();
                        uploadAudio.put("userName", name);
                        uploadAudio.put("url", url);
                        uploadAudio.put("audio", "audio");
                        uploadAudio.put("timeStamp", GetTimeAgo.getTimeInMillis());
                        uploadAudio.put("messageDateTime", dateTime);

                        commentsRef.add(uploadAudio);

                    }

            ).addOnFailureListener(CommentsActivity.this, e -> {

                pd.dismiss();
                DisplayViewUI.displayToast(CommentsActivity.this, Objects.requireNonNull(e.getMessage()));

            });

        });


    }

    private void startRecordingAudio() {
        File audioFile = FileUtils.createFileWithExtension("3gpp");
        String recordUrl = null;
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

                try {
                    voiceRecordingAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    @Override
    protected void onStop() {
        super.onStop();
        registration.remove();
    }
}