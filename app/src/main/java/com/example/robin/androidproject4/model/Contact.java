package com.example.robin.androidproject4.model;

import android.net.Uri;

/**
 * Created by robin on 6/1/16.
 */
public class Contact {
    private String username;
    private Uri profilePictureUri;

    public Contact(String username, Uri profilePictureUri) {
        this.username = username;
        this.profilePictureUri = profilePictureUri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Uri getProfilePictureUri() {
        return profilePictureUri;
    }
}
