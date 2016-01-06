package com.example.robin.androidproject4.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.robin.androidproject4.R;
import com.example.robin.androidproject4.model.Contact;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // TODO: Use real instead of dummy data
    private ArrayList<Contact> DUMMY_CONTACTS;

    private SharedPreferences pref;

    private ContactListAdapter contactListAdapter;
    private ListView contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get shared preferences
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        checkIfLoggedIn();

        // Get UI elements
        contactList = (ListView) findViewById(R.id.contactListView);

        // Populate list view
        // TODO: Remove dummy data
        DUMMY_CONTACTS = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            DUMMY_CONTACTS.add(new Contact("Contact " + (i + 1)));
        }
        contactListAdapter = new ContactListAdapter(this, DUMMY_CONTACTS);
        contactList.setAdapter(contactListAdapter);
        contactList.setOnItemClickListener(new ContactSelectedListener());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.menu_settings :
                Log.i("ActionMenu", "Selected Settings in menu (main)");
//                startActivityForResult(new Intent(this, SettingsActivity.class), 200);
                break;
            case R.id.menu_logout :
                Log.i("ActionMenu", "Selected Log out in menu (main)");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkIfLoggedIn() {
        String loggedInUser = pref.getString("loggedInUser", null);

        if (loggedInUser == null) {
            Log.i("Login", "Not logged in, redirect to login activity");
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        }
    }

    private class ContactSelectedListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i("ContactList", "Contact " + position + " selected");
            Intent chat = new Intent(getApplicationContext(), ChatActivity.class);
            startActivity(chat);
        }
    }
}