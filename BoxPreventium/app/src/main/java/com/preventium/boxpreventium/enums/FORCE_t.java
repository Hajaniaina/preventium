package com.preventium.boxpreventium.enums;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 13/09/2016.
 */

public enum FORCE_t {

    UNKNOW(-1),
    TURN_LEFT(0),
    TURN_RIGHT(1),
    ACCELERATION(2),
    BRAKING(3);

    private int value;
    private static Map<Integer, FORCE_t> map = new HashMap<>();

    FORCE_t(int value) { this.value = value; }
    static { for (FORCE_t e : FORCE_t.values() ) map.put(e.value, e); }
    public static FORCE_t valueOf(int cmdType) {
        return map.get(cmdType);
    }
    public int getValue() {
        return value;
    }
    @NonNull
    public String toString() {
        switch ( valueOf(value) ) {
            case TURN_LEFT    :
                return "FORCE_t[TURN_LEFT]";
            case TURN_RIGHT    :
                return "FORCE_t[TURN_RIGHT]";
            case ACCELERATION  :
                return "FORCE_t[ACCELERATION]";
            case BRAKING :
                return "FORCE_t[BRAKING]";
            default                 :
                return "FORCE_t[???]";
        }
    }
}
