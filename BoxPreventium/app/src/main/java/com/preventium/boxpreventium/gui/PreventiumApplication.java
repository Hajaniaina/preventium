package com.preventium.boxpreventium.gui;

import android.app.Application;
import android.content.res.Configuration;

import com.devs.acr.AutoErrorReporter;
import com.preventium.boxpreventium.utils.ComonUtils;

public class PreventiumApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        AutoErrorReporter.get(this)
                .setEmailAddresses("hoanyprojet@gmail.com")
                .setEmailSubject("Preventium Crash Report")
                .start();


        // error handler
        /*
        ErrorException
                .get(this)
                .Start();*/


        ComonUtils.setLanguage(this);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        ComonUtils.setLanguage(this);
    }
}
