package com.preventium.boxpreventium.utils.superclass.bluetooth.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

public class BluetoothIO extends BluetoothGattCallback {
    private static final String TAG = "BluetoothIO";
    ActionCallback currentCallback;
    NotifyListener disconnectedListener = null;
    public BluetoothGatt gatt;
    HashMap<UUID, NotifyListener> notifyListeners = new HashMap();

    public void connect(Context context, BluetoothDevice device, ActionCallback callback) {
        this.currentCallback = callback;
        device.connectGatt(context, false, this);
    }

    public void disconnect() {
        if (this.gatt == null) {
            Log.e(TAG, "Connect to device first");
        } else {
            this.gatt.disconnect();
        }
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.disconnectedListener = disconnectedListener;
    }

    public BluetoothDevice getDevice() {
        if (this.gatt != null) {
            return this.gatt.getDevice();
        }
        Log.e(TAG, "Connect to device first");
        return null;
    }

    public void writeAndRead(final UUID serviceUUID, final UUID uuid, byte[] valueToWrite, final ActionCallback callback) {
        writeCharacteristic(serviceUUID, uuid, valueToWrite, new ActionCallback() {
            public void onSuccess(Object characteristic) {
                BluetoothIO.this.readCharacteristic(serviceUUID, uuid, callback);
            }

            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        });
    }

    public void writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] value, ActionCallback callback) {
        try {
            if (this.gatt == null) {
                Log.e(TAG, "Connect to device first");
                throw new Exception("Connect to device first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = this.gatt.getService(serviceUUID).getCharacteristic(characteristicUUID);
            if (chara == null) {
                onFail(-1, "BluetoothGattCharacteristic " + characteristicUUID + " is not exsit");
                return;
            }
            chara.setValue(value);
            if (!this.gatt.writeCharacteristic(chara)) {
                onFail(-1, "gatt.writeCharacteristic() return false");
            }
        } catch (Throwable tr) {
            Log.e(TAG, "writeCharacteristic", tr);
            onFail(-1, tr.getMessage());
        }
    }

    public void readCharacteristic(UUID serviceUUID, UUID uuid, ActionCallback callback) {
        try {
            if (this.gatt == null) {
                Log.e(TAG, "Connect to device first");
                throw new Exception("Connect to device first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = this.gatt.getService(serviceUUID).getCharacteristic(uuid);
            if (chara == null) {
                onFail(-1, "BluetoothGattCharacteristic " + uuid + " is not exsit");
            } else if (!this.gatt.readCharacteristic(chara)) {
                onFail(-1, "gatt.readCharacteristic() return false");
            }
        } catch (Throwable tr) {
            Log.e(TAG, "readCharacteristic", tr);
            onFail(-1, tr.getMessage());
        }
    }

    public void readRssi(ActionCallback callback) {
        try {
            if (this.gatt == null) {
                Log.e(TAG, "Connect to device first");
                throw new Exception("Connect to device first");
            }
            this.currentCallback = callback;
            this.gatt.readRemoteRssi();
        } catch (Throwable tr) {
            Log.e(TAG, "readRssi", tr);
            onFail(-1, tr.getMessage());
        }
    }

    public void setNotifyListener(UUID serviceUUID, UUID characteristicId, NotifyListener listener) {
        if (this.gatt == null) {
            Log.e(TAG, "Connect to device first");
            return;
        }

        try {
            BluetoothGattCharacteristic chara = this.gatt.getService(serviceUUID).getCharacteristic(characteristicId);
            if (chara == null) {
                Log.e(TAG, "characteristicId " + characteristicId.toString() + " not found in service " + serviceUUID.toString());
                return;
            }
            this.gatt.setCharacteristicNotification(chara, true);
            BluetoothGattDescriptor descriptor = chara.getDescriptor(Profile.UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            this.gatt.writeDescriptor(descriptor);
            this.notifyListeners.put(characteristicId, listener);
        }catch(Exception e) { e.printStackTrace(); }
    }

    public boolean serviceExist(UUID serviceUUID) {
        try {
            if (this.gatt == null) {
                Log.e(TAG, "Connect to device first");
                throw new Exception("Connect to device first");
            } else if (this.gatt.getService(serviceUUID) != null) {
                return true;
            } else {
                return false;
            }
        } catch (Throwable tr) {
            Log.e(TAG, "serviceExist", tr);
            onFail(-1, tr.getMessage());
            return false;
        }
    }

    public boolean characteristicExist(UUID serviceUUID, UUID characteristicId) {
        try {
            if (this.gatt == null) {
                Log.e(TAG, "Connect to device first");
                throw new Exception("Connect to device first");
            } else if (this.gatt.getService(serviceUUID).getCharacteristic(characteristicId) != null) {
                return true;
            } else {
                return false;
            }
        } catch (Throwable tr) {
            Log.e(TAG, "characteristicExist", tr);
            onFail(-1, tr.getMessage());
            return false;
        }
    }

    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (newState == 2) {
            gatt.discoverServices();
        } else if (newState == 0) {
            gatt.close();
            if (this.disconnectedListener != null) {
                this.disconnectedListener.onNotify(null);
            }
        }
    }

    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (status == 0) {
            onSuccess(characteristic);
        } else {
            onFail(status, "onCharacteristicRead fail");
        }
    }

    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (status == 0) {
            onSuccess(characteristic);
        } else {
            onFail(status, "onCharacteristicWrite fail");
        }
    }

    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (status == 0) {
            onSuccess(Integer.valueOf(rssi));
        } else {
            onFail(status, "onCharacteristicRead fail");
        }
    }

    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (status == 0) {
            this.gatt = gatt;
            onSuccess(null);
            return;
        }
        onFail(status, "onServicesDiscovered fail");
    }

    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (this.notifyListeners.containsKey(characteristic.getUuid())) {
            ((NotifyListener) this.notifyListeners.get(characteristic.getUuid())).onNotify(characteristic.getValue());
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

    public static boolean setAliasName(BluetoothDevice device, String alias) {
        try {
            Method method = device.getClass().getMethod("setAlias", new Class[]{String.class});
            if (method == null) {
                return false;
            }
            method.invoke(device, new Object[]{alias});
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
            return false;
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
            return false;
        }
    }

    public static String getAliasName(BluetoothDevice device) {
        String deviceAlias = device.getName();
        try {
            Method method = device.getClass().getMethod("getAliasName", new Class[0]);
            if (method != null) {
                return (String) method.invoke(device, new Object[0]);
            }
            return deviceAlias;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return deviceAlias;
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
            return deviceAlias;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return deviceAlias;
        }
    }
}
