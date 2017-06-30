package com.preventium.boxpreventium.gui;

import android.app.Application;
import com.devs.acr.AutoErrorReporter;

public class PreventiumApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        /*
        AutoErrorReporter.get(this)
                .setEmailAddresses("")
                .setEmailSubject("Preventium Crash Report")
                .start();*/
    }
}
