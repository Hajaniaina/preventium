package com.preventium.boxpreventium.utils.superclass.bluetooth.scanner;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.preventium.boxpreventium.utils.superclass.bluetooth.BluetoothScanner;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class BluetoothLeScannerCompat {
    static final BluetoothLeScannerCompatImpl IMPL;
    private static final String TAG = "BTLeScannerCompat";

    BluetoothAdapter adapter;

    interface BluetoothLeScannerCompatImpl {
        void flushPendingScanResults(BluetoothAdapter bluetoothAdapter, ScanCallbackCompat scanCallbackCompat);

        void startScan(BluetoothAdapter bluetoothAdapter, ScanCallbackCompat scanCallbackCompat);

        void startScan(BluetoothAdapter bluetoothAdapter, List<ScanFilterCompat> list, ScanSettingsCompat scanSettingsCompat, ScanCallbackCompat scanCallbackCompat);

        void stopScan(BluetoothAdapter bluetoothAdapter, ScanCallbackCompat scanCallbackCompat);
    }

    @TargetApi(18)
    static class API18BluetoothLeScannerCompatImpl implements BluetoothLeScannerCompatImpl {
        static final SimpleArrayMap<ScanCallbackCompat, API18ScanCallback> callbackMap = new SimpleArrayMap();

        API18BluetoothLeScannerCompatImpl() {
        }

        public void flushPendingScanResults(BluetoothAdapter adapter, ScanCallbackCompat callbackCompat) {
        }

        public void startScan(BluetoothAdapter adapter, List<ScanFilterCompat> filters, ScanSettingsCompat settings, ScanCallbackCompat callbackCompat) {
            adapter.startLeScan(registerCallback(filters, callbackCompat));
        }

        public void startScan(BluetoothAdapter adapter, ScanCallbackCompat callbackCompat) {
            adapter.startLeScan(registerCallback(null, callbackCompat));
        }

        public void stopScan(BluetoothAdapter adapter, ScanCallbackCompat callbackCompat) {
            API18ScanCallback callback = (API18ScanCallback) callbackMap.remove(callbackCompat);
            if (callback != null) {
                adapter.stopLeScan(callback);
            }
        }

        private API18ScanCallback registerCallback(List<ScanFilterCompat> filters, ScanCallbackCompat callbackCompat) {
            API18ScanCallback result = (API18ScanCallback) callbackMap.get(callbackCompat);
            if (result != null) {
                return result;
            }
            result = new API18ScanCallback(filters, callbackCompat);
            callbackMap.put(callbackCompat, result);
            return result;
        }
    }

    static class API18ScanCallback implements LeScanCallback {
        private final WeakReference<ScanCallbackCompat> callbackCompatRef;
        private final List<ScanFilterCompat> filters;

        API18ScanCallback(List<ScanFilterCompat> filters, ScanCallbackCompat callbackCompat) {
            this.filters = filters;
            this.callbackCompatRef = new WeakReference(callbackCompat);
        }

        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            ScanCallbackCompat callbackCompat = (ScanCallbackCompat) this.callbackCompatRef.get();
            if (callbackCompat != null) {
                ScanResultCompat result = new ScanResultCompat(device, ScanRecordCompat.parseFromBytes(scanRecord), rssi, System.nanoTime());
                if (this.filters == null) {
                    callbackCompat.onScanResult(1, result);
                    return;
                }
                for (ScanFilterCompat filter : this.filters) {
                    if (filter.matches(result)) {
                        callbackCompat.onScanResult(1, result);
                        return;
                    }
                }
            }
        }
    }

    @TargetApi(21)
    static class API21BluetoothLeScannerCompatImpl implements BluetoothLeScannerCompatImpl {
        static final SimpleArrayMap<ScanCallbackCompat, API21ScanCallback> callbackMap = new SimpleArrayMap();
        private BroadcastReceiver broadcastReceiver;

        API21BluetoothLeScannerCompatImpl() {
        }

        public void flushPendingScanResults(BluetoothAdapter adapter, ScanCallbackCompat callbackCompat) {
            API21ScanCallback result = (API21ScanCallback) callbackMap.get(callbackCompat);
            if (result != null) {
                adapter.getBluetoothLeScanner().flushPendingScanResults(result);
            }
        }

        public void startScan(BluetoothAdapter adapter, List<ScanFilterCompat> filters, ScanSettingsCompat settings, ScanCallbackCompat callbackCompat) {
            List<ScanFilter> scanFilters = null;
            if (filters != null) {
                scanFilters = new ArrayList(filters.size());
                for (ScanFilterCompat filter : filters) {
                    scanFilters.add(filter.toApi21());
                }
            }
            if (settings == null) {
                throw new IllegalStateException("Scan settings are null");
            }

            adapter.getBluetoothLeScanner().startScan(scanFilters, settings.toApi21(), registerCallback(callbackCompat));
        }

        private boolean receiverIsRunAlready = false;
        public void startScan(BluetoothAdapter adapter, ScanCallbackCompat callbackCompat) {

            Log.w("StartScan", "it run");
            Log.v("StartScan", "it run");
            System.out.println("it run startScan");

            if( !receiverIsRunAlready ) {
                ((BluetoothScanner) callbackCompat).context.registerReceiver(registerBroadCastReceiver(adapter, callbackCompat), new IntentFilter(BluetoothDevice.ACTION_FOUND));
                adapter.startDiscovery();
                receiverIsRunAlready = true;
            }
        }

        public void stopScan(BluetoothAdapter adapter, ScanCallbackCompat callbackCompat) {
            API21ScanCallback result = (API21ScanCallback) callbackMap.remove(callbackCompat);
            if (result != null) {
                adapter.getBluetoothLeScanner().stopScan(result);
                if( broadcastReceiver != null ) {
                    ((BluetoothScanner) callbackCompat).context.unregisterReceiver(broadcastReceiver);
                    adapter.cancelDiscovery();
                }
            }
        }

        private BroadcastReceiver registerBroadCastReceiver(final BluetoothAdapter adapter, final ScanCallbackCompat callbackCompat) {
            return new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_ON:
                            adapter.getBluetoothLeScanner().startScan(registerCallback(callbackCompat));
                            break;
                    }
                }
            };
        }

        private API21ScanCallback registerCallback(ScanCallbackCompat callbackCompat) {
            API21ScanCallback result = (API21ScanCallback) callbackMap.get(callbackCompat);
            if (result != null) {
                return result;
            }
            result = new API21ScanCallback(callbackCompat);
            callbackMap.put(callbackCompat, result);
            return result;
        }


    }

    @TargetApi(21)
    static class API21ScanCallback extends ScanCallback {
        private final WeakReference<ScanCallbackCompat> callbackCompatRef;

        API21ScanCallback(ScanCallbackCompat callbackCompat) {
            this.callbackCompatRef = new WeakReference(callbackCompat);
        }

        public void onScanResult(int callbackType, ScanResult result) {
            ScanCallbackCompat callbackCompat = (ScanCallbackCompat) this.callbackCompatRef.get();
            if (callbackCompat != null) {
                callbackCompat.onScanResult(callbackType, new ScanResultCompat(result));
            }
        }

        public void onBatchScanResults(List<ScanResult> results) {
            ScanCallbackCompat callbackCompat = (ScanCallbackCompat) this.callbackCompatRef.get();
            if (callbackCompat != null) {
                List<ScanResultCompat> compatResults = new ArrayList(results.size());
                for (ScanResult result : results) {
                    compatResults.add(new ScanResultCompat(result));
                }
                callbackCompat.onBatchScanResults(compatResults);
            }
        }

        public void onScanFailed(int errorCode) {
            ScanCallbackCompat callbackCompat = (ScanCallbackCompat) this.callbackCompatRef.get();
            if (callbackCompat != null) {
                callbackCompat.onScanFailed(errorCode);
            }
        }
    }

    public static void flushPendingScanResults(@NonNull BluetoothAdapter adapter, @NonNull ScanCallbackCompat callbackCompat) {
        IMPL.flushPendingScanResults(adapter, callbackCompat);
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADMIN")
    public static void startScan(@NonNull BluetoothAdapter adapter, @Nullable List<ScanFilterCompat> filters, @NonNull ScanSettingsCompat settings, @NonNull ScanCallbackCompat callbackCompat) {
        IMPL.startScan(adapter, filters, settings, callbackCompat);
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADMIN")
    public static void startScan(@NonNull BluetoothAdapter adapter, @NonNull ScanCallbackCompat callbackCompat) {
        IMPL.startScan(adapter, callbackCompat);
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADMIN")
    public static void stopScan(@NonNull BluetoothAdapter adapter, @NonNull ScanCallbackCompat callbackCompat) {
        IMPL.stopScan(adapter, callbackCompat);
    }

    static {
        if (VERSION.SDK_INT >= 21) {
            IMPL = new API21BluetoothLeScannerCompatImpl();
        } else {
            IMPL = new API18BluetoothLeScannerCompatImpl();
        }
    }
}
