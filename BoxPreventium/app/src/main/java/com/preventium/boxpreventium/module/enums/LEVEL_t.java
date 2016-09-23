package com.preventium.boxpreventium.module.enums;

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
    @NonNull
    public String toString() {
        switch ( valueOf(value) ) {
            case LEVEL_1    :
                return "LEVEL_t[LEVEL_1]";
            case LEVEL_2    :
                return "LEVEL_t[LEVEL_2]";
            case LEVEL_3  :
                return "LEVEL_t[LEVEL_3]";
            case LEVEL_4 :
                return "LEVEL_t[LEVEL_4]";
            case LEVEL_5 :
                return "LEVEL_t[LEVEL_1]";
            default                 :
                return "LEVEL_t[???]";
        }
    }
}
