package com.dalilu.ui.bottomsheets;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.dalilu.MainActivity;
import com.dalilu.R;
import com.dalilu.databinding.PopUpAlerterBottomSheetBinding;
import com.dalilu.model.RequestModel;
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
    private String name, id, phoneNumber, photoUrl, response;
    private TextView txtName;
    private ImageView imgPhoto;
    private Button btnAddUser;
    private DatabaseReference friendsDbRef, friendsDbCheck;
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

        txtName = popUpAlerterBottomSheetBinding.txtName;
        imgPhoto = popUpAlerterBottomSheetBinding.imgPhoto;
        btnAddUser = popUpAlerterBottomSheetBinding.btnAddUser;

        friendsDbCheck = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsDbRef = FirebaseDatabase.getInstance().getReference("Friends");
        friendsCollectionReference = FirebaseFirestore.getInstance().collection("Friends");

        Bundle getUserDetailsBundle = getArguments();
        if (getUserDetailsBundle != null) {

            name = getUserDetailsBundle.getString(AppConstants.USER_NAME);
            id = getUserDetailsBundle.getString(AppConstants.UID);
            photoUrl = getUserDetailsBundle.getString(AppConstants.USER_PHOTO_URL);
            phoneNumber = getUserDetailsBundle.getString(AppConstants.PHONE_NUMBER);
            response = getUserDetailsBundle.getString(AppConstants.RESPONSE);

            txtName.setText(name);
            Glide.with(requireActivity()).load(photoUrl).into(imgPhoto);

        }


        //sender details
        String senderName = MainActivity.userName;
        String senderId = MainActivity.userId;
        String senderPhotoUrl = MainActivity.userPhotoUrl;
        String senderPhoneNumber = MainActivity.phoneNumber;

        //receiver
        Map<String, Object> from = new HashMap<>();
        from.put("id", senderId);
        from.put("name", senderName);
        from.put("photo", senderPhotoUrl);
        from.put("phoneNumber", senderPhoneNumber);
        from.put("response", "received");

        //sender
        Map<String, Object> to = new HashMap<>();
        to.put("id", id);
        to.put("name", name);
        to.put("photo", photoUrl);
        to.put("phoneNumber", phoneNumber);
        to.put("response", "sent");


        btnAddUser.setOnClickListener(view -> {
            try {
                friendsCollectionReference.document(senderId).collection(senderId).document(id).set(to);
                friendsCollectionReference.document(id).collection(id).document(senderId).set(from);
                dismiss();

                DisplayViewUI.displayToast(requireContext(), requireActivity().getString(R.string.addedUser));
/*

                    friendsCollectionReference.document(senderId).collection(senderId).document(id).set(to).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                friendsCollectionReference.document(id).collection(id).document(senderId).set(from);
                                progressBar.dismiss();
                                dismiss();

                            } else {
                                progressBar.dismiss();
                                DisplayViewUI.displayToast(requireContext(), Objects.requireNonNull(task.getException()).getMessage());
                            }
                        }
                    });
*/


            } catch (Exception e) {

                e.printStackTrace();
            }

        });

        popUpAlerterBottomSheetBinding.btnCancel.setOnClickListener(v -> dismiss());

        //Double checking
        //1. check friends db
        //2. check the senders and receivers node respectively
        //3. check the response and update the UI
        Query query = friendsCollectionReference.document(senderId).collection(senderId).document(senderId).collection(id);

        query.addSnapshotListener((value, error) -> {

            assert value != null;
            for (QueryDocumentSnapshot ds : value) {
                RequestModel requestModel = ds.toObject(RequestModel.class);
                String response = requestModel.getResponse();

                if (response.equals("sent")) {
                    //change add user btn to cancel request
                    Log.i(TAG, "Response: " + response);

                    btnAddUser.setText(R.string.cancelRequest);
                    btnAddUser.setTextColor(requireActivity().getResources().getColor(R.color.white));
                    btnAddUser.setBackgroundColor(requireActivity().getResources().getColor(R.color.colorRed));
                    btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, requireActivity().getDrawable(R.drawable.ic_baseline_cancel_24), null);

                    // TODO: 8/14/2020 remove request sent
                    btnAddUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DisplayViewUI.displayToast(requireActivity(), "removing user");
                        }
                    });
                } else if (response.equals("friends")) {
                    DisplayViewUI.displayToast(requireActivity(), response);
                    btnAddUser.setText(R.string.deleteUser);
                    btnAddUser.setTextColor(requireActivity().getResources().getColor(R.color.black));
                    btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, requireActivity().getDrawable(R.drawable.ic_delete), null);
                    // TODO: 8/14/2020 delete request sent
                    btnAddUser.setOnClickListener(view -> DisplayViewUI.displayToast(requireActivity(), "deleting user"));

                } else if (response.equals("received")) {
                    DisplayViewUI.displayToast(requireActivity(), response);
                    Log.i(TAG, "Response: " + response);
                    btnAddUser.setText(R.string.delete);
                    btnAddUser.setTextColor(requireActivity().getResources().getColor(R.color.white));
                    btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, requireActivity().getDrawable(R.drawable.ic_delete), null);
                    // TODO: 8/14/2020 remove request received
                    btnAddUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DisplayViewUI.displayToast(requireActivity(), "removing user");

                        }
                    });
                }

            }

        });


       /* friendsDbCheck.child(senderId).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String response = (String) snapshot.child("response").getValue();

                    Log.i("Response", "Response: " + response);
                    assert response != null;
                    if (response.equals("sent")) {
                        //change add user btn to cancel request
                        btnAddUser.setText(R.string.cancelRequest);
                        btnAddUser.setTextColor(requireActivity().getResources().getColor(R.color.colorRed));
                        btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, requireActivity().getDrawable(R.drawable.ic_baseline_cancel_24), null);
                    } else if (response.equals("friends")) {
//change add user btn to delete request
                        btnAddUser.setText(R.string.delete);
                        btnAddUser.setTextColor(requireActivity().getResources().getColor(R.color.white));
                        btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, requireActivity().getDrawable(R.drawable.ic_delete), null);

                    }*//*else {
                        //change add user btn to accept  request
                        btnAddUser.setText(R.string.acpt);
                        btnAddUser.setTextColor(requireActivity().getResources().getColor(R.color.acceptGreen));
                        btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, requireActivity().getDrawable(R.drawable.ic_check_black_24dp), null);

                    }*//*
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
*/

       /* friendsDbCheck.child(id).child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String response = (String) snapshot.child("response").getValue();
                assert  response != null;

                Log.i(TAG, "response: " + response);

                if (response.equals("received")){
                    Log.i(TAG, "response: " + response);
                    //change add user btn to accept  request
                    btnAddUser.setText(R.string.acpt);
                    btnAddUser.setTextColor(requireActivity().getResources().getColor(R.color.acceptGreen));
                    btnAddUser.setCompoundDrawablesWithIntrinsicBounds(null, null, requireActivity().getDrawable(R.drawable.ic_check_black_24dp), null);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
*/


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
}
