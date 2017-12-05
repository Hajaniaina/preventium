package com.preventium.boxpreventium.server.EPC;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.utils.BytesUtils;
import com.preventium.boxpreventium.utils.ComonUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by Franck on 23/09/2016.
 */

public class ReaderEPCFile {

    private final static String TAG = "ReaderEPCFile";
    private ForceSeuil[] seuil = new ForceSeuil[20];
    private boolean lat_long = false;
    private String EPC_name1 = "";
    private String EPC_name2 = "";
    private String EPC_name3 = "";
    private String EPC_name4 = "";
    private String EPC_name5 = "";

    public ReaderEPCFile(){clear();}

    private void clear(){
        EPC_name1 = "";
        EPC_name2 = "";
        EPC_name3 = "";
        EPC_name4 = "";
        EPC_name5 = "";
    }
    public String getEPCFileName(Context ctx, int i, boolean acknowledge ) {
        if( acknowledge )return String.format(Locale.getDefault(), ComonUtils.getIMEInumber(ctx) + "_%d_ok.EPC", i);
        return String.format(Locale.getDefault(), ComonUtils.getIMEInumber(ctx) + "_%d.EPC", i);
    }

    public String getNameFileName(Context ctx) {
        //if( acknowledge )return String.format(Locale.getDefault(), ComonUtils.getIMEInumber(ctx) + "_%d_ok.EPC", i);
        return String.format(Locale.getDefault(), ComonUtils.getIMEInumber(ctx) + "_EPC.NAME");
    }

