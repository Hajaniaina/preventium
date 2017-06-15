package com.preventium.boxpreventium.enums;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 13/09/2016.
 */
public enum AXE_t {
    UNKNOW(-1),
    HORIZONTAL(0),
    VERTICAL(2);

    private int value;
    private static Map<Integer, AXE_t> map = new HashMap<>();

    AXE_t(int value) { this.value = value; }
    static { for (AXE_t e : AXE_t.values() ) map.put(e.value, e); }
    public static AXE_t valueOf(int cmdType) {
        return map.get(cmdType);
    }
    public int getValue() {
        return value;
    }

    @NonNull
    public String toString() {
        switch ( valueOf(value) ) {
            case HORIZONTAL :
                return "AXE_t[HORIZONTAL]";
            case VERTICAL :
                return "AXE_t[VERTICAL]";
            case UNKNOW :
                return "AXE_t[UNKNOW]";
            default                 :
                return "AXE_t[???]";
        }
    }
}
