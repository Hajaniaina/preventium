package com.preventium.boxpreventium.enums;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public enum SCORE_t {

    UNKNOW(-1),
    CORNERING(0),
    BRAKING(1),
    ACCELERATING(2),
    AVERAGE(3);

    private int value;
    private static Map<Integer, SCORE_t> map = new HashMap<>();

    SCORE_t(int value) { this.value = value; }
    static { for (SCORE_t e : SCORE_t.values() ) map.put(e.value, e); }
    public static SCORE_t valueOf(int cmdType) {
        return map.get(cmdType);
    }
    public int getValue() {
        return value;
    }

    @NonNull
    public String toString() {

        switch ( valueOf(value) ) {

            case CORNERING:
                return "SCORE_t[CORNERING]";
            case BRAKING:
                return "SCORE_t[BRAKING]";
            case ACCELERATING :
                return "SCORE_t[ACCELERATING]";
            case AVERAGE:
                return "SCORE_t[AVERAGE]";
            default                 :
                return "SCORE_t[???]";
        }
    }
}
