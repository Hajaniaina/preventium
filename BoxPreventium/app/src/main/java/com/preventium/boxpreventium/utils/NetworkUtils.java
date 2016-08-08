package com.preventium.boxpreventium.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by Franck on 08/08/2016.
 */

public class NetworkUtils {

    public static NetworkInfo getActiveNetworkInfo(Context ctxt) {
        NetworkInfo nfo = ((ConnectivityManager) ctxt
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return nfo;
    }

    public static boolean isConnected( Context ctxt ) {
        NetworkInfo nfo = getActiveNetworkInfo( ctxt );
        if (nfo != null) {
            return nfo.isConnected();
        }
        return false;
    }

    public static boolean isWifiType( Context ctxt ) {
        NetworkInfo nfo = getActiveNetworkInfo( ctxt );
        if (nfo != null) {
            return nfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

    public static String currentSSID( Context ctxt ) {
        WifiManager wifiManager = (WifiManager) ctxt.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if ( wifiInfo != null && wifiInfo.getSupplicantState()== SupplicantState.COMPLETED) {
            return wifiInfo.getSSID();
        }
        return "";
    }

    public static String currentBSSID( Context ctxt ) {
        WifiManager wifiManager = (WifiManager) ctxt.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if ( wifiInfo != null && wifiInfo.getSupplicantState()== SupplicantState.COMPLETED) {
            return wifiInfo.getBSSID();
        }
        return "";
    }

}
