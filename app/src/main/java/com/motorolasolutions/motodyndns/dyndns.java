package com.motorolasolutions.motodyndns;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class dyndns extends Service {

    private static final String TAG = "dynDNS service";
    private Handler mainHandler;
    private Timer timer;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "dynDNS service started");
        //myPrefs = getApplicationContext().getSharedPreferences("configuration", MODE_MULTI_PROCESS );
        SharedPreferences myPrefs = getSharedPreferences("configuration", MODE_MULTI_PROCESS);
        String hostname = myPrefs.getString("hostname", "");
        String entry_type = myPrefs.getString("entry_type", "A");
        String ttl = myPrefs.getString("ttl", "86400");
        String domain = myPrefs.getString("domain", "dns.com");
        JSONObject js = new JSONObject();
        try {
            js.put("hostname", hostname);
            js.put("entry_type", entry_type);
            js.put("ttl", ttl);
            js.put("domain", domain);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        myPrefs = getSharedPreferences("configuration", MODE_MULTI_PROCESS);
        String url = (myPrefs.getString("server", "http://dns.com") + ":" +
                myPrefs.getString("port", "80")); //Name of DNS server
        // Send only if a hostname is configured
        if (!hostname.isEmpty()) sendDynDnsUpdate(js, url);
        if (!intent.hasExtra("conn_changed")) {
            mainHandler = new Handler(getApplicationContext().getMainLooper());
            timer = new Timer();
            timer.schedule(new MyTimerTask(js, url), 600000, 600000);
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "Task removed, stopping service");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
        Log.d(TAG, "dynDNS service destroyed");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setClass(this, dynDNSBroadcastReceiver.class);
        broadcastIntent.setAction("restartService");

        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(),
                1001, broadcastIntent, PendingIntent.FLAG_ONE_SHOT);

        ((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE)).
                set(AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + 1000,
                        pi);
    }

    private void sendDynDnsUpdate(final JSONObject js, final String url) {
        Log.d(TAG, "Sending dynDNS message: " + js + " to URL: " + url);
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            public byte[] getBody() {
                return js.toString().getBytes();
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private class MyTimerTask extends TimerTask {
        private final JSONObject js;
        private final String url;

        MyTimerTask(JSONObject js, String url) {
            this.js = js;
            this.url = url;
        }

        @Override
        public void run() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendDynDnsUpdate(js, url);
                }
            });
        }
    }
}
