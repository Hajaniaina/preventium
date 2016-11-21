package com.preventium.boxpreventium.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.mikephil.charting.utils.FileUtils;
import com.github.mikephil.charting.utils.Utils;
import com.preventium.boxpreventium.server.CFG.DataCFG;
import com.preventium.boxpreventium.server.ECA.ECALine;
import com.preventium.boxpreventium.utils.BytesUtils;
import com.preventium.boxpreventium.utils.ComonUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final String TAG = "DBHelper";
    private Context ctx = null;

    /// ============================================================================================
    /// DATABASE
    /// ============================================================================================
    private static final String DATABASE_NAME = "GPSData.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String TABLE_CREATE =
                "CREATE TABLE " + TABLE_ECA + " (" +
                COLUMN_ECA_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_ECA_PARCOUR_ID + " INTEGER, " +
                COLUMN_ECA_FILE_ID + " INTEGER, " +
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

        TABLE_CREATE =
                "CREATE TABLE " + TABLE_CEP + " (" +
                        COLUMN_CEP_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_CEP_TIME + " INTEGER, " +
                        COLUMN_CEP_MAC + " TEXT, " +
                        COLUMN_CEP_LONG_POS + " FLOAT, " +
                        COLUMN_CEP_LAT_POS + " FLOAT, " +
                        COLUMN_CEP_STATUS + " INTEGER);";
        sqLiteDatabase.execSQL(TABLE_CREATE);

        TABLE_CREATE =
                "CREATE TABLE " + TABLE_DRIVER + " (" +
                        COLUMN_DRIVER_TIME+ " INTEGER PRIMARY KEY, " +
                        COLUMN_DRIVER_ID + " INTEGER, " +
                        COLUMN_DRIVER_PARCOUR_ID + " INTEGER );";
        sqLiteDatabase.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_ECA + ";";
        sqLiteDatabase.execSQL(TABLE_DROP);
        TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_CEP + ";";
        sqLiteDatabase.execSQL(TABLE_DROP);
        TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_DRIVER + ";";
        sqLiteDatabase.execSQL(TABLE_DROP);
        onCreate(sqLiteDatabase);
    }

    public void clear(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ECA,null,null);
        db.delete(TABLE_CEP,null,null);
        db.delete(TABLE_DRIVER,null,null);
        db.close();
    }

    public void clear_obselete_data(){
        long end = startOfDays(System.currentTimeMillis());
        long begin = end - (5 * 24 * 3600 * 1000);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ECA,COLUMN_ECA_TIME + " < " + begin,null);
        db.delete(TABLE_CEP,COLUMN_CEP_TIME + " < " + begin,null);
        db.delete(TABLE_DRIVER,COLUMN_DRIVER_TIME + " < " + begin,null);
        db.close();
    }

    private long startOfDays(long timestamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis( timestamp );
        cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        cal.set(Calendar.MINUTE, 0); // set minutes to zero
        cal.set(Calendar.SECOND, 0); //set seconds to zero
        return cal.getTimeInMillis();
    }

    /// ============================================================================================
    /// ECA
    /// ============================================================================================

    /// Table and columns name
    private static final String TABLE_ECA = "eca";
    private static final String COLUMN_ECA_ID = "id";
    private static final String COLUMN_ECA_PARCOUR_ID = "parcour_id";
    private static final String COLUMN_ECA_FILE_ID = "file_id";
    private static final String COLUMN_ECA_TIME = "time";
    private static final String COLUMN_ECA_ALERTID = "alertid";
    private static final String COLUMN_ECA_PADDIND = "padding";
    private static final String COLUMN_ECA_LONG_POS = "long_pos";
    private static final String COLUMN_ECA_LAT_POS = "lat_pos";
    private static final String COLUMN_ECA_LONG_POS_ORIENTATION = "long_pos_orientation";
    private static final String COLUMN_ECA_LAT_POS_ORIENTATION = "lat_pos_orientation";
    private static final String COLUMN_ECA_SPEED = "speed";
    private static final String COLUMN_ECA_DISTANCE = "distance";

    /// Get number of event by parcours, or all parcours (parcours_id = -1), by events type,
    /// between timespamp
    public int countNbEvent( int alertID, long parcour_id, long begin, long end  ){
        int ret = 0;
        String request =
                "SELECT DISTINCT COUNT(" + COLUMN_ECA_ID + ") " +
                "FROM " + TABLE_ECA + " WHERE " +
                COLUMN_ECA_ALERTID + " = " + alertID + " AND ";
        if( parcour_id > 0 ) request +=
                COLUMN_ECA_PARCOUR_ID + " = " + parcour_id + " AND ";
        request +=
                COLUMN_ECA_TIME + " BETWEEN " + begin + " AND " + end + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( request, null );
        if( cursor != null && cursor.moveToFirst() ) {
            ret = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return ret;
    }

    // Get number of event by parcours, or all parcours (parcours_id = -1), in the last X seconds
    public int countNbEvent( int alertID, long parcour_id, long secs  ){
        long begin = System.currentTimeMillis() - (secs*1000);
        long end = System.currentTimeMillis() + 10000;
        return countNbEvent(alertID, parcour_id, begin, end);
    }

    // Get number of event by parcours, or all parcours (parcours_id = -1);
    public int countNbEvent( int alertID, long parcour_id ){
        long begin = 0;
        long end = System.currentTimeMillis() + 10000;
        return countNbEvent(alertID, parcour_id, begin, end);
    }

    /// Add an ECA event for a parcours
    public boolean addECA( long parcour_id, ECALine line ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ECA_TIME, line.location.getTime());
        contentValues.put(COLUMN_ECA_PARCOUR_ID, parcour_id);
        contentValues.put(COLUMN_ECA_FILE_ID, -1);
        contentValues.put(COLUMN_ECA_ALERTID, line.alertID);
        contentValues.put(COLUMN_ECA_PADDIND, line.padding);
        contentValues.put(COLUMN_ECA_LONG_POS, line.location.getLongitude());
        contentValues.put(COLUMN_ECA_LAT_POS, line.location.getLatitude());
        contentValues.put(COLUMN_ECA_LONG_POS_ORIENTATION, line.long_pos_orientation);
        contentValues.put(COLUMN_ECA_LAT_POS_ORIENTATION, line.lat_pos_orientation);
        contentValues.put(COLUMN_ECA_SPEED, line.location.getSpeed());
        contentValues.put(COLUMN_ECA_DISTANCE, line.distance);
        db.insert(TABLE_ECA, null, contentValues);
        return true;
    }

    /// Share preferences key who indicate the last ECA line sending to the server.
    private final static String KEY_LAST = "LastSend";
    private final static String KEY_LAST_eca_parcour = "last_eca_parcour";
    private final static String KEY_LAST_eca_counter = "last_eca_counter";
    private final static String KEY_LAST_eca_time = "last_eca_time";

    /// Get the last "parcour ID" for which an ECA file was sent.
    private static long get_eca_parcour_send(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
        return sp.getLong( KEY_LAST_eca_parcour, -1 );
    }

    /// Get the last number of ECA file was sent (for a parcours),
    /// return -1 if no file has been sent
    private static int get_eca_counter(Context ctx, long parcour){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
        if( parcour != get_eca_parcour_send(ctx) ) return -1;
        return sp.getInt( KEY_LAST_eca_counter, -1 );
    }

    /// Get the last ECA timestamp who was sent.
    public static long get_last_eca_time_send(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
        return sp.getLong( KEY_LAST_eca_time, -1 );
    }

    /// Update the sharepreference values.
    public boolean update_last_send(Context ctx, long time){
        boolean ret = false;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ECA +
                " WHERE " + COLUMN_ECA_TIME + " = " + time +
                " ORDER BY " + COLUMN_ECA_ID + " LIMIT 1 ;", null);
        if( cursor.moveToFirst() ){
            SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            long parcour_id = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_PARCOUR_ID));
            int counter = get_eca_counter(ctx,parcour_id) + 1;
            editor.putLong( KEY_LAST_eca_time, time );
            editor.putLong(KEY_LAST_eca_parcour,parcour_id);
            editor.putInt(KEY_LAST_eca_counter,counter);
            editor.apply();
            ret = true;
        }
        cursor.close();
        db.close();
        return ret;
    }

    /// Create ECA file
    public long create_eca_file(Context ctx, long after_time){
        long ret_time = -1;
        File folder = new File(ctx.getFilesDir(), "ECA");
        // Create folder if not exist
        if (!folder.exists())
            if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
        if( folder.exists() ) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_ECA +
                            " WHERE " + COLUMN_ECA_TIME + " > " + after_time +
                            " ORDER BY " + COLUMN_ECA_TIME + " ;", null);
            if ( cursor.moveToFirst() ) {
                ECALine line = null;
                long parcour_id = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_PARCOUR_ID));
                long cnt = get_eca_counter(ctx,parcour_id) + 1;
                long time = cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_TIME));
                long driver_id = get_driver_id(parcour_id, time);
                String filename = String.format(Locale.getDefault(),"%s_%s_%04d.ECA",
                        ComonUtils.getIMEInumber(ctx), parcour_id, cnt );
                File file = new File(folder.getAbsolutePath(), filename );
                try {
                    if( file.createNewFile() ){
                        Log.d(TAG, "FILE CREATE:" + file.getAbsolutePath());
                        OutputStream output
                                = new BufferedOutputStream(
                                new FileOutputStream(file.getAbsolutePath()));
                        //output.write( new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00} );// Vehicule ID
                        output.write( ByteBuffer.allocate(8).putLong(driver_id).array() ); // Driver ID
                        line = new ECALine();
                        boolean all_points = DataCFG.get_SEND_ALL_GPS_POINTS(ctx);
                        while ( !cursor.isAfterLast() ) {
                            if( parcour_id
                                    == cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_PARCOUR_ID)) )
                            {
                                line.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_ID));
                                line.alertID = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_ALERTID));
                                line.padding = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_PADDIND));
                                line.long_pos_orientation = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_LONG_POS_ORIENTATION));
                                line.lat_pos_orientation = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_LAT_POS_ORIENTATION));
                                line.distance = cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_DISTANCE));
                                line.location = new Location("");
                                line.location.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_TIME)));
                                line.location.setLongitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_LONG_POS)));
                                line.location.setLatitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_LAT_POS)));
                                line.location.setSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_SPEED)));
                                ret_time = line.location.getTime();
