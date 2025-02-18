package com.preventium.boxpreventium.module;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import com.preventium.boxpreventium.utils.superclass.bluetooth.BluetoothScanner;
import com.preventium.boxpreventium.utils.superclass.bluetooth.device.BluetoothReceiver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Franck on 14/09/2016.
 */

public class DiscoverBox implements BluetoothReceiver.BluetoothCallback {

    private static final String TAG = "DiscoverBox";
    private static final boolean DEBUG = true;
    private static BluetoothReceiver bluetoothReceiver;
    private static boolean receiverActive = false;

    public interface DiscoverBoxNotify {
        void onScanChanged(boolean scanning, ArrayList<BluetoothDevice> devices);
        void nombreDiviceFound(int nombreDivice);//by francisco
//        void onScanState( boolean scanning );
//        void onDevicesResult( ArrayList<BluetoothDevice> devices );
    }

    private BluetoothScanner scanner = null;
    private ArrayList<BluetoothDevice> mBluetoothDevices = null;
    private ArrayList<Integer> mBluetoothRssi = null;
    private DiscoverBoxNotify notify = null;
    private IntentFilter filter;
    private WeakReference<Context> context;


    public void ScanIntent () {
        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        if( context.get() != null )
            context.get().registerReceiver(bluetoothReceiver, filter);
        receiverActive = true;
    }

    public DiscoverBox(Context ctx, DiscoverBoxNotify notify ){
        this.notify = notify;
        // this.scanner = new BluetoothScanner(ctx,this);
        this.context = new WeakReference<Context>(ctx);
        this.mBluetoothDevices = new ArrayList<>();
        this.mBluetoothRssi = new ArrayList<>();
        bluetoothReceiver = new BluetoothReceiver(this);
        this.ScanIntent();
    }

    public void scan(){
        // scanner.startLeScan();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if( !adapter.isEnabled() ) adapter.enable();
        adapter.startDiscovery();

        // reorder
        // this.onScanState(false);
    }

    public void stop(){
        /* scanner.stopLeScan(); */
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if( adapter.isEnabled() || adapter.isDiscovering() ) adapter.cancelDiscovery();
        // reorder
        this.onScanState(false);
    }

    public static void stopReceiver (Context context) {
        try {
            if (receiverActive) {
                context.unregisterReceiver(bluetoothReceiver);
                receiverActive = false;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

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
            while( !tab_en_ordre )
            {
                tab_en_ordre = true;
                if( mBluetoothDevices.size() > 1 ) {
                    for (int i = 0; i < taille - 1; i++) {
                        if (mBluetoothRssi.get(i) > mBluetoothRssi.get(i + 1)) {
                            int tmp_rssi = mBluetoothRssi.get(i);
                            mBluetoothRssi.set(i, mBluetoothRssi.get(i + 1));
                            mBluetoothRssi.set(i + 1, tmp_rssi);

                            BluetoothDevice tmp_device = mBluetoothDevices.get(i);
                            mBluetoothDevices.set(i, mBluetoothDevices.get(i + 1));
                            mBluetoothDevices.set(i + 1, tmp_device);

                            tab_en_ordre = false;
                        }
                    }
                }
                taille--;
            }

            if( DEBUG ) Log.d(TAG,"Scanning finish, " +  mBluetoothDevices.size() + " devices." );

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
            // Toast.makeText(this.context, "Box accepté: " + name, Toast.LENGTH_LONG).show();

            if( name != null ) {
                if( !name.startsWith("Preventium") || "Preventium" == name ) {
                    if( DEBUG ) Log.d(TAG, String.format( Locale.getDefault(), "Ignore device \"%s\" [%s] (%d).", name, device.getAddress(), rssi));
                } else {
                    if( mBluetoothDevices.contains(device) ) {
                        int position = mBluetoothDevices.indexOf( device );
                        mBluetoothRssi.set( position, rssi );
                        if( DEBUG ) Log.d(TAG, String.format( Locale.getDefault(), "Update device \"%s\" [%s] (%d) to proximity devices list.", name, device.getAddress(), rssi));
                    } else {
                        mBluetoothDevices.add( device );
                        mBluetoothRssi.add( rssi );
                        if( DEBUG ) Log.d(TAG,String.format( Locale.getDefault(), "Added device \"%s\" [%s] (%d) to proximity devices list.", name, device.getAddress(), rssi));
                    }
                }
            }

            // lance in state
            this.onScanState(false);
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
