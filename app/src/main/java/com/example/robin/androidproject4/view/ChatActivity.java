package com.example.robin.androidproject4.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.robin.androidproject4.R;
import com.example.robin.androidproject4.model.Message;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

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

    private Bitmap selectedImage = null;
    private ArrayList<Message> history;
    private SharedPreferences pref;

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
        // TODO: Get history from server, don't use dummy data
        for (int i = 0; i < 5; i++) {
            history.add(new Message("Sender name", "Can you hear me?"));
        }
//        history.add(new Message("Sender name", "Can you see the image? (Doesn't work in emulator!)", getResources().getDrawable(R.drawable.ic_account_square_gray)));

        chatHistoryAdapter = new ChatHistoryAdapter(this, history);
        chatHistory.setAdapter(chatHistoryAdapter);
        chatHistory.setOnItemClickListener(new ChatMessageClickedListener());
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
                Log.i("ActionMenu", "Selected Log out in menu (chat)");
                Log.i("Login", "Logged out (chat)");

                // Clear logged in user from shared preferences
                SharedPreferences.Editor editor = pref.edit();
                editor.remove("loggedInUser");
                editor.apply();

                // Send back to login activity
                Intent login = new Intent(this, LoginActivity.class);
                login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
                this.finish();
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

                    try {
                        // Save image to variable
                        Uri uriToImage = data.getData();
                        InputStream imageStream = getContentResolver().openInputStream(uriToImage);
                        selectedImage = BitmapFactory.decodeStream(imageStream);

                        // Set icon in message field to give feedback an image is attached
                        Drawable img = getResources().getDrawable(R.drawable.ic_attach_file_black_48dp);
                        img.setBounds(0, 0, 50, 50);
                        textField.setCompoundDrawables(null, null, img, null);

                        // Hide camera/gallery buttons and show only delete attachment button
                        cameraButton.setVisibility(View.GONE);
                        galleryButton.setVisibility(View.GONE);
                        deleteAttachmentButton.setVisibility(View.VISIBLE);
                    }
                    catch (FileNotFoundException e) {
                        Log.i("Chat", "File inputstream couldn't be read.");
                        Toast.makeText(getApplicationContext(), getString(R.string.chat_error_could_not_read_image), Toast.LENGTH_LONG).show();
                    }
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
            // No image was selected
            if (selectedImage == null) {
                history.add(new Message(pref.getString("loggedInUser", null), textField.getText().toString()));
            }
            // Image was selected
            else {
                history.add(new Message(pref.getString("loggedInUser", null), textField.getText().toString(), selectedImage));
                // Clear attachment icon from text field
                textField.setCompoundDrawables(null, null, null, null);

                // Show camera/gallery buttons and hide delete attachment button
                cameraButton.setVisibility(View.VISIBLE);
                galleryButton.setVisibility(View.VISIBLE);
                deleteAttachmentButton.setVisibility(View.GONE);

                // Set image to null again so it won't be sent next time
                selectedImage = null;
            }

            textField.setText("");
            chatHistory.setSelection(chatHistory.getCount() - 1);
        }
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
                // TODO: Open uploaded image instead of test image (Message object might need to store URI instead of Drawable)
                i.setDataAndType(Uri.parse("http://www.networkforgood.com/wp-content/uploads/2015/08/bigstock-Test-word-on-white-keyboard-27134336.jpg"), "image/*");

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
}
