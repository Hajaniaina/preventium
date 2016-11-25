package com.preventium.boxpreventium.gui;

import android.app.Application;
import com.github.stkent.bugshaker.BugShaker;
import com.github.stkent.bugshaker.flow.dialog.AlertDialogType;
import com.preventium.boxpreventium.BuildConfig;

public final class CustomApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        BugShaker.get(this)
                .setEmailAddresses("v.kosinov@gmail.com")
                .setLoggingEnabled(BuildConfig.DEBUG)
                .setAlertDialogType(AlertDialogType.APP_COMPAT)
                .assemble()
                .start();
    }
}
