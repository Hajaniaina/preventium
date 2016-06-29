package com.preventium.boxpreventium;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresPermission;

/**
 * Created by Franck on 29/06/2016.
 */

public class CommonUtils {

    public static boolean haveInternetConnected(Context ctx){
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Network[] networks = cm.getAllNetworks();
                NetworkInfo networkInfo;
                for (Network mNetwork : networks) {
                    networkInfo = cm.getNetworkInfo(mNetwork);
                    if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) return true;
                }
            } else {
                NetworkInfo[] info = cm.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean haveMobileConnected(Context ctx){
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Network[] networks = cm.getAllNetworks();
                NetworkInfo networkInfo;
                for (Network mNetwork : networks) {
                    networkInfo = cm.getNetworkInfo(mNetwork);
                    if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) return true;
                }
            } else {
                NetworkInfo[] info = cm.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getType() == ConnectivityManager.TYPE_MOBILE) return true;
                    }
                }
            }
        }
        return false;
    }

    // === WIFI

    public static boolean haveWifiEnabled(Context ctx) {
        WifiManager wm = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
        if( wm != null ) return wm.isWifiEnabled();
        return false;
    }

    public static boolean haveWifiSupport(Context ctx){
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI);
    }

    public static boolean haveWifiConnected(Context ctx){
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Network[] networks = cm.getAllNetworks();
                NetworkInfo networkInfo;
                for (Network mNetwork : networks) {
                    networkInfo = cm.getNetworkInfo(mNetwork);
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) return true;
                }
            } else {
                NetworkInfo[] info = cm.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getType() == ConnectivityManager.TYPE_WIFI) return true;
                    }
                }
            }
        }
        return false;
    }

    // === BLUETOOTH

    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public static boolean haveBluetoothEnabled() {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if( ba != null ) return ba.isEnabled();
        return false;
    }

    public static boolean haveBluetoothSupport(Context ctx){
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }

    public static boolean haveBluetoothLESupport(Context ctx){
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    // === LOCATION

    public static boolean haveLocationEnabled(Context ctx) {
        LocationManager lm = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
        if( lm != null ) return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return false;
    }

    public static boolean haveLocationSupport(Context ctx){
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    // === PHONE

    public static String getIMEInumber(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    // === APK

    public static int getVersionCode(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException ex) {}
        return 0;
    }

    public static String getVersionName(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException ex) {}
        return "0.0.0";
    }

    public static String getAppID(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException ex) {}
        return "0.0.0";
    }

    public static String getPackageName(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
            return pi.packageName;
        } catch (PackageManager.NameNotFoundException ex) {}
        return "";
    }
}

