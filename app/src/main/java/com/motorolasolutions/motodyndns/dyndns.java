package com.motorolasolutions.motodyndns;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class dyndns extends Service {

    private static String TAG = "dynDNS service";
    public SharedPreferences myPrefs;

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d(TAG, "dynDNS service started");

        while (true) {
            myPrefs = getSharedPreferences("configuration", MODE_PRIVATE);
            String hostname = myPrefs.getString("hostname", "Default");
            long endTime = System.currentTimeMillis() + 5 * 1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                        Log.d(TAG, "Error with while");
                    }
                }
            }
            JSONObject js = new JSONObject();

            try {
                js.put("hostname", hostname);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendDynDnsUpdate(js);
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d(TAG, "dynDNS service destroyed");

    }

    public void sendDynDnsUpdate(JSONObject js) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        myPrefs = getSharedPreferences("configuration", MODE_PRIVATE);
        String url = (myPrefs.getString("server", "http://dns.com") + ":" +
                myPrefs.getString("port", "80")); //Name of DNS server
        Log.d(TAG, "Sending dynDNS message: " + js + " to URL: " + url);
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
                        Log.d(TAG, "Error with http message");
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(postRequest);
    }
}
