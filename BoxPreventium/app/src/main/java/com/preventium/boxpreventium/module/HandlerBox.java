package com.preventium.boxpreventium.module;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.preventium.boxpreventium.module.device.BluetoothBox;
import com.preventium.boxpreventium.module.enums.CONNEXION_STATE_t;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.module.trames.SensorSmoothAccelerometerInfo;
import com.preventium.boxpreventium.utils.Chrono;
import com.preventium.boxpreventium.utils.ThreadDefault;

import java.util.ArrayList;

/**
 * Created by Franck on 14/09/2016.
 */

public class HandlerBox extends ThreadDefault
    implements DiscoverBox.DiscoverBoxNotify{

    private final static boolean DEBUG = true;
    private final static String TAG = "HandlerBox";

    public interface NotifyListener {
        void onScanState(boolean scanning);
        void onDeviceState( String device_mac, boolean connected );
        void onNumberOfBox(int nb);
        void onForceChanged(double mG);
    }

    private Context context = null;
    private DiscoverBox discoverBox = null;
    private NotifyListener listener = null;

    private boolean scanning = false;
    private ArrayList<BluetoothDevice> proximityDevices = new ArrayList<BluetoothDevice>();
    private ArrayList<String> mMacList = new ArrayList<String>();
    private ArrayList<BluetoothBox> mBoxList = new ArrayList<BluetoothBox>();

    private boolean calibrate_1 = false;
    private boolean calibrate_2 = false;

    double last_force_mG = 0.0, curr_force_mG = 0.0;

    public HandlerBox(Context ctx, NotifyListener listener) {
        super(null);
        this.context = ctx;
        this.listener = listener;
        this.discoverBox = new DiscoverBox(context,this);
    }

    public boolean setActive( boolean enable ) {
        if( enable )
            return activate();
        desactivate();
        return true;
    }

    public boolean activate() {
        boolean ret = false;
        if( !isRunning()  ) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HandlerBox.this.run();
                }
            }).start();
            ret = true;
        }
        return ret;
    }

    public void desactivate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HandlerBox.this.setStop();
            }
        }).start();
    }

    public void on_constant_speed(){ calibrate_1 = true; }

    public void on_acceleration(){ calibrate_2 = true; }

    @Override
    public void onScanChanged(boolean scanning, ArrayList<BluetoothDevice> devices) {
        this.scanning = scanning;
        this.proximityDevices = devices;
        if( this.listener != null ) listener.onScanState( scanning );
        if( DEBUG ) Log.d(TAG, "on scan state changed: " + scanning );
    }

    @Override
    public void myRun() throws InterruptedException {
        super.myRun();

        Chrono chrono = new Chrono();
        chrono.start();
        discoverBox.scan();

        last_force_mG = curr_force_mG = 0.0;

        int nb = mBoxList.size();
        if( listener != null ) {
            listener.onNumberOfBox( nb );
            listener.onForceChanged( last_force_mG );
        }

        while( isRunning() ) {

            sleep(1000);

            // WHEN SCANNING
            if( scanning ) { // If scanning is in progress.
                chrono.start(); // Restart chrono who indicate the elapsed time since the last scan
            }

            curr_force_mG = 0.0;
            for( int i = mBoxList.size()-1; i >= 0; i-- ) {

                if (mBoxList.get(i).getConnectionState() == CONNEXION_STATE_t.DISCONNECTED) {
                    if( listener != null ) listener.onDeviceState( mMacList.get(i), false );
                    if( DEBUG ) Log.d(TAG,"LOST " + mMacList.get(i) );
                    mBoxList.remove(i);
                    mMacList.remove(i);

                } else {

                    SensorSmoothAccelerometerInfo smooth = mBoxList.get(i).getSmooth();
                    if( smooth != null ) {
                        if(  interval(0.0,smooth.value()) >= interval(0.0,curr_force_mG) ) {
                            curr_force_mG = smooth.value();
                        }
                    }
                }
            }
            //Log.d(TAG,"Force changed: " + last_force_mG + " mG -> " + curr_force_mG + " mG." );
            if( last_force_mG != curr_force_mG ) {
                last_force_mG = curr_force_mG;
                if( listener != null )
                    listener.onForceChanged( curr_force_mG );
                if( DEBUG ) Log.d(TAG,"Force changed: " + last_force_mG + " mG.");
            }

            // Calibration 'g' on constant speed
            if( calibrate_1 ) {
                if( DEBUG ) Log.d(TAG,"Calibrate triggered by constant speed.");
                for( int i = mBoxList.size()-1; i >= 0; i-- )
                    mBoxList.get(i).calibrate_if_constant_speed();
                calibrate_1 = false;
            }

            // Calibration 'o' on acceleration
            if( calibrate_2 ) {
                if( DEBUG ) Log.d(TAG,"Calibrate triggered by acceleration.");
                for( int i = mBoxList.size()-1; i >= 0; i-- )
                    mBoxList.get(i).calibrate_if_acceleration();
                calibrate_2 = false;
            }

            // WHEN NOT SCANNING
            if( !scanning ) {

                // If the result of the scan is not empty
                if( !proximityDevices.isEmpty() ) {
                    // Trying to connect to the BoxPreventium devices
                    for (BluetoothDevice device : proximityDevices) add( device );
                    proximityDevices.clear();
                } else {
                    // Wainting before rescan
                    if( (mBoxList.size() == 0 && chrono.getSeconds() > 15.0)
                            || (mBoxList.size() == 1 && chrono.getMinutes() > 1.0)
                            || (mBoxList.size() == 2 && chrono.getMinutes() > 3.0)
                            ) {
                        discoverBox.scan(); // Restart scanning
                    }
                }
            }

            // NUMBER OF CONNECTED DEVICE CHANGED
            if( nb != mBoxList.size() ) {
                nb = mBoxList.size();
                if( listener != null ) listener.onNumberOfBox( nb );
                if( DEBUG ) Log.d(TAG,"Number of connected device changed: " + nb );
            }
        }

        discoverBox.stop();

        for( int i = mBoxList.size()-1; i >= 0; i-- ) {
            if (mBoxList.get(i).getConnectionState() == CONNEXION_STATE_t.DISCONNECTED) {
                if( listener != null ) listener.onDeviceState( mMacList.get(i), false );
                if( DEBUG ) Log.d(TAG,"LOST " + mMacList.get(i) );
                mBoxList.remove(i);
                mMacList.remove(i);
            } else {
                mBoxList.get(i).close();
            }
        }
    }

    private void add( BluetoothDevice device ) {
        if( mBoxList.size() < 3 ) {
            BluetoothBox box = new BluetoothBox(context);
            if( listener != null ) listener.onDeviceState( device.getAddress(), true );
            if( DEBUG ) Log.d(TAG,"FIND " + device.getAddress() );
            mBoxList.add(box);
            mMacList.add(device.getAddress());
            box.connect(device);
        }
    }

    private double interval(double d1, double d2){
        double ret = d1 - d2;
        if( ret < 0.0 ) ret = -ret;
        return ret;
    }
}
