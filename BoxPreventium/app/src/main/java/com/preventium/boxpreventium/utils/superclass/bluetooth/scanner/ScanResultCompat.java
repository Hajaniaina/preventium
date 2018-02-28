package com.preventium.boxpreventium.utils.superclass.bluetooth.scanner;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class ScanResultCompat implements Parcelable {
    public static final Creator<ScanResultCompat> CREATOR = new C01401();
    private BluetoothDevice mDevice;
    private int mRssi;
    @Nullable
    private ScanRecordCompat mScanRecord;
    private long mTimestampNanos;

    static class C01401 implements Creator<ScanResultCompat> {
        C01401() {
        }

        public ScanResultCompat createFromParcel(Parcel source) {
            return new ScanResultCompat(source);
        }

        public ScanResultCompat[] newArray(int size) {
            return new ScanResultCompat[size];
        }
    }

    public ScanResultCompat(BluetoothDevice device, @Nullable ScanRecordCompat scanRecord, int rssi, long timestampNanos) {
        this.mDevice = device;
        this.mScanRecord = scanRecord;
        this.mRssi = rssi;
        this.mTimestampNanos = timestampNanos;
    }

    @TargetApi(21)
    ScanResultCompat(ScanResult result) {
        this.mDevice = result.getDevice();
        this.mScanRecord = new ScanRecordCompat(result.getScanRecord());
        this.mRssi = result.getRssi();
        this.mTimestampNanos = result.getTimestampNanos();
    }

    private ScanResultCompat(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mDevice != null) {
            dest.writeInt(1);
            this.mDevice.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        if (this.mScanRecord != null) {
            dest.writeInt(1);
            dest.writeByteArray(this.mScanRecord.getBytes());
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.mRssi);
        dest.writeLong(this.mTimestampNanos);
    }

    private void readFromParcel(Parcel in) {
        if (in.readInt() == 1) {
            this.mDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(in);
        }
        if (in.readInt() == 1) {
            this.mScanRecord = ScanRecordCompat.parseFromBytes(in.createByteArray());
        }
        this.mRssi = in.readInt();
        this.mTimestampNanos = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    @Nullable
    public ScanRecordCompat getScanRecord() {
        return this.mScanRecord;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public long getTimestampNanos() {
        return this.mTimestampNanos;
    }

    public int hashCode() {
        return ObjectsCompat.hash(this.mDevice, Integer.valueOf(this.mRssi), this.mScanRecord, Long.valueOf(this.mTimestampNanos));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScanResultCompat other = (ScanResultCompat) obj;
        if (ObjectsCompat.equals(this.mDevice, other.mDevice) && this.mRssi == other.mRssi && ObjectsCompat.equals(this.mScanRecord, other.mScanRecord) && this.mTimestampNanos == other.mTimestampNanos) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "ScanResult{mDevice=" + this.mDevice + ", mScanRecord=" + ObjectsCompat.toString(this.mScanRecord) + ", mRssi=" + this.mRssi + ", mTimestampNanos=" + this.mTimestampNanos + '}';
    }
}
