package com.preventium.boxpreventium.module.device;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;
import com.preventium.boxpreventium.module.trames.BatteryInfo;
import com.preventium.boxpreventium.module.trames.SensorShockAccelerometerInfo;
import com.preventium.boxpreventium.module.trames.SensorSmoothAccelerometerInfo;
import com.preventium.boxpreventium.utils.superclass.bluetooth.BluetoothDev;
import com.preventium.boxpreventium.utils.superclass.bluetooth.device.ActionCallback;
import com.preventium.boxpreventium.utils.superclass.bluetooth.device.NotifyListener;
import java.util.Arrays;

public class BluetoothBoxIO extends BluetoothDev {
    private static final String TAG = "BluetoothBoxIO";

    public interface TramesNotifyListener {
        void onBatteryInfoReceived(BatteryInfo batteryInfo);

        void onShockInfoReceived(SensorShockAccelerometerInfo sensorShockAccelerometerInfo);

        void onSmoothInfoReceived(SensorSmoothAccelerometerInfo sensorSmoothAccelerometerInfo);
    }

    public BluetoothBoxIO(Context context) {
        super(context);
    }

    public void firmware(final ActionCallback callback) {
        if (exist(ProfileBox.UUID_SERVICE_FIRMWARE, ProfileBox.UUID_CHAR_FIRMWARE)) {
            this.io.readCharacteristic(ProfileBox.UUID_SERVICE_FIRMWARE, ProfileBox.UUID_CHAR_FIRMWARE, new ActionCallback() {
                public void onSuccess(Object data) {
                    BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                    Log.i(BluetoothBoxIO.TAG, "Firmware result " + Arrays.toString(characteristic.getValue()));
                    byte[] bytes = characteristic.getValue();
                    if (bytes.length > 0) {
                        float fwm = Float.parseFloat(new String(bytes));
                        if (callback != null) {
                            callback.onSuccess(Float.valueOf(fwm));
                        }
                    } else if (callback != null) {
                        callback.onFail(-1, "result format wrong!");
                    }
                }

                public void onFail(int errorCode, String msg) {
                    if (callback != null) {
                        callback.onFail(errorCode, msg);
                    }
                }
            });
            return;
        }
        Log.i(TAG, "Firmware not exist, return 0.0");
        if (callback != null) {
            callback.onSuccess(Float.valueOf(0.0f));
        }
    }

    public void command(final CmdBox cmd) {
        Log.d(TAG, "Writing... " + cmd.toString());
        this.io.writeCharacteristic(ProfileBox.UUID_SERVICE_SENSOR_DATA, ProfileBox.UUID_CHAR_CMD_DATA, cmd.toData(), new ActionCallback() {
            public void onSuccess(Object data) {
                Log.d(BluetoothBoxIO.TAG, "Write " + cmd.toString() + " success.");
            }

            public void onFail(int errorCode, String msg) {
                Log.d(BluetoothBoxIO.TAG, "Write " + cmd.toString() + " fail! " + msg);
            }
        });
    }

    public void setTramesListener(final TramesNotifyListener listener) {
        this.io.setNotifyListener(ProfileBox.UUID_SERVICE_SENSOR_DATA, ProfileBox.UUID_CHAR_BATTERY, new NotifyListener() {
            public void onNotify(byte[] data) {
                if (listener != null) {
                    listener.onBatteryInfoReceived(BatteryInfo.fromData(data));
                }
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.io.setNotifyListener(ProfileBox.UUID_SERVICE_SENSOR_DATA, ProfileBox.UUID_CHAR_SENSOR_ACC1_DATA, new NotifyListener() {
            public void onNotify(byte[] data) {
                if (listener != null) {
                    listener.onSmoothInfoReceived(SensorSmoothAccelerometerInfo.fromData(data));
                }
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        this.io.setNotifyListener(ProfileBox.UUID_SERVICE_SENSOR_DATA, ProfileBox.UUID_CHAR_SENSOR_ACC2_DATA, new NotifyListener() {
            public void onNotify(byte[] data) {
                if (listener != null) {
                    listener.onShockInfoReceived(SensorShockAccelerometerInfo.fromData(data));
                }
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e22) {
            e22.printStackTrace();
        }
    }
}
