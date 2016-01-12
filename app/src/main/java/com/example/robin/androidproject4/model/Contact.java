package com.example.robin.androidproject4.model;

import android.net.Uri;

import java.util.Date;

/**
 * Created by robin on 6/1/16.
 */
public class Contact {
    private String username;
    private Date lastReceivedTimestamp;
    private Uri profilePictureUri;

    public Contact(String username, Date lastReceivedTimestamp, Uri profilePictureUri) {
        this.username = username;
        this.lastReceivedTimestamp = lastReceivedTimestamp;
        this.profilePictureUri = profilePictureUri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getLastReceivedTimestamp() {
        return lastReceivedTimestamp;
    }

    public void setLastReceivedTimestamp(Date lastReceivedTimestamp) {
        this.lastReceivedTimestamp = lastReceivedTimestamp;
    }

    public Uri getProfilePictureUri() {
        return profilePictureUri;
    }
}
