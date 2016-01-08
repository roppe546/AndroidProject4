package com.example.robin.androidproject4.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.robin.androidproject4.R;
import com.example.robin.androidproject4.model.Message;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    private ArrayList<Message> history;

    private SharedPreferences pref;
    private ListView chatHistory;
    private ChatHistoryAdapter chatHistoryAdapter;
    private EditText textField;
    private ImageButton galleryButton;
    private Button sendButton;

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
        galleryButton = (ImageButton) findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(new GalleryButtonClickListener());
        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new SendButtonClickListener());

        // Populate chat history
        history = new ArrayList<>();
        // TODO: Get history from server, don't use dummy data
        for (int i = 0; i < 5; i++) {
            history.add(new Message("Sender name", "Can you hear me?"));
        }
        chatHistoryAdapter = new ChatHistoryAdapter(this, history);
        chatHistory.setAdapter(chatHistoryAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

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

    private class GalleryButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i("Chat", "Gallery button clicked");

            Intent picImage = Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), getString(R.string.chat_choose_image_text));
            startActivity(picImage);
        }
    }

    private class SendButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i("Chat", "Send button clicked");

            if (textField.getText().length() <= 0 || textField.getText() == null) {
                Log.i("Chat", "Text field empty, not sending");
                return;
            }

            history.add(new Message(pref.getString("loggedInUser", null), textField.getText().toString()));
            textField.setText("");
            chatHistory.setSelection(chatHistory.getCount() - 1);
            // TODO: Send to server instead of doing it locally
        }
    }
}
