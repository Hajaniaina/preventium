package com.preventium.boxpreventium.server.ECA;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.preventium.boxpreventium.server.EPC.ForceSeuil;

/**
 * Created by Franck on 28/09/2016.
 */

public class ECALine {
//
//    public int id = -1;
//    public int alertID = -1;
//    public long time = 0;
//    public int padding = 0;
//    public float long_pos = 0f;
//    public float lat_pos = 0f;
//    public int long_pos_orientation = 0;
//    public int lat_pos_orientation = 0;
//    public float speed = 0f;
//    public float distance = 0f;
//
//    public ECALine(){};
//
//    public static ECALine newInstance( @NonNull Location location ) {
//        ECALine ret = new ECALine();
//        ret.alertID = 254;
//        ret.time = location.getTime();
//        ret.long_pos = (float)location.getLongitude();
//        ret.lat_pos = (float)location.getLatitude();
//        ret.speed = location.getSpeed();
//        ret.distance = 0f;
//        return  ret;
//    }
//
//    public static ECALine newInstance( int alertID, @NonNull Location location ) {
//        ECALine ret = new ECALine();
//        ret.alertID = alertID;
//        ret.time = location.getTime();
//        ret.long_pos = (float)location.getLongitude();
//        ret.lat_pos = (float)location.getLatitude();
//        ret.speed = location.getSpeed();
//        ret.distance = 0f;
//        return  ret;
//    }
//
//    @Override
//    public String toString() {
//        return String.format( "ECALine{ time: %s; alertID: %d; padding: %d; " +
//                "long_pos: %s; lat_pos: %s; long_pos_orientation: %d; lat_pos_orientation: %d; " +
//                "speed: %s; distance: %s }",
//                time, alertID, padding, long_pos, lat_pos,
//                long_pos_orientation, lat_pos_orientation, speed, distance );
//    }


    public int id = -1;
    public int alertID = -1;
    public int padding = 0;
    public int long_pos_orientation = 0;
    public int lat_pos_orientation = 0;
    public Location location = null;
    public float distance = 0f;

    public ECALine(){};

    public static ECALine newInstance(@NonNull Location location, @Nullable Location prev_location ) {
        ECALine ret = new ECALine();
        ret.alertID = 254;
        ret.location = location;
        ret.distance = ( prev_location == null ) ? 0f : location.distanceTo( prev_location );
        return ret;
    }

    public static ECALine newInstance(int alertID, @NonNull Location location, @Nullable Location prev_location ) {
        ECALine ret = ECALine.newInstance(location,prev_location);
        ret.alertID = alertID;
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if( this == obj ) return true;
        if( obj == null ) return false;
        if( getClass() != obj.getClass() ) return false;
        ECALine other = (ECALine)obj;
        return (alertID == other.alertID
                && location.distanceTo( other.location ) <= 10 );
    }

    @Override
    public String toString() {
        return String.format( "ECALine{ time: %s; alertID: %d; padding: %d; " +
                        "long_pos: %s; lat_pos: %s; long_pos_orientation: %d; lat_pos_orientation: %d; " +
                        "speed: %s; distance: %s }",
                location.getTime(), alertID, padding, location.getLongitude(), location.getLatitude(),
                long_pos_orientation, lat_pos_orientation, location.getSpeed(), distance );
    }
}
