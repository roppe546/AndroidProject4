package com.example.robin.androidproject4.model;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * This class holds the account of the currently signed in user.
 *
 * Created by robin on 11/1/16.
 */
public class Account {
    private static GoogleSignInAccount account;

    public static GoogleSignInAccount getAccount() {
        return account;
    }

    public static void setAccount(GoogleSignInAccount googleSignInAccount) {
        account = googleSignInAccount;
    }
}
