package com.preventium.boxpreventium.enums;

import android.support.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;

public enum SPEED_t {

    UNKNOW(-1),
    IN_CORNERS(0),
    IN_STRAIGHT_LINE(1),
    MAX_LIMIT(2);

    private int value;
    private static Map<Integer, SPEED_t> map = new HashMap<>();

    SPEED_t(int value) { this.value = value; }
    static { for (SPEED_t e : SPEED_t.values() ) map.put(e.value, e); }
    public static SPEED_t valueOf(int cmdType) {
        return map.get(cmdType);
    }
    public int getValue() {
        return value;
    }

    @NonNull
    public String toString() {

        switch ( valueOf(value) ) {
            case IN_CORNERS    :
                return "SPEED_t[IN_CORNERS]";
            case IN_STRAIGHT_LINE    :
                return "SPEED_t[IN_STRAIGHT_LINE]";
            case MAX_LIMIT  :
                return "SPEED_t[MAX_LIMIT]";
            default                 :
                return "SPEED_t[???]";
        }
    }
}

