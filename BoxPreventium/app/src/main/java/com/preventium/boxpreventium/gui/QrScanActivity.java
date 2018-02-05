package com.preventium.boxpreventium.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture;
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic;
import com.google.android.gms.vision.barcode.Barcode;
import com.preventium.boxpreventium.R;

import java.util.List;

import xyz.belvi.mobilevisionbarcodescanner.BarcodeRetriever;

public class QrScanActivity extends AppCompatActivity implements BarcodeRetriever {
    public static final String QR_SCAN_REQUEST_PARAM = "QR_REQUEST";
    public static final String SCAN_MODE_DRIVER_DISABLED   = "0";
    public static final String SCAN_MODE_VEHICLE_DISABLED = "0";
    public static final String SCAN_MODE_VEHICLE_FRONT = "1";
    public static final String SCAN_MODE_VEHICLE_FRONT_BACK = "2";
    public static final String SCAN_VEHICLE_BACK = "ARRIERE";
    public static final String SCAN_VEHICLE_FRONT = "AVANT";
    private static final String TAG = "QrScanActivity";
    private AppColor appColor;
    private BarcodeCapture barcodeCapture;
    private CheckBox checkBoxDriverId;
    private CheckBox checkBoxVehicleBack;
    private CheckBox checkBoxVehicleFront;
    private boolean flashOn = false;
    private QrScanRequest qrRequest;
    private Intent returnIntent;
    private boolean scannedOnce = false;
    private SharedPreferences sharedPref;

    class C00541 implements OnClickListener {
        C00541() {
        }

        public void onClick(View view) {
            if (QrScanActivity.this.flashOn) {
                QrScanActivity.this.flashOn = false;
            } else {
                QrScanActivity.this.flashOn = true;
            }
            QrScanActivity.this.barcodeCapture.setShowFlash(QrScanActivity.this.flashOn);
            //onRetrieved(barcodeCapture);
            QrScanActivity.this.barcodeCapture.refresh();
        }
    }

    class C00552 implements OnClickListener {
        C00552() {
        }

        public void onClick(View view) {
            QrScanActivity.this.onBackPressed();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);
        this.returnIntent = getIntent();
        this.qrRequest = (QrScanRequest) this.returnIntent.getParcelableExtra(QR_SCAN_REQUEST_PARAM);
        setResult(0, this.returnIntent);
        this.appColor = new AppColor(getApplicationContext());
        this.checkBoxDriverId = (CheckBox) findViewById(R.id.checkbox_driver_id);
        this.checkBoxVehicleFront = (CheckBox) findViewById(R.id.checkbox_vehicle_front);
        this.checkBoxVehicleBack = (CheckBox) findViewById(R.id.checkbox_vehicle_back);
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String scanModeOnStart = this.sharedPref.getString(getString(R.string.qr_select_start_mode_key), SCAN_MODE_VEHICLE_DISABLED);
        String scanModeOnStop = this.sharedPref.getString(getString(R.string.qr_select_stop_mode_key), SCAN_MODE_VEHICLE_DISABLED);
        String scanModeIc = this.sharedPref.getString(getString(R.string.qr_select_ic_mode_key), SCAN_MODE_DRIVER_DISABLED);

        if (scanModeOnStart.equals(SCAN_MODE_VEHICLE_FRONT_BACK)) {
            this.qrRequest.vehicleFrontOnStartEnabled = true;
            this.qrRequest.vehicleBackOnStartEnabled = true;
        } else {
            this.qrRequest.vehicleFrontOnStartEnabled = false;
            this.qrRequest.vehicleBackOnStartEnabled = false;
        }
        if (scanModeOnStart.equals(SCAN_MODE_VEHICLE_FRONT)) {
            this.qrRequest.vehicleFrontOnStartEnabled = true;
        }
        if (scanModeOnStop.equals(SCAN_MODE_VEHICLE_FRONT_BACK)) {
            this.qrRequest.vehicleFrontOnStopEnabled = true;
            this.qrRequest.vehicleBackOnStopEnabled = true;
        } else {
            this.qrRequest.vehicleFrontOnStopEnabled = false;
            this.qrRequest.vehicleBackOnStopEnabled = false;
        }
        if (scanModeOnStop.equals(SCAN_MODE_VEHICLE_FRONT)) {
            this.qrRequest.vehicleFrontOnStopEnabled = true;
        }
        if (scanModeIc.equals(SCAN_MODE_DRIVER_DISABLED)) {
            Log.d(TAG, "qr_select_ic_mode_key: "+scanModeIc);
            this.qrRequest.driverIdEnabled = false;
        } else {

            this.qrRequest.driverIdEnabled = true;
        }

        if (!this.qrRequest.vehicleFrontOnStartEnabled) {
            this.checkBoxVehicleFront.setVisibility(View.GONE);
        }
        if (!this.qrRequest.vehicleBackOnStartEnabled) {
            this.checkBoxVehicleBack.setVisibility(View.GONE);
        }


        if (!this.qrRequest.driverIdEnabled) {
            this.checkBoxDriverId.setVisibility(View.GONE);
        }

