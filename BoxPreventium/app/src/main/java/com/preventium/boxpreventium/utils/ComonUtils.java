package com.preventium.boxpreventium.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.server.CFG.ReaderCFGFile;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.util.Base64.decode;

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
        // return "357726081420365";

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

    public static long difference(long a, long b) {
        return Math.abs(a - b);
    }

    public static String currentDateTime() {
        return new SimpleDateFormat("d-MMM-yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
    }

    public  static float round (float d) {

        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        return bd.floatValue();
    }


    //######### Older paste

    public static void SavePreferences(String key, String name, int value, Context c) {
        SharedPreferences.Editor editor = c.getSharedPreferences(key, 0).edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public static int LoadPreferences(String key, String name, Context c) {
        return c.getSharedPreferences(key, 0).getInt(name, 0);
    }

    public static void SaveStringPreferences(String key, String name, String value, Context c) {
        SharedPreferences.Editor editor = c.getSharedPreferences(key, 0).edit();
        editor.putString(name, value);
        editor.commit();
    }

    public static String LoadStringPreferences(String key, String name, Context c) {
        String val = "";
        return c.getSharedPreferences(key, 0).getString(name, "");
    }

    public static String currentTime() {
        return new SimpleDateFormat("d-MM-yy HH:mm:ss", Locale.getDefault()).format(Long.valueOf(System.currentTimeMillis()));
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public static String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    public static String decryt (String crypt, String key) {
        if( crypt.equals("") || key.equals("") ) return null;
        final byte[] decodedByteArray = decode(crypt, Base64.DEFAULT);
        String s = new String(decodedByteArray);
        return s.replace(key, "");
    }

    public static ReaderCFGFile getCFG (Context context) {
        String srcFileName = ComonUtils.getIMEInumber(context.getApplicationContext()) + ".CFG";
        String desFileName = String.format(Locale.getDefault(), "%s/%s", context.getApplicationContext().getFilesDir(), srcFileName);
        ReaderCFGFile cfg =  new ReaderCFGFile();
        cfg.read(desFileName);
        return cfg;
    }

    public static String getServer (Context context) {
        DataLocal local = DataLocal.get(context.getApplicationContext());
        return (String)local.getValue("cfg_server", "https://test.preventium.fr");
    }

    //######### Older paste
    public static void setLanguage (Context context) {
        Application main = (Application)context;
        SharedPreferences preferences = main.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String language = preferences.getString(main.getString(R.string.select_language_key), null);
        language = language == null ? Locale.getDefault().getLanguage() : language;

        // setting resources and others
        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();

        // update if needed
        if( !configuration.locale.equals(language) ) {
            configuration.setLocale(new Locale(language.toLowerCase()));
            res.updateConfiguration(configuration, null);
        }
    }

    public static boolean is_tablet (Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        Activity activity = (Activity) context;
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float yInches= metrics.heightPixels/metrics.ydpi;
        float xInches= metrics.widthPixels/metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches);
        return diagonalInches >= 6.5 ? true : false;
    }
}
