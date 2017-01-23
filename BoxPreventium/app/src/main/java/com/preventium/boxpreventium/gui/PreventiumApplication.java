package com.preventium.boxpreventium.gui;

import android.app.Application;
import com.devs.acr.AutoErrorReporter;
import com.github.stkent.bugshaker.BugShaker;
import com.github.stkent.bugshaker.flow.dialog.AlertDialogType;

public class PreventiumApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        AutoErrorReporter.get(this)
                .setEmailAddresses("franck@ikalogic.com")
                .setEmailSubject("Preventium Crash Report")
                .start();

        BugShaker.get(this)
                .setEmailAddresses("franck@ikalogic.com")
                .setEmailSubjectLine("Preventium Feedback")
                .setAlertDialogType(AlertDialogType.NATIVE)
                .setLoggingEnabled(true)
                .setIgnoreFlagSecure(true)
                .assemble()
                .start();
    }
}