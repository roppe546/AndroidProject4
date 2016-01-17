package com.example.robin.androidproject4.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.robin.androidproject4.R;
import com.example.robin.androidproject4.model.Account;
import com.example.robin.androidproject4.model.Communicator;
import com.example.robin.androidproject4.model.Message;
import com.example.robin.androidproject4.model.WifiChangeBroadcastReceiver;
import com.example.robin.androidproject4.model.XMLParser;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {
    // Request codes
    private final int CAPTURE_IMAGE_REQUEST_CODE = 1000;
    private final int CHOOSE_IMAGE_FROM_ALBUM_REQUEST_CODE = 1005;

    // UI elements and adapters
    private ListView chatHistory;
    private ChatHistoryAdapter chatHistoryAdapter;
    private EditText textField;
    private ImageButton cameraButton;
    private ImageButton galleryButton;
    private ImageButton deleteAttachmentButton;
    private ImageButton sendButton;

    private ArrayList<Message> history;
    private SharedPreferences pref;

    // Google Log-in
    private GoogleApiClient mGoogleApiClient;

    // Variables used for sending/receiving data
    private String loggedInUserEmail;
    private String contactEmail;
    private Thread subscribeThread;
    private Uri selectedImageUri = null;
    private Uri selectedImageRealPath = null;
    private final String BROKER_CONNECTION_URI = "amqp://vxoqwope:CQyPw9I5N8Hn70MjvQu0dd9lwcdnJZA0@spotted-monkey.rmq.cloudamqp.com/vxoqwope";
    private ConnectionFactory factory = new ConnectionFactory();

    private WifiChangeBroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Add back button to the action menu
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get shared preferences
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        // Save contact email from intent data
        loggedInUserEmail = pref.getString("loggedInUserEmail", null);
        contactEmail = getIntent().getStringExtra("contactEmail");

        // Get UI elements
        chatHistory = (ListView) findViewById(R.id.history_list_view);
        textField = (EditText) findViewById(R.id.textfield);
        cameraButton = (ImageButton) findViewById(R.id.camera_button);
        galleryButton = (ImageButton) findViewById(R.id.gallery_button);
        deleteAttachmentButton = (ImageButton) findViewById(R.id.delete_attachment_button);
        sendButton = (ImageButton) findViewById(R.id.send_button);

        // Set button listeners
        cameraButton.setOnClickListener(new CameraButtonClickListener());
        galleryButton.setOnClickListener(new GalleryButtonClickListener());
        deleteAttachmentButton.setOnClickListener(new DeleteAttachmentButtonClickListener());
        sendButton.setOnClickListener(new SendButtonClickListener());

        // See whether image was attached form earlier (i.e. before rotating)
        if (savedInstanceState != null && savedInstanceState.getString("selectedImageUri") != null) {
            // Image was attached from before, save uri and set buttons
            selectedImageUri = Uri.parse(savedInstanceState.getString("selectedImageUri"));
            changeUiToAttachmentAdded();
        }
        else {
            // No image was attached from before
            changeUiToAttachmentRemoved();
        }

        // Populate chat history
        history = new ArrayList<>();
        history = Communicator.getChatHistoryRequest(loggedInUserEmail, contactEmail);

        chatHistoryAdapter = new ChatHistoryAdapter(this, history);
        chatHistory.setAdapter(chatHistoryAdapter);
        chatHistory.setOnItemClickListener(new ChatMessageClickedListener());

        // Scroll to bottom of chat
        chatHistory.setSelection(chatHistory.getCount() - 1);

        // Configure Google Sign-In
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, null).addApi(Auth.GOOGLE_SIGN_IN_API).build();

        // Connect to RabbitMQ and subscribe to queue
        setupConnectionFactory();
        subscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register broadcast receiver
        broadcastReceiver = new WifiChangeBroadcastReceiver();

        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        Log.i("BroadcastReceiver", "Registering WifiChangeBroadCastReceiver");
        registerReceiver(broadcastReceiver, i);
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Unregister broadcast receiver
        Log.i("BroadcastReceiver", "Unregistering WifiChangeBroadCastReceiver");
        unregisterReceiver(broadcastReceiver);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close subscription thread
        subscribeThread.interrupt();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("State", "Saving state data");

        if (selectedImageUri != null) {
            outState.putString("selectedImageUri", selectedImageUri.toString());
            Log.i("State", "Saved selectedImageUri: " + selectedImageUri.toString());
        }
    }


    /**
     * Create an action menu for the view
     *
     * @param menu  the menu which to add elements to
     * @return      true/false
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Handles clicks to option menu items.
     *
     * @param item  the item in the action menu that was clicked
     * @return      true/false whether action succeeded or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.menu_settings :
                Log.i("ActionMenu", "Selected Settings in menu (chat)");
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                break;
            case R.id.menu_logout :
                Log.i("ActionMenu", "Selected Log out in menu (main)");

                signOut();

                // Send back to login activity
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
                finish();
            case android.R.id.home:
                Log.i("ActionMenu", "Selected back arrow in menu (chat)");
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Signs out the user from Google. Clears local login data.
     */
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i("Login", "Logged out (main)");

                            // Set status offline
                            Communicator.putUserRequest(loggedInUserEmail, Account.getAccount().getPhotoUrl(), getString(R.string.STATUS_OFFLINE));

                            // Clear logged in user from shared preferences
                            SharedPreferences.Editor editor = pref.edit();
                            editor.clear();
                            editor.apply();

                            Account.setAccount(null);
                        }
                    }
                });
    }


    /**
     * Class used for handling clicks to the camera button.
     *
     * http://stackoverflow.com/questions/6448856/android-camera-intent-how-to-get-full-sized-photo
     */
    private class CameraButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i("Chat", "Camera button clicked");

            File photo = null;
            try {
                photo = createTempFile("picture", ".jpg");
                photo.delete();
            }
            catch(Exception e) {
                Log.i("Chat", "Can't create temporary file to store image in");
                Toast.makeText(getApplicationContext(), getText(R.string.chat_error_cant_take_picture), Toast.LENGTH_LONG);
            }

            if (photo != null) {
                // Get uri for temp file
                selectedImageUri = Uri.fromFile(photo);
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Put extra information for intent so it saves image taken in location where
                // selectedImageUri points
                i.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
                startActivityForResult(i, CAPTURE_IMAGE_REQUEST_CODE);
            }
        }
    }


    /**
     * This method creates a temporary file where images taken with the camera
     * are saved.
     *
     * @param part  prefix for image name
     * @param ext   extension type of the image
     * @return      file where image can be stored
     * @throws Exception
     */
    private File createTempFile(String part, String ext) throws Exception {
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath()+"/.temp/");

        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        return File.createTempFile(part, ext, tempDir);
    }


    /**
     * Class used for handling clicks to the gallery button.
     */
    private class GalleryButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i("Chat", "Gallery button clicked");

            Intent picImage = Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), getString(R.string.chat_choose_image_text));
            startActivityForResult(picImage, CHOOSE_IMAGE_FROM_ALBUM_REQUEST_CODE);
        }
    }


    /**
     * Callback method which is called when either was captured from camera
     * or an image was chosen from the gallery.
     *
     * @param requestCode   the request code which was used
     * @param resultCode    whether the action finished successfully
     * @param data          data that was returned with the result, in this case picture data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("Chat", "Result from activity with request code: " + requestCode);

        switch (requestCode) {
            case CAPTURE_IMAGE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.i("Chat", "Image was captured");
                    changeUiToAttachmentAdded();
                }
                else if (resultCode == RESULT_CANCELED) {
                    Log.i("Chat", "No image was taken");

                    selectedImageUri = null;
                    selectedImageRealPath = null;
                }

                break;
            case CHOOSE_IMAGE_FROM_ALBUM_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.i("Chat", "Image was chosen");

                    // Save image to variable
                    selectedImageUri = data.getData();

                    changeUiToAttachmentAdded();
                }
                else if (resultCode == RESULT_CANCELED) {
                    Log.i("Chat", "No image was chosen");

                    selectedImageUri = null;
                    selectedImageRealPath = null;
                }

                break;
        }
    }


    /**
     * Class used for handling clicks to the delete attachment button.
     */
    private class DeleteAttachmentButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            selectedImageUri = null;
            selectedImageRealPath = null;

            changeUiToAttachmentRemoved();
        }
    }


    /**
     * Class used for handling clicks to the send button.
     */
    private class SendButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i("Chat", "Send button clicked");

            // Make sure message field is not empty
            if (textField.getText().length() <= 0 || textField.getText() == null) {
                Log.i("Chat", "Text field empty, not sending");
                return;
            }

            // Make sure message isn't too long
            if (textField.getText().length() > 256) {
                Log.i("Chat", "Message longer than allowed (max 256 characters), message was: " + textField.getText().length() + " characters long");
                Toast.makeText(getApplicationContext(), getText(R.string.chat_error_message_too_long), Toast.LENGTH_LONG).show();
                return;
            }

            if (selectedImageUri == null) {
                Log.i("Chat", "Image was not selected");
                sendMessageNoImage();
            }
            else {
                Log.i("Chat", "Image was selected");
                sendMessageWithImage();
                changeUiToAttachmentRemoved();
            }

            // Clear message field and scroll down to last message
            textField.setText("");
            chatHistory.setSelection(chatHistory.getCount() - 1);

        }
    }


    /**
     * This method is used when a message without an image is to be sent.
     */
    private void sendMessageNoImage() {

        // Add locally to history
        history.add(new Message(loggedInUserEmail, new Date(), textField.getText().toString(), null));
        chatHistoryAdapter.notifyDataSetChanged();

        // Send to remote server
        Communicator.addNewMessageRequest(loggedInUserEmail, contactEmail, textField.getText().toString(), null);
    }


    /**
     * This method is used when a message with an image attached is to be sent.
     */
    private void sendMessageWithImage() {

        if (selectedImageUri.getScheme().compareTo("file") == 0) {
            Log.i("Chat", "Image was taken with camera");
            selectedImageRealPath = Uri.parse(selectedImageUri.getPath());
        }
        else if (selectedImageUri.getScheme().compareTo("content") == 0) {
            Log.i("Chat", "Image was chosen from gallery");
            // Get real path as gallery app returns a content path, otherwise it will
            // fail to find the image when uploading
            selectedImageRealPath = getRealPath(selectedImageUri);
        }
        Log.i("Chat", "Real path for image: " + selectedImageRealPath.toString());

        // Add locally to history
        history.add(new Message(loggedInUserEmail, new Date(), textField.getText().toString(), selectedImageUri, selectedImageRealPath));
        chatHistoryAdapter.notifyDataSetChanged();

        // Send to remote server
        Communicator.addNewMessageRequest(loggedInUserEmail, contactEmail, textField.getText().toString(), selectedImageRealPath);

        // Set image to null again so it won't be sent next time
        selectedImageUri = null;
        selectedImageRealPath = null;
    }


    /**
     * This method gets the real path of a selected image. Because Android 4.4 (which was used in
     * development) has issues with getting the proper path using Uri.getPath() this was needed to
     * make it work.
     *
     *  http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework
     *
     * @param uri   the uri
     * @return      a string with the real uri
     */
    private Uri getRealPath(Uri uri) {
        String fullId = DocumentsContract.getDocumentId(uri);

        // Split at the colon and use the second (right hand) part of the split
        String id = fullId.split(":") [1];

        String[] column = {
                MediaStore.Images.Media.DATA
        };

        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel,
                new String[] {id},
                null);

        String filePath = "";

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();

        Log.i("Chat", "Real image uri: " + filePath);
        return Uri.parse(filePath);
    }


    /**
     * Class used to handle clicks to individual messages in the chat history
     */
    private class ChatMessageClickedListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i("Chat", "Message touched");

            Message message = history.get(position);

            if (message.hasImage()) {
                Log.i("Chat", "Message has image attached");

                Intent i = new Intent(Intent.ACTION_VIEW);

                if (message.getLocalImagePath() == null) {
                    // Image is remote
                    i.setDataAndType(message.getImageUri(), "image/*");
                }
                else {
                    // Image is local
                    i.setDataAndType(message.getLocalImagePath(), "image/*");
                }

                try {
                    startActivity(i);
                }
                catch (ActivityNotFoundException e) {
                    // No image viewer found
                    Toast.makeText(getApplicationContext(), R.string.chat_error_no_gallery_app_found, Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * This method changes the UI to show a delete attachment button and a send
     * button.
     */
    private void changeUiToAttachmentAdded() {
        // Set icon in message field to give feedback an image is attached
        Drawable img = getResources().getDrawable(R.drawable.ic_attach_file_black_48dp);
        img.setBounds(0, 0, 50, 50);
        textField.setCompoundDrawables(null, null, img, null);

        // Hide camera/gallery buttons and show delete attachment button
        cameraButton.setVisibility(View.GONE);
        galleryButton.setVisibility(View.GONE);
        deleteAttachmentButton.setVisibility(View.VISIBLE);
    }


    /**
     * This method changes the UI to show a camera button, a gallery button and
     * a send button.
     */
    private void changeUiToAttachmentRemoved() {
        // Clear attachment icon from text field
        textField.setCompoundDrawables(null, null, null, null);

        // Show camera/gallery buttons and hide delete attachment button
        cameraButton.setVisibility(View.VISIBLE);
        galleryButton.setVisibility(View.VISIBLE);
        deleteAttachmentButton.setVisibility(View.GONE);
    }


    /**
     * Creates a connection to the RabbitMQ broker in order to be able to
     * subscribe to a queue.
     */
    private void setupConnectionFactory() {
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(BROKER_CONNECTION_URI);
        }
        catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Subscribes to a particular queue. In this case it subscribes to the queue
     * named {sender@email.com}TO{receiver@email.com}.
     */
    void subscribe() {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    Connection connection = null;
                    Channel channel = null;

                    try {
                        connection = factory.newConnection();
                        channel = connection.createChannel();
                        channel.basicQos(1);
                        AMQP.Queue.DeclareOk q = channel.queueDeclare();

                        // Create queue name to subscribe to
                        String loggedInUserEmail = pref.getString("loggedInUserEmail", null);
                        String contactEmail = getIntent().getStringExtra("contactEmail");
                        String queue = contactEmail + "TO" + loggedInUserEmail;

                        Log.i("Push", "Subscribing to queue: " + queue);
                        channel.queueBind(q.getQueue(), "chat", queue);

                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume(q.getQueue(), true, consumer);

                        while (true) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            Log.i("Push", "Message was received");
                            String string = new String(delivery.getBody());

                            Message messageReceived = XMLParser.getMessageFromXmlString(string);
                            history.add(messageReceived);

                            // Use main thread to update UI with new message
                            ChatActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatHistoryAdapter.notifyDataSetChanged();
                                    chatHistory.setSelection(chatHistory.getCount() - 1);
                                }
                            });
                        }
                    }
                    catch (InterruptedException e) {
                        Log.i("Push", "Received InterruptedException, disconnecting");

                        try {
                            connection.close();
                            Log.i("Push", "Successfully disconnected");
                        }
                        catch (IOException ex) {
                            ex.printStackTrace();
                        }

                        break;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Log.i("Push", "Connection broken: " + e.getClass().getName());

                        try {
                            // Sleep 5 seconds and try again
                            Thread.sleep(5000);
                        }
                        catch (InterruptedException ex) {
                            Log.i("Push", "Received InterruptedException, disconnecting");

                            try {
                                connection.close();
                                Log.i("Push", "Successfully disconnected");
                            }
                            catch (IOException ioe) {
                                ex.printStackTrace();
                            }

                            break;
                        }
                    }
                }
            }
        });

        subscribeThread.start();
    }
}
