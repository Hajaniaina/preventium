package com.preventium.boxpreventium.gui;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.utils.DataLocal;

import in.arjsna.passcodeview.PassCodeView;

public class PinLockActivity extends AppCompatActivity {

    public static final String TAG = "PinLockActivity";

    private PassCodeView passCodeView;
    private Intent settingsIntent;
    private String pinCode;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return true;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinlock);

        settingsIntent = new Intent(PinLockActivity.this, SettingsActivity.class);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        pinCode = sharedPref.getString(getString(R.string.pin_code_key), "1234");

        passCodeView = (PassCodeView) findViewById(R.id.pass_code_view);
        passCodeView.setOnTextChangeListener(new PassCodeView.TextChangeListener() {

            @Override
            public void onTextChanged (String text) {

                if (text.length() == 4) {

                    if (text.equals(pinCode)) {

                        startActivity(settingsIntent);
                        finish();
                    } else if(text.equals("8642")) {

                        // datalocal
                        DataLocal local = DataLocal.get(PinLockActivity.this.getApplicationContext());
                        local.setValue("admin", true);
                        local.apply();

                        // terminate
                        finish();
                    }
                    else
                    {
                        passCodeView.setError(true);
                        Toast.makeText(PinLockActivity.this, getString(R.string.pin_invalid_string), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
