package com.preventium.boxpreventium.utils;

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
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Franck on 21/09/2016.
 */

public class ComonUtils {

    private final static String TAG = "ComonUtils";


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

    public static void printFhoneInfo(Context ctx){
        //Get the instance of TelephonyManager
        TelephonyManager tm=(TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
        //Calling the methods of TelephonyManager the returns the information
        String IMEINumber=tm.getDeviceId();
        String subscriberID=tm.getDeviceId();
        String SIMSerialNumber=tm.getSimSerialNumber();
        String networkCountryISO=tm.getNetworkCountryIso();
        String SIMCountryISO=tm.getSimCountryIso();
        String softwareVersion=tm.getDeviceSoftwareVersion();
        String voiceMailNumber=tm.getVoiceMailNumber();
        //Get the phone type
        String strphoneType="";
        int phoneType=tm.getPhoneType();
        switch (phoneType)
        {
            case (TelephonyManager.PHONE_TYPE_CDMA):
                strphoneType="CDMA";
                break;
            case (TelephonyManager.PHONE_TYPE_GSM):
                strphoneType="GSM";
                break;
            case (TelephonyManager.PHONE_TYPE_NONE):
                strphoneType="NONE";
                break;
        }
        //getting information if phone is in roaming
        boolean isRoaming=tm.isNetworkRoaming();
        String info="Phone Details:\n";
        info+="\n IMEI Number:"+IMEINumber;
        info+="\n SubscriberID:"+subscriberID;
        info+="\n Sim Serial Number:"+SIMSerialNumber;
        info+="\n Network Country ISO:"+networkCountryISO;
        info+="\n SIM Country ISO:"+SIMCountryISO;
        info+="\n Software Version:"+softwareVersion;
        info+="\n Voice Mail Number:"+voiceMailNumber;
        info+="\n Phone Network Type:"+strphoneType;
        info+="\n In Roaming? :"+isRoaming;
        Log.d(TAG,info);
    }

    public static String getIMEInumber(Context ctx) {
//return "358656074739746";
        //Get the instance of TelephonyManager
        TelephonyManager tm = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
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

    // === Other

    public static double interval(double d1, double d2){
        double ret = d1 - d2;
        if( ret < 0.0 ) ret = -ret;
        return ret;
    }

    public static String currentDateTime() {
        return new SimpleDateFormat("d-MMM-yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
    }
}
