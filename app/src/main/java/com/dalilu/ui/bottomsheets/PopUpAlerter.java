package com.dalilu.ui.bottomsheets;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.dalilu.R;
import com.dalilu.databinding.PopUpAlerterBottomSheetBinding;
import com.dalilu.model.RequestModel;
import com.dalilu.ui.activities.BaseActivity;
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class PopUpAlerter extends BottomSheetDialogFragment {
    PopUpAlerterBottomSheetBinding popUpAlerterBottomSheetBinding;
    private static final String TAG = "PopUpAlerter";
    private String name;
    private String id;
    private String phoneNumber;
    private String photoUrl;
    private AppCompatButton btnAddUser;
    private CollectionReference friendsCollectionReference;


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
        ProgressDialog progressBar = DisplayViewUI.displayProgress(requireActivity(), getString(R.string.addingUser));

        TextView txtName = popUpAlerterBottomSheetBinding.txtName;
        ImageView imgPhoto = popUpAlerterBottomSheetBinding.imgPhoto;
        btnAddUser = popUpAlerterBottomSheetBinding.btnAddUser;

        DatabaseReference friendsDbCheck = FirebaseDatabase.getInstance().getReference().child("Friends");
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
        String senderId = BaseActivity.uid;
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

                if (view.getTag().equals(R.string.cancelRequest)) {
                    Log.i(TAG, "cancelling request");


                } else if (view.getTag().equals(R.string.deleteUser)) {
                    Log.i(TAG, "deleting request");

                } else if (view.getTag().equals(R.string.delete)) {
                    Log.i(TAG, "removing request");

                }


                friendsCollectionReference.document(senderId).collection(senderId).document(id).set(to);
                friendsCollectionReference.document(id).collection(id).document(senderId).set(from);
                dismiss();

                DisplayViewUI.displayToast(requireContext(), requireActivity().getString(R.string.addedUser));

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

        query.addSnapshotListener((value, error) -> {

            assert value != null;
            for (QueryDocumentSnapshot ds : value) {
                RequestModel requestModel = ds.toObject(RequestModel.class);
                String response = requestModel.getResponse();

                switch (response) {
                    case "sent":
                        //change add user btn to cancel request
                        Log.i(TAG, "Response: " + response);

                        btnAddUser.setTag(R.string.cancelRequest);
                        btnAddUser.setText(R.string.cancelRequest);
                        btnAddUser.setTextColor(requireActivity().getResources().getColor(R.color.white));
                        btnAddUser.setBackgroundColor(requireActivity().getResources().getColor(R.color.colorRed));
                        btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_cancel_24), null);


                        break;
                    case "friends":
                        DisplayViewUI.displayToast(requireActivity(), response);
                        btnAddUser.setText(R.string.deleteUser);
                        btnAddUser.setTag(R.string.deleteUser);
                        btnAddUser.setTextColor(requireActivity().getResources().getColor(R.color.black));
                        btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete), null);
                        // TODO: 8/14/2020 delete request sent
                        /*if (Objects.equals(btnAddUser.getText().toString(), R.string.deleteUser)){
                            btnAddUser.setOnClickListener(view -> DisplayViewUI.displayToast(requireContext(), "deleting user"));
                            Log.i(TAG, "deleting user");

                        }*/

                        break;
                    case "received":
                        DisplayViewUI.displayToast(requireActivity(), response);
                        Log.i(TAG, "Response: " + response);
                        btnAddUser.setText(R.string.delete);
                        btnAddUser.setTag(R.string.delete);
                        btnAddUser.setTextColor(requireActivity().getResources().getColor(R.color.white));
                        btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete), null);
                        // TODO: 8/14/2020 remove request received
                        // TODO: 8/14/2020 delete request sent
                        /*if (Objects.equals(btnAddUser.getText().toString(), R.string.deleteUser)){
                            btnAddUser.setOnClickListener(view -> DisplayViewUI.displayToast(requireContext(), "removing user"));

                        }*/
                        break;

                    default:
                        btnAddUser.setTag(R.string.add);
                }

            }

        });


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
