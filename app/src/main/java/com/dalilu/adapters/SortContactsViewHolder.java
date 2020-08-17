package com.dalilu.adapters;

import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.databinding.LayoutTestContactsBinding;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class SortContactsViewHolder extends RecyclerView.ViewHolder {
    public LayoutTestContactsBinding layoutTestContactsBinding;
    public CircleImageView imgUserPhoto;
    public Button btnSentLocation;

    public SortContactsViewHolder(@NonNull LayoutTestContactsBinding layoutTestContactsBinding) {
        super(layoutTestContactsBinding.getRoot());
        this.layoutTestContactsBinding = layoutTestContactsBinding;
        imgUserPhoto = layoutTestContactsBinding.userImage;
        btnSentLocation = layoutTestContactsBinding.btnSendLocation;

    }

    abstract void onDoneChanged(boolean isDone);


}