        this.scannedOnce = false;
        updateCheckBoxes();
        this.barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.qr_scanner);
        this.barcodeCapture.setTouchAsCallback(false);
        this.barcodeCapture.setRetrieval(this);
        ((FloatingActionButton) findViewById(R.id.button_qr_flash)).setOnClickListener(new C00541());
        ((FloatingActionButton) findViewById(R.id.button_qr_close)).setOnClickListener(new C00552());
    }

    public void onRetrieved(final Barcode barcode) {
        runOnUiThread(new Runnable() {
            public void run() {
                String code = barcode.displayValue;
                if (code.startsWith(QrScanActivity.SCAN_VEHICLE_FRONT) || code.startsWith(QrScanActivity.SCAN_VEHICLE_BACK)) {
                    if (code.startsWith(QrScanActivity.SCAN_VEHICLE_FRONT)) {
                        if (QrScanActivity.this.qrRequest.vehicleFrontOnStartEnabled && QrScanActivity.this.qrRequest.vehicleFrontReq == 0) {
                            QrScanActivity.this.qrRequest.vehicleFrontReq = 1;
                            QrScanActivity.this.showConfirmDialog(QrScanActivity.this.getString(R.string.scan_qr_ok_string), true);
                        }
                    } else if (!code.startsWith(QrScanActivity.SCAN_VEHICLE_BACK)) {
                        QrScanActivity.this.showConfirmDialog(QrScanActivity.this.getString(R.string.scan_qr_error_string), false);
                    } else if (QrScanActivity.this.qrRequest.vehicleBackOnStartEnabled && QrScanActivity.this.qrRequest.vehicleBackReq == 0) {
                        QrScanActivity.this.qrRequest.vehicleBackReq = 1;
                        QrScanActivity.this.showConfirmDialog(QrScanActivity.this.getString(R.string.scan_qr_ok_string), true);
                    }
                } else if (!QrScanActivity.this.scannedOnce) {
                    QrScanActivity.this.scannedOnce = true;

                    if (code.contains("/")) {
                        if (QrScanActivity.this.qrRequest.driverIdEnabled ) {
                            String[] subStrings = code.split("/");
                            long driverId = QrScanActivity.this.parseDriverId(subStrings[0]);
                            if (driverId > 0) {
                                String driverName = subStrings[1];
                                if (QrScanActivity.this.qrRequest.driverIdReq == 0) {
                                    QrScanActivity.this.qrRequest.driverIdReq = 1;
                                }
                                QrScanActivity.this.qrRequest.driverId = driverId;
                                QrScanActivity.this.qrRequest.driverName = driverName;
                                Editor editor = QrScanActivity.this.sharedPref.edit();
                                editor.putLong(QrScanActivity.this.getString(R.string.driver_id_key), driverId);
                                editor.putString(QrScanActivity.this.getString(R.string.driver_name_key), driverName);
                                editor.apply();

                                Log.e("Drivename za : ", String.valueOf(driverName));
                                Log.e("DriveId za : ", String.valueOf(driverId));

                                QrScanActivity.this.showConfirmDialog(QrScanActivity.this.getString(R.string.hello_string) + " " + driverName.split(" ")[1], true);

                            } else {
                                QrScanActivity.this.showConfirmDialog(QrScanActivity.this.getString(R.string.scan_qr_error_string), false);
                            }

                        }
                    } else {
                        QrScanActivity.this.showConfirmDialog(QrScanActivity.this.getString(R.string.scan_qr_error_string), false);
                    }
                }
                QrScanActivity.this.updateCheckBoxes();
                QrScanActivity.this.returnIntent.putExtra(QrScanActivity.QR_SCAN_REQUEST_PARAM, QrScanActivity.this.qrRequest);
                QrScanActivity.this.setResult(-1, QrScanActivity.this.returnIntent);
            }
        });
    }

    public void onRetrievedMultiple(Barcode closetToClick, List<BarcodeGraphic> list) {
    }

    public void onRetrievedFailed(String reason) {
    }

    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
    }

    private void showConfirmDialog(String msg, boolean ok) {
        int themeId;
        if (ok) {
            themeId = R.style.PositiveDialogStyle;
        } else {
            themeId = R.style.NegativeDialogStyle;
        }
        Builder builder = new Builder(this, themeId);
        builder.setTitle("");
        builder.setMessage(msg);
        final AlertDialog dialog = builder.show();
        new CountDownTimer(1500, 1500) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }.start();
    }

    private long parseDriverId(String str) {
        try {
            long num = Long.parseLong(str);
            if (num > 0) {
                return num;
            }
            return 0;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void updateCheckBoxes() {
        if (this.qrRequest.driverIdReq == 0) {
            this.checkBoxDriverId.setChecked(false);
            this.checkBoxDriverId.setTextColor(this.appColor.getColor(0));
        } else {
            this.checkBoxDriverId.setChecked(true);
            this.checkBoxDriverId.setTextColor(this.appColor.getColor(4));
        }
        if (this.qrRequest.vehicleFrontOnStartEnabled) {
            if (this.qrRequest.vehicleFrontReq == 0) {
                this.checkBoxVehicleFront.setChecked(false);
                this.checkBoxVehicleFront.setTextColor(this.appColor.getColor(0));
            } else {
                this.checkBoxVehicleFront.setChecked(true);
                this.checkBoxVehicleFront.setTextColor(this.appColor.getColor(4));
            }
        }
        if (!this.qrRequest.vehicleBackOnStartEnabled) {
            return;
        }
        if (this.qrRequest.vehicleBackReq == 0) {
            this.checkBoxVehicleBack.setChecked(false);
            this.checkBoxVehicleBack.setTextColor(this.appColor.getColor(0));
            return;
        }
        this.checkBoxVehicleBack.setChecked(true);
        this.checkBoxVehicleBack.setTextColor(this.appColor.getColor(4));
    }
}
