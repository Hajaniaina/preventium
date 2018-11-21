package com.preventium.boxpreventium.location;

import com.preventium.boxpreventium.database.DatabaseHelper;

import org.json.JSONObject;

/**
 * Created by tog on 05/11/2018.
 */

public class MarkerData {
    public int COLUMN_MARKER_ID = 0;
    public String COLUMN_MARKER_IMEI = "";
    public String COLUMN_MARKER_DATE = "";
    public double COLUMN_MARKER_LAT = 0.0;
    public double COLUMN_MARKER_LONG = 0.0;
    public String COLUMN_MARKER_LABEL = "";
    public String COLUMN_MARKER_ATTACHMENT = "";
    public int COLUMN_MARKER_ENTERPRISE_ID = 0;
    public int COLUMN_MARKER_CREATEUR_ID = 0;
    public int COLUMN_MARKER_PERIMETRE = 0;
    public String COLUMN_MARKER_TITRE = "";

    public MarkerData () {}

    public MarkerData (JSONObject json) {
        try {
            this.COLUMN_MARKER_ID = json.getInt(DatabaseHelper.COLUMN_MARKER_ID);
            this.COLUMN_MARKER_IMEI = json.getString(DatabaseHelper.COLUMN_MARKER_IMEI);
            this.COLUMN_MARKER_DATE = json.getString(DatabaseHelper.COLUMN_MARKER_DATE);
            this.COLUMN_MARKER_LAT = json.getDouble(DatabaseHelper.COLUMN_MARKER_LAT);
            this.COLUMN_MARKER_LONG = json.getDouble(DatabaseHelper.COLUMN_MARKER_LONG);
            this.COLUMN_MARKER_LABEL = json.getString(DatabaseHelper.COLUMN_MARKER_LABEL);
            this.COLUMN_MARKER_ATTACHMENT = json.getString(DatabaseHelper.COLUMN_MARKER_ATTACHMENT);
            this.COLUMN_MARKER_ENTERPRISE_ID = json.getInt(DatabaseHelper.COLUMN_MARKER_ENTERPRISE_ID);
            this.COLUMN_MARKER_CREATEUR_ID = json.getInt(DatabaseHelper.COLUMN_MARKER_CREATEUR_ID);
            this.COLUMN_MARKER_PERIMETRE = json.getInt(DatabaseHelper.COLUMN_MARKER_PERIMETRE);
            this.COLUMN_MARKER_TITRE = json.getString(DatabaseHelper.COLUMN_MARKER_TITRE);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON () {
        JSONObject json = new JSONObject();
        try {
            json.put(DatabaseHelper.COLUMN_MARKER_ID, this.COLUMN_MARKER_ID);
            json.put(DatabaseHelper.COLUMN_MARKER_IMEI, this.COLUMN_MARKER_IMEI);
            json.put(DatabaseHelper.COLUMN_MARKER_DATE, this.COLUMN_MARKER_DATE);
            json.put(DatabaseHelper.COLUMN_MARKER_LAT, this.COLUMN_MARKER_LAT);
            json.put(DatabaseHelper.COLUMN_MARKER_LONG, this.COLUMN_MARKER_LONG);
            json.put(DatabaseHelper.COLUMN_MARKER_LABEL, this.COLUMN_MARKER_LABEL);
            json.put(DatabaseHelper.COLUMN_MARKER_ATTACHMENT, this.COLUMN_MARKER_ATTACHMENT);
            json.put(DatabaseHelper.COLUMN_MARKER_ENTERPRISE_ID, this.COLUMN_MARKER_ENTERPRISE_ID);
            json.put(DatabaseHelper.COLUMN_MARKER_CREATEUR_ID, this.COLUMN_MARKER_CREATEUR_ID);
            json.put(DatabaseHelper.COLUMN_MARKER_PERIMETRE, this.COLUMN_MARKER_PERIMETRE);
            json.put(DatabaseHelper.COLUMN_MARKER_TITRE, this.COLUMN_MARKER_TITRE);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return json;
    }
}
