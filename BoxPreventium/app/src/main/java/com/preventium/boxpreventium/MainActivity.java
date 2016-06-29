package com.preventium.boxpreventium;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;

import android.util.Log;
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
        Log.d( "AAA","getPackageName " + CommonUtils.getPackageName(this) );
        Log.d( "AAA","getVersionName " + CommonUtils.getVersionName(this) );
        Log.d( "AAA","getVersionCode " + CommonUtils.getVersionCode(this) );

        final Intent gpsIntent = new Intent(this, GpsTrackerService.class);
        startService(gpsIntent);


        Handler handler=new Handler();
        Runnable r=new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopService(gpsIntent);
                    }
                });

            }
        };
        handler.postDelayed(r, 50000);


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

