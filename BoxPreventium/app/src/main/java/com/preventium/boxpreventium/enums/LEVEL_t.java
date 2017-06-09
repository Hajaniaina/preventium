package com.preventium.boxpreventium.enums;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 13/09/2016.
 */

public enum LEVEL_t {

    LEVEL_UNKNOW(-1),
    LEVEL_1(1),
    LEVEL_2(2),
    LEVEL_3(3),
    LEVEL_4(4),
    LEVEL_5(5);

    private int value;
    private static Map<Integer, LEVEL_t> map = new HashMap<>();

    LEVEL_t(int value) { this.value = value; }
    static { for (LEVEL_t e : LEVEL_t.values() ) map.put(e.value, e); }
    public static LEVEL_t valueOf(int cmdType) {
        return map.get(cmdType);
    }
    public int getValue() {
        return value;
    }
    public int get_ui_time_ms(){
        switch ( valueOf(value) ) {
            case LEVEL_1: return 3000;
            case LEVEL_2: return 3000;
            case LEVEL_3: return 3000;
            case LEVEL_4: return 5000;
            case LEVEL_5: return 5000;
            case LEVEL_UNKNOW: return 0;
        }
        return 0;
    }
    @NonNull
    public String toString() {
        switch ( valueOf(value) ) {
            case LEVEL_UNKNOW    :
                return "LEVEL_t[LEVEL_UNKNOW]";
            case LEVEL_1    :
                return "LEVEL_t[LEVEL_1]";
            case LEVEL_2    :
                return "LEVEL_t[LEVEL_2]";
            case LEVEL_3  :
                return "LEVEL_t[LEVEL_3]";
            case LEVEL_4 :
                return "LEVEL_t[LEVEL_4]";
            case LEVEL_5 :
                return "LEVEL_t[LEVEL_5]";
            default                 :
                return "LEVEL_t[???]";
        }
    }
}
