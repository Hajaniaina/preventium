package com.preventium.boxpreventium.utils.superclass.bluetooth.scanner;

import java.util.List;

/**
 * Created by Franck on 08/08/2016.
 */

public abstract class ScanCallbackCompat {

    /**
     * Fails to start scan as BLE scan with the same settings is already started by the app.
     */
    public static final int SCAN_FAILED_ALREADY_STARTED = 1;

    /**
     * Fails to start scan as app cannot be registered.
     */
    public static final int SCAN_FAILED_APPLICATION_REGISTRATION_FAILED = 2;

    /**
     * Fails to start scan due an internal error
     */
    public static final int SCAN_FAILED_INTERNAL_ERROR = 3;

    /**
     * Fails to start power optimized scan as this feature is not supported.
     */
    public static final int SCAN_FAILED_FEATURE_UNSUPPORTED = 4;

    /**
     * Callback when a BLE advertisement has been found.
     *
     * @param callbackType Determines how this callback was triggered. Currently could only be
     *                     {@link android.bluetooth.le.ScanSettings#CALLBACK_TYPE_ALL_MATCHES}.
     * @param result       A Bluetooth LE scan result.
     */
    @SuppressWarnings("EmptyMethod")
    public void onScanResult(int callbackType, ScanResultCompat result) {
        // no implementation
    }

    /**
     * Callback when batch results are delivered.
     *
     * @param results List of scan results that are previously scanned.
     */
    @SuppressWarnings("EmptyMethod")
    public void onBatchScanResults(List<ScanResultCompat> results) {
    }

    /**
     * Callback when scan could not be started.
     *
     * @param errorCode Error code (one of SCAN_FAILED_*) for scan failure.
     */
    @SuppressWarnings("EmptyMethod")
    public void onScanFailed(int errorCode) {
    }
}
