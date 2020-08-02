package com.dalilu.ui.contacts;

import android.os.Bundle;
import android.util.Log;
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
import com.dalilu.databinding.FragmentContactsBinding;
import com.dalilu.model.RequestModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";
    private static final int INITIAL_LOAD = 15;
    private FragmentContactsBinding fragmentContactsBinding;
    private DatabaseReference requestDbRef;
    private RequestAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentContactsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false);
        return fragmentContactsBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String senderId = MainActivity.userId;

        Log.i(TAG, "Sender id: " + senderId);

        RecyclerView rv = fragmentContactsBinding.contactsRecyclerView;
        rv.setHasFixedSize(true);

        requestDbRef = FirebaseDatabase.getInstance().getReference().child("Friends").child("90JgpRJ50oS733JZjIpV85Tmkuf1");

        Query query = requestDbRef.orderByChild("senderId").equalTo(senderId);
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