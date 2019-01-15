package com.preventium.boxpreventium.gui;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.provider.Telephony;

import com.devs.acr.AutoErrorReporter;
import com.preventium.boxpreventium.receiver.SMSReceiver;
import com.preventium.boxpreventium.utils.ComonUtils;
// import com.squareup.leakcanary.LeakCanary;
// import com.squareup.leakcanary.RefWatcher;

public class PreventiumApplication extends Application {

    private static Context context;
    private static SMSReceiver smsReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        // the context base
        PreventiumApplication.context = getApplicationContext();

        // report error
        AutoErrorReporter.get(this)
                .setEmailAddresses("hoanyprojet@gmail.com")
                .setEmailSubject("Preventium Crash Report")
                .start();

        // error handler
        /*
        ErrorException
                .get(this)
                .Start();*/

        // set sms receiver
        smsReceiver = new SMSReceiver();
        registerReceiver(smsReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));

        // set laguage
        ComonUtils.setLanguage(this);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        ComonUtils.setLanguage(this);
    }

    public static Context getContext () {
        return PreventiumApplication.context;
    }
    public static SMSReceiver getSmsReceiver () {
        return PreventiumApplication.smsReceiver;
    }

    @Override
    public void onTerminate() {
        unregisterReceiver(smsReceiver);
        super.onTerminate();
    }
}
