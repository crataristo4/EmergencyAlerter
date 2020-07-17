package com.emergency.alerter.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.emergency.alerter.R;
import com.emergency.alerter.databinding.FragmentAlertsBinding;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;

public class AlertsFragment extends Fragment {

    FragmentAlertsBinding fragmentAlertsBinding;
    CameraView cameraView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentAlertsBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_alerts, container, false);

        return fragmentAlertsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraView = fragmentAlertsBinding.camera;

        cameraView.setLifecycleOwner(getViewLifecycleOwner());

        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);



                // Access the raw data if needed.
                byte[] data = result.getData();
                Log.i( "onPictureTaken: ", String.valueOf(data.length));
            }

            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                super.onVideoTaken(result);
            }
        });
        cameraView.takePicture();

    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.open();
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }
}