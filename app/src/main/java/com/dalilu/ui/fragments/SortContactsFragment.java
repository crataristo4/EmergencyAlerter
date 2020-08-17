package com.dalilu.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.dalilu.adapters.SortContactsAdapter;
import com.dalilu.databinding.FragmentSortContactsBinding;
import com.dalilu.model.Contact;
import com.dalilu.utils.DisplayViewUI;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SortContactsFragment extends Fragment {
    public static final String PREF_NAME = "key";
    public static final String KEY_MOBILE_NUMBER = "number";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHOTO = "photo";
    private static final String TAG = "TestContactsFragment";
    ProgressBar progressDialog;
    private RecyclerView mRecyclerView;
    ContactLoader contactLoader;
    private ArrayList<Contact> mContacts;
    private SortContactsAdapter mAdapter;
    private CollectionReference usersCollectionReference;
    private FragmentSortContactsBinding fragmentSortContactsBinding;


    public SortContactsFragment() {
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
        fragmentSortContactsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sort_contacts, container, false);
        return fragmentSortContactsBinding.getRoot();
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        progressDialog = fragmentSortContactsBinding.progressLoading;
        usersCollectionReference = FirebaseFirestore.getInstance().collection("Users");

        mRecyclerView = requireActivity().findViewById(R.id.contactsRv);
        contactLoader = new ContactLoader();

        mAdapter = new SortContactsAdapter(getContext(), getLayoutInflater(null), null);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(requireActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        fragmentSortContactsBinding.imgRefresh.setOnClickListener(view -> {

            if (progressDialog.getVisibility() == View.GONE) {
//                contactLoader.execute();
                progressDialog.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> progressDialog.setVisibility(View.GONE), 2000);

                mAdapter.notifyDataSetChanged();
            } else {
                progressDialog.setVisibility(View.GONE);
            }
        });

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else {
            contactLoader.execute();

        }

        mAdapter.setOnItemClickListener(new SortContactsAdapter.onItemClickListener() {
            @Override
            public void onClick(RecyclerView.ViewHolder vh, Contact contact, int position) {
                String name = contact.getUserName();
                String phone = contact.getPhoneNumber();
                DisplayViewUI.displayToast(requireActivity(), "nAME : " + name + " phone : " + phone);

            }
        });
  /*   ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener((recyclerView, position, v) -> {

            SortContactsViewHolder holder = (SortContactsViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
            assert holder != null;
            String name = holder.layoutTestContactsBinding.txtName.getText().toString();
            DisplayViewUI.displayToast(requireActivity(),"Send location to: " + name);



        });*/

/*
                      locationDbRef.child(senderId).child(locationDbId).setValue(fromUser).addOnCompleteListener(task -> {
                          if (task.isSuccessful()) {
                              progressBar.dismiss();
                              DisplayViewUI.displayToast(requireActivity(), getString(R.string.successFull));
                              locationDbRef.child(getUserId).child(locationDbId).setValue(toReceiver);

                              SharedPreferences.Editor shareLocationEditor = pref.edit();
                              shareLocationEditor.putBoolean(AppConstants.IS_LOCATION_SHARED, true);
                              shareLocationEditor.apply();


                          } else {
                              progressBar.dismiss();
                              DisplayViewUI.displayToast(requireActivity(), Objects.requireNonNull(task.getException()).getMessage());

                          }
                      });
*//*



                            } else if (i == -2) {
                                dialogInterface.dismiss();


                            }
                        });


            });

        });
*/


    }


    // TODO: 8/16/2020  fix results from here
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(getActivity(), "Contacts permission Granted", Toast.LENGTH_SHORT).show();
                requireActivity().recreate();

            } else {
                Toast.makeText(getActivity(), "Contacts permission Denied", Toast.LENGTH_SHORT).show();
            }
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
            progressDialog.setVisibility(View.VISIBLE);
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
                                String getPhotoUrl = Objects.requireNonNull(contact).getUserPhotoUrl();

                                Log.i(TAG, "From cloud: " + getPhoneNumberFromCloud);

                                if (Objects.equals(getPhoneNumberFromCloud, finalNumber)) {

                                    //user exists
                                    if (!isContainsContact(mContacts, finalNumber)) {
                                        mAdapter.addItem(new Contact(name, getPhoneNumberFromCloud, getPhotoUrl));
                                        mContacts.add(new Contact(name, getPhoneNumberFromCloud, getPhotoUrl));
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            }

                        });

                    }
                    phones.close();
                }

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
            progressDialog.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();


        }
    }
}