package com.preventium.boxpreventium.connectivity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import com.preventium.boxpreventium.connectivity.listeners.NotifyListener;


/**
 * Created by Franck on 27/05/2016.
 */

public class ConnectivityStatus extends BroadcastReceiver {

    private final static String TAG = "ConnectivityStatus";

    public final int BLUETOOTH_STATE_OFF            = 0xA0;
    public final int BLUETOOTH_STATE_TURNING_OFF    = 0xA1;
    public final int BLUETOOTH_STATE_ON             = 0xA2;
    public final int BLUETOOTH_STATE_TURNING_ON     = 0xA3;
    public final int WIFI_STATE_DISABLED            = 0xB0;
    public final int WIFI_STATE_DISABLING           = 0xB1;
    public final int WIFI_STATE_ENABLED             = 0xB2;
    public final int WIFI_STATE_ENABLING            = 0xB3;
    public final int CONNECTIVITY_STATE_DISABLED    = 0xC0;
    public final int CONNECTIVITY_STATE_ENABLED     = 0xC1;

    private final Context mContext;
    private IntentFilter intentFilter;
    private NotifyListener mNotify;

    // Constructors

    public ConnectivityStatus(Context context) {
        this.mContext = context;
        this.mNotify = null;
        initializeIntentFilter();
    }

    public ConnectivityStatus( Context context, NotifyListener listener ) {
        this.mContext = context;
        this.mNotify = listener;
        initializeIntentFilter();
    }

    // Protected

    protected void initializeIntentFilter() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(this,intentFilter);
    }

    // Setter

    public void setNotify( NotifyListener listener ) { this.mNotify = listener; }

    // Status

    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for ( NetworkInfo ni : netInfo ) {
            if ( ni.getTypeName().equalsIgnoreCase("WIFI") && ni.isConnected() ) haveConnectedWifi = true;
            if ( ni.getTypeName().equalsIgnoreCase("MOBILE") && ni.isConnected() ) haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public boolean haveWifiConnection() {
        boolean haveConnectedWifi = false;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for ( NetworkInfo ni : netInfo )
            if ( ni.getTypeName().equalsIgnoreCase("WIFI") && ni.isConnected() ) haveConnectedWifi = true;
        return haveConnectedWifi;
    }

    public boolean haveMobileConnection() {
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for ( NetworkInfo ni : netInfo )
            if ( ni.getTypeName().equalsIgnoreCase("MOBILE") && ni.isConnected() ) haveConnectedMobile = true;
        return haveConnectedMobile;
    }

    public boolean haveInternetConnection() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if ( connectivity != null ) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if ( info != null )
                for ( int i = 0; i < info.length; i++ )
                    if ( info[i].getState() == NetworkInfo.State.CONNECTED )
                        return true;
        }
        return false;
    }

    public boolean haveBluetoothSupported() {
        boolean isBluetoothSupported = mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
        return isBluetoothSupported;
    }

    public boolean haveBluetoothLowEnergySupported() {
        boolean isBluetoothSupported = mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        return isBluetoothSupported;
    }

    public boolean haveBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }

    // Alert dialog

    public void showWifiSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Wi-Fi network");
        alertDialog.setMessage("Wi-Fi network is not enabled. Do you want to go to settings menu?");
        alertDialog.setIcon(android.R.drawable.ic_menu_help);
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                mContext.startActivity( intent );
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void showMobileSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Mobile network");
        alertDialog.setMessage("Mobile network is not enabled. Do you want to go to settings menu?");
        alertDialog.setIcon(android.R.drawable.ic_menu_help);
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                mContext.startActivity( intent );
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void showBluetoothSupportedAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Bluetooth");
        alertDialog.setMessage("Bluetooth is not supported on the device!");
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void showBluetoothLowEnergySupportedAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Bluetooth Low Energy");
        alertDialog.setMessage("BLE (Bluetooth Low Energy) is not supported on the device!");
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void showBluetoothSettingAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Bluetooth");
        alertDialog.setMessage("Bluetooth is not enabled. Do you want to go to settings menu?");
        alertDialog.setIcon(android.R.drawable.ic_menu_help);
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mContext.startActivity( intent );
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    // BroadcastReceiver

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        if( action.equals(BluetoothAdapter.ACTION_STATE_CHANGED) ) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG, "Bluetooth adapter is off");
                    if( mNotify != null ) {
                        mNotify.onNotify( BLUETOOTH_STATE_OFF );
                        mNotify.onBluetoothStateChanged( BLUETOOTH_STATE_OFF );
                    }
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d(TAG, "Bluetooth adapter is turning off");
                    if( mNotify != null ) {
                        mNotify.onNotify( BLUETOOTH_STATE_TURNING_OFF );
                        mNotify.onBluetoothStateChanged( BLUETOOTH_STATE_TURNING_OFF );
                    }
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "Bluetooth adapter is on");
                    if( mNotify != null ) {
                        mNotify.onNotify( BLUETOOTH_STATE_ON );
                        mNotify.onBluetoothStateChanged( BLUETOOTH_STATE_ON );
                    }
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d(TAG, "Bluetooth adapter is turning on");
                    if( mNotify != null ) {
                        mNotify.onNotify( BLUETOOTH_STATE_TURNING_ON );
                        mNotify.onBluetoothStateChanged( BLUETOOTH_STATE_TURNING_ON );
                    }
                    break;
            }
        }

        if( action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION) ) {
            final int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.ERROR_AUTHENTICATING);
            switch (state) {
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.d(TAG, "Wi-Fi is disabled.");
                    if( mNotify != null ) {
                        mNotify.onNotify( WIFI_STATE_DISABLED );
                        mNotify.onWifiStateChanged( WIFI_STATE_DISABLED );
                    }
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    Log.d(TAG, "Wi-Fi is currently being disabled.");
                    if( mNotify != null ) {
                        mNotify.onNotify( WIFI_STATE_DISABLING );
                        mNotify.onWifiStateChanged( WIFI_STATE_DISABLING );
                    }
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.d(TAG, "Wi-Fi is enabled.");
                    if( mNotify != null ) {
                        mNotify.onNotify( WIFI_STATE_ENABLED );
                        mNotify.onWifiStateChanged( WIFI_STATE_ENABLED );
                    }
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    Log.d(TAG, "Wi-Fi is currently being enabled.");
                    if( mNotify != null ) {
                        mNotify.onNotify( WIFI_STATE_ENABLING );
                        mNotify.onWifiStateChanged( WIFI_STATE_ENABLING );
                    }
                    break;
            }
        }

        if( action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ) {
            boolean no_connectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,false);
            if( no_connectivity )
                Log.d(TAG, "The device has no connectivity at all.");
            else
                Log.d(TAG, "The device has connectivity.");
            if( mNotify != null ) {
                mNotify.onNotify( (no_connectivity) ? CONNECTIVITY_STATE_DISABLED : CONNECTIVITY_STATE_ENABLED);
                mNotify.onConnectivityChanged( no_connectivity );
            }
            /*
            Bundle extras = intent.getExtras();

            if (extras != null) {
                for (String key: extras.keySet()) {
                    Log.v(TAG, "key [" + key + "]: " +
                            extras.get(key));
                }
            }
            else {
                Log.v(TAG, "no extras");
            }
            */

        }

    }
}
