package com.ikalogic.franck.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;

import com.ikalogic.franck.bluetooth.scanner.BluetoothLeScannerCompat;
import com.ikalogic.franck.bluetooth.scanner.ScanCallbackCompat;
import com.ikalogic.franck.bluetooth.scanner.ScanFilterCompat;
import com.ikalogic.franck.bluetooth.scanner.ScanResultCompat;
import com.ikalogic.franck.bluetooth.scanner.ScanSettingsCompat;
import com.ikalogic.franck.bluetooth.scanner.ScannerCallback;

import java.util.List;

/**
 * Created by franck on 6/18/16.
 */
public class BluetoothScanner extends ScanCallbackCompat {

    private static final String TAG = "BluetoothScanner";
    public static final long SCAN_PERIOD = 40000;// Stops scanning after 40 seconds.
    private long scan_period = SCAN_PERIOD;
    private ScannerCallback scannerCallback;
    private Handler handler;
    private Context context;

    public BluetoothScanner(Context context, ScannerCallback callback) {
        this.context = context;
        scannerCallback = callback;
        handler = new Handler();
    }

    public void startLeScan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if( null == adapter ) {
            Log.w(TAG,"Bluetooth adapter is null");
            if( scannerCallback != null ) scannerCallback.onBluetoothAdapterIsNull();
            return;
        }
        if( !adapter.isEnabled() ){
            Log.d(TAG,"Bluetooth adapter is disable");
            if( scannerCallback != null ) scannerCallback.onBluetoothAdapterIsDisable();
            return;
        }

        final int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.M) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                Log.d(TAG,"Location service is disable!");
                if( scannerCallback != null ) scannerCallback.onLocationServiceIsDisable();
                return;
            }
        }

        // Stops scanning after a pre-defined scan period.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() { stopLeScan(); }
        }, scan_period);
        // Start scanning
        BluetoothLeScannerCompat.startScan(adapter,this);
        if( scannerCallback != null ) scannerCallback.onScanState(true);
    }

    public void startLeScan(List<ScanFilterCompat> filters, ScanSettingsCompat settings ) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if( null == adapter ) {
            Log.w(TAG,"Bluetooth adapter is null");
            if( scannerCallback != null ) scannerCallback.onBluetoothAdapterIsNull();
            return;
        }
        if( !adapter.isEnabled() ){
            Log.d(TAG,"Bluetooth adapter is disable");
            if( scannerCallback != null ) scannerCallback.onBluetoothAdapterIsDisable();
            return;
        }

        final int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.M) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                Log.d(TAG,"Location service is disable!");
                if( scannerCallback != null ) scannerCallback.onLocationServiceIsDisable();
                return;
            }
        }

        // Stops scanning after a pre-defined scan period.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() { stopLeScan(); }
        }, scan_period);
        // Start scanning
        BluetoothLeScannerCompat.startScan(adapter, filters, settings ,this);
        if( scannerCallback != null ) scannerCallback.onScanState(true);
    }

    public void stopLeScan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if( null == adapter ) {
            Log.w(TAG,"Bluetooth adapter is null");
            if( scannerCallback != null ) scannerCallback.onBluetoothAdapterIsNull();
            return;
        }
        BluetoothLeScannerCompat.stopScan(adapter,this);
        if( scannerCallback != null ) scannerCallback.onScanState(false);
    }

    @Override
    public void onScanResult(int callbackType, ScanResultCompat result) {
        super.onScanResult(callbackType, result);
        if( scannerCallback != null )
            scannerCallback.onScanResult( result.getDevice(), result.getRssi() );
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
    }

    @Override
    public void onBatchScanResults(List<ScanResultCompat> results) {
        super.onBatchScanResults(results);
    }

    public static boolean hasBluetooth(){
        boolean hasBluetooth = false;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if( null != adapter ) {
            hasBluetooth = true;
        }
        return hasBluetooth;
    }

    public static boolean hasBluetoothLE(Context context){
        PackageManager pm = context.getPackageManager();
        boolean hasBLE = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        return hasBLE;
    }

}


