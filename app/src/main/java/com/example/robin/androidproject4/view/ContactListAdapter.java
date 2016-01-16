package com.example.robin.androidproject4.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robin.androidproject4.R;
import com.example.robin.androidproject4.model.Contact;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Custom adapter for the contact list.
 *
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

        // Get elements in item and put in view holder
        ViewHolder holder = new ViewHolder();
        holder.profilePicture = (ImageView) convertView.findViewById(R.id.contactlist_item_profile_picture);
        holder.username = (TextView) convertView.findViewById(R.id.contactlist_item_username);
        holder.onlineStatus = (TextView) convertView.findViewById(R.id.contactlist_item_status);
        convertView.setTag(holder);

        // Set fields
        // Profile picture
        // TODO: Use Picasso fallback and error methods instead.
        if (contact.getProfilePictureUri() == null) {
            // Placeholder in case user hasn't chosen an image on Google account
            Picasso.with(getContext()).load(R.drawable.ic_profile_placeholder).into(holder.profilePicture);
        }
        else {
            Picasso.with(getContext()).load(contact.getProfilePictureUri().toString()).placeholder(R.drawable.ic_profile_placeholder).into(holder.profilePicture);
        }

        // Username
        holder.username.setText(contact.getUsername());

        // Online status
        String status = contact.getStatus();
        holder.onlineStatus.setText(status);

        return convertView;
    }

    // For view holder pattern
    static class ViewHolder {
        ImageView profilePicture;
        TextView username;
        TextView onlineStatus;
    }
}
