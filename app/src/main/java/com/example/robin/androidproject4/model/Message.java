package com.example.robin.androidproject4.model;

import java.util.Date;

/**
 * Created by robin on 7/1/16.
 */
public class Message {
    private String sender;
    private Date timestamp;
    private String message;
    // image variable in case user choose an image

    public Message(String sender, String message) {
        this.sender = sender;
        this.message = message;
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
}
