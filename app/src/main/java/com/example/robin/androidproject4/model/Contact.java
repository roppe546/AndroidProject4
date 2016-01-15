package com.example.robin.androidproject4.model;

import android.net.Uri;


/**
 * Created by robin on 6/1/16.
 */
public class Contact {
    private String username;
    private String status;
    private Uri profilePictureUri;

    public Contact(String username, String status, Uri profilePictureUri) {
        this.username = username;
        this.status = status;
        this.profilePictureUri = profilePictureUri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Uri getProfilePictureUri() {
        return profilePictureUri;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "username='" + username + '\'' +
                ", status=" + status +
                ", profilePictureUri=" + profilePictureUri +
                '}';
    }
}
