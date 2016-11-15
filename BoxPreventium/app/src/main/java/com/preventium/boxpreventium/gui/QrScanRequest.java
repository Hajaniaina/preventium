package com.preventium.boxpreventium.gui;

import android.os.Parcel;
import android.os.Parcelable;

public class QrScanRequest implements Parcelable {

    public static final int REQUEST_PENDING   = 0;
    public static final int REQUEST_COMPLETED = 1;

    public boolean vehicleFrontEnabled = true;
    public boolean vehicleBackEnabled = true;

    public int driverIdReq = REQUEST_PENDING;
    public int vehicleFrontReq = REQUEST_PENDING;
    public int vehicleBackReq = REQUEST_PENDING;
    public long driverId = 0;

    public QrScanRequest() {}

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel (Parcel dest, int flags) {

        dest.writeByte(this.vehicleFrontEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(this.vehicleBackEnabled ? (byte) 1 : (byte) 0);
        dest.writeInt(this.driverIdReq);
        dest.writeInt(this.vehicleFrontReq);
        dest.writeInt(this.vehicleBackReq);
        dest.writeLong(this.driverId);
    }

    protected QrScanRequest (Parcel in) {

        this.vehicleFrontEnabled = in.readByte() != 0;
        this.vehicleBackEnabled = in.readByte() != 0;
        this.driverIdReq = in.readInt();
        this.vehicleFrontReq = in.readInt();
        this.vehicleBackReq = in.readInt();
        this.driverId = in.readLong();
    }

    public static final Creator<QrScanRequest> CREATOR = new Creator<QrScanRequest>() {

        @Override
        public QrScanRequest createFromParcel (Parcel source) {

            return new QrScanRequest(source);
        }

        @Override
        public QrScanRequest[] newArray (int size) {

            return new QrScanRequest[size];
        }
    };
}
