package com.dalilu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dalilu.R;
import com.dalilu.databinding.LayoutTestContactsBinding;
import com.dalilu.model.Contact;

import de.hdodenhof.circleimageview.CircleImageView;

public class SortContactsAdapter extends RecyclerView.Adapter<SortContactsAdapter.ContactsViewHolder> {
    final LayoutInflater mLayoutInflater;
    SortedList<Contact> mContacts;
    private Context mContext;
    private static SortContactsAdapter.onItemClickListener onItemClickListener;


    public SortContactsAdapter(Context context, LayoutInflater layoutInflater, Contact... items) {
        mContext = context;

        mLayoutInflater = layoutInflater;
        mContacts = new SortedList<>(Contact.class, new SortedListAdapterCallback<Contact>(this) {
            @Override
            public int compare(Contact t0, Contact t1) {
                return t0.getUserName().compareTo(t1.getUserName());
            }

            @Override
            public boolean areContentsTheSame(Contact oldItem,
                                              Contact newItem) {
                return oldItem.getUserName().equals(newItem.getUserName());
            }

            @Override
            public boolean areItemsTheSame(Contact item1, Contact item2) {
                return item1.getPhoneNumber().equals(item2.getPhoneNumber());
            }
        });

        if (items != null) {
            for (Contact item : items) {
                mContacts.add(item);
            }
        }
    }

    public void setOnItemClickListener(SortContactsAdapter.onItemClickListener onItemClickListener) {
        SortContactsAdapter.onItemClickListener = onItemClickListener;

    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactsViewHolder((DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.layout_test_contacts, parent,
                false))) {


            @Override
            void onDoneChanged(boolean isDone) {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) {
                    return;
                }
                mContacts.recalculatePositionOfItemAt(adapterPosition);
            }
        };

    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        Contact contact = mContacts.get(position);
        holder.layoutTestContactsBinding.setContacts(contact);
        //load user photos
        Glide.with(holder.layoutTestContactsBinding.getRoot())
                .load(contact.getUserPhotoUrl())
                .thumbnail(0.5f)
                .error(holder.layoutTestContactsBinding.getRoot().getContext().getDrawable(R.drawable.boy))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imgUserPhoto);

        holder.btnSentLocation.setOnClickListener(view -> onItemClickListener.onClick(holder, contact, position));

    }


    public interface onItemClickListener {
        void onClick(RecyclerView.ViewHolder vh, Contact contact, int position);
    }

    public interface GenericItemClick<T, VH extends RecyclerView.ViewHolder> {
        void onClick(VH vh, T item, int position);
    }

   /* @Override
    public void onBindViewHolder(@NonNull SortContactsViewHolder holder, int position) {




    }*/

    public void addItem(Contact item) {
        mContacts.add(item);
    }

    @Override
    public int getItemCount() {
        return mContacts == null ? 0 : mContacts.size();
    }

    public abstract static class ContactsViewHolder extends RecyclerView.ViewHolder {
        public LayoutTestContactsBinding layoutTestContactsBinding;
        public CircleImageView imgUserPhoto;
        public Button btnSentLocation;

        public ContactsViewHolder(@NonNull LayoutTestContactsBinding layoutTestContactsBinding) {
            super(layoutTestContactsBinding.getRoot());
            this.layoutTestContactsBinding = layoutTestContactsBinding;
            imgUserPhoto = layoutTestContactsBinding.userImage;
            btnSentLocation = layoutTestContactsBinding.btnSendLocation;


        }

        abstract void onDoneChanged(boolean isDone);
    }

}





