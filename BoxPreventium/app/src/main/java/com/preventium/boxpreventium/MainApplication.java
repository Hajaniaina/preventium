package com.preventium.boxpreventium;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.ikalogic.franck.permissions.Perm;

/**
 * Created by Franck on 29/06/2016.
 */

public class MainApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "MainApplication";
    protected static Context context;
    protected static Activity activity;

    @Override
    public void onCreate() {
        super.onCreate();
        Perm.initialize(this);
        MainApplication.context = getApplicationContext();
        registerActivityLifecycleCallbacks(this);
    }

    public static Context getAppContext(){ return MainApplication.context; }

    public static Activity getCurrentActivity() { return  MainApplication.activity; }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        MainApplication.context = activity;
    }

    @Override
    public void onActivityStarted(Activity activity) { MainApplication.context = activity;}

    @Override
    public void onActivityResumed(Activity activity) { MainApplication.context = activity; }

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}
