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

public class MainActivity extends AppCompatActivity implements AddContactDialog.AddContactDialogListener {
    private SharedPreferences pref;

    private ArrayList<Contact> contacts;

    private ContactListAdapter contactListAdapter;
    private ListView contactList;

    // Google Log-in
    private GoogleApiClient mGoogleApiClient;

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

        // Check whether the user logged reaching this activity is logged
        // in or not. If not, close activity and go back to the login
        // activity.
        if (!checkIfLoggedIn()) {
            this.finish();
        }
    }


    /**
     * Inflates a menu into the action bar.
     *
     * @param menu  the menu which to inflate
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_contact_list, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Called when an item in the action menu was selected.
     *
     * @param item  the item that was selected
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.menu_add_contact :
                Log.i("ActionMenu", "Selected Add Contact in menu (main)");
                DialogFragment addContactDialog = new AddContactDialog();

                // Send logged in user as argument to fragment
                Bundle arguments = new Bundle();
                arguments.putString("loggedInUserEmail", pref.getString("loggedInUserEmail", null));
                addContactDialog.setArguments(arguments);

                addContactDialog.show(getFragmentManager(), "add_contact");
                break;
            case R.id.menu_settings :
                Log.i("ActionMenu", "Selected Settings in menu (main)");
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                break;
            case R.id.menu_logout :
                Log.i("ActionMenu", "Selected Log out in menu (main)");

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


    /**
     * This method is used by the Add Contact DialogFramgent when it finishes
     * to notify this activity whether the dialog succeeded or not.
     *
     * @param success   true or false whether dialog finished successfully
     */
    @Override
    public void onFinishAddContactDialog(boolean success) {
        if (success) {
            Log.i("ContactList", "Successfully added new contact, refreshing.");

            // Clear previous contacts and get fresh contact data
            ArrayList<Contact> temp = Communicator.getContactsRequest(pref.getString("loggedInUserEmail", null));
            if (temp != null) {
                contacts.clear();
                contacts.addAll(temp);
                contactListAdapter.notifyDataSetChanged();
            }
        }
    }


    /**
     * Signs out the user from Google. Clears local login data.
     */
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i("Login", "Logged out (main)");

                            // Set status offline
                            Communicator.putUserRequest(pref.getString("loggedInUserEmail", null), Account.getAccount().getPhotoUrl(), getString(R.string.STATUS_OFFLINE));

                            // Clear logged in user from shared preferences
                            SharedPreferences.Editor editor = pref.edit();
                            editor.clear();
                            editor.apply();

                            Account.setAccount(null);
                        }
                    }
                });
    }


    /**
     * Checks if a user is logged in. If the user is not logged in he/she is
     * redirected to the login activity.
     *
     * @return  true or false whether the user is logged in or not
     */
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


    /**
     * This class checks which of the contacts was selected in the list and
     * redirects to the activity which has the chat with that user.
     */
    private class ContactSelectedListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i("ContactList", "Contact " + contacts.get(position).getUsername() + " selected");
            Intent chat = new Intent(getApplicationContext(), ChatActivity.class);
            chat.putExtra("contactEmail", contacts.get(position).getUsername());
            startActivity(chat);
        }
    }
}
