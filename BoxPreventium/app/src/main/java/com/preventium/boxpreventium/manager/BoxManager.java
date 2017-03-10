package com.preventium.boxpreventium.manager;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.preventium.boxpreventium.enums.ENGINE_t;
import com.preventium.boxpreventium.module.DiscoverBox;
import com.preventium.boxpreventium.module.HandlerBox;
import com.preventium.boxpreventium.module.device.BluetoothBox;
import com.preventium.boxpreventium.module.enums.CONNEXION_STATE_t;
import com.preventium.boxpreventium.module.trames.BatteryInfo;
import com.preventium.boxpreventium.module.trames.SensorShockAccelerometerInfo;
import com.preventium.boxpreventium.module.trames.SensorSmoothAccelerometerInfo;
import com.preventium.boxpreventium.utils.Chrono;
import com.preventium.boxpreventium.utils.ThreadDefault;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Thread.sleep;

/**
 * Created by Franck on 21/11/2016.
 */


public class BoxManager
        implements DiscoverBox.DiscoverBoxNotify {

    private final static boolean DEBUG = true;
    private final static String TAG = "BoxManager";


    private Context _ctx = null;
    private DiscoverBox _discover = null;
    private HandlerBox.NotifyListener _listener = null;
    private boolean _scanning = false;
    private ArrayList<BluetoothDevice> _proximityDevices = new ArrayList<BluetoothDevice>();
    private ArrayList<BluetoothBox> mBoxList = new ArrayList<BluetoothBox>();

    private Pair<Double,Short> last_smooth; // mG, RAW
    private Pair<Double,Short> curr_smooth; // mG, RAW
    private Pair<Double,Short> last_shock; // mG, RAW
    private Pair<Double,Short> curr_shock; // mG, RAW

    private ENGINE_t last_engine_t = ENGINE_t.UNKNOW;
    private Chrono chrono = Chrono.newInstance();
    private boolean calibrate_1 = false;
    private boolean calibrate_2 = false;

    public BoxManager(Context ctx, HandlerBox.NotifyListener listener){
        this._ctx = ctx;
        this._listener = listener;
        this._discover = new DiscoverBox(ctx,this);
    }

    /// ============================================================================================
    /// ACTIVATION
    /// ============================================================================================

    private boolean _activate = false;

    public boolean setActive( boolean enable ) throws InterruptedException {
        if( enable ) return activate();
        return desactivate();
    }

    private boolean activate() throws InterruptedException {
        boolean ret = false;
        if( !_activate  ) {

            _activate = true;

            disconnectAll();

            curr_smooth = Pair.create(0.0, (short)0);
            last_smooth = Pair.create(0.0, (short)0);
            curr_shock = Pair.create(0.0, (short)0);
            last_shock = Pair.create(0.0, (short)0);
            last_engine_t = ENGINE_t.UNKNOW;

            int nb = mBoxList.size();
            if( _listener != null ) {
                _listener.onNumberOfBox( nb );
                _listener.onForceChanged( last_smooth, last_shock );
                _listener.onEngineStateChanged( last_engine_t );
            }

            _discover.scan();


            ret = true;
        }
        process();
        return ret;
    }

    private boolean desactivate() {
        disconnectAll();
        _activate = false;
        return true;
    }

    @Override
    public void onScanChanged(boolean scanning, ArrayList<BluetoothDevice> devices) {
        this._scanning = scanning;
        if( scanning ) chrono.stop(); else chrono.start();
        this._proximityDevices = devices;
        if( this._listener != null ) _listener.onScanState( scanning );
        if( DEBUG ) Log.d(TAG, "on scan state changed: " + scanning );
    }

    private void disconnectAll(){
        // DISCONNECT ALL DEVICE
        int nb = mBoxList.size();
        while( nb > 0 ) {
            for (int i = mBoxList.size() - 1; i >= 0; i--) {
                if (mBoxList.get(i).getConnectionState() == CONNEXION_STATE_t.DISCONNECTED) {
                    if (_listener != null)
                        _listener.onDeviceState(mBoxList.get(i).getMacAddr(), false);
                    if (DEBUG) Log.d(TAG, "LOST " + mBoxList.get(i).getMacAddr());
                    mBoxList.remove(i);
                } else {
                    mBoxList.get(i).close();
                }
            }
            nb = mBoxList.size();
            if( nb > 0 ) try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // NUMBER OF CONNECTED DEVICE CHANGED
            if( nb != mBoxList.size() ) {
                nb = mBoxList.size();
                if( _listener != null ) _listener.onNumberOfBox( nb );
                if( DEBUG ) Log.d(TAG,"Number of connected device changed: " + nb );
            }
        }
    }

    public void on_constant_speed(){ calibrate_1 = true; }

    public void on_acceleration(){ calibrate_2 = true; }

    public void process() throws InterruptedException {

        int nb = mBoxList.size();
        curr_smooth = Pair.create(0.0, (short)0);
        curr_shock = Pair.create(0.0, (short)0);

        for( int i = mBoxList.size()-1; i >= 0; i-- ) {

            if (mBoxList.get(i).getConnectionState() == CONNEXION_STATE_t.DISCONNECTED) {
                if( _listener != null ) _listener.onDeviceState( mBoxList.get(i).getMacAddr(), false );
                if( DEBUG ) Log.d(TAG,"LOST " + mBoxList.get(i).getMacAddr() );
                mBoxList.remove(i);
            } else {
                SensorSmoothAccelerometerInfo smooth = mBoxList.get(i).getSmooth();
                if( smooth != null ) {
                    if( interval(0.0,smooth.value()) >= interval(0.0,curr_smooth.first) ) {
                        curr_smooth = Pair.create(smooth.value(), smooth.value_raw());
                    }
                }

                SensorShockAccelerometerInfo shock = mBoxList.get(i).getShock();
                if( shock != null ) {
                    if( interval(0.0,shock.value()) >= interval(0.0,curr_shock.first) ) {
                        curr_shock = Pair.create(shock.value(), shock.value_raw());
                    }
                }

            }
        }

        boolean change = false;
        if( !Objects.equals(last_smooth, curr_smooth) ) {
            last_smooth = Pair.create(curr_smooth.first,curr_smooth.second);
            change = true;
        }
        if( !Objects.equals(last_shock, curr_shock) ) {
            last_shock = Pair.create(curr_shock.first,curr_shock.second);
            change = true;
        }
        if( change ) {
            if( _listener != null ) _listener.onForceChanged( curr_smooth, curr_shock );
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

        // Battery info
        if( !mBoxList.isEmpty() ) {
            ENGINE_t engine_t = ENGINE_t.UNKNOW;
            BatteryInfo bat = mBoxList.get(0).getBat();
            if( bat != null ) engine_t = (bat.running()) ? ENGINE_t.ON : ENGINE_t.OFF;
            if( _listener != null && engine_t != last_engine_t )
                _listener.onEngineStateChanged( engine_t );
            last_engine_t = engine_t;
        }

        // WHEN NOT SCANNING
        if( !_scanning ) {

            // If the result of the scan is not empty
            if( !_proximityDevices.isEmpty() ) {
                // Trying to connect to the BoxPreventium devices
                for( int i = 0; i < _proximityDevices.size(); i++ ){
                    add( _proximityDevices.get(i) );
                }
                _proximityDevices.clear();
                orderBox();
            } else {
                // Wainting before rescan
                if( (mBoxList.size() == 0 && chrono.getSeconds() > 15.0)
                        || (mBoxList.size() == 1 && chrono.getMinutes() > 1.0)
                        || (mBoxList.size() == 2 && chrono.getMinutes() > 3.0)
                        ) {
                    _discover.scan(); // Restart scanning
                }
            }
        }

        // NUMBER OF CONNECTED DEVICE CHANGED
        if( nb != mBoxList.size() ) {
            nb = mBoxList.size();
            if( _listener != null ) _listener.onNumberOfBox( nb );
            if( DEBUG ) Log.d(TAG,"Number of connected device changed: " + nb );
        }

    }

    private void add( BluetoothDevice device ) throws InterruptedException {
        if( mBoxList.size() < 3 ) {
            BluetoothBox box = new BluetoothBox(_ctx);
            if( _listener != null ) _listener.onDeviceState( device.getAddress(), true );
            if( DEBUG ) Log.d(TAG,"FIND " + device.getAddress() );
            box.connect(device);
            while( _activate && box.getConnectionState() == CONNEXION_STATE_t.CONNECTING ) sleep(50);
            if( box.getConnectionState() == CONNEXION_STATE_t.CONNECTED ) {
                mBoxList.add(box);
                if( DEBUG ) Log.d(TAG,"ADDED " + device.getAddress() );
            }
        }
    }

    private void orderBox() throws InterruptedException {

        if( mBoxList.size() > 1 ){

            // Update RSSI if needed
            boolean rssi_ok = false;
            while ( _activate && !rssi_ok ){
                sleep(1000);
                rssi_ok = true;
                for(int i=0 ; i < mBoxList.size() ; i++){
                    if( mBoxList.get(i).getRSSI() == null ){
                        rssi_ok = false;
                        mBoxList.get(i).readRSSI();
                    }
                }
            }

            // Order device
            if( _activate ) {
                // ORDER RSSI BY asc ...
                boolean tab_en_ordre = false;
                int taille = mBoxList.size();
                while (!tab_en_ordre) {
                    tab_en_ordre = true;
                    for (int i = 0; i < taille - 1; i++) {
                        if (mBoxList.get(i).getRSSI() < mBoxList.get(i + 1).getRSSI()) {
                            BluetoothBox tmp_device = mBoxList.get(i);
                            mBoxList.set(i, mBoxList.get(i + 1));
                            mBoxList.set(i + 1, tmp_device);
                            tab_en_ordre = false;
                        }
                    }
                    taille--;
                }
            }
        }

//        for ( BluetoothBox b : mBoxList ) {
//            Log.d("AAAAA","MAC: " + b.getMacAddr() + " RSSI: " + b.getRSSI() );
//        }
    }

    private double interval(double d1, double d2){
        double ret = d1 - d2;
        if( ret < 0.0 ) ret = -ret;
        return ret;
    }
}

