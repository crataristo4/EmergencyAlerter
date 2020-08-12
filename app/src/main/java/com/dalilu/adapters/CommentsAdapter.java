package com.dalilu.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.R;
import com.dalilu.databinding.LayoutCommentBinding;
import com.dalilu.databinding.LayoutPlayAudioBinding;
import com.dalilu.model.Message;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> commentList;

    public CommentsAdapter(ArrayList<Message> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new CommentsViewHolder((DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.layout_comment, viewGroup, false)));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
       /* Message commentMsg = commentList.get(position);

        holder.layoutCommentBinding.setComments(commentMsg);*/


    }


    @Override
    public int getItemCount() {
        return commentList == null ? 0 : commentList.size();
    }


    @Override
    public long getItemId(int position) {
        return commentList.get(position).getId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    static class CommentsViewHolder extends RecyclerView.ViewHolder {

        LayoutCommentBinding layoutCommentBinding;

        CommentsViewHolder(@NonNull LayoutCommentBinding layoutCommentBinding) {
            super(layoutCommentBinding.getRoot());
            this.layoutCommentBinding = layoutCommentBinding;

        }


    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {

        LayoutPlayAudioBinding layoutPlayAudioBinding;

        AudioViewHolder(@NonNull LayoutPlayAudioBinding layoutPlayAudioBinding) {
            super(layoutPlayAudioBinding.getRoot());
            this.layoutPlayAudioBinding = layoutPlayAudioBinding;

        }


    }
}
