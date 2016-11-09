package com.preventium.boxpreventium.gui;

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
    private BarcodeCapture barcodeCapture;
    private boolean flashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.qr_scanner);
        barcodeCapture.setRetrieval(this);
        barcodeCapture.setTouchAsCallback(false);

        FloatingActionButton flashButton = (FloatingActionButton) findViewById(R.id.flash_button);
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
    }

    @Override
    public void onRetrieved (final Barcode barcode) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                final AlertDialog.Builder builder = new AlertDialog.Builder(QrScanActivity.this);
                builder.setTitle("");
                builder.setMessage(barcode.displayValue);
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
}