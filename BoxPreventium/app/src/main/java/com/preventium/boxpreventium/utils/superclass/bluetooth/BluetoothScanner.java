package com.preventium.boxpreventium.utils.superclass.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build.VERSION;
import android.os.Handler;
import android.util.Log;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.preventium.boxpreventium.utils.superclass.bluetooth.scanner.BluetoothLeScannerCompat;
import com.preventium.boxpreventium.utils.superclass.bluetooth.scanner.ScanCallbackCompat;
import com.preventium.boxpreventium.utils.superclass.bluetooth.scanner.ScanFilterCompat;
import com.preventium.boxpreventium.utils.superclass.bluetooth.scanner.ScanResultCompat;
import com.preventium.boxpreventium.utils.superclass.bluetooth.scanner.ScanSettingsCompat;
import com.preventium.boxpreventium.utils.superclass.bluetooth.scanner.ScannerCallback;
import java.util.List;

public class BluetoothScanner extends ScanCallbackCompat {
    public static final long SCAN_PERIOD = 40000;
    private static final String TAG = "BluetoothScanner";
    private Context context;
    private Handler handler;
    private long scan_period = SCAN_PERIOD;
    private ScannerCallback scannerCallback;

    class C01341 implements Runnable {
        C01341() {
        }

        public void run() {
            BluetoothScanner.this.stopLeScan();
        }
    }

    class C01352 implements Runnable {
        C01352() {
        }

        public void run() {
            BluetoothScanner.this.stopLeScan();
        }
    }

    public BluetoothScanner(Context context, ScannerCallback callback) {
        this.context = context;
        this.scannerCallback = callback;
        this.handler = new Handler();
    }

    @SuppressLint("WrongConstant")
    public void startLeScan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.w(TAG, "Bluetooth adapter is null");
            if (this.scannerCallback != null) {
                this.scannerCallback.onBluetoothAdapterIsNull();
            }
        } else if (!adapter.isEnabled()) {
            Log.d(TAG, "Bluetooth adapter is disable");
            if (this.scannerCallback != null) {
                this.scannerCallback.onBluetoothAdapterIsDisable();
            }
        } else if (VERSION.SDK_INT < 23 || ((LocationManager) this.context.getSystemService(Param.LOCATION)).isProviderEnabled("gps")) {
            this.handler.postDelayed(new C01341(), this.scan_period);
            BluetoothLeScannerCompat.startScan(adapter, this);
            if (this.scannerCallback != null) {
                this.scannerCallback.onScanState(true);
            }
        } else {
            Log.d(TAG, "Location service is disable!");
            if (this.scannerCallback != null) {
                this.scannerCallback.onLocationServiceIsDisable();
            }
        }
    }

    public void startLeScan(List<ScanFilterCompat> filters, ScanSettingsCompat settings) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.w(TAG, "Bluetooth adapter is null");
            if (this.scannerCallback != null) {
                this.scannerCallback.onBluetoothAdapterIsNull();
            }
        } else if (!adapter.isEnabled()) {
            Log.d(TAG, "Bluetooth adapter is disable");
            if (this.scannerCallback != null) {
                this.scannerCallback.onBluetoothAdapterIsDisable();
            }
        } else if (VERSION.SDK_INT < 23 || ((LocationManager) this.context.getSystemService(Param.LOCATION)).isProviderEnabled("gps")) {
            this.handler.postDelayed(new C01352(), this.scan_period);
            BluetoothLeScannerCompat.startScan(adapter, filters, settings, this);
            if (this.scannerCallback != null) {
                this.scannerCallback.onScanState(true);
            }
        } else {
            Log.d(TAG, "Location service is disable!");
            if (this.scannerCallback != null) {
                this.scannerCallback.onLocationServiceIsDisable();
            }
        }
    }

    public void stopLeScan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.w(TAG, "Bluetooth adapter is null");
            if (this.scannerCallback != null) {
                this.scannerCallback.onBluetoothAdapterIsNull();
                return;
            }
            return;
        }
        BluetoothLeScannerCompat.stopScan(adapter, this);
        if (this.scannerCallback != null) {
            this.scannerCallback.onScanState(false);
        }
    }

    public void onScanResult(int callbackType, ScanResultCompat result) {
        super.onScanResult(callbackType, result);
        if (this.scannerCallback != null) {
            this.scannerCallback.onScanResult(result.getDevice(), result.getRssi());
        }
    }

    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
    }

    public void onBatchScanResults(List<ScanResultCompat> results) {
        super.onBatchScanResults(results);
    }

    public static boolean hasBluetooth() {
        if (BluetoothAdapter.getDefaultAdapter() != null) {
            return true;
        }
        return false;
    }

    public static boolean hasBluetoothLE(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }
}
