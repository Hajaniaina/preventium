package com.preventium.boxpreventium;

import android.util.Log;

import com.ikalogic.franck.permissions.MultiplePermissionsReport;
import com.ikalogic.franck.permissions.PermissionToken;
import com.ikalogic.franck.permissions.listener.PermissionDeniedResponse;
import com.ikalogic.franck.permissions.listener.PermissionGrantedResponse;
import com.ikalogic.franck.permissions.listener.PermissionRequest;
import com.ikalogic.franck.permissions.listener.multi.MultiplePermissionsListener;

import java.util.List;

/**
 * Created by Franck on 17/06/2016.
 */

public class MainMultiplePermissionListener implements MultiplePermissionsListener {

    private MainActivity activity;

    public MainMultiplePermissionListener(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onPermissionsChecked(MultiplePermissionsReport report) {
        for (PermissionGrantedResponse response : report.getGrantedPermissionResponses()) {
            //activity.showPermissionGranted(response.getPermissionName());
        }

        for (PermissionDeniedResponse response : report.getDeniedPermissionResponses()) {
            //activity.showPermissionDenied(response.getPermissionName(), response.isPermanentlyDenied());
        }
    }

    @Override
    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
        activity.showPermissionRationale(permissionToken);
    }
}
