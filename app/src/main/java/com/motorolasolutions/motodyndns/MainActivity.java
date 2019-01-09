package com.motorolasolutions.motodyndns;

import android.app.Activity;
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
    public SharedPreferences sharedPref;
    public Button mButtonSave, mButtonStart;
    public EditText mEditServer, mEditHostname, mEditPort;
    public TextView mTextHostname, mTextServer, mTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        mButtonSave = findViewById(R.id.SaveButton);
        mButtonStart = findViewById(R.id.StartButton);
        mEditPort = findViewById(R.id.PortEdit);
        mEditServer = findViewById(R.id.ServerEdit);
        mEditHostname = findViewById(R.id.HostnameEdit);
        mTextHostname = findViewById(R.id.HostnameText);
        mTextServer = findViewById(R.id.ServerText);
        mTitleText =  findViewById(R.id.TitleText);

        sharedPref = getApplicationContext().getSharedPreferences("configuration", 0);
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
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("server", mEditServer.getText().toString());
                        editor.putString("port", mEditPort.getText().toString());
                        editor.putString("hostname", mEditHostname.getText().toString());
                        editor.commit();
                        mEditServer.setHint(mEditServer.getText().toString());
                        mEditPort.setHint(mEditPort.getText().toString());
                        mEditHostname.setHint(mEditHostname.getText().toString());
                        String server = "Server: " + mEditServer.getText().toString() +
                                ":" + mEditPort.getText().toString();
                        mTextServer.setText(server);
                        String hostname = "Hostname: " + mEditHostname.getText().toString();
                        mTextHostname.setText(hostname);
                    }
                });
        mButtonStart.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        Log.d(TAG, "Starting service");
                        startService(new Intent(getApplication(), dyndns.class));
                    }
                });
    }
}
