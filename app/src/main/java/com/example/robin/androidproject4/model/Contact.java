package com.example.robin.androidproject4.model;

import android.graphics.drawable.Drawable;

/**
 * Created by robin on 6/1/16.
 */
public class Contact {
    private String username;
    private Drawable profilePicture;

    public Contact(String username, Drawable profilePicture) {
        this.username = username;
        this.profilePicture = profilePicture;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Drawable getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Drawable profilePicture) {
        this.profilePicture = profilePicture;
    }
}
