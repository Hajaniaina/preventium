package com.preventium.boxpreventium.utils.superclass.bluetooth.scanner;

import java.util.List;

public abstract class ScanCallbackCompat {
    public static final int SCAN_FAILED_ALREADY_STARTED = 1;
    public static final int SCAN_FAILED_APPLICATION_REGISTRATION_FAILED = 2;
    public static final int SCAN_FAILED_FEATURE_UNSUPPORTED = 4;
    public static final int SCAN_FAILED_INTERNAL_ERROR = 3;

    public void onScanResult(int callbackType, ScanResultCompat result) {
    }

    public void onBatchScanResults(List<ScanResultCompat> list) {
    }

    public void onScanFailed(int errorCode) {
    }
}
