package com.preventium.boxpreventium.module.ErrorEmail;

import android.content.Context;
import android.content.Intent;

import com.preventium.boxpreventium.gui.MainActivity;
import com.preventium.boxpreventium.utils.DataLocal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Created by tog on 30/08/2018.
 */

public class ErrorException {

    private static ErrorException error = null;
    private Context context;
    private boolean IsSent = false;
    private Throwable throwable;

    public static ErrorException get(Context context) {
        if( error == null ) error = new ErrorException(context);
        return error;
    }

    public static ErrorException get() {
        if( error == null ) error = new ErrorException();
        return error;
    }

    public ErrorException (Context context) {
        this.context = context;
    }

    public ErrorException () {
        // this.context = context;
    }

    public void restartApp () {
        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("crashApp", true);

            context.startActivity(intent);
        }catch(Exception e) {}
    }

    public void killAndSurvive () {
        // restart
        restartApp();
        // make sure we die, otherwise the app will hang ...
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);

    }

    public void sendData () {

        String content = "";
        if( this.throwable != null ) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(out);
            this.throwable.printStackTrace(ps);
            content = out.toString();
        }

        try {

            DataLocal local = DataLocal.get(context);
            int time = (int) local.getValue("crashNumber", 0);
            time += 1;
            local.setValue("crashApp", true);
            local.setValue("crashTrace", content);
            local.setValue("crashNumber", time);
            local.apply();

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void Start() {

        final Thread.UncaughtExceptionHandler Handler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {

                /*
                if( Handler != null ) {
                    Handler.uncaughtException(thread, throwable);
                }
                */

                if( Handler != null ) {

                    // exception
                    ErrorException.this.throwable = throwable;
                    // sent data
                    ErrorException.this.sendData();
                    // kill
                    ErrorException.this.killAndSurvive();

                } else
                    System.exit(2);
            }
        });

    }

}
