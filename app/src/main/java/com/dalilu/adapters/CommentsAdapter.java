package com.dalilu.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.R;
import com.dalilu.databinding.LayoutCommentBinding;
import com.dalilu.model.Message;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

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
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {

        Message commentMsg = commentList.get(position);

        holder.layoutCommentBinding.setComments(commentMsg);

    }

    @Override
    public int getItemCount() {
        return commentList == null ? 0 : commentList.size();
    }


    static class CommentsViewHolder extends RecyclerView.ViewHolder {

        LayoutCommentBinding layoutCommentBinding;

        CommentsViewHolder(@NonNull LayoutCommentBinding layoutCommentBinding) {
            super(layoutCommentBinding.getRoot());
            this.layoutCommentBinding = layoutCommentBinding;

        }


    }
}
