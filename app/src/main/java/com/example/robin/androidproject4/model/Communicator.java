package com.example.robin.androidproject4.model;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * This does all the communication with the back end servers.
 *
 * Created by robin on 12/1/16.
 */
public class Communicator {
    private static final String API_ENDPOINT = "http://192.168.0.11:60630/api/";

    public static boolean registerUser(String userEmail, Uri photoUri) {
        try {
            if (photoUri == null) {
                // No profile photo
                return new doPostNewUserRequest().execute(API_ENDPOINT + "users", userEmail, "").get();
            }
            else {
                // Photo exists
                return new doPostNewUserRequest().execute(API_ENDPOINT + "users", userEmail, photoUri.toString()).get();
            }
        }
        catch (Exception e) {
            return false;
        }
    }

    public static boolean putUserRequest(String userEmail, Uri photoUri, String status) {
        try {
            if (photoUri == null) {
                return new doPutUserRequest().execute(API_ENDPOINT + "users", userEmail, "", status).get();
            }
            else {
                return new doPutUserRequest().execute(API_ENDPOINT + "users", userEmail, photoUri.toString(), status).get();
            }
        }
        catch (Exception e) {
            return false;
        }
    }

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

    /**
     * Send a message to the specified user.
     *
     * @param from          who sends the message
     * @param to            to whom the message is sent
     * @param message       the message itself
     * @param imageUri      uri to image if there is one, use null if not image is attached
     * @return              true or false whether it succeeded
     */
    public static boolean addNewMessageRequest(String from, String to, String message, Uri imageUri) {

        try {
            new doPostNewMessageRequest(imageUri).execute(API_ENDPOINT + "messages", from, to, message);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static class doPostNewUserRequest extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection http = null;

            try {
                // Put request in JSON
                JSONObject data = new JSONObject();
                String email = params[1];
                String photoUri = params[2];
                data.put("Email", email);
                data.put("ImageUrl", photoUri);

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
                    Log.i("Communicator", "User was successfully added in back end");
                    return true;
                }
                else {
                    Log.i("Communicator", "User was NOT successfully added in back end");
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

    private static class doPutUserRequest extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection http = null;

            try {
                // Put request in JSON
                JSONObject data = new JSONObject();
                String email = params[1];
                String imageUrl = params[2];
                String status = params[3];
                data.put("Email", email);
                data.put("ImageUrl", imageUrl);
                data.put("Status", status);

                if (!(data.length() > 0)) {
                    // JSON object empty, return
                    return false;
                }

                // Create connection
                URL url = new URL(params[0]);
                http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("PUT");
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
                    Log.i("Communicator", "User status was successfully changed in back end");
                    return true;
                }
                else {
                    Log.i("Communicator", "User status was NOT successfully changed in back end");
                    return false;
                }
            }
            catch (Exception e) {
                // Could not change status, returning
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
            String status = null;
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
                            if (xpp.getName().equals("Status")) {
                                if (xpp.next() == XmlPullParser.TEXT) {
                                    Log.i("PARSE", "Status tag found in Friend: " + xpp.getText());
                                    status = xpp.getText();
                                    xpp.nextTag();
                                }
                            }
                            break;
                    }

                    eventType = xpp.next();
                }

                Log.i("PARSE", "End friend");
                Log.i("PARSE", "Creating new contact object");
                newContact = new Contact(email, status, imageUri);
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
                    Log.i("Communicator", "Contact was NOT successfully added in back end");
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
        Uri imageUri = null;

        public doPostNewMessageRequest(Uri uriToImageLocal) {
            this.imageUri = uriToImageLocal;
        }

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

                if (imageUri != null) {
                    Log.i("Communicator", "Param 4 (imageUri) NOT null");
                    String url = uploadImage(API_ENDPOINT + "upload");
                    Log.i("Communicator", "Url returned from uploadImage: " + url);
                    data.put("Image", url);
                }
                else {
                    Log.i("Communicator", "Param 4 (imageUri) IS null");
                    data.put("Image", "");
                }

                if (!(data.length() > 0)) {
                    // JSON object empty, return
                    return false;
                }

                Log.i("Communicator", "JSON object sent: " + data.toString());

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
                int responseCode = http.getResponseCode();
                Log.i("Communicator", "Send message to server response code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.i("Communicator", "Message was successfully sent to back end");
                    return true;
                }
                else {
                    Log.i("Communicator", "Message was NOT successfully sent to back end");
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

        private String uploadImage(String uploadUri) {
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            int bytesRead;
            int bytesAvailable;
            int bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;

            String blobUrl = "";

            HttpURLConnection http;
            DataOutputStream output = null;
            InputStream input;
            FileInputStream fileInput = null;

            try {
                fileInput = new FileInputStream(new File(imageUri.getPath()));

                // Create connection
                URL url = new URL(uploadUri);
                http = (HttpURLConnection) url.openConnection();
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setUseCaches(false);
                http.setRequestMethod("POST");
                http.setRequestProperty("Connection", "Keep-Alive");
                http.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                // Create output stream
                output = new DataOutputStream(http.getOutputStream());

                // Send file to server
                output.writeBytes(twoHyphens + boundary + lineEnd);
                output.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + imageUri.getPath() + "\"" + lineEnd);
                output.writeBytes(lineEnd);

                bytesAvailable = fileInput.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read from file
                bytesRead = fileInput.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    output.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInput.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInput.read(buffer, 0, bufferSize);
                }

                output.writeBytes(lineEnd);
                output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Read response
                int responseCode = http.getResponseCode();
                Log.i("Communicator", "uploadImage: response code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.i("Communicator", "uploadImage: Got http OK back");
                    input = http.getInputStream();

                    // Copy data from input stream into string
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(input, writer, "UTF-8");
                    String str = writer.toString();

                    // Split screen and get part which has uri
                    String[] parts = str.split("\"");
                    blobUrl = parts[3];
                }

                fileInput.close();
                output.flush();
                output.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (fileInput != null)
                        fileInput.close();
                    if (output != null) {
                        output.flush();
                        output.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Log.i("PARSE", "uploadImage returning: " + blobUrl);
            return blobUrl;
        }
    }
}
