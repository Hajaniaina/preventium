package com.preventium.boxpreventium.server.EPC;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Marino on 30/11/2017.
 */

public class NameEPC {
    private final static String TAG = "NameEPC";
    private final static String KEY_EPC_NAME = "EPCname";
    private final static String KEY_name = "Name%d";


    /// GETTERS

    public static String get_EPC_Name(Context ctx, int i){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_EPC_NAME, Context.MODE_PRIVATE);
        return sp.getString( String.format(Locale.getDefault(),KEY_name,i), "");
    }



    /// SETTERS

    public static void set_EPC_Name(Context ctx, String value, int i) {
        Log.d(TAG, "EPCvalue name \"" + value + "\"");
        SharedPreferences sp = ctx.getSharedPreferences(KEY_EPC_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString( String.format(KEY_name,i), (value!=null) ? value : "" );
        editor.apply();
    }



}
