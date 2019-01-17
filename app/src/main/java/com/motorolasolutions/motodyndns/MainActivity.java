package com.motorolasolutions.motodyndns;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {


    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPref;
    private EditText mEditServer;
    private EditText mEditHostname;
    private EditText mEditPort;
    private TextView mTextHostname;
    private TextView mTextServer;
    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        Button mButtonSave = findViewById(R.id.SaveButton);
        Button mButtonStart = findViewById(R.id.StartButton);
        mEditPort = findViewById(R.id.PortEdit);
        mEditServer = findViewById(R.id.ServerEdit);
        mEditHostname = findViewById(R.id.HostnameEdit);
        mTextHostname = findViewById(R.id.HostnameText);
        mTextServer = findViewById(R.id.ServerText);

        dyndns mDynDns = new dyndns();
        mServiceIntent = new Intent(this, mDynDns.getClass());
        Log.d(TAG, "Starting service");
        if (!isMyServiceRunning(mDynDns.getClass())) {
            startService(mServiceIntent);
        }

        sharedPref = getApplicationContext().getSharedPreferences("configuration", MODE_MULTI_PROCESS );
        String hostname = sharedPref.getString("hostname", "Default_Hostname");
        mEditHostname.setHint(hostname);
        hostname = "Hostname: " + hostname;
        mTextHostname.setText(hostname);
        String server = sharedPref.getString("server", "Default_Server");
        mEditServer.setHint(server);
        String port = sharedPref.getString("port", "Default_Port");
        mEditPort.setHint(port);
        server = "Server: " + server + ":" + port;
        mTextServer.setText(server);

        mButtonSave.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        Log.d(TAG, "Saving configuration.");
                        String server = saveParam("server", mEditServer);
                        String port = saveParam("port", mEditPort);
                        String hostname = saveParam("hostname", mEditHostname);
                        mEditServer.setHint(server);
                        mEditPort.setHint(port);
                        mEditHostname.setHint(hostname);
                        server = "Server: " + server + ":" + port;
                        mTextServer.setText(server);
                        hostname = "Hostname: " + hostname;
                        mTextHostname.setText(hostname);
                    }
                });
        mButtonStart.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        stopService(mServiceIntent);
                    }
                });
    }

    private String saveParam(String name, EditText text){
        SharedPreferences.Editor editor = sharedPref.edit();
        String result = text.getText().toString();
        if (result.isEmpty()){
            result = sharedPref.getString(name, "Default");
            editor.putString(name, result);
        }
        else{
            editor.putString(name, result);
        }
        editor.apply();
        return result;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, serviceClass.getName());
                Log.d (TAG, "Service status: Running");
                return true;
            }
        }
        Log.d (TAG, "Service status: Not running");
        return false;
    }

    @Override
    protected void onPause() {
        Log.d (TAG, "Application paused");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d (TAG, "Application destroyed");
        stopService(mServiceIntent);
        super.onDestroy();
    }
}
