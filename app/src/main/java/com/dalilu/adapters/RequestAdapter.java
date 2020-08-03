package com.dalilu.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dalilu.MainActivity;
import com.dalilu.R;
import com.dalilu.databinding.UsersSingleLayoutBinding;
import com.dalilu.model.RequestModel;
import com.dalilu.utils.DisplayViewUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends FirebaseRecyclerAdapter<RequestModel, RequestAdapter.RequestViewHolder> {

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

      /*  holder.layoutRequestReceivedBinding.setUsers(requestModel);
        holder.showResponse(requestModel.getResponse());
*/

        DatabaseReference br = FirebaseDatabase.getInstance().getReference().child("Friends");

        br.addListenerForSingleValueEvent(new ValueEventListener() {
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


        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestModel.getResponse().equals("pending")) {
                    DisplayViewUI.displayToast(view.getContext(), "adding user");
                    //br.child(requestModel.getSenderId()).child(requestModel.getReceiverId())

                }

            }
        });


    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder((DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.users_single_layout, parent, false)));

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

       /* LayoutRequestReceivedBinding layoutRequestReceivedBinding;
        private Button btnAddContact;
        private CheckBox selectUserCheckBox;
        private TextView txtNewReq;*/

     /*     public RequestViewHolder(@NonNull LayoutRequestReceivedBinding layoutRequestReceivedBinding) {
            super(layoutRequestReceivedBinding.getRoot());
          this.layoutRequestReceivedBinding = layoutRequestReceivedBinding;

            btnAddContact = layoutRequestReceivedBinding.btnAddContact;
            selectUserCheckBox = layoutRequestReceivedBinding.checkBox;
            txtNewReq = layoutRequestReceivedBinding.txtNewRequest;
        }*/


        UsersSingleLayoutBinding usersSingleLayoutBinding;
        Button btnAccept, btnDecline;
        TextView txtName, txtId;
        CircleImageView img;

        public RequestViewHolder(@NonNull UsersSingleLayoutBinding usersSingleLayoutBinding) {
            super(usersSingleLayoutBinding.getRoot());
            this.usersSingleLayoutBinding = usersSingleLayoutBinding;

            btnAccept = usersSingleLayoutBinding.accept;
            btnDecline = usersSingleLayoutBinding.decline;
            txtName = usersSingleLayoutBinding.txtName;
            txtId = usersSingleLayoutBinding.txtId;
            img = usersSingleLayoutBinding.userImage;

        }


        //display the response details
        void showResponse(String response) {

            if (response.equals("pending")) {
                btnAccept.setText("Accept");
                btnDecline.setText("Decline");

               /* btnAddContact.setText(R.string.add);
                selectUserCheckBox.setVisibility(View.GONE);

*/

            }
            if (response.equals("accepted")) {
                btnDecline.setVisibility(View.GONE);
                btnAccept.setText("Accepted");

               /* btnAddContact.setText(R.string.added);
                selectUserCheckBox.setVisibility(View.VISIBLE);
                txtNewReq.setVisibility(View.GONE);
*/

            }

            if (response.equals("declined")) {
                btnDecline.setVisibility(View.GONE);
                btnAccept.setText("Declined");

               /* btnAddContact.setText(R.string.declined);
                selectUserCheckBox.setVisibility(View.GONE);
                txtNewReq.setVisibility(View.GONE);
*/

            }


        }


    }

}
