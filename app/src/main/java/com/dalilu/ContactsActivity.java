package com.dalilu;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {
    private static final String TAG = "ContactsActivity";
    public static ArrayList<String> names = new ArrayList<>();
    public static ArrayList<String> phones = new ArrayList<>();
    static ListView listView;
    ArrayList<String> contactsModels;
    static ArrayAdapter<String> arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        listView = findViewById(R.id.contactsListView);
        contactsModels = new ArrayList<>();

        Log.i(TAG, "Phones: " + ContactsActivity.phones);
        Log.i(TAG, "Names: " + ContactsActivity.names);
        // arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsModels);
        // listView.setAdapter(arrayAdapter);
        //loadContactsFromPhone();

        new ContactLoader().execute();


    }


    private void loadContactsFromPhone() {
        runOnUiThread(() -> {
            ContentResolver cr = getContentResolver();
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    //
                    //  Get all phone numbers.
                    //
                    Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);

                    assert phones != null;
                    while (phones.moveToNext()) {
                        String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s+", "");
                        String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        if (!ContactsActivity.phones.contains(number)) {
                            ContactsActivity.names.add(name);
                            ContactsActivity.phones.add(number);
                        }

                        contactsModels.add(name);
                    }
                    phones.close();
                }
            }
            assert cursor != null;
            cursor.close();
        });


    }


    @SuppressWarnings("deprecation")
    static class ContactLoader extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //visible your progress bar here
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> contacts = new ArrayList<>();
            String order = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
            try (Cursor cursor = Dalilu.getDaliluAppContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, order)) {
                assert cursor != null;
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contacts.add(phonenumber);
                }
                cursor.close();
            }
            return contacts;
        }

        @Override
        protected void onPostExecute(ArrayList<String> contactList) {
            super.onPostExecute(contactList);
            //set list to your adapter
            arrayAdapter = new ArrayAdapter<>(Dalilu.getDaliluAppContext(), android.R.layout.simple_list_item_1, contactList);

            listView.setAdapter(arrayAdapter);
        }
    }


}