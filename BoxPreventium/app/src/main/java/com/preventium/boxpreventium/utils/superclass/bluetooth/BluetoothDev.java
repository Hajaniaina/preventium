package com.preventium.boxpreventium.utils.superclass.bluetooth;

import android.annotation.SuppressLint;
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

public class BluetoothDev {
    private static final String TAG = "BluetoothDev";
    private ActionCallback connectedCallback = null;
    protected Context context;
    private NotifyListener disconnectNotify = null;
    protected BluetoothIO io;

    public BluetoothDev(Context context) {
        this.context = context;
        this.io = new BluetoothIO();
    }

    public void connect(BluetoothDevice device, ActionCallback callback) {
        this.io.connect(this.context, device, callback);
    }

    @SuppressLint("WrongConstant")
    public void connect(String address, ActionCallback callback) {
        connect(((BluetoothManager) this.context.getSystemService("bluetooth")).getAdapter().getRemoteDevice(address), callback);
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.io.setDisconnectedListener(disconnectedListener);
    }

    public void disconnect() {
        this.io.disconnect();
    }

    public BluetoothDevice getDevice() {
        return this.io.getDevice();
    }

    public String readName() {
        String ret = "";
        BluetoothDevice device = getDevice();
        if (device != null) {
            return device.getName();
        }
        return ret;
    }

    public String readAlias() {
        String ret = "";
        if (getDevice() == null) {
            return ret;
        }
        BluetoothIO bluetoothIO = this.io;
        return BluetoothIO.getAliasName(getDevice());
    }

    public String readAddress() {
        String ret = "";
        BluetoothDevice device = getDevice();
        if (device != null) {
            return device.getAddress();
        }
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
