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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class LocationSharingAdapter extends FirebaseRecyclerAdapter<ShareLocation, LocationSharingAdapter.LocationSharingViewHolder> {

    //  String uid = MainActivity.userId;
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
        holder.txtTime.setText(GetTimeAgo.getTimeAgo(shareLocation.getTime()));
        Glide.with(holder.layoutLocationSharingBinding.getRoot())
                .load(shareLocation.getPhoto())
                .error(holder.layoutLocationSharingBinding.getRoot().getResources().getDrawable(R.drawable.defaultavatar))
                .into(holder.imgPhoto);


       /* DatabaseReference locationDbRef = FirebaseDatabase.getInstance().getReference().child("Locations");

        locationDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild(uid)){

                    holder.txtName.setText("Your location is shared with " + shareLocation.getUserName());
                    holder.txtKnownName.setText(shareLocation.getKnownName());
                    holder.txtTime.setText(GetTimeAgo.getTimeAgo(shareLocation.getTime()));
                    holder.txtDate.setText(shareLocation.getDate());
holder.txtTo.setText(shareLocation.getUserName());
                    holder.txtUrl.setText(shareLocation.getUrl());
                    holder.txtUrl.setMovementMethod(LinkMovementMethod.getInstance());



                }else{

                    holder.txtName.setText(shareLocation.getFrom() + "shared location with you ");
                    holder.txtKnownName.setText(shareLocation.getKnownName());
                    holder.txtTime.setText(GetTimeAgo.getTimeAgo(shareLocation.getTime()));
                    holder.txtDate.setText(shareLocation.getDate());
                    holder.txtTo.setText(shareLocation.getFrom());
                        holder.txtUrl.setText(shareLocation.getUrl());
                        holder.txtUrl.setMovementMethod(LinkMovementMethod.getInstance());


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
*/

    }

    @NonNull
    @Override
    public LocationSharingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LocationSharingViewHolder((DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_location_sharing, parent, false)));

    }

    public static class LocationSharingViewHolder extends RecyclerView.ViewHolder {

        LayoutLocationSharingBinding layoutLocationSharingBinding;
        CircleImageView imgPhoto;
        TextView txtTime;//,txtName , txtKnownName , txtUrl , txtDate ,txtTo;

        public LocationSharingViewHolder(@NonNull LayoutLocationSharingBinding layoutLocationSharingBinding) {
            super(layoutLocationSharingBinding.getRoot());
            this.layoutLocationSharingBinding = layoutLocationSharingBinding;
            imgPhoto = layoutLocationSharingBinding.imgPhoto;
            txtTime = layoutLocationSharingBinding.txtTime;

            /*txtDate = layoutLocationSharingBinding.txtDate;
            txtKnownName = layoutLocationSharingBinding.txtKnownName;
            txtUrl = layoutLocationSharingBinding.txtUrl;
            txtTime = layoutLocationSharingBinding.txtTime;
            txtTo = layoutLocationSharingBinding.txtTo;

*/
        }

    }

}
