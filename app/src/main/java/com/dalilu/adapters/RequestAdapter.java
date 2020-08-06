package com.dalilu.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.MainActivity;
import com.dalilu.R;
import com.dalilu.databinding.LayoutAddUserBinding;
import com.dalilu.model.RequestModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RequestAdapter extends FirebaseRecyclerAdapter<RequestModel, RequestAdapter.RequestViewHolder> {
    private static ContactsAdapter.onItemClickListener onItemClickListener;
    String uid;


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public RequestAdapter(@NonNull FirebaseRecyclerOptions<RequestModel> options) {
        super(options);
        uid = MainActivity.userId;
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestViewHolder holder, int i, @NonNull RequestModel requestModel) {

        DatabaseReference br = FirebaseDatabase.getInstance().getReference().child("Friends");

      /*  br.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.hasChild(uid)) {


                    holder.usersSingleLayoutBinding.setRequests(requestModel);
                    Glide.with(holder.usersSingleLayoutBinding.getRoot()).load(requestModel.getSenderPhoto()).into(holder.img);
                    holder.showResponse(requestModel.getResponse());

                } else {

                    String rvName = (String) snapshot.child("receiverName").getValue();
                    String rvId = (String) snapshot.child("receiverId").getValue();

                    Log.i("Receiver : ", rvName + " id: " + rvId);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestModel.getResponse().equals("pending")) {
                    DisplayViewUI.displayToast(view.getContext(), "adding user");
                    //br.child(requestModel.getSenderId()).child(requestModel.getReceiverId())

                }

            }
        });


*/

        //  holder.layoutAddUserBinding.setUsers(requestModel);
        holder.showResponse(requestModel.getResponse());

    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder((DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_add_user, parent, false)));

    }

    public void setOnItemClickListener(ContactsAdapter.onItemClickListener onItemClickListener) {
        RequestAdapter.onItemClickListener = onItemClickListener;

    }

    public interface onItemClickListener {
        void onClick(View view, int position);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LayoutAddUserBinding layoutAddUserBinding;
        Button btnAdd;


        public RequestViewHolder(@NonNull LayoutAddUserBinding layoutAddUserBinding) {
            super(layoutAddUserBinding.getRoot());
            this.layoutAddUserBinding = layoutAddUserBinding;

            btnAdd = layoutAddUserBinding.btnAddContact;
            btnAdd.setOnClickListener(this);


        }


        //display the response details
        void showResponse(String response) {

            if (response.equals("sent")) {
                btnAdd.setText(R.string.dcln);


            }
            if (response.equals("accepted")) {

                btnAdd.setText(R.string.frnds);


            }

            if (response.equals("declined")) {

                btnAdd.setText(R.string.Pending);


            }


        }


        @Override
        public void onClick(View view) {
            onItemClickListener.onClick(layoutAddUserBinding.getRoot(), getAdapterPosition());

        }
    }

}
