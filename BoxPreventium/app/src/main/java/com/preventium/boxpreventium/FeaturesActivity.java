package com.preventium.boxpreventium;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.preventium.boxpreventium.utils.CommonUtils;

/**
 * Created by Franck on 29/06/2016.
 */

public class FeaturesActivity extends Activity  implements View.OnClickListener {

    private Button buttonRequestAll;
    private Button buttonRequestLocation;
    private Button buttonRequestBluetooth;
    private Button buttonStateLocation;
    private Button buttonStateBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.features);

        buttonRequestAll = (Button)findViewById(R.id.buttonRequestAll);
        buttonRequestLocation = (Button)findViewById(R.id.buttonRequestLocation);
        buttonRequestBluetooth = (Button)findViewById(R.id.buttonRequestBluetooth);
        buttonStateLocation = (Button)findViewById(R.id.buttonStateLocation);
        buttonStateBluetooth = (Button)findViewById(R.id.buttonStateBluetooth);

        buttonRequestAll.setOnClickListener( this );
        buttonRequestLocation.setOnClickListener( this );
        buttonRequestBluetooth.setOnClickListener( this );

        updateText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateText();
    }

    @Override
    public void onClick(View view) {
        switch ( view.getId() ) {
            case R.id.buttonRequestAll:
                if( !CommonUtils.haveBluetoothEnabled() || !CommonUtils.haveLocationEnabled( FeaturesActivity.this ) ) {
                    if( isFinishing() ) return;
                    CommonUtils.showSettingDialog( FeaturesActivity.this,
                            R.string.dialog_features_settings,
                            R.string.dialog_features_message );
                } else {
                    updateText();
                }
                break;
            case R.id.buttonRequestLocation:
                if( !CommonUtils.haveLocationEnabled( FeaturesActivity.this ) ) {
                    if( isFinishing() ) return;
                    CommonUtils.showLocationSettingDialog( FeaturesActivity.this );
                } else {
                    updateText();
                }
                break;
            case R.id.buttonRequestBluetooth:
                if( !CommonUtils.haveBluetoothEnabled() ) {
                    if( isFinishing() ) return;
                    CommonUtils.showBluetoothSettingDialog( FeaturesActivity.this );
                } else {
                    updateText();
                }
                break;
        }
    }

    private void updateText() {
        boolean enable = CommonUtils.haveLocationEnabled( FeaturesActivity.this );
        buttonStateLocation.setText(enable
                ? R.string.state_enable_feedback
                : R.string.state_disable_feedback);
        buttonStateLocation.setTextColor( ContextCompat.getColor(this, enable ? R.color.state_enable : R.color.state_disable) );

        enable = CommonUtils.haveBluetoothEnabled();
        buttonStateBluetooth.setText(enable
                ? R.string.state_enable_feedback
                : R.string.state_disable_feedback);
        buttonStateBluetooth.setTextColor( ContextCompat.getColor(this, enable ? R.color.state_enable : R.color.state_disable) );
    }
}
