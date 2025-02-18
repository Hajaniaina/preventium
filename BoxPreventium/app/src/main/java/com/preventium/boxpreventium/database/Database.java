package com.preventium.boxpreventium.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.preventium.boxpreventium.gui.PreventiumApplication;
import com.preventium.boxpreventium.server.CFG.DataCFG;
import com.preventium.boxpreventium.server.ECA.ECALine;
import com.preventium.boxpreventium.utils.ColorCEP;
import com.preventium.boxpreventium.utils.ComonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_DEVICE_A;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_DEVICE_F;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_DEVICE_M;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_DEVICE_V;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_DIST_COV;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_ID;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_LAT_POS;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_LONG_POS;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_MAC;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_NB_BOX;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_NB_ECA;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_NOTE;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_PAR_DUR;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_STATUS;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_TIME;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_VITESSE_LD;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_CEP_VITESSE_VR;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_DRIVER_ID;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_DRIVER_PARCOUR_ID;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_DRIVER_TIME;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_ALERTID;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_DISTANCE;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_FILE_ID;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_ID;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_LAT_POS;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_LAT_POS_ORIENTATION;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_LONG_POS;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_LONG_POS_ORIENTATION;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_PADDIND;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_PARCOUR_ID;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_SPEED;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_ECA_TIME;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_EPC_NUM_EPC;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_EPC_PARCOUR_ID;
import static com.preventium.boxpreventium.database.DatabaseHelper.TABLE_CEP;
import static com.preventium.boxpreventium.database.DatabaseHelper.TABLE_DRIVER;
import static com.preventium.boxpreventium.database.DatabaseHelper.TABLE_ECA;
import static com.preventium.boxpreventium.database.DatabaseHelper.TABLE_EPC;

/**
 * Created by Franck on 02/03/2017.
 */

public class Database {

    private static final String TAG = "Database";
    private static float MS_TO_KMH = 3.6f;
    private Context ctx = null;

    public Database(Context context){
        ctx = context;
        DatabaseManager.initializeInstance( new DatabaseHelper(context) );
    }

    /// ============================================================================================
    /// CLEAR DATABASE
    /// ============================================================================================

    public void clear() {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(TABLE_ECA,null,null);
        db.delete(TABLE_CEP,null,null);
        db.delete(TABLE_DRIVER,null,null);
        DatabaseManager.getInstance().closeDatabase();
    }

