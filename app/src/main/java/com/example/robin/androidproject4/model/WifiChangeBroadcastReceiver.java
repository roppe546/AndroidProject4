package com.example.robin.androidproject4.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.example.robin.androidproject4.R;

/**
 * This broadcast receiver checks whether the state of the Wifi connection
 * has changed, and notifies users depending on if device connected or
 * disconnected to Wifi.
 *
 * Created by robin on 17/1/16.
 */
public class WifiChangeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("BroadcastReceiver", "onReceive was called");
        String str = intent.getAction();

        if (str.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, true)) {
                // Wifi connected, notify user that sending files is no problem
                Log.i("BroadcastReceiver", "Connected to Wifi");
                Toast.makeText(context, context.getString(R.string.broadcast_receiver_wifi_connected), Toast.LENGTH_LONG).show();
            }
            else {
                // Wifi disconnected, notify user that sending files is going to use mobile data
                Log.i("BroadcastReceiver", "Disconnected from Wifi");
                Toast.makeText(context, context.getString(R.string.broadcast_receiver_wifi_disconnected), Toast.LENGTH_LONG).show();
            }
        }
    }
}
