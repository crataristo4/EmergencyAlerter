package com.dalilu.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.databinding.LayoutTestContactsBinding;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class TestSortedContactsViewHolder extends RecyclerView.ViewHolder {
    LayoutTestContactsBinding layoutTestContactsBinding;
    CircleImageView imgUserPhoto;

    public TestSortedContactsViewHolder(@NonNull LayoutTestContactsBinding layoutTestContactsBinding) {
        super(layoutTestContactsBinding.getRoot());
        this.layoutTestContactsBinding = layoutTestContactsBinding;
        imgUserPhoto = layoutTestContactsBinding.userImage;

    }

    abstract void onDoneChanged(boolean isDone);
}
