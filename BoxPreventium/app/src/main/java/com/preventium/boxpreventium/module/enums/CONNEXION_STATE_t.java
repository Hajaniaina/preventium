package com.preventium.boxpreventium.module.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 04/08/2016.
 */


public enum CONNEXION_STATE_t {
    CONNECTING(0),
    CONNECTED(1),
    DISCONNECTED(2);
    private int value;
    private static Map map = new HashMap<>();
    private CONNEXION_STATE_t(int value) {
        this.value = value;
    }
    static {
        for (CONNEXION_STATE_t statusType : CONNEXION_STATE_t.values()) {
            map.put(statusType.value, statusType);
        }
    }
    public static CONNEXION_STATE_t valueOf(int statusType) {
        return (CONNEXION_STATE_t) map.get(statusType);
    }
    public int getValue() {return value;}

    @Override
    public String toString() {
        switch ( valueOf(value) ) {
            case CONNECTING: return "Connecting";
            case CONNECTED: return "Connected";
            case DISCONNECTED: return "Disconnected";
        }
        return "";
    }
}

