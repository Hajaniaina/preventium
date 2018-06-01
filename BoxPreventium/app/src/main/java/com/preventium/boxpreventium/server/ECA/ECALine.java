package com.preventium.boxpreventium.server.ECA;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Franck on 28/09/2016.
 */

public class ECALine {

    private static final float MS_TO_KMH = 3.6f;

    public int id = -1;
    public int alertID = -1;
    public int padding = 0;
    public int long_pos_orientation = 0;
    public int lat_pos_orientation = 0;
    public Location location = null;
    public float distance = 0f;

    public static final int ID_BEGIN = 0;
    public static final int ID_PAUSE = 230;
    public static final int ID_RESUME = 231;
    public static final int ID_POSITION = 254;
    public static final int ID_END = 255;

    public ECALine(){};

    public static ECALine instance = null;
    public static ECALine instance () {
        if( instance == null ) instance = new ECALine();
        return instance;
    }

    public static ECALine newInstance(@NonNull Location location, @Nullable Location prev_location ) {
        if( instance == null ) instance = new ECALine();
        instance.alertID = ID_POSITION;
        instance.location = location;
        instance.distance = ( prev_location == null ) ? 0f : location.distanceTo( prev_location );
        return instance;
    }

    public static ECALine newInstance(int alertID, @NonNull Location location, @Nullable Location prev_location ) {
        ECALine ret = ECALine.newInstance(location, prev_location);
        ret.alertID = alertID;
        return ret;
    }

    public byte[] toData(){
        byte[] line = new byte[20];
        byte[] b;
        int i = 0;

        // _TIMESTAMP timestamp; //6 bytes unsigned char, identique au TIMESTAMP fichiers ECE
        Calendar aGMTCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        aGMTCalendar.setTimeInMillis( location.getTime() );
        line[i++] = (byte)aGMTCalendar.get(Calendar.DAY_OF_MONTH);
        line[i++] = (byte)(aGMTCalendar.get(Calendar.MONTH)+1);
        line[i++] = (byte)(aGMTCalendar.get(Calendar.YEAR)&0xFF);
        line[i++] = (byte)aGMTCalendar.get(Calendar.HOUR_OF_DAY);
        line[i++] = (byte)aGMTCalendar.get(Calendar.MINUTE);
        line[i++] = (byte)aGMTCalendar.get(Calendar.SECOND);

        // unsigned char alertID; //1 byte
        line[i++] = (byte)alertID;
        // unsigned char padding; //1 byte
        line[i++] = (byte)0x00;
        // float long_pos; //4 bytes
        float L = (float)location.getLongitude();
        b = ByteBuffer.allocate(4).putFloat(Float.isNaN(L) ? 0 : L).array();
        line[i++] = b[0];
        line[i++] = b[1];
        line[i++] = b[2];
        line[i++] = b[3];
        // float lat_pos; //4 bytes
        float l = (float)location.getLatitude();
        b = ByteBuffer.allocate(4).putFloat(Float.isNaN(l) ? 0 : l).array();
        line[i++] = b[0];
        line[i++] = b[1];
        line[i++] = b[2];
        line[i++] = b[3];
        // unsigned char long_pos_orientation; //1 byte
        line[i++] = (byte)0x00;
        // unsigned char lat_pos_orientation; //1 byte
        line[i++] = (byte)0x00;
        // speed
        int speed = (int)location.getSpeed()*(int)MS_TO_KMH;
        speed = Integer.toString(speed) != null ? speed : 0;
        line[i++] = (byte)( (speed & 0xFF00) >> 8 );
        line[i] = (byte)(speed & 0xFF);

        return line;
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
