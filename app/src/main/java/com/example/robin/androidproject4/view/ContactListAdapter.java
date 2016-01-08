package com.example.robin.androidproject4.view;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robin.androidproject4.R;
import com.example.robin.androidproject4.model.Contact;

import java.util.ArrayList;
import java.util.Calendar;

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
        profilePicture.setImageDrawable(contact.getProfilePicture());

        // Username
        username.setText(contact.getUsername());

        // Last message received
        // TODO: Use timestamps for when last message was received instead of current time
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        Resources res = getContext().getResources();
        String lastReceivedText = String.format(res.getString(R.string.contact_list_last_received_time), year, (month + 1), date, hour, minute);
        lastMessageReceived.setText(lastReceivedText);

        return convertView;
    }
}
