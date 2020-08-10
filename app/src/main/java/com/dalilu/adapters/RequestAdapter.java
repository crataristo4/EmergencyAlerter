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

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends FirebaseRecyclerAdapter<RequestModel, RequestAdapter.RequestViewHolder> {
    private static RequestAdapter.onItemClickListener onItemClickListener;

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

      /*  Glide.with(holder.layoutRequestReceivedBinding.getRoot().getContext())
                .load(requestModel.getPhoto())
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(holder.layoutRequestReceivedBinding.getRoot().getContext().getDrawable(R.drawable.photo))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imgPhoto);*/

    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder((DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_request_received, parent, false)));

    }

    public void setOnItemClickListener(RequestAdapter.onItemClickListener onItemClickListener) {
        RequestAdapter.onItemClickListener = onItemClickListener;


    }


    public interface onItemClickListener {
        void onClick(View view, int position);


    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LayoutRequestReceivedBinding layoutRequestReceivedBinding;
        Button btnAccept, btnDecline, btnSendLocation;
        CircleImageView imgPhoto;

        public RequestViewHolder(@NonNull LayoutRequestReceivedBinding layoutRequestReceivedBinding) {
            super(layoutRequestReceivedBinding.getRoot());
            this.layoutRequestReceivedBinding = layoutRequestReceivedBinding;

            btnAccept = layoutRequestReceivedBinding.accept;
            btnDecline = layoutRequestReceivedBinding.decline;
            btnSendLocation = layoutRequestReceivedBinding.btnSendLocation;
            imgPhoto = layoutRequestReceivedBinding.userImage;

            btnAccept.setOnClickListener(this);
            // btnSendLocation.setOnClickListener(this);


        }


        //display the response details
        void showResponse(String response) {

            if (response.equals("friends")) {

                btnAccept.setText(R.string.frnds);
                btnAccept.setEnabled(false);
                btnDecline.setVisibility(View.GONE);
                btnSendLocation.setVisibility(View.VISIBLE);


            } else if (response.equals("received")) {

                btnAccept.setVisibility(View.VISIBLE);
                btnDecline.setVisibility(View.VISIBLE);
                btnSendLocation.setVisibility(View.GONE);


            } else if (response.equals("sent")) {

                btnAccept.setText(R.string.Pending);
                btnDecline.setText(R.string.cancelRequest);
                btnSendLocation.setVisibility(View.GONE);


            } else if (response.equals("declined")) {
                btnAccept.setText(R.string.Pending);
                btnSendLocation.setVisibility(View.GONE);
                btnAccept.setEnabled(false);

            }

            // TODO: 8/7/2020 do same if request is declined...


        }


        @Override
        public void onClick(View view) {
            onItemClickListener.onClick(layoutRequestReceivedBinding.getRoot(), getAdapterPosition());
            // onItemClickListener.onClickLocation(btnSendLocation, getAdapterPosition());

        }
    }

}
