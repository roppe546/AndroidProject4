package com.example.robin.androidproject4.view;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.example.robin.androidproject4.R;

public class ChatActivity extends AppCompatActivity {
    private ImageButton galleryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Add back button to the action menu
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get UI elements
        galleryButton = (ImageButton) findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(new GalleryButtonClickListener());
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
//                startActivityForResult(new Intent(this, SettingsActivity.class), 200);
                break;
            case R.id.menu_logout :
                Log.i("ActionMenu", "Selected Log out in menu (chat)");
                break;
            case android.R.id.home:
                Log.i("ActionMenu", "Selected back arrow in menu (chat)");
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class GalleryButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.i("Chat", "Gallery button clicked");

            Intent picImage = Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), "Choose an image");
            startActivity(picImage);
        }
    }
}
