package com.example.robin.androidproject4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
        TextView username = (TextView) convertView.findViewById(R.id.contactlist_item_username);
        TextView lastMessageReceived = (TextView) convertView.findViewById(R.id.contactlist_item_last_message_received_timestamp);

        // Set fields
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
        lastMessageReceived.setText(year + "-" + (month + 1) + "-" + date + " " + hour + ":" + minute);

        return convertView;
    }
}
