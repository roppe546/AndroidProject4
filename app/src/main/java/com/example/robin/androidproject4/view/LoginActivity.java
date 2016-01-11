package com.example.robin.androidproject4.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.example.robin.androidproject4.R;
import com.example.robin.androidproject4.model.Account;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private final int SIGN_IN_REQUEST_CODE = 500;

    private SharedPreferences pref;

    // Google Log-in
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get shared preferences
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SignInButton mEmailSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, null).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SIGN_IN_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SIGN_IN_REQUEST_CODE :
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
                break;
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.i("SignIn", "handleSignInResult: " + result.isSuccess());

        if (result.isSuccess()) {
            // Sign in succeeded
            GoogleSignInAccount account = result.getSignInAccount();
            Account.setAccount(account);

            // Add logged in user to shared preferences
            try {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("loggedInUser", account.getDisplayName());
                editor.putString("loggedInUserEmail", account.getEmail());
                editor.putString("loggedInUserId", account.getId());
                editor.putString("loggedInUserIdToken", account.getIdToken());
                editor.commit();
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }

            Intent main = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(main);

            finish();
        }
        else {
            // Sign in failed
            Toast.makeText(LoginActivity.this, "Could not sign in", Toast.LENGTH_LONG).show();
        }
    }
}

