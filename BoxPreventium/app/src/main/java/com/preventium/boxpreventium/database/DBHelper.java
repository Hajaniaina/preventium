package com.preventium.boxpreventium.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.preventium.boxpreventium.server.ECA.ECALine;
import com.preventium.boxpreventium.utils.BytesUtils;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.database.DatabaseUtils.queryNumEntries;

/**
 * Created by Franck on 28/09/2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "GPSData.db";

    public static final String TABLE_ECA = "eca";
    public static final String COLUMN_ECA_ID = "id";
    public static final String COLUMN_ECA_PARCOUR = "parcour";
    public static final String COLUMN_ECA_SENDING = "sending";
    public static final String COLUMN_ECA_TIME = "time";
    public static final String COLUMN_ECA_ALERTID = "alertid";
    public static final String COLUMN_ECA_PADDIND = "padding";
    public static final String COLUMN_ECA_LONG_POS = "long_pos";
    public static final String COLUMN_ECA_LAT_POS = "lat_pos";
    public static final String COLUMN_ECA_LONG_POS_ORIENTATION = "long_pos_orientation";
    public static final String COLUMN_ECA_LAT_POS_ORIENTATION = "lat_pos_orientation";
    public static final String COLUMN_ECA_SPEED = "speed";
    public static final String COLUMN_ECA_DISTANCE = "distance";

    public static final String TABLE_CEP = "cep";
    public static final String COLUMN_CEP_ID = "id";
    public static final String COLUMN_CEP_TIME = "time";
    public static final String COLUMN_CEP_MAC = "mac";
    public static final String COLUMN_CEP_LONG_POS = "long_pos";
    public static final String COLUMN_CEP_LAT_POS = "lat_pos";
    public static final String COLUMN_CEP_STATUS = "status";


    public DBHelper(Context context) { super(context, DATABASE_NAME, null, 3); }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String TABLE_CREATE =
                "CREATE TABLE " + TABLE_ECA + " (" +
                        COLUMN_ECA_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_ECA_PARCOUR + " INTEGER, " +
                        COLUMN_ECA_SENDING + " INTEGER, " +
                        COLUMN_ECA_TIME + " INTEGER, " +
                        COLUMN_ECA_ALERTID + " INTEGER, " +
                        COLUMN_ECA_PADDIND + " INTEGER, " +
                        COLUMN_ECA_LONG_POS + " FLOAT, " +
                        COLUMN_ECA_LAT_POS + " FLOAT, " +
                        COLUMN_ECA_LONG_POS_ORIENTATION + " INTEGER, " +
                        COLUMN_ECA_LAT_POS_ORIENTATION + " INTEGER, " +
                        COLUMN_ECA_SPEED + " FLOAT, " +
                        COLUMN_ECA_DISTANCE + " FLOAT);" ;
        sqLiteDatabase.execSQL(TABLE_CREATE);

        TABLE_CREATE ="CREATE TABLE " + TABLE_CEP + " (" +
                COLUMN_CEP_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_CEP_TIME + " INTEGER, " +
                COLUMN_CEP_MAC + " TEXT, " +
                COLUMN_CEP_LONG_POS + " FLOAT, " +
                COLUMN_CEP_LAT_POS + " FLOAT, " +
                COLUMN_CEP_STATUS + " INTEGER);";
        sqLiteDatabase.execSQL(TABLE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_ECA + ";";
        sqLiteDatabase.execSQL(TABLE_DROP);
        TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_CEP + ";";
        sqLiteDatabase.execSQL(TABLE_DROP);
        onCreate(sqLiteDatabase);
    }

    public void clearAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ECA,null,null);
        db.delete(TABLE_CEP,null,null);
    }

    // ECA

    public List<Long> get_parcours_id(){
        List<Long> ret = new ArrayList<Long>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT DISTINCT " + COLUMN_ECA_PARCOUR + " from " + TABLE_ECA + "where " + COLUMN_ECA_SENDING + " = 0;", null );
        if( cursor != null && cursor.moveToFirst() ) {
            while ( !cursor.isAfterLast() ){
                ret.add( cursor.getLong(0) );
                cursor.moveToNext();
            }
        }

        return ret;
    }

    public boolean addECA( long parcour_id, ECALine line ){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ECA_TIME, line.location.getTime() );
        contentValues.put(COLUMN_ECA_PARCOUR, parcour_id );
        contentValues.put(COLUMN_ECA_SENDING, 0);
        contentValues.put(COLUMN_ECA_ALERTID, line.alertID );
        contentValues.put(COLUMN_ECA_PADDIND, line.padding );
        contentValues.put(COLUMN_ECA_LONG_POS, line.location.getLongitude() );
        contentValues.put(COLUMN_ECA_LAT_POS, line.location.getLatitude() );
        contentValues.put(COLUMN_ECA_LONG_POS_ORIENTATION, line.long_pos_orientation );
        contentValues.put(COLUMN_ECA_LAT_POS_ORIENTATION, line.lat_pos_orientation );
        contentValues.put(COLUMN_ECA_SPEED, line.location.getSpeed() );
        contentValues.put(COLUMN_ECA_DISTANCE, line.distance );
        db.insert(TABLE_ECA, null, contentValues);
        return true;
    }

//    public int numberOfRows(){
//        SQLiteDatabase db = this.getReadableDatabase();
//        return (int) queryNumEntries(db, TABLE_ECA);
//    }

    public double distanceTraveled(){
        double ret = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT SUM(" + COLUMN_ECA_DISTANCE + ") from " + TABLE_ECA + ";", null );
        if( cursor != null && cursor.moveToFirst() ) ret = cursor.getDouble(0);
        return ret;
    }

    public ArrayList<ECALine> alertList(){
        ArrayList<ECALine> ret = new ArrayList<ECALine>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT * from " + TABLE_ECA + ";", null );
        if( cursor != null && cursor.moveToFirst() ) {
            ECALine line = null;
            while ( !cursor.isAfterLast() ){
                line = new ECALine();

                line.id = cursor.getInt( cursor.getColumnIndex(COLUMN_ECA_ID) );
                line.alertID = cursor.getInt( cursor.getColumnIndex(COLUMN_ECA_ALERTID) );
                line.padding = cursor.getInt( cursor.getColumnIndex(COLUMN_ECA_PADDIND) );
                line.long_pos_orientation = cursor.getInt( cursor.getColumnIndex(COLUMN_ECA_LONG_POS_ORIENTATION) );
                line.lat_pos_orientation = cursor.getInt( cursor.getColumnIndex(COLUMN_ECA_LAT_POS_ORIENTATION) );
                line.distance = cursor.getFloat( cursor.getColumnIndex(COLUMN_ECA_DISTANCE) );

                line.location = new Location("");
                line.location.setTime( cursor.getLong( cursor.getColumnIndex(COLUMN_ECA_TIME) ) );
                line.location.setLongitude( cursor.getFloat( cursor.getColumnIndex(COLUMN_ECA_LONG_POS) ) );
                line.location.setLatitude( cursor.getFloat( cursor.getColumnIndex(COLUMN_ECA_LAT_POS) ) );
                line.location.setSpeed( cursor.getFloat( cursor.getColumnIndex(COLUMN_ECA_SPEED) ) );

                Log.d("ECA", line.toString() );
                ret.add( line );

                cursor.moveToNext();
            }
        }
        return ret;
    }

    // BOX CONNECTION/DISCONNECTION

    public boolean addCEP(@NonNull Location location, @NonNull String device_mac, boolean connected ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CEP_TIME, location.getTime() );
        contentValues.put(COLUMN_CEP_MAC, device_mac );
        contentValues.put(COLUMN_CEP_LONG_POS, location.getLongitude() );
        contentValues.put(COLUMN_CEP_LAT_POS, location.getLatitude() );
        contentValues.put(COLUMN_CEP_STATUS, (connected ? 1 : 0) );
        db.insert(TABLE_CEP, null, contentValues);
        return true;
    }

    public byte[] boxEventsData(){

        byte[] ret = new byte[0];

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT * from " + TABLE_CEP + ";", null );
        if( cursor != null && cursor.moveToFirst() ) {
            byte[] line;
            byte[] b;
            String[] macAddressParts;
            int i;

            int id;
            long time;
            float long_pos;
            float lat_pos;
            String mac;
            int status;
            while( !cursor.isAfterLast() ){

                id = cursor.getInt( cursor.getColumnIndex(COLUMN_CEP_ID) );
                time = cursor.getLong( cursor.getColumnIndex(COLUMN_CEP_TIME) );
                long_pos = cursor.getFloat( cursor.getColumnIndex(COLUMN_CEP_LONG_POS) );
                lat_pos = cursor.getFloat( cursor.getColumnIndex(COLUMN_CEP_LAT_POS) );
                mac = cursor.getString( cursor.getColumnIndex(COLUMN_CEP_MAC) );
                status = cursor.getInt( cursor.getColumnIndex(COLUMN_CEP_STATUS) );

                Log.d("CEP", String.format(Locale.getDefault(),
                        "id: %s; time: %s; long_pos: %s; lat_pos: %s; mac: %s; status: %s",
                        id, time, long_pos, lat_pos, mac, status ) );
                i = 0;
                line = new byte[21];
                line[i] = (byte)id;

                Calendar aGMTCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                aGMTCalendar.setTimeInMillis( time );
//                Log.d("AA","Time = " + time );
//                Log.d("AA","DAY = " + aGMTCalendar.get(Calendar.DAY_OF_MONTH) );
//                Log.d("AA","MONTH = " + aGMTCalendar.get(Calendar.MONTH) );
//                Log.d("AA","YEAR = " + aGMTCalendar.get(Calendar.YEAR) );
//                Log.d("AA","HOURS = " + aGMTCalendar.get(Calendar.HOUR) );
//                Log.d("AA","MINUTES = " + aGMTCalendar.get(Calendar.MINUTE) );
//                Log.d("AA","SECONDS = " + aGMTCalendar.get(Calendar.SECOND) );
                line[i++] = (byte)aGMTCalendar.get(Calendar.DAY_OF_MONTH);
                line[i++] = (byte)(aGMTCalendar.get(Calendar.MONTH)+1);
                line[i++] = (byte)(aGMTCalendar.get(Calendar.YEAR)&0xFF);
                line[i++] = (byte)aGMTCalendar.get(Calendar.HOUR);
                line[i++] = (byte)aGMTCalendar.get(Calendar.MINUTE);
                line[i++] = (byte)aGMTCalendar.get(Calendar.SECOND);
                b = ByteBuffer.allocate(4).putFloat(long_pos).array();
                line[i++] = b[0];
                line[i++] = b[1];
                line[i++] = b[2];
                line[i++] = b[3];
                b = ByteBuffer.allocate(4).putFloat(lat_pos).array();
                line[i++] = b[0];
                line[i++] = b[1];
                line[i++] = b[2];
                line[i++] = b[3];
                macAddressParts = mac.split(":");
                b = new byte[6];
                for(int m=0; m<6; m++){
                    Integer hex = Integer.parseInt(macAddressParts[m], 16);
                    b[m] = hex.byteValue();
                }
                line[i++] = b[0];
                line[i++] = b[1];
                line[i++] = b[2];
                line[i++] = b[3];
                line[i++] = b[4];
                line[i++] = b[5];
                line[i] = (byte)status;

                ret = BytesUtils.concatenateByteArrays(ret,line);

                cursor.moveToNext();
            }
        }
        return  ret;
    }

}
