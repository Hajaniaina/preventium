package com.preventium.boxpreventium.server.EPC;

import android.content.Context;
import android.content.SharedPreferences;

import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Franck on 05/10/2016.
 */

public class DataEPC {

    private final static String KEY_EPC = "EPC%d";
    private final static String KEY_IDAlert = "IDAlert%s";
    private final static String KEY_Tps = "Tps%s";
    private final static String KEY_SeuilBas = "SeuilBas%s";
    private final static String KEY_SeuilHaut = "SeuilHaut%s";
    private final static String KEY_LatLong = "LatLong";

    public static List<Integer> getAppEpcExist(Context ctx){
        List<Integer> ret = new ArrayList<Integer>();
        for( int i = 1; i <= 5; i++ ){
            if( preferenceFileExist(ctx,i) ) ret.add(i);
        }
        return ret;
    }

    public static boolean preferenceFileExist(Context ctx, int epc) {
        File f = new File(ctx.getApplicationContext().getApplicationInfo().dataDir + "/shared_prefs/"
                + String.format(Locale.getDefault(),KEY_EPC,epc) + ".xml");
        return f.exists();
    }

    public static void removePreferenceFile(Context ctx, int epc) {
        SharedPreferences sp = ctx.getSharedPreferences(
                String.format(Locale.getDefault(),KEY_EPC,epc), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    public static void setEPCLine(Context ctx, int epc, int i, ForceSeuil seuil) {
        SharedPreferences sp = ctx.getSharedPreferences(
                String.format(Locale.getDefault(),KEY_EPC,epc), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt( String.format(Locale.getDefault(),KEY_IDAlert,i), seuil.IDAlert );
        editor.putInt( String.format(Locale.getDefault(),KEY_Tps,i), seuil.TPS );
        editor.putFloat( String.format(Locale.getDefault(),KEY_SeuilBas,i), (float)seuil.mG_low );
        editor.putFloat( String.format(Locale.getDefault(),KEY_SeuilHaut,i), (float)seuil.mG_high );
        editor.apply();
    }

    public static ForceSeuil getEPCLine(Context ctx, int epc, int i) {
        SharedPreferences sp = ctx.getSharedPreferences(
                String.format(Locale.getDefault(),KEY_EPC,epc), Context.MODE_PRIVATE);

        ForceSeuil seuil = new ForceSeuil();
        seuil.index = i;
        seuil.IDAlert = (short)sp.getInt( String.format(Locale.getDefault(),KEY_IDAlert,i), -1 );
        seuil.TPS = (short)sp.getInt( String.format(Locale.getDefault(),KEY_Tps,i), -1 );
        seuil.mG_low = Math.round( sp.getFloat( String.format(Locale.getDefault(),KEY_SeuilBas,i), 0f ) );
        seuil.mG_high = Math.round( (double)sp.getFloat( String.format(Locale.getDefault(),KEY_SeuilHaut,i), 0f ) );
        switch ( i ) {
            case 0: case 5: case 10: case 15:
                seuil.level = LEVEL_t.LEVEL_1; break;
            case 1: case 6: case 11: case 16:
                seuil.level = LEVEL_t.LEVEL_2; break;
            case 2: case 7: case 12: case 17:
                seuil.level = LEVEL_t.LEVEL_3; break;
            case 3: case 8: case 13: case 18:
                seuil.level = LEVEL_t.LEVEL_4; break;
            case 4: case 9: case 14: case 19:
                seuil.level = LEVEL_t.LEVEL_5; break;
            default:
                seuil.level = LEVEL_t.LEVEL_UNKNOW; break;
        }
        switch ( i ) {
            case 0: case 1: case 2: case 3: case 4:
                seuil.type = FORCE_t.ACCELERATION; break;
            case 5: case 6: case 7: case 8: case 9:
                seuil.type = FORCE_t.BRAKING; break;
            case 10: case 11: case 12: case 13: case 14:
                seuil.type = FORCE_t.TURN_RIGHT; break;
            case 15: case 16: case 17: case 18: case 19:
                seuil.type = FORCE_t.TURN_LEFT; break;
            default:
                seuil.type = FORCE_t.UNKNOW; break;
        }
        return seuil;
    }

    public static void setLatLong(Context ctx, int epc, boolean enable ){
        SharedPreferences sp = ctx.getSharedPreferences(
                String.format(Locale.getDefault(),KEY_EPC,epc), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean( KEY_LatLong, enable );
        editor.apply();
    }

    public static boolean getLatLong(Context ctx, int epc ){
        SharedPreferences sp = ctx.getSharedPreferences(
                String.format(Locale.getDefault(),KEY_EPC,epc), Context.MODE_PRIVATE);
        return sp.getBoolean( KEY_LatLong, false );
    }
}
