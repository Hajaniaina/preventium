package com.preventium.boxpreventium.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

/**
 * Created by tog on 31/08/2018.
 */

public class DataLocal {

    private static DataLocal instance;

    private String key;
    private Context context;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    public static DataLocal get(Context context) {
        if( instance == null ) instance = new DataLocal(context);
        return instance;
    }

    private DataLocal (Context context) {
        this.context = context;
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = sharedPref.edit();
    }

    public Object getValue(String key, Object _defaultValue) {
        for(Map.Entry<String, ?> shared : sharedPref.getAll().entrySet()) {
            if( shared.getKey().equals(key) ) return shared.getValue();
        }
        return _defaultValue;
    }


    // getting float
    public float getFloat(String key, float _defaultValue) {
        return sharedPref.getFloat(key, _defaultValue);
    }

    // getting boolean
    public boolean getBoolean(String key, boolean _defaultValue) {
        return sharedPref.getBoolean(key, _defaultValue);
    }

    // getting int
    public int getInt(String key, int _defaultValue) {
        return sharedPref.getInt(key, _defaultValue);
    }

    // getting string
    public String getString(String key, String _defaultValue) {
        return sharedPref.getString(key, _defaultValue);
    }

    public void setValue(String key, Object value) {
        if( value.getClass().equals(Boolean.class) ) editor.putBoolean(key, (boolean) value);
        if( value.getClass().equals(String.class) ) editor.putString(key, value.toString());
        if( value.getClass().equals(Integer.class) ) editor.putInt(key, (int) value);
        if( value.getClass().equals(Float.class) ) editor.putFloat(key, (float) value);
    }

    public void remValue(String key) {
        editor.remove(key);
    }

    // commit end of transaction
    public void commit () {
        editor.commit();
    }

    public void apply () {
        editor.apply();
    }
}
