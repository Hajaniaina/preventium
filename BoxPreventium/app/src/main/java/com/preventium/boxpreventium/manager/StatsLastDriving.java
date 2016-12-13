package com.preventium.boxpreventium.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.preventium.boxpreventium.database.DBHelper;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.SCORE_t;
import com.preventium.boxpreventium.server.DOBJ.DataDOBJ;
import com.preventium.boxpreventium.utils.ComonUtils;

import java.util.Locale;

import static android.R.attr.type;

/**
 * Created by Franck on 16/11/2016.
 */

public class StatsLastDriving {

    /// ============================================================================================
    /// Share preferences key
    /// ============================================================================================

    /// Share preference key
    private final static String KEY_STAT = "STAT";
    private final static String KEY_PREF_OBJ = "objectif_%s_%s";
    private final static String KEY_PREF_RES = "result_%s_%s";
    private final static String KEY_PREF_ST = "startAt";
    private final static String KEY_PREF_NOTE = "note";
    private final static String KEY_PREF_T = "time";
    private final static String KEY_PREF_D = "distance";

    /// Event type names
    public final static String ACCELERATIONS = "A";
    public final static String FREINAGES = "F";
    public final static String VIRAGES = "V";
    /// Level names
    public final static String VERT = "V";
    public final static String BLEU = "B";
    public final static String JAUNE = "J";
    public final static String ORANGE = "O";
    public final static String ROUGE = "R";

    /// ============================================================================================
    /// INITIALIZE
    /// ============================================================================================

    public static void startDriving( Context ctx, long parcour_id ){
        clear(ctx);
        init_objectif(ctx);
        set_start_at(ctx, parcour_id);
    }

