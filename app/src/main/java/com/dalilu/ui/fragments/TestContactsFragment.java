package com.dalilu.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dalilu.MainActivity;
import com.dalilu.R;
import com.dalilu.adapters.TestSortedContactsAdapter;
import com.dalilu.databinding.FragmentTestContactsBinding;
import com.dalilu.model.Contact;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class TestContactsFragment extends Fragment {
    public static final String PREF_NAME = "key";
    public static final String KEY_MOBILE_NUMBER = "number";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHOTO = "photo";
    private static final String TAG = "TestContactsFragment";
    int PRIVATE_MODE = 0;
    ProgressDialog progressDialog;
    private SharedPreferences mPref;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;
    private RecyclerView mRecyclerView;
    private TestSortedContactsAdapter mAdapter;
    private ArrayList<Contact> mContacts;
    private FragmentTestContactsBinding fragmentTestContactsBinding;
    private CollectionReference usersCollectionReference;


    public TestContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentTestContactsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_contacts, container, false);
        return fragmentTestContactsBinding.getRoot();
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setMessage("loading");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        usersCollectionReference = FirebaseFirestore.getInstance().collection("Users");

        mPref = requireActivity().getApplicationContext().getSharedPreferences(PREF_NAME, PRIVATE_MODE);

        mRecyclerView = requireActivity().findViewById(R.id.contactsRv);

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else {

            new ContactLoader().execute();


        }

        mAdapter = new TestSortedContactsAdapter(getContext(), getLayoutInflater(null), null);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(requireActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


    }

    private void getContactsList() {


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Contacts permission Granted", Toast.LENGTH_SHORT).show();
                requireActivity().recreate();
            } else {
                Toast.makeText(getActivity(), "Contacts permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public boolean isContainsContact(List<Contact> list, String mobileNo) {
        for (Contact object : list) {
            if (object.getPhoneNumber().contains(mobileNo)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    class ContactLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //visible your progress bar here
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mContacts = new ArrayList<>();
            mContacts.add(new Contact(MainActivity.userName,
                    MainActivity.phoneNumber));

            ContentResolver cr = requireActivity().getContentResolver();
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                    Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);

                    assert phones != null;
                    while (phones.moveToNext()) {
                        String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s+", "");
                        final String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                        number = number.replace(" ", "");
                        number = number.replace("-", "");
                        number = number.replace("+", "");
                        number = number.replace(".", "");
                        number = number.replace(",", "");
                        number = number.replace("#", "");


                        String finalNumber = number;
                        usersCollectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {

                            for (DocumentSnapshot ds : queryDocumentSnapshots) {
                                Contact contact = ds.toObject(Contact.class);
                                String getPhoneNumberFromCloud = Objects.requireNonNull(contact).getPhoneNumber();

                                Log.i(TAG, "From cloud: " + getPhoneNumberFromCloud);

                                if (Objects.equals(getPhoneNumberFromCloud, finalNumber)) {

                                    //user exists
                                    if (!isContainsContact(mContacts, finalNumber)) {
                                        mAdapter.addItem(new Contact(name, getPhoneNumberFromCloud));
                                        mContacts.add(new Contact(name, getPhoneNumberFromCloud));
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            }

                        });

                    }
                    phones.close();
                }

        /*    usersCollectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {

                for (DocumentSnapshot ds : queryDocumentSnapshots){
                    Log.i(TAG, "From cloud: " + Objects.requireNonNull(ds.getData()).get("phoneNumber"));

                    if (Objects.equals(Objects.requireNonNull(ds.getData()).get("phoneNumber"), finalPhoneNumber)){

                        //user exists
                        if (!isContainsContact(mContacts, finalPhoneNumber)) {
                            mAdapter.addItem(new Contact(finalName, finalPhoneNumber));
                            mContacts.add(new Contact(finalName, finalPhoneNumber));
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }

            });*/

            }
            assert cursor != null;
            cursor.close();

            return null;

        }

        @SuppressLint("RestrictedApi")
        @Override
        protected void onPostExecute(Void Void) {
            super.onPostExecute(Void);
            //set list to your adapter
            progressDialog.dismiss();
            mRecyclerView.setAdapter(mAdapter);


        }
    }
}