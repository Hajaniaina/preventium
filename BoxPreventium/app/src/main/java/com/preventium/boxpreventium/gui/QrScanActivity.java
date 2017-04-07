package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture;
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic;
import com.google.android.gms.vision.barcode.Barcode;
import com.preventium.boxpreventium.R;
import java.util.List;
import xyz.belvi.mobilevisionbarcodescanner.BarcodeRetriever;

public class QrScanActivity extends AppCompatActivity implements BarcodeRetriever {

    private static final String TAG = "QrScanActivity";

    public static final String QR_SCAN_REQUEST_PARAM = "QR_REQUEST";
    public static final String SCAN_DRIVER_PREFIX    = "DRIVER";
    public static final String SCAN_VEHICLE_PREFIX   = "VEHICLE";
    public static final String SCAN_VEHICLE_FRONT    = "FRONT";
    public static final String SCAN_VEHICLE_BACK     = "BACK";

    public static final String SCAN_MODE_VEHICLE_DISABLED   = "0";
    public static final String SCAN_MODE_VEHICLE_FRONT      = "1";
    public static final String SCAN_MODE_VEHICLE_FRONT_BACK = "2";

    private Intent returnIntent;
    private AppColor appColor;
    private BarcodeCapture barcodeCapture;
    private boolean flashOn = false;
    private boolean scannedOnce = false;
    private QrScanRequest qrRequest;
    private SharedPreferences sharedPref;

    private CheckBox checkBoxDriverId;
    private CheckBox checkBoxVehicleFront;
    private CheckBox checkBoxVehicleBack;

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        appColor = new AppColor(getApplicationContext());
        returnIntent = getIntent();
        qrRequest = returnIntent.getParcelableExtra(QR_SCAN_REQUEST_PARAM);
        setResult(Activity.RESULT_CANCELED, returnIntent);

        checkBoxDriverId = (CheckBox) findViewById(R.id.checkbox_driver_id);
        checkBoxVehicleFront = (CheckBox) findViewById(R.id.checkbox_vehicle_front);
        checkBoxVehicleBack = (CheckBox) findViewById(R.id.checkbox_vehicle_back);

        if (!qrRequest.driverIdEnabled) {

            checkBoxDriverId.setVisibility(View.GONE);
        }

        if (!qrRequest.vehicleFrontOnStartEnabled) {

            checkBoxVehicleFront.setVisibility(View.GONE);
        }

        if (!qrRequest.vehicleBackOnStartEnabled) {

            checkBoxVehicleBack.setVisibility(View.GONE);
        }

        scannedOnce = false;
        updateCheckBoxes();

        barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.qr_scanner);
        barcodeCapture.setTouchAsCallback(false);
        barcodeCapture.setRetrieval(QrScanActivity.this);

        FloatingActionButton flashButton = (FloatingActionButton) findViewById(R.id.button_qr_flash);
        flashButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                if (flashOn) {

                    flashOn = false;
                }
                else {

                    flashOn = true;
                }

                barcodeCapture.setShowFlash(flashOn);
                barcodeCapture.refresh();
            }
        });

        FloatingActionButton closeButton = (FloatingActionButton) findViewById(R.id.button_qr_close);
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                onBackPressed();
            }
        });
    }

    @Override
    public void onRetrieved (final Barcode barcode) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                String code = barcode.displayValue;

                if (code.startsWith(SCAN_VEHICLE_PREFIX)) {

                    if (code.contains(SCAN_VEHICLE_FRONT)) {

                        if (qrRequest.vehicleFrontOnStartEnabled) {

                            if (qrRequest.vehicleFrontReq == QrScanRequest.REQUEST_PENDING) {

                                qrRequest.vehicleFrontReq = QrScanRequest.REQUEST_COMPLETED;
                                showConfirmDialog(getString(R.string.scan_qr_ok_string), true);
                            }
                        }
                    }
                    else if (code.contains(SCAN_VEHICLE_BACK)) {

                        if (qrRequest.vehicleBackOnStartEnabled) {

                            if (qrRequest.vehicleBackReq == QrScanRequest.REQUEST_PENDING) {

                                qrRequest.vehicleBackReq = QrScanRequest.REQUEST_COMPLETED;
                                showConfirmDialog(getString(R.string.scan_qr_ok_string), true);
                            }
                        }
                    }
                    else {

                        showConfirmDialog(getString(R.string.scan_qr_error_string), false);
                    }
                }
                else {

                    if (!scannedOnce) {

                        scannedOnce = true;

                        String subStrings[] = code.split("/");
                        long driverId = parseDriverId(subStrings[0]);
                        String driverName = subStrings[1];

                        if (driverId > 0) {

                            if (qrRequest.driverIdReq == QrScanRequest.REQUEST_PENDING) {

                                qrRequest.driverIdReq = QrScanRequest.REQUEST_COMPLETED;
                            }

                            qrRequest.driverId = driverId;
                            qrRequest.driverName = driverName;

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putLong(getString(R.string.driver_id_key), driverId);
                            editor.putString(getString(R.string.driver_name_key), driverName);
                            editor.apply();

                            String name[] = driverName.split(" ");
                            String hello = getString(R.string.hello_string) + " " + name[0];
                            showConfirmDialog(hello, true);
                        }
                        else {

                            showConfirmDialog(getString(R.string.scan_qr_error_string), false);
                        }
                    }
                }

                updateCheckBoxes();
                returnIntent.putExtra(QR_SCAN_REQUEST_PARAM, qrRequest);
                setResult(Activity.RESULT_OK, returnIntent);
            }
        });
    }

    @Override
    public void onRetrievedMultiple (final Barcode closetToClick, final List<BarcodeGraphic> barcodeGraphics) {}

    @Override
    public void onRetrievedFailed (String reason) {}

    @Override
    public void onBitmapScanned (SparseArray<Barcode> sparseArray) {}

    private void showConfirmDialog (String msg, boolean ok) {

        int themeId;

        if (ok) {

            themeId = R.style.PositiveDialogStyle;
        }
        else {

            themeId = R.style.NegativeDialogStyle;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, themeId);

        builder.setTitle("");
        builder.setMessage(msg);

        final AlertDialog dialog = builder.show();

        new CountDownTimer(1500, 1500) {

            public void onTick (long millisUntilFinished) {}

            public void onFinish() {

                if (dialog.isShowing()) {

                    dialog.dismiss();
                }
            }

        }.start();
    }

    private long parseDriverId (String str) {

        try {

            long num = Long.parseLong(str);

            if (num > 0) {

                return num;
            }
        }
        catch (NumberFormatException e)
        {
            return -1;
        }

        return 0;
    }

    private void updateCheckBoxes() {

        if (qrRequest.driverIdReq == QrScanRequest.REQUEST_PENDING) {

            checkBoxDriverId.setChecked(false);
            checkBoxDriverId.setTextColor(appColor.getColor(AppColor.RED));
        }
        else {

            checkBoxDriverId.setChecked(true);
            checkBoxDriverId.setTextColor(appColor.getColor(AppColor.GREEN));
        }

        if (qrRequest.vehicleFrontOnStartEnabled) {

            if (qrRequest.vehicleFrontReq == QrScanRequest.REQUEST_PENDING) {

                checkBoxVehicleFront.setChecked(false);
                checkBoxVehicleFront.setTextColor(appColor.getColor(AppColor.RED));
            }
            else {

                checkBoxVehicleFront.setChecked(true);
                checkBoxVehicleFront.setTextColor(appColor.getColor(AppColor.GREEN));
            }
        }

        if (qrRequest.vehicleBackOnStartEnabled) {

            if (qrRequest.vehicleBackReq == QrScanRequest.REQUEST_PENDING) {

                checkBoxVehicleBack.setChecked(false);
                checkBoxVehicleBack.setTextColor(appColor.getColor(AppColor.RED));
            }
            else {

                checkBoxVehicleBack.setChecked(true);
                checkBoxVehicleBack.setTextColor(appColor.getColor(AppColor.GREEN));
            }
        }
    }
}