    public static void clear(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    /// ============================================================================================
    /// CHECK IF DATA READY
    /// ============================================================================================

    public static boolean dataIsAvailable(Context ctx) {
        return ( get_start_at(ctx) > 0
                && ( objectifIsAvailable(ctx,SCORE_t.ACCELERATING)
                || objectifIsAvailable(ctx,SCORE_t.BRAKING)
                || objectifIsAvailable(ctx,SCORE_t.CORNERING)
                || resultIsAvailable(ctx,SCORE_t.ACCELERATING)
                || resultIsAvailable(ctx,SCORE_t.BRAKING)
                || resultIsAvailable(ctx,SCORE_t.CORNERING) ) );
    }

    public static boolean objectifIsAvailable(Context ctx, SCORE_t score_t){
        switch ( score_t ) {
            case CORNERING:
                return (get_start_at(ctx) > 0
                        && ( get_objectif_V(ctx, LEVEL_t.LEVEL_1) > 0
                        || get_objectif_V(ctx, LEVEL_t.LEVEL_2) > 0
                        || get_objectif_V(ctx, LEVEL_t.LEVEL_3) > 0
                        || get_objectif_V(ctx, LEVEL_t.LEVEL_4) > 0
                        || get_objectif_V(ctx, LEVEL_t.LEVEL_5) > 0) );
            case BRAKING:
                return (get_start_at(ctx) > 0
                        && ( get_objectif_F(ctx, LEVEL_t.LEVEL_1) > 0
                        || get_objectif_F(ctx, LEVEL_t.LEVEL_2) > 0
                        || get_objectif_F(ctx, LEVEL_t.LEVEL_3) > 0
                        || get_objectif_F(ctx, LEVEL_t.LEVEL_4) > 0
                        || get_objectif_F(ctx, LEVEL_t.LEVEL_5) > 0) );
            case ACCELERATING:
                return (get_start_at(ctx) > 0
                        && ( get_objectif_A(ctx, LEVEL_t.LEVEL_1) > 0
                        || get_objectif_A(ctx, LEVEL_t.LEVEL_2) > 0
                        || get_objectif_A(ctx, LEVEL_t.LEVEL_3) > 0
                        || get_objectif_A(ctx, LEVEL_t.LEVEL_4) > 0
                        || get_objectif_A(ctx, LEVEL_t.LEVEL_5) > 0) );

        }
        return false;
    }

    public static boolean resultIsAvailable(Context ctx, SCORE_t score_t){
        switch ( score_t ) {
            case CORNERING:
                return (get_start_at(ctx) > 0
                        && ( get_resultat_V(ctx, LEVEL_t.LEVEL_1) > 0
                        || get_resultat_V(ctx, LEVEL_t.LEVEL_2) > 0
                        || get_resultat_V(ctx, LEVEL_t.LEVEL_3) > 0
                        || get_resultat_V(ctx, LEVEL_t.LEVEL_4) > 0
                        || get_resultat_V(ctx, LEVEL_t.LEVEL_5) > 0) );
            case BRAKING:
                return (get_start_at(ctx) > 0
                        && ( get_resultat_F(ctx, LEVEL_t.LEVEL_1) > 0
                        || get_resultat_F(ctx, LEVEL_t.LEVEL_2) > 0
                        || get_resultat_F(ctx, LEVEL_t.LEVEL_3) > 0
                        || get_resultat_F(ctx, LEVEL_t.LEVEL_4) > 0
                        || get_resultat_F(ctx, LEVEL_t.LEVEL_5) > 0) );
            case ACCELERATING:
                return (get_start_at(ctx) > 0
                        && (get_resultat_A(ctx, LEVEL_t.LEVEL_1) > 0
                        || get_resultat_A(ctx, LEVEL_t.LEVEL_2) > 0
                        || get_resultat_A(ctx, LEVEL_t.LEVEL_3) > 0
                        || get_resultat_A(ctx, LEVEL_t.LEVEL_4) > 0
                        || get_resultat_A(ctx, LEVEL_t.LEVEL_5) > 0 ));

        }

        return false;
    }

    /// ============================================================================================
    /// PHONE INFO
    /// ============================================================================================

    public static String getIMEI(Context ctx){ return ComonUtils.getIMEInumber(ctx); }

    /// ============================================================================================
    /// PARCOURS INFO
    /// ============================================================================================

    /// GETTER
    public static long get_start_at(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        return sp.getLong(KEY_PREF_ST,0);
    }

    public static float get_note(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        return sp.getFloat(KEY_PREF_NOTE,0f);
    }

    public static long get_distance(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        return sp.getLong(KEY_PREF_D,0);
    }

    public static long get_times(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        return sp.getLong(KEY_PREF_T,0);
    }

    /// SETTER
    public static void set_start_at(Context ctx, long timestamp) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(KEY_PREF_ST,timestamp);
        editor.apply();
    }

    public static void set_note(Context ctx, float note){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(KEY_PREF_NOTE,note);
        editor.apply();
    }

