package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture;
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic;
import com.google.android.gms.vision.barcode.Barcode;
import com.preventium.boxpreventium.R;
import java.util.List;
import xyz.belvi.mobilevisionbarcodescanner.BarcodeRetriever;

public class QrScanActivity extends AppCompatActivity implements BarcodeRetriever {

    private static final String TAG = "QrScanActivity";

    private static final int QR_CODE_REQUEST = 0;

    private static final int SCAN_DRIVER_ID = 1;
    private static final int SCAN_VEHICLE   = 2;

    public static final String SCAN_DRIVER_PREFIX  = "DRIVER";
    public static final String SCAN_VEHICLE_PREFIX = "VEHICLE";

    private Intent returnIntent;
    private BarcodeCapture barcodeCapture;
    private boolean flashOn = false;
    private int scanMode = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        returnIntent = getIntent();
        setResult(Activity.RESULT_CANCELED, returnIntent);

        barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.qr_scanner);
        barcodeCapture.setTouchAsCallback(false);

        FloatingActionButton scanDriverButton = (FloatingActionButton) findViewById(R.id.button_qr_driver);
        scanDriverButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                scanMode = SCAN_DRIVER_ID;
                barcodeCapture.setRetrieval(QrScanActivity.this);
            }
        });

        FloatingActionButton scanVehicleButton = (FloatingActionButton) findViewById(R.id.button_qr_vehicle);
        scanVehicleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                scanMode = SCAN_VEHICLE;
                barcodeCapture.setRetrieval(QrScanActivity.this);
            }
        });

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

                    String msg = barcode.displayValue;

                    if (scanMode == SCAN_DRIVER_ID) {

                        if (msg.startsWith(SCAN_DRIVER_PREFIX)) {

                            showConfirmDialog(getString(R.string.scan_qr_ok_string));

                            returnIntent.putExtra(SCAN_DRIVER_PREFIX, msg.substring(msg.lastIndexOf(":") + 1));
                            setResult(Activity.RESULT_OK, returnIntent);
                        }
                        else {

                            showConfirmDialog(getString(R.string.scan_qr_error_string));
                        }
                    }
                    else if (scanMode == SCAN_VEHICLE) {

                        if (msg.startsWith(SCAN_VEHICLE_PREFIX)) {

                            showConfirmDialog(getString(R.string.scan_qr_ok_string));

                            returnIntent.putExtra(SCAN_DRIVER_PREFIX, msg.substring(msg.lastIndexOf(":") + 1));
                            setResult(Activity.RESULT_OK, returnIntent);
                        }
                        else

                            showConfirmDialog(getString(R.string.scan_qr_error_string));
                        }
                    }

                    barcodeCapture.setRetrieval(null);
                    barcodeCapture.refresh();
            }
        });
    }

    @Override
    public void onRetrievedMultiple (final Barcode closetToClick, final List<BarcodeGraphic> barcodeGraphics) {

    }

    @Override
    public void onBitmapScanned (SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onRetrievedFailed (String reason) {

    }

    private void showConfirmDialog (String msg) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(QrScanActivity.this);
        builder.setTitle("");
        builder.setMessage(msg);
        final AlertDialog dialog = builder.show();

        new CountDownTimer(2000, 2000) {

            public void onTick (long millisUntilFinished) {}

            public void onFinish() {

                if (dialog.isShowing()) {

                    dialog.dismiss();
                }
            }

        }.start();
    }
}
