package com.example.robin.androidproject4.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robin.androidproject4.R;
import com.example.robin.androidproject4.model.Message;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Custom adapter for the chat history list.
 *
 * Created by robin on 7/1/16.
 */
public class ChatHistoryAdapter extends ArrayAdapter<Message> {
    private SharedPreferences pref;

    public ChatHistoryAdapter(Context context, List<Message> objects) {
        super(context, 0, objects);
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chathistory_item, parent, false);
        }

        // Get elements in item and put in view holder
        ViewHolder holder = new ViewHolder();
        holder.sender = (TextView) convertView.findViewById(R.id.chat_sender);
        holder.timestamp = (TextView) convertView.findViewById(R.id.chat_timestamp);
        holder.messageText = (TextView) convertView.findViewById(R.id.chat_message);
        holder.image = (ImageView) convertView.findViewById(R.id.chat_message_image);
        convertView.setTag(holder);

        // Find scale of screen
        float px = convertView.getResources().getDisplayMetrics().density;

        // Set minimum height for text view so chat doesn't look too squashed
        int minHeight = (int) (32 * px + 0.5f);
        holder.messageText.setMinHeight(minHeight);

        // Set view for text dynamically so it adjust height of item in history
        holder.messageText.measure(View.MeasureSpec.makeMeasureSpec((int) px, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        // Set fields
        // Username
        holder.sender.setText(message.getSender());

        // Timestamp
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String receivedText = df.format(message.getTimestamp());
        holder.timestamp.setText(receivedText);

        // Message
        holder.messageText.setText(message.getMessage());

        // Image
        if (message.getImageUri() == null) {
            // Put 1 pixel in image view in case message doesn't have an image attached to itself.
            // This needed to be done otherwise Picasso would do very weird things, such as loading
            // images into wrong items and such.
            Picasso.with(getContext()).load(R.drawable.ic_one_pixel_transparent).into(holder.image);
        }
        else {
            Picasso.with(getContext()).load(R.drawable.ic_image_black_48dp).into(holder.image);

            // The below line can be used if we want to show a small thumbnail of the actual
            // image instead of the thumbnail icon

            // Picasso.with(getContext()).load(message.getImageUri().toString()).placeholder(R.drawable.ic_image_black_48dp).into(holder.image);
        }

        return convertView;
    }

    // For view holder pattern
    static class ViewHolder {
        TextView sender;
        TextView timestamp;
        TextView messageText;
        ImageView image;
    }
}
