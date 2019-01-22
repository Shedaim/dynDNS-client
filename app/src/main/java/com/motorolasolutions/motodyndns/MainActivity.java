package com.motorolasolutions.motodyndns;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    String[] ENTRY_TYPES = {"A", "PTR", "AAAA"};
    String SERVER_URL_ALLOWED_CHARS = "[a-zA-Z0-9:/\\\\.%$#&_?=+-]{1,200}+";
    String DOMAIN_ALLOWED_CHARS = "[a-zA-Z0-9:/\\\\._-]{1,200}+";
    String HOSTNAME_ALLOWED_CHARS = "[a-zA-Z0-9:/\\\\._-]{1,200}+";

    // Declarations of activity visual objects
    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPref;
    private TextView mTextHostname, mTextServer;
    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EditText mEditServer, mEditPort, mEditHostname, mEditDomain, mEditEntry, mEditTTL;
        // Initiation of activity visual objects
        setContentView(R.layout.main_activity);
        Button mButtonSave = findViewById(R.id.SaveButton);
        Button mButtonStart = findViewById(R.id.RestartButton);
        mEditServer = findViewById(R.id.ServerEdit);
        mEditPort = findViewById(R.id.PortEdit);
        mEditHostname = findViewById(R.id.HostnameEdit);
        mEditDomain = findViewById(R.id.DomainEdit);
        mEditEntry = findViewById(R.id.EntryEdit);
        mEditTTL = findViewById(R.id.TTLEdit);
        mTextHostname = findViewById(R.id.HostnameText);
        mTextServer = findViewById(R.id.ServerText);


        // Read saved configuration and set it as hints if empty
        sharedPref = getApplicationContext().getSharedPreferences("configuration", MODE_MULTI_PROCESS );

        // Create dictionary of EditText objects corresponding to each configuration key
        final Map<String, EditText> edit_text_dictionary = new HashMap<>();
        edit_text_dictionary.put("server", mEditServer);
        edit_text_dictionary.put("port", mEditPort);
        edit_text_dictionary.put("hostname", mEditHostname);
        edit_text_dictionary.put("domain", mEditDomain);
        edit_text_dictionary.put("entry_type", mEditEntry);
        edit_text_dictionary.put("ttl", mEditTTL);

        // Create dictionary of default strings corresponding to each configuration key
        final Map<String, String> default_values_dictionary = new HashMap<>();
        default_values_dictionary.put("server", getString(R.string.default_server));
        default_values_dictionary.put("port", getString(R.string.default_port));
        default_values_dictionary.put("hostname", getString(R.string.default_hostname));
        default_values_dictionary.put("domain", getString(R.string.default_domain));
        default_values_dictionary.put("entry_type", getString(R.string.default_entry_type));
        default_values_dictionary.put("ttl", getString(R.string.default_TTL));

        for ( Map.Entry<String, EditText> entry : edit_text_dictionary.entrySet() ) {
            String key = entry.getKey();
            EditText edit_obj = entry.getValue();
            String saved_conf = sharedPref.getString(key, "");
            if (saved_conf.isEmpty())edit_obj.setHint(default_values_dictionary.get(key));
            else edit_obj.setText(saved_conf);
        }

        String server = "Server: "+ getServerString();
        mTextServer.setText(server);
        String hostname = "Hostname: "+ getHostnameString();
        mTextHostname.setText(hostname);

        // Start DynDNS service
        dyndns mDynDns = new dyndns();
        mServiceIntent = new Intent(this, mDynDns.getClass());
        Log.d(TAG, "Starting service");
        if (!isMyServiceRunning(mDynDns.getClass())) {
            startService(mServiceIntent);
        }

        mButtonSave.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        Log.d(TAG, "Saving configuration.");

                        // Read written configuration every time a user chooses to save
                        for ( Map.Entry<String, EditText> entry : edit_text_dictionary.entrySet() ) {
                            String key = entry.getKey();
                            EditText edit_obj = entry.getValue();
                            if (isValidInput(key, edit_obj.getText().toString())){
                                saveParam(key, edit_obj);
                            }
                        }
                        String server = "Server: "+ getServerString();
                        mTextServer.setText(server);
                        String hostname = "Hostname: "+ getHostnameString();
                        mTextHostname.setText(hostname);
                        Toast.makeText(getApplicationContext(), "Configuration saved", Toast.LENGTH_SHORT).show();
                    }
                });

        mButtonStart.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        stopService(mServiceIntent);
                        Toast.makeText(getApplicationContext(), "Service restarted", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getHostnameString() {
        sharedPref = getApplicationContext().getSharedPreferences("configuration", MODE_MULTI_PROCESS );
        String hostname = sharedPref.getString("hostname", "");
        String domain = sharedPref.getString("domain", "");
        if (hostname.isEmpty() && domain.isEmpty()) return "";
        else return hostname + "." + domain;
    }

    private String getServerString() {
        sharedPref = getApplicationContext().getSharedPreferences("configuration", MODE_MULTI_PROCESS );
        String server = sharedPref.getString("server", "");
        String port = sharedPref.getString("port", "");
        if (server.isEmpty() && port.isEmpty()) return "";
        else return server + ":" + port;
    }

    private Boolean isValidInput(String conf, String value){
        if (value.isEmpty()) return true;
        Pattern pattern;
        switch (conf) {
            case "port":
                try {
                    int port_number = Integer.parseInt(value);
                    if (port_number < 65535 && port_number > 0) return true;
                    Toast.makeText(this, "Port input = " + value + " - Port number out of range", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Port input = " + value + " is not a valid integer", Toast.LENGTH_SHORT).show();
                }
                return false;
            case "entry_type":
                if (Arrays.asList(ENTRY_TYPES).contains(value)) return true;
                else {
                    Toast.makeText(this, "Entry type = " + value + " - Entry type is not a valid DNS entry", Toast.LENGTH_SHORT).show();
                    return false;
                }
            case "hostname":
                pattern = Pattern.compile(HOSTNAME_ALLOWED_CHARS);
                if (pattern.matcher(value).matches()) return true;
                else {
                    Toast.makeText(this, "Hostname =  " + value + " - Hostname contains illegal characters", Toast.LENGTH_SHORT).show();
                    return false;
                }
            case "server":
                pattern = Pattern.compile(SERVER_URL_ALLOWED_CHARS);
                if (pattern.matcher(value).matches()) return true;
                else {
                    Toast.makeText(this, "Server = " + value + " - Server is not a valid web URL", Toast.LENGTH_SHORT).show();
                    return false;
                }
            case "domain":
                pattern = Pattern.compile(DOMAIN_ALLOWED_CHARS);
                if (pattern.matcher(value).matches()) return true;
                else {
                    Toast.makeText(this, "Domain = " + value + " - Domain contains illegal characters", Toast.LENGTH_SHORT).show();
                    return false;
                }
            case "ttl":
                try {
                    Double ttl_value = Double.parseDouble(value);
                    if (ttl_value > 0) return true;
                    Toast.makeText(this, "TTL = " + value + " - TTL must be positive", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "TTL = " + value + " - TTL not a valid Double", Toast.LENGTH_SHORT).show();
                }
        }
        return false;
    }

    private void saveParam(String name, EditText text){
        SharedPreferences.Editor editor = sharedPref.edit();
        String result = text.getText().toString();
        // If user wrote a value save it as the new configuration
        if (!result.isEmpty()) editor.putString(name, result);
        else editor.remove(name);
        editor.apply();
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
