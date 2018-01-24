package com.preventium.boxpreventium.utils.superclass.bluetooth.scanner;

import android.bluetooth.BluetoothDevice;

public interface ScannerCallback {
    void onBluetoothAdapterIsDisable();

    void onBluetoothAdapterIsNull();

    void onLocationServiceIsDisable();

    void onScanResult(BluetoothDevice bluetoothDevice, int i);

    void onScanState(boolean z);
}
