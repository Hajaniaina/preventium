package com.preventium.boxpreventium.enums;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 13/09/2016.
 */

public enum MOVING_t {

    NO_RIDE(0),
    RIDE_IN_MOVING(1),
    RIDE_IN_PAUSE(2);

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
            case NO_RIDE    :
                return "MOVING_t[NO_RIDE]";
            case RIDE_IN_MOVING    :
                return "MOVING_t[RIDE_IN_MOVING]";
            case RIDE_IN_PAUSE  :
                return "MOVING_t[RIDE_IN_PAUSE]";
            default                 :
                return "MOVING_t[???]";
        }
    }
}
