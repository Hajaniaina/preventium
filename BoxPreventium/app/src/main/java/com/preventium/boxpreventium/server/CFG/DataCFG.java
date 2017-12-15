package com.preventium.boxpreventium.server.CFG;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPConfig;

import java.util.Locale;

/**
 * Created by Franck on 23/09/2016.
 */

public class DataCFG {

    private final static String KEY_CFG = "CFGConfig";
    private final static String KEY_Host = "Host";
    private final static String KEY_Login = "Login";
    private final static String KEY_Pwd = "Pwd";
    private final static String KEY_Port = "Port";
    private final static String KEY_Path = "Path";
    private final static String KEY_Sms_Call = "SmsCall%d";
    private final static String KEY_Send_In_Real_Time = "SendInRealTime";
    private final static String KEY_Simple_Gps_Points = "SimpleGpsPoints";
    private final static String KEY_Server = "ServerUrl";

    /// GETTERS

    public static FTPConfig getFptConfig(Context ctx){
        FTPConfig ret = null;
        SharedPreferences sp = ctx.getSharedPreferences(KEY_CFG, Context.MODE_PRIVATE);
        String FTP = sp.getString(KEY_Host,"");
        String FTP_Login = sp.getString(KEY_Login,"");
        String FTP_pwd = sp.getString(KEY_Pwd,"");
        int FTP_Port = sp.getInt(KEY_Port,-1);
        String FTP_Path = sp.getString(KEY_Path,"");
        String FTP_server = sp.getString(KEY_Server,"");
        if( !FTP.isEmpty() )
            ret = new FTPConfig(FTP,FTP_Login,FTP_pwd,FTP_Port,FTP_Path);
        return ret;
    }

    public static String get_SMS_CALL(Context ctx, int i){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_CFG, Context.MODE_PRIVATE);
        return sp.getString( String.format(Locale.getDefault(),KEY_Sms_Call,i), "");
    }

    public static boolean get_SEND_IN_REAL_TIME(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_CFG, Context.MODE_PRIVATE);
        return sp.getBoolean( KEY_Send_In_Real_Time, false);
    }

    public static boolean get_SEND_ALL_GPS_POINTS(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_CFG, Context.MODE_PRIVATE);
        return sp.getBoolean( KEY_Simple_Gps_Points, true);
    }

    /// SETTERS

    public static void setFTPConfig(Context ctx, FTPConfig config) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_CFG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_Host, (config!=null) ? config.getFtpServer() : "" );
        editor.putString(KEY_Login, (config!=null) ? config.getUsername() : "" );
        editor.putString(KEY_Pwd, (config!=null) ? config.getPassword() : "" );
        editor.putInt(KEY_Port, (config!=null) ? config.getPort() : -1 );
        editor.putString(KEY_Path, (config!=null) ? config.getWorkDirectory() : "" );
        editor.apply();
    }

    public static void set_SMS_CALL(Context ctx, int i, String value){
        if( i >= 1 && i <= 5 ) {
            SharedPreferences sp = ctx.getSharedPreferences(KEY_CFG, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(String.format(Locale.getDefault(),KEY_Sms_Call,i), (value!=null) ? value : "" );
            editor.apply();
        }
    }

    public static void set_SEND_IN_REAL_TIME(Context ctx, boolean enable) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_CFG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_Send_In_Real_Time, enable );
        editor.apply();
    }

    public static void set_SEND_ALL_GPS_POINTS(Context ctx, boolean enable) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_CFG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_Simple_Gps_Points, enable );
        editor.apply();
    }

    public static void set_prefs_to_UI(Context ctx) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString( ctx.getResources().getString(R.string.phone_number_1_key),
                DataCFG.get_SMS_CALL(ctx,1) );
        editor.putString( ctx.getResources().getString(R.string.phone_number_2_key),
                DataCFG.get_SMS_CALL(ctx,2) );
        editor.putString( ctx.getResources().getString(R.string.phone_number_3_key),
                DataCFG.get_SMS_CALL(ctx,3) );
        editor.putString( ctx.getResources().getString(R.string.phone_number_4_key),
                DataCFG.get_SMS_CALL(ctx,4) );
        editor.putString( ctx.getResources().getString(R.string.phone_number_5_key),
                DataCFG.get_SMS_CALL(ctx,5) );

        editor.apply();
    }
}
