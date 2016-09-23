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

/**
 * Created by Franck on 13/09/2016.
 */

public class BluetoothBoxIO extends BluetoothDev {
    private static final String TAG = "BluetoothBoxIO";

    public interface TramesNotifyListener {
        void onBatteryInfoReceived(BatteryInfo info);
        void onShockInfoReceived(SensorShockAccelerometerInfo info);
        void onSmoothInfoReceived(SensorSmoothAccelerometerInfo info);
    }

    public BluetoothBoxIO(Context context) {
        super(context);
    }

    public void firmware( final ActionCallback callback ){
        if( !exist( ProfileBox.UUID_SERVICE_FIRMWARE, ProfileBox.UUID_CHAR_FIRMWARE ) ){
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
            this.io.readCharacteristic(ProfileBox.UUID_SERVICE_FIRMWARE, ProfileBox.UUID_CHAR_FIRMWARE, ioCallback);
        }
    }

    public void command(final CmdBox cmd ) {
        Log.d( TAG, "Writing... " + cmd.toString() );
        this.io.writeCharacteristic(ProfileBox.UUID_SERVICE_SENSOR_DATA,
                ProfileBox.UUID_CHAR_CMD_DATA, cmd.toData(), new ActionCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        Log.d( TAG, "Write " + cmd.toString() + " success." );
                    }

                    @Override
                    public void onFail(int errorCode, String msg) {
                        Log.d( TAG, "Write " + cmd.toString()  + " fail! " + msg);
                    }
                });
    }

    public void setTramesListener(final TramesNotifyListener listener){
        this.io.setNotifyListener(ProfileBox.UUID_SERVICE_SENSOR_DATA,
                ProfileBox.UUID_CHAR_BATTERY, new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        if( listener != null )
                            listener.onBatteryInfoReceived( BatteryInfo.fromData(data) );
                    }
                });
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
        this.io.setNotifyListener(ProfileBox.UUID_SERVICE_SENSOR_DATA,
                ProfileBox.UUID_CHAR_SENSOR_ACC1_DATA, new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        if( listener != null )
                            listener.onSmoothInfoReceived( SensorSmoothAccelerometerInfo.fromData(data) );
                    }
                });
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
        this.io.setNotifyListener(ProfileBox.UUID_SERVICE_SENSOR_DATA,
                ProfileBox.UUID_CHAR_SENSOR_ACC2_DATA, new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        if( listener != null )
                            listener.onShockInfoReceived( SensorShockAccelerometerInfo.fromData(data) );
                    }
                });
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
    }
}
