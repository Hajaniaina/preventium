package com.ikalogic.franck.bluetooth.scanner;

import android.bluetooth.BluetoothDevice;

/**
 * Created by franck on 6/18/16.
 */
public interface ScannerCallback {

    void onScanState(boolean scanning);
    void onScanResult(BluetoothDevice device, int rssi);
    void onBluetoothAdapterIsDisable();
    void onBluetoothAdapterIsNull();
    void onLocationServiceIsDisable();
}
