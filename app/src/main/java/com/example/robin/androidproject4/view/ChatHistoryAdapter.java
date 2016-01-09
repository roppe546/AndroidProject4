package com.example.robin.androidproject4.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robin.androidproject4.R;
import com.example.robin.androidproject4.model.Message;

import java.util.Calendar;
import java.util.List;

/**
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

        // Get elements in item
        TextView sender = (TextView) convertView.findViewById(R.id.chat_sender);
        TextView timestamp = (TextView) convertView.findViewById(R.id.chat_timestamp);
        TextView messageText = (TextView) convertView.findViewById(R.id.chat_message);
        ImageView image = (ImageView) convertView.findViewById(R.id.chat_message_image);

        // Set fields
        // Username
        sender.setText(message.getSender());

        // Timestamp
        // TODO: Use timestamps for when message was sent instead of current time
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        Resources res = getContext().getResources();
        String receivedText = String.format(res.getString(R.string.chat_history_received_time), year, (month + 1), date, hour, minute);
        timestamp.setText(receivedText);

        // Message
        messageText.setText(message.getMessage());

        // Image
        image.setImageBitmap(message.getImage());

        return convertView;
    }
}