//                                try {
//                                    PrintWriter printWriter = null;
//                                    printWriter = new PrintWriter(new FileOutputStream(file.getAbsolutePath(), true));
//                                    printWriter.write(line.toDataEncoded());
//                                    printWriter.write(System.getProperty("line.separator"));
//                                    printWriter.close();
//                                } catch (FileNotFoundException e) {
//                                    e.printStackTrace();
//                                }

                                if( all_points || line.alertID != 254 ) output.write( line.toData() );
                                cursor.moveToNext();
                            }
                            else
                            {
                                break;
                            }
                        }
                        output.close();

                    } else {
                        Log.w(TAG, "FILE NOT CREATED:" + file.getAbsolutePath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            cursor.close();
            db.close();
        }
        return ret_time;
    }

    /// Remove ECA files
    public boolean remove_eca_file(Context ctx){
        boolean ret = true;
        File folder = new File(ctx.getFilesDir(), "ECA");
        File[] listOfFiles = folder.listFiles();
        if( listOfFiles != null ) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    //System.out.println(file.getName());
                    if (!file.delete()) ret = false;
                }
            }
        }
        return ret;
    }

    /// ============================================================================================
    /// PARCOURS
    /// ============================================================================================

    public long get_last_parcours_id(){
        long ret = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery(
                "SELECT DISTINCT " + COLUMN_ECA_PARCOUR_ID +
                        " FROM " + TABLE_ECA +
                        " ORDER BY DESC " + COLUMN_ECA_PARCOUR_ID +
                        " LIMIT 1 ;", null );
        if( cursor != null && cursor.moveToFirst() ) {
            ret = cursor.getLong(0);
            cursor.close();
        }
        db.close();
        return ret;
    }

    public long get_distance(long parcour_id) {
        long ret = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery(
                "SELECT SUM( " + COLUMN_ECA_DISTANCE +
                        " ) FROM " + TABLE_ECA +
                        " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id, null );
        if( cursor != null ){
            if( cursor.moveToFirst() ) ret = cursor.getLong(0);
            cursor.close();
        }
        db.close();
        return ret;
    }

    /// Get speed max by parcours, by events type, since X seconds
    public float speed_max(long parcour_id, long secs, int... alertID){
        float ret = 0f;
        SQLiteDatabase db = this.getReadableDatabase();
        long begin = System.currentTimeMillis() - (secs*1000);
        String request =
                "SELECT MAX( " + COLUMN_ECA_SPEED + " )" +
                " FROM " + TABLE_ECA +
                " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id +
                " AND " + COLUMN_ECA_TIME + " >= " + begin ;
                if( alertID != null && alertID.length > 0 ){
                    if( alertID.length > 1 ) {
                        request += " AND " +  COLUMN_ECA_ALERTID + "IN (" + alertID[0];
                        for( int i = 1; i < alertID.length; i++ ) request += ", " + alertID[i];
                        request += " )";
                    } else {
                        request += " AND " + COLUMN_ECA_ALERTID + " = " + alertID[0];
                    }
                }
        request += " ;";

        Cursor cursor = db.rawQuery(request, null );
        if( cursor != null ){
            if( cursor.moveToFirst() ) ret = cursor.getFloat(0);
            cursor.close();
        }
        db.close();
        return ret;
    }

    /// Get speed average by parcours, by events type, since X seconds
    public float speed_avg(long parcour_id, long secs, int... alertID){
        float ret = 0f;
        SQLiteDatabase db = this.getReadableDatabase();
        long begin = System.currentTimeMillis() - (secs*1000);
        String request =
                "SELECT AVG( " + COLUMN_ECA_SPEED + " )" +
                        " FROM " + TABLE_ECA +
                        " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id +
                        " AND " + COLUMN_ECA_TIME + " >= " + begin ;
        if( alertID != null && alertID.length > 0 ){
            if( alertID.length > 1 ) {
                request += " AND " +  COLUMN_ECA_ALERTID + "IN (" + alertID[0];
                for( int i = 1; i < alertID.length; i++ ) request += ", " + alertID[i];
                request += " )";
            } else {
                request += " AND " + COLUMN_ECA_ALERTID + " = " + alertID[0];
            }
        }
        request += " ;";

        Cursor cursor = db.rawQuery(request, null );
        if( cursor != null ){
            if( cursor.moveToFirst() ) ret = cursor.getFloat(0);
            cursor.close();
        }
        db.close();
        return ret;
    }

    /// ============================================================================================
    /// DRIVER ID
    /// ============================================================================================

    /// Table and columns name
    private static final String TABLE_DRIVER = "eca_driver";
    private static final String COLUMN_DRIVER_TIME = "time";
    private static final String COLUMN_DRIVER_ID = "id";
    private static final String COLUMN_DRIVER_PARCOUR_ID = "parcour_id";

    /// Add an event who indicate when a driver ID change for a parcours
    public boolean add_driver( long parcour_id, long driver_id){
        long time = System.currentTimeMillis();
        if( get_driver_id(parcour_id,time) == driver_id ) return true;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_DRIVER_TIME, time );
        contentValues.put(COLUMN_DRIVER_ID, driver_id );
        contentValues.put(COLUMN_DRIVER_PARCOUR_ID, parcour_id);
        long row = db.insert(TABLE_DRIVER, null, contentValues);
        db.close();
        return row >= 0;
    }

    /// Get the driver ID for a parcours and for a time
    private long get_driver_id(long parcour_id, long time){
        long ret = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =
                db.rawQuery(
                "SELECT " + COLUMN_DRIVER_ID +
                " FROM " + TABLE_DRIVER +
                " WHERE " + COLUMN_DRIVER_PARCOUR_ID + " = " + parcour_id +
                " AND " + COLUMN_DRIVER_TIME + " >= " + time +
                " ORDER BY " + COLUMN_DRIVER_TIME + " LIMIT 1 ;", null );

        if( cursor != null && cursor.moveToFirst() ) {
            ret = cursor.getLong(0);
            cursor.close();
        }
        db.close();
        return ret;
    }

    /// ============================================================================================
    /// CEP (Connections Events of Preventium's devices)
    /// ============================================================================================

    /// Table and colums name
    private static final String TABLE_CEP = "cep";
    private static final String COLUMN_CEP_ID = "id";
    private static final String COLUMN_CEP_TIME = "time";
    private static final String COLUMN_CEP_MAC = "mac";
    private static final String COLUMN_CEP_LONG_POS = "long_pos";
    private static final String COLUMN_CEP_LAT_POS = "lat_pos";
    private static final String COLUMN_CEP_STATUS = "status";

    /// Add an Preventium Box event (Connected/Disconnected)
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

    /// Create CEP file
    public void create_cep_file(long parcour_id) {
        File folder = new File(ctx.getFilesDir(), "CEP");
        // Create folder if not exist
        if (!folder.exists())
            if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
        if (folder.exists()) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * from " + TABLE_CEP + ";", null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String filename = String.format(Locale.getDefault(), "%s_%s.ECA",
                            ComonUtils.getIMEInumber(ctx), parcour_id);
                    File file = new File(folder.getAbsolutePath(), filename);
                    try {
                        if (file.createNewFile()) {
                            Log.d(TAG, "FILE CREATE:" + file.getAbsolutePath());
                            OutputStream output
                                    = new BufferedOutputStream(
                                    new FileOutputStream(file.getAbsolutePath()));
                            Calendar GMTCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                            byte[] line = new byte[21];
                            byte[] b;
                            String[] macAddressParts;
                            int i;
                            int id;
                            long time;
                            float long_pos;
                            float lat_pos;
                            String mac;
                            int status;
                            while (!cursor.isAfterLast()) {
                                id = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_ID));
                                time = cursor.getLong(cursor.getColumnIndex(COLUMN_CEP_TIME));
                                long_pos = cursor.getFloat(cursor.getColumnIndex(COLUMN_CEP_LONG_POS));
                                lat_pos = cursor.getFloat(cursor.getColumnIndex(COLUMN_CEP_LAT_POS));
                                mac = cursor.getString(cursor.getColumnIndex(COLUMN_CEP_MAC));
                                status = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_STATUS));

                                i = 0;
                                GMTCalendar.setTimeInMillis(time);
                                line[i++] = (byte) GMTCalendar.get(Calendar.DAY_OF_MONTH);
                                line[i++] = (byte) (GMTCalendar.get(Calendar.MONTH) + 1);
                                line[i++] = (byte) (GMTCalendar.get(Calendar.YEAR) & 0xFF);
                                line[i++] = (byte) GMTCalendar.get(Calendar.HOUR);
                                line[i++] = (byte) GMTCalendar.get(Calendar.MINUTE);
                                line[i++] = (byte) GMTCalendar.get(Calendar.SECOND);
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
                                for (int m = 0; m < 6; m++) {
                                    Integer hex = Integer.parseInt(macAddressParts[m], 16);
                                    b[m] = hex.byteValue();
                                }
                                line[i++] = b[0];
                                line[i++] = b[1];
                                line[i++] = b[2];
                                line[i++] = b[3];
                                line[i++] = b[4];
                                line[i++] = b[5];
                                line[i] = (byte) status;

                                output.write(line);
                            }
                            output.flush();
                            output.close();
                        } else {
                            Log.w(TAG, "FILE NOT CREATED:" + file.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    cursor.close();
                }
                db.close();
            }
        }
    }

    /// Clear all CEP records
    public void clear_cep_data() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CEP,null,null);
        db.close();
    }

