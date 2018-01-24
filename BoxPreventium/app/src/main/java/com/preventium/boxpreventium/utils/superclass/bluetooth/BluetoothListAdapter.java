package com.preventium.boxpreventium.utils.superclass.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.utils.superclass.bluetooth.device.BluetoothIO;
import java.util.ArrayList;

public class BluetoothListAdapter extends BaseAdapter {
    private ArrayList<Boolean> mBluetoothChecked = new ArrayList();
    private ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList();
    private ArrayList<Integer> mBluetoothRssi = new ArrayList();
    private boolean mCkeckable;
    private LayoutInflater mInflater;

    static class ViewHolder {
        TextView deviceAddress;
        CheckBox deviceChecked;
        TextView deviceName;
        TextView deviceRssi;

        ViewHolder() {
        }
    }

    public BluetoothListAdapter(LayoutInflater layoutinflater) {
        this.mInflater = layoutinflater;
        this.mCkeckable = false;
    }

    public void setCheckable(boolean checkable) {
        this.mCkeckable = checkable;
        notifyDataSetChanged();
    }

    public void addDevice(BluetoothDevice device, int rssi) {
        addDevice(device, rssi, false);
    }

    public void addDevice(BluetoothDevice device, int rssi, boolean checked) {
        if (this.mBluetoothDevices.contains(device)) {
            this.mBluetoothRssi.set(this.mBluetoothDevices.indexOf(device), Integer.valueOf(rssi));
        } else {
            this.mBluetoothDevices.add(device);
            this.mBluetoothRssi.add(Integer.valueOf(rssi));
            ArrayList arrayList = this.mBluetoothChecked;
            boolean z = this.mCkeckable && checked;
            arrayList.add(Boolean.valueOf(z));
        }
        notifyDataSetChanged();
    }

    public BluetoothDevice getDevice(int position) {
        return (BluetoothDevice) this.mBluetoothDevices.get(position);
    }

    public int getRssi(int position) {
        return ((Integer) this.mBluetoothRssi.get(position)).intValue();
    }

    public boolean getChecked(int position) {
        return ((Boolean) this.mBluetoothChecked.get(position)).booleanValue();
    }

    public ArrayList<BluetoothDevice> getCheckedDevices() {
        ArrayList<BluetoothDevice> ret = new ArrayList();
        if (this.mCkeckable) {
            for (int position = 0; position < this.mBluetoothChecked.size(); position++) {
                if (((Boolean) this.mBluetoothChecked.get(position)).booleanValue()) {
                    ret.add(this.mBluetoothDevices.get(position));
                }
            }
        }
        return ret;
    }

    public void clear() {
        this.mBluetoothDevices.clear();
        this.mBluetoothRssi.clear();
        this.mBluetoothChecked.clear();
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mBluetoothDevices.size();
    }

    public Object getItem(int position) {
        return this.mBluetoothDevices.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(final int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = this.mInflater.inflate(R.layout.scan_activity_item_model, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceName = (TextView) view.findViewById(R.id.name);
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.address);
            viewHolder.deviceRssi = (TextView) view.findViewById(R.id.rssi);
            viewHolder.deviceChecked = (CheckBox) view.findViewById(R.id.checkbox);
            viewHolder.deviceChecked.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    BluetoothListAdapter.this.mBluetoothChecked.set(position, Boolean.valueOf(isChecked));
                }
            });
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        BluetoothDevice device = (BluetoothDevice) this.mBluetoothDevices.get(position);
        int rssi = ((Integer) this.mBluetoothRssi.get(position)).intValue();
        String deviceName = BluetoothIO.getAliasName(device);
        if (deviceName == null || deviceName.length() == 0 || deviceName.isEmpty()) {
            deviceName = device.getName();
        }
        if (deviceName == null || deviceName.length() == 0 || deviceName.isEmpty()) {
            deviceName = "Unknow device";
        }
        viewHolder.deviceName.setText(deviceName);
        viewHolder.deviceAddress.setText(device.getAddress());
        viewHolder.deviceRssi.setText(Integer.toString(rssi));
        viewHolder.deviceChecked.setVisibility(this.mCkeckable ? View.VISIBLE : View.GONE);
        viewHolder.deviceChecked.setChecked(((Boolean) this.mBluetoothChecked.get(position)).booleanValue());
        return view;
    }
}
