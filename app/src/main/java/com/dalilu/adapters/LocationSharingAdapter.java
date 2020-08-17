package com.dalilu.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dalilu.R;
import com.dalilu.databinding.LayoutLocationSharingBinding;
import com.dalilu.model.ShareLocation;
import com.dalilu.utils.GetTimeAgo;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class LocationSharingAdapter extends FirestoreRecyclerAdapter<ShareLocation, LocationSharingAdapter.LocationSharingViewHolder> {


    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public LocationSharingAdapter(@NonNull FirestoreRecyclerOptions<ShareLocation> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull LocationSharingViewHolder holder, int i, @NonNull ShareLocation shareLocation) {
        holder.layoutLocationSharingBinding.setLocation(shareLocation);
        holder.txtTime.setText(GetTimeAgo.getTimeAgo(shareLocation.getTimeStamp()));
        Glide.with(holder.layoutLocationSharingBinding.getRoot())
                .load(shareLocation.getPhoto())
                .error(holder.layoutLocationSharingBinding.getRoot().getResources().getDrawable(R.drawable.defaultavatar))
                .into(holder.imgPhoto);


    }

    @NonNull
    @Override
    public LocationSharingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LocationSharingViewHolder((DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_location_sharing, parent, false)));

    }

    public static class LocationSharingViewHolder extends RecyclerView.ViewHolder {

        LayoutLocationSharingBinding layoutLocationSharingBinding;
        CircleImageView imgPhoto;
        TextView txtTime;

        public LocationSharingViewHolder(@NonNull LayoutLocationSharingBinding layoutLocationSharingBinding) {
            super(layoutLocationSharingBinding.getRoot());
            this.layoutLocationSharingBinding = layoutLocationSharingBinding;
            imgPhoto = layoutLocationSharingBinding.imgPhoto;
            txtTime = layoutLocationSharingBinding.txtTime;


        }

    }

}
