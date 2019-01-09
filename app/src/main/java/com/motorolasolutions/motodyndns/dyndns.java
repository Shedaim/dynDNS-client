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

    private static String TAG = "dyndns service";
    public SharedPreferences myPrefs;

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d(TAG, "dyndns service started");

        while (true) {
            myPrefs = getSharedPreferences("hostname", MODE_PRIVATE);
            String StoredValue = myPrefs.getString("hostname", "Default");
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
                js.put("hostname", StoredValue);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "going to send");
            Log.d(TAG, String.valueOf(js));
            sendDynDnsUpdate(js);
            Log.d(TAG, "sent");
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d(TAG, "dyndns service destroyed");

    }

    public void sendDynDnsUpdate(JSONObject js) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://dfint.lxn500.com:1024";

        // Request a string response from the provided URL.
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, js,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.d(TAG, String.valueOf(response));
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
