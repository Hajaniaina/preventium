package com.preventium.boxpreventium.module;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ikalogic.franck.bluetooth.BluetoothListAdapter;
import com.ikalogic.franck.bluetooth.BluetoothScanner;
import com.ikalogic.franck.bluetooth.scanner.ScannerCallback;
import com.ikalogic.franck.bluetooth.scanner.SnackbarOnBluetoothAdapter;
import com.preventium.boxpreventium.R;

import java.util.ArrayList;

/**
 * Created by Franck on 20/07/2016.
 */

public class ScanActivity extends Activity
    implements View.OnClickListener, AdapterView.OnItemClickListener, ScannerCallback {

    private static final String TAG = "ScanActivity";

    private BluetoothScanner scanner;
    private BluetoothListAdapter bluetoothlistAdapter;
    private Button buttonStart, buttonStop, buttonQuit, buttonApply;
    private ProgressBar progressBar;
    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scan);

        scanner = new BluetoothScanner(this,this);
        buttonStart = (Button)findViewById(R.id.btnStart);
        buttonStop = (Button)findViewById(R.id.btnStop);
        buttonQuit = (Button)findViewById(R.id.btnQuit);
        buttonApply = (Button)findViewById(R.id.btnApply);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        listview = (ListView)findViewById(R.id.listView);
        bluetoothlistAdapter = new BluetoothListAdapter(this.getLayoutInflater());
        bluetoothlistAdapter.setCheckable( true );
        listview.setAdapter(bluetoothlistAdapter);
        progressBar.setVisibility( View.INVISIBLE );
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);

        buttonStart.setOnClickListener( this );
        buttonStop.setOnClickListener( this );
        buttonQuit.setOnClickListener( this );
        buttonApply.setOnClickListener( this );
        listview.setOnItemClickListener( this );

        if( !BluetoothScanner.hasBluetooth() ) {
            Toast.makeText(this, com.ikalogic.franck.bluetooth.R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if( !BluetoothScanner.hasBluetoothLE(ScanActivity.this) ) {
            Toast.makeText(this, com.ikalogic.franck.bluetooth.R.string.bluetooth_low_energy_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        buttonStart.callOnClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.stopLeScan();
    }

    @Override
    public void onClick(View view) {
        switch ( view.getId() ) {
            case R.id.btnStart: {
                scanner.startLeScan();
            } break;
            case R.id.btnStop: {
                scanner.stopLeScan();
            } break;
            case R.id.btnQuit: {
                scanner.stopLeScan();
                ScanActivity.this.setResult(RESULT_CANCELED);
                ScanActivity.this.finish();
            } break;
            case R.id.btnApply: {
                scanner.stopLeScan();
                ArrayList<BluetoothDevice> devices = bluetoothlistAdapter.getCheckedDevices();

                if( devices.size() < 1 ) {
                    ViewGroup rootView = (ViewGroup)findViewById(android.R.id.content);
                    SnackbarOnBluetoothAdapter snackbar = SnackbarOnBluetoothAdapter.Builder.with(rootView, R.string.bluetooth_selected_inf).build();
                    snackbar.showSnackbar();
                } else if( devices.size() > 2 ) {
                    ViewGroup rootView = (ViewGroup)findViewById(android.R.id.content);
                    SnackbarOnBluetoothAdapter snackbar = SnackbarOnBluetoothAdapter.Builder.with(rootView, R.string.bluetooth_selected_sup).build();
                    snackbar.showSnackbar();
                } else {
                    Log.d(TAG,"Checked devices count: " + devices.size() );
                    for( BluetoothDevice device: devices ) Log.d(TAG,device.getName());
                }
                //ScanActivity.this.setResult(RESULT_CANCELED);
                //ScanActivity.this.finish();
            } break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//        scanner.stopLeScan();
//        final BluetoothDevice device = bluetoothlistAdapter.getDevice( position );
//        Log.d( TAG, "Select device " + device.getName() + "(" + device.getAddress() + ")" );
//        Intent intent = ScanActivity.this.getIntent();
//        intent.putExtra( "device", device );
//        ScanActivity.this.setResult(RESULT_OK,intent);
//        ScanActivity.this.finish();
    }

    @Override
    public void onScanState(final boolean scanning) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if( scanning ) {
                    bluetoothlistAdapter.clear();
                    bluetoothlistAdapter.notifyDataSetChanged();
                    progressBar.setVisibility( View.VISIBLE );
                } else {
                    progressBar.setVisibility( View.INVISIBLE );
                }
                buttonStart.setEnabled( !scanning );
                buttonStop.setEnabled( scanning );
            }
        });
    }

    @Override
    public void onScanResult(final BluetoothDevice device, final int rssi) {
        //Log.d(TAG,"Finding device: " + device.getName() + " rssi: " + rssi);
        //if( device.getName().startsWith("PREVENTIUM") ) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bluetoothlistAdapter.addDevice( device, rssi );
                    bluetoothlistAdapter.notifyDataSetChanged();
                }
            });
        //}
    }

    @Override
    public void onBluetoothAdapterIsDisable() {
        ViewGroup rootView = (ViewGroup)findViewById(android.R.id.content);
        SnackbarOnBluetoothAdapter snackbar = SnackbarOnBluetoothAdapter.Builder.with(rootView, com.ikalogic.franck.bluetooth.R.string.bluetooth_disable).withEnableBluetoothButton(com.ikalogic.franck.bluetooth.R.string.enable).build();
        snackbar.showSnackbar();
    }

    @Override
    public void onBluetoothAdapterIsNull() {
        ViewGroup rootView = (ViewGroup)findViewById(android.R.id.content);
        SnackbarOnBluetoothAdapter snackbar = SnackbarOnBluetoothAdapter.Builder.with(rootView, com.ikalogic.franck.bluetooth.R.string.bluetooth_not_supported).build();
        snackbar.showSnackbar();
    }

    @Override
    public void onLocationServiceIsDisable() {
        ViewGroup rootView = (ViewGroup)findViewById(android.R.id.content);
        SnackbarOnBluetoothAdapter snackbar = SnackbarOnBluetoothAdapter.Builder.with(rootView, com.ikalogic.franck.bluetooth.R.string.location_disable).withEnableLocationButton(com.ikalogic.franck.bluetooth.R.string.enable).build();
        snackbar.showSnackbar();
    }

}
