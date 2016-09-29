package com.preventium.boxpreventium.manager;

import android.location.Location;

import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.server.EPC.ForceSeuil;

/**
 * Created by Franck on 23/09/2016.
 */

public class AlertForce {

    private Location location = null;
    private ForceSeuil seuil = null;
    private double mG = 0.0;

    public AlertForce(){}

    public static AlertForce newInsance(Location location, ForceSeuil seuil, double mG){
        AlertForce alert = new AlertForce();
        alert.setForceLocation( location );
        alert.setForceSeuil( seuil );
        alert.setForceMG( mG );
        return alert;
    }

    public void setForceLocation( Location location ){ this.location = location; }
    public void setForceSeuil( ForceSeuil seuil ){ this.seuil = seuil; }
    public void setForceMG( double mG ){ this.mG = mG; }

    public Location getForceLocation(){ return location; }
    public ForceSeuil getForceSeuil(){ return seuil; }
    public double getForceMG(){ return mG; }
    public FORCE_t getForceType(){ return ( seuil != null ) ? seuil.type : FORCE_t.UNKNOW; }
    public LEVEL_t getForceLevel(){ return ( seuil != null ) ? seuil.level : LEVEL_t.LEVEL_UNKNOW; }


}
