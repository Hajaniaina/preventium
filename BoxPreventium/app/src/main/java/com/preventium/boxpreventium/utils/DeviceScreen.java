package com.preventium.boxpreventium.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

public class DeviceScreen {
    private DisplayMetrics dm;
    private double screenInches;
    private double f14x;
    private double f15y;

    public double getDeviceScreen(Activity activity) {
        this.dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(this.dm);
        this.f14x = Math.pow((double) (((float) this.dm.widthPixels) / this.dm.xdpi), 2.0d);
        this.f15y = Math.pow((double) (((float) this.dm.heightPixels) / this.dm.ydpi), 2.0d);
        this.screenInches = Math.sqrt(this.f14x + this.f15y);
        return this.screenInches;
    }
}