    public static void set_distance(Context ctx, long meters) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(KEY_PREF_D,meters);
        editor.apply();
    }

    public static void set_times(Context ctx, long secs) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(KEY_PREF_T,secs);
        editor.apply();
    }

    /// ============================================================================================
    /// OBJECTIFS
    /// ============================================================================================

    /// GETTER
    /// Accelerations
    public static int get_objectif_A(Context ctx, LEVEL_t level){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        String key = "";
        switch ( level ){
            case LEVEL_UNKNOW:
                break;
            case LEVEL_1:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,ACCELERATIONS,VERT);
                break;
            case LEVEL_2:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,ACCELERATIONS,BLEU);
                break;
            case LEVEL_3:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,ACCELERATIONS,JAUNE);
                break;
            case LEVEL_4:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,ACCELERATIONS,ORANGE);
                break;
            case LEVEL_5:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,ACCELERATIONS,ROUGE);
                break;
        }
        if( !key.isEmpty() ) return sp.getInt(key,0);
        return 0;
    }
    /// Freinage
    public static int get_objectif_F(Context ctx, LEVEL_t level){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        String key = "";
        switch ( level ){
            case LEVEL_UNKNOW:
                break;
            case LEVEL_1:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,FREINAGES,VERT);
                break;
            case LEVEL_2:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,FREINAGES,BLEU);
                break;
            case LEVEL_3:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,FREINAGES,JAUNE);
                break;
            case LEVEL_4:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,FREINAGES,ORANGE);
                break;
            case LEVEL_5:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,FREINAGES,ROUGE);
                break;
        }
        if( !key.isEmpty() ) return sp.getInt(key,0);
        return 0;
    }
    /// Virages
    public static int get_objectif_V(Context ctx, LEVEL_t level){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        String key = "";
        switch ( level ){
            case LEVEL_UNKNOW:
                break;
            case LEVEL_1:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,VIRAGES,VERT);
                break;
            case LEVEL_2:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,VIRAGES,BLEU);
                break;
            case LEVEL_3:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,VIRAGES,JAUNE);
                break;
            case LEVEL_4:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,VIRAGES,ORANGE);
                break;
            case LEVEL_5:
                key = String.format(Locale.getDefault(),KEY_PREF_OBJ,VIRAGES,ROUGE);
                break;
        }
        if( !key.isEmpty() ) return sp.getInt(key,0);
        return 0;
    }

    /// SETTER
    public static void init_objectif(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        // Accelerations
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,ACCELERATIONS,VERT),
                DataDOBJ.get_objectif(ctx,DataDOBJ.ACCELERATIONS,DataDOBJ.VERT) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,ACCELERATIONS,BLEU),
                DataDOBJ.get_objectif(ctx,DataDOBJ.ACCELERATIONS,DataDOBJ.BLEU) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,ACCELERATIONS,JAUNE),
                DataDOBJ.get_objectif(ctx,DataDOBJ.ACCELERATIONS,DataDOBJ.JAUNE) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,ACCELERATIONS,ORANGE),
                DataDOBJ.get_objectif(ctx,DataDOBJ.ACCELERATIONS,DataDOBJ.ORANGE) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,ACCELERATIONS,ROUGE),
                DataDOBJ.get_objectif(ctx,DataDOBJ.ACCELERATIONS,DataDOBJ.ROUGE) );
        // Freinages
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,FREINAGES,VERT),
                DataDOBJ.get_objectif(ctx,DataDOBJ.FREINAGES,DataDOBJ.VERT) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,FREINAGES,BLEU),
                DataDOBJ.get_objectif(ctx,DataDOBJ.FREINAGES,DataDOBJ.BLEU) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,FREINAGES,JAUNE),
                DataDOBJ.get_objectif(ctx,DataDOBJ.FREINAGES,DataDOBJ.JAUNE) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,FREINAGES,ORANGE),
                DataDOBJ.get_objectif(ctx,DataDOBJ.FREINAGES,DataDOBJ.ORANGE) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,FREINAGES,ROUGE),
                DataDOBJ.get_objectif(ctx,DataDOBJ.FREINAGES,DataDOBJ.ROUGE) );
        // Virages
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,VIRAGES,VERT),
                DataDOBJ.get_objectif(ctx,DataDOBJ.VIRAGES,DataDOBJ.VERT) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,VIRAGES,BLEU),
                DataDOBJ.get_objectif(ctx,DataDOBJ.VIRAGES,DataDOBJ.BLEU) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,VIRAGES,JAUNE),
                DataDOBJ.get_objectif(ctx,DataDOBJ.VIRAGES,DataDOBJ.JAUNE) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,VIRAGES,ORANGE),
                DataDOBJ.get_objectif(ctx,DataDOBJ.VIRAGES,DataDOBJ.ORANGE) );
        editor.putInt( String.format(Locale.getDefault(),KEY_PREF_OBJ,VIRAGES,ROUGE),
                DataDOBJ.get_objectif(ctx,DataDOBJ.VIRAGES,DataDOBJ.ROUGE) );

        editor.apply();
    }

    /// ============================================================================================
    /// RESULTATS
    /// ============================================================================================

    /// GETTER
    /// Accelerations
    public static int get_resultat_A(Context ctx, LEVEL_t level){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        String key = "";
        switch ( level ){
            case LEVEL_UNKNOW:
                break;
            case LEVEL_1:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,ACCELERATIONS,VERT);
                break;
            case LEVEL_2:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,ACCELERATIONS,BLEU);
                break;
            case LEVEL_3:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,ACCELERATIONS,JAUNE);
                break;
            case LEVEL_4:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,ACCELERATIONS,ORANGE);
                break;
            case LEVEL_5:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,ACCELERATIONS,ROUGE);
                break;
        }
        if( !key.isEmpty() ) return sp.getInt(key,0);
        return 0;
    }
    /// Freingaes
    public static int get_resultat_F(Context ctx, LEVEL_t level){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        String key = "";
        switch ( level ){
            case LEVEL_UNKNOW:
                break;
            case LEVEL_1:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,FREINAGES,VERT);
                break;
            case LEVEL_2:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,FREINAGES,BLEU);
                break;
            case LEVEL_3:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,FREINAGES,JAUNE);
                break;
            case LEVEL_4:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,FREINAGES,ORANGE);
                break;
            case LEVEL_5:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,FREINAGES,ROUGE);
                break;
        }
        if( !key.isEmpty() ) return sp.getInt(key,0);
        return 0;
    }
    /// Virages
    public static int get_resultat_V(Context ctx, LEVEL_t level){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
        String key = "";
        switch ( level ){
            case LEVEL_UNKNOW:
                break;
            case LEVEL_1:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,VIRAGES,VERT);
                break;
            case LEVEL_2:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,VIRAGES,BLEU);
                break;
            case LEVEL_3:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,VIRAGES,JAUNE);
                break;
            case LEVEL_4:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,VIRAGES,ORANGE);
                break;
            case LEVEL_5:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,VIRAGES,ROUGE);
                break;
        }
        if( !key.isEmpty() ) return sp.getInt(key,0);
        return 0;
    }

    /// SETTER
    /// Accelerations
    public static void set_resultat_A(Context ctx, LEVEL_t level, int val){
        String key = "";
        switch ( level ){
            case LEVEL_UNKNOW:
                break;
            case LEVEL_1:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,ACCELERATIONS,VERT);
                break;
            case LEVEL_2:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,ACCELERATIONS,BLEU);
                break;
            case LEVEL_3:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,ACCELERATIONS,JAUNE);
                break;
            case LEVEL_4:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,ACCELERATIONS,ORANGE);
                break;
            case LEVEL_5:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,ACCELERATIONS,ROUGE);
                break;
        }
        if( !key.isEmpty() ){
            SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(key,val);
            editor.apply();
        }
    }
    /// Freingaes
    public static void set_resultat_F(Context ctx, LEVEL_t level, int val){
        String key = "";
        switch ( level ){
            case LEVEL_UNKNOW:
                break;
            case LEVEL_1:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,FREINAGES,VERT);
                break;
            case LEVEL_2:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,FREINAGES,BLEU);
                break;
            case LEVEL_3:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,FREINAGES,JAUNE);
                break;
            case LEVEL_4:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,FREINAGES,ORANGE);
                break;
            case LEVEL_5:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,FREINAGES,ROUGE);
                break;
        }
        if( !key.isEmpty() ){
            SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(key,val);
            editor.apply();
        }
    }
    /// Virages
    public static void set_resultat_V(Context ctx, LEVEL_t level, int val){
        String key = "";
        switch ( level ){
            case LEVEL_UNKNOW:
                break;
            case LEVEL_1:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,VIRAGES,VERT);
                break;
            case LEVEL_2:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,VIRAGES,BLEU);
                break;
            case LEVEL_3:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,VIRAGES,JAUNE);
                break;
            case LEVEL_4:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,VIRAGES,ORANGE);
                break;
            case LEVEL_5:
                key = String.format(Locale.getDefault(),KEY_PREF_RES,VIRAGES,ROUGE);
                break;
        }
        if( !key.isEmpty() ){
            SharedPreferences sp = ctx.getSharedPreferences(KEY_STAT, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(key,val);
            editor.apply();
        }
    }

}
