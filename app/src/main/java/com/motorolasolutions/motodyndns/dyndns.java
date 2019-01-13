package com.motorolasolutions.motodyndns;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class dyndns extends Service {

    private static String TAG = "dynDNS service";
    public SharedPreferences myPrefs;
    private Handler mainHandler;
    private Timer timer;

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "dynDNS service started");
        mainHandler = new Handler(getApplicationContext().getMainLooper());
        timer = new Timer();
        timer.schedule(new MyTimerTask(), 60000, 60000);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
        Log.d(TAG, "dynDNS service destroyed");

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartService");
        broadcastIntent.setClass(this, dynDNSBroadcastReceiver.class);
        this.sendBroadcast(broadcastIntent);
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    myPrefs = getSharedPreferences("configuration", MODE_PRIVATE);
                    String hostname = myPrefs.getString("hostname", "Default");
                    JSONObject js = new JSONObject();
                    try {
                        js.put("hostname", hostname);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    myPrefs = getSharedPreferences("configuration", MODE_PRIVATE);
                    String url = (myPrefs.getString("server", "http://dns.com") + ":" +
                            myPrefs.getString("port", "80")); //Name of DNS server
                    sendDynDnsUpdate(js, url);
                }
            });
        }
    }



    public void sendDynDnsUpdate(final JSONObject js, final String url) {
        Log.d(TAG, "Sending dynDNS message: " + js + " to URL: " + url);
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Create JSON request containing {"hostname":hostname}
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, js,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.d(TAG, "Response message: " + String.valueOf(response));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error sending message: " + js + " to URL: " + url);
                    }
                });
        // Add the request to the RequestQueue.
        queue.add(postRequest);
    }
}
