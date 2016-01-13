package com.example.robin.androidproject4.model;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Created by robin on 12/1/16.
 */
public class Communicator {
    private static final String API_ENDPOINT = "http://192.168.0.11:60630/api/";

    public static ArrayList<Contact> getContactsRequest(String userEmail) {
        try {
            return new doGetContactsRequest().execute(API_ENDPOINT + "users?email=" + userEmail).get();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean addNewContactRequest(String userEmail, String emailBeingAdded) {
        try {
            return new doPostNewContactRequest().execute(API_ENDPOINT + "friends", userEmail, emailBeingAdded).get();
        }
        catch (Exception e) {
            return false;
        }
    }

    public static ArrayList<Message> getChatHistoryRequest(String userEmail, String contactEmail ) {
        try {
            return new doGetChatHistoryRequest().execute(API_ENDPOINT + "messages?from=" + userEmail + "&to=" + contactEmail).get();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    // TODO: Should handle images as well
    public static boolean addNewMessageRequest(String from, String to, String message) {

        try {
            return new doPostNewMessageRequest().execute(API_ENDPOINT + "messages", from, to, message).get();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class doGetContactsRequest extends AsyncTask<String, Void, ArrayList<Contact>> {

        @Override
        protected ArrayList<Contact> doInBackground(String... params) {
            ArrayList<Contact> contacts = new ArrayList<>();

            XmlPullParserFactory factory;
            HttpURLConnection http = null;
            InputStream input;

            try {
                factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                URL url = new URL(params[0]);

                // create connection
                http = (HttpURLConnection) url.openConnection();
                http.addRequestProperty("Content-Type", "text/xml; charset=utf-8");

                int responseCode = http.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    input = http.getInputStream();
                    xpp.setInput(input, null);

                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT :
                                Log.i("PARSE", "Start document");
                                break;
                            case XmlPullParser.START_TAG:
                                if (xpp.getName().equals("FriendDTO")) {
                                    Log.i("PARSE", "FriendDTO tag found");
                                    contacts.add(getContact(xpp));
                                }
                                break;
                        }

                        eventType = xpp.next();
                    }

                    Log.i("PARSE", "End document");
                }
            }
            catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (http != null) {
                    http.disconnect();
                }
            }

            return contacts;
        }

        private Contact getContact(XmlPullParser xpp) {
            String email = null;
            Uri imageUri = null;
            Date lastReceivedDate = null;
            Contact newContact = null;

            try {
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_TAG) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (xpp.getName().equals("Email")) {
                                if (xpp.next() == XmlPullParser.TEXT) {
                                    Log.i("PARSE", "Email tag found in Friend: " + xpp.getText());
                                    email = xpp.getText();
                                    xpp.nextTag();
                                }
                            }
                            if (xpp.getName().equals("ImageUrl")) {
                                if (xpp.next() == XmlPullParser.TEXT) {
                                    Log.i("PARSE", "ImageUrl tag found in Friend: " + xpp.getText());
                                    imageUri = Uri.parse(xpp.getText());
                                    xpp.nextTag();
                                }
                            }
                            if (xpp.getName().equals("LastReceivedMessage")) {
                                if (xpp.next() == XmlPullParser.TEXT) {
                                    Log.i("PARSE", "LastReceivedMessage tag found in Friend: " + xpp.getText());

                                    try {
                                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        lastReceivedDate = format.parse(xpp.getText());
                                    }
                                    catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    xpp.nextTag();
                                }
                            }
                            break;
                    }

                    eventType = xpp.next();
                }

                Log.i("PARSE", "Creating new contact object");
                newContact = new Contact(email, lastReceivedDate, imageUri);
                Log.i("PARSE", "End friend");
            }
            catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return newContact;
        }
    }

    private static class doPostNewContactRequest extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            HttpURLConnection http = null;

            try {
                // Put request in JSON
                JSONObject data = new JSONObject();
                String userEmail = params[1];
                String emailBeingAdded = params[2];
                data.put("From", userEmail);
                data.put("To", emailBeingAdded);

                if (!(data.length() > 0)) {
                    // JSON object empty, return
                    return false;
                }

                // Create connection
                URL url = new URL(params[0]);
                http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");
                http.setRequestProperty("Content-Type", "application/json");
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setUseCaches(false);
                http.connect();

                // Send data to back end
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(http.getOutputStream(), "UTF-8"));
                writer.write(String.valueOf(data));
                writer.close();

                // Receive result
                if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i("Communicator", "Contact was successfully added in back end");
                    return true;
                }
                else {
                    Log.i("Communicator", "Contact was NOT added successfully added in back end");
                    return false;
                }
            }
            catch (Exception e) {
                // Could not add contact, returning
                e.printStackTrace();
                return false;
            }
            finally {
                if (http != null) {
                    http.disconnect();
                }
            }
        }
    }

    private static class doGetChatHistoryRequest extends AsyncTask<String, Void, ArrayList<Message>> {

        @Override
        protected ArrayList<Message> doInBackground(String... params) {
            ArrayList<Message> messages = new ArrayList<>();

            XmlPullParserFactory factory;
            HttpURLConnection http = null;
            InputStream input;

            try {
                factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                URL url = new URL(params[0]);

                // create connection
                http = (HttpURLConnection) url.openConnection();
                http.addRequestProperty("Content-Type", "text/xml; charset=utf-8");

                int responseCode = http.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    input = http.getInputStream();
                    xpp.setInput(input, null);

                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT :
                                Log.i("PARSE", "Start document");
                                break;
                            case XmlPullParser.START_TAG:
                                if (xpp.getName().equals("MessageDTO")) {
                                    Log.i("PARSE", "MessageDTO tag found");

                                    messages.add(getMessage(xpp));
                                }
                                break;
                        }

                        eventType = xpp.next();
                    }

                    Log.i("PARSE", "End document");
                }
            }
            catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (http != null) {
                    http.disconnect();
                }
            }

            return messages;
        }

        private Message getMessage(XmlPullParser xpp) {
            int id = -1;        // can be used if we want to implement message editing later
            String sender = null;
            Date timestamp = null;
            String message = null;
            Uri imageUri = null;
            Message newMessage = null;

            try {
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_TAG) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (xpp.getName().equals("Id")) {
                                if (xpp.next() == XmlPullParser.TEXT) {
                                    Log.i("PARSE", "Id tag found in Message: " + xpp.getText());
                                    id = Integer.parseInt(xpp.getText());
                                    xpp.nextTag();
                                }
                            }
                            if (xpp.getName().equals("Email")) {
                                if (xpp.next() == XmlPullParser.TEXT) {
                                    Log.i("PARSE", "Email tag found in Message: " + xpp.getText());
                                    sender = xpp.getText();
                                    xpp.nextTag();
                                }
                            }
                            if (xpp.getName().equals("Image")) {
                                if (xpp.next() == XmlPullParser.TEXT) {
                                    Log.i("PARSE", "Image tag found in Message: " + xpp.getText());

                                    if (xpp.getText().compareTo("") != 0) {
                                        // String in image uri field
                                        imageUri = Uri.parse(xpp.getText());
                                    }
                                    xpp.nextTag();
                                }
                            }
                            if (xpp.getName().equals("Text")) {
                                if (xpp.next() == XmlPullParser.TEXT) {
                                    Log.i("PARSE", "Text tag found in Message: " + xpp.getText());
                                    message = xpp.getText();
                                    xpp.nextTag();
                                }
                            }
                            if (xpp.getName().equals("Timestamp")) {
                                if (xpp.next() == XmlPullParser.TEXT) {
                                    Log.i("PARSE", "Timestamp tag found in Message: " + xpp.getText());

                                    try {
                                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        timestamp = format.parse(xpp.getText());
                                    }
                                    catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    xpp.nextTag();
                                }
                            }
                            break;
                    }

                    eventType = xpp.next();
                }

                Log.i("PARSE", "Creating new message object");
                if (imageUri == null) {
                    // No image attached
                    newMessage = new Message(sender, timestamp, message);
                }
                else {
                    // Image attached
                    newMessage = new Message(sender, timestamp, message, imageUri);
                }

                Log.i("PARSE", "End message");
            }
            catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return newMessage;
        }
    }

    private static class doPostNewMessageRequest extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection http = null;

            try {
                // Put request in JSON
                JSONObject data = new JSONObject();
                String from = params[1];
                String to = params[2];
                String message = params[3];
                data.put("Sender", from);
                data.put("Receiver", to);
                data.put("Text", message);
                // TODO: Use image data
                data.put("Image", "");

                if (!(data.length() > 0)) {
                    // JSON object empty, return
                    return false;
                }

                // Create connection
                URL url = new URL(params[0]);
                http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");
                http.setRequestProperty("Content-Type", "application/json");
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setUseCaches(false);
                http.connect();

                // Send data to back end
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(http.getOutputStream(), "UTF-8"));
                writer.write(String.valueOf(data));
                writer.close();

                // Receive result
                if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i("Communicator", "Message was successfully sent to back end");
                    return true;
                }
                else {
                    Log.i("Communicator", "Message was NOT added successfully sent to back end");
                    return false;
                }
            }
            catch (Exception e) {
                // Could not send message, returning
                e.printStackTrace();
                return false;
            }
            finally {
                if (http != null) {
                    http.disconnect();
                }
            }
        }
    }
}
