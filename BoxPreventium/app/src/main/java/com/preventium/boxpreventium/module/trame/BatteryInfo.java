package com.preventium.boxpreventium.module.trame;

import com.preventium.boxpreventium.utils.CommonUtils;

/**
 * Created by Franck on 20/07/2016.
 */

public class BatteryInfo {

    private byte[] data = new byte[0];
    private int bat_level = 0;
    private boolean S = false;
    private boolean C = false;
    private boolean F = false;

    public static BatteryInfo fromData(byte[] data ) {
        BatteryInfo info = new BatteryInfo();
        if( data.length == 3 ) {
            info.data = data;
            if( data[0] == (byte)0xBA ) {
                info.bat_level = (data[1] * 100) / 0xFF ;
                info.S = ( ((data[2]&0b10000000) >> 7) != 0 );
                info.C = ( ((data[2]&0b01000000) >> 6) != 0 );
                info.F = ( ((data[2]&0b00100000) >> 5) != 0 );
            }
        }
        return info;
    }
    public int level() { return bat_level; }
    public boolean running() { return S; }
    public boolean charging() { return C; }
    public boolean full() { return F; }
    public String toString() {
        return String.format("Battery { battery level: %d%, engine running: %s, "
                +"battery charging: %s, full battery: %s, data: %s }",
                bat_level, S, C, F, CommonUtils.dataToHex(data) ); }
}
