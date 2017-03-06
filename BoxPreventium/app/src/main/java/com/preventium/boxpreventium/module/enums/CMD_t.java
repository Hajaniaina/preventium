package com.preventium.boxpreventium.module.enums;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 13/09/2016.
 */

public enum CMD_t {

    UNKNOW(-1),
    START_MEASURING('s'),
    PAUSE_MEASURING('p'),
    FIRST_CALIBRATION('g'),
    SECOND_CALIBRATION('o'),
    RAZ_CALIBRATION('k');

    private int value;
    private static Map<Integer, CMD_t> map = new HashMap<>();

    CMD_t(int value) { this.value = value; }
    static { for (CMD_t e : CMD_t.values() ) map.put(e.value, e); }
    public static CMD_t valueOf(int cmdType) {
        return map.get(cmdType);
    }
    public int getValue() {
        return value;
    }
    @NonNull
    public String toString() {
        switch ( valueOf(value) ) {
            case START_MEASURING    :
                return "Start measuring (streaming) {'s',0x73}";
            case PAUSE_MEASURING    :
                return "Pause measuring (streaming) {'p',0x70}";
            case FIRST_CALIBRATION  :
                return "First calibration (measure of severity) {'g',0x67}";
            case SECOND_CALIBRATION :
                return "Second calibration (measurement of the vehicle traveling direction) {'o',0x6F}";
            default                 :
                return "Unknow command";
        }
    }
}
