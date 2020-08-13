package com.dalilu.adapters;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.Dalilu;
import com.dalilu.R;
import com.dalilu.databinding.LayoutCommentBinding;
import com.dalilu.databinding.LayoutPlayAudioBinding;
import com.dalilu.model.Message;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.GetTimeAgo;
import com.danikula.videocache.HttpProxyCacheServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> commentList;
    Context context;
    private MediaPlayer mediaPlayer;

    public CommentsAdapter(ArrayList<Message> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
        mediaPlayer = new MediaPlayer();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case AppConstants.TEXT_TYPE:
                return new CommentsViewHolder((DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.layout_comment, viewGroup, false)));

            case AppConstants.AUDIO_TYPE:
                return new AudioViewHolder(DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.layout_play_audio, viewGroup, false));


        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message commentMsg = commentList.get(position);

        if (commentMsg != null) {
            switch (commentMsg.type) {
                case AppConstants.TEXT_TYPE:

                    ((CommentsViewHolder) holder).layoutCommentBinding.setComments(commentMsg);
                    ((CommentsViewHolder) holder).txtDateTime.setText(GetTimeAgo.getTimeAgo(commentMsg.getTimeStamp()));


                    break;

                case AppConstants.AUDIO_TYPE:
                    ((AudioViewHolder) holder).layoutPlayAudioBinding.setAudio(commentMsg);
                    ((AudioViewHolder) holder).layoutPlayAudioBinding.txtDataTime.setText(GetTimeAgo.getTimeAgo(commentMsg.getTimeStamp()));

                    HttpProxyCacheServer proxy = Dalilu.getProxy(((AudioViewHolder) holder).layoutPlayAudioBinding.getRoot().getContext());
                    String proxyUrl = proxy.getProxyUrl(commentMsg.getUrl());

                    ((AudioViewHolder) holder).playAudio.setOnClickListener(view -> {
                        if (!mediaPlayer.isPlaying()) {

                            try {
                                ((AudioViewHolder) holder).playAudio.setImageDrawable(((AudioViewHolder) holder).layoutPlayAudioBinding.getRoot().getResources().getDrawable(R.drawable.ic_baseline_pause_24));

                                mediaPlayer.reset();
                                mediaPlayer.setDataSource(proxyUrl);
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                                mediaPlayer.setOnCompletionListener(mediaPlayer -> ((AudioViewHolder) holder).playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_play_arrow_24)));

                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        } else if (mediaPlayer.isPlaying()) {

                            mediaPlayer.stop();
                            mediaPlayer.release();
                            ((AudioViewHolder) holder).playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_play_arrow_24));
                            ((AudioViewHolder) holder).playAudio.setVisibility(View.VISIBLE);
                        }
                    });




              /*          ((AudioViewHolder) holder).playAudio.setOnClickListener(view -> {
                            if (!mediaPlayer.isPlaying()){

                                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                                    String uriLink = uri.toString();

                                    try {
                                        ((AudioViewHolder) holder).playAudio.setImageDrawable(((AudioViewHolder) holder).layoutPlayAudioBinding.getRoot().getResources().getDrawable(R.drawable.ic_stop_black_24px));

                                        mediaPlayer.reset();
                                        mediaPlayer.setDataSource(uriLink);
                                        mediaPlayer.prepare();
                                        mediaPlayer.start();
                                        mediaPlayer.setOnCompletionListener(mediaPlayer -> ((AudioViewHolder) holder).playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_play_arrow_24)));

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                });


                            } else if (mediaPlayer.isPlaying()){

                                mediaPlayer.stop();
                                ((AudioViewHolder) holder).audioSeekBar.setProgress(0);
                                ((AudioViewHolder) holder).playAudio.setImageDrawable(((AudioViewHolder) holder).layoutPlayAudioBinding.getRoot().getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));

                            }

                        });*/

                    break;
            }
        }


    }


    @Override
    public int getItemCount() {
        return commentList == null ? 0 : commentList.size();
    }


    @Override
    public int getItemViewType(int position) {

        switch (commentList.get(position).type) {
            case 1:
                return AppConstants.TEXT_TYPE;
            case 2:
                return AppConstants.AUDIO_TYPE;
            default:
                return 0;
        }
    }

    static class CommentsViewHolder extends RecyclerView.ViewHolder {

        LayoutCommentBinding layoutCommentBinding;
        TextView txtDateTime;

        CommentsViewHolder(@NonNull LayoutCommentBinding layoutCommentBinding) {
            super(layoutCommentBinding.getRoot());
            this.layoutCommentBinding = layoutCommentBinding;
            txtDateTime = layoutCommentBinding.txtDateTime;

        }


    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {

        LayoutPlayAudioBinding layoutPlayAudioBinding;
        ImageView playAudio;
        TextView txtDateTime;
        SeekBar audioSeekBar;

        AudioViewHolder(@NonNull LayoutPlayAudioBinding layoutPlayAudioBinding) {
            super(layoutPlayAudioBinding.getRoot());
            this.layoutPlayAudioBinding = layoutPlayAudioBinding;
            playAudio = layoutPlayAudioBinding.imgPlayBtn;
            txtDateTime = layoutPlayAudioBinding.txtDataTime;
            audioSeekBar = layoutPlayAudioBinding.seekBar;


        }


    }
}
