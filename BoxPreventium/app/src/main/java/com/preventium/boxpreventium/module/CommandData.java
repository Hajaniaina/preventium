package com.preventium.boxpreventium.module;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 08/08/2016.
 */

public class CommandData {

    public enum Cmd {
        UNKNOW(-1),
        START_MEASURING('s'), PAUSE_MEASURING('p'),
        FIRST_CALIBRATION('g'), SECOND_CALIBRATION('o');
        private int value;
        private static Map map = new HashMap<>();
        Cmd(int value) { this.value = value; }
        static { for (Cmd cmdType : Cmd.values() ) map.put(cmdType.value, cmdType); }
        public static Cmd valueOf(int cmdType) {
            return (Cmd) map.get(cmdType);
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

    private Cmd cmd = Cmd.UNKNOW;
    public static CommandData newInstance( Cmd cmd ) {
        CommandData ret = new CommandData();
        ret.cmd = cmd;
        return ret;
    }
    public String toString() { return String.format("CommandData[ %s ]", this.cmd.toString()); }
    public byte[] toData() {
        byte[] data = new byte[1];
        data[0] = (byte)this.cmd.getValue();
        return data;
    }
}
