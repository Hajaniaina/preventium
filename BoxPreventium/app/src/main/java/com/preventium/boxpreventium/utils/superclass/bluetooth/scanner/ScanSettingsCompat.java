package com.preventium.boxpreventium.utils.superclass.bluetooth.scanner;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanSettings;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ScanSettingsCompat implements Parcelable {
    public static final int CALLBACK_TYPE_ALL_MATCHES = 1;
    public static final Creator<ScanSettingsCompat> CREATOR = new C01411();
    public static final int SCAN_MODE_BALANCED = 1;
    public static final int SCAN_MODE_LOW_LATENCY = 2;
    public static final int SCAN_MODE_LOW_POWER = 0;
    private final int mCallbackType;
    private final long mReportDelayMillis;
    private final int mScanMode;

    static class C01411 implements Creator<ScanSettingsCompat> {
        C01411() {
        }

        public ScanSettingsCompat[] newArray(int size) {
            return new ScanSettingsCompat[size];
        }

        public ScanSettingsCompat createFromParcel(Parcel in) {
            return new ScanSettingsCompat(in);
        }
    }

    public static final class Builder {
        private final int mCallbackType = 1;
        private long mReportDelayMillis = 0;
        private int mScanMode = 0;

        public Builder setScanMode(int scanMode) {
            if (scanMode < 0 || scanMode > 2) {
                throw new IllegalArgumentException("invalid scan mode " + scanMode);
            }
            this.mScanMode = scanMode;
            return this;
        }

        private boolean isValidCallbackType(int callbackType) {
            return callbackType == 1;
        }

        public Builder setReportDelay(long reportDelayMillis) {
            if (reportDelayMillis < 0) {
                throw new IllegalArgumentException("reportDelay must be > 0");
            }
            this.mReportDelayMillis = reportDelayMillis;
            return this;
        }

        public ScanSettingsCompat build() {
            return new ScanSettingsCompat(this.mScanMode, 1, this.mReportDelayMillis);
        }
    }

    public int getScanMode() {
        return this.mScanMode;
    }

    public int getCallbackType() {
        return this.mCallbackType;
    }

    public long getReportDelayMillis() {
        return this.mReportDelayMillis;
    }

    private ScanSettingsCompat(int scanMode, int callbackType, long reportDelayMillis) {
        this.mScanMode = scanMode;
        this.mCallbackType = callbackType;
        this.mReportDelayMillis = reportDelayMillis;
    }

    private ScanSettingsCompat(Parcel in) {
        this.mScanMode = in.readInt();
        this.mCallbackType = in.readInt();
        this.mReportDelayMillis = in.readLong();
    }

    @TargetApi(21)
    ScanSettings toApi21() {
        return new android.bluetooth.le.ScanSettings.Builder().setReportDelay(getReportDelayMillis()).setScanMode(getScanMode()).build();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mScanMode);
        dest.writeInt(this.mCallbackType);
        dest.writeLong(this.mReportDelayMillis);
    }

    public int describeContents() {
        return 0;
    }
}
