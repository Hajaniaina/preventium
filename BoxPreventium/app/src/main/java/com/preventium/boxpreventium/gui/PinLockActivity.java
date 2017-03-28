package com.preventium.boxpreventium.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
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

    private PinLockView mPinLockView;
    private Intent settingsIntent;
    private String pinCode;

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pinlock);

        settingsIntent = new Intent(PinLockActivity.this, SettingsActivity.class);
        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        IndicatorDots mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setVerticalScrollBarEnabled(false);

        mPinLockView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                return (motionEvent.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        pinCode = sharedPref.getString(getString(R.string.pin_code_key), "1234");

        PinLockListener mPinLockListener = new PinLockListener() {

            @Override
            public void onComplete (String pin) {

                if (pin.equals(pinCode)) {

                    startActivity(settingsIntent);
                    finish();
                }
                else {

                    mPinLockView.resetPinLockView();

                    if (getCurrentFocus() != null) {

                        Snackbar.make(getCurrentFocus(), getString(R.string.pin_invalid_string), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                }
            }

            @Override
            public void onEmpty() {}

            @Override
            public void onPinChange (int pinLength, String intermediatePin) {}
        };

        mPinLockView.setPinLockListener(mPinLockListener);
    }
}
