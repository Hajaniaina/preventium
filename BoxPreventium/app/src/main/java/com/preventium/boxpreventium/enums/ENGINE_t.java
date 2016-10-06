package com.preventium.boxpreventium.enums;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 13/09/2016.
 */

public enum ENGINE_t {
    UNKNOW(-1),
    OFF(0),
    ON(1);

    private int value;
    private static Map<Integer, ENGINE_t> map = new HashMap<>();

    ENGINE_t(int value) { this.value = value; }
    static { for (ENGINE_t e : ENGINE_t.values() ) map.put(e.value, e); }
    public static ENGINE_t valueOf(int cmdType) {
        return map.get(cmdType);
    }
    public int getValue() { return value; }
    @NonNull
    public String toString() {
        switch ( valueOf(value) ) {
            case OFF    :
                return "ENGINE_t[OFF]";
            case ON  :
                return "ENGINE_t[ON]";
            default                 :
                return "ENGINE_t[???]";
        }
    }
}
