package com.preventium.boxpreventium.gui;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firetrap.permissionhelper.action.OnDenyAction;
import com.firetrap.permissionhelper.action.OnGrantAction;
import com.firetrap.permissionhelper.helper.PermissionHelper;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.STATUS_t;
import com.preventium.boxpreventium.enums.SPEED_t;
import com.preventium.boxpreventium.location.CustomMarker;
import com.preventium.boxpreventium.location.MarkerManager;
import com.preventium.boxpreventium.location.PositionManager;
import com.preventium.boxpreventium.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.preventium.boxpreventium.manager.AppManager;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.Connectivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, AppManager.AppManagerListener {

    private static final String TAG = "MainActivity";

    private static final int MAP_ZOOM_ON_MOVE = 17;
    private static final int MAP_ZOOM_ON_PAUSE = 15;
    private static final int QR_REQUEST_ID = 0;
    private static final int QR_REQUEST_PENDING_TMR = 0;

    private PositionManager posManager;
    private MarkerManager markerManager;
    private ScoreView scoreView;
    private SpeedView speedView;
    private AccForceView accForceView;
    private AppManager appManager;

    private TextView debugView;
    private TextView boxNumView;
    private TextView drivingTimeView;
    private ImageView backgroundView;

    private FloatingActionMenu optMenu;
    private FloatingActionButton infoButton;
    private FloatingActionButton callButton;
    private FloatingActionButton scanQrCodeButton;
    private FloatingActionButton settingsButton;
    private FloatingActionButton stopButton;
    private FloatingActionButton menuButton1;
    private FloatingActionButton menuButton2;
    private FloatingActionButton menuButton3;

    private GoogleMap googleMap;
    private LatLng lastPos;
    private Location lastLocation;
    private AppColor appColor;
    private ProgressDialog progress;
    private boolean toggle = false;
    private SupportMapFragment mapFrag;
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;
    private boolean startMarkerAdded = false;
    private boolean initDone = false;
    private boolean actionCanceled = false;
    private boolean mapReady = false;
    private boolean permissionsChecked = false;
    private Intent pinLockIntent;
    private SharedPreferences sharedPreferences;
    private PermissionHelper.PermissionBuilder permissionRequest;
    private QrScanRequest qrRequest;
    private boolean parcourActive = false;
    private boolean parcourPause = false;

    // --------------------------------------------------------------------------------------------//

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d(TAG, "onCreate");

        if (checkPermissions() > 0) {

            if (savedInstanceState == null) {

                init(true);
            }
            else {

                init(false);
            }
        }
        else {

            requestPermissions();
        }
    }

    @Override
    protected void onResume() {

        Log.d(TAG, "onResume");

        if (!PositionManager.isLocationEnabled(getApplicationContext())) {

            showLocationAlert();
        }

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {

            BluetoothAdapter.getDefaultAdapter().enable();
        }

        int permissionsGranted = checkPermissions();

        if (permissionsGranted > 0) {

            if (!initDone) {

                init(true);
            }
        }
        else if (permissionsGranted == 0) {

            requestPermissions();
        }

        super.onResume();
    }

    @Override
    public void onPause() {

        Log.d(TAG, "onPause");

        if (progress != null) {

            progress.dismiss();
        }

        super.onPause();
    }

    @Override
    public void onStop() {

        permissionsChecked = false;
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {

        // outState.putParcelable("accForceView", accForceView);
        // outState.putParcelable("scoreView", scoreView);
        // outState.putParcelable("speedView", speedView);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult");
        permissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {

        if (requestCode == QR_REQUEST_ID) {

            if (resultCode == RESULT_OK) {

                if (data.hasExtra(QrScanActivity.QR_SCAN_REQUEST_PARAM)) {

                    qrRequest = data.getParcelableExtra(QrScanActivity.QR_SCAN_REQUEST_PARAM);

                    if (qrRequest.driverIdReq == QrScanRequest.REQUEST_COMPLETED) {

                        appManager.set_driver_id(qrRequest.driverId);
                    }

                    if (qrRequest.vehicleFrontReq == QrScanRequest.REQUEST_COMPLETED) {

                    }

                    if (qrRequest.vehicleBackReq == QrScanRequest.REQUEST_COMPLETED) {

                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------//

    @Override
    public void onMapReady (GoogleMap googleMap) {

        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        this.googleMap.getUiSettings().setAllGesturesEnabled(true);

        mapReady = true;

        if (progress != null && progress.isShowing()) {

            progress.setMessage(getString(R.string.progress_location_string));
        }

        posManager = new PositionManager(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            return;
        }

        this.googleMap.setMyLocationEnabled(true);
        markerManager.setMap(this.googleMap);

        setMapListeners();
        setButtonListeners();
        setPositionListeners();
    }

    // --------------------------------------------------------------------------------------------//

    @Override
    public void onStatusChanged (final STATUS_t status) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                switch (status) {

                    case GETTING_CFG:

                        if (progress != null) {

                            progress.show();

                            if (Connectivity.isConnected(getApplicationContext())) {

                                progress.setMessage(getString(R.string.progress_cfg_string));
                            }
                            else {

                                progress.setMessage(getString(R.string.network_alert_string));
                            }
                        }

                        break;

                    case GETTING_EPC:

                        if (progress != null) {

                            progress.show();
                            progress.setMessage(getString(R.string.progress_epc_string));
                        }

                        break;

                    case GETTING_DOBJ:

                        if (progress != null) {

                            progress.show();
                            progress.setMessage(getString(R.string.progress_obj_string));
                        }

                        break;

                    case PAR_STARTED:

                        parcourActive = true;
                        parcourPause = false;

                        speedView.setText(SPEED_t.IN_STRAIGHT_LINE, "RUN");

                        if (mapReady) {

                            googleMap.getUiSettings().setAllGesturesEnabled(false);
                        }

                        stopButton.setVisibility(View.GONE);

                        changeViewColorFilter(drivingTimeView, AppColor.GREEN);

                        boolean qrRequestPending = false;

                        if (qrRequest.driverIdReq == QrScanRequest.REQUEST_PENDING) {

                            qrRequestPending = true;
                        }

                        if (qrRequest.vehicleFrontEnabled) {

                            if (qrRequest.vehicleFrontReq == QrScanRequest.REQUEST_PENDING) {

                                qrRequestPending = true;
                            }
                        }

                        if (qrRequest.vehicleBackEnabled) {

                            if (qrRequest.vehicleBackReq == QrScanRequest.REQUEST_PENDING) {

                                qrRequestPending = true;
                            }
                        }

                        if (qrRequestPending) {

                            appManager.add_ui_timer(15, QR_REQUEST_PENDING_TMR);
                        }

                        break;

                    case PAR_RESUME:

                        parcourActive = true;
                        parcourPause = false;

                        speedView.setText(SPEED_t.IN_STRAIGHT_LINE, "RESUME");

                        if (mapReady) {

                            googleMap.getUiSettings().setAllGesturesEnabled(false);
                        }

                        stopButton.setVisibility(View.GONE);

                        changeViewColorFilter(drivingTimeView, AppColor.GREEN);

                        break;

                    case PAR_PAUSING:

                        parcourActive = true;
                        parcourPause = true;

                        speedView.setText(SPEED_t.IN_STRAIGHT_LINE, "PAUSE");

                        if (mapReady) {

                            CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPos).zoom(MAP_ZOOM_ON_PAUSE).bearing(0).tilt(0).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            googleMap.getUiSettings().setAllGesturesEnabled(true);
                        }

                        stopButton.setVisibility(View.VISIBLE);

                        changeViewColorFilter(drivingTimeView, AppColor.ORANGE);

                        accForceView.hide(true);

                        break;

                    case PAR_STOPPED:

                        parcourActive = false;
                        parcourPause = false;

                        appManager.clear_ui_timer();

                        stopButton.setVisibility(View.GONE);

                        speedView.setText(SPEED_t.IN_STRAIGHT_LINE, "STOP");
                        changeViewColorFilter(drivingTimeView, AppColor.GREY);

                        if (progress != null) {

                            progress.setMessage(getString(R.string.progress_ready_string));
                            progress.hide();
                        }

                        break;
                }
            }
        });
    }

    @Override
    public void onNumberOfBoxChanged (final int nb) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (nb > 0) {

                    if (nb > 1) {

                        changeViewColorFilter(boxNumView, AppColor.GREEN);
                    }
                    else {

                        changeViewColorFilter(boxNumView, AppColor.BLUE);
                    }
                }
                else {

                    changeViewColorFilter(boxNumView, AppColor.RED);
                }

                boxNumView.setText(String.valueOf(nb));
            }
        });
    }

    @Override
    public void onDrivingTimeChanged (final String txt) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                drivingTimeView.setText(txt);
            }
        });
    }

    @Override
    public void onForceChanged (final FORCE_t type, final LEVEL_t level) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (parcourActive && !parcourPause) {

                    if (type != FORCE_t.UNKNOW) {

                        accForceView.hide(false);
                        accForceView.setValue(type, level);
                    }
                    else {

                        accForceView.hide(true);
                    }
                }
            }
        });
    }

    @Override
    public void onDriveScoreChanged (float score) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onCustomMarkerDataListGet() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                appManager.setCustomMarkerDataList(markerManager.getUserMarkersData());
            }
        });
    }

    @Override
    public void onParcoursTypeGet() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                appManager.set_parcours_type(null);
            }
        });
    }

    @Override
    public void onUiTimeout (final int timer_id, final STATUS_t status) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (timer_id == QR_REQUEST_PENDING_TMR) {

                    if (parcourActive) {

                        drawAttention(5);
                        showQrRequestAlert();
                    }
                }
            }
        });
    }

    @Override
    public void onDebugLog (final String txt) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (txt.isEmpty()) {

                    debugView.setVisibility(View.INVISIBLE);

                }
                else {

                    debugView.setVisibility(View.VISIBLE);
                }

                debugView.setText(txt);
            }
        });
    }

    // --------------------------------------------------------------------------------------------//

    private void init (boolean firstLaunch) {

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mapReady = false;
        initDone = true;
        appColor = new AppColor(this);
        pinLockIntent = new Intent(MainActivity.this, PinLockActivity.class);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        appManager = new AppManager(this, this);
        qrRequest = new QrScanRequest();

        progress = new ProgressDialog(this, R.style.InfoDialogStyle);
        progress.setMessage(getString(R.string.progress_map_string));
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setProgressNumberFormat(null);
        progress.setProgressPercentFormat(null);

        if (!isFinishing()) {

            progress.show();
        }

        markerManager = new MarkerManager(this);
        speedView = new SpeedView(this);
        scoreView = new ScoreView(this);
        accForceView = new AccForceView(this);
        accForceView.hide(true);

        boxNumView = (TextView) findViewById(R.id.box_num_connected);
        drivingTimeView = (TextView) findViewById(R.id.driving_time_text);
        changeViewColorFilter(drivingTimeView, AppColor.ORANGE);

        backgroundView = (ImageView) findViewById(R.id.background_image);
        debugView = (TextView) findViewById(R.id.debug_view);
        debugView.setVisibility(View.INVISIBLE);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (firstLaunch) {

            mapFrag.setRetainInstance(true);
        }

        mapFrag.getMapAsync(this);
    }

    private int checkPermissions() {

        int ok = 1;

        Log.d(TAG, "Check permissions");

        if (!permissionsChecked) {

            permissionsChecked = true;

            if (!PermissionHelper.checkPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ok = 0;
            }

            if (!PermissionHelper.checkPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                ok = 0;
            }

            if (!PermissionHelper.checkPermissions(this, Manifest.permission.CALL_PHONE)) {

                ok = 0;
            }

            if (!PermissionHelper.checkPermissions(this, Manifest.permission.CAMERA)) {

                ok = 0;
            }
        }
        else {

            ok = -1;
        }

        return ok;
    }

    private void requestPermissions() {

        permissionRequest = PermissionHelper.with(this)
                .build(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                       Manifest.permission.ACCESS_FINE_LOCATION,
                       Manifest.permission.CALL_PHONE,
                       Manifest.permission.SEND_SMS,
                       Manifest.permission.CAMERA).onPermissionsDenied(new OnDenyAction() {

                    @Override
                    public void call (int requestCode, boolean shouldShowRequestPermissionRationale) {

                        Log.d(TAG, "Permissions denied");

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

                        Log.d(TAG, "Permissions granted");

                        if (!initDone) {

                            init(true);
                        }
                    }
                })
                .request(0);
    }

    public void showPermissionsAlert (final boolean retry) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(getString(R.string.permissions_string));

        String msg = "";
        String btnName = "";

        if (retry) {

            msg = getString(R.string.permissions_enable_string);
            btnName = getString(R.string.permissions_retry_string);
        }
        else {

            msg = getString(R.string.force_permissions_string);
            btnName = getString(R.string.action_settings);
        }

        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton(btnName, new DialogInterface.OnClickListener() {

            public void onClick (DialogInterface dialog,int which) {

                if (retry) {

                    permissionRequest.retry();
                }
                else {

                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                }
            }
        });

        alertDialog.show();
    }

    public void showLocationAlert() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setCancelable(false);
        alertDialog.setTitle(getString(R.string.location_settings_string));
        alertDialog.setMessage(getString(R.string.location_rationale_string));

        alertDialog.setPositiveButton(getString(R.string.action_settings), new DialogInterface.OnClickListener() {

            public void onClick (DialogInterface dialog,int which) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.show();
    }

    public void showQrRequestAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.NegativeDialogStyle);

        builder.setCancelable(true);
        builder.setMessage(getString(R.string.qr_rationale_string));

        final AlertDialog dialog = builder.show();

        new CountDownTimer(15000, 15000) {

            public void onTick (long millisUntilFinished) {}

            public void onFinish() {

                if (dialog.isShowing()) {

                    dialog.dismiss();
                }
            }

        }.start();
    }

    public void askEndDayConfirmation () {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage(getString(R.string.end_day_confirm_string));
        builder.setCancelable(false);

        builder.setNegativeButton(getString(R.string.no_string), null);
        builder.setPositiveButton(getString(R.string.yes_string), new DialogInterface.OnClickListener() {

            @Override
            public void onClick (DialogInterface dialogInterface, int i) {

                appManager.setStopped();
            }
        });

        builder.show();
    }

    protected void showCallDialog() {

        // String syncConnPref = sharedPreferences.getString("phone_select_sms", "default");
        // Snackbar.make(getCurrentFocus(), syncConnPref, Snackbar.LENGTH_LONG).setAction("Action", null).show();

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setMessage(getString(R.string.make_call_confirma_string));
        alertBuilder.setCancelable(false);
        alertBuilder.setNegativeButton(getString(R.string.cancel_string), null);
        alertBuilder.setPositiveButton(getString(R.string.call_string), new DialogInterface.OnClickListener() {

            @Override
            public void onClick (DialogInterface dialogInterface, int i) {

                makeCall("0623141536");
            }
        });

        alertBuilder.create().show();

        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        Drawable drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_phone_black_24p);
        int color = ContextCompat.getColor(MainActivity.this, R.color.material_teal500);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        builder.setIcon(drawable);
        builder.setTitle("  " + getString(R.string.make_call_string));

        final CharSequence[] callLsit = new CharSequence[5];

        for (int i = 0; i < callLsit.length; i++) {

            callLsit[i] = getString(R.string.number_string) + " " + String.valueOf(i + 1);
        }

        builder.setSingleChoiceItems(callLsit, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick (DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton(getString(R.string.call_string), new DialogInterface.OnClickListener() {

            @Override
            public void onClick (DialogInterface dialog, int id) {

            }
        });

        builder.setNegativeButton(getString(R.string.cancel_string), null);

        AlertDialog alertDlg = builder.create();
        alertDlg.show();
        */
    }

    protected void showMarkerEditDialog (final Marker marker, final boolean creation) {

        final CustomMarker customMarker = markerManager.getMarker(marker);

        if (customMarker == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.marker_edit_dialog, null));

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton(getString(R.string.cancel_string), null);

        if (!creation) builder.setNeutralButton(getString(R.string.delete_string), null);

        final AlertDialog alertDlg = builder.create();

        if (creation) alertDlg.setCancelable(false);

        alertDlg.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow (DialogInterface dialogInterface) {

                EditText editTitle = (EditText) alertDlg.findViewById(R.id.marker_title);
                CheckBox alarmCheckBox = (CheckBox) alertDlg.findViewById(R.id.marker_alarm_checkbox);
                Spinner markerTypeSpinner = (Spinner) alertDlg.findViewById(R.id.marker_type_spinner);
                final Spinner markerPerimeterSpinner = (Spinner) alertDlg.findViewById(R.id.marker_perimeter_spinner);
                markerPerimeterSpinner.setVisibility(View.GONE);

                final String titleBefore = customMarker.getTitle();
                editTitle.setText(titleBefore);

                if (!creation) {

                    if (customMarker.getType() == CustomMarker.MARKER_INFO) {

                        markerTypeSpinner.setSelection(0);
                    }
                    else {

                        markerTypeSpinner.setSelection(1);
                    }

                    if (customMarker.isAlertEnabled()) {

                        alarmCheckBox.setChecked(true);
                        markerPerimeterSpinner.setVisibility(View.VISIBLE);
                    }
                    else {

                        alarmCheckBox.setChecked(false);
                        markerPerimeterSpinner.setVisibility(View.GONE);
                    }

                    markerPerimeterSpinner.setSelection(customMarker.getPerimeterId());
                }

                markerTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {

                        if (position == 0) {

                            customMarker.setType(CustomMarker.MARKER_INFO);
                        }
                        else {

                            customMarker.setType(CustomMarker.MARKER_DANGER);
                        }
                    }

                    @Override
                    public void onNothingSelected (AdapterView<?> parent) {}
                });

                markerPerimeterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {

                        customMarker.setPerimeterById(position);
                    }

                    @Override
                    public void onNothingSelected (AdapterView<?> parent) {}
                });

                alarmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged (CompoundButton compoundButton, boolean b) {

                        if (b) {

                            customMarker.enableAlert(true);
                            markerPerimeterSpinner.setVisibility(View.VISIBLE);
                        }
                        else {

                            customMarker.enableAlert(false);
                            markerPerimeterSpinner.setVisibility(View.GONE);
                        }
                    }
                });

                Button btnOk = alertDlg.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnCancel = alertDlg.getButton(AlertDialog.BUTTON_NEGATIVE);
                Button btnDelete = alertDlg.getButton(AlertDialog.BUTTON_NEUTRAL);

                btnOk.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick (View view) {

                        EditText editTitle = (EditText) alertDlg.findViewById(R.id.marker_title);

                        assert editTitle != null;
                        String currTitle = editTitle.getText().toString();

                        if (titleBefore != currTitle) {

                            if (currTitle.length() > 0) {

                                customMarker.setTitle(currTitle);

                                // Refresh marker's title
                                marker.hideInfoWindow();
                                marker.showInfoWindow();

                                alertDlg.dismiss();
                            }
                            else {

                                editTitle.setError(getString(R.string.marker_tittle_invalid_string));
                            }
                        }
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick (View view) {

                        if (creation) {

                            markerManager.remove(customMarker);
                            marker.remove();
                        }

                        alertDlg.dismiss();
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick (View view) {

                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertBuilder.setMessage(getString(R.string.validate_marker_delete_string));

                        alertBuilder.setNegativeButton(getString(R.string.cancel_string), null);
                        alertBuilder.setPositiveButton(getString(R.string.delete_string), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick (DialogInterface dialogInterface, int i) {

                                markerManager.remove(customMarker);
                                marker.remove();
                                alertDlg.dismiss();
                            }
                        });

                        alertBuilder.create().show();
                    }
                });
            }
        });

        alertDlg.show();
    }

    private void changeViewColorFilter (View view, int color) {

        Drawable background = view.getBackground();
        background.setColorFilter(appColor.getColor(color), PorterDuff.Mode.SRC_ATOP);
        view.setBackground(background);
    }

    private void makeCall (String number) {

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number.trim()));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "CALL_PHONE permission disabled");
            return;
        }

        startActivity(intent);
    }

    private String getSmsHeader() {

        String str = "[";

        str += ComonUtils.getIMEInumber(getApplicationContext()) + " ";
        str += String.valueOf(qrRequest.driverId) + " ";

        if (lastLocation != null) {

            str += String.valueOf(lastLocation.getLatitude()) + " ";
            str += String.valueOf(lastLocation.getLongitude()) + " ";
            String date = new SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.FRANCE).format(lastLocation.getTime());
            str += date;
        }

        str += "]";
        return str;
    }

    private void sendSms (String phoneNumber, String msg) {

        /*
        try {

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, msg, null, null);
        }
        catch (Exception ex) {

            ex.printStackTrace();
        }
        */

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive (Context arg0, Intent arg1) {

                switch (getResultCode())
                {
                    case RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive (Context arg0, Intent arg1) {

                switch (getResultCode())
                {
                    case RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;

                    case RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, msg, sentPI, deliveredPI);
    }

    private void sendSos() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        actionCanceled = false;

        builder.setMessage(getString(R.string.send_sos_string) + " 5 sec");
        builder.setCancelable(false);

        builder.setNegativeButton(getString(R.string.cancel_string), new DialogInterface.OnClickListener() {

            @Override
            public void onClick (DialogInterface dialogInterface, int i) {

                actionCanceled = true;
            }
        });

        final AlertDialog dialog = builder.show();

        new CountDownTimer(5000, 1000) {

            int countdown = 5;

            public void onTick (long millisUntilFinished) {

                if (actionCanceled) {

                    cancel();
                    dialog.dismiss();
                }
                else {

                    dialog.setMessage(getString(R.string.send_sos_string) + " " + String.valueOf(countdown) + " sec");
                    countdown--;
                }
            }

            public void onFinish() {

                if (!actionCanceled) {

                    String msg = "SOS " + getSmsHeader();
                    sendSms("0623141536", msg);
                }

                if (dialog.isShowing()) {

                    dialog.dismiss();
                }
            }

        }.start();
    }

    private void drawLine (LatLng startPoint, LatLng endPoint) {

        PolylineOptions opt = new PolylineOptions();

        opt.width(3);
        opt.geodesic(true);
        opt.color(Color.rgb(0, 160, 255));
        opt.add(startPoint, endPoint);

        googleMap.addPolyline(opt);
    }

    private void disableActionButtons (boolean disable) {

        FloatingActionButton[] actionBtnArray = {infoButton, callButton, scanQrCodeButton, settingsButton};

        for (int i = 0; i < actionBtnArray.length; i++) {

            if (disable) {

                actionBtnArray[i].setEnabled(false);
            }
            else {

                actionBtnArray[i].setEnabled(true);
            }
        }
    }

    private void hideActionButtons (boolean hide) {

        FloatingActionButton[] actionBtnArray = {infoButton, callButton, scanQrCodeButton, settingsButton};

        for (int i = 0; i < actionBtnArray.length; i++) {

            if (hide) {

                actionBtnArray[i].hide(true);
            }
            else {

                actionBtnArray[i].show(true);
            }
        }
    }

    private void drawAttention (int seconds) {

        flashBackground(seconds);
        vibrate(seconds);
        beep(seconds);
    }

    private void flashBackground (int seconds) {

        int ms = seconds * 1000;

        new CountDownTimer(ms, 500) {

            public void onTick (long millisUntilFinished) {

                if (toggle) {

                    toggle = false;
                    backgroundView.setVisibility(View.VISIBLE);
                }
                else {

                    toggle = true;
                    backgroundView.setVisibility(View.INVISIBLE);
                }
            }

            public void onFinish() {

                backgroundView.setVisibility(View.GONE);
            }

        }.start();
    }

    private void beep (int seconds) {

        int ms = seconds * 1000;
        final ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        new CountDownTimer(ms, 500) {

            public void onTick (long millisUntilFinished) {

                tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            }

            public void onFinish() {}

        }.start();
    }

    private void vibrate (int seconds) {

        int ms = seconds * 1000;

        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(ms);
    }

    private void setMapListeners() {

        // MAP SHORT CLICK
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick (LatLng latLng) {

            }
        });

        // MAP LONG CLICK
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick (final LatLng latLng) {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setMessage(getString(R.string.validate_marker_create_string));

                alertBuilder.setNegativeButton(getString(R.string.cancel_string), null);
                alertBuilder.setPositiveButton(getString(R.string.create_string), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick (DialogInterface dialogInterface, int i) {

                        Marker marker = markerManager.addMarker("", latLng, CustomMarker.MARKER_INFO);
                        showMarkerEditDialog(marker, true);
                    }
                });

                alertBuilder.create().show();
            }
        });

        // MARKER CLICK
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick (Marker marker) {

                    return false;
                }
            }
        );

        // MARKER INFO WINDOW CLICK
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick (Marker marker) {

                CustomMarker customMarker = markerManager.getMarker(marker);

                if (customMarker.isEditable()) {

                    showMarkerEditDialog(marker, false);
                }
            }
        });

        // MARKER DRAG
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart (Marker marker) {

            }

            @Override
            public void onMarkerDrag (Marker marker) {

            }

            @Override
            public void onMarkerDragEnd (Marker marker) {

            }
        });
    }

    private void setPositionListeners() {

        posManager.setPositionChangedListener(new PositionManager.PositionListener() {

            @Override
            public void onRawPositionUpdate (Location location) {

                appManager.setLocation(location);

                lastLocation = location;
                lastPos = new LatLng(location.getLatitude(), location.getLongitude());

                if (!startMarkerAdded) {

                    startMarkerAdded = true;
                    markerManager.addMarker(getString(R.string.start_marker_string), lastPos, CustomMarker.MARKER_START);

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPos).zoom(MAP_ZOOM_ON_PAUSE).bearing(0).tilt(0).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    if (progress != null && progress.isShowing()) {

                        progress.setMessage(getString(R.string.progress_loading_string));
                    }
                }

                if (parcourActive) {

                    speedView.setSpeed(SPEED_t.IN_CORNERS, LEVEL_t.LEVEL_1, posManager.getInstantSpeed());

                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(lastPos));
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPos).zoom(MAP_ZOOM_ON_MOVE).bearing(0).tilt(30).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                else {

                    speedView.setSpeed(SPEED_t.IN_CORNERS, LEVEL_t.LEVEL_UNKNOW, 0);
                }
            }

            @Override
            public void onPositionUpdate (Location prevLoc, Location currLoc) {

                if (parcourActive) {

                    LatLng prevPos = new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude());
                    LatLng currPos = new LatLng(currLoc.getLatitude(), currLoc.getLongitude());

                    drawLine(prevPos, currPos);
                }
            }
        });
    }

    private void setButtonListeners() {

        infoButton = (FloatingActionButton) findViewById(R.id.button_info);
        callButton = (FloatingActionButton) findViewById(R.id.button_call);
        scanQrCodeButton = (FloatingActionButton) findViewById(R.id.button_qrcode);
        settingsButton = (FloatingActionButton) findViewById(R.id.button_settings);

        stopButton = (FloatingActionButton) findViewById(R.id.button_stop);
        stopButton.setVisibility(View.GONE);

        // INFO
        infoButton.setOnClickListener (new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });

        // CALL
        callButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                showCallDialog();
            }
        });

        // QR CODE
        scanQrCodeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (final View view) {

                Intent intent = new Intent(MainActivity.this, QrScanActivity.class);
                intent.putExtra(QrScanActivity.QR_SCAN_REQUEST_PARAM, qrRequest);
                startActivityForResult(intent, QR_REQUEST_ID);
            }
        });

        // SETTINGS
        settingsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (final View view) {

                // startActivity(pinLockIntent);
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        // STOP
        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                askEndDayConfirmation();
            }
        });

        // MENU BUTTON
        optMenu = (FloatingActionMenu) findViewById(R.id.opt_menu);
        optMenu.setClosedOnTouchOutside(false);

        optMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {

            @Override
            public void onMenuToggle(boolean opened) {

                if (optMenu.isOpened()) {

                    hideActionButtons(true);
                }
                else {

                    hideActionButtons(false);
                }
            }
        });

        menuButton1 = (FloatingActionButton) findViewById(R.id.menu_button1);
        menuButton2 = (FloatingActionButton) findViewById(R.id.menu_button2);
        menuButton3 = (FloatingActionButton) findViewById(R.id.menu_button3);

        menuButton1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                sendSos();

                /*
                if (mapType > GoogleMap.MAP_TYPE_HYBRID)
                {
                    mapType = GoogleMap.MAP_TYPE_NORMAL;
                }
                else
                {
                    mapType++;
                }

                googleMap.setMapType(mapType);
                */
            }
        });

        menuButton2.setLabelText(getString(R.string.disable_tracking_string));

        menuButton2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                if (posManager.isUpdatesEnabled()) {

                    posManager.enableUpdates(false);

                    menuButton2.setImageResource(R.drawable.ic_play);
                    menuButton2.setLabelText(getString(R.string.enable_tracking_string));
                }
                else {

                    posManager.enableUpdates(true);

                    menuButton2.setImageResource(R.drawable.ic_stop);
                    menuButton2.setLabelText(getString(R.string.disable_tracking_string));
                }
            }
        });

        menuButton3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                optMenu.close(true);
            }
        });
    }

    // --------------------------------------------------------------------------------------------//
}
