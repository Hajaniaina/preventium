package com.preventium.boxpreventium.gui;

import android.app.Application;
import com.devs.acr.AutoErrorReporter;

public class PreventiumApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
//#####report beug
        AutoErrorReporter.get(this)
                .setEmailAddresses("foliosantoni@gmail.com")
                .setEmailSubject("Preventium Crash Report")
                .start();
//#####report beug
        /*
        AutoErrorReporter.get(this)
                .setEmailAddresses("")
                .setEmailSubject("Preventium Crash Report")
                .start();*/
    }
}
