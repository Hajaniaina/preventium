package com.preventium.boxpreventium.module.Load;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.firetrap.permissionhelper.action.OnDenyAction;
import com.firetrap.permissionhelper.action.OnGrantAction;
import com.firetrap.permissionhelper.helper.PermissionHelper;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.gui.MainActivity;

/**
 * Created by tog on 28/11/2018.
 */

public class LoadPermission {

    private Context context;
    private boolean permissionsChecked;
    private MainActivity main;
    private PermissionHelper.PermissionBuilder permissionRequest;
    private boolean permission;

    public LoadPermission (Context context) {
        this.context = context;
        this.main = (MainActivity) context;
    }

    public boolean isPermission() {
        return permission;
    }

    public int checkPermissions() {
        int ok = 1;

        if (!permissionsChecked) {
            permissionsChecked = true;

            if (!PermissionHelper.checkPermissions(main, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ok = 0;
            }

            if (!PermissionHelper.checkPermissions(main, Manifest.permission.READ_PHONE_STATE)) {
                ok = 0;
            }

            if (!PermissionHelper.checkPermissions(main, Manifest.permission.CALL_PHONE)) {
                ok = 0;
            }

            if (!PermissionHelper.checkPermissions(main, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ok = 0;
            }

            if (!PermissionHelper.checkPermissions(main, Manifest.permission.SEND_SMS)) {
                ok = 0;
            }

            if (!PermissionHelper.checkPermissions(main, Manifest.permission.RECEIVE_SMS)) {
                ok = 0;
            }

            if (!PermissionHelper.checkPermissions(main, Manifest.permission.READ_SMS)) {
                ok = 0;
            }

            if (!PermissionHelper.checkPermissions(main, Manifest.permission.CAMERA)) {
                ok = 0;
            }
        }
        else {
            ok = -1;
        }

        return ok;
    }

    public void requestPermissions(final CallbackPermission callback) {
        boolean _return = false;
        permissionRequest = PermissionHelper.with(main)
                .build(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS)
                .onPermissionsDenied(new OnDenyAction() {
                    @Override
                    public void call (int requestCode, boolean shouldShowRequestPermissionRationale) {
                        if (shouldShowRequestPermissionRationale) {
                            showPermissionsAlert(true);
                        }
                        else {
                            showPermissionsAlert(false);
                        }
                    }
                })
                .onPermissionsGranted(new OnGrantAction() {
                    @Override
                    public void call (int requestCode) {
                        callback.onCall();
                    }
                })
                .request(0);

        // settings
        this.main.setPermissionRequest(permissionRequest);
    }

    public void showPermissionsAlert (final boolean retry) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(main.getString(R.string.permissions_string));

        String msg = "";
        String btnName = "";

        if (retry) {

            msg = main.getString(R.string.permissions_enable_string);
            btnName = main.getString(R.string.permissions_retry_string);
        }
        else {

            msg = main.getString(R.string.force_permissions_string);
            btnName = main.getString(R.string.action_settings);
        }

        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton(btnName, new DialogInterface.OnClickListener() {

            public void onClick (DialogInterface dialog,int which) {

                if (retry) {
                    permissionRequest.retry();
                }
                else {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", main.getPackageName(), null));
                    main.startActivity(intent);
                }
            }
        });

        alertDialog.show();
    }

    public interface CallbackPermission {
        void onCall();
    }
}
