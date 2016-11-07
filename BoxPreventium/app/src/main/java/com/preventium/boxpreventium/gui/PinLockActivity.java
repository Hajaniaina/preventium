package com.preventium.boxpreventium.gui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.preventium.boxpreventium.R;

public class PinLockActivity extends AppCompatActivity {

    public static final String TAG = "PinLockActivity";
    public static final String DEFAULT_PIN_CODE = "0000";

    private PinLockView mPinLockView;
    private ImageView lockImageView;
    private String pinCode = DEFAULT_PIN_CODE;
    private Intent settingsIntent;
    private AppColor appColor;

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pinlock);

        appColor = new AppColor(this);
        settingsIntent = new Intent(PinLockActivity.this, SettingsActivity.class);
        lockImageView = (ImageView) findViewById(R.id.lock_image);
        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        IndicatorDots mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        mPinLockView.attachIndicatorDots(mIndicatorDots);

        PinLockListener mPinLockListener = new PinLockListener() {

            @Override
            public void onComplete (String pin) {

                if (pin.equals(pinCode)) {

                    startActivity(settingsIntent);
                    finish();
                }
                else {

                    mPinLockView.resetPinLockView();
                    lockImageView.setColorFilter(Color.RED);

                    Snackbar.make(getCurrentFocus(), getString(R.string.pin_error_string) + ": " + pin, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }

            @Override
            public void onEmpty() {}

            @Override
            public void onPinChange (int pinLength, String intermediatePin) {}
        };

        mPinLockView.setPinLockListener(mPinLockListener);
    }

    public void setPinCode (String strPin) {

        pinCode = strPin;
    }
}
