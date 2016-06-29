package com.ikalogic.franck.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.ikalogic.franck.bluetooth.device.ActionCallback;
import com.ikalogic.franck.bluetooth.device.BluetoothIO;
import com.ikalogic.franck.bluetooth.device.NotifyListener;

import java.util.UUID;

/**
 * Created by franck on 6/18/16.
 */

public class BluetoothDev {

    private static final String TAG = "BluetoothDev";
    protected Context context;
    protected BluetoothIO io;

    public BluetoothDev(Context context) {
        this.context = context;
        this.io = new BluetoothIO();
    }

    public void connect(BluetoothDevice device, final ActionCallback callback){
        this.io.connect(context, device, callback);
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.io.setDisconnectedListener(disconnectedListener);
    }

    public void disconnect() { this.io.disconnect(); }

    public BluetoothDevice getDevice() { return this.io.getDevice(); }

    public String readName() {
        String ret = "";
        BluetoothDevice device = getDevice();
        if( null != device ) ret = device.getName();
        return ret;
    }

    public String readAlias(){
        String ret = "";
        BluetoothDevice device = getDevice();
        if( null != device ) ret = this.io.getAliasName( getDevice() );
        return ret;
    }

    public String readAddress() {
        String ret = "";
        BluetoothDevice device = getDevice();
        if( null != device ) ret = device.getAddress();
        return ret;
    }

    public void readRssi(ActionCallback callback) { this.io.readRssi(callback); }

    public boolean exist(UUID serviceUUID) {
        return this.io.serviceExist(serviceUUID);
    }

    public boolean exist(UUID serviceUUID, UUID characteristicId) {
        return this.io.characteristicExist(serviceUUID,characteristicId);
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
