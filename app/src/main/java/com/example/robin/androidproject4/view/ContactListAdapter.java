package com.example.robin.androidproject4.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robin.androidproject4.R;
import com.example.robin.androidproject4.model.Contact;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by robin on 6/1/16.
 */
public class ContactListAdapter extends ArrayAdapter<Contact> {
    public ContactListAdapter(Context context, ArrayList<Contact> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contactlist_item, parent, false);
        }

        // Get elements in item
        ImageView profilePicture = (ImageView) convertView.findViewById(R.id.contactlist_item_profile_picture);
        TextView username = (TextView) convertView.findViewById(R.id.contactlist_item_username);
        TextView lastMessageReceived = (TextView) convertView.findViewById(R.id.contactlist_item_last_message_received_timestamp);

        // Set fields
        // Profile picture
//        new ProfilePictureLoadTask(contact.getProfilePictureUri(), profilePicture).execute();

        // Username
        username.setText(contact.getUsername());

        // Last message received
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String stringDate = df.format(contact.getLastReceivedTimestamp());

        lastMessageReceived.setText(stringDate);

        return convertView;
    }


    public class ProfilePictureLoadTask extends AsyncTask<Void, Void, Bitmap> {
        private Uri uri;
        private ImageView imageView;

        public ProfilePictureLoadTask(Uri uri, ImageView imageView) {
            this.uri = uri;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(uri.toString());
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap picture = BitmapFactory.decodeStream(input);
                return picture;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }
    }
}
