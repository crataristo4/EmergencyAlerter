package com.dalilu.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.dalilu.R;
import com.dalilu.databinding.FragmentAlertsBinding;


public class AlertsFragment extends Fragment {

    FragmentAlertsBinding fragmentAlertsBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentAlertsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_alerts, container, false);

        return fragmentAlertsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
}