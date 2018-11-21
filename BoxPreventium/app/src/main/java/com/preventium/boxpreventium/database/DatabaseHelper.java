package com.preventium.boxpreventium.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Franck on 02/03/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "DatabaseHelper";
    private static final int DATABASE_VERSION = 2;

    // EPC SELECTED: Table Names and Colums names
    public static final String TABLE_EPC = "epc";
    public static final String COLUMN_EPC_ID = "id";
    public static final String COLUMN_EPC_PARCOUR_ID = "parcour_id";
    public static final String COLUMN_EPC_NUM_EPC = "num_epc";

    // ECA: Table Name and column names
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

    /// DriverID: Table and columns name
    public static final String TABLE_DRIVER = "eca_driver";
    public static final String COLUMN_DRIVER_TIME = "time";
    public static final String COLUMN_DRIVER_ID = "id";
    public static final String COLUMN_DRIVER_PARCOUR_ID = "parcour_id";

    // CEP: : Table and columns name
    public static final String TABLE_CEP = "cep";
    public static final String COLUMN_CEP_ID = "id";
    public static final String COLUMN_CEP_TIME = "time";
    public static final String COLUMN_CEP_MAC = "mac";
    public static final String COLUMN_CEP_LONG_POS = "long_pos";
    public static final String COLUMN_CEP_LAT_POS = "lat_pos";
    public static final String COLUMN_CEP_STATUS = "status";
    public static final String COLUMN_CEP_NB_BOX = "nb_box";
    public static final String COLUMN_CEP_NB_ECA = "nb_eca";
    public static final String COLUMN_CEP_NOTE = "note";
    public static final String COLUMN_CEP_PAR_DUR = "parcour_duration";
    public static final String COLUMN_CEP_VITESSE_LD = "vitesse_ld";
    public static final String COLUMN_CEP_VITESSE_VR = "vitesse_vr";
    public static final String COLUMN_CEP_DIST_COV = "distance_covered";
    public static final String COLUMN_CEP_DEVICE_A = "device_a";
    public static final String COLUMN_CEP_DEVICE_V = "device_v";
    public static final String COLUMN_CEP_DEVICE_F = "device_f";
    public static final String COLUMN_CEP_DEVICE_M = "device_m";

    /* for marqueur */
    public static final String TABLE_MARKER = "marker_share";
    public static final String COLUMN_MARKER_ID = "id_marker";
    public static final String COLUMN_MARKER_IMEI = "marker_imei";
    public static final String COLUMN_MARKER_DATE = "marker_date";
    public static final String COLUMN_MARKER_LAT = "marker_lat";
    public static final String COLUMN_MARKER_LONG = "marker_long";
    public static final String COLUMN_MARKER_LABEL = "marker_label";
    public static final String COLUMN_MARKER_ATTACHMENT = "marker_attachment";
    public static final String COLUMN_MARKER_ENTERPRISE_ID = "marker_enterprise_id";
    public static final String COLUMN_MARKER_CREATEUR_ID = "marker_createur_id";
    public static final String COLUMN_MARKER_PERIMETRE = "marker_perimetre";
    public static final String COLUMN_MARKER_TITRE = "marker_titre";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_ECA_TABLE =
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
                        COLUMN_ECA_DISTANCE + " FLOAT);";
        String CREATE_CEP_TABLE =
                "CREATE TABLE " + TABLE_CEP + " (" +
                        COLUMN_CEP_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_CEP_TIME + " INTEGER, " +
                        COLUMN_CEP_LONG_POS + " FLOAT, " +
                        COLUMN_CEP_LAT_POS + " FLOAT, " +
                        COLUMN_CEP_MAC + " TEXT, " +
                        COLUMN_CEP_NOTE + " INTEGER, " +
                        COLUMN_CEP_VITESSE_LD + " INTEGER, " +
                        COLUMN_CEP_VITESSE_VR + " INTEGER, " +
                        COLUMN_CEP_DIST_COV + " INTEGER, " +
                        COLUMN_CEP_PAR_DUR + " INTEGER, " +
                        COLUMN_CEP_NB_ECA + " INTEGER, " +
                        COLUMN_CEP_NB_BOX + " INTEGER, " +
                        COLUMN_CEP_DEVICE_A + " INTEGER, " +
                        COLUMN_CEP_DEVICE_V + " INTEGER, " +
                        COLUMN_CEP_DEVICE_F + " INTEGER, " +
                        COLUMN_CEP_DEVICE_M + " INTEGER, " +
                        COLUMN_CEP_STATUS + " INTEGER);";
        String CREATE_DRIVERID_TABLE =
                "CREATE TABLE " + TABLE_DRIVER + " (" +
                        COLUMN_DRIVER_TIME + " INTEGER PRIMARY KEY, " +
                        COLUMN_DRIVER_ID + " INTEGER, " +
                        COLUMN_DRIVER_PARCOUR_ID + " INTEGER );";
        String CREATE_EPCID_TABLE =
                "CREATE TABLE " + TABLE_EPC + " (" +
                        COLUMN_EPC_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_EPC_PARCOUR_ID + " INTEGER, " +
                        COLUMN_EPC_NUM_EPC + " INTEGER );";
        String CREATE_MARKER_TABLE =
                "CREATE TABLE " + TABLE_MARKER + " (" +
                        COLUMN_MARKER_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_MARKER_IMEI + " TEXT, " +
                        COLUMN_MARKER_LAT + " FLOAT, " +
                        COLUMN_MARKER_LONG + " FLOAT, " +
                        COLUMN_MARKER_LABEL + " TEXT, " +
                        COLUMN_MARKER_ATTACHMENT + " TEXT, " +
                        COLUMN_MARKER_ENTERPRISE_ID + " INTEGER, " +
                        COLUMN_MARKER_CREATEUR_ID + " INTEGER, " +
                        COLUMN_MARKER_PERIMETRE + " INTEGER, " +
                        COLUMN_MARKER_TITRE + " TEXT, " +
                        COLUMN_MARKER_DATE + " TEXT );";

        sqLiteDatabase.execSQL(CREATE_ECA_TABLE);
        sqLiteDatabase.execSQL(CREATE_CEP_TABLE);
        sqLiteDatabase.execSQL(CREATE_DRIVERID_TABLE);
        sqLiteDatabase.execSQL(CREATE_EPCID_TABLE);
        sqLiteDatabase.execSQL(CREATE_MARKER_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ECA);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DRIVER);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CEP);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EPC);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKER);
            onCreate(sqLiteDatabase);
        }
    }
}
