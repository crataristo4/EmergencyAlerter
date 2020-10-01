package com.dalilu.ui.bottomsheets;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.dalilu.R;
import com.dalilu.databinding.PopUpAlerterBottomSheetBinding;
import com.dalilu.ui.activities.BaseActivity;
import com.dalilu.utils.AppConstants;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class PopUpAlerter extends BottomSheetDialogFragment {
    PopUpAlerterBottomSheetBinding popUpAlerterBottomSheetBinding;
    private static final String TAG = "PopUpAlerter";
    private String name;
    private String id;
    private String phoneNumber;
    private String photoUrl;
    String senderId;
    private AppCompatButton btnAddUser;
    private CollectionReference friendsCollectionReference;
    ConstraintLayout constraintLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        popUpAlerterBottomSheetBinding = DataBindingUtil.inflate(inflater, R.layout.pop_up_alerter_bottom_sheet, container, false);

        return popUpAlerterBottomSheetBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();


    }

    private void initViews() {
        //ProgressDialog progressBar = DisplayViewUI.displayProgress(requireActivity(), getString(R.string.addingUser));
        TextView txtName = popUpAlerterBottomSheetBinding.txtName;
        ImageView imgPhoto = popUpAlerterBottomSheetBinding.imgPhoto;
        btnAddUser = popUpAlerterBottomSheetBinding.btnAddUser;
        constraintLayout = popUpAlerterBottomSheetBinding.linearLayout2;

        friendsCollectionReference = FirebaseFirestore.getInstance().collection("Friends");

        Bundle getUserDetailsBundle = getArguments();
        if (getUserDetailsBundle != null) {

            name = getUserDetailsBundle.getString(AppConstants.USER_NAME);
            id = getUserDetailsBundle.getString(AppConstants.UID);
            photoUrl = getUserDetailsBundle.getString(AppConstants.USER_PHOTO_URL);
            phoneNumber = getUserDetailsBundle.getString(AppConstants.PHONE_NUMBER);

            txtName.setText(name);
            Glide.with(requireActivity()).load(photoUrl).into(imgPhoto);

        }
        //sender details
        String senderName = BaseActivity.userName;
        senderId = BaseActivity.uid;
        String senderPhotoUrl = BaseActivity.userPhotoUrl;
        String senderPhoneNumber = BaseActivity.phoneNumber;

        //receiver
        Map<String, Object> from = new HashMap<>();
        from.put("id", senderId);
        from.put("name", senderName);
        from.put("photo", senderPhotoUrl);
        from.put("phoneNumber", senderPhoneNumber);
        from.put("response", "received");
        from.put("isSharingLocation", false);

        //sender
        Map<String, Object> to = new HashMap<>();
        to.put("id", id);
        to.put("name", name);
        to.put("photo", photoUrl);
        to.put("phoneNumber", phoneNumber);
        to.put("response", "sent");
        from.put("isSharingLocation", false);

        btnAddUser.setOnClickListener(view -> {
            try {
                friendsCollectionReference.document(senderId).collection(senderId).document(id).set(to);
                friendsCollectionReference.document(id).collection(id).document(senderId).set(from);
                //  DisplayViewUI.displaySnackBar(constraintLayout,"Request sent to " + name);
                new Handler().postDelayed(this::dismiss, 2000);

            } catch (Exception e) {

                e.printStackTrace();
            }

        });

        popUpAlerterBottomSheetBinding.btnCancel.setOnClickListener(v -> dismiss());

        //Double checking
        //1. check friends db
        //2. check the senders and receivers node respectively
        //3. check the response and update the UI
        Query query = friendsCollectionReference.document(senderId).collection(senderId);

/*
        query.addSnapshotListener((value, error) -> {

            assert value != null;
            for (QueryDocumentSnapshot ds : value) {
                RequestModel requestModel = ds.toObject(RequestModel.class);
                String response = requestModel.getResponse();

                switch (response) {
                    case "sent":
                        btnAddUser.setTag(R.string.cancelRequest);
                        btnAddUser.setText(R.string.cancelRequest);
                        btnAddUser.setTextColor(requireContext().getResources().getColor(R.color.white));
                        btnAddUser.setBackgroundColor(requireContext().getResources().getColor(R.color.colorRed));
                        btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_cancel_24), null);

                        if (btnAddUser.getTag().equals(R.string.cancelRequest)) {
                            btnAddUser.setOnClickListener(view -> {
                                deleteOrRemoveUser();
                                Log.i(TAG, "cancelling request");

                            });


                        }

                        break;
                    case "friends":
//                        DisplayViewUI.displayToast(requireActivity(), response);
                        btnAddUser.setText(R.string.deleteUser);
                        btnAddUser.setTag(R.string.deleteUser);
                        btnAddUser.setTextColor(requireContext().getResources().getColor(R.color.black));
                        btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete), null);
                        // Delete user from friends
                        if (btnAddUser.getTag().equals(R.string.deleteUser)) {
                            btnAddUser.setOnClickListener(view -> {
                                deleteOrRemoveUser();
                                Log.i(TAG, "deleting request");

                            });
                        }

                        break;
                    case "received":
                        // DisplayViewUI.displayToast(requireContext(), response);
                        Log.i(TAG, "Response: " + response);
                        btnAddUser.setText(R.string.delete);
                        btnAddUser.setTag(R.string.delete);
                        btnAddUser.setTextColor(requireContext().getResources().getColor(R.color.white));
                        btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete), null);
                        //  remove request received
                        //  delete request sent
                        if (btnAddUser.getTag().equals(R.string.delete)) {
                            btnAddUser.setOnClickListener(view -> {
                                deleteOrRemoveUser();
                                Log.i(TAG, "removing request");

                            });
                        }
                        break;

                }

            }

        });
*/


    }

    void deleteOrRemoveUser() {
        friendsCollectionReference.document(id).collection(id).document(senderId).delete();
        friendsCollectionReference.document(senderId).collection(senderId).document(id).delete();
        new Handler().postDelayed(this::dismiss, 2000);
    }

}
