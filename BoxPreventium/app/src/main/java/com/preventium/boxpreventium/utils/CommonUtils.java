package com.preventium.boxpreventium.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;

import com.preventium.boxpreventium.R;

import org.jetbrains.annotations.Contract;

import java.io.File;

/**
 * Created by Franck on 29/06/2016.
 */

public class CommonUtils {

    // === PERMISSIONS LIST

    private static String[] listOfPermissions() {
        return concatenateStringArrays(
                listOfNormalPermissions(),
                listOfDangerousPermissions() );
    }

    @Contract(pure = true)
    private static String[] listOfNormalPermissions() {
        String ret[] = {Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE};
        return ret;
    }

    @Contract(pure = true)
    private static String[] listOfDangerousPermissions() {
        String ret[] = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE};
        return ret;
    }

    public static boolean havePermissionsReady(Context ctx) {
        for( String permission : listOfPermissions() ) {
            if( !( PackageManager.PERMISSION_GRANTED
                    == ContextCompat.checkSelfPermission( ctx, permission ) ) ) return false;
        }
        return true;
    }

    // === INTERNET

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
        WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        return wm != null && wm.isWifiEnabled();
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

    // === SETTINGS

    public static void showSettingDialog(final Context ctx, int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(android.R.drawable.ic_menu_help);
        builder.setPositiveButton(R.string.setting_dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                ctx.startActivity( intent );
            }
        });
        builder.setNegativeButton(R.string.setting_dialog_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    // === BLUETOOTH

    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public static boolean haveBluetoothEnabled() {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if( ba != null ) return ba.isEnabled();
        return false;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public static boolean setBluetoothEnabled( boolean enabled ) {
        boolean ret = false;
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if( ba != null ) {
            if (enabled)
                ret = ba.enable();
            else
                ret = ba.disable();
        }
        return ret;
    }

    public static boolean haveBluetoothSupport(Context ctx){
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }

    public static boolean haveBluetoothLESupport(Context ctx){
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static void showBluetoothSettingDialog(final Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.bluetooth_dialog_title);
        builder.setMessage(R.string.bluetooth_dialog_message);
        builder.setIcon(android.R.drawable.ic_menu_help);
        builder.setPositiveButton(R.string.bluetooth_dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ctx.startActivity( intent );
            }
        });
        builder.setNegativeButton(R.string.bluetooth_dialog_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    // === LOCATION

    public static boolean haveLocationEnabled(Context ctx) {
        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        return lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean haveLocationSupport(Context ctx){
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    public static void showLocationSettingDialog(final Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.location_dialog_title);
        builder.setMessage(R.string.location_dialog_message);
        builder.setIcon(android.R.drawable.ic_menu_help);
        builder.setPositiveButton(R.string.location_dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                ctx.startActivity( intent );
            }
        });
        builder.setNegativeButton(R.string.location_dialog_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
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

    // Others

    public static String dataToHex( byte[] data ){
        String ret = "";
        for (int i = 0; i < data.length; i++) {
            if( !ret.isEmpty() ) ret += " ";
            ret += "0x" + Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1);
        }
        return ret;
    }

    public static String dataToString( byte[] data ){
        String ret = "";
        for (int i = 0; i < data.length; i++)
            ret += (char)data[i];
        return ret;
    }

    public static String dataToDecimal( byte[] data ){
        String ret = "";
        for (int i = 0; i < data.length; i++) {
            if( !ret.isEmpty() ) ret += " ";
            ret += Integer.toString((data[i] & 0xff) + 0x100, 10).substring(1);
        }
        return ret;
    }

    public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static String[] concatenateStringArrays(String[] a, String[] b) {
        String[] result = new String[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

}

