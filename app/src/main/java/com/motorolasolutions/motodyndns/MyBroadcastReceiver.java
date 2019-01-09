package com.motorolasolutions.motodyndns;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences myPrefs;
        myPrefs = context.getSharedPreferences("hostname", context.MODE_PRIVATE);
        String StoredValue = myPrefs.getString("hostname", "Default");
        context.startService(new Intent(context, dyndns.class));
    }
}