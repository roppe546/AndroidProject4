package com.example.robin.androidproject4;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get shared preferences
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        checkIfLoggedIn();

        setContentView(R.layout.activity_main);
    }

    private void checkIfLoggedIn() {
        String loggedInUser = pref.getString("loggedInUser", null);

        if (loggedInUser == null) {
            Log.i("Login", "Not logged in, redirect to login activity");
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        }
    }
}
