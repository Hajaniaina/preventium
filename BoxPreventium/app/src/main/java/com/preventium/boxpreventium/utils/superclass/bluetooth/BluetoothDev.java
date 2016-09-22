package com.preventium.boxpreventium.utils.superclass.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.preventium.boxpreventium.utils.superclass.bluetooth.device.ActionCallback;
import com.preventium.boxpreventium.utils.superclass.bluetooth.device.BluetoothIO;
import com.preventium.boxpreventium.utils.superclass.bluetooth.device.NotifyListener;

import java.util.UUID;

/**
 * Created by Franck on 13/09/2016.
 */

public class BluetoothDev {

    private static final String TAG = "BluetoothDev";
    protected Context context;
    protected BluetoothIO io;

    private ActionCallback connectedCallback = null;
    private NotifyListener disconnectNotify = null;

    public BluetoothDev(Context context) {
        this.context = context;
        this.io = new BluetoothIO();
    }

    public void connect(BluetoothDevice device, final ActionCallback callback) {
        this.io.connect(context, device, callback);
    }

    public void connect(String address, final ActionCallback callback) {
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        connect(device, callback);
    }

    public void setDisconnectedListener(final NotifyListener disconnectedListener) {
        this.io.setDisconnectedListener(disconnectedListener);
    }

    public void disconnect() { this.io.disconnect(); }

    public BluetoothDevice getDevice() { return this.io.getDevice(); }

    public String readName() {
        String ret = "";
        BluetoothDevice device = getDevice();
        if (null != device) ret = device.getName();
        return ret;
    }

    public String readAlias() {
        String ret = "";
        BluetoothDevice device = getDevice();
        if (null != device) ret = this.io.getAliasName(getDevice());
        return ret;
    }

    public String readAddress() {
        String ret = "";
        BluetoothDevice device = getDevice();
        if (null != device) ret = device.getAddress();
        return ret;
    }

    public void readRssi(ActionCallback callback) {
        this.io.readRssi(callback);
    }

    public boolean exist(UUID serviceUUID) {
        return this.io.serviceExist(serviceUUID);
    }

    public boolean exist(UUID serviceUUID, UUID characteristicId) {
        return this.io.characteristicExist(serviceUUID, characteristicId);
    }

    public void showServicesAndCharacteristics() {
        for (BluetoothGattService service : this.io.gatt.getServices()) {
            Log.d(TAG, "onServicesDiscovered:" + service.getUuid());
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                Log.d(TAG, "  char:" + characteristic.getUuid());
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    Log.d(TAG, "    descriptor:" + descriptor.getUuid());
                }
            }
        }
    }
}
