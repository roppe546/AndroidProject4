package com.example.robin.androidproject4.model;

import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class parses json/xml data.
 *
 * Created by robin on 14/1/16.
 */
// TODO: Move all parsing code to here from Communicator.
public class XMLParser {
    public static Message getMessageFromXmlString(String string) {
        int id = -1;        // can be used if we want to implement message editing later
        String sender = null;
        Date timestamp = null;
        String message = null;
        Uri imageUri = null;
        Message newMessage = null;

        XmlPullParserFactory factory;

        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            InputStream input = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
            xpp.setInput(input, "UTF-8");

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
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
