package com.preventium.boxpreventium.module.trames;

import com.preventium.boxpreventium.utils.BytesUtils;

import java.util.Locale;

/**
 * Created by Franck on 14/09/2016.
 */

public class SensorSmoothAccelerometerInfo {

    private byte[] data = new byte[0];
    private byte aa = 0x00;
    private byte bb = 0x00;
    private int A1 = 0;
    private double mG = 0.0;

    public static SensorSmoothAccelerometerInfo fromData(byte[] data ) {
        SensorSmoothAccelerometerInfo info = new SensorSmoothAccelerometerInfo();
        if( data.length == 3 ) {
            info.data = data;
            if( data[0] == (byte)0xA1 ) {
                info.aa = data[1];
                info.bb = data[2];
                info.A1 = (info.aa << 8) | info.bb;
                info.mG = (double)info.A1 * 2000.0 / 32000.0;
            }
        }
        return info;
    }
    public int value_raw() { return A1; }
    public double value() { return mG; }
    public String toString() {
        return String.format(Locale.getDefault(),"Sensor smooth accelerometer { value: %s mG, data: %s }"
                , mG, BytesUtils.dataToHex(data)); }
}
