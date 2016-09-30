package com.preventium.boxpreventium.enums;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 13/09/2016.
 */

public enum MOVING_t {
    UNKNOW(-1),
    STP(0),
    ACC(1),
    BRK(2),
    CST(3);

    private int value;
    private static Map<Integer, MOVING_t> map = new HashMap<>();

    MOVING_t(int value) { this.value = value; }
    static { for (MOVING_t e : MOVING_t.values() ) map.put(e.value, e); }
    public static MOVING_t valueOf(int cmdType) {
        return map.get(cmdType);
    }
    public int getValue() { return value; }
    @NonNull
    public String toString() {
        switch ( valueOf(value) ) {
            case STP    :
                return "MOVING_t[Stopped]";
            case ACC    :
                return "MOVING_t[Acceleration]";
            case BRK  :
                return "MOVING_t[Freinage]";
            case CST  :
                return "MOVING_t[Constant speed]";
            default                 :
                return "MOVING_t[???]";
        }
    }
}
