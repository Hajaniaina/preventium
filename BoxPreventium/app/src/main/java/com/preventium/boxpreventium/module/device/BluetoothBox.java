package com.preventium.boxpreventium.module.device;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.preventium.boxpreventium.module.enums.CMD_t;
import com.preventium.boxpreventium.module.enums.CONNEXION_STATE_t;
import com.preventium.boxpreventium.module.trames.BatteryInfo;
import com.preventium.boxpreventium.module.trames.SensorShockAccelerometerInfo;
import com.preventium.boxpreventium.module.trames.SensorSmoothAccelerometerInfo;
import com.preventium.boxpreventium.utils.ThreadDefault;
import com.preventium.boxpreventium.utils.superclass.bluetooth.device.ActionCallback;
import com.preventium.boxpreventium.utils.superclass.bluetooth.device.NotifyListener;

/**
 * Created by Franck on 14/09/2016.
 */

public class BluetoothBox
        implements ActionCallback, BluetoothBoxIO.TramesNotifyListener, NotifyListener {

    private final static String TAG = "BluetoothBox";
    private final static boolean DEBUG = false;
    private BatteryInfo last_battery_info = null;
    private SensorSmoothAccelerometerInfo last_smooth_info = null;
    private SensorShockAccelerometerInfo last_shock_info = null;

    private CONNEXION_STATE_t state = CONNEXION_STATE_t.DISCONNECTED;

    private BluetoothBoxIO io = null;

    public BluetoothBox(Context context) {
        io = new BluetoothBoxIO(context);
        io.setDisconnectedListener( this );
    }

    public void connect(BluetoothDevice device){
        updateState( CONNEXION_STATE_t.CONNECTING );
        io.connect( device, this );
    }

    public void connect(String address){
        updateState( CONNEXION_STATE_t.CONNECTING );
        io.connect( address, this );
    }

    public void calibrate_if_constant_speed(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                io.command(CmdBox.newInstance(CMD_t.FIRST_CALIBRATION));
            }
        }).start();
    }

    public void calibrate_if_acceleration(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                io.command(CmdBox.newInstance(CMD_t.SECOND_CALIBRATION));
            }
        }).start();
    }

    public CONNEXION_STATE_t getConnectionState(){ return state; }

    public BatteryInfo getBat(){ return last_battery_info; };

    public SensorSmoothAccelerometerInfo getSmooth(){ return last_smooth_info; };

    public SensorShockAccelerometerInfo getShock(){ return last_shock_info; };
    // CONNECTED

    @Override
    public void onSuccess(Object data) {
        updateState( CONNEXION_STATE_t.CONNECTED );
        new Thread(new Runnable() {
            @Override
            public void run() {
                io.setTramesListener( BluetoothBox.this );
                io.command(CmdBox.newInstance(CMD_t.START_MEASURING));
            }
        }).start();
    }

    @Override
    public void onFail(int errorCode, String msg) {
        updateState( CONNEXION_STATE_t.DISCONNECTED );
    }

    // READ

    @Override
    public void onBatteryInfoReceived(BatteryInfo info) {
        if( DEBUG && info != null ) Log.d(TAG,info.toString());
        last_battery_info = info;
    }

    @Override
    public void onShockInfoReceived(SensorShockAccelerometerInfo info) {
        if( DEBUG && info != null ) Log.d(TAG,info.toString());
        last_shock_info = info;
    }

    @Override
    public void onSmoothInfoReceived(SensorSmoothAccelerometerInfo info) {
        if( DEBUG && info != null ) Log.d(TAG,info.toString());
        last_smooth_info = info;
    }

    // DISCONNECTED

    @Override
    public void onNotify(byte[] data) {
        updateState( CONNEXION_STATE_t.DISCONNECTED );
    }

    // STATUS CHANGED

    private void updateState( CONNEXION_STATE_t state ) {
        this.state = state;
        Log.d(TAG,"State changed: " + state );
    }

}
