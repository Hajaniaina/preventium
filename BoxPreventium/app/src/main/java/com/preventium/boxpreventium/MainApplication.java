package com.preventium.boxpreventium;

import android.app.Application;
import com.ikalogic.franck.permissions.Perm;

/**
 * Created by Franck on 17/06/2016.
 */

public class MainApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        Perm.initialize(this);
    }
}
