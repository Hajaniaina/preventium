package com.preventium.boxpreventium.utils.superclass.bluetooth.scanner;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ScanFilterCompat implements Parcelable {
    public static final Creator<ScanFilterCompat> CREATOR = new C01391();
    @Nullable
    private final String mDeviceAddress;
    @Nullable
    private final String mDeviceName;
    @Nullable
    private final byte[] mManufacturerData;
    @Nullable
    private final byte[] mManufacturerDataMask;
    private final int mManufacturerId;
    @Nullable
    private final byte[] mServiceData;
    @Nullable
    private final byte[] mServiceDataMask;
    @Nullable
    private final ParcelUuid mServiceDataUuid;
    @Nullable
    private final ParcelUuid mServiceUuid;
    @Nullable
    private final ParcelUuid mServiceUuidMask;

    static class C01391 implements Creator<ScanFilterCompat> {
        C01391() {
        }

        public ScanFilterCompat[] newArray(int size) {
            return new ScanFilterCompat[size];
        }

        public ScanFilterCompat createFromParcel(Parcel in) {
            Builder builder = new Builder();
            if (in.readInt() == 1) {
                builder.setDeviceName(in.readString());
            }
            if (in.readInt() == 1) {
                builder.setDeviceAddress(in.readString());
            }
            if (in.readInt() == 1) {
                ParcelUuid uuid = (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader());
                builder.setServiceUuid(uuid);
                if (in.readInt() == 1) {
                    builder.setServiceUuid(uuid, (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader()));
                }
            }
            if (in.readInt() == 1) {
                ParcelUuid serviceDataUuid = (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader());
                if (in.readInt() == 1) {
                    byte[] serviceData = new byte[in.readInt()];
                    in.readByteArray(serviceData);
                    if (in.readInt() == 0) {
                        builder.setServiceData(serviceDataUuid, serviceData);
                    } else {
                        byte[] serviceDataMask = new byte[in.readInt()];
                        in.readByteArray(serviceDataMask);
                        builder.setServiceData(serviceDataUuid, serviceData, serviceDataMask);
                    }
                }
            }
            int manufacturerId = in.readInt();
            if (in.readInt() == 1) {
                byte[] manufacturerData = new byte[in.readInt()];
                in.readByteArray(manufacturerData);
                if (in.readInt() == 0) {
                    builder.setManufacturerData(manufacturerId, manufacturerData);
                } else {
                    byte[] manufacturerDataMask = new byte[in.readInt()];
                    in.readByteArray(manufacturerDataMask);
                    builder.setManufacturerData(manufacturerId, manufacturerData, manufacturerDataMask);
                }
            }
            return builder.build();
        }
    }

    public static final class Builder {
        private String mDeviceAddress;
        private String mDeviceName;
        private byte[] mManufacturerData;
        private byte[] mManufacturerDataMask;
        private int mManufacturerId = -1;
        private byte[] mServiceData;
        private byte[] mServiceDataMask;
        private ParcelUuid mServiceDataUuid;
        private ParcelUuid mServiceUuid;
        private ParcelUuid mUuidMask;

        public Builder setDeviceName(String deviceName) {
            this.mDeviceName = deviceName;
            return this;
        }

        public Builder setDeviceAddress(String deviceAddress) {
            if (deviceAddress == null || BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
                this.mDeviceAddress = deviceAddress;
                return this;
            }
            throw new IllegalArgumentException("invalid device address " + deviceAddress);
        }

        public Builder setServiceUuid(ParcelUuid serviceUuid) {
            this.mServiceUuid = serviceUuid;
            this.mUuidMask = null;
            return this;
        }

        public Builder setServiceUuid(ParcelUuid serviceUuid, ParcelUuid uuidMask) {
            if (this.mUuidMask == null || this.mServiceUuid != null) {
                this.mServiceUuid = serviceUuid;
                this.mUuidMask = uuidMask;
                return this;
            }
            throw new IllegalArgumentException("uuid is null while uuidMask is not null!");
        }

        public Builder setServiceData(ParcelUuid serviceDataUuid, byte[] serviceData) {
            if (serviceDataUuid == null) {
                throw new IllegalArgumentException("serviceDataUuid is null");
            }
            this.mServiceDataUuid = serviceDataUuid;
            this.mServiceData = serviceData;
            this.mServiceDataMask = null;
            return this;
        }

        public Builder setServiceData(ParcelUuid serviceDataUuid, byte[] serviceData, byte[] serviceDataMask) {
            if (serviceDataUuid == null) {
                throw new IllegalArgumentException("serviceDataUuid is null");
            }
            if (this.mServiceDataMask != null) {
                if (this.mServiceData == null) {
                    throw new IllegalArgumentException("serviceData is null while serviceDataMask is not null");
                } else if (this.mServiceData.length != this.mServiceDataMask.length) {
                    throw new IllegalArgumentException("size mismatch for service data and service data mask");
                }
            }
            this.mServiceDataUuid = serviceDataUuid;
            this.mServiceData = serviceData;
            this.mServiceDataMask = serviceDataMask;
            return this;
        }

        public Builder setManufacturerData(int manufacturerId, byte[] manufacturerData) {
            if (manufacturerData == null || manufacturerId >= 0) {
                this.mManufacturerId = manufacturerId;
                this.mManufacturerData = manufacturerData;
                this.mManufacturerDataMask = null;
                return this;
            }
            throw new IllegalArgumentException("invalid manufacture id");
        }

        public Builder setManufacturerData(int manufacturerId, byte[] manufacturerData, byte[] manufacturerDataMask) {
            if (manufacturerData == null || manufacturerId >= 0) {
                if (this.mManufacturerDataMask != null) {
                    if (this.mManufacturerData == null) {
                        throw new IllegalArgumentException("manufacturerData is null while manufacturerDataMask is not null");
                    } else if (this.mManufacturerData.length != this.mManufacturerDataMask.length) {
                        throw new IllegalArgumentException("size mismatch for manufacturerData and manufacturerDataMask");
                    }
                }
                this.mManufacturerId = manufacturerId;
                this.mManufacturerData = manufacturerData;
                this.mManufacturerDataMask = manufacturerDataMask;
                return this;
            }
            throw new IllegalArgumentException("invalid manufacture id");
        }

        public ScanFilterCompat build() {
            return new ScanFilterCompat(this.mDeviceName, this.mDeviceAddress, this.mServiceUuid, this.mUuidMask, this.mServiceDataUuid, this.mServiceData, this.mServiceDataMask, this.mManufacturerId, this.mManufacturerData, this.mManufacturerDataMask);
        }
    }

    private ScanFilterCompat(@Nullable String name, @Nullable String deviceAddress, @Nullable ParcelUuid uuid, @Nullable ParcelUuid uuidMask, @Nullable ParcelUuid serviceDataUuid, @Nullable byte[] serviceData, @Nullable byte[] serviceDataMask, int manufacturerId, @Nullable byte[] manufacturerData, @Nullable byte[] manufacturerDataMask) {
        this.mDeviceName = name;
        this.mServiceUuid = uuid;
        this.mServiceUuidMask = uuidMask;
        this.mDeviceAddress = deviceAddress;
        this.mServiceDataUuid = serviceDataUuid;
        this.mServiceData = serviceData;
        this.mServiceDataMask = serviceDataMask;
        this.mManufacturerId = manufacturerId;
        this.mManufacturerData = manufacturerData;
        this.mManufacturerDataMask = manufacturerDataMask;
    }

    @TargetApi(21)
    ScanFilter toApi21() {
        android.bluetooth.le.ScanFilter.Builder builder = new android.bluetooth.le.ScanFilter.Builder();
        if (this.mDeviceName != null) {
            builder.setDeviceName(this.mDeviceName);
        }
        if (this.mServiceUuid != null) {
            builder.setServiceUuid(this.mServiceUuid, this.mServiceUuidMask);
        }
        if (this.mDeviceAddress != null) {
            builder.setDeviceAddress(this.mDeviceAddress);
        }
        if (this.mServiceDataUuid != null) {
            builder.setServiceData(this.mServiceDataUuid, this.mServiceData, this.mServiceDataMask);
        }
        if (this.mManufacturerId < 0) {
            builder.setManufacturerData(this.mManufacturerId, this.mManufacturerData, this.mManufacturerDataMask);
        }
        return builder.build();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 0;
        dest.writeInt(this.mDeviceName == null ? 0 : 1);
        if (this.mDeviceName != null) {
            dest.writeString(this.mDeviceName);
        }
        if (this.mDeviceAddress == null) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mDeviceAddress != null) {
            dest.writeString(this.mDeviceAddress);
        }
        if (this.mServiceUuid == null) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mServiceUuid != null) {
            dest.writeParcelable(this.mServiceUuid, flags);
            if (this.mServiceUuidMask == null) {
                i = 0;
            } else {
                i = 1;
            }
            dest.writeInt(i);
            if (this.mServiceUuidMask != null) {
                dest.writeParcelable(this.mServiceUuidMask, flags);
            }
        }
        if (this.mServiceDataUuid == null) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mServiceDataUuid != null) {
            dest.writeParcelable(this.mServiceDataUuid, flags);
            if (this.mServiceData == null) {
                i = 0;
            } else {
                i = 1;
            }
            dest.writeInt(i);
            if (this.mServiceData != null) {
                dest.writeInt(this.mServiceData.length);
                dest.writeByteArray(this.mServiceData);
                if (this.mServiceDataMask == null) {
                    i = 0;
                } else {
                    i = 1;
                }
                dest.writeInt(i);
                if (this.mServiceDataMask != null) {
                    dest.writeInt(this.mServiceDataMask.length);
                    dest.writeByteArray(this.mServiceDataMask);
                }
            }
        }
        dest.writeInt(this.mManufacturerId);
        if (this.mManufacturerData == null) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mManufacturerData != null) {
            dest.writeInt(this.mManufacturerData.length);
            dest.writeByteArray(this.mManufacturerData);
            if (this.mManufacturerDataMask != null) {
                i2 = 1;
            }
            dest.writeInt(i2);
            if (this.mManufacturerDataMask != null) {
                dest.writeInt(this.mManufacturerDataMask.length);
                dest.writeByteArray(this.mManufacturerDataMask);
            }
        }
    }

    @Nullable
    public String getDeviceName() {
        return this.mDeviceName;
    }

    @Nullable
    public ParcelUuid getServiceUuid() {
        return this.mServiceUuid;
    }

    @Nullable
    public ParcelUuid getServiceUuidMask() {
        return this.mServiceUuidMask;
    }

    @Nullable
    public String getDeviceAddress() {
        return this.mDeviceAddress;
    }

    @Nullable
    public byte[] getServiceData() {
        return this.mServiceData;
    }

    @Nullable
    public byte[] getServiceDataMask() {
        return this.mServiceDataMask;
    }

    @Nullable
    public ParcelUuid getServiceDataUuid() {
        return this.mServiceDataUuid;
    }

    public int getManufacturerId() {
        return this.mManufacturerId;
    }

    @Nullable
    public byte[] getManufacturerData() {
        return this.mManufacturerData;
    }

    @Nullable
    public byte[] getManufacturerDataMask() {
        return this.mManufacturerDataMask;
    }

    public boolean matches(ScanResultCompat scanResult) {
        if (scanResult == null) {
            return false;
        }
        BluetoothDevice device = scanResult.getDevice();
        if (this.mDeviceAddress != null && (device == null || !this.mDeviceAddress.equals(device.getAddress()))) {
            return false;
        }
        ScanRecordCompat scanRecord = scanResult.getScanRecord();
        if (scanRecord == null && (this.mDeviceName != null || this.mServiceUuid != null || this.mManufacturerData != null || this.mServiceData != null || this.mServiceDataUuid != null || this.mManufacturerId >= 0)) {
            return false;
        }
        if (this.mDeviceName != null && !this.mDeviceName.equals(scanRecord.getDeviceName())) {
            return false;
        }
        if (this.mServiceUuid != null && !matchesServiceUuids(this.mServiceUuid, this.mServiceUuidMask, scanRecord.getServiceUuids())) {
            return false;
        }
        if (this.mServiceDataUuid != null && !matchesPartialData(this.mServiceData, this.mServiceDataMask, scanRecord.getServiceData(this.mServiceDataUuid))) {
            return false;
        }
        if (this.mManufacturerId < 0 || scanRecord == null || matchesPartialData(this.mManufacturerData, this.mManufacturerDataMask, scanRecord.getManufacturerSpecificData(this.mManufacturerId))) {
            return true;
        }
        return false;
    }

    private boolean matchesServiceUuids(ParcelUuid uuid, ParcelUuid parcelUuidMask, List<ParcelUuid> uuids) {
        if (uuid == null) {
            return true;
        }
        if (uuids == null) {
            return false;
        }
        for (ParcelUuid parcelUuid : uuids) {
            if (matchesServiceUuid(uuid.getUuid(), parcelUuidMask == null ? null : parcelUuidMask.getUuid(), parcelUuid.getUuid())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesServiceUuid(UUID uuid, UUID mask, UUID data) {
        if (mask == null) {
            return uuid.equals(data);
        }
        if ((uuid.getLeastSignificantBits() & mask.getLeastSignificantBits()) == (data.getLeastSignificantBits() & mask.getLeastSignificantBits()) && (uuid.getMostSignificantBits() & mask.getMostSignificantBits()) == (data.getMostSignificantBits() & mask.getMostSignificantBits())) {
            return true;
        }
        return false;
    }

    private boolean matchesPartialData(byte[] data, byte[] dataMask, byte[] parsedData) {
        if (parsedData == null || parsedData.length < data.length) {
            return false;
        }
        int i;
        if (dataMask == null) {
            for (i = 0; i < data.length; i++) {
                if (parsedData[i] != data[i]) {
                    return false;
                }
            }
            return true;
        }
        for (i = 0; i < data.length; i++) {
            if ((dataMask[i] & parsedData[i]) != (dataMask[i] & data[i])) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "BluetoothLeScanFilter [mDeviceName=" + this.mDeviceName + ", mDeviceAddress=" + this.mDeviceAddress + ", mUuid=" + this.mServiceUuid + ", mUuidMask=" + this.mServiceUuidMask + ", mServiceDataUuid=" + ObjectsCompat.toString(this.mServiceDataUuid) + ", mServiceData=" + Arrays.toString(this.mServiceData) + ", mServiceDataMask=" + Arrays.toString(this.mServiceDataMask) + ", mManufacturerId=" + this.mManufacturerId + ", mManufacturerData=" + Arrays.toString(this.mManufacturerData) + ", mManufacturerDataMask=" + Arrays.toString(this.mManufacturerDataMask) + "]";
    }

    public int hashCode() {
        return ObjectsCompat.hash(this.mDeviceName, this.mDeviceAddress, Integer.valueOf(this.mManufacturerId), this.mManufacturerData, this.mManufacturerDataMask, this.mServiceDataUuid, this.mServiceData, this.mServiceDataMask, this.mServiceUuid, this.mServiceUuidMask);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScanFilterCompat other = (ScanFilterCompat) obj;
        if (ObjectsCompat.equals(this.mDeviceName, other.mDeviceName) && ObjectsCompat.equals(this.mDeviceAddress, other.mDeviceAddress) && this.mManufacturerId == other.mManufacturerId && ObjectsCompat.deepEquals(this.mManufacturerData, other.mManufacturerData) && ObjectsCompat.deepEquals(this.mManufacturerDataMask, other.mManufacturerDataMask) && ObjectsCompat.deepEquals(this.mServiceDataUuid, other.mServiceDataUuid) && ObjectsCompat.deepEquals(this.mServiceData, other.mServiceData) && ObjectsCompat.deepEquals(this.mServiceDataMask, other.mServiceDataMask) && ObjectsCompat.equals(this.mServiceUuid, other.mServiceUuid) && ObjectsCompat.equals(this.mServiceUuidMask, other.mServiceUuidMask)) {
            return true;
        }
        return false;
    }
}
