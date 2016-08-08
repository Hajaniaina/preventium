package com.preventium.boxpreventium.bluetooth.scanner;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Franck on 08/08/2016.
 */

public interface ScannerCallback {

    void onScanState(boolean scanning);
    void onScanResult(BluetoothDevice device, int rssi);
    void onBluetoothAdapterIsDisable();
    void onBluetoothAdapterIsNull();
    void onLocationServiceIsDisable();
}
