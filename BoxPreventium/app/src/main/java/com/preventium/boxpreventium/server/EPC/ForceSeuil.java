package com.preventium.boxpreventium.server.EPC;

import android.util.Log;

import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;

import java.util.Locale;

/**
 * Created by Franck on 23/09/2016.
 */

public class ForceSeuil {

    public int index = -1;
    public short IDAlert = -1; // IDAlert.
    public short TPS = 0; // Temps de déclenchemnt en secondes.
    public double mG_low = 0.0; // Seuil bas en mG.
    public double mG_high = 0.0; // Seuil haut en mG.
    public LEVEL_t level = LEVEL_t.LEVEL_UNKNOW;
    public FORCE_t type = FORCE_t.UNKNOW;

    ForceSeuil(){};
    public ForceSeuil(int index, short IDAlert, short secs, double mG_min, double mG_max) {
        this.index = index;
        this.IDAlert = IDAlert;
        this.TPS = secs;
        this.mG_low = mG_min;
        this.mG_high = mG_max;
    }

    @Override
    public String toString() {
        String name = "";
        if( index >= 0 && index < 5 ) name = String.format(Locale.getDefault(),"+X%d",index+1);
        else if( index >= 5 && index < 10 ) name = String.format(Locale.getDefault(),"-X%d",index-4);
        else if( index >= 10 && index < 15 ) name = String.format(Locale.getDefault(),"Y%d",index-9);
        else if( index >= 15 && index < 20 ) name = String.format(Locale.getDefault(),"-Y%d",index-14);
        return String.format(Locale.getDefault(),
                "ForceSeuil %s[ IDAlert:%d, secs:%d, mG(%s;%s), type:%s, level:%s ]",
                name, IDAlert, TPS, mG_low, mG_high, type, level);
    }
}
