package com.preventium.boxpreventium.module.trame;

import com.preventium.boxpreventium.utils.CommonUtils;

/**
 * Created by Franck on 20/07/2016.
 */

public class SensorShockAccelerometerInfo {

    private byte[] data = new byte[0];
    private byte aa = 0x00;
    private byte bb = 0x00;
    private int A2 = 0;

    public static SensorShockAccelerometerInfo fromData(byte[] data ) {
        SensorShockAccelerometerInfo info = new SensorShockAccelerometerInfo();
        if( data.length == 3 ) {
            info.data = data;
            if( data[0] == (byte)0xA2 ) {
                info.aa = data[1];
                info.bb = data[2];
                info.A2 = ( (info.aa << 16) & 0xFF00 ) | ( info.bb & 0x00FF );
            }
        }
        return info;
    }
    public int value() { return A2; }
    public String toString() {
        return String.format("Sensor shock accelerometer { value: %d, data: %d }"
                , A2, CommonUtils.dataToHex(data)); }
}
