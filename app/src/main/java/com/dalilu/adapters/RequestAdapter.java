package com.dalilu.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.R;
import com.dalilu.databinding.LayoutRequestReceivedBinding;
import com.dalilu.model.RequestModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class RequestAdapter extends FirebaseRecyclerAdapter<RequestModel, RequestAdapter.RequestViewHolder> {
    private static ContactsAdapter.onItemClickListener onItemClickListener;

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

        holder.layoutRequestReceivedBinding.setRequests(requestModel);
        holder.showResponse(requestModel.getResponse());

    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder((DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_request_received, parent, false)));

    }

    public void setOnItemClickListener(ContactsAdapter.onItemClickListener onItemClickListener) {
        RequestAdapter.onItemClickListener = onItemClickListener;

    }

    public interface onItemClickListener {
        void onClick(View view, int position);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LayoutRequestReceivedBinding layoutRequestReceivedBinding;
        Button btnAccept, btnDecline;

        public RequestViewHolder(@NonNull LayoutRequestReceivedBinding layoutRequestReceivedBinding) {
            super(layoutRequestReceivedBinding.getRoot());
            this.layoutRequestReceivedBinding = layoutRequestReceivedBinding;

            btnAccept = layoutRequestReceivedBinding.accept;
            btnDecline = layoutRequestReceivedBinding.decline;

            btnAccept.setOnClickListener(this);
            btnDecline.setOnClickListener(this);


        }


        //display the response details
        void showResponse(String response) {

            if (response.equals("friends")) {

                btnAccept.setText(R.string.frnds);
                btnDecline.setVisibility(View.GONE);


            }
            if (response.equals("received")) {

                btnAccept.setText(R.string.pending);
                btnDecline.setVisibility(View.GONE);


            }

            // TODO: 8/7/2020 do same if request is declined...


        }


        @Override
        public void onClick(View view) {
            onItemClickListener.onClick(btnAccept, getAdapterPosition());
            onItemClickListener.onClick(btnDecline, getAdapterPosition());

        }
    }

}
