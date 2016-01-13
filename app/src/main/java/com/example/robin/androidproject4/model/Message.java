package com.example.robin.androidproject4.model;

import android.net.Uri;

import java.util.Date;

/**
 * Created by robin on 7/1/16.
 */
public class Message {
    private int id;     // can be used if we want to implement message editing later
    private String sender;
    private Date timestamp;
    private String message;
    private Uri imageUri;


    /**
     * Constructor for messages without image attached.
     *
     * @param sender    sender of message
     * @param message   receiver of message
     */
    public Message(String sender, Date timestamp, String message) {
        this.sender = sender;
        this.timestamp = timestamp;
        this.message = message;
        this.imageUri = null;
    }

    /**
     * Constructor for messages with an image attached.
     *
     * @param sender    sender of message
     * @param message   receiver of message
     * @param image     image attached to message
     */
    public Message(String sender, Date timestamp, String message, Uri image) {
        this.sender = sender;
        this.timestamp = timestamp;
        this.message = message;
        this.imageUri = image;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public boolean hasImage() {
        if (imageUri == null)
            return false;

        return true;
    }
}
