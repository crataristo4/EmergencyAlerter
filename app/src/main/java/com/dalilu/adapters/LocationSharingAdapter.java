package com.dalilu.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dalilu.R;
import com.dalilu.databinding.LayoutLocationSharingBinding;
import com.dalilu.model.ShareLocation;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class LocationSharingAdapter extends FirebaseRecyclerAdapter<ShareLocation, LocationSharingAdapter.LocationSharingViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public LocationSharingAdapter(@NonNull FirebaseRecyclerOptions<ShareLocation> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull LocationSharingViewHolder holder, int i, @NonNull ShareLocation shareLocation) {
        holder.layoutLocationSharingBinding.setLocation(shareLocation);
        Glide.with(holder.layoutLocationSharingBinding.getRoot())
                .load(shareLocation.getPhotoUrl())
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

        public LocationSharingViewHolder(@NonNull LayoutLocationSharingBinding layoutLocationSharingBinding) {
            super(layoutLocationSharingBinding.getRoot());
            this.layoutLocationSharingBinding = layoutLocationSharingBinding;
            imgPhoto = layoutLocationSharingBinding.imgPhoto;


        }

    }

}
