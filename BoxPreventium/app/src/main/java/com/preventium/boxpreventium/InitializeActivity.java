package com.preventium.boxpreventium;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Franck on 29/06/2016.
 */

public class InitializeActivity extends Activity implements InitializeThread.DataTransfertCallBack {

    private InitializeThread thread;

    private GifView gifview;
    private TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initialize);

        gifview = (GifView) findViewById(R.id.GifView);
        textview = (TextView) findViewById(R.id.textView);

        thread = new InitializeThread(this,this);
        thread.start();
    }

    @Override
    public void onThreadStatusChanged(ThreadDefault.ThreadStatus status) {

    }

    @Override
    public void onCheckingProgress(InitializeThread.CheckingFlag flag, InitializeThread.CheckingState status) {


        switch ( status ) {
            case CHECHING: {

            } break;
            case TRUE: {

            } break;
            case FALSE: {

            } break;
        }

        switch ( flag ) {
        case PERMISSIONS: updateProgressText( R.string.checking_permissions ); break;
        case BLUETOOTH_SUPPORT: updateProgressText( R.string.checking_bluetooth_support ); break;
        case BLUETOOTH_LE_SUPPORT: updateProgressText( R.string.checking_bluetooth_le_support ); break;
        case LOCATION_SUPPORT: updateProgressText( R.string.checking_location_support ); break;
        case BLUETOOTH_ENABLED: updateProgressText( R.string.checking_bluetooth_enabled ); break;
        case LOCATION_ENABLED: updateProgressText( R.string.checking_location_enabled ); break;
        case INTERNET_ENABLED: updateProgressText( R.string.checking_internet_enabled ); break;
        case CONFIG_READY: updateProgressText( R.string.checking_config_ready ); break;
        }
    }

    private void updateProgressText(final int resId ) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textview.setText( getString(resId) );
            }
        });
    }
}
