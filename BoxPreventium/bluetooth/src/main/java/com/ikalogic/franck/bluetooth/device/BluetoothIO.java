package com.ikalogic.franck.bluetooth.device;

/**
 * Created by franck on 6/18/16.
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;
import java.util.prefs.PreferenceChangeEvent;

public class BluetoothIO extends BluetoothGattCallback {

    private static final String TAG = "BluetoothIO";
    public BluetoothGatt gatt;
    ActionCallback currentCallback;
    HashMap<UUID, NotifyListener> notifyListeners = new HashMap<UUID, NotifyListener>();
    NotifyListener disconnectedListener = null;

    public void connect(final Context context, BluetoothDevice device, final ActionCallback callback) {
        BluetoothIO.this.currentCallback = callback;
        device.connectGatt(context, false, BluetoothIO.this);
    }

    public void disconnect() {
        if( null == gatt ) {
            Log.e(TAG, "Connect to device first");
            return;
        }
        this.gatt.disconnect();
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.disconnectedListener = disconnectedListener;
    }

    public BluetoothDevice getDevice() {
        if (null == gatt) {
            Log.e(TAG, "Connect to device first");
            return null;
        }
        return gatt.getDevice();
    }

    public void writeAndRead(final UUID serviceUUID, final UUID uuid, byte[] valueToWrite, final ActionCallback callback) {
        ActionCallback readCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object characteristic) {
                BluetoothIO.this.readCharacteristic(serviceUUID,uuid, callback);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        this.writeCharacteristic(serviceUUID,uuid, valueToWrite, readCallback);
    }

    public void writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] value, ActionCallback callback) {
        try {
            if (null == gatt) {
                Log.e(TAG, "Connect to device first");
                throw new Exception("Connect to device first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(characteristicUUID);
            if (null == chara) {
                this.onFail(-1, "BluetoothGattCharacteristic " + characteristicUUID + " is not exsit");
                return;
            }
            chara.setValue(value);
            if (false == this.gatt.writeCharacteristic(chara)) {
                this.onFail(-1, "gatt.writeCharacteristic() return false");
            }
        } catch (Throwable tr) {
            Log.e(TAG, "writeCharacteristic", tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public void readCharacteristic(UUID serviceUUID, UUID uuid, ActionCallback callback) {
        try {
            if (null == gatt) {
                Log.e(TAG, "Connect to device first");
                throw new Exception("Connect to device first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(uuid);
            if (null == chara) {
                this.onFail(-1, "BluetoothGattCharacteristic " + uuid + " is not exsit");
                return;
            }
            if (false == this.gatt.readCharacteristic(chara)) {
                this.onFail(-1, "gatt.readCharacteristic() return false");
            }
        } catch (Throwable tr) {
            Log.e(TAG, "readCharacteristic", tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public void readRssi(ActionCallback callback) {
        try {
            if (null == gatt) {
                Log.e(TAG, "Connect to device first");
                throw new Exception("Connect to device first");
            }
            this.currentCallback = callback;
            this.gatt.readRemoteRssi();
        } catch (Throwable tr) {
            Log.e(TAG, "readRssi", tr);
            this.onFail(-1, tr.getMessage());
        }

    }

    public void setNotifyListener(UUID serviceUUID, UUID characteristicId, NotifyListener listener) {
        if (null == gatt) {
            Log.e(TAG, "Connect to device first");
            return;
        }

        BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(characteristicId);
        if (chara == null) {
            Log.e(TAG, "characteristicId " + characteristicId.toString() + " not found in service " + serviceUUID.toString());
            return;
        }

        this.gatt.setCharacteristicNotification(chara, true);
        BluetoothGattDescriptor descriptor = chara.getDescriptor(Profile.UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        this.gatt.writeDescriptor(descriptor);
        this.notifyListeners.put(characteristicId, listener);
    }

    public boolean serviceExist(UUID serviceUUID){
        boolean ret = false;
        try {
            if (null == gatt) {
                Log.e(TAG, "Connect to device first");
                throw new Exception("Connect to device first");
            }
            BluetoothGattService serv = gatt.getService(serviceUUID);
            if( serv != null ) ret = true;
        } catch (Throwable tr) {
            Log.e(TAG, "serviceExist", tr);
            this.onFail(-1, tr.getMessage());
        }
        return ret;
    }

    public boolean characteristicExist(UUID serviceUUID, UUID characteristicId){
        boolean ret = false;
        try {
            if (null == gatt) {
                Log.e(TAG, "Connect to device first");
                throw new Exception("Connect to device first");
            }
            BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(characteristicId);
            if (chara != null) ret = true;
        } catch (Throwable tr) {
            Log.e(TAG, "characteristicExist", tr);
            this.onFail(-1, tr.getMessage());
        }
        return ret;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            gatt.close();
            if (this.disconnectedListener != null)
                this.disconnectedListener.onNotify(null);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(characteristic);
        } else {
            this.onFail(status, "onCharacteristicRead fail");
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(characteristic);
        } else {
            this.onFail(status, "onCharacteristicWrite fail");
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(rssi);
        } else {
            this.onFail(status, "onCharacteristicRead fail");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            this.gatt = gatt;
            this.onSuccess(null);
        } else {
            this.onFail(status, "onServicesDiscovered fail");
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (this.notifyListeners.containsKey(characteristic.getUuid())) {
            this.notifyListeners.get(characteristic.getUuid()).onNotify(characteristic.getValue());
        }
    }

    private void onSuccess(Object data) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            this.currentCallback = null;
            callback.onSuccess(data);
        }
    }

    private void onFail(int errorCode, String msg) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            this.currentCallback = null;
            callback.onFail(errorCode, msg);
        }
    }

    // Change name

    public static boolean setAliasName(BluetoothDevice device, String alias){

        boolean ret = false;
        try {
            Method method = device.getClass().getMethod("setAlias", String.class);
            if(method != null) {
                method.invoke(device, alias);
                ret = true;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }


        return ret;
    }

    public static String getAliasName(BluetoothDevice device) {
        String deviceAlias = device.getName();
        try {
            Method method = device.getClass().getMethod("getAliasName");
            if (method != null) {
                deviceAlias = (String) method.invoke(device);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return deviceAlias;
    }

}
