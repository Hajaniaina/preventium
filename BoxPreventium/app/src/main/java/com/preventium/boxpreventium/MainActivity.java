package com.preventium.boxpreventium;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private FtpPreventium a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //a = new FtpPreventium();
        //a.start();
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(msg);
            smsManager.sendMultipartTextMessage(phoneNo, null, parts, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

}