    public void get_last_data_cep () {
        // aujourd'hui
        long end = startOfDays(System.currentTimeMillis());
        long begin = end - (5 * 24 * 3600 * 1000);
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CEP + " WHERE " + COLUMN_CEP_TIME + " < " + begin + ";", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                while(!cursor.isAfterLast()) {
                    int device_a = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_A));
                    int device_v = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_V));
                    int device_f = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_F));
                    int device_m = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_M));

                    // settings last data
                    ColorCEP.getInstance().addColors(device_a, device_v, device_f, device_m);
                }
            }
        }
    }

    public void clear_obselete_data() {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        // select before and clean
        String request = "SELECT * FROM " + TABLE_ECA ; // par latitude
        Cursor cursor =  db.rawQuery( request, null );
        Log.w(TAG, "Nombre de ECA au début " + String.valueOf(cursor.getCount()));

        long end = startOfDays(System.currentTimeMillis());
        long begin = end - (3 * 24 * 3600 * 1000);
        db.delete(TABLE_ECA,COLUMN_ECA_TIME + " < " + begin,null);
        db.delete(TABLE_CEP,COLUMN_CEP_TIME + " < " + begin,null);
        db.delete(TABLE_DRIVER,COLUMN_DRIVER_TIME + " < " + begin,null);
        DatabaseManager.getInstance().closeDatabase();
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

    /// Get number of event by parcours, or all parcours (parcours_id = -1), by events type,
    /// between timespamp
    public int countNbEvent( int alertID, long parcour_id, long begin, long end  ){
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        int ret = 0;
        String request = "SELECT DISTINCT COUNT(" + COLUMN_ECA_ID + ") " +
                        "FROM " + TABLE_ECA + " WHERE " +
                        COLUMN_ECA_ALERTID + " = " + alertID + " AND ";
        if( parcour_id > 0 )
            request += COLUMN_ECA_PARCOUR_ID + " = " + parcour_id + " AND ";

        request += COLUMN_ECA_TIME + " BETWEEN " + begin + " AND " + end + ";";

        Cursor cursor =  db.rawQuery( request, null );
        if( cursor != null && cursor.moveToFirst() ) {
            ret = cursor.getInt(0);
            cursor.close();
        }

        DatabaseManager.getInstance().closeDatabase();
        return ret;
    }

    // Get number of event by parcours, or all parcours (parcours_id = -1);
    public int countNbEvent( int alertID, long parcour_id ){
        long begin = 0;
        long end = System.currentTimeMillis() + 10000;
        return countNbEvent(alertID, parcour_id, begin, end);
    }

    /// Add an ECA event for a parcours
    public boolean addECA( long parcour_id, ECALine line ) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_ECA_PARCOUR_ID, parcour_id);
        contentValues.put(COLUMN_ECA_FILE_ID, -1);
        contentValues.put(COLUMN_ECA_ALERTID, line.alertID);
        contentValues.put(COLUMN_ECA_PADDIND, line.padding);
        if (line.location != null) {
            contentValues.put(COLUMN_ECA_TIME, line.location.getTime());
            contentValues.put(COLUMN_ECA_LONG_POS, line.location.getLongitude());
            contentValues.put(COLUMN_ECA_LAT_POS, line.location.getLatitude());
            contentValues.put(COLUMN_ECA_SPEED, line.location.getSpeed());
        }else{
            contentValues.put(COLUMN_ECA_TIME, System.currentTimeMillis());
            contentValues.put(COLUMN_ECA_LONG_POS, 0f);
            contentValues.put(COLUMN_ECA_LAT_POS, 0f);
            contentValues.put(COLUMN_ECA_SPEED, 0f);
        }
        contentValues.put(COLUMN_ECA_LONG_POS_ORIENTATION, line.long_pos_orientation);
        contentValues.put(COLUMN_ECA_LAT_POS_ORIENTATION, line.lat_pos_orientation);

        contentValues.put(COLUMN_ECA_DISTANCE, line.distance);
        db.insert(TABLE_ECA, null, contentValues);

        DatabaseManager.getInstance().closeDatabase();

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
    public static int get_eca_counter(Context ctx, long parcour) {
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, 0);
        if (parcour != get_eca_parcour_send(ctx)) {
            return -1;
        }
        return sp.getInt(KEY_LAST_eca_counter, -1);
    }

   /* private static int get_eca_counter(Context ctx, long parcour){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
        if( parcour != get_eca_parcour_send(ctx) ) return -1;
        return sp.getInt( KEY_LAST_eca_counter, -1 );
    }
*/
    /// Get the last ECA timestamp who was sent.
    public static long get_last_eca_time_send(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
        return sp.getLong( KEY_LAST_eca_time, -1 );
    }

    /// Update the sharepreference values.
    public boolean update_last_send(Context ctx, long time){
        boolean ret = false;

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ECA +
                " WHERE " + COLUMN_ECA_TIME + " = " + time +
                " ORDER BY " + COLUMN_ECA_ID + " LIMIT 1 ;", null);
        if( cursor.moveToFirst() ){
            SharedPreferences sp = ctx.getSharedPreferences(KEY_LAST, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            long parcour_id = cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_PARCOUR_ID));
            int counter = get_eca_counter(ctx,parcour_id) + 1;
            editor.putLong( KEY_LAST_eca_time, time );
            editor.putLong(KEY_LAST_eca_parcour,parcour_id);
            editor.putInt(KEY_LAST_eca_counter,counter);
            editor.apply();
            ret = true;
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
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
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_ECA +
                            " WHERE " + COLUMN_ECA_TIME + " > " + after_time +
                            " ORDER BY " + COLUMN_ECA_TIME + " ;", null);

            Log.d(TAG, "Nombre d'ECA créer en fichier est " + String.valueOf(cursor.getCount()));

            if ( cursor.moveToFirst() ) {
                ECALine line = null;
                long parcour_id = cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_PARCOUR_ID));
                long cnt = get_eca_counter(ctx,parcour_id) + 1;
                long time = cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_TIME));
                long driver_id = get_driver_id(parcour_id, time);
                char epc_num = (char) get_num_epc(parcour_id);
                String filename = String.format(Locale.getDefault(),"%s_%s_%04d.ECA",
                        ComonUtils.getIMEInumber(ctx), Long.toString(parcour_id), cnt );
                File file = new File(folder.getAbsolutePath(), filename );
                File fileTxt = new File(folder.getAbsolutePath(), filename + ".txt" );
                try {
                    boolean isECA = file.createNewFile();
                    boolean isECATxt = fileTxt.createNewFile();
                    if( isECA ){
                        Log.d(TAG, "FILE CREATE:" + file.getAbsolutePath());
                        OutputStream output
                                = new BufferedOutputStream(
                                new FileOutputStream(file.getAbsolutePath()));
                        //output.write( new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00} );// Vehicule ID
                        output.write( ByteBuffer.allocate(8).putLong(driver_id).array() ); // Driver ID
                        output.write( epc_num );
                        line = new ECALine();
                        boolean all_points = DataCFG.get_SEND_ALL_GPS_POINTS(ctx);
                        // toWrite text
                        PrintWriter writer = new PrintWriter(fileTxt.getAbsolutePath(), "UTF-8");
                        while ( !cursor.isAfterLast() ) {
                            Log.d(TAG, "IDParcourId BDD" + String.valueOf(cursor.getColumnIndex(COLUMN_ECA_PARCOUR_ID)) + " : " + String.valueOf(parcour_id));
                            if( parcour_id
                                    == cursor.getLong(cursor.getColumnIndex(COLUMN_ECA_PARCOUR_ID)) ) {
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

                                Log.d(TAG, "ID ECA " + String.valueOf(line.alertID));

                                if (all_points || line.alertID != 254) output.write(line.toData());
                                Log.d("ECA SEND", line.toString());
                                writer.println(line.toString());

                                cursor.moveToNext();
                            }
                            else
                            {
                                break;
                            }
                        }

                        writer.flush();

                        // close
                        output.close();
                        writer.close();

                    } else {
                        Log.w(TAG, "FILE NOT CREATED:" + file.getAbsolutePath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            cursor.close();
            DatabaseManager.getInstance().closeDatabase();
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
                if (file.isFile() /* && file.getName().endsWith(".ECA") */ ) {
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
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        Cursor cursor =  db.rawQuery(
                "SELECT " + COLUMN_ECA_PARCOUR_ID +
                        " FROM " + TABLE_ECA +
                        " ORDER BY " + COLUMN_ECA_PARCOUR_ID + " DESC " +
                        " LIMIT 1 ;", null );
        if( cursor != null && cursor.moveToFirst() ) {
            ret = cursor.getLong(0);
            cursor.close();
        }

        DatabaseManager.getInstance().closeDatabase();

        return ret;
    }

    public boolean parcour_is_closed(long parcour_id){
        boolean ret = true;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor =  db.rawQuery(
                "SELECT * FROM " + TABLE_ECA +
                        " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id +
                        " AND " + COLUMN_ECA_ALERTID + " = " + ECALine.ID_END +
                        " LIMIT 1 ;", null );
        if( cursor != null && cursor.moveToFirst()  ) {
            ret = true;
            cursor.close();
        } else {
            ret = false;
        }
        DatabaseManager.getInstance().closeDatabase();
        return ret;
    }

    public boolean parcour_expired(long parcour_id, long delay) {
        boolean ret = false;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor =  db.rawQuery(
                "SELECT * FROM " + TABLE_ECA +
                        " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id +
                        " ORDER BY " + COLUMN_ECA_TIME + " DESC " +
                        " LIMIT 1 ;", null );
        if( cursor != null && cursor.moveToFirst()  ) {
            long time = cursor.getLong( cursor.getColumnIndex(COLUMN_ECA_TIME) );
            if( System.currentTimeMillis() - time >= delay ) {
                ret = true;
            }
            cursor.close();
        }
        DatabaseManager.getInstance().closeDatabase();
        return ret;
    }

    public long close_last_parcour() {

        long ret = get_last_parcours_id();
        if( ret > 0 ) {
            if( !parcour_is_closed(ret) ) {

                // READ LAST LINE
                SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
                Cursor cursor =  db.rawQuery(
                        "SELECT * FROM " + TABLE_ECA +
                                " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + ret +
                                " ORDER BY " + COLUMN_ECA_TIME + " DESC " +
                                " LIMIT 1 ;", null );
                if( cursor != null && cursor.moveToFirst()  ) {
                    ECALine line = new ECALine();
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
                    cursor.close();

                    // ADD ENDING LINE
                    line.alertID = ECALine.ID_END;
                    if( !addECA( ret, line ) ) { ret = -1; }
                }
                DatabaseManager.getInstance().closeDatabase();
            }
        }
        return ret;
    }

    public long get_last_timestamp(){
        long timestamp = 0;
        long last_parcour_id = get_last_parcours_id();
        if( last_parcour_id > 0 ){
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            Cursor cursor =  db.rawQuery(
                    "SELECT " + COLUMN_ECA_TIME +
                            " FROM " + TABLE_ECA +
                            " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + last_parcour_id +
                            " ORDER BY " + COLUMN_ECA_TIME + " DESC " +
                            " LIMIT 1 ;", null );
            if( cursor != null && cursor.moveToFirst() ) {
                timestamp = cursor.getLong(0);
                cursor.close();
            }
            db.close();
            DatabaseManager.getInstance().closeDatabase();
        }
        return timestamp;
    }

    public long get_distance(long parcour_id) {
        long ret = 0;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor =  db.rawQuery(
                "SELECT SUM( " + COLUMN_ECA_DISTANCE +
                        " ) FROM " + TABLE_ECA +
                        " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id, null );
        if( cursor != null ){
            if( cursor.moveToFirst() ) ret = cursor.getLong(0);
            cursor.close();
        }
        DatabaseManager.getInstance().closeDatabase();
        return ret;
    }

    /// Get speed max by parcours, by events type, since X seconds
    public float speed_max(long parcour_id, long secs, @Nullable int... alertID){
        float ret = 0f;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        long begin = System.currentTimeMillis() - (secs*1000);
        String request =
                "SELECT MAX( " + COLUMN_ECA_SPEED + " )" +
                        " FROM " + TABLE_ECA +
                        " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id +
                        " AND " + COLUMN_ECA_TIME + " >= " + begin ;
        if( alertID != null && alertID.length > 0 ){
            if( alertID.length > 1 ) {
                request += " AND " +  COLUMN_ECA_ALERTID + " IN (" + alertID[0];
                for( int i = 1; i < alertID.length; i++ ) request += ", " + alertID[i];
                request += " )";
            } else {
                request += " AND " + COLUMN_ECA_ALERTID + " = " + alertID[0];
            }
        }
        request += " AND "  + COLUMN_ECA_SPEED  + " >= 0 AND " + COLUMN_ECA_SPEED + " <= " + 190/MS_TO_KMH;
        request += " ;";

        Cursor cursor = db.rawQuery(request, null );
        if( cursor != null ){
            if( cursor.moveToFirst() ) ret = cursor.getFloat(0);
            cursor.close();
        }
        DatabaseManager.getInstance().closeDatabase();
        return ret;
    }

    public float speed_max_test(long parcour_id, long n_last_secs,
                                long n_minimum_points, long n_max_last_secs,
                                @Nullable int... alertID){
        float ret = 0f;
        long nb = 0;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        long begin = System.currentTimeMillis() - (n_last_secs*1000);
        long begin_max = System.currentTimeMillis() - (n_max_last_secs*1000);

        // COUNT NB ROW
        String request =
                "SELECT COUNT(*)" +
                        " FROM " + TABLE_ECA +
                        " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id +
                        " AND " + COLUMN_ECA_TIME + " >= " + begin ;
        if( alertID != null && alertID.length > 0 ){
            if( alertID.length > 1 ) {
                request += " AND " +  COLUMN_ECA_ALERTID + " IN (" + alertID[0];
                for( int i = 1; i < alertID.length; i++ ) request += ", " + alertID[i];
                request += " )";
            } else {
                request += " AND " + COLUMN_ECA_ALERTID + " = " + alertID[0];
            }
        }
        request += " AND "  + COLUMN_ECA_SPEED  + " >= 0 AND " + COLUMN_ECA_SPEED + " <= " + 190/MS_TO_KMH;
        request += " ;";
        Cursor cursor = db.rawQuery(request, null );
        if( cursor != null ){
            if( cursor.moveToFirst() ) nb = cursor.getLong(0);
            cursor.close();
        }
Log.d("AAAAA","NB POINTS " + nb);
        if( nb >= 50 ) {
            // GET MAX
            request =
                    "SELECT MAX( " + COLUMN_ECA_SPEED + " )" +
                            " FROM " + TABLE_ECA +
                            " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id +
                            " AND " + COLUMN_ECA_TIME + " >= " + begin ;
            if( alertID != null && alertID.length > 0 ){
                if( alertID.length > 1 ) {
                    request += " AND " +  COLUMN_ECA_ALERTID + " IN (" + alertID[0];
                    for( int i = 1; i < alertID.length; i++ ) request += ", " + alertID[i];
                    request += " )";
                } else {
                    request += " AND " + COLUMN_ECA_ALERTID + " = " + alertID[0];
                }
            }
            request += " AND "  + COLUMN_ECA_SPEED  + " >= 0 AND " + COLUMN_ECA_SPEED + " <= " + 190/MS_TO_KMH;
            request += " ORDER BY " + COLUMN_ECA_TIME + " DESC";
            request += " LIMIT " + n_minimum_points;
            request += " ;";

            cursor = db.rawQuery(request, null );
            if( cursor != null ){
                if( cursor.moveToFirst() ) ret = cursor.getFloat(0);
                cursor.close();
            }

        } else {
            request =
                    "SELECT MAX( " + COLUMN_ECA_SPEED + " )" +
                            " FROM " + TABLE_ECA +
                            " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id +
                            " AND " + COLUMN_ECA_TIME + " >= " + begin ;
            if( alertID != null && alertID.length > 0 ){
                if( alertID.length > 1 ) {
                    request += " AND " +  COLUMN_ECA_ALERTID + " IN (" + alertID[0];
                    for( int i = 1; i < alertID.length; i++ ) request += ", " + alertID[i];
                    request += " )";
                } else {
                    request += " AND " + COLUMN_ECA_ALERTID + " = " + alertID[0];
                }
            }
            request += " AND "  + COLUMN_ECA_SPEED  + " >= 0 AND " + COLUMN_ECA_SPEED + " <= " + 190/MS_TO_KMH;
            request += " ORDER BY " + COLUMN_ECA_TIME + " DESC";
            request += " LIMIT " + n_minimum_points;
            request += " ;";
            cursor = db.rawQuery(request, null );
            if( cursor != null ){
                if( cursor.moveToFirst() ) ret = cursor.getFloat(0);
                cursor.close();
            }
        }

        DatabaseManager.getInstance().closeDatabase();
        return ret;
    }

    /// Get speed average by parcours, by events type, since X seconds
    public float speed_avg(long parcour_id, long secs, float speed_min, @Nullable int... alertID){
        float ret = 0f;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        long begin = System.currentTimeMillis() - (secs*1000);
        String request =
                "SELECT AVG( " + COLUMN_ECA_SPEED + " )" +
                        " FROM " + TABLE_ECA +
                        " WHERE " + COLUMN_ECA_PARCOUR_ID + " = " + parcour_id +
                        " AND " + COLUMN_ECA_TIME + " >= " + begin ;
        if( alertID != null && alertID.length > 0 ){
            if( alertID.length > 1 ) {
                request += " AND " +  COLUMN_ECA_ALERTID + " IN (" + alertID[0];
                for( int i = 1; i < alertID.length; i++ ) request += ", " + alertID[i];
                request += " )";
            } else {
                request += " AND " + COLUMN_ECA_ALERTID + " = " + alertID[0];
            }
        }
        request += " AND " + COLUMN_ECA_SPEED + " >= " + speed_min ;
        request += " AND " + COLUMN_ECA_SPEED + " <= " + 190/MS_TO_KMH;
        request += " ;";

        Cursor cursor = db.rawQuery(request, null );
        if( cursor != null ){
            if( cursor.moveToFirst() ) ret = cursor.getFloat(0);
            cursor.close();
        }

        DatabaseManager.getInstance().closeDatabase();
        return ret;
    }

    /// Get num EPC for a parcours
    public int get_num_epc(long parcour_id){
        int ret = 0;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor =
                db.rawQuery(
                        "SELECT " + COLUMN_EPC_NUM_EPC +
                                " FROM " + TABLE_EPC +
                                " WHERE " + COLUMN_EPC_PARCOUR_ID + " = " + parcour_id +
                                " LIMIT 1 ;", null );

        if( cursor != null && cursor.moveToFirst() ) {
            ret = cursor.getInt(0);
            cursor.close();
        }
        DatabaseManager.getInstance().closeDatabase();
        return ret;
    }

    /// Get num EPC for a parcours
    public boolean set_num_epc( long parcour_id, int num_epc ){
        if( num_epc < 1 || num_epc > 5 ) return false;
        if( get_num_epc(parcour_id) != 0 ) return true;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EPC_NUM_EPC, num_epc );
        contentValues.put(COLUMN_EPC_PARCOUR_ID, parcour_id );
        long row = db.insert(TABLE_EPC, null, contentValues);
        DatabaseManager.getInstance().closeDatabase();
        return row >= 0;
    }


    /// ============================================================================================
    /// DRIVER ID
    /// ============================================================================================

    /// Get the driver ID for a parcours and for a time
    private long get_driver_id(long parcour_id, long time){
        long ret = 0;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
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
        DatabaseManager.getInstance().closeDatabase();
        return ret;
    }

    /// Add an event who indicate when a driver ID change for a parcours
    public boolean add_driver( long parcour_id, long driver_id){
        long time = System.currentTimeMillis();
        if( get_driver_id(parcour_id,time) == driver_id ) return true;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_DRIVER_TIME, time );
        contentValues.put(COLUMN_DRIVER_ID, driver_id );
        contentValues.put(COLUMN_DRIVER_PARCOUR_ID, parcour_id);
        long row = db.insert(TABLE_DRIVER, null, contentValues);
        DatabaseManager.getInstance().closeDatabase();
        return row >= 0;
    }

    /// ============================================================================================
    /// CEP (Connections Events of Preventium's devices)
    /// ============================================================================================

    /// Add an Preventium Box event (Connected/Disconnected)


    public boolean addCEP(@Nullable Location location, @NonNull String device_mac, int note, int vitesse_ld, int vitesse_vr, long distance_covered, long parcour_duration, int nb_eca, int nb_box, int device_a,int device_v, int device_f, int device_m, boolean connected) {

        final int a = device_a, v = device_v, f = device_f, m = device_m;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues contentValues = new ContentValues();
        // Long time = location.
        if (location != null) {
            contentValues.put("time", Long.valueOf(location.getTime()));
            contentValues.put("long_pos", Double.valueOf(location.getLongitude()));
            contentValues.put("lat_pos", Double.valueOf(location.getLatitude()));
        } else {
            contentValues.put("time", Long.valueOf(System.currentTimeMillis()));
            contentValues.put("long_pos", Float.valueOf(0.0f));
            contentValues.put("lat_pos", Float.valueOf(0.0f));
        }
        contentValues.put(DatabaseHelper.COLUMN_CEP_MAC, device_mac);
        contentValues.put(COLUMN_CEP_NOTE, Integer.valueOf(note));
        contentValues.put(COLUMN_CEP_VITESSE_LD, Integer.valueOf(vitesse_ld));
        contentValues.put(COLUMN_CEP_VITESSE_VR, Integer.valueOf(vitesse_vr));
        contentValues.put(COLUMN_CEP_DIST_COV, Long.valueOf(distance_covered));
        contentValues.put(COLUMN_CEP_PAR_DUR, Long.valueOf(parcour_duration));
        contentValues.put(COLUMN_CEP_NB_ECA, Integer.valueOf(nb_eca));
        contentValues.put(COLUMN_CEP_NB_BOX, Integer.valueOf(nb_box));
        contentValues.put(COLUMN_CEP_DEVICE_A, Integer.valueOf(device_a));
        contentValues.put(COLUMN_CEP_DEVICE_V, Integer.valueOf(device_v));
        contentValues.put(COLUMN_CEP_DEVICE_F, Integer.valueOf(device_f));
        contentValues.put(COLUMN_CEP_DEVICE_M, Integer.valueOf(device_m));
        contentValues.put("status", Integer.valueOf(connected ? 1 : 0));
        db.insert(DatabaseHelper.TABLE_CEP, null, contentValues);
        DatabaseManager.getInstance().closeDatabase();
        return true;
    }

    /// Clear all CEP records
    public void clear_cep_data() {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(TABLE_CEP,null,null);
        DatabaseManager.getInstance().closeDatabase();
    }

    /// Create CEP file
    public void create_cep_file(long parcour_id) {
        String data;
        try {

            File folder = new File(ctx.getFilesDir(), "CEP");
            // Create folder if not exist
            if (!folder.exists())
                if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
            if (folder.exists()) {
                SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
                Cursor cursor = db.rawQuery("SELECT * from " + TABLE_CEP + ";", null);
                int a = cursor.getCount();
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        // nom du fichier
                        String filename = String.format(Locale.getDefault(), "%s_%s.CEP",
                                ComonUtils.getIMEInumber(ctx), Long.toString(parcour_id));

                        // donnée json en cep
                        JSONObject json = new JSONObject();
                        json.put("filename", filename);

                        // le fichier
                        File file = new File(folder.getAbsolutePath(), filename);
                        try {
                            if (!file.exists() && file.createNewFile()) {
                                // handler write
                                FileWriter output = new FileWriter(file.getAbsolutePath());

                                // tableau de data
                                JSONArray json_array = new JSONArray();

                                // les données
                                Calendar GMTCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                                byte[] line = new byte[73]; //61 taloha
                                byte[] b;
                                String[] macAddressParts;
                                int i;
                                int id;
                                long time;
                                float long_pos;
                                float lat_pos;
                                int note;
                                int vitesseLd;
                                int vitesseVr;
                                long distanceCovered;
                                long parcoursDuration;
                                int nbEca;
                                int nbBox;
                                String mac;
                                int device_a;
                                int device_v;
                                int device_f;
                                int device_m;
                                int status;
                                JSONObject datas;
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PreventiumApplication.getContext());
                                while (!cursor.isAfterLast()) {

                                    // tableau de donnée
                                    datas = new JSONObject();
                                    id = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_ID));

                                    /** time */
                                    time = cursor.getLong(cursor.getColumnIndex(COLUMN_CEP_TIME));
                                    Date _date = new Date(time);
                                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(_date);
                                    datas.put("date", date);
                                    /* end time */

                                    /* longitude */
                                    long_pos = cursor.getFloat(cursor.getColumnIndex(COLUMN_CEP_LONG_POS));
                                    datas.put("longitude", long_pos);
                                    /* end longitude */

                                    /* latitude */
                                    lat_pos = cursor.getFloat(cursor.getColumnIndex(COLUMN_CEP_LAT_POS));
                                    datas.put("latitude", lat_pos);
                                    /* end latitude */

                                    /* latitude */
                                    mac = cursor.getString(cursor.getColumnIndex(COLUMN_CEP_MAC));
                                    datas.put("adresse_mac", mac);
                                    /* end latitude */

                                    /* latitude */
                                    note = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_NOTE));
                                    datas.put("note", note);
                                    /* end latitude */

                                    /* vitesseLd */
                                    vitesseLd = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_VITESSE_LD));
                                    datas.put("vitesse_ld", vitesseLd);
                                    /* end vitesseLd */

                                    /* vitesseVr */
                                    vitesseVr = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_VITESSE_VR));
                                    datas.put("vitesse_vr", vitesseVr);
                                    /* end vitesseVr */

                                    /* distanceCovered */
                                    distanceCovered = cursor.getLong(cursor.getColumnIndex(COLUMN_CEP_DIST_COV));
                                    datas.put("distance", distanceCovered);
                                    /* end distanceCovered */

                                    /* distanceCovered */
                                    parcoursDuration = cursor.getLong(cursor.getColumnIndex(COLUMN_CEP_PAR_DUR));
                                    datas.put("parcour_duration", parcoursDuration);
                                    /* end distanceCovered */

                                    /* nbEca */
                                    nbEca = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_NB_ECA));
                                    datas.put("nombre_eca", nbEca);
                                    /* end nbEca */

                                    /* nbBox */
                                    nbBox = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_NB_BOX));
                                    datas.put("nombre_box", nbBox);
                                    /* end nbBox */

                                    /* AVFM */
                                    device_a = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_A)) >= 0 ? cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_A)) : sharedPref.getInt(COLUMN_CEP_DEVICE_A, -1);
                                    datas.put("A", device_a);
                                    device_v = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_V)) >= 0 ? cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_V)) : sharedPref.getInt(COLUMN_CEP_DEVICE_V, -1);
                                    datas.put("V", device_v);
                                    device_f = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_F)) >= 0 ? cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_F)) : sharedPref.getInt(COLUMN_CEP_DEVICE_F, -1);
                                    datas.put("F", device_f);
                                    device_m = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_M)) >= 0 ? cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_DEVICE_M)) : sharedPref.getInt(COLUMN_CEP_DEVICE_M, -1);
                                    datas.put("M", device_m);
                                    /* end AVFM */

                                    /* status */
                                    status = cursor.getInt(cursor.getColumnIndex(COLUMN_CEP_STATUS));
                                    datas.put("status", status);
                                    /* end status */

                                    // data
                                    json_array.put(datas);
                                    // next
                                    cursor.moveToNext();
                                }

                                /**
                                 * on efface les données
                                 * @Arnaud
                                 */
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.remove(COLUMN_CEP_DEVICE_A);
                                editor.remove(COLUMN_CEP_DEVICE_V);
                                editor.remove(COLUMN_CEP_DEVICE_F);
                                editor.remove(COLUMN_CEP_DEVICE_M);
                                editor.apply();
                                ColorCEP.getInstance().unsetColors();

                                // write
                                json.put("data", json_array);
                                output.write(json.toString());

                                // output
                                // System.out.println(json.toString());
                                System.out.println(file.isFile());

                                output.flush();
                                output.close();

                                Log.w(TAG, "FILE CREATED:" + file.getAbsolutePath());
                            } else {
                                Log.w(TAG, "FILE NOT CREATED OR EXISTS:" + file.getAbsolutePath());
                                // MainActivity.instance().Alert("File CEP not created", Toast.LENGTH_LONG);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        cursor.close();
                    }
                    DatabaseManager.getInstance().closeDatabase();
                }

            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