    public String getEPCFilePath(Context ctx, int i ) {
        String fileName = getEPCFileName(ctx,i,false);
        return String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), fileName);
    }

    public boolean read( Context ctx, int i ) {
        String srcFilePath = getEPCFilePath(ctx,i);
        return read( srcFilePath );
    }

    public boolean read( String filename ) {
        boolean ret = false;
        FileInputStream in = null;
        try {
            in = new FileInputStream(filename);
            byte[] data = new byte[122];
            try {
                if( in.read( data ) == 122 ){

                    int i = 0;
                    for( int s = 0; s < seuil.length; s++ ) {
                        seuil[s] = new ForceSeuil();
                        seuil[s].IDAlert = data[i++];
                        seuil[s].TPS = (short) data[i++];
                        seuil[s].mG_low = (double) ( (data[i++] & 0xFF) | ((data[i++] << 8) & 0xFF00) );
                        seuil[s].mG_high = (double) ( (data[i++] & 0xFF) | ((data[i++] << 8) & 0xFF00) );
                        switch ( s ) {
                            case 0: case 5: case 10: case 15:
                                seuil[s].level = LEVEL_t.LEVEL_1; break;
                            case 1: case 6: case 11: case 16:
                                seuil[s].level = LEVEL_t.LEVEL_2; break;
                            case 2: case 7: case 12: case 17:
                                seuil[s].level = LEVEL_t.LEVEL_3; break;
                            case 3: case 8: case 13: case 18:
                                seuil[s].level = LEVEL_t.LEVEL_4; break;
                            case 4: case 9: case 14: case 19:
                                seuil[s].level = LEVEL_t.LEVEL_5; break;
                            default:
                                seuil[s].level = LEVEL_t.LEVEL_UNKNOW; break;
                        }
                        switch ( s ) {
                            case 0: case 1: case 2: case 3: case 4:
                                seuil[s].type = FORCE_t.ACCELERATION; break;
                            case 5: case 6: case 7: case 8: case 9:
                                seuil[s].type = FORCE_t.BRAKING; break;
                            case 10: case 11: case 12: case 13: case 14:
                                seuil[s].type = FORCE_t.TURN_RIGHT; break;
                            case 15: case 16: case 17: case 18: case 19:
                                seuil[s].type = FORCE_t.TURN_LEFT; break;
                            default:
                                seuil[s].type = FORCE_t.UNKNOW; break;
                        }

                    }
                    lat_long = (data[i] != 0x00);
                    ret = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean readname( String filename ) {
        boolean ret = false;
        try {
            FileInputStream in = new FileInputStream(filename);
            int total_size = in.available();
            byte[] data = new byte[total_size];
            if( in.read( data ) == total_size ){
                String txt = BytesUtils.dataToString(data);
                String[] split = txt.split(",");
                if( split.length == 5 ) {
                    int i = 0;
                    EPC_name1 = split[i++];
                    EPC_name2 = split[i++];
                    EPC_name3 = split[i++];
                    EPC_name4 = split[i++];
                    EPC_name5 = split[i++];

                    ret = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public int[] get_all_alertID(){
        int ret[] = new int[20];
        for( int i = 0; i < 20; i++ ) ret[i] = seuil[i].IDAlert;
        return  ret;
    }
    public short get_TPS(int index) {
        return  ( index >= 0 && index < seuil.length )
                ? seuil[index].TPS : -1;
    }
    public long get_TPS_ms(int index) {
        return  ( index >= 0 && index < seuil.length )
                ? (seuil[index].TPS*1000) : -1;
    }
    public ForceSeuil getForceSeuil(int index) {
        return  ( index >= 0 && index < seuil.length )
                ? seuil[index] : null;
    }

    public ForceSeuil getForceSeuil(double XmG, double YmG) {
        return  ( ComonUtils.interval(0.0,XmG) >= ComonUtils.interval(0.0,YmG) )
                ? getForceSeuilForX( XmG ) : getForceSeuilForY( YmG );
    }

    public ForceSeuil getForceSeuilForX(double XmG) {
        ForceSeuil ret = null;
        if( XmG >= 0.0 ) {
            for( int s = 0; s < 5; s++ ) {
                if( XmG >= seuil[s].mG_low && XmG <= seuil[s].mG_high ) {
                    ret = seuil[s];
                    break;
                }
            }
        } else {
            for( int s = 5; s < 10; s++ ) {
                if( -XmG >= seuil[s].mG_low && -XmG <= seuil[s].mG_high ) {
                    ret = seuil[s];
                    break;
                }
            }
        }
        return ret;
    }

    public ForceSeuil getForceSeuilForY(double YmG) {
        ForceSeuil ret = null;
        if( YmG >= 0.0 ) {
            for( int s = 10; s < 15; s++ ) {
                if( YmG >= seuil[s].mG_low && YmG <= seuil[s].mG_high ) {
                    ret = seuil[s];
                    break;
                }
            }
        } else {
            for( int s = 15; s < 20; s++ ) {
                if( -YmG >= seuil[s].mG_low && -YmG <= seuil[s].mG_high ) {
                    ret = seuil[s];
                    break;
                }
            }
        }
        return ret;
    }

    public ForceSeuil getForceSeuilByID(short IDAlert) {
        ForceSeuil ret = null;
        for( int i = 0; i < 20; i++ ){
            if( seuil[i].IDAlert == IDAlert ) {
                ret = seuil[i];
                break;
            }
        }
        return ret;
    }

    public void print(){
        for (ForceSeuil aSeuil : seuil) Log.d(TAG, aSeuil.toString());
        Log.d(TAG, "lat/long enable: " + lat_long );
    }

    public int selectedEPC( Context ctx ) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String key = ctx.getResources().getString(R.string.epc_selected_key);
        int val = ctx.getResources().getInteger(R.integer.epc_selected_def_key);
        return sp.getInt(key,val);
    }
    public boolean loadFromApp( Context ctx ) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String key = ctx.getResources().getString(R.string.epc_selected_key);
        int val = ctx.getResources().getInteger(R.integer.epc_selected_def_key);
        val = sp.getInt(key,val);

        return loadFromApp(ctx,val);
    }

    public boolean loadFromApp( Context ctx, int epc ) {
        boolean ret = false;
        if( DataEPC.preferenceFileExist(ctx,epc) ){
            for( int i = 0; i < seuil.length; i++ ) {
                seuil[i] = DataEPC.getEPCLine(ctx,epc,i);
            }
            lat_long = DataEPC.getLatLong(ctx,epc);
            ret = true;
        }
        return ret;
    }

    public boolean applyToApp( Context ctx, int epc ){
        boolean ret = false;
        if( epc >= 1 && epc <= 5 ) {
            for (int i = 0; i < seuil.length; i++) {
                DataEPC.setEPCLine(ctx, epc, i, seuil[i]);
            }
            DataEPC.setLatLong(ctx, epc, lat_long);
            ret = true;
        }
        return ret;
    }

    public boolean loadNameFromApp( Context ctx) {
        clear();
        boolean ret = false;
        EPC_name1 = NameEPC.get_EPC_Name(ctx,1);
        EPC_name2 = NameEPC.get_EPC_Name(ctx,2);
        EPC_name3 = NameEPC.get_EPC_Name(ctx,3);
        EPC_name4 = NameEPC.get_EPC_Name(ctx,4);
        EPC_name5 = NameEPC.get_EPC_Name(ctx,5);
        ret = true;
        return ret;
    }


    public void applyNameToApp( Context ctx ) {
        NameEPC.set_EPC_Name(ctx, EPC_name1, 1);
        NameEPC.set_EPC_Name(ctx, EPC_name2, 2);
        NameEPC.set_EPC_Name(ctx, EPC_name3, 3);
        NameEPC.set_EPC_Name(ctx, EPC_name4, 4);
        NameEPC.set_EPC_Name(ctx, EPC_name5, 5);
    }

}
