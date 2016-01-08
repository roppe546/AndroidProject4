package com.example.robin.androidproject4.model;

import android.graphics.drawable.Drawable;

import java.util.Date;

/**
 * Created by robin on 7/1/16.
 */
public class Message {
    private String sender;
    private Date timestamp;
    private String message;
    // image variable in case message has an image
    private Drawable image;

    /**
     * Constructor for messages without image attached.
     *
     * @param sender    sender of message
     * @param message   receiver of message
     */
    public Message(String sender, String message) {
        this.sender = sender;
        this.timestamp = new Date();
        this.message = message;
        this.image = null;
    }

    /**
     * Constructor for messages with an image attached.
     *
     * @param sender    sender of message
     * @param message   receiver of message
     * @param image     image attached to message
     */
    public Message(String sender, String message, Drawable image) {
        this.sender = sender;
        this.timestamp = new Date();
        this.message = message;
        this.image = image;
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

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public boolean hasImage() {
        if (image == null)
            return false;

        return true;
    }
}
