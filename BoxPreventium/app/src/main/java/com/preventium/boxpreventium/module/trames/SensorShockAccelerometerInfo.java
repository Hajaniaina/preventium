package com.preventium.boxpreventium.module.trames;

import com.preventium.boxpreventium.utils.BytesUtils;

import java.util.Locale;

/**
 * Created by Franck on 14/09/2016.
 */

public class SensorShockAccelerometerInfo {

    private byte[] data = new byte[0];
    private byte aa = 0x00;
    private byte bb = 0x00;
    private short A2 = 0;
    private double mG = 0.0;

    public static SensorShockAccelerometerInfo fromData(byte[] data ) {
        SensorShockAccelerometerInfo info = new SensorShockAccelerometerInfo();
        if( data.length == 3 ) {
            info.data = data;
            if( data[0] == (byte)0xA2 ) {
                info.aa = data[1];
                info.bb = data[2];
                info.A2 = (short)((( info.aa & 0xFF) << 8) | ( info.bb & 0xFF));
                info.mG = (double)info.A2 * 8000.0 / 32000.0;
            }
        }
        return info;
    }
    public short value_raw() { return A2; }
    public double value() { return mG; }
    public String toString() {
        return String.format(Locale.getDefault(),"Sensor shock accelerometer { value: %s mG, data: %s }"
                , mG, BytesUtils.dataToHex(data)); }
}
