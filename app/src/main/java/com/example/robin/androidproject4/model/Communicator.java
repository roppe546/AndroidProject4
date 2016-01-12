package com.example.robin.androidproject4.model;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
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
    public static ArrayList<Contact> makeGetRequest(String urlString) {
        try {
            return new doGetContactsRequest().execute(urlString).get();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class doGetContactsRequest extends AsyncTask<String, Void, ArrayList<Contact>> {
        @Override
        protected ArrayList<Contact> doInBackground(String... params) {
            ArrayList<Contact> contacts = new ArrayList<>();

            XmlPullParserFactory factory;
            HttpURLConnection http;
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
}
