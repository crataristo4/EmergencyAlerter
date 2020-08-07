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
import com.dalilu.utils.AppConstants;
import com.dalilu.utils.DisplayViewUI;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PopUpAlerter extends BottomSheetDialogFragment {
    PopUpAlerterBottomSheetBinding popUpAlerterBottomSheetBinding;
    private static final String TAG = "PopUpAlerter";
    private String name, id, phoneNumber, photoUrl;
    private TextView txtName;
    private ImageView imgPhoto;
    private Button btnAddUser;
    private DatabaseReference friendsDbCheck;


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

        friendsDbCheck = FirebaseDatabase.getInstance().getReference("Friends");
        initViews();


    }

    private void initViews() {

        ProgressDialog progressBar = DisplayViewUI.displayProgress(requireActivity(), getString(R.string.addingUser));

        txtName = popUpAlerterBottomSheetBinding.txtName;
        imgPhoto = popUpAlerterBottomSheetBinding.imgPhoto;
        btnAddUser = popUpAlerterBottomSheetBinding.btnAddUser;

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
        from.put("response", "sent");

        //sender
        Map<String, Object> to = new HashMap<>();
        to.put("id", id);
        to.put("name", name);
        to.put("photo", photoUrl);
        to.put("phoneNumber", phoneNumber);
        to.put("response", "received");

        btnAddUser.setOnClickListener(view -> {
            progressBar.show();

            requireActivity().runOnUiThread(() -> {
                try {
                    friendsDbCheck.child(senderId).child(id).setValue(to).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            friendsDbCheck.child(id).child(senderId).setValue(from).addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()) {
                                    progressBar.dismiss();
                                    dismiss();

                                } else {
                                    progressBar.dismiss();
                                    DisplayViewUI.displayToast(requireContext(), Objects.requireNonNull(task1.getException()).getMessage());
                                }
                            });
                        } else {
                            progressBar.dismiss();
                            DisplayViewUI.displayToast(requireContext(), Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        popUpAlerterBottomSheetBinding.btnCancel.setOnClickListener(v -> dismiss());


        //check friends details and update button
        friendsDbCheck.child(id).child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    String response = (String) snapshot.child("response").getValue();

                    Log.i(TAG, "Response: " + response);
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

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
}
