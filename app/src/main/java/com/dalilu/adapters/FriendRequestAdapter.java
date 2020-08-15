package com.dalilu.adapters;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.dalilu.MainActivity;
import com.dalilu.R;
import com.dalilu.databinding.LayoutRequestReceivedBinding;
import com.dalilu.model.RequestModel;
import com.dalilu.utils.DisplayViewUI;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends FirestoreRecyclerAdapter<RequestModel, FriendRequestAdapter.RequestViewHolder> {
    private static FriendRequestAdapter.onItemClickListener onItemClickListener;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FriendRequestAdapter(@NonNull FirestoreRecyclerOptions<RequestModel> options) {
        super(options);
    }


    public void setOnItemClickListener(FriendRequestAdapter.onItemClickListener onItemClickListener) {
        FriendRequestAdapter.onItemClickListener = onItemClickListener;

    }

    @SuppressLint("CheckResult")
    @Override
    protected void onBindViewHolder(@NonNull FriendRequestAdapter.RequestViewHolder holder, int position, @NonNull RequestModel users) {
        holder.layoutRequestReceivedBinding.setRequests(users);
        holder.showResponse(users.getResponse());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(DisplayViewUI.getRandomDrawableColor());
        requestOptions.error(DisplayViewUI.getRandomDrawableColor());
        requestOptions.centerCrop();

        Glide.with(holder.layoutRequestReceivedBinding.getRoot().getContext())
                .load(users.getPhoto())
                .thumbnail(0.5f)
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (isFirstResource) {
                            holder.pbImageLoading.setVisibility(View.GONE);

                        }
                        holder.pbImageLoading.setVisibility(View.VISIBLE);
                        return false;

                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.pbImageLoading.setVisibility(View.GONE);

                        return false;
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(holder.layoutRequestReceivedBinding.getRoot().getContext().getDrawable(R.drawable.photo))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imgPhoto);

        CollectionReference friendsCollectionReference = FirebaseFirestore.getInstance().collection("Friends");

        String id = MainActivity.userId;
        String receiverId = getSnapshots().get(position).getId();
        String name = getSnapshots().get(position).getName();

        String friends = "friends", declined = "declined", response = "response";

        holder.btnAccept.setOnClickListener(view -> {
            friendsCollectionReference.document(receiverId).collection(receiverId).document(id).update(response, friends);
            friendsCollectionReference.document(id).collection(id).document(receiverId).update(response, friends);
            DisplayViewUI.displayToast(view.getContext(), view.getContext().getResources().getString(R.string.successFull));


        });


        if (holder.btnDecline.getText().toString().equals(holder.layoutRequestReceivedBinding.getRoot().getResources().getString(R.string.cancelRequest))) {
            holder.btnDecline.setOnClickListener(view -> {
                DisplayViewUI.displayAlertDialog(view.getContext(),
                        "Decline", "Are you sure you want to cancel request to " + name,
                        " Yes", "No",
                        (dialogInterface, i) -> {
                            if (i == -1) {

                                //remove document id for both users
                                friendsCollectionReference.document(id).collection(id).document(receiverId).delete();
                                friendsCollectionReference.document(receiverId).collection(receiverId).document(id).delete();
                            } else if (i == -2) {
                                dialogInterface.dismiss();
                            }


                        });

            });
        } else if (holder.btnDecline.getText().toString().equals(holder.layoutRequestReceivedBinding.getRoot().getResources().getString(R.string.dcln))) {
            holder.btnDecline.setOnClickListener(view -> {

                DisplayViewUI.displayAlertDialog(view.getContext(),
                        "Decline", "Are you sure you want to decline " + name,
                        " Yes", "No",
                        (dialogInterface, i) -> {
                            if (i == -1) {

                                //remove document id for receiver
                                friendsCollectionReference.document(receiverId).collection(receiverId).document(id).update(response, declined);
                                friendsCollectionReference.document(id).collection(id).document(receiverId).delete();

                            } else if (i == -2) {
                                dialogInterface.dismiss();
                            }


                        });


            });

        }


    }

    @NonNull
    @Override
    public FriendRequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendRequestAdapter.RequestViewHolder((DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_request_received, parent, false)));

    }

    public interface onItemClickListener {
        void onClick(View view, int position);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LayoutRequestReceivedBinding layoutRequestReceivedBinding;
        Button btnAccept, btnDecline, btnSendLocation;
        CircleImageView imgPhoto;
        ImageView imgDelete;
        TextView txtRequestDes;
        ProgressBar pbImageLoading;

        public RequestViewHolder(@NonNull LayoutRequestReceivedBinding layoutRequestReceivedBinding) {
            super(layoutRequestReceivedBinding.getRoot());
            this.layoutRequestReceivedBinding = layoutRequestReceivedBinding;

            btnAccept = layoutRequestReceivedBinding.accept;
            btnDecline = layoutRequestReceivedBinding.decline;
            btnSendLocation = layoutRequestReceivedBinding.btnSendLocation;
            imgPhoto = layoutRequestReceivedBinding.userImage;
            imgDelete = layoutRequestReceivedBinding.imgDeleteUser;
            txtRequestDes = layoutRequestReceivedBinding.txtRequestDes;
            pbImageLoading = layoutRequestReceivedBinding.pbImageLoading;

            btnSendLocation.setOnClickListener(this);


        }


        //display the response details
        void showResponse(String response) {

            switch (response) {
                case "friends":

                    btnAccept.setText(R.string.frnds);
                    btnAccept.setEnabled(false);
                    btnDecline.setVisibility(View.GONE);
                    imgDelete.setVisibility(View.VISIBLE);
                    btnSendLocation.setVisibility(View.VISIBLE);
                    txtRequestDes.setVisibility(View.GONE);


                    break;
                case "received":

                    btnAccept.setVisibility(View.VISIBLE);
                    btnDecline.setVisibility(View.VISIBLE);
                    btnSendLocation.setVisibility(View.GONE);
                    imgDelete.setVisibility(View.GONE);
                    txtRequestDes.setVisibility(View.VISIBLE);
                    txtRequestDes.setText(R.string.newReq);
                    txtRequestDes.setTextColor(this.layoutRequestReceivedBinding.getRoot().getContext().getResources().getColor(R.color.acceptGreen));


                    break;
                case "sent":

                    btnAccept.setVisibility(View.GONE);
                    btnDecline.setText(R.string.cancelRequest);
                    btnSendLocation.setVisibility(View.GONE);
                    imgDelete.setVisibility(View.GONE);
                    txtRequestDes.setVisibility(View.VISIBLE);
                    txtRequestDes.setText(R.string.requestSentTo);
                    txtRequestDes.setTextColor(this.layoutRequestReceivedBinding.getRoot().getContext().getResources().getColor(R.color.colorRed));


                    break;
                case "declined":
                    btnAccept.setText(R.string.Pending);
                    btnAccept.setVisibility(View.VISIBLE);
                    btnDecline.setVisibility(View.GONE);
                    btnSendLocation.setVisibility(View.GONE);
                    imgDelete.setVisibility(View.GONE);
                    btnAccept.setEnabled(false);

                    break;
            }


        }


        @Override
        public void onClick(View view) {
            onItemClickListener.onClick(layoutRequestReceivedBinding.getRoot(), getAdapterPosition());
            // onItemClickListener.onClickLocation(btnSendLocation, getAdapterPosition());


        }
    }


}