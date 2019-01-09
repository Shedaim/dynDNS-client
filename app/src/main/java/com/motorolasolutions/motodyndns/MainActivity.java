package com.motorolasolutions.motodyndns;

import android.app.Activity;
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
    public SharedPreferences sharedPref;
    public Button mButton;
    public EditText mEdit;
    public TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        mButton = findViewById(R.id.button);
        mEdit = findViewById(R.id.editText);
        mText = findViewById(R.id.textView);

        sharedPref = getApplicationContext().getSharedPreferences("hostname", 0);
        String StoredValue = sharedPref.getString("hostname", "Default");
        mEdit.setHint(StoredValue);
        mText.setText("Hostname: " + StoredValue);

        mButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("hostname", mEdit.getText().toString());
                        editor.commit();
                        mEdit.setHint(mEdit.getText().toString());
                        mText.setText("Hostname: " + mEdit.getText().toString());
                        startService(new Intent(getApplication(), dyndns.class));
                    }
                });
    }
}
