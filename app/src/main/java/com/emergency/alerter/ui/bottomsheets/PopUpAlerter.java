package com.emergency.alerter.ui.bottomsheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.emergency.alerter.R;
import com.emergency.alerter.databinding.PopUpAlerterBottomSheetBinding;
import com.emergency.alerter.utils.DisplayViewUI;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PopUpAlerter extends BottomSheetDialogFragment {
    PopUpAlerterBottomSheetBinding popUpAlerterBottomSheetBinding;


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
//alert actions
        String[] alertActions = new String[]{getResources().getString(R.string.capture)
                , getResources().getString(R.string.recordVideo)
                , getResources().getString(R.string.recordAudio)};
//alert icons
        int[] alertIcons = new int[]{R.drawable.ic_photo_camera
                , R.drawable.ic_baseline_videocam_24
                , R.drawable.ic_baseline_record_voice_over_24};

        //row to store items
        List<HashMap<String, String>> hashMaps = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("icons", Integer.toString(alertIcons[i]));
            hm.put("actions", alertActions[i]);
            hashMaps.add(hm);


        }

        //keys for hash map
        String[] from = {"icons", "actions"};

        //ids of views
        int[] to = {R.id.imgAlertIcon
                , R.id.txtAlertAction};

        ListView alertListItems = popUpAlerterBottomSheetBinding.alertList;

        SimpleAdapter adapter = new SimpleAdapter(requireContext(), hashMaps, R.layout.layout_alert_items, from, to);

        alertListItems.setAdapter(adapter);
        alertListItems.setOnItemClickListener((parent, view1, position, id) -> {

            switch (position) {
                case 0:
                    DisplayViewUI.displayToast(getActivity(),"opening camera");
                    break;
                case 1:

                    DisplayViewUI.displayToast(getActivity(),"recording video");
                    break;

                case 2:
                    DisplayViewUI.displayToast(getActivity(),"recording audio");

                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + position);
            }


        });

        popUpAlerterBottomSheetBinding.btnCancel.setOnClickListener(v -> dismiss());

    }
}
