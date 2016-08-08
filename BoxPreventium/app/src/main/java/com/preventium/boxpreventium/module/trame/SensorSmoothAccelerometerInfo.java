package com.preventium.boxpreventium.module.trame;

import com.preventium.boxpreventium.utils.BytesUtils;

/**
 * Created by Franck on 08/08/2016.
 */

public class SensorSmoothAccelerometerInfo {

    private byte[] data = new byte[0];
    private byte aa = 0x00;
    private byte bb = 0x00;
    private int A1 = 0;

    public static SensorSmoothAccelerometerInfo fromData( byte[] data ) {
        SensorSmoothAccelerometerInfo info = new SensorSmoothAccelerometerInfo();
        if( data.length == 3 ) {
            info.data = data;
            if( data[0] == (byte)0xA1 ) {
                info.aa = data[1];
                info.bb = data[2];
                info.A1 = ( (info.aa << 16) & 0xFF00 ) | ( info.bb & 0x00FF );
            }
        }
        return info;
    }
    public int value() { return A1; }
    public String toString() {
        return String.format("Sensor smooth accelerometer { value: %d, data: %d }"
                , A1, BytesUtils.dataToHex(data)); }
}
