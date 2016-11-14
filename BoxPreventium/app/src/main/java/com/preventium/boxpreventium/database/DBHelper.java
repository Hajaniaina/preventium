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

    public static final String DATABASE_NAME = "GPSData.db";

    public static final String TABLE_ECA = "eca";
    public static final String COLUMN_ECA_ID = "id";
    public static final String COLUMN_ECA_PARCOUR_ID = "parcour_id";
    public static final String COLUMN_ECA_FILE_ID = "file_id";
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

    public static final String TABLE_DRIVER = "eca_driver";
    public static final String COLUMN_DRIVER_TIME = "time";
    public static final String COLUMN_DRIVER_ID = "id";
    public static final String COLUMN_DRIVER_PARCOUR_ID = "parcour_id";

    private static final String TAG = "DBHelper";
    private Context ctx = null;

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
    }

    // ECA

    public long countNbEvent( long parcour_id, int alertID ){
        long ret = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT DISTINCT COUNT(" + COLUMN_ECA_ID + ")" +
                " FROM " + TABLE_ECA +
                " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id +
                " AND " + COLUMN_ECA_ALERTID + " = " + alertID +
                " ;", null );
        if( cursor != null && cursor.moveToFirst() ) {
            ret = cursor.getLong(0);
            cursor.close();
        }
        db.close();
        return ret;
    }

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

    // DRIVER
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

    public long get_driver_id(long parcour_id, long time){
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

//    public long getLastECATime(){
//        long ret = -1;
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor =  db.rawQuery( "SELECT DISTINCT "
//                + COLUMN_ECA_TIME + " from "
//                + TABLE_ECA + " ORDER BY "
//                + COLUMN_ECA_TIME + " LIMIT 1 ;", null );
//        if( cursor != null && cursor.moveToFirst() ) {
//            ret = cursor.getLong(0);
//        }
//        return ret;
//    }

    public List<Long> get_parcours_id(){
        List<Long> ret = new ArrayList<Long>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT DISTINCT " + COLUMN_ECA_PARCOUR_ID + " from " + TABLE_ECA + " ;", null );
        if( cursor != null && cursor.moveToFirst() ) {
            while ( !cursor.isAfterLast() ){
                ret.add( cursor.getLong(0) );
                cursor.moveToNext();
            }
        }
        return ret;
    }


//    public int numberOfRows(){
//        SQLiteDatabase db = this.getReadableDatabase();
//        return (int) queryNumEntries(db, TABLE_ECA);
//    }


//    public double distanceTraveled(){
//        double ret = 0.0;
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor =  db.rawQuery( "SELECT SUM(" + COLUMN_ECA_DISTANCE + ") from " + TABLE_ECA + ";", null );
//        if( cursor != null && cursor.moveToFirst() ) ret = cursor.getDouble(0);
//        return ret;
//    }
//
//    public ArrayList<ECALine> alertList(){
//        ArrayList<ECALine> ret = new ArrayList<ECALine>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor =  db.rawQuery( "SELECT * from " + TABLE_ECA + ";", null );
//        if( cursor != null && cursor.moveToFirst() ) {
//            ECALine line = null;
//            while ( !cursor.isAfterLast() ){
//                line = new ECALine();
//
//                line.id = cursor.getInt( cursor.getColumnIndex(COLUMN_ECA_ID) );
//                line.alertID = cursor.getInt( cursor.getColumnIndex(COLUMN_ECA_ALERTID) );
//                line.padding = cursor.getInt( cursor.getColumnIndex(COLUMN_ECA_PADDIND) );
//                line.long_pos_orientation = cursor.getInt( cursor.getColumnIndex(COLUMN_ECA_LONG_POS_ORIENTATION) );
//                line.lat_pos_orientation = cursor.getInt( cursor.getColumnIndex(COLUMN_ECA_LAT_POS_ORIENTATION) );
//                line.distance = cursor.getFloat( cursor.getColumnIndex(COLUMN_ECA_DISTANCE) );
//
//                line.location = new Location("");
//                line.location.setTime( cursor.getLong( cursor.getColumnIndex(COLUMN_ECA_TIME) ) );
//                line.location.setLongitude( cursor.getFloat( cursor.getColumnIndex(COLUMN_ECA_LONG_POS) ) );
//                line.location.setLatitude( cursor.getFloat( cursor.getColumnIndex(COLUMN_ECA_LAT_POS) ) );
//                line.location.setSpeed( cursor.getFloat( cursor.getColumnIndex(COLUMN_ECA_SPEED) ) );
//
//                Log.d("ECA", line.toString() );
//                ret.add( line );
//
//                cursor.moveToNext();
//            }
//        }
//        return ret;
//    }
//
//    public void generate_eca_file_1(){
//
//        Log.d(TAG, "generate_eca_file()");
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor =  db.rawQuery( "SELECT * from " + TABLE_ECA + ";", null );
//        if( cursor != null && cursor.moveToFirst() ) {
//            long parcour_id = 0;
//            ECALine line = null;
//            while ( !cursor.isAfterLast() ) {
//                // READ CURRENT LINE
//                parcour_id = cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_PARCOUR_ID) );
//                line = new ECALine();
//                line.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_ID));
//                line.alertID = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_ALERTID));
//                line.padding = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_PADDIND));
//                line.long_pos_orientation = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_LONG_POS_ORIENTATION));
//                line.lat_pos_orientation = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_LAT_POS_ORIENTATION));
//                line.distance = cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_DISTANCE));
//                line.location = new Location("");
//                line.location.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_TIME)));
//                line.location.setLongitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_LONG_POS)));
//                line.location.setLatitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_LAT_POS)));
//                line.location.setSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_SPEED)));
//
//                File folder = new File(ctx.getFilesDir() + "ECA", "");
//                // Create folder if not exist
//                if (!folder.exists())
//                    if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
//                if( folder.exists() ) {
//
//                    int cnt = 0;
//                    boolean ready = false;
//                    String filename = "";
//                    String desFileName = "";
//                    File file = null;
//                    while( !ready ) {
//                        filename = String.format(Locale.getDefault(),"%s_%s_%04d.ECA",
//                                ComonUtils.getIMEInumber(ctx), parcour_id, cnt );
//                        desFileName = String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), filename);
//                        file = new File(desFileName);
//                        if (!file.exists()) {
//                            ready = true;
//                            try {
//                                if (file.createNewFile())
//                                    Log.d(TAG, "FILE CREATE:" + desFileName);
//                                else
//                                    Log.w(TAG, "FILE NOT CREATE:" + desFileName);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        } else {
//                            double bytes = file.length();
//                            double kilobytes = (bytes / 1024);
//                            double megabytes = (kilobytes / 1024);
//                            //double gigabytes = (megabytes / 1024);
//                            //double terabytes = (gigabytes / 1024);
//                            //double petabytes = (terabytes / 1024);
//                            //double exabytes = (petabytes / 1024);
//                            //double zettabytes = (exabytes / 1024);
//                            //double yottabytes = (zettabytes / 1024);
//                            if (megabytes < 0.5) {
//                                ready = true;
//                            } else {
//                                cnt++;
//                            }
//                        }
//                    }
//
//                    if( file.exists() ) {
//                        try {
//                            PrintWriter printWriter = null;
//                            printWriter = new PrintWriter(new FileOutputStream(file.getAbsolutePath(), true));
//                            printWriter.write(System.getProperty("line.separator"));
//                            printWriter.write(Arrays.toString(line.toData()));
//                            printWriter.close();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//
//
//
//                }
//                //Log.d(TAG, line.toString());
//                cursor.moveToNext();
//            }
//        }
//    }
//
//    public boolean generate_eca_file_if_needed(){
//        Log.d(TAG, "generate_eca_file()");
//
//        boolean ret = false;
//
//        File folder = new File(ctx.getFilesDir() + "ECA", "");
//        // Create folder if not exist
//        if (!folder.exists())
//            if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
//        if( folder.exists() ) {
//
//            SQLiteDatabase db = this.getReadableDatabase();
//            Cursor cursor = db.rawQuery("SELECT * from " + TABLE_ECA + " where " + COLUMN_ECA_FILE_ID + " = -1;", null);
//            while (!cursor.isAfterLast()) {
//                long parcour_id = 0;
//                ECALine line = null;
//                while (!cursor.isAfterLast()) {
//                    // READ CURRENT LINE
//                    parcour_id = cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_PARCOUR_ID));
//                    line = new ECALine();
//                    line.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_ID));
//                    line.alertID = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_ALERTID));
//                    line.padding = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_PADDIND));
//                    line.long_pos_orientation = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_LONG_POS_ORIENTATION));
//                    line.lat_pos_orientation = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_LAT_POS_ORIENTATION));
//                    line.distance = cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_DISTANCE));
//                    line.location = new Location("");
//                    line.location.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_TIME)));
//                    line.location.setLongitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_LONG_POS)));
//                    line.location.setLatitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_LAT_POS)));
//                    line.location.setSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_SPEED)));
//                    Log.d("AAA", "Parcour + " + parcour_id + " -> " + line.toString());
//                }
//
//            }
//        }
//        return ret;
//
//
//
//
//
//
////
////        SQLiteDatabase db = this.getReadableDatabase();
////        Cursor cursor =  db.rawQuery( "SELECT * from " + TABLE_ECA + ";", null );
////        if( cursor != null && cursor.moveToFirst() ) {
////            long parcour_id = 0;
////            ECALine line = null;
////            while ( !cursor.isAfterLast() ) {
////                // READ CURRENT LINE
////                parcour_id = cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_PARCOUR) );
////                line = new ECALine();
////                line.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_ID));
////                line.alertID = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_ALERTID));
////                line.padding = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_PADDIND));
////                line.long_pos_orientation = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_LONG_POS_ORIENTATION));
////                line.lat_pos_orientation = cursor.getInt(cursor.getColumnIndex(COLUMN_ECA_LAT_POS_ORIENTATION));
////                line.distance = cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_DISTANCE));
////                line.location = new Location("");
////                line.location.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_TIME)));
////                line.location.setLongitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_LONG_POS)));
////                line.location.setLatitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_LAT_POS)));
////                line.location.setSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_ECA_SPEED)));
////
////                File folder = new File(ctx.getFilesDir() + "ECA", "");
////                // Create folder if not exist
////                if (!folder.exists())
////                    if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
////                if( folder.exists() ) {
////
////                    int cnt = 0;
////                    boolean ready = false;
////                    String filename = "";
////                    String desFileName = "";
////                    File file = null;
////                    while( !ready ) {
////                        filename = String.format(Locale.getDefault(),"%s_%s_%04d.ECA",
////                                ComonUtils.getIMEInumber(ctx), parcour_id, cnt );
////                        desFileName = String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), filename);
////                        file = new File(desFileName);
////                        if (!file.exists()) {
////                            ready = true;
////                            try {
////                                if (file.createNewFile())
////                                    Log.d(TAG, "FILE CREATE:" + desFileName);
////                                else
////                                    Log.w(TAG, "FILE NOT CREATE:" + desFileName);
////                            } catch (IOException e) {
////                                e.printStackTrace();
////                            }
////                        } else {
////                            double bytes = file.length();
////                            double kilobytes = (bytes / 1024);
////                            double megabytes = (kilobytes / 1024);
////                            //double gigabytes = (megabytes / 1024);
////                            //double terabytes = (gigabytes / 1024);
////                            //double petabytes = (terabytes / 1024);
////                            //double exabytes = (petabytes / 1024);
////                            //double zettabytes = (exabytes / 1024);
////                            //double yottabytes = (zettabytes / 1024);
////                            if (megabytes < 0.5) {
////                                ready = true;
////                            } else {
////                                cnt++;
////                            }
////                        }
////                    }
////
////                    if( file.exists() ) {
////                        try {
////                            PrintWriter printWriter = null;
////                            printWriter = new PrintWriter(new FileOutputStream(file.getAbsolutePath(), true));
////                            printWriter.write(System.getProperty("line.separator"));
////                            printWriter.write(Arrays.toString(line.toData()));
////                            printWriter.close();
////                        } catch (FileNotFoundException e) {
////                            e.printStackTrace();
////                        }
////                    }
////
////
////
////
////                }
////                //Log.d(TAG, line.toString());
////                cursor.moveToNext();
////            }
////        }
//
//    }
//
//    public void get_eca_files_list(){
//        File folder = new File( String.format(Locale.getDefault(), "%s", ctx.getFilesDir()) );
//        File[] listOfFiles = folder.listFiles();
//        for (int i = 0; i < listOfFiles.length; i++) {
//            if (listOfFiles[i].isFile()) {
//                Log.d(TAG, "File " + listOfFiles[i].getName());
//            } else if (listOfFiles[i].isDirectory()) {
//                Log.d(TAG, "Directory " + listOfFiles[i].getName());
//            }
//        }
//    }
//
//    public void create_eca_files(){
//
//    }

    private final static String KEY_LAST = "LastSend";
    private final static String KEY_LAST_eca_parcour = "last_eca_parcour";
    private final static String KEY_LAST_eca_counter = "last_eca_counter";
    private final static String KEY_LAST_eca_time = "last_eca_time";
    public static long get_eca_parcour_send(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
        return sp.getLong( KEY_LAST_eca_parcour, -1 );
    }
    public static int get_eca_counter(Context ctx, long parcour){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
        if( parcour != get_eca_parcour_send(ctx) ) return -1;
        return sp.getInt( KEY_LAST_eca_counter, -1 );
    }
    public static long get_last_eca_time_send(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
        return sp.getLong( KEY_LAST_eca_time, -1 );
    }
    public static void set_eca_parcour_send(Context ctx, long parcours){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong( KEY_LAST_eca_parcour, parcours );
        editor.apply();
    }
    public static void set_eca_counter(Context ctx, int counter){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt( KEY_LAST_eca_counter, counter );
        editor.apply();
    }
    public static void set_last_eca_time_send(Context ctx, long time){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong( KEY_LAST_eca_time, time );
        editor.apply();
    }
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

                                output.write( line.toData() );
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
