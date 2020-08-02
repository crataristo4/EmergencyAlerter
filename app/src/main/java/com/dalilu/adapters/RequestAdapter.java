package com.dalilu.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.R;
import com.dalilu.databinding.LayoutRequestReceivedBinding;
import com.dalilu.model.RequestModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class RequestAdapter extends FirebaseRecyclerAdapter<RequestModel, RequestAdapter.RequestViewHolder> {


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public RequestAdapter(@NonNull FirebaseRecyclerOptions<RequestModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestViewHolder holder, int i, @NonNull RequestModel requestModel) {

        holder.layoutRequestReceivedBinding.setUsers(requestModel);
        holder.showResponse(requestModel.getResponse());


    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder((DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_request_received, parent, false)));

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        LayoutRequestReceivedBinding layoutRequestReceivedBinding;
        private Button btnAddContact;
        private CheckBox selectUserCheckBox;
        private TextView txtNewReq;

        public RequestViewHolder(@NonNull LayoutRequestReceivedBinding layoutRequestReceivedBinding) {
            super(layoutRequestReceivedBinding.getRoot());
            this.layoutRequestReceivedBinding = layoutRequestReceivedBinding;

            btnAddContact = layoutRequestReceivedBinding.btnAddContact;
            selectUserCheckBox = layoutRequestReceivedBinding.checkBox;
            txtNewReq = layoutRequestReceivedBinding.txtNewRequest;
        }


        //display the response details
        void showResponse(String response) {

            //customer can only chat , rate and view the route only when their request are accepted
            if (response.equals("pending")) {
                btnAddContact.setText(R.string.add);
                selectUserCheckBox.setVisibility(View.GONE);


            }
            if (response.equals("accepted")) {
                btnAddContact.setText(R.string.added);
                selectUserCheckBox.setVisibility(View.VISIBLE);
                txtNewReq.setVisibility(View.GONE);


            }

            if (response.equals("declined")) {
                btnAddContact.setText(R.string.declined);
                selectUserCheckBox.setVisibility(View.GONE);
                txtNewReq.setVisibility(View.GONE);


            }


        }


    }

}
