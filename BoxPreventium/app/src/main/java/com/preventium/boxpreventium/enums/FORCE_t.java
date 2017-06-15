package com.preventium.boxpreventium.enums;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

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

    public AXE_t getAxe() {
        switch ( valueOf(value) ) {
            case TURN_LEFT :
            case TURN_RIGHT :
                return AXE_t.HORIZONTAL;
            case ACCELERATION :
            case BRAKING :
                return AXE_t.VERTICAL;
            case UNKNOW :
            default :
                return AXE_t.UNKNOW;
        }
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
            case UNKNOW :
                return "FORCE_t[UNKNOW]";
            default                 :
                return "FORCE_t[???]";
        }
    }
}
