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
    PAR_STOPPED(3),
    PAR_STARTED(4),
    PAR_PAUSING(5),
    PAR_RESUME(6);


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
            case PAR_STOPPED:
                return "STATUS_t[PAR_STOPPED]";
            case PAR_STARTED:
                return "STATUS_t[PAR_STARTED]";
            case PAR_PAUSING:
                return "STATUS_t[PAR_PAUSING]";
            case PAR_RESUME:
                return "STATUS_t[PAR_RESUME]";
            default                 :
                return "STATUS_t[???]";
        }
    }
}
