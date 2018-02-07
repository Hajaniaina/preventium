package com.preventium.boxpreventium.gui;

import android.os.Parcel;
import android.os.Parcelable;

public class QrScanRequest implements Parcelable {

    public static final int REQUEST_PENDING   = 0;
    public static final int REQUEST_COMPLETED = 1;
    public static final int REQUEST_ON_START  = 0;
    public static final int REQUEST_ON_STOP   = 1;

    public long driverId = 0;
    public String driverName = "";

    public boolean driverIdEnabled = false;
    public boolean vehicleFrontOnStartEnabled = true;
    public boolean vehicleBackOnStartEnabled = true;
    public boolean vehicleFrontOnStopEnabled = true;
    public boolean vehicleBackOnStopEnabled = true;

    public int driverIdReq = REQUEST_PENDING;
    public int vehicleFrontReq = REQUEST_PENDING;
    public int vehicleBackReq = REQUEST_PENDING;

    public QrScanRequest() {

    }

    public String toString() {

        String str = "";

        str += driverIdEnabled + " ";
        str += vehicleFrontOnStartEnabled + " ";
        str += vehicleBackOnStartEnabled + " ";
        str += vehicleFrontOnStopEnabled + " ";
        str += vehicleBackOnStopEnabled + " ";

        return str;
    }

    public void resetVehicleReq() {

        vehicleFrontReq = REQUEST_PENDING;
        vehicleBackReq = REQUEST_PENDING;
    }

    public void resetAllReq() {

        driverIdReq = REQUEST_PENDING;
        vehicleFrontReq = REQUEST_PENDING;
        vehicleBackReq = REQUEST_PENDING;
    }

    public boolean isVehicleReqPending (int reqTime) {

        boolean pending = false;
        boolean reqFrontEn = true, reqBackEn = true;

        if (reqTime == REQUEST_ON_START) {

            reqFrontEn = vehicleFrontOnStartEnabled;
            reqBackEn = vehicleBackOnStartEnabled;
        }
        else if (reqTime == REQUEST_ON_STOP) {

            reqFrontEn = vehicleFrontOnStopEnabled;
            reqBackEn = vehicleBackOnStopEnabled;
        }

        if (reqFrontEn) {

            if (vehicleFrontReq != REQUEST_COMPLETED) {

                pending = true;
            }
        }

        if (reqBackEn) {

            if (vehicleBackReq != REQUEST_COMPLETED) {

                pending = true;
            }
        }

        return pending;
    }

    public boolean isAnyReqPending (int reqTime) {

        boolean pending = false;
        boolean reqFrontEn = true, reqBackEn = true;

        if (reqTime == REQUEST_ON_START) {

            reqFrontEn = vehicleFrontOnStartEnabled;
            reqBackEn = vehicleBackOnStartEnabled;
        }
        else if (reqTime == REQUEST_ON_STOP) {

            reqFrontEn = vehicleFrontOnStopEnabled;
            reqBackEn = vehicleBackOnStopEnabled;
        }

        if (driverIdEnabled) {

            if (driverIdReq != REQUEST_COMPLETED) {

                pending = true;
            }
        }

        if (reqFrontEn) {

            if (vehicleFrontReq != REQUEST_COMPLETED) {

                pending = true;
            }
        }

        if (reqBackEn) {

            if (vehicleBackReq != REQUEST_COMPLETED) {

                pending = true;
            }
        }

        return pending;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel (Parcel dest, int flags) {

        dest.writeLong(this.driverIdEnabled ? this.driverId : 0);
        dest.writeString(this.driverIdEnabled ? this.driverName : "");
        dest.writeByte(this.driverIdEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(this.vehicleFrontOnStartEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(this.vehicleBackOnStartEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(this.vehicleFrontOnStopEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(this.vehicleBackOnStopEnabled ? (byte) 1 : (byte) 0);
        dest.writeInt(this.driverIdReq);
        dest.writeInt(this.vehicleFrontReq);
        dest.writeInt(this.vehicleBackReq);
    }

    protected QrScanRequest (Parcel in) {

        this.driverId = in.readLong();
        this.driverName = in.readString();
        this.driverIdEnabled = in.readByte() != 0;
        this.vehicleFrontOnStartEnabled = in.readByte() != 0;
        this.vehicleBackOnStartEnabled = in.readByte() != 0;
        this.vehicleFrontOnStopEnabled = in.readByte() != 0;
        this.vehicleBackOnStopEnabled = in.readByte() != 0;
        this.driverIdReq = in.readInt();
        this.vehicleFrontReq = in.readInt();
        this.vehicleBackReq = in.readInt();
    }

    public static final Parcelable.Creator<QrScanRequest> CREATOR = new Parcelable.Creator<QrScanRequest>() {

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
