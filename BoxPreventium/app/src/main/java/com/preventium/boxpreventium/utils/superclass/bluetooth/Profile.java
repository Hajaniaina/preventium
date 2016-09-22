package com.preventium.boxpreventium.utils.superclass.bluetooth;

import java.util.UUID;

/**
 * Created by Franck on 13/09/2016.
 */

public class Profile {

    public static final UUID UUID_SERVICE_FIRMWARE
            = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_FIRMWARE
            = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_SERVICE_SENSOR_DATA
            = UUID.fromString("96343710-829a-4621-8cdd-19ac1155c1fc");
    public static final UUID UUID_CHAR_CMD_DATA
            = UUID.fromString("96343710-0e70-4d62-ab98-20efb5afcd07");
    public static final UUID UUID_CHAR_SENSOR_ACC1_DATA
            = UUID.fromString("96343710-246e-4660-bffe-8fc13809e424");
    public static final UUID UUID_CHAR_SENSOR_ACC2_DATA
            = UUID.fromString("96343710-cd6a-45df-8088-44eda01b07c1");
    public static final UUID UUID_CHAR_BATTERY
            = UUID.fromString("96343710-ea24-4423-8262-62501b6b9f96");

}
