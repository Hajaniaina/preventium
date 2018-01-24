package com.preventium.boxpreventium.utils.superclass.bluetooth;

import android.annotation.SuppressLint;
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
import com.preventium.boxpreventium.utils.superclass.bluetooth.scanner.SnackbarOnBluetoothAdapter.Builder;

@SuppressLint("Registered")
public class ScanActivity extends Activity implements ScannerCallback {
    private BluetoothListAdapter bluetoothlistAdapter;
    private ProgressBar progressBar;
    private BluetoothScanner scanner;

    /*
    class C01361 implements OnClickListener {
        C01361() {
        }

        @Override
        public void onClick(View view) {
            ScanActivity.this.scanner.startLeScan();
        }
    }

    class C01372 implements OnClickListener {
        C01372() {
        }

        @Override
        public void onClick(View view) {
            ScanActivity.this.scanner.stopLeScan();
        }
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_activity_item_model);
        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        Button buttonStop = (Button) findViewById(R.id.buttonStop);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ListView listview = (ListView) findViewById(R.id.listView);
        this.bluetoothlistAdapter = new BluetoothListAdapter(getLayoutInflater());
        listview.setAdapter(this.bluetoothlistAdapter);
        this.progressBar.setVisibility(View.GONE);
        //this.buttonStart.setOnClickListener(new C01361());
        //this.buttonStop.setOnClickListener(new C01372());

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scanner.startLeScan();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanner.stopLeScan();
            }
        });


        this.scanner = new BluetoothScanner(this, this);
        if (!BluetoothScanner.hasBluetooth()) {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        } else if (!BluetoothScanner.hasBluetoothLE(this)) {
            Toast.makeText(this, R.string.bluetooth_low_energy_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onScanState(boolean scanning) {
        if (scanning) {
            this.bluetoothlistAdapter.clear();
            this.progressBar.setVisibility(View.VISIBLE);
            return;
        }
        this.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onScanResult(BluetoothDevice device, int rssi) {
        this.bluetoothlistAdapter.addDevice(device, rssi);
    }

    @Override
    public void onBluetoothAdapterIsDisable() {
        //Builder.with((ViewGroup) findViewById(16908290), (int) R.string.bluetooth_disable).withEnableBluetoothButton((int) R.string.enable).build().showSnackbar();
        Builder.with((ViewGroup) findViewById(android.R.id.content), (int) R.string.bluetooth_disable).withEnableBluetoothButton((int) R.string.enable).build().showSnackbar();

    }

    @Override
    public void onBluetoothAdapterIsNull() {
        //Builder.with((ViewGroup) findViewById(16908290), (int) R.string.bluetooth_not_supported).build().showSnackbar();
        Builder.with((ViewGroup) findViewById(android.R.id.content), (int) R.string.bluetooth_not_supported).build().showSnackbar();

    }

    @Override
    public void onLocationServiceIsDisable() {
        Builder.with((ViewGroup) findViewById(android.R.id.content), (int) R.string.location_disable).withEnableLocationButton((int) R.string.enable).build().showSnackbar();
    }
}
