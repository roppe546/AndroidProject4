package com.example.robin.androidproject4.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

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

    private Uri selectedImageUri = null;
    private Uri selectedImageRealPath = null;
    private Bitmap selectedImage = null;
    private ArrayList<Message> history;
    private SharedPreferences pref;

    // Google Log-in
    GoogleApiClient mGoogleApiClient;

    private Thread subscribeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Add back button to the action menu
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get shared preferences
        pref = PreferenceManager.getDefaultSharedPreferences(this);

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

        // Populate chat history
        history = new ArrayList<>();
        history = Communicator.getChatHistoryRequest(pref.getString("loggedInUserEmail", null), getIntent().getStringExtra("contactEmail"));

        chatHistoryAdapter = new ChatHistoryAdapter(this, history);
        chatHistory.setAdapter(chatHistoryAdapter);
        chatHistory.setOnItemClickListener(new ChatMessageClickedListener());

        // Scroll to bottom of chat
        chatHistory.setSelection(chatHistory.getCount() - 1);

        // Configure Google Sign-In
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, null).addApi(Auth.GOOGLE_SIGN_IN_API).build();

        //TEST CODE BELOW
        setupConnectionFactory();
        subscribe();
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

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i("Login", "Logged out (main)");

                            // Set status offline
                            Communicator.putUserRequest(pref.getString("loggedInUserEmail", null), Account.getAccount().getPhotoUrl(), getString(R.string.STATUS_OFFLINE));

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
     */
    private class CameraButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i("Chat", "Camera button clicked");

            Intent takeImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takeImage, CAPTURE_IMAGE_REQUEST_CODE);
        }
    }


    /**
     * Class used for handling clicks to the gallery button
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
     * Callback method which is called when a user chooses an image from
     * the gallery to attach to a message.
     *
     * @param requestCode   the request code which was used
     * @param resultCode    whether the action finished successfully
     * @param data          data that was returned with the result (in our case the picture)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("Chat", "Result from activity with request code: " + requestCode);

        switch (requestCode) {
            case CAPTURE_IMAGE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.i("Chat", "Image was captured");

                    // Save image to variable
                    selectedImage = (Bitmap) data.getExtras().get("data");

                    // Set icon in message field to give feedback an image is attached
                    Drawable img = getResources().getDrawable(R.drawable.ic_attach_file_black_48dp);
                    img.setBounds(0, 0, 50, 50);
                    textField.setCompoundDrawables(null, null, img, null);

                    // Hide camera/gallery buttons and show delete attachment button
                    cameraButton.setVisibility(View.GONE);
                    galleryButton.setVisibility(View.GONE);
                    deleteAttachmentButton.setVisibility(View.VISIBLE);
                }
                else if (resultCode == RESULT_CANCELED) {
                    Log.i("Chat", "No image was taken");
                }

                break;
            case CHOOSE_IMAGE_FROM_ALBUM_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.i("Chat", "Image was chosen");

//                    try {
                        // Save image to variable
                        selectedImageUri = data.getData();
//                        InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
//                        selectedImage = BitmapFactory.decodeStream(imageStream);

                        // Set icon in message field to give feedback an image is attached
                        Drawable img = getResources().getDrawable(R.drawable.ic_attach_file_black_48dp);
                        img.setBounds(0, 0, 50, 50);
                        textField.setCompoundDrawables(null, null, img, null);

                        // Hide camera/gallery buttons and show only delete attachment button
                        cameraButton.setVisibility(View.GONE);
                        galleryButton.setVisibility(View.GONE);
                        deleteAttachmentButton.setVisibility(View.VISIBLE);
//                    }
//                    catch (FileNotFoundException e) {
//                        Log.i("Chat", "File inputstream couldn't be read.");
//                        Toast.makeText(getApplicationContext(), getString(R.string.chat_error_could_not_read_image), Toast.LENGTH_LONG).show();
//                    }
                }
                else if (resultCode == RESULT_CANCELED) {
                    Log.i("Chat", "No image was chosen");
                }

                break;
        }
    }


    /**
     * Class used for handling clicks to the delete attachment button
     */
    private class DeleteAttachmentButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            selectedImage = null;

            // Clear attachment icon from text field
            textField.setCompoundDrawables(null, null, null, null);

            // Show camera/gallery buttons and hide delete attachment button
            cameraButton.setVisibility(View.VISIBLE);
            galleryButton.setVisibility(View.VISIBLE);
            deleteAttachmentButton.setVisibility(View.GONE);
        }
    }


    /**
     * Class used for handling clicks to the send button
     */
    private class SendButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i("Chat", "Send button clicked");

            if (textField.getText().length() <= 0 || textField.getText() == null) {
                Log.i("Chat", "Text field empty, not sending");
                return;
            }

            // TODO: Send to server instead of doing it locally
            if (selectedImageUri == null) {
                // No image was selected
                Log.i("Chat", "Image was not selected");

                // Add locally to history
                history.add(new Message(pref.getString("loggedInUserEmail", null), new Date(), textField.getText().toString(), null));
                chatHistoryAdapter.notifyDataSetChanged();
                Communicator.addNewMessageRequest(pref.getString("loggedInUserEmail", null), getIntent().getStringExtra("contactEmail"), textField.getText().toString(), null);
            }
            else {
                // Image was selected
                Log.i("Chat", "Image was selected");

                // Get real path, otherwise it will fail to find the image when uploading
                selectedImageRealPath = getRealPath(selectedImageUri);

                // Add locally to history
                history.add(new Message(pref.getString("loggedInUserEmail", null), new Date(), textField.getText().toString(), selectedImageUri, selectedImageRealPath));
                chatHistoryAdapter.notifyDataSetChanged();

                Communicator.addNewMessageRequest(pref.getString("loggedInUserEmail", null), getIntent().getStringExtra("contactEmail"), textField.getText().toString(), selectedImageRealPath);
                // Clear attachment icon from text field
                textField.setCompoundDrawables(null, null, null, null);

                // Show camera/gallery buttons and hide delete attachment button
                cameraButton.setVisibility(View.VISIBLE);
                galleryButton.setVisibility(View.VISIBLE);
                deleteAttachmentButton.setVisibility(View.GONE);

                // Set image to null again so it won't be sent next time
                selectedImage = null;
                selectedImageUri = null;
                selectedImageRealPath = null;
            }

            textField.setText("");
            chatHistory.setSelection(chatHistory.getCount() - 1);
        }
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
        String fullId = DocumentsContract.getDocumentId(selectedImageUri);

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

    // TEST CODE BELOW
    private BlockingDeque queue = new LinkedBlockingDeque();
    void publishMessage(String message) {
        try {
            Log.i("TEST123", "[q] " + message);
            queue.putLast(message);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    ConnectionFactory factory = new ConnectionFactory();
    private void setupConnectionFactory() {
        String uri = "amqp://vxoqwope:CQyPw9I5N8Hn70MjvQu0dd9lwcdnJZA0@spotted-monkey.rmq.cloudamqp.com/vxoqwope";
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }

    void subscribe() {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel();
                        channel.basicQos(1);
                        AMQP.Queue.DeclareOk q = channel.queueDeclare();
                        channel.queueBind(q.getQueue(), "amq.fanout", "chat");
                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume(q.getQueue(), true, consumer);

                        while (true) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            String string = new String(delivery.getBody());

                            Message messageReceived = XMLParser.getMessageFromXmlString(string);
                            history.add(messageReceived);
                            chatHistoryAdapter.notifyDataSetChanged();

                            // TODO: FIX SO LIST VIEW SHOWS MESSAGE WITHOUT HAVING TO BE SCROLLED FIRST
                            chatHistory.setSelection(chatHistory.getCount() - 1);
                        }
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                    catch (Exception e1) {
                        Log.d("", "Connection broken: " + e1.getClass().getName());
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        });
        subscribeThread.start();
    }
}
