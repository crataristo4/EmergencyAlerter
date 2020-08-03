package com.dalilu.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.MainActivity;
import com.dalilu.R;
import com.dalilu.adapters.RequestAdapter;
import com.dalilu.databinding.FragmentRequestBinding;
import com.dalilu.model.RequestModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class RequestFragment extends Fragment {

    private FragmentRequestBinding fragmentRequestBinding;
    private DatabaseReference requestDbRef;
    private RequestAdapter adapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentRequestBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_request, container, false);
        return fragmentRequestBinding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = fragmentRequestBinding.requestRecyclerView;
        rv.setHasFixedSize(true);

        requestDbRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(MainActivity.userId);
        Query query = requestDbRef.orderByChild("receiverId").equalTo(MainActivity.userId);

        FirebaseRecyclerOptions<RequestModel> options =
                new FirebaseRecyclerOptions.Builder<RequestModel>().setQuery(query,
                        RequestModel.class).build();

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RequestAdapter(options);
        rv.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}