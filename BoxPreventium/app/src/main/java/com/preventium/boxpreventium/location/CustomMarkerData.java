package com.preventium.boxpreventium.location;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class CustomMarkerData implements Parcelable {

    public int type = 0;
    public LatLng position = null;
    public String title = null;
    public boolean alert = false;
    public int alertRadius = 0;
    public ArrayList<String> alertAttachments = null;
    public String alertMsg = null;
    public boolean alertReqSignature = false;
    public boolean shared = false;

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel (Parcel dest, int flags) {

        dest.writeInt(this.type);
        dest.writeParcelable(this.position, flags);
        dest.writeString(this.title);
        dest.writeByte(this.alert ? (byte) 1 : (byte) 0);
        dest.writeInt(this.alertRadius);
        dest.writeStringList(this.alertAttachments);
        dest.writeString(this.alertMsg);
        dest.writeByte(this.alertReqSignature ? (byte) 1 : (byte) 0);
        dest.writeByte(this.shared ? (byte) 1 : (byte) 0);
    }

    public CustomMarkerData() {

    }

    protected CustomMarkerData (Parcel in) {

        this.type = in.readInt();
        this.position = in.readParcelable(LatLng.class.getClassLoader());
        this.title = in.readString();
        this.alert = in.readByte() != 0;
        this.alertRadius = in.readInt();
        this.alertAttachments = in.createStringArrayList();
        this.alertMsg = in.readString();
        this.alertReqSignature = in.readByte() != 0;
        this.shared = in.readByte() != 0;
    }

    public static final Parcelable.Creator<CustomMarkerData> CREATOR = new Parcelable.Creator<CustomMarkerData>() {

        @Override
        public CustomMarkerData createFromParcel (Parcel source) {

            return new CustomMarkerData(source);
        }

        @Override
        public CustomMarkerData[] newArray (int size) {

            return new CustomMarkerData[size];
        }
    };
}
