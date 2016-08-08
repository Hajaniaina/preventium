package com.preventium.boxpreventium.module;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.preventium.boxpreventium.bluetooth.BluetoothDev;
import com.preventium.boxpreventium.bluetooth.device.ActionCallback;
import com.preventium.boxpreventium.bluetooth.device.NotifyListener;
import com.preventium.boxpreventium.module.trame.BatteryInfo;
import com.preventium.boxpreventium.module.trame.SensorShockAccelerometerInfo;
import com.preventium.boxpreventium.module.trame.SensorSmoothAccelerometerInfo;

import java.util.Arrays;

/**
 * Created by Franck on 08/08/2016.
 */

public class BluetoothModule extends BluetoothDev {

    private static final String TAG = "BluetoothModule";

    public BluetoothModule( Context context ) { super(context); }

    public void firmware( final ActionCallback callback ){
        if( !exist( Profile.UUID_SERVICE_FIRMWARE, Profile.UUID_CHAR_FIRMWARE ) ){
            Log.i( TAG, "Firmware not exist, return 0.0" );
            if( callback != null ) callback.onSuccess( 0.0f );
        } else {
            ActionCallback ioCallback = new ActionCallback() {
                @Override
                public void onSuccess(Object data) {
                    BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                    Log.i( TAG, "Firmware result " + Arrays.toString( characteristic.getValue()) );
                    byte[] bytes = characteristic.getValue();
                    if ( bytes.length > 0 ) {
                        String fwm_read = new String( bytes );
                        float fwm = Float.parseFloat( fwm_read );
                        if( callback != null ) callback.onSuccess( fwm );
                    } else {
                        if( callback != null ) callback.onFail(-1, "result format wrong!");
                    }
                }
                @Override
                public void onFail(int errorCode, String msg) {
                    if( callback != null ) callback.onFail(errorCode, msg);
                }
            };
            this.io.readCharacteristic(Profile.UUID_SERVICE_FIRMWARE, Profile.UUID_CHAR_FIRMWARE, ioCallback);
        }
    }

    public void command( CommandData cmd ) {
        Log.d( TAG, "Writing... " + cmd.toString() );
        this.io.writeCharacteristic( Profile.UUID_SERVICE_SENSOR_DATA,
                Profile.UUID_CHAR_CMD_DATA, cmd.toData(), null );
    }

    public void setTramesListener( final TrameNotifyListener listener ) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_SENSOR_DATA,
                Profile.UUID_CHAR_SENSOR_ACC1_DATA, new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        if( listener != null ) {
                            SensorSmoothAccelerometerInfo info
                                    = SensorSmoothAccelerometerInfo.fromData(data);
                            listener.onSensorSmoothAccelerometerInfoNotify( info );
                        }
                    }
                });
        try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
        this.io.setNotifyListener(Profile.UUID_SERVICE_SENSOR_DATA,
                Profile.UUID_CHAR_SENSOR_ACC2_DATA, new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        if( listener != null ) {
                            SensorShockAccelerometerInfo info
                                    = SensorShockAccelerometerInfo.fromData(data);
                            listener.onSensorShockAccelerometerInfoNotify( info );
                        }
                    }
                });
        try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
        this.io.setNotifyListener(Profile.UUID_SERVICE_SENSOR_DATA,
                Profile.UUID_CHAR_BATTERY, new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        if( listener != null ) {
                            BatteryInfo info = BatteryInfo.fromData(data);
                            listener.onBatteryInfoNotify( info );
                        }
                    }
                });
    }

    public interface TrameNotifyListener {
        void onSensorSmoothAccelerometerInfoNotify(SensorSmoothAccelerometerInfo info);
        void onSensorShockAccelerometerInfoNotify(SensorShockAccelerometerInfo info);
        void onBatteryInfoNotify(BatteryInfo info);
    }
}

