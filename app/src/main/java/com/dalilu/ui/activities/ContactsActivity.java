package com.dalilu.ui.activities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.dalilu.R;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {
    private static final String TAG = "ContactsActivity";
    public static final ArrayList<String> names = new ArrayList<>();
    public static final ArrayList<String> phones = new ArrayList<>();
    ListView listView;
    ArrayList<String> contactsModels;
    ArrayAdapter<String> arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        listView = findViewById(R.id.contactsListView);
        contactsModels = new ArrayList<>();

        Log.i(TAG, "Phones: " + ContactsActivity.phones);
        Log.i(TAG, "Names: " + ContactsActivity.names);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsModels);
        listView.setAdapter(arrayAdapter);
        loadContactsFromPhone();


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


}