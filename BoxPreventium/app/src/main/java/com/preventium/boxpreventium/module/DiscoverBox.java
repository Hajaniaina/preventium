package com.preventium.boxpreventium.module;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.preventium.boxpreventium.utils.superclass.bluetooth.BluetoothScanner;
import com.preventium.boxpreventium.utils.superclass.bluetooth.scanner.ScannerCallback;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Franck on 14/09/2016.
 */

public class DiscoverBox implements ScannerCallback {

    private static final String TAG = "DiscoverBox";
    private static final boolean DEBUG = true;

    public interface DiscoverBoxNotify {
        void onScanChanged(boolean scanning, ArrayList<BluetoothDevice> devices);
//        void onScanState( boolean scanning );
//        void onDevicesResult( ArrayList<BluetoothDevice> devices );
    }

    private BluetoothScanner scanner = null;
    private ArrayList<BluetoothDevice> mBluetoothDevices = null;
    private ArrayList<Integer> mBluetoothRssi = null;
    private DiscoverBoxNotify notify = null;

    public DiscoverBox(Context ctx, DiscoverBoxNotify notify ){
        this.notify = notify;
        this.scanner = new BluetoothScanner(ctx,this);
        this.mBluetoothDevices = new ArrayList<>();
        this.mBluetoothRssi = new ArrayList<>();
    }

    public void scan(){ scanner.startLeScan(); }

    public void stop(){ scanner.stopLeScan(); }

    @Override
    public void onScanState(boolean scanning) {
        if( scanning ) {
            if( DEBUG ) Log.d(TAG,"Scanning... ");
            mBluetoothDevices.clear();
            mBluetoothRssi.clear();
        } else {

            // ORDER RSSI BY DESC ...
            boolean tab_en_ordre = false;
            int taille = mBluetoothRssi.size();
            while(!tab_en_ordre)
            {
                tab_en_ordre = true;
                for(int i=0 ; i < taille-1 ; i++)
                {
                    if(mBluetoothRssi.get(i) > mBluetoothRssi.get(i+1))
                    {
                        int tmp_rssi = mBluetoothRssi.get(i);
                        mBluetoothRssi.set(i, mBluetoothRssi.get(i+1) );
                        mBluetoothRssi.set(i+1, tmp_rssi );

                        BluetoothDevice tmp_device = mBluetoothDevices.get(i);
                        mBluetoothDevices.set(i, mBluetoothDevices.get(i+1) );
                        mBluetoothDevices.set(i+1, tmp_device );

                        tab_en_ordre = false;
                    }
                }
                taille--;
            }

            if( DEBUG ) Log.d(TAG,"Scanning finish," +  mBluetoothDevices.size() + " devices." );
            for( int i = 0; i < mBluetoothDevices.size() && i < mBluetoothRssi.size(); i++ ) {
                Log.d(TAG,String.format( Locale.getDefault(), "--> \"%s\" [%s] (%d)",
                        mBluetoothDevices.get(i).getName(),
                        mBluetoothDevices.get(i).getAddress(),
                        mBluetoothRssi.get(i)));
            }

        }
        if( notify != null ) notify.onScanChanged( scanning, mBluetoothDevices);
    }

    @Override
    public void onScanResult(BluetoothDevice device, int rssi) {
        if( device != null ) {
            String name = device.getName();
            if( name != null ) {
                if( !name.startsWith("Preventium") ) {
                    if( DEBUG ) Log.d(TAG,String.format( Locale.getDefault(), "Ignore device \"%s\" [%s] (%d).",name,device.getAddress(),rssi));
                } else {
                    if( mBluetoothDevices.contains(device) ) {
                        int position = mBluetoothDevices.indexOf( device );
                        mBluetoothRssi.set( position, rssi );
                        if( DEBUG ) Log.d(TAG,String.format( Locale.getDefault(), "Update device \"%s\" [%s] (%d) to proximity devices list.",name,device.getAddress(),rssi));
                    } else {
                        mBluetoothDevices.add( device );
                        mBluetoothRssi.add( rssi );
                        if( DEBUG ) Log.d(TAG,String.format( Locale.getDefault(), "Added device \"%s\" [%s] (%d) to proximity devices list.",name,device.getAddress(),rssi));
                    }
                }
            }
        }
    }

    @Override
    public void onBluetoothAdapterIsDisable() {
        Log.w(TAG,"Bluetooth adapter is disable!");
    }

    @Override
    public void onBluetoothAdapterIsNull() {
        Log.w(TAG,"Bluetooth adpter is null!");
    }

    @Override
    public void onLocationServiceIsDisable() {
        Log.w(TAG,"Location service is disable!");
    }

}
