package com.preventium.boxpreventium.manager.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.preventium.boxpreventium.manager.AppManager;

/**
 * Created by tog on 22/12/2018.
 */

public class ServiceManager extends Service {
    private Handler handler = new Handler();
    private final String TAG = "ServiceManager";

    /* task */
    private class Task implements Runnable {
        private int index = 0;
        private Context context;

        public Task (Context context) {
            this.context = context;
        }

        @Override
        public void run() {

            AppManager app = AppManager.getInstance();
            if( app != null && app.isReady() ) {
                app.getContent();
                // Toast.makeText(context, String.valueOf(index), Toast.LENGTH_LONG).show();
                Log.d(TAG, "AppManager: " + String.valueOf(index));
            }

            index++;
            handler.postDelayed(this, 500);
        }
    };

    private Task task;

    @Override
    public void onCreate() {
        super.onCreate();
        task = new Task(this);
        handler.post(task);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if( task != null ) { // rarement true
            handler.removeCallbacks(task);
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
