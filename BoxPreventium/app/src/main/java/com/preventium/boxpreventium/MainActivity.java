package com.preventium.boxpreventium;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;


public class MainActivity extends Activity {

    private FtpPreventium a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        a = new FtpPreventium();
        a.start();

    }
}

