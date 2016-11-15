package com.preventium.boxpreventium.server.DOBJ;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.Locale;

/**
 * Created by Franck on 05/10/2016.
 */

public class DataDOBJ {

    /// ============================================================================================
    /// Share preferences key
    /// ============================================================================================

    /// Share preference key
    private final static String KEY_OBJ = "OBJ";
    private final static String KEY_PREF_COEFF = "Coeff_%s_%s";
    private final static String KEY_PREF_OBJ = "Obj_%s_%s";
    /// Event type names
    public final static String ACCELERATIONS = "A";
    public final static String FREINAGES = "F";
    public final static String VIRAGES = "V";
    /// Level names
    public final static String GENERAL = "G";
    public final static String VERT = "V";
    public final static String BLEU = "B";
    public final static String JAUNE = "J";
    public final static String ORANGE = "O";
    public final static String ROUGE = "R";

    /// ============================================================================================
    /// PREFERENCE FILE
    /// ============================================================================================

    /// Check if the preference file exist.
    public static boolean preferenceFileExist(Context ctx) {
        File f = new File(ctx.getApplicationContext().getApplicationInfo().dataDir + "/shared_prefs/"
            + KEY_OBJ + ".xml");
        return f.exists();
    }

    /// Remove the preference file.
    public static void removePreferenceFile(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_OBJ, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    /// ============================================================================================
    /// COEFFICIENTS
    /// ============================================================================================

    /// Get general coefficient (value in percent) per a type.
    public static float get_coefficient_general(Context ctx, String type) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_OBJ, Context.MODE_PRIVATE);
        String key = String.format(Locale.getDefault(),KEY_PREF_COEFF,type,GENERAL);
        return sp.getFloat(key,0f);
    }

    /// Set general coefficient (value in percent) per a type.
    public static void set_coefficient_general(Context ctx, String type, float v) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_OBJ, Context.MODE_PRIVATE);
        String key = String.format(Locale.getDefault(),KEY_PREF_COEFF,type,GENERAL);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(key,v);
        editor.apply();
    }

    /// Get coefficient (value in percent) per a type and a level.
    public static int get_coefficient(Context ctx, String type, String level){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_OBJ, Context.MODE_PRIVATE);
        String key = String.format(Locale.getDefault(),KEY_PREF_COEFF,type,level);
        return sp.getInt(key,0);
    }

    /// Set coefficient (value in percent) per type and per level.
    public static void set_coefficient(Context ctx, String type, String level, int percent){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_OBJ, Context.MODE_PRIVATE);
        String key = String.format(Locale.getDefault(),KEY_PREF_COEFF,type,level);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key,percent);
        editor.apply();
    }

    /// ============================================================================================
    /// OBJECTIFS
    /// ============================================================================================

    /// Get objectif (value in percent) per a type and a level.
    public static int get_objectif(Context ctx, String type, String level){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_OBJ, Context.MODE_PRIVATE);
        String key = String.format(Locale.getDefault(),KEY_PREF_OBJ,type,level);
        return sp.getInt(key,0);
    }

    /// Set objectif (value in percent) per a type and a level.
    public static void set_objectif(Context ctx, String type, String level, int percent){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_OBJ, Context.MODE_PRIVATE);
        String key = String.format(Locale.getDefault(),KEY_PREF_OBJ,type,level);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key,percent);
        editor.apply();
    }


}
