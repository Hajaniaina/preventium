package com.preventium.boxpreventium.utils.superclass.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.utils.superclass.bluetooth.scanner.ScannerCallback;
import com.preventium.boxpreventium.utils.superclass.bluetooth.scanner.SnackbarOnBluetoothAdapter;

/**
 * Created by Franck on 08/08/2016.
 */

public class ScanActivity extends Activity implements ScannerCallback {

    private BluetoothListAdapter bluetoothlistAdapter;
    private BluetoothScanner scanner;
    private Button buttonStart, buttonStop;
    private ProgressBar progressBar;
    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_activity_item_model);

        buttonStart = (Button)findViewById(R.id.buttonStart);
        buttonStop = (Button)findViewById(R.id.buttonStop);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        listview = (ListView)findViewById(R.id.listView);

        bluetoothlistAdapter = new BluetoothListAdapter(this.getLayoutInflater());
        listview.setAdapter(bluetoothlistAdapter);

        progressBar.setVisibility( View.GONE );

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                List<ScanFilterCompat> filters = new ArrayList<ScanFilterCompat>();
//                filters.add( new ScanFilterCompat.Builder().setDeviceName("FPT").build() );
//                filters.add( new ScanFilterCompat.Builder().setDeviceName("FPTLC").build() );
//                ScanSettingsCompat settings = new ScanSettingsCompat.Builder().setScanMode(ScanSettingsCompat.SCAN_MODE_LOW_LATENCY).build();
//                scanner.startLeScan(filters,settings);
                scanner.startLeScan();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanner.stopLeScan();
            }
        });

        scanner = new BluetoothScanner(this,this);

        if( !BluetoothScanner.hasBluetooth() ) {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if( !BluetoothScanner.hasBluetoothLE(ScanActivity.this) ) {
            Toast.makeText(this, R.string.bluetooth_low_energy_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public void onScanState(boolean scanning) {
        if( scanning ) {
            bluetoothlistAdapter.clear();
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onScanResult(BluetoothDevice device, int rssi) {
        bluetoothlistAdapter.addDevice(device, rssi);
    }

    @Override
    public void onBluetoothAdapterIsDisable() {
        ViewGroup rootView = (ViewGroup)findViewById(android.R.id.content);
        SnackbarOnBluetoothAdapter.Builder.with(rootView, R.string.bluetooth_disable).withEnableBluetoothButton(R.string.enable).build().showSnackbar();
    }

    @Override
    public void onBluetoothAdapterIsNull() {
        ViewGroup rootView = (ViewGroup)findViewById(android.R.id.content);
        SnackbarOnBluetoothAdapter.Builder.with(rootView, R.string.bluetooth_not_supported).build().showSnackbar();
    }

    @Override
    public void onLocationServiceIsDisable() {
        ViewGroup rootView = (ViewGroup)findViewById(android.R.id.content);
        SnackbarOnBluetoothAdapter.Builder.with(rootView, R.string.location_disable).withEnableLocationButton(R.string.enable).build().showSnackbar();
    }
}
