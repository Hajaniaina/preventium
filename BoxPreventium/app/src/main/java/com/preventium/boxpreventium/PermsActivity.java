package com.preventium.boxpreventium;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ikalogic.franck.permissions.MultiplePermissionsReport;
import com.ikalogic.franck.permissions.Perm;
import com.ikalogic.franck.permissions.PermActivity;
import com.ikalogic.franck.permissions.PermissionToken;
import com.ikalogic.franck.permissions.listener.PermissionDeniedResponse;
import com.ikalogic.franck.permissions.listener.PermissionGrantedResponse;
import com.ikalogic.franck.permissions.listener.PermissionRequest;
import com.ikalogic.franck.permissions.listener.multi.CompositeMultiplePermissionsListener;
import com.ikalogic.franck.permissions.listener.multi.MultiplePermissionsListener;
import com.ikalogic.franck.permissions.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener;
import com.ikalogic.franck.permissions.listener.single.CompositePermissionListener;
import com.ikalogic.franck.permissions.listener.single.PermissionListener;
import com.ikalogic.franck.permissions.listener.single.SnackbarOnDeniedPermissionListener;

import java.util.List;

/**
 * Created by Franck on 29/06/2016.
 */

public class PermsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "PermsActivity";

    private ViewGroup rootView;

    private Button buttonRequestAll;
    private Button buttonRequestPhone;
    private Button buttonRequestSms;
    private Button buttonRequestStorage;
    private Button buttonRequestLocation;
    private Button buttonStatePhone;
    private Button buttonStateSms;
    private Button buttonStateStorage;
    private Button buttonStateLocation;

    private MultiplePermissionsListener allPermissionsListener;
    private PermissionListener phonePermissionListener;
    private PermissionListener smsPermissionListener;
    private PermissionListener storagePermissionListener;
    private PermissionListener locationPermissionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perms);

        rootView = (ViewGroup)findViewById(android.R.id.content);
        buttonRequestAll = (Button)findViewById(R.id.buttonRequestAll);
        buttonRequestPhone = (Button)findViewById(R.id.buttonRequestPhone);
        buttonRequestSms = (Button)findViewById(R.id.buttonRequestSms);
        buttonRequestStorage = (Button)findViewById(R.id.buttonRequestStorage);
        buttonRequestLocation = (Button)findViewById(R.id.buttonRequestLocation);
        buttonStatePhone = (Button)findViewById(R.id.buttonStatePhone);
        buttonStateSms = (Button)findViewById(R.id.buttonStateSms);
        buttonStateStorage = (Button)findViewById(R.id.buttonStateStorage);
        buttonStateLocation = (Button)findViewById(R.id.buttonStateLocation);

        buttonRequestAll.setOnClickListener( this );
        buttonRequestPhone.setOnClickListener( this );
        buttonRequestSms.setOnClickListener( this );
        buttonRequestStorage.setOnClickListener( this );
        buttonRequestLocation.setOnClickListener( this );

        createPermissionListeners();
        Perm.continuePendingRequestsIfPossible( allPermissionsListener );

        buttonRequestAll.callOnClick();
    }

    private void createPermissionListeners() {

        PermissionListener feedbackViewPermissionListener = new PermActivityPermissionListener(this);
        MultiplePermissionsListener feedbackViewMultiplePermissionListener =
                new PermActivityMultiplePermissionListener(this);

        allPermissionsListener =
                new CompositeMultiplePermissionsListener(feedbackViewMultiplePermissionListener,
                        SnackbarOnAnyDeniedMultiplePermissionsListener.Builder.with(rootView,
                                R.string.all_permissions_denied_feedback)
                                .withOpenSettingsButton(R.string.permission_rationale_settings_button_text)
                                .build());

        phonePermissionListener = new CompositePermissionListener(feedbackViewPermissionListener,
                SnackbarOnDeniedPermissionListener.Builder.with(rootView,
                        R.string.phone_permission_denied_dialog_feedback)
                        .withOpenSettingsButton(R.string.permission_rationale_settings_button_text)
                        .withCallback(new Snackbar.Callback() {
                            @Override
                            public void onShown(Snackbar snackbar) {
                                super.onShown(snackbar);
                            }
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                            }
                        })
                        .build());

        smsPermissionListener = new CompositePermissionListener(feedbackViewPermissionListener,
                SnackbarOnDeniedPermissionListener.Builder.with(rootView,
                        R.string.sms_permission_denied_dialog_feedback)
                        .withOpenSettingsButton(R.string.permission_rationale_settings_button_text)
                        .withCallback(new Snackbar.Callback() {
                            @Override
                            public void onShown(Snackbar snackbar) {
                                super.onShown(snackbar);
                            }
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                            }
                        })
                        .build());

        storagePermissionListener = new CompositePermissionListener(feedbackViewPermissionListener,
                SnackbarOnDeniedPermissionListener.Builder.with(rootView,
                        R.string.storage_permission_denied_dialog_feedback)
                        .withOpenSettingsButton(R.string.permission_rationale_settings_button_text)
                        .withCallback(new Snackbar.Callback() {
                            @Override
                            public void onShown(Snackbar snackbar) {
                                super.onShown(snackbar);
                            }
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                            }
                        })
                        .build());

        locationPermissionListener = new CompositePermissionListener(feedbackViewPermissionListener,
                SnackbarOnDeniedPermissionListener.Builder.with(rootView,
                        R.string.location_permission_denied_dialog_feedback)
                        .withOpenSettingsButton(R.string.permission_rationale_settings_button_text)
                        .withCallback(new Snackbar.Callback() {
                            @Override
                            public void onShown(Snackbar snackbar) {
                                super.onShown(snackbar);
                            }
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                            }
                        })
                        .build());
    }

    public void showPermissionGranted(String permissionName) {
        Button feedbackView = getFeedbackViewForPermission(permissionName);
        if( feedbackView == null ) return;
        feedbackView.setText(R.string.permission_granted_feedback);
        feedbackView.setTextColor(ContextCompat.getColor(this, R.color.permission_granted));
    }

    public void showPermissionDenied(String permissionName, boolean permanentlyDenied) {
        Button feedbackView = getFeedbackViewForPermission(permissionName);
        if( feedbackView == null ) return;
        feedbackView.setText(permanentlyDenied
                ? R.string.permission_permanently_denied_feedback
                : R.string.permission_denied_feedback);
        feedbackView.setTextColor(ContextCompat.getColor(this, R.color.permission_denied));
    }

    private Button getFeedbackViewForPermission(String name) {
        Button feedbackView = null;
        switch (name) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                feedbackView = buttonStateLocation;
                break;
            case Manifest.permission.READ_PHONE_STATE:
                feedbackView = buttonStatePhone;
                break;
            case Manifest.permission.SEND_SMS:
                feedbackView = buttonStateSms;
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                feedbackView = buttonStateStorage;
                break;
            default:
                feedbackView = null;
                break;
        }
        return feedbackView;
    }

    public void showPermissionRationale(final PermissionToken token) {
        if( isFinishing() ) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_message)
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
                });
        builder.create().show();

    }

    @Override
    public void onClick(View view) {
        switch ( view.getId() ) {
            case R.id.buttonRequestAll:
                if( Perm.isRequestOngoing() ) return;
                Perm.checkPermissions(allPermissionsListener,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_PHONE_STATE);
                break;
            case R.id.buttonRequestLocation:
                if (Perm.isRequestOngoing()) return;
                Perm.checkPermission(locationPermissionListener, Manifest.permission.ACCESS_FINE_LOCATION);
                break;
            case R.id.buttonRequestStorage:
                if (Perm.isRequestOngoing()) return;
                Perm.checkPermission(storagePermissionListener, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
            case R.id.buttonRequestPhone:
                if (Perm.isRequestOngoing()) return;
                Perm.checkPermission(phonePermissionListener, Manifest.permission.READ_PHONE_STATE);
                break;
            case R.id.buttonRequestSms:
                if (Perm.isRequestOngoing()) return;
                Perm.checkPermission(smsPermissionListener, Manifest.permission.SEND_SMS);
                break;
        }
    }

    protected class PermActivityPermissionListener implements PermissionListener {

        private final PermsActivity activity;

        public PermActivityPermissionListener(PermsActivity activity) {
            this.activity = activity;
        }

        @Override public void onPermissionGranted(PermissionGrantedResponse response) {
            activity.showPermissionGranted(response.getPermissionName());
        }

        @Override public void onPermissionDenied(PermissionDeniedResponse response) {
            activity.showPermissionDenied(response.getPermissionName(), response.isPermanentlyDenied());
        }

        @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                 PermissionToken token) {
            activity.showPermissionRationale(token);
        }
    }

    protected class PermActivityMultiplePermissionListener implements MultiplePermissionsListener {

        private final PermsActivity activity;

        public PermActivityMultiplePermissionListener(PermsActivity activity) {
            this.activity = activity;
        }

        @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
            for (PermissionGrantedResponse response : report.getGrantedPermissionResponses()) {
                activity.showPermissionGranted(response.getPermissionName());
            }
            for (PermissionDeniedResponse response : report.getDeniedPermissionResponses()) {
                activity.showPermissionDenied(response.getPermissionName(), response.isPermanentlyDenied());
            }
        }

        @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                 PermissionToken token) {
            activity.showPermissionRationale(token);
        }
    }
}


