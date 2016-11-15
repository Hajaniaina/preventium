package com.preventium.boxpreventium.enums;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 13/09/2016.
 */

public enum STATUS_t {
    GETTING_CFG(0),
    GETTING_EPC(1),
    GETTING_DOBJ(2),
    CAR_STOPPED(3),
    CAR_MOVING(4),
    CAR_PAUSING(5);


    private int value;
    private static Map<Integer, STATUS_t> map = new HashMap<>();

    STATUS_t(int value) { this.value = value; }
    static { for (STATUS_t e : STATUS_t.values() ) map.put(e.value, e); }
    public static STATUS_t valueOf(int cmdType) {
        return map.get(cmdType);
    }
    public int getValue() { return value; }
    @NonNull
    public String toString() {
        switch ( valueOf(value) ) {
            case GETTING_CFG    :
                return "STATUS_t[GETTING_CFG]";
            case GETTING_EPC  :
                return "STATUS_t[GETTING_EPC]";
            case GETTING_DOBJ  :
                return "STATUS_t[GETTING_DOBJ]";
            case CAR_STOPPED    :
                return "STATUS_t[CAR_STOPPED]";
            case CAR_MOVING    :
                return "STATUS_t[CAR_MOVING]";
            case CAR_PAUSING    :
                return "STATUS_t[CAR_PAUSING]";
            default                 :
                return "STATUS_t[???]";
        }
    }
}
