package com.example.robin.androidproject4.view;

import android.app.DialogFragment;
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
import com.example.robin.androidproject4.model.Account;
import com.example.robin.androidproject4.model.Communicator;
import com.example.robin.androidproject4.model.Contact;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences pref;

    private ArrayList<Contact> contacts;

    private ContactListAdapter contactListAdapter;
    private ListView contactList;

    // Google Log-in
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get shared preferences
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (checkIfLoggedIn()) {
            // Get UI elements
            contactList = (ListView) findViewById(R.id.contactListView);

            // Populate list view
            contacts = new ArrayList<>();
            contacts = Communicator.getContactsRequest(pref.getString("loggedInUserEmail", null));

            contactListAdapter = new ContactListAdapter(this, contacts);
            contactList.setAdapter(contactListAdapter);
            contactList.setOnItemClickListener(new ContactSelectedListener());

            // Configure Google Sign-In
            mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, null).addApi(Auth.GOOGLE_SIGN_IN_API).build();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkIfLoggedIn()) {
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_contact_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.menu_add_contact :
                Log.i("ActionMenu", "Selected Add Contact in menu (main)");
                DialogFragment addContactDialog = new AddContactDialog();
                addContactDialog.show(getFragmentManager(), "add_contact");
                break;
            case R.id.menu_settings :
                Log.i("ActionMenu", "Selected Settings in menu (main)");
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                break;
            case R.id.menu_logout :
                Log.i("ActionMenu", "Selected Log out in menu (main)");

                // Sign out from Google
                signOut();

                // Send back to login activity
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i("Login", "Logged out (main)");

                            // Clear logged in user from shared preferences
                            SharedPreferences.Editor editor = pref.edit();
                            editor.clear();
                            editor.apply();

                            Account.setAccount(null);
                        }
                    }
                });
    }

    private boolean checkIfLoggedIn() {
        try {
            Account.getAccount().getId();
        }
        catch (NullPointerException e) {
            Log.i("Login", "Not logged in, redirect to login activity");

            // Clear logged in user from shared preferences
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();

            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
            this.finish();

            return false;
        }

        return true;
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
