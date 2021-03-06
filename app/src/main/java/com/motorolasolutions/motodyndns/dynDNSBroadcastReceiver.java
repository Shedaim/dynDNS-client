package com.motorolasolutions.motodyndns;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class dynDNSBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "dynDNSBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        assert action != null;
        switch (action) {
            case "android.net.conn.CONNECTIVITY_CHANGE": {
                Log.d(TAG, "Connectivity changed, updating service to resend data.");
                Intent new_intent = new Intent(context, dyndns.class);
                new_intent.putExtra("conn_changed", true);
                context.startService(new_intent);
                break;
            }
            case "restartService": {
                Log.d(TAG, "Restart service Broadcast received, starting dynDNS service");
                Intent new_intent = new Intent(context, dyndns.class);
                context.startService(new_intent);
                break;
            }
            case "android.intent.action.BOOT_COMPLETED":{
                Log.d(TAG, "Received boot intent --> Starting service on boot.");
                Intent new_intent = new Intent(context, dyndns.class);
                context.startService(new_intent);
                break;
            }
            default:
                Log.d(TAG, "Received broadcast without action.");
                break;
        }
    }
}