package com.preventium.boxpreventium.gui;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.firetrap.permissionhelper.action.OnDenyAction;
import com.firetrap.permissionhelper.action.OnGrantAction;
import com.firetrap.permissionhelper.helper.PermissionHelper;
import com.google.android.gms.maps.model.Polyline;
import com.gregacucnik.EditableSeekBar;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.SCORE_t;
import com.preventium.boxpreventium.enums.STATUS_t;
import com.preventium.boxpreventium.enums.SPEED_t;
import com.preventium.boxpreventium.location.CustomMarker;
import com.preventium.boxpreventium.location.CustomMarkerData;
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
import com.preventium.boxpreventium.manager.StatsLastDriving;
import com.preventium.boxpreventium.server.EPC.DataEPC;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.Connectivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, AppManager.AppManagerListener {

    private static final String TAG = "MainActivity";

    private static final boolean DEBUG_UI_ON      = false;
    private static final boolean DEBUG_LOGVIEW_ON = false;

    private static final int MAP_ZOOM_ON_MOVE         = 17;
    private static final int MAP_ZOOM_ON_PAUSE        = 15;
    private static final int QR_REQUEST_ID            = 0;
    private static final int QR_CHECK_ON_START_TMR    = 0;
    private static final int QR_CHECK_ON_END_TMR      = 1;
    private static final int QR_SEND_ON_START_SMS_TMR = 2;
    private static final int QR_SEND_ON_END_SMS_TMR   = 3;
    private static final int QR_RESTART_REQ_TMR       = 4;
    private static final int PAUSE_NOTIFICATION_TMR   = 5;

    private PositionManager posManager;
    private MarkerManager markerManager;
    private ScoreView scoreView;
    private SpeedView speedView;
    private AccForceView accForceView;
    private AppManager appManager;

    private TextView debugView;
    private LinearLayout debugLayout;
    private TextView boxNumView;
    private TextView drivingTimeView;
    private ImageView backgroundView;

    private FloatingActionMenu optMenu;
    private FloatingActionButton infoButton;
    private FloatingActionButton callButton;
    private FloatingActionButton scanQrCodeButton;
    private FloatingActionButton epcSettingsButton;
    private FloatingActionButton stopButton;
    private FloatingActionButton menuButtonSos;
    private FloatingActionButton menuButtonResetCalib;
    private FloatingActionButton menuButtonTracking;
    private FloatingActionButton menuButtonSettings;

    private GoogleMap googleMap;
    private LatLng lastPos;
    private Location lastLocation = null;
    private AppColor appColor;
    private ProgressDialog progress;
    private boolean toggle = false;
    List<Polyline> mapPolylineList = new ArrayList<>();
    private Marker startMarker = null, stopMarker = null;
    private boolean initDone = false;
    private boolean actionCanceled = false;
    private boolean mapReady = false;
    private boolean permissionsChecked = false;
    private boolean shockDetected = false;
    private SharedPreferences sharedPref;
    private PermissionHelper.PermissionBuilder permissionRequest;
    private QrScanRequest qrRequest;
    private boolean routeActive = false;
    private boolean routeInPause = false;
    private int qrSmsTimeout = 0;
    private int selectedEpcFile = 0;
    private boolean trackingActivated = true;
    private int locFilterTimeout = 0;

    // -------------------------------------------------------------------------------------------- //

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

        if (Connectivity.isConnected(getApplicationContext())) {

            if (progress != null) {

                progress.setMessage(getString(R.string.network_alert_string));
            }
        }

        super.onResume();
    }

    @Override
    public void onPause() {

        if (progress != null) {

            progress.dismiss();
        }

        super.onPause();
    }

    @Override
    public void onStop() {

        permissionsChecked = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

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
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

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

    // -------------------------------------------------------------------------------------------- //

    @Override
    public void onMapReady (GoogleMap map) {

        googleMap = map;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        this.googleMap.getUiSettings().setAllGesturesEnabled(true);

        mapReady = true;

        if (Connectivity.isConnected(getApplicationContext())) {

            if (progress != null) {

                progress.setMessage(getString(R.string.network_alert_string));
            }
        }

        if (progress != null && progress.isShowing()) {

            progress.setMessage(getString(R.string.progress_location_string));
        }

        posManager = new PositionManager(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            return;
        }

        googleMap.setMyLocationEnabled(true);

        setMapListeners();
        setButtonListeners();
        setPositionListeners();
    }

    private void setMapListeners() {

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick (final LatLng latLng) {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setMessage(getString(R.string.validate_marker_create_string));

                alertBuilder.setNegativeButton(getString(R.string.cancel_string), null);
                alertBuilder.setPositiveButton(getString(R.string.create_string), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick (DialogInterface dialogInterface, int i) {

                        Marker marker = markerManager.addMarker(googleMap, "", latLng, CustomMarker.MARKER_INFO, true);
                        showMarkerEditDialog(marker, true);
                    }
                });

                alertBuilder.create().show();
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick (LatLng latLng) {

                markerManager.hideAllAlertCircles();
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick (Marker marker) {

                CustomMarker customMarker = markerManager.getMarker(marker);

                if (customMarker.isEditable()) {

                    showMarkerEditDialog(marker, false);
                }
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick (Marker marker) {

                CustomMarker customMarker = markerManager.getMarker(marker);

                if (customMarker.isAlertEnabled()) {

                    markerManager.showAlertCircle(customMarker, true);
                }

                return false;
            }
        });
    }

    private void drawMapLine (LatLng startPoint, LatLng endPoint) {

        PolylineOptions opt = new PolylineOptions();

        opt.width(3);
        opt.geodesic(true);
        opt.color(Color.rgb(0, 160, 255));
        opt.add(startPoint, endPoint);

        mapPolylineList.add(googleMap.addPolyline(opt));
    }

    // -------------------------------------------------------------------------------------------- //

    @Override
    public void onStatusChanged (final STATUS_t status) {

        if (DEBUG_UI_ON) {

            if (status != STATUS_t.PAR_STARTED &&
                status != STATUS_t.PAR_PAUSING &&
                status != STATUS_t.PAR_PAUSING_WITH_STOP &&
                status != STATUS_t.PAR_RESUME &&
                status != STATUS_t.PAR_STOPPED) {

                return;
            }
        }

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                switch (status) {

                    case CHECK_ACTIF:

                        if (progress != null) {

                            progress.show();
                            progress.setMessage(getString(R.string.progress_check_imei_string) + StatsLastDriving.getIMEI(MainActivity.this));
                        }

                        break;

                    case IMEI_INACTIF:

                        if (progress != null) {

                            progress.show();
                            progress.setMessage(getString(R.string.progress_inactive_imei_string) );
                        }

                        break;

                    case GETTING_CFG:

                        if (progress != null) {

                            progress.show();

                            if (Connectivity.isConnected(getApplicationContext())) {

                                progress.setMessage((getString(R.string.progress_cfg_string) + StatsLastDriving.getIMEI(MainActivity.this)));
                            }
                            else {

                                progress.setMessage(getString(R.string.network_alert_string));
                            }
                        }

                        break;

                    case GETTING_EPC:

                        if (progress != null) {

                            progress.show();
                            progress.setMessage((getString(R.string.progress_epc_string) + StatsLastDriving.getIMEI(MainActivity.this)));
                        }

                        break;

                    case GETTING_DOBJ:

                        if (progress != null) {

                            progress.show();
                            progress.setMessage((getString(R.string.progress_obj_string) + StatsLastDriving.getIMEI(MainActivity.this)));
                        }

                        break;

                    case SETTING_CEP:
                    case SETTING_MARKERS:
                    case SETTING_PARCOUR_TYPE:

                        if (progress != null) {

                            progress.show();
                            progress.setMessage((getString(R.string.progress_send_string)));
                        }

                        break;

                    case PAR_STARTED:

                        routeActive = true;
                        routeInPause = false;

                        stopButton.setVisibility(View.GONE);

                        if (qrRequest.isAnyReqPending(QrScanRequest.REQUEST_ON_START)) {

                            appManager.add_ui_timer(120, QR_CHECK_ON_START_TMR);
                        }

                        trackingActivated = sharedPref.getBoolean(getString(R.string.tracking_activated), true);

                        if (mapReady) {

                            for (Polyline line : mapPolylineList) {

                                line.remove();
                            }

                            mapPolylineList.clear();

                            if (startMarker != null) {

                                markerManager.removeMarker(startMarker);
                                startMarker = null;
                            }

                            if (stopMarker != null) {

                                markerManager.removeMarker(stopMarker);
                                stopMarker = null;
                            }

                            googleMap.getUiSettings().setAllGesturesEnabled(false);

                            if (startMarker == null) {

                                startMarker = markerManager.addMarker(googleMap, getString(R.string.start_string), lastPos, CustomMarker.MARKER_GREEN, false);
                            }
                        }

                        break;

                    case PAR_RESUME:

                        routeActive = true;
                        routeInPause = false;

                        if (mapReady) {

                            googleMap.getUiSettings().setAllGesturesEnabled(false);
                        }

                        stopButton.setVisibility(View.GONE);

                        break;

                    case PAR_PAUSING:
                    case PAR_PAUSING_WITH_STOP:

                        System.gc();
                        routeInPause = true;

                        qrSmsTimeout = sharedPref.getInt(getString(R.string.phone_select_sms_qr_timeout_key), 0);
                        qrSmsTimeout *= 60;

                        if (mapReady) {

                            CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPos).zoom(MAP_ZOOM_ON_PAUSE).bearing(0).tilt(0).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            googleMap.getUiSettings().setAllGesturesEnabled(true);
                        }

                        stopButton.setVisibility((status == STATUS_t.PAR_PAUSING_WITH_STOP) ? View.VISIBLE : View.GONE);
                        accForceView.hide(true);

                        int pauseNotifTimeout = sharedPref.getInt(getString(R.string.phone_select_sms_pause_timeout_key), 0);

                        if (pauseNotifTimeout > 0) {

                            appManager.add_ui_timer(pauseNotifTimeout, PAUSE_NOTIFICATION_TMR);
                        }

                        break;

                    case PAR_STOPPED:

                        stopButton.setVisibility(View.GONE);

                        if (progress != null) {

                            progress.setMessage(getString(R.string.progress_ready_string));
                            progress.dismiss();
                        }

                        if (routeActive) {

                            qrRequest.resetVehicleReq();
                            appManager.add_ui_timer(180, QR_CHECK_ON_END_TMR);
                        }

                        if (stopMarker == null) {

                            stopMarker = markerManager.addMarker(googleMap, getString(R.string.stop_string), lastPos, CustomMarker.MARKER_RED, false);
                        }

                        routeActive = false;
                        routeInPause = false;

                        break;
                }
            }
        });
    }

    @Override
    public void onUiTimeout (final int timer_id, final STATUS_t status) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                switch (timer_id) {

                    case QR_CHECK_ON_START_TMR:

                        if (routeActive) {

                            if (qrRequest.isAnyReqPending(QrScanRequest.REQUEST_ON_START)) {

                                drawAttention(5);
                                showQrRequestAlert();

                                appManager.add_ui_timer(qrSmsTimeout, QR_SEND_ON_START_SMS_TMR);
                            }
                        }
                        else {

                            qrRequest.resetAllReq();
                            appManager.clear_ui_timer();
                         }

                        break;

                    case QR_SEND_ON_START_SMS_TMR:

                        if (routeActive) {

                            if (qrRequest.isAnyReqPending(QrScanRequest.REQUEST_ON_START)) {

                                sendSms(getPhoneNumber(R.string.phone_select_sms_qr), getString(R.string.sms_qr_msg_string));
                            }
                        }
                        else {

                            qrRequest.resetAllReq();
                        }

                        appManager.clear_ui_timer();

                        break;

                    case QR_CHECK_ON_END_TMR:

                        if (qrRequest.isVehicleReqPending(QrScanRequest.REQUEST_ON_STOP)) {

                            drawAttention(5);
                            showQrRequestAlert();

                            appManager.add_ui_timer(qrSmsTimeout, QR_SEND_ON_END_SMS_TMR);
                        }
                        else {

                            appManager.add_ui_timer((qrSmsTimeout / 2), QR_RESTART_REQ_TMR);
                        }

                        break;

                    case QR_SEND_ON_END_SMS_TMR:

                        if (qrRequest.isAnyReqPending(QrScanRequest.REQUEST_ON_STOP)) {

                            sendSms(getPhoneNumber(R.string.phone_select_sms_qr), getString(R.string.sms_qr_msg_string));
                        }

                        appManager.add_ui_timer((qrSmsTimeout / 2), QR_RESTART_REQ_TMR);

                        break;

                    case QR_RESTART_REQ_TMR:

                        qrRequest.resetAllReq();
                        appManager.clear_ui_timer();

                        break;

                    case PAUSE_NOTIFICATION_TMR:

                        if (routeInPause) {

                            sendSms(getPhoneNumber(R.string.phone_select_sms_pause), getString(R.string.sms_pause_msg_string));
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

                    boxNumView.setTextColor(Color.GRAY);
                }
                else {

                    boxNumView.setTextColor(Color.RED);
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

                if (routeActive && !routeInPause) {

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
    public void onCustomMarkerDataListGet() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                appManager.setCustomMarkerDataList(markerManager.fetchAllUserMarkersData());
            }
        });
    }

    @Override
    public void onSharedPositionsChanged (final List<CustomMarkerData> list) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (list.size() > 0) {

                    markerManager.removeMarker(CustomMarker.MARKER_INFO, true);
                    markerManager.removeMarker(CustomMarker.MARKER_DANGER, true);
                }

                for (CustomMarkerData data : list) {

                    Marker marker = markerManager.addMarker(googleMap, data);
                    CustomMarker customMarker = markerManager.getMarker(marker);

                    if (customMarker.isAlertEnabled())
                    {
                        markerManager.addAlertCircle(googleMap, customMarker);
                        markerManager.showAlertCircle(customMarker, false);
                    }
                }
            }
        });
    }

    @Override
    public void onNoteChanged (final int note_par, final LEVEL_t level_par, final LEVEL_t level_5_days) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                scoreView.setFinalScore(level_par, level_5_days, note_par);
            }
        });
    }

    @Override
    public void onScoreChanged (final SCORE_t type, final LEVEL_t level) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                scoreView.setScore(type, level);
            }
        });
    }

    @Override
    public void onShock (final double mG, final short raw) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (!shockDetected) {

                    shockDetected = true;

                    new CountDownTimer(5000, 5000) {

                        public void onTick(long millisUntilFinished) {}

                        public void onFinish() {

                            drawAttention(5);
                            shockDetected = false;

                            String msg = getString(R.string.sms_shock_msg_string) + " " + String.valueOf(mG) + "mG (" + String.valueOf(raw) + ")";
                            sendSms(getPhoneNumber(R.string.phone_select_sms_shock_key), msg);

                            markerManager.addMarker(googleMap, "Shock " + String.valueOf(mG) + "mG (" + String.valueOf(raw) + ")", lastPos, CustomMarker.MARKER_ROSE, false);
                        }

                    }.start();
                }
            }
        });
    }

    @Override
    public void onRecommendedSpeedChanged (final SPEED_t speed_t, final int kmh, final LEVEL_t level, final boolean valid) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                speedView.setSpeed(speed_t, level, kmh, valid);
            }
        });
    }

    @Override
    public void onInternetConnectionChanged() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (progress != null) {

                    if (Connectivity.isConnected(getApplicationContext())) {

                        progress.dismiss();
                    }
                    else {

                        progress.setMessage(getString(R.string.network_alert_string));
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

                if (debugView != null) {

                    if (txt.isEmpty()) {

                       debugLayout.setVisibility(View.GONE);
                    }
                    else {

                       debugLayout.setVisibility(View.VISIBLE);
                    }

                    debugView.setText(txt);
                }
            }
        });
    }

    @Override
    public void onCalibrateOnConstantSpeed() {

        /*
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                markerManager.addMarker(googleMap, "Const speed calibration", lastPos, CustomMarker.MARKER_MAGENTA, false);
            }
        });
        */
    }

    @Override
    public void onCalibrateOnAcceleration() {

        /*
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                markerManager.addMarker(googleMap, "Acceleration calibration", lastPos, CustomMarker.MARKER_CYAN, false);
            }
        });
        */
    }

    @Override
    public void onCalibrateRAZ() {

        /*
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                markerManager.addMarker(googleMap, "Reset calibration", lastPos, CustomMarker.MARKER_VIOLET, false);
            }
        });
        */
    }

    // -------------------------------------------------------------------------------------------- //

    private void init (boolean firstLaunch) {

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mapReady = false;
        initDone = true;
        appColor = new AppColor(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        appManager = new AppManager(this, this);

        qrRequest = new QrScanRequest();

        progress = new ProgressDialog(this, R.style.InfoDialogStyle);
        progress.setMessage(getString(R.string.progress_map_string));
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setProgressNumberFormat(null);
        progress.setProgressPercentFormat(null);

        if (!DEBUG_UI_ON) {

            progress.setCancelable(false);
        }

        if (!isFinishing()) {

            progress.show();
        }

        markerManager = new MarkerManager(getApplicationContext());
        speedView = new SpeedView(this);
        scoreView = new ScoreView(this);
        accForceView = new AccForceView(this);
        accForceView.hide(true);

        backgroundView = (ImageView) findViewById(R.id.background_image);
        boxNumView = (TextView) findViewById(R.id.box_num_connected);
        drivingTimeView = (TextView) findViewById(R.id.driving_time_text);

        infoButton = (FloatingActionButton) findViewById(R.id.button_info);
        callButton = (FloatingActionButton) findViewById(R.id.button_call);
        scanQrCodeButton = (FloatingActionButton) findViewById(R.id.button_qrcode);
        epcSettingsButton = (FloatingActionButton) findViewById(R.id.button_epc_settings);
        stopButton = (FloatingActionButton) findViewById(R.id.button_stop);
        stopButton.setVisibility(View.GONE);

        optMenu = (FloatingActionMenu) findViewById(R.id.opt_menu);
        optMenu.setClosedOnTouchOutside(false);
        menuButtonSos = (FloatingActionButton) findViewById(R.id.menu_button_sos);
        menuButtonResetCalib = (FloatingActionButton) findViewById(R.id.menu_button_reset_calibration);
        menuButtonTracking = (FloatingActionButton) findViewById(R.id.menu_button_tracking);
        menuButtonSettings = (FloatingActionButton) findViewById(R.id.menu_button_settings);

        trackingActivated = sharedPref.getBoolean(getString(R.string.tracking_activated_key), true);

        if (trackingActivated) {

            menuButtonTracking.setColorNormal(appColor.getColor(AppColor.GREEN));
            menuButtonTracking.setColorPressed(appColor.getColor(AppColor.ORANGE));
        }
        else {

            menuButtonTracking.setColorNormal(appColor.getColor(AppColor.ORANGE));
            menuButtonTracking.setColorPressed(appColor.getColor(AppColor.GREEN));
        }

        if (DEBUG_LOGVIEW_ON){

            debugLayout = (LinearLayout) findViewById(R.id.debug_layout);
            debugView = (TextView) findViewById(R.id.debug_view);
        }

        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (firstLaunch) {

            mapFrag.setRetainInstance(true);
        }

        mapFrag.getMapAsync(this);
    }

    private int checkPermissions() {

        int ok = 1;

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

        new CountDownTimer(10000, 10000) {

            public void onTick (long millisUntilFinished) {}

            public void onFinish() {

                if (dialog.isShowing()) {

                    dialog.dismiss();
                }
            }

        }.start();
    }

    public void showMarkerAlert (final CustomMarker customMarker) {

        customMarker.setAsActivated(true);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final int filesNum = customMarker.getTotalAttachmentsNum();
        String msg = "Attention, vous vous approchez du marqueur '" + customMarker.getTitle() + "'\n";

        if (filesNum > 0) {

            msg += "\nVous avez " + filesNum + " fichier(s) à prendre en compte";
        }
        else {

            msg += "\nIl n'y a aucun fichier ou message attribué à cette position";
        }

        alertDialog.setCancelable(false);
        alertDialog.setTitle("");
        alertDialog.setMessage(msg);

        if (filesNum > 0) {

            alertDialog.setPositiveButton(getString(R.string.open_string), new DialogInterface.OnClickListener() {

                public void onClick (DialogInterface dialog,int which) {

                    System.gc();
                    CustomMarkerData data = markerManager.getUserMarkerData(customMarker);

                    Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.MARKER_DATA_PARAM, data);
                    startActivity(intent);

                    dialog.dismiss();
                }
            });
        }
        else {

            alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });
        }

        alertDialog.show();
    }

    public void askEndDayConfirm() {

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

    protected void askCallConfirm() {

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setMessage(getString(R.string.make_call_confirma_string));
        alertBuilder.setCancelable(false);
        alertBuilder.setNegativeButton(getString(R.string.cancel_string), null);
        alertBuilder.setPositiveButton(getString(R.string.call_string), new DialogInterface.OnClickListener() {

            @Override
            public void onClick (DialogInterface dialogInterface, int i) {

                call(getPhoneNumber(R.string.phone_select_voice_key));
            }
        });

        alertBuilder.create().show();
    }

    protected void askTrackingConfirm() {

        trackingActivated = sharedPref.getBoolean(getString(R.string.tracking_activated), true);

        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setCancelable(false);

        String actionStr = "";

        actionStr = getString(R.string.yes_string);
        alertBuilder.setMessage(getString(R.string.return_location_string) + " ?");

        alertBuilder.setNegativeButton(getString(R.string.cancel_string), null);
        alertBuilder.setPositiveButton(actionStr, new DialogInterface.OnClickListener() {

            @Override
            public void onClick (DialogInterface dialogInterface, int i) {

                if (lastLocation != null) {

                    lastPos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPos).zoom(MAP_ZOOM_ON_PAUSE).bearing(0).tilt(0).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    menuButtonTracking.setColorNormal(appColor.getColor(AppColor.GREEN));
                    menuButtonTracking.setColorPressed(appColor.getColor(AppColor.ORANGE));

                   // googleMap.animateCamera(CameraUpdateFactory.newLatLng(lastPos));
                }

            }
        });

        alertBuilder.create().show();
    }

    protected void showMarkerEditDialog (final Marker marker, final boolean creation) {

        final CustomMarker customMarker = markerManager.getMarker(marker);

        if (customMarker == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.marker_edit_dialog, null));

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton(getString(R.string.cancel_string), null);

        if (!creation) {

            builder.setNeutralButton(getString(R.string.delete_string), null);
        }

        final AlertDialog alertDlg = builder.create();

        if (creation) {

            alertDlg.setCancelable(false);
        }

        alertDlg.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow (DialogInterface dialogInterface) {

                final EditText editTitle = (EditText) alertDlg.findViewById(R.id.marker_title);
                final CheckBox shareCheckBox = (CheckBox) alertDlg.findViewById(R.id.marker_share_checkbox);
                final CheckBox alarmCheckBox = (CheckBox) alertDlg.findViewById(R.id.marker_alarm_checkbox);
                final Spinner markerTypeSpinner = (Spinner) alertDlg.findViewById(R.id.marker_type_spinner);
                final EditableSeekBar alertRadiusSeekBar = (EditableSeekBar) alertDlg.findViewById(R.id.marker_radius_seekbar);

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
                        alertRadiusSeekBar.setVisibility(View.VISIBLE);
                    }
                    else {

                        alarmCheckBox.setChecked(false);
                        alertRadiusSeekBar.setVisibility(View.GONE);
                    }

                    if (customMarker.isShared()) {

                        shareCheckBox.setChecked(true);
                    }
                    else {

                        shareCheckBox.setChecked(false);
                    }

                    alertRadiusSeekBar.setValue(customMarker.getAlertRadius());
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

                shareCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged (CompoundButton compoundButton, boolean b) {

                        if (b) {

                            customMarker.share(true);
                        }
                        else {

                            customMarker.share(false);
                        }
                    }
                });

                alarmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged (CompoundButton compoundButton, boolean b) {

                        if (b) {

                            customMarker.enableAlert(true);
                            customMarker.showAlertCircle(true);

                            alertRadiusSeekBar.setVisibility(View.VISIBLE);
                        }
                        else {

                            customMarker.enableAlert(false);
                            customMarker.showAlertCircle(false);

                            alertRadiusSeekBar.setVisibility(View.GONE);
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

                        if (customMarker.isAlertEnabled())
                        {
                            customMarker.setAlertRadius(alertRadiusSeekBar.getValue());
                            markerManager.addAlertCircle(googleMap, customMarker);
                        }

                        if (titleBefore != currTitle) {

                            if (currTitle.length() > 0) {

                                customMarker.setTitle(currTitle);

                                marker.hideInfoWindow();    // Refresh marker's title
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

                            markerManager.removeMarker(customMarker);
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

                                markerManager.removeMarker(customMarker);
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

    protected void showEpcSelectDialog() {

        List<Integer> epcExistList = DataEPC.getAppEpcExist(this);
        final String[] epcStrList = new String[5];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_epc_string));

        if (epcExistList.size() > 0) {

            for (int i = 0; i < epcStrList.length; i++) {

                epcStrList[i] = "Seuils de force " + String.valueOf(i + 1);

                boolean exist = false;

                for (int k = 0; k < epcExistList.size(); k++) {

                    if (i == (epcExistList.get(k) - 1)) {

                        exist = true;
                    }
                }

                if (!exist) {

                    epcStrList[i] += " [" + getString(R.string.empty_string) + "]";
                }
            }

            selectedEpcFile = sharedPref.getInt(getString(R.string.epc_selected), 1) - 1;

            builder.setSingleChoiceItems(epcStrList, selectedEpcFile, new DialogInterface.OnClickListener() {

                @Override
                public void onClick (DialogInterface dialog, int which) {

                    selectedEpcFile = which + 1;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.epc_selected), selectedEpcFile);
                    editor.apply();
                }
            });
        }
        else {

            builder.setMessage(getString(R.string.epc_missing_string));
        }

        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void changeViewColorFilter (View view, int color) {

        Drawable background = view.getBackground();
        background.setColorFilter(appColor.getColor(color), PorterDuff.Mode.SRC_ATOP);
        view.setBackground(background);
    }

    private String getSmsHeader() {

        String str = "[";

        str += ComonUtils.getIMEInumber(getApplicationContext()) + ", ";

        if (qrRequest.driverName.length() > 0) {

            str += String.valueOf(qrRequest.driverName) + ", ";
        }

        if (qrRequest.driverId > 0) {

            str += String.valueOf(qrRequest.driverId) + ", ";
        }

        if (lastLocation != null) {

            str += String.valueOf(lastLocation.getLatitude()) + " ";
            str += String.valueOf(lastLocation.getLongitude()) + ", ";
            String date = new SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.getDefault()).format(lastLocation.getTime());
            str += date;
        }

        str += "]";
        return str;
    }

    private void sendSms (String phoneNumber, String msg) {

        if (phoneNumber.isEmpty()) {

            return;
        }

        String str = msg + " " + getSmsHeader();
        SmsManager sms = SmsManager.getDefault();

        try {

            sms.sendTextMessage(phoneNumber, null, str, null, null);
        }
        catch (Exception ex) {

        }
    }

    private void call (String phoneNumber) {

        if (phoneNumber.isEmpty()) {

            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        startActivity(intent);
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

                    sendSms(getPhoneNumber(R.string.phone_select_sms_sos_key), getString(R.string.sms_sos_msg_string));
                }

                if (dialog.isShowing()) {

                    dialog.dismiss();
                }
            }

        }.start();
    }

    private void disableActionButtons (boolean disable) {

        FloatingActionButton[] actionBtnArray = {infoButton, callButton, scanQrCodeButton, epcSettingsButton};

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

        FloatingActionButton[] actionBtnArray = {infoButton, callButton, scanQrCodeButton, epcSettingsButton};

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

        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate((seconds * 1000));
    }

    private String getPhoneNumber (int key) {

        String[] numberList = new String[5];

        numberList[0] = sharedPref.getString(getString(R.string.phone_number_1_key), "");
        numberList[1] = sharedPref.getString(getString(R.string.phone_number_2_key), "");
        numberList[2] = sharedPref.getString(getString(R.string.phone_number_3_key), "");
        numberList[3] = sharedPref.getString(getString(R.string.phone_number_4_key), "");
        numberList[4] = sharedPref.getString(getString(R.string.phone_number_5_key), "");

        int number = Integer.parseInt(sharedPref.getString(getString(key), "0"));

        if (number > 0) {

            return numberList[number - 1].trim();
        }

        return "";
    }

    private void setPositionListeners() {

        posManager.setPositionChangedListener(new PositionManager.PositionListener() {

            @Override
            public void onRawPositionUpdate (Location location) {

                lastPos = new LatLng(location.getLatitude(), location.getLongitude());

                if (lastLocation == null) {

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPos).zoom(MAP_ZOOM_ON_PAUSE).bearing(0).tilt(0).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    if (progress != null && progress.isShowing()) {

                        progress.setMessage(getString(R.string.progress_loading_string));
                    }
                }

                lastLocation = location;
                appManager.setLocation(lastLocation);

                if (routeActive && !routeInPause) {

                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(lastPos));
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPos).zoom(MAP_ZOOM_ON_MOVE).bearing(0).tilt(30).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }

            @Override
            public void onPositionUpdate (Location prevLoc, Location currLoc) {

                if (routeActive) {

                    LatLng prevPos = new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude());
                    LatLng currPos = new LatLng(currLoc.getLatitude(), currLoc.getLongitude());
                    drawMapLine(prevPos, currPos);

                    if (locFilterTimeout++ > 5) {

                        markerManager.fetchNearMarkers(currPos);
                        locFilterTimeout = 0;
                    }

                    CustomMarker customMarker = markerManager.findClosestAlertMarker(currPos);

                    if (customMarker != null) {

                        showMarkerAlert(customMarker);
                        customMarker.setAsActivated(true);
                    }
                }
            }

            @Override
            public void onGpsStatusChange (boolean gpsFix) {

                appManager.setGpsStatus(gpsFix);
            }
        });
    }

    private void setButtonListeners() {

        infoButton.setOnClickListener (new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                Intent intent = new Intent(MainActivity.this, StatsActivity.class);
                startActivity(intent);
                optMenu.close(true);
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                askCallConfirm();
            }
        });

        scanQrCodeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (final View view) {

                Intent intent = new Intent(MainActivity.this, QrScanActivity.class);
                intent.putExtra(QrScanActivity.QR_SCAN_REQUEST_PARAM, qrRequest);
                startActivityForResult(intent, QR_REQUEST_ID);
            }
        });

        epcSettingsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (final View view) {

                showEpcSelectDialog();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                askEndDayConfirm();
            }
        });

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

        menuButtonSos.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                sendSos();
            }
        });

        menuButtonResetCalib.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                appManager.raz_calibration();
            }
        });

        menuButtonTracking.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                askTrackingConfirm();
            }
        });

        menuButtonSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                startActivity(new Intent(MainActivity.this, PinLockActivity.class));
                optMenu.close(true);
            }
        });
    }

    // -------------------------------------------------------------------------------------------- //
}
