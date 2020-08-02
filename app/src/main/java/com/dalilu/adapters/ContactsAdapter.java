package com.dalilu.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.R;
import com.dalilu.databinding.LayoutContactsBinding;
import com.dalilu.model.Users;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

public class ContactsAdapter extends FirestoreRecyclerAdapter<Users, ContactsAdapter.ContactsViewHolder> {

    private static onItemClickListener onItemClickListener;
    String uid;
    FirebaseAuth mAuth;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ContactsAdapter(@NonNull FirestoreRecyclerOptions<Users> options) {
        super(options);

        mAuth = FirebaseAuth.getInstance();
        uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();


    }


    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactsViewHolder((DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_contacts, parent, false)));

    }


    @Override
    protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Users users) {

        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
        String id = documentSnapshot.getId();

        if (uid.equals(id)) {

            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.layoutContactsBinding.getRoot().getLayoutParams();

            layoutParams.height = 0;
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            holder.layoutContactsBinding.getRoot().setVisibility(View.VISIBLE);

        } else {

            holder.layoutContactsBinding.setUsers(users);

        }


    }


    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        ContactsAdapter.onItemClickListener = onItemClickListener;

    }

    public interface onItemClickListener {
        void onClick(View view, int position);
    }


    static class ContactsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LayoutContactsBinding layoutContactsBinding;

        ContactsViewHolder(@NonNull LayoutContactsBinding layoutContactsBinding) {
            super(layoutContactsBinding.getRoot());
            this.layoutContactsBinding = layoutContactsBinding;

            layoutContactsBinding.btnAddContact.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {
            onItemClickListener.onClick(layoutContactsBinding.getRoot(), getAdapterPosition());

        }
    }
}
