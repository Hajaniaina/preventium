package com.preventium.boxpreventium.utils.superclass.bluetooth.device;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by tog on 28/08/2018.
 */

public class BluetoothReceiver extends BroadcastReceiver {

    // interface
    public interface BluetoothCallback {
        public void onScanState(boolean scanning);
        public void onScanResult(BluetoothDevice device, int rssi);
        public void onBluetoothAdapterIsDisable();
        public void onBluetoothAdapterIsNull();
        public void onLocationServiceIsDisable();
    }

    // variable
    private BluetoothCallback callback;

    public BluetoothReceiver (BluetoothCallback callback ) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if( BluetoothDevice.ACTION_FOUND.equals(action) ) {

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
            if( this.callback != null && device != null ) {
                this.callback.onScanResult(device, rssi);
                // Toast.makeText(context, "Box trouvé: " + device.getName(), Toast.LENGTH_SHORT).show();
            } // else
                // Toast.makeText(context, "Box pas trouver", Toast.LENGTH_LONG).show();

        }

        if( BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) ) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Toast.makeText(context, device.getName() + " est connnected", Toast.LENGTH_SHORT).show();
        }

        if( BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) ) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Toast.makeText(context, device.getName() + " est deconnected", Toast.LENGTH_SHORT).show();
        }

        if( BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action) ) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Toast.makeText(context, device.getName() + " est ajouté au bond", Toast.LENGTH_SHORT).show();
        }
    }
}
