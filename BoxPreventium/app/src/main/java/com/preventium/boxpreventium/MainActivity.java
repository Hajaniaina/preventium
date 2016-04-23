package com.preventium.boxpreventium;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ikalogic.franck.permissions.Perm;
import com.ikalogic.franck.permissions.PermissionToken;
import com.ikalogic.franck.permissions.listener.multi.CompositeMultiplePermissionsListener;
import com.ikalogic.franck.permissions.listener.multi.MultiplePermissionsListener;
import com.ikalogic.franck.permissions.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener;
import com.preventium.boxpreventium.camera.AutoFitTextureView;
import com.preventium.boxpreventium.camera.Camera;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private CompositeMultiplePermissionsListener allPermissionsListener;

    private FtpPreventium a;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        createPermissionListeners();
        Perm.continuePendingRequestsIfPossible(allPermissionsListener);
        checkPermissions();
        //a = new FtpPreventium();
        //a.start();
    }


    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(msg);
            smsManager.sendMultipartTextMessage(phoneNo, null, parts, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    private void createPermissionListeners() {
        ViewGroup rootView = (ViewGroup)findViewById(android.R.id.content);
        MultiplePermissionsListener feedbackViewMultiplePermissionListener = new MainMultiplePermissionListener(this);
        allPermissionsListener = new CompositeMultiplePermissionsListener(feedbackViewMultiplePermissionListener,
                SnackbarOnAnyDeniedMultiplePermissionsListener.Builder.with(rootView, "R.string.all_permissions_denied_feedback")
                        .withOpenSettingsButton("R.string.permission_rationale_settings_button_text")
                        .build());
    }


    public void showPermissionRationale(final PermissionToken token) {
        new AlertDialog.Builder(this).setTitle("R.string.permission_rationale_title")
                .setMessage("R.string.permission_rationale_message")
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.cancelPermissionRequest();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.continuePermissionRequest();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override public void onDismiss(DialogInterface dialog) {
                        token.cancelPermissionRequest();
                    }
                })
                .show();
    }

    private void checkPermissions() {
        Log.d("AA","AA");
        if (Perm.isRequestOngoing()) {
            return;
        }
        Log.d("AA","BB");
        String permissions[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN};
        Perm.checkPermissions(allPermissionsListener, permissions);
    }
}

