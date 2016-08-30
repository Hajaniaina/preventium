package com.preventium.boxpreventium.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.bluetooth.device.BluetoothIO;

import java.util.ArrayList;

/**
 * Created by Franck on 08/08/2016.
 */

public class BluetoothListAdapter extends BaseAdapter {


    private ArrayList<BluetoothDevice> mBluetoothDevices;
    private ArrayList<Integer> mBluetoothRssi;
    private ArrayList<Boolean> mBluetoothChecked;
    private LayoutInflater mInflater;
    private boolean mCkeckable;


    public BluetoothListAdapter(LayoutInflater layoutinflater) {
        super();
        mBluetoothDevices = new ArrayList<BluetoothDevice>();
        mBluetoothRssi = new ArrayList<Integer>();
        mBluetoothChecked = new ArrayList<Boolean>();
        mInflater = layoutinflater;
        mCkeckable = false;
    }

    public void setCheckable(boolean checkable){
        mCkeckable = checkable;
        notifyDataSetChanged();
    }

    public void addDevice(BluetoothDevice device, int rssi) { addDevice( device, rssi, false ); }

    public void addDevice(BluetoothDevice device, int rssi, boolean checked ) {

        if( mBluetoothDevices.contains(device) )
        {
            int position = mBluetoothDevices.indexOf( device );
            mBluetoothRssi.set( position, rssi );
        }
        else
        {
            mBluetoothDevices.add( device );
            mBluetoothRssi.add( rssi );
            mBluetoothChecked.add(
                    mCkeckable && checked );
        }
        notifyDataSetChanged();
    }

    public BluetoothDevice getDevice(int position) {
        return mBluetoothDevices.get(position);
    }

    public int getRssi(int position) { return mBluetoothRssi.get(position); }

    public boolean getChecked(int position) { return  mBluetoothChecked.get(position); }

    public ArrayList<BluetoothDevice> getCheckedDevices() {
        ArrayList<BluetoothDevice> ret = new ArrayList<BluetoothDevice>();
        if( mCkeckable ) {
            for (int position = 0; position < mBluetoothChecked.size(); position++) {
                if( mBluetoothChecked.get(position) ) ret.add( mBluetoothDevices.get(position) );
            }
        }
        return ret;
    }

    public void clear() {
        mBluetoothDevices.clear();
        mBluetoothRssi.clear();
        mBluetoothChecked.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return mBluetoothDevices.size(); }

    @Override
    public Object getItem(int position) { return mBluetoothDevices.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflater.inflate(R.layout.scan_activity_item_model, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceName = (TextView) view.findViewById(R.id.name);
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.address);
            viewHolder.deviceRssi = (TextView) view.findViewById(R.id.rssi);
            viewHolder.deviceChecked = (CheckBox) view.findViewById(R.id.checkbox);
            viewHolder.deviceChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mBluetoothChecked.set(position,isChecked);
                }
            });
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
        viewHolder.deviceChecked.setVisibility( mCkeckable ? View.VISIBLE : View.GONE );
        viewHolder.deviceChecked.setChecked( mBluetoothChecked.get(position) );
        return view;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
        CheckBox deviceChecked;
    }
}