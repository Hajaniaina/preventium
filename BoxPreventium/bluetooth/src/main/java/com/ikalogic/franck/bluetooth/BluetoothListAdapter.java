package com.ikalogic.franck.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ikalogic.franck.bluetooth.device.BluetoothIO;

import java.util.ArrayList;

/**
 * Created by franck on 6/18/16.
 */
public class BluetoothListAdapter extends BaseAdapter {


    private ArrayList<BluetoothDevice> mBluetoothDevices;
    private ArrayList<Integer> mBluetoothRssi;
    private LayoutInflater mInflater;

    public BluetoothListAdapter(LayoutInflater layoutinflater) {
        super();
        mBluetoothDevices = new ArrayList<BluetoothDevice>();
        mBluetoothRssi = new ArrayList<Integer>();
        mInflater = layoutinflater;
    }

    public void addDevice(BluetoothDevice device, int rssi) {

        if( mBluetoothDevices.contains(device) )
        {
            int position = mBluetoothDevices.indexOf( device );
            mBluetoothRssi.set( position, rssi );
        }
        else
        {
            mBluetoothDevices.add( device );
            mBluetoothRssi.add( rssi );
        }
        notifyDataSetChanged();
    }

    public BluetoothDevice getDevice(int position) {
        return mBluetoothDevices.get(position);
    }

    public int getRssi(int position) { return mBluetoothRssi.get(position); }

    public void clear() {
        mBluetoothDevices.clear();
        mBluetoothRssi.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return mBluetoothDevices.size(); }

    @Override
    public Object getItem(int position) { return mBluetoothDevices.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflater.inflate(R.layout.bluetooth_item, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceName = (TextView) view.findViewById(R.id.name);
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.address);
            viewHolder.deviceRssi = (TextView) view.findViewById(R.id.rssi);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        BluetoothDevice device = mBluetoothDevices.get(position);
        int rssi = mBluetoothRssi.get(position);

        String deviceName = BluetoothIO.getAliasName(device);
        if( deviceName == null || deviceName.length() == 0 || deviceName.isEmpty() ) deviceName = device.getName();
        if( deviceName == null || deviceName.length() == 0 || deviceName.isEmpty() ) deviceName = "Unknow device";

        viewHolder.deviceName.setText(deviceName);
        viewHolder.deviceAddress.setText(device.getAddress());
        viewHolder.deviceRssi.setText( Integer.toString( rssi ) );
        return view;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
    }
}