//    public byte[] boxEventsData(){
//
//        byte[] ret = new byte[0];
//
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor =  db.rawQuery( "SELECT * from " + TABLE_CEP + ";", null );
//        if( cursor != null && cursor.moveToFirst() ) {
//            byte[] line;
//            byte[] b;
//            String[] macAddressParts;
//            int i;
//
//            int id;
//            long time;
//            float long_pos;
//            float lat_pos;
//            String mac;
//            int status;
//            while( !cursor.isAfterLast() ){
//
//                id = cursor.getInt( cursor.getColumnIndex(COLUMN_CEP_ID) );
//                time = cursor.getLong( cursor.getColumnIndex(COLUMN_CEP_TIME) );
//                long_pos = cursor.getFloat( cursor.getColumnIndex(COLUMN_CEP_LONG_POS) );
//                lat_pos = cursor.getFloat( cursor.getColumnIndex(COLUMN_CEP_LAT_POS) );
//                mac = cursor.getString( cursor.getColumnIndex(COLUMN_CEP_MAC) );
//                status = cursor.getInt( cursor.getColumnIndex(COLUMN_CEP_STATUS) );
//
//                Log.d("CEP", String.format(Locale.getDefault(),
//                        "id: %s; time: %s; long_pos: %s; lat_pos: %s; mac: %s; status: %s",
//                        id, time, long_pos, lat_pos, mac, status ) );
//                i = 0;
//                line = new byte[21];
//                line[i] = (byte)id;
//
//                Calendar aGMTCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//                aGMTCalendar.setTimeInMillis( time );
////                Log.d("AA","Time = " + time );
////                Log.d("AA","DAY = " + aGMTCalendar.get(Calendar.DAY_OF_MONTH) );
////                Log.d("AA","MONTH = " + aGMTCalendar.get(Calendar.MONTH) );
////                Log.d("AA","YEAR = " + aGMTCalendar.get(Calendar.YEAR) );
////                Log.d("AA","HOURS = " + aGMTCalendar.get(Calendar.HOUR) );
////                Log.d("AA","MINUTES = " + aGMTCalendar.get(Calendar.MINUTE) );
////                Log.d("AA","SECONDS = " + aGMTCalendar.get(Calendar.SECOND) );
//                line[i++] = (byte)aGMTCalendar.get(Calendar.DAY_OF_MONTH);
//                line[i++] = (byte)(aGMTCalendar.get(Calendar.MONTH)+1);
//                line[i++] = (byte)(aGMTCalendar.get(Calendar.YEAR)&0xFF);
//                line[i++] = (byte)aGMTCalendar.get(Calendar.HOUR);
//                line[i++] = (byte)aGMTCalendar.get(Calendar.MINUTE);
//                line[i++] = (byte)aGMTCalendar.get(Calendar.SECOND);
//                b = ByteBuffer.allocate(4).putFloat(long_pos).array();
//                line[i++] = b[0];
//                line[i++] = b[1];
//                line[i++] = b[2];
//                line[i++] = b[3];
//                b = ByteBuffer.allocate(4).putFloat(lat_pos).array();
//                line[i++] = b[0];
//                line[i++] = b[1];
//                line[i++] = b[2];
//                line[i++] = b[3];
//                macAddressParts = mac.split(":");
//                b = new byte[6];
//                for(int m=0; m<6; m++){
//                    Integer hex = Integer.parseInt(macAddressParts[m], 16);
//                    b[m] = hex.byteValue();
//                }
//                line[i++] = b[0];
//                line[i++] = b[1];
//                line[i++] = b[2];
//                line[i++] = b[3];
//                line[i++] = b[4];
//                line[i++] = b[5];
//                line[i] = (byte)status;
//
//                ret = BytesUtils.concatenateByteArrays(ret,line);
//
//                cursor.moveToNext();
//            }
//        }
//        return  ret;
//    }

//    public List<Long> get_parcours_id(){
//        List<Long> ret = new ArrayList<Long>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor =  db.rawQuery( "SELECT DISTINCT " + COLUMN_ECA_PARCOUR_ID + " from " + TABLE_ECA + " ;", null );
//        if( cursor != null && cursor.moveToFirst() ) {
//            while ( !cursor.isAfterLast() ){
//                ret.add( cursor.getLong(0) );
//                cursor.moveToNext();
//            }
//        }
//        return ret;
//    }


//    public static void set_eca_parcour_send(Context ctx, long parcours){
//        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putLong( KEY_LAST_eca_parcour, parcours );
//        editor.apply();
//    }
//    public static void set_eca_counter(Context ctx, int counter){
//        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putInt( KEY_LAST_eca_counter, counter );
//        editor.apply();
//    }
//    public static void set_last_eca_time_send(Context ctx, long time){
//        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putLong( KEY_LAST_eca_time, time );
//        editor.apply();
//    }


}
