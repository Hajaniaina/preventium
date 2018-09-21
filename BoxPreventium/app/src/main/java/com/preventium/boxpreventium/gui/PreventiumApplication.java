package com.preventium.boxpreventium.gui;

import android.app.Application;

import com.preventium.boxpreventium.module.ErrorEmail.ErrorException;

public class PreventiumApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        /*
        AutoErrorReporter.get(this)
                .setEmailAddresses("hoanyprojet@gmail.com")
                .setEmailSubject("Preventium Crash Report")
                .start();
        */

        // error handler
        /*ErrorException
                .get(this)
                .Start();
                */
    }
}
