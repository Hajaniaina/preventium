package com.preventium.boxpreventium.gui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.text.Html;
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
import android.widget.Toast;

import com.firetrap.permissionhelper.action.OnDenyAction;
import com.firetrap.permissionhelper.action.OnGrantAction;
import com.firetrap.permissionhelper.helper.PermissionHelper;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.gregacucnik.EditableSeekBar;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.SCORE_t;
import com.preventium.boxpreventium.enums.SPEED_t;
import com.preventium.boxpreventium.enums.STATUS_t;
import com.preventium.boxpreventium.location.CustomMarker;
import com.preventium.boxpreventium.location.CustomMarkerData;
import com.preventium.boxpreventium.location.MarkerManager;
import com.preventium.boxpreventium.location.PositionManager;
import com.preventium.boxpreventium.manager.AppManager;
import com.preventium.boxpreventium.manager.Force;
import com.preventium.boxpreventium.manager.SpeedCorner;
import com.preventium.boxpreventium.manager.SpeedLine;
import com.preventium.boxpreventium.manager.StatsLastDriving;
import com.preventium.boxpreventium.module.HandlerBox;
import com.preventium.boxpreventium.server.CFG.ReaderCFGFile;
import com.preventium.boxpreventium.server.EPC.DataEPC;
import com.preventium.boxpreventium.server.EPC.NameEPC;
import com.preventium.boxpreventium.server.JSON.ParseJsonData;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.Connectivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//public class MainActivity extends FragmentActivity implements OnMapReadyCallback, AppManager.AppManagerListener {
public class MainActivity extends FragmentActivity implements OnMapReadyCallback, AppManager.AppManagerListener, LocationListener {

    private static final String TAG = "MainActivity";
    private static final String APPPREFERENCES = "AppPrefs" ;
    private final long DURATION = 3000L;

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

    private TextView corner_n_v;
    private TextView brake_n_v;
    private TextView acc_n_v;
    private TextView avg_n_v;
    private TextView drive_n_v;

    private ImageView backgroundView;
    private ImageView flagView;
    private ImageView no_map;

    private FloatingActionMenu optMenu;
    private FloatingActionButton infoButton;
    private FloatingActionButton callButton;
    private FloatingActionButton scanQrCodeButton;
    private FloatingActionButton epcSettingsButton;
    private FloatingActionButton stopButton;
    private FloatingActionButton menuButtonSos;
    private FloatingActionButton menuButtonMapRecenter;
    private FloatingActionButton menuButtonResetCalib;
    //private FloatingActionButton menuButtonTracking;
    private FloatingActionButton menuButtonSettings;

    private GoogleMap googleMap;
    private LatLng lastPos;
    private Location lastLocation = null;
    private AppColor appColor;
    private ProgressDialog progress;
    private ProgressDialog progressOPT;
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


    private int opt_panneau = 99;
    private int opt_carte = 99;
    private int opt_note = 99;
    private int opt_VFAM = 99;
    private int opt_duree = 99;
    private int opt_qrcode = 99;
    private int opt_seulforce = 99;
    private int opt_config_type = 99;
    private int opt_langue = 99;
    private int opt_screen_size = 99;

    private String opt_test;
    String jsonString;
    NotificationCompat.Builder notif;

    private static final int id_notif_qr = 10;
    private static final int id_notif_conf = 11;
    private static final int id_notif_avfm = 12;
    private static final int id_notif_seuil = 13;
    private static final int id_notif_note = 14;
    private static final int id_notif_lang = 15;
    private static final int id_notif_duree = 16;
    private static final int id_notif_pan = 17;
    private static final int id_notif_map = 18;
    private SupportMapFragment mapFrag;
    private boolean internet_activeopt = true;


    String serveur="tsis";
    ReaderCFGFile reader1 = new ReaderCFGFile();
    String srcFileName="";
    String desFileName="";

    boolean cfgi = false;


    private String epcname = "";

//##### add old veer
    private SharedPreferences force_pref;
    private Marker forceMarker = null;
    private List<Force> posList = new ArrayList();
    private Marker pauseMarker = null;
    private Marker resumeMarker = null;
    private List<SpeedCorner> speed_corner = new ArrayList();
    private List<SpeedLine> speed_line = new ArrayList();
    private TextView forceView;
    private SharedPreferences speedCorner;
    private SharedPreferences speedLine;
    private MediaPlayer mediaPlayer;
    private LatLng firstPos;
    private Lock lock;
    boolean gps;



    private final long MESSAGE_DURATION = 10800000L;
    private TextView smsMessageView;
    private static MainActivity activity;
    private boolean flag_run_once = false;

    @Override
    public void onLocationChanged(Location location) {
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(this.lastPos));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    //###santo
    class C00322 implements GoogleMap.OnMapLongClickListener {
        C00322() {
        }

        public void onMapLongClick(final LatLng latLng) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
            alertBuilder.setMessage(MainActivity.this.getString(R.string.validate_marker_create_string));
            alertBuilder.setNegativeButton(MainActivity.this.getString(R.string.cancel_string), null);
            alertBuilder.setPositiveButton(MainActivity.this.getString(R.string.create_string), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.showMarkerEditDialog(MainActivity.this.markerManager.addMarker(MainActivity.this.googleMap, "", "", latLng, 13, false), true);
                }
            });
            alertBuilder.create().show();
        }
    }

    class C00333 implements GoogleMap.OnMapClickListener {
        C00333() {
        }

        public void onMapClick(LatLng latLng) {
            MainActivity.this.markerManager.hideAllAlertCircles();
        }
    }

    class C00414 implements GoogleMap.OnInfoWindowClickListener {
        C00414() {
        }

        public void onInfoWindowClick(Marker marker) {
            if (MainActivity.this.markerManager.getMarker(marker).isEditable()) {
                MainActivity.this.showInfoDialog(marker);
            }
        }
    }

    class C00465 implements GoogleMap.OnMarkerClickListener {
        C00465() {
        }

        public boolean onMarkerClick(Marker marker) {
            CustomMarker customMarker = MainActivity.this.markerManager.getMarker(marker);
            if (customMarker.isAlertEnabled()) {
                MainActivity.this.markerManager.showAlertCircle(customMarker, true);
            }
            if (customMarker.isEditable()) {
                marker.setSnippet(MainActivity.this.getResources().getString(R.string.more_string));
            }
            return false;
        }
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {


        //setRepeatingAsyncTask();

        super.onCreate(savedInstanceState);

        activity = this;

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        if (checkPermissions() > 0) {



            if (savedInstanceState == null) {


                init(true);

                notif = new NotificationCompat.Builder(this);
                notif.setAutoCancel(true);

                localizationFlag();
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
        //cfgi= reader1.read(desFileName);
        setRepeatingAsyncTask();
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
            }else {

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

/*
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
*/
   //######qrcod test

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == -1 && data.hasExtra(QrScanActivity.QR_SCAN_REQUEST_PARAM)) {
            this.qrRequest = (QrScanRequest) data.getParcelableExtra(QrScanActivity.QR_SCAN_REQUEST_PARAM);
            if (this.qrRequest.driverIdReq == 1) {
                this.appManager.set_driver_id(this.qrRequest.driverId);
            }
            if (this.qrRequest.vehicleFrontReq == 1) {
            }
            if (this.qrRequest.vehicleBackReq != 1) {
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
//##santo
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.googleMap.getUiSettings().setRotateGesturesEnabled(false);

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

    /* //###santo
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
*/
    private void setMapListeners() {
        this.googleMap.setOnMapLongClickListener(new C00322());
        this.googleMap.setOnMapClickListener(new C00333());
        this.googleMap.setOnInfoWindowClickListener(new C00414());
        this.googleMap.setOnMarkerClickListener(new C00465());
    }

    private void drawMapLine (LatLng startPoint, LatLng endPoint) {

        PolylineOptions opt = new PolylineOptions();

        //opt.width(3);
        opt.width(3.0f);
        opt.geodesic(true);
        opt.color(Color.rgb(0, 160, 255));
        opt.add(startPoint, endPoint);

        mapPolylineList.add(googleMap.addPolyline(opt));
    }

    // -------------------------------------------------------------------------------------------- //
/*
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

                        routeActive = false;   //santo routeLine
                        routeInPause = false;

                        break;
                }

                //mbol afaka asiako

            }
        });
    }

   */ //######santo rep
    @Override
    public void onStatusChanged(final STATUS_t status) {


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
            public void run() {
                Force[] force;
                int i;
                MainActivity mainActivity;
                MainActivity mainActivity2;



                switch (status) {
                    case CHECK_ACTIF:
                        MainActivity.this.googleMap.clear();
                        try {
                            MainActivity.this.force_pref = MainActivity.this.getSharedPreferences("", 0);
                            force = (Force[]) new Gson().fromJson(MainActivity.this.force_pref.getString("force", ""), Force[].class);
                            if (force != null) {
                                for (i = 0; i < force.length; i++) {
                                    if (i != 0) {
                                        mainActivity = MainActivity.this;
                                        mainActivity2 = mainActivity;
                                        mainActivity2.forceMarker = MainActivity.this.markerManager.addMarker(MainActivity.this.googleMap, force[i].getForce(), "<body> Force: " + force[i].getForce() + "<br>" + "Vitesse en ligne droite: " + force[i].getSpeed_l() + "<br>" + "Vitesse dans virage: " + force[i].getSpeed_c() + "<br>" + "Ecart forces: " + (Integer.valueOf(force[i].getForce()).intValue() - Integer.valueOf(force[i - 1].getForce()).intValue()) + "<br>" + "Ecart vitesses en ligne droite: " + (Integer.valueOf(force[i].getSpeed_l()).intValue() - Integer.valueOf(force[i - 1].getSpeed_l()).intValue()) + "<br>" + "Ecart vitesses dans virage: " + (Integer.valueOf(force[i].getSpeed_c()).intValue() - Integer.valueOf(force[i - 1].getSpeed_c()).intValue()) + "<br>" + "</body>", new LatLng(force[i].getLoc().latitude, force[i].getLoc().longitude), MainActivity.this.getForceCode(force[i].getType_X(), force[i].getLevel_X()), true);
                                    } else {
                                        mainActivity = MainActivity.this;
                                        mainActivity2 = mainActivity;
                                        mainActivity2.forceMarker = MainActivity.this.markerManager.addMarker(MainActivity.this.googleMap, force[i].getForce(), "<body> Force: " + force[i].getForce() + "<br>" + "Vitesse en ligne droite: " + force[i].getSpeed_l() + "<br>" + "Vitesse dans virage: " + force[i].getSpeed_c() + "<br>" + "</body>", new LatLng(force[i].getLoc().latitude, force[i].getLoc().longitude), MainActivity.this.getForceCode(force[i].getType_X(), force[i].getLevel_X()), true);
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                        if (MainActivity.this.progress != null) {
                            MainActivity.this.progress.show();
                            MainActivity.this.progress.setMessage(MainActivity.this.getString(R.string.progress_check_imei_string) + StatsLastDriving.getIMEI(MainActivity.this));
                            return;
                        }
                        return;
                    case IMEI_INACTIF:
                        if (MainActivity.this.progress != null) {
                            MainActivity.this.progress.show();
                            MainActivity.this.progress.setMessage(MainActivity.this.getString(R.string.progress_inactive_imei_string));
                            return;
                        }
                        return;
                    case GETTING_CFG:
                        if (MainActivity.this.progress != null) {
                            MainActivity.this.progress.show();
                            if (Connectivity.isConnected(MainActivity.this.getApplicationContext())) {
                                MainActivity.this.progress.setMessage(MainActivity.this.getString(R.string.progress_cfg_string) + StatsLastDriving.getIMEI(MainActivity.this));
                                return;
                            } else {
                                MainActivity.this.progress.setMessage(MainActivity.this.getString(R.string.network_alert_string));
                                return;
                            }
                        }
                        return;
                    case GETTING_EPC:
                        if (MainActivity.this.progress != null) {
                            MainActivity.this.progress.show();
                            MainActivity.this.progress.setMessage(MainActivity.this.getString(R.string.progress_epc_string) + StatsLastDriving.getIMEI(MainActivity.this));
                            return;
                        }
                        return;

                    case GETTING_DOBJ:
                    if (progress != null) {

                        progress.show();
                        progress.setMessage((getString(R.string.progress_obj_string) + StatsLastDriving.getIMEI(MainActivity.this)));

                        return;
                    }

                        return;
                    case SETTING_CEP:
                    case SETTING_MARKERS:
                    case SETTING_PARCOUR_TYPE:
                        if (MainActivity.this.progress != null) {
                            MainActivity.this.progress.show();
                            MainActivity.this.progress.setMessage(MainActivity.this.getString(R.string.progress_send_string));
                            return;
                        }
                        return;
                    case PAR_STARTED:
                        ComonUtils.SavePreferences("moved", "moved", 1, MainActivity.this);
                        MainActivity.this.routeActive = true;
                        MainActivity.this.routeInPause = false;
                        MainActivity.this.googleMap.clear();
                        MainActivity.this.posList.clear();
                        MainActivity.this.force_pref.edit().clear().commit();
                        if (MainActivity.this.qrRequest.isAnyReqPending(0)) {
                            MainActivity.this.appManager.add_ui_timer(120, 0);
                        }
                        MainActivity.this.trackingActivated = MainActivity.this.sharedPref.getBoolean(MainActivity.this.getString(R.string.tracking_activated), true);
                        if (MainActivity.this.mapReady) {
                            for (Polyline line : MainActivity.this.mapPolylineList) {
                                line.remove();
                            }
                            MainActivity.this.mapPolylineList.clear();
                            if (MainActivity.this.startMarker != null) {
                                MainActivity.this.markerManager.removeMarker(MainActivity.this.startMarker);
                                MainActivity.this.startMarker = null;
                            }
                            if (MainActivity.this.forceMarker != null) {
                                MainActivity.this.markerManager.removeMarker(MainActivity.this.forceMarker);
                                MainActivity.this.forceMarker = null;
                            }
                            if (MainActivity.this.pauseMarker != null) {
                                MainActivity.this.markerManager.removeMarker(MainActivity.this.pauseMarker);
                                MainActivity.this.pauseMarker = null;
                            }
                            if (MainActivity.this.resumeMarker != null) {
                                MainActivity.this.markerManager.removeMarker(MainActivity.this.resumeMarker);
                                MainActivity.this.resumeMarker = null;
                            }
                            if (MainActivity.this.stopMarker != null) {
                                MainActivity.this.markerManager.removeMarker(MainActivity.this.stopMarker);
                                MainActivity.this.stopMarker = null;
                            }
                            MainActivity.this.googleMap.getUiSettings().setAllGesturesEnabled(false);
                            if (MainActivity.this.startMarker == null) {
                                MainActivity.this.startMarker = MainActivity.this.markerManager.addMarker(MainActivity.this.googleMap, "" + ComonUtils.currentTime(), "", MainActivity.this.lastPos, 15, false);
                            }
                        }
                        MainActivity.this.speed_line.clear();
                        MainActivity.this.speed_corner.clear();
                        return;
                    case PAR_RESUME:
                        MainActivity.this.routeActive = true;
                        MainActivity.this.routeInPause = false;
                        if (MainActivity.this.mapReady) {
                            MainActivity.this.googleMap.getUiSettings().setAllGesturesEnabled(false);
                            if (MainActivity.this.resumeMarker == null) {
                                MainActivity.this.resumeMarker = MainActivity.this.markerManager.addMarker(MainActivity.this.googleMap, "" + ComonUtils.currentTime(), "", MainActivity.this.lastPos, 18, false);
                                return;
                            }
                            return;
                        }
                        return;
                    case PAR_PAUSING:
                        if (MainActivity.this.pauseMarker == null) {
                            MainActivity.this.pauseMarker = MainActivity.this.markerManager.addMarker(MainActivity.this.googleMap, "" + ComonUtils.currentTime(), "", MainActivity.this.lastPos, 17, false);
                            break;
                        }
                        break;
                    case PAR_PAUSING_WITH_STOP:
                        break;
                    case PAR_STOPPED:
                        ComonUtils.SavePreferences("moved", "moved", 0, MainActivity.this);
                        if (MainActivity.this.progress != null) {
                            MainActivity.this.progress.setMessage(MainActivity.this.getString(R.string.progress_ready_string));
                            MainActivity.this.progress.dismiss();
                        }
                        if (MainActivity.this.mapReady) {
                            MainActivity.this.googleMap.getUiSettings().setAllGesturesEnabled(true);
                            MainActivity.this.googleMap.getUiSettings().setRotateGesturesEnabled(false);
                            if (MainActivity.this.forceMarker == null) {
                                try {
                                    MainActivity.this.force_pref = MainActivity.this.getSharedPreferences("", 0);
                                    force = (Force[]) new Gson().fromJson(MainActivity.this.force_pref.getString("force", ""), Force[].class);
                                    if (force != null) {
                                        for (i = 0; i < force.length; i++) {
                                            mainActivity = MainActivity.this;
                                            mainActivity2 = mainActivity;
                                            mainActivity2.forceMarker = MainActivity.this.markerManager.addMarker(MainActivity.this.googleMap, force[i].getForce(), "", new LatLng(force[i].getLoc().latitude, force[i].getLoc().longitude), MainActivity.this.getForceCode(force[i].getType_X(), force[i].getLevel_X()), false);
                                            double lat1 = force[i].getLoc().latitude;
                                            double lng1 = force[i].getLoc().longitude;
                                            double lat2 = force[i + 1].getLoc().latitude;
                                            double dLon = force[i + 1].getLoc().longitude - lng1;
                                            MainActivity.this.googleMap.addMarker(new MarkerOptions().position(force[i + 1].getLoc()).rotation((float) Math.toDegrees(Math.atan2(Math.sin(dLon) * Math.cos(lat2), (Math.cos(lat1) * Math.sin(lat2)) - ((Math.sin(lat1) * Math.cos(lat2)) * Math.cos(dLon))))).anchor(-0.65f, 0.5f).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.arrow_head))));
                                        }
                                    }
                                } catch (Exception e2) {
                                }
                            }
                            if (MainActivity.this.stopMarker == null) {
                                MainActivity.this.stopMarker = MainActivity.this.markerManager.addMarker(MainActivity.this.googleMap, "" + ComonUtils.currentTime(), "", MainActivity.this.lastPos, 16, false);
                            }
                        }
                        if (MainActivity.this.routeActive) {
                            MainActivity.this.qrRequest.resetVehicleReq();
                            MainActivity.this.appManager.add_ui_timer(180, 1);
                        }
                        MainActivity.this.routeActive = false;
                        MainActivity.this.routeInPause = false;
                        return;
                    default:
                        return;
                }
                if (status == STATUS_t.PAR_PAUSING_WITH_STOP) {
                    MainActivity.this.askEndDayConfirm();
                }
                System.gc();
                MainActivity.this.routeInPause = true;
                MainActivity.this.qrSmsTimeout = MainActivity.this.sharedPref.getInt(MainActivity.this.getString(R.string.phone_select_sms_qr_timeout_key), 0);
                MainActivity.this.qrSmsTimeout = MainActivity.this.qrSmsTimeout * 60;
                if (MainActivity.this.mapReady) {
                    MainActivity.this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(MainActivity.this.lastPos).zoom(15.0f).bearing(0.0f).tilt(0.0f).build()));
                    MainActivity.this.googleMap.getUiSettings().setAllGesturesEnabled(true);
                }
                MainActivity.this.accForceView.hide(true, getMap(), getScreen(), getSeuil());
                int pauseNotifTimeout = MainActivity.this.sharedPref.getInt(MainActivity.this.getString(R.string.phone_select_sms_pause_timeout_key), 0);
                if (pauseNotifTimeout > 0) {
                    MainActivity.this.appManager.add_ui_timer((long) pauseNotifTimeout, 5);
                }
            }
        });
    }


    private int getForceCode(FORCE_t type, LEVEL_t level) {
        switch (type) {
            case TURN_LEFT:
                switch (level) {
                    case LEVEL_1:
                        return 20;
                    case LEVEL_2:
                        return 21;
                    case LEVEL_3:
                        return 22;
                    case LEVEL_4:
                        return 23;
                    case LEVEL_5:
                        return 24;
                    default:
                        return 0;
                }
            case TURN_RIGHT:
                switch (level) {
                    case LEVEL_1:
                        return 25;
                    case LEVEL_2:
                        return 26;
                    case LEVEL_3:
                        return 27;
                    case LEVEL_4:
                        return 28;
                    case LEVEL_5:
                        return 29;
                    default:
                        return 0;
                }
            case ACCELERATION:
                switch (level) {
                    case LEVEL_1:
                        return 30;
                    case LEVEL_2:
                        return 31;
                    case LEVEL_3:
                        return 32;
                    case LEVEL_4:
                        return 33;
                    case LEVEL_5:
                        return 34;
                    default:
                        return 0;
                }
            case BRAKING:
                switch (level) {
                    case LEVEL_1:
                        return 35;
                    case LEVEL_2:
                        return 36;
                    case LEVEL_3:
                        return 37;
                    case LEVEL_4:
                        return 38;
                    case LEVEL_5:
                        return 39;
                    default:
                        return 0;
                }
            default:
                return 0;
        }
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
        HandlerBox box_leurre = new HandlerBox(this);
        final int active_leurre =  box_leurre.get_active_from_serveur();

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (nb > 0) {

                    boxNumView.setTextColor(Color.GRAY);
                }
                else {
                    boxNumView.setTextColor(Color.RED);
                    if(active_leurre == 1){
                        boxNumView.setTextColor(Color.GREEN);
                    }
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

  /*  @Override
    public void onForceChanged(FORCE_t type, LEVEL_t level) {

    }
*/
    //##santo
    public void onForceDisplayed(final double force) {
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            public void run() {
                MainActivity.this.forceView.setText("" + ((int) force));
            }
        });
    }



    /*
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
*/
    public void onForceChanged(FORCE_t type, LEVEL_t level, double force, float speed_l, float speed_c) {
        final FORCE_t fORCE_t = type;
        final LEVEL_t lEVEL_t = level;
        final double d = force;
        final float f = speed_l;
        final float f2 = speed_c;
        runOnUiThread(new Runnable() {
            public void run() {
                if (MainActivity.this.routeActive && !MainActivity.this.routeInPause) {
                    if (fORCE_t != FORCE_t.UNKNOW) {
                        MainActivity.this.accForceView.hide(false, getMap(), getScreen(), getSeuil());
                        MainActivity.this.accForceView.setValue(fORCE_t, lEVEL_t);
                        Force strength = new Force(fORCE_t, lEVEL_t, MainActivity.this.lastPos, String.valueOf((int) d), String.valueOf((int) f), String.valueOf((int) f2));
                        try {
                            MainActivity.this.force_pref = MainActivity.this.getSharedPreferences("", 0);
                            SharedPreferences.Editor editor = MainActivity.this.force_pref.edit();
                            Gson gson = new Gson();
                            MainActivity.this.posList.add(strength);
                            editor.putString("force", gson.toJson(MainActivity.this.posList));
                            editor.commit();
                            return;
                        } catch (Exception e) {
                            return;
                        }
                    }
                    MainActivity.this.accForceView.hide(true, getMap(), getScreen(), getSeuil());
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

   // @Override
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
                //scoreView.hide(true);
            }
        });
    }

 /*   @Override
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
*/
 //##santo
 public void onShock(final double mG, final short raw) {
     runOnUiThread(new Runnable() {
         public void run() {
             if (!MainActivity.this.shockDetected) {
                 MainActivity.this.shockDetected = true;
                 new CountDownTimer(5000, 5000) {
                     public void onTick(long millisUntilFinished) {
                     }

                     public void onFinish() {
                         MainActivity.this.drawAttention(5);
                         MainActivity.this.shockDetected = false;
                         MainActivity.this.sendSms(MainActivity.this.getPhoneNumber(R.string.phone_select_sms_shock_key), MainActivity.this.getString(R.string.sms_shock_msg_string) + " " + String.valueOf(mG) + "mG (" + String.valueOf(raw) + ")");
                         MainActivity.this.markerManager.addMarker(MainActivity.this.googleMap, "Shock " + String.valueOf(mG) + "mG (" + String.valueOf(raw) + ")", "", MainActivity.this.lastPos, 9, false);
                     }
                 }.start();
             }
         }
     });
 }


    public void onSpeedLineKept(final int kmh, final LEVEL_t level) {
        runOnUiThread(new Runnable() {
            public void run() {
                SpeedLine speed_l = new SpeedLine(kmh, level);
                try {
                    MainActivity.this.speedLine = MainActivity.this.getSharedPreferences("", 0);
                    SharedPreferences.Editor speed_line_editor = MainActivity.this.speedLine.edit();
                    Gson gson_line = new Gson();
                    MainActivity.this.speed_line.add(speed_l);
                    speed_line_editor.putString("vitesse_ligne_droite", gson_line.toJson(MainActivity.this.speed_line));
                    speed_line_editor.commit();
                } catch (Exception e) {
                }
            }
        });
    }

    public void onSpeedCornerKept(final int kmh, final LEVEL_t level) {
        runOnUiThread(new Runnable() {
            public void run() {
                SpeedCorner speed_c = new SpeedCorner(kmh, level);
                try {
                    MainActivity.this.speedCorner = MainActivity.this.getSharedPreferences("", 0);
                    SharedPreferences.Editor speed_corner_editor = MainActivity.this.speedCorner.edit();
                    Gson gson_corner = new Gson();
                    MainActivity.this.speed_corner.add(speed_c);
                    speed_corner_editor.putString("vitesse_virage", gson_corner.toJson(MainActivity.this.speed_corner));
                    speed_corner_editor.commit();
                } catch (Exception e) {
                }
            }
        });
    }

    public void onSpeedLine() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    MainActivity.this.speedLine = MainActivity.this.getSharedPreferences("", 0);
                    SpeedLine[] speed_l = (SpeedLine[]) new Gson().fromJson(MainActivity.this.speedLine.getString("vitesse_ligne_droite", ""), SpeedLine[].class);
                    if (speed_l != null) {
                        MainActivity.this.speedView.setSpeed(SPEED_t.IN_STRAIGHT_LINE, speed_l[speed_l.length - 1].getLevel(), Integer.valueOf(speed_l[speed_l.length - 1].getSpeed_line()), true);
                    } else {
                        MainActivity.this.speedView.setSpeed(SPEED_t.IN_STRAIGHT_LINE, LEVEL_t.LEVEL_UNKNOW, Integer.valueOf(0), true);
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    public void onSpeedCorner() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    MainActivity.this.speedCorner = MainActivity.this.getSharedPreferences("", 0);
                    SpeedCorner[] speed_c = (SpeedCorner[]) new Gson().fromJson(MainActivity.this.speedCorner.getString("vitesse_virage", ""), SpeedCorner[].class);
                    if (speed_c != null) {
                        MainActivity.this.speedView.setSpeed(SPEED_t.IN_CORNERS, speed_c[speed_c.length - 1].getLevel(), Integer.valueOf(speed_c[speed_c.length - 1].getSpeed_corner()), true);
                    } else {
                        MainActivity.this.speedView.setSpeed(SPEED_t.IN_CORNERS, LEVEL_t.LEVEL_UNKNOW, Integer.valueOf(0), true);
                    }
                } catch (Exception e) {
                }
            }
        });
    }




    @Override
    public void onRecommendedSpeedChanged(SPEED_t speed_t, int kmh, LEVEL_t level, boolean valid) {
        final SPEED_t sPEED_t = speed_t;
        final LEVEL_t lEVEL_t = level;
        final int i = kmh;
        final boolean z = valid;
        runOnUiThread(new Runnable() {
            public void run() {
                MainActivity.this.speedView.setSpeed(sPEED_t, lEVEL_t, Integer.valueOf(i), z);
            }
        });
    }

    public void onLevelNotified(final LEVEL_t level) {
        runOnUiThread(new Runnable() {
            public void run() {
                switch (level) {
                    case LEVEL_3:
                        MainActivity.this.mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.yellow);
                        MainActivity.this.mediaPlayer.start();
                        return;
                    case LEVEL_4:
                        MainActivity.this.mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.orange);
                        MainActivity.this.mediaPlayer.start();
                        return;
                    case LEVEL_5:
                        MainActivity.this.mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.red);
                        MainActivity.this.mediaPlayer.start();
                        return;
                    default:
                        return;
                }
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

    //##santoo



    private void setPositionClient() {
        this.posManager = new PositionManager(this);
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            this.posManager.setPositionChangedListener(new PositionManager.PositionListener() {
                public void onRawPositionUpdate(Location location) {
                    MainActivity.this.firstPos = new LatLng(location.getLatitude(), location.getLongitude());
                }

                public void onPositionUpdate(Location firstLoc, Location lastLoc) {
                    LatLng posInitial = new LatLng(firstLoc.getLatitude(), lastLoc.getLongitude());
                    LatLng posFinal = new LatLng(firstLoc.getLatitude(), lastLoc.getLongitude());
                }

                public void onGpsStatusChange(boolean gpsFix) {
                    MainActivity.this.setGpsStatus(gpsFix);
                }
            });
        }
    }

    public void setGpsStatus(boolean active) {
        this.lock = new ReentrantLock();
        this.lock.lock();
        this.gps = true;
        this.lock.unlock();
    }

    // -------------------------------------------------------------------------------------------- //

    private void init (boolean firstLaunch) {
     /*   if (getSharedPreferences("prime", 0).getInt("prime", 0) == 0) {
            setPositionClient();

            return;
        }
        */

        smsMessageView = (TextView) findViewById(R.id.sms_message_text);


        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mapReady = false;
        initDone = true;
        appColor = new AppColor(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        appManager = new AppManager(this, this);

        //qrRequest = new QrScanRequest();
        qrRequest = new QrScanRequest();


        progressOPT = new ProgressDialog(this, R.style.InfoDialogStyle);
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


        runOnce();
        markerManager = new MarkerManager(getApplicationContext());
        speedView = new SpeedView(this);
        scoreView = new ScoreView(this);
        accForceView = new AccForceView(this);
        accForceView.hide(true, getMap(), getScreen(), getSeuil());

        backgroundView = (ImageView) findViewById(R.id.background_image);
        boxNumView = (TextView) findViewById(R.id.box_num_connected);
        drivingTimeView = (TextView) findViewById(R.id.driving_time_text);
        flagView = (ImageView) findViewById(R.id.localization_flag_image);
        no_map = (ImageView) findViewById(R.id.no_map);






        corner_n_v = ((TextView) findViewById(R.id.corner_note_view));
        brake_n_v =((TextView) findViewById(R.id.brake_note_view));
        acc_n_v =((TextView) findViewById(R.id.acc_note_view));
        avg_n_v =((TextView) findViewById(R.id.avg_note_view));
        drive_n_v =(TextView) findViewById(R.id.driving_score_view);

        infoButton = (FloatingActionButton) findViewById(R.id.button_info);
        callButton = (FloatingActionButton) findViewById(R.id.button_call);
        scanQrCodeButton = (FloatingActionButton) findViewById(R.id.button_qrcode);
        epcSettingsButton = (FloatingActionButton) findViewById(R.id.button_epc_settings);
        stopButton = (FloatingActionButton) findViewById(R.id.button_stop);
        stopButton.setVisibility(View.GONE);

        optMenu = (FloatingActionMenu) findViewById(R.id.opt_menu);
        optMenu.setClosedOnTouchOutside(false);
        menuButtonSos = (FloatingActionButton) findViewById(R.id.menu_button_sos);
        menuButtonMapRecenter = (FloatingActionButton) findViewById(R.id.menu_button_map_recenter);
        menuButtonResetCalib = (FloatingActionButton) findViewById(R.id.menu_button_reset_calibration);
        //menuButtonTracking = (FloatingActionButton) findViewById(R.id.menu_button_tracking);
        menuButtonSettings = (FloatingActionButton) findViewById(R.id.menu_button_settings);

        trackingActivated = sharedPref.getBoolean(getString(R.string.tracking_activated_key), true);
/*
        if (trackingActivated) {

            menuButtonTracking.setColorNormal(appColor.getColor(AppColor.GREEN));
            menuButtonTracking.setColorPressed(appColor.getColor(AppColor.ORANGE));
        }
        else {

            menuButtonTracking.setColorNormal(appColor.getColor(AppColor.ORANGE));
            menuButtonTracking.setColorPressed(appColor.getColor(AppColor.GREEN));
        }

        */  //btn -------tracking ----

        if (DEBUG_LOGVIEW_ON){

            debugLayout = (LinearLayout) findViewById(R.id.debug_layout);
            debugView = (TextView) findViewById(R.id.debug_view);
        }

            mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


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

            if (!PermissionHelper.checkPermissions(this, Manifest.permission.RECEIVE_SMS)) {

                ok = 0;
            }

            if (!PermissionHelper.checkPermissions(this, Manifest.permission.READ_SMS)) {

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
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS).onPermissionsDenied(new OnDenyAction() {

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

    //####santo
    protected void showInfoDialog(Marker marker) {
        CustomMarker customMarker = this.markerManager.getMarker(marker);
        if (customMarker != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View v = getLayoutInflater().inflate(R.layout.info_on_arrow, null);
            TextView measure = (TextView) v.findViewById(R.id.measure);
            ((TextView) v.findViewById(R.id.force)).setCompoundDrawablesWithIntrinsicBounds(this.markerManager.getTypeMarker(customMarker), 0, 0, 0);
            measure.setText(Html.fromHtml(this.markerManager.getDataMarker(customMarker)));
            builder.setView(v);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.close_string, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setNegativeButton("Générer PDF", new DialogInterface.OnClickListener() {
                @SuppressLint("WrongConstant")
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "Aucune application de lecture PDF!", 1).show();
                }
            });
            builder.show();
        }
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

                epcname = NameEPC.get_EPC_Name(this,i + 1);

                epcStrList[i] = epcname; //"Seuils de force " + String.valueOf(i + 1);

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


    private void recenterMap(){

        //get the center coordinates of the current displayed screen
        int mWidth= this.getResources().getDisplayMetrics().widthPixels/2;
        int mHeight= this.getResources().getDisplayMetrics().heightPixels/2;

        if (lastPos == null) {

            CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPos).zoom(MAP_ZOOM_ON_PAUSE).bearing(0).tilt(0).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            if (progress != null && progress.isShowing()) {

                progress.setMessage(getString(R.string.progress_loading_string));
            }
        }
        //Get current tracker position in the screen
        Projection projection = googleMap.getProjection();
        Point markerPoint = projection.toScreenLocation(lastPos);

        //Gets the offset x and y (the distance you want that point to move to the right and to the left)
        int offsetX = mWidth - markerPoint.x;
        int offsetY = mHeight - markerPoint.y;

        //The new position
        Point targetPoint = new Point(mWidth - offsetX , mHeight - offsetY);
        LatLng targetPosition = projection.fromScreenLocation(targetPoint);
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(targetPosition), 1000, null);
    }




    private void disableActionButtons (boolean disable) {

        FloatingActionButton[] actionBtnArray = {menuButtonSos, callButton, menuButtonResetCalib, menuButtonMapRecenter};

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

        FloatingActionButton[] actionBtnArray = {menuButtonSos, callButton, menuButtonResetCalib, menuButtonMapRecenter};

        for (int i = 0; i < actionBtnArray.length; i++) {

            if (hide) {



                actionBtnArray[i].hide(true);
            }
            else {
                if(i==2 && opt_qrcode== 0){
                    //qrcode
                        actionBtnArray[i].hide(true);

                } else if(i==3 && opt_seulforce== 0){
                    //epcbtn
                        actionBtnArray[i].hide(true);
                }else {
                    actionBtnArray[i].show(true);
                }


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

//################# form on start #########################
    private void runOnce () {
       // boolean isFirstRun = sharedPref.getBoolean("FIRSTRUN", true);
        boolean isFirstRun = sharedPref.getBoolean(getString(R.string.firstrun_key), true);

        Log.e("isFirstRun val : ", String.valueOf(isFirstRun));
        Log.e("FIRSTRUN val : ", String.valueOf(sharedPref.getBoolean(getString(R.string.firstrun_key), true)));
        if (isFirstRun)
        {
            // load and sent
            if( !flag_run_once ) {
                progressOPT.hide();
                progress.hide();
                OpenForm();
                flag_run_once = true;
            }

        }
    }

    //==============================================================================================
    private AlertDialog dialog;
    private void MessageWait () {
        AlertDialog.Builder bd = new AlertDialog.Builder(MainActivity.this);
        View mV = getLayoutInflater().inflate(R.layout.openform_valid_activity, null);
        bd.setView(mV);
        dialog = bd.create();
        Button close = (Button) mV.findViewById(R.id.form_valid_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                MainActivity.this.finish();
                System.exit(0);

 //############---------- Annulation du load config blue screen ###################
                SharedPreferences.Editor editor = sharedPref.edit();
                //editor.putBoolean("FIRSTRUN", false);
                editor.putBoolean(getString(R.string.load_alert_cfg_key), false);

                editor.apply();

                Log.e("AFTERRUN valcfg : ", String.valueOf(sharedPref.getBoolean(getString(R.string.load_alert_cfg_key), true)));

            }
        });
        dialog.setCancelable(false);
        dialog.show();
        Log.w("Wait message", "in waiting response");
    }

    protected void OpenForm () {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View mView = getLayoutInflater().inflate(R.layout.openform_activity, null);
        builder.setView(mView);
        final AlertDialog di = builder.create();
        di.setCancelable(false);

        Button form_submit = (Button) mView.findViewById(R.id.form_submit);
        form_submit.setOnClickListener(new View.OnClickListener() {

            private TextView form_name_id;
            private TextView form_phone_id;
            private TextView form_email_id;
            private TextView form_titulaire_id;

            @Override
            public void onClick(View view) {
                form_name_id = (TextView) mView.findViewById(R.id.form_name_id);
                form_phone_id = (TextView) mView.findViewById(R.id.form_phone_id);
                form_email_id = (TextView) mView.findViewById(R.id.form_email_id);
                form_titulaire_id = (TextView) mView.findViewById(R.id.form_titulaire_id);

                if( form_name_id.getText().toString().isEmpty() ||
                        form_phone_id.getText().toString().isEmpty() ||
                        form_email_id.getText().toString().isEmpty() ||
                        form_titulaire_id.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, getString(R.string.form_invalid_string), Toast.LENGTH_SHORT).show();
                } else {

               /*     runOnUiThread(new Runnable() {
                        @Override
                        public void run() { */
                          //  boolean location = false;

                         //   while( !location ) {
                            /*    if( lastLocation == null ) {
                                    try {
                                        Thread.sleep(500);
                                    }catch(InterruptedException ie) {}
                              */  //} else {

                    boolean activeopt = Connectivity.isConnected(getApplicationContext());
                    Log.e("connect za form : ", String.valueOf(Connectivity.isConnected(getApplicationContext())));
                    if( activeopt != internet_activeopt ) {
                        internet_activeopt = activeopt;
                    }

                    Log.e("connect zaInterForm : ", String.valueOf(internet_activeopt));

                    if (internet_activeopt) {

                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                    Date date = new Date();
                                    String[] form = {
                                            form_name_id.getText().toString(),
                                            form_phone_id.getText().toString(),
                                            form_email_id.getText().toString(),
                                            form_titulaire_id.getText().toString(),
                                            String.valueOf(lastLocation.getLatitude()),
                                            String.valueOf(lastLocation.getLongitude()),
                                            dateFormat.format(date)
                                    };

                                        appManager.OpenForm(form);


                                    MessageWait();
                                    di.hide();


//############# annulation du formulaire
                                    SharedPreferences.Editor editor1 = sharedPref.edit();
                                    //editor.putBoolean("FIRSTRUN", false);
                                    editor1.putBoolean(getString(R.string.firstrun_key), false);

                                    editor1.apply();


                                    Log.e("AFTERRUN val : ", String.valueOf(sharedPref.getBoolean(getString(R.string.firstrun_key), true)));

//############# annulation du formulaire
                }else {
                    //Toast.makeText(MainActivity.this, getString(R.string.form_invalid_string), Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, getString(R.string.erreur_connect), Toast.LENGTH_SHORT).show();
                }
                                  //  break;
                               // }
                          //  }
                       // }
                  //  });

                }
            }
        });
        di.show();
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



                if(opt_qrcode== 1)
                   // if(true)
                {

                    class C00431 implements DialogInterface.OnClickListener {
                        C00431() {
                        }

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }


                Intent intent = new Intent(MainActivity.this, QrScanActivity.class);
                intent.putExtra(QrScanActivity.QR_SCAN_REQUEST_PARAM, qrRequest);
                startActivityForResult(intent, QR_REQUEST_ID);


            }else {
                    alertopt();

                }

            }
        });

        epcSettingsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (final View view) {

                if(opt_seulforce== 1)
                {
                    showEpcSelectDialog();
                }else {
                    alertopt();
                }

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
                    if(opt_config_type==0){

                        menuButtonSettings.setVisibility(View.GONE);
                    }


                    hideActionButtons(true);
                }
                else {

                    menuButtonSettings.setVisibility(View.GONE);
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

        menuButtonMapRecenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recenterMap();
            }
        });



        menuButtonResetCalib.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                appManager.raz_calibration();
            }
        });


        menuButtonSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                if(opt_config_type== 1)
                {
                    startActivity(new Intent(MainActivity.this, PinLockActivity.class));
                    optMenu.close(true);

                }else {
                    alertopt();
                }



            }
        });
    }

    // -------------------------------------------------------------------------------------------- //


    private void hideOPT(){


   //-----VFAM
        if(opt_VFAM==0){
            corner_n_v.setVisibility(View.INVISIBLE);
            brake_n_v.setVisibility(View.INVISIBLE);
            acc_n_v.setVisibility(View.INVISIBLE);
            avg_n_v.setVisibility(View.INVISIBLE);
        }else {
            corner_n_v.setVisibility(View.VISIBLE);
            brake_n_v.setVisibility(View.VISIBLE);
            acc_n_v.setVisibility(View.VISIBLE);
            avg_n_v.setVisibility(View.VISIBLE);
        }
        /*
        if(opt_VFAM == 1){


            scoreView.hideFAMV(false);
        }
        else {

            scoreView.hideFAMV(true);
        }
*/

        //----- drive_note
        if(opt_note==0){
            drive_n_v.setVisibility(View.INVISIBLE);
        }else {
            drive_n_v.setVisibility(View.VISIBLE);
        }
        /*
        if(opt_note == 1){
            // onNoteChanged (0, LEVEL_t.LEVEL_UNKNOW,LEVEL_t.LEVEL_UNKNOW);
            scoreView.hideNote(false);
        }else {
            //onNoteChanged(20,LEVEL_t.LEVEL_1,LEVEL_t.LEVEL_1);
            scoreView.hideNote(true);
        }
        */

        //---- qrcode
        if(opt_qrcode==0){

            scanQrCodeButton.hide(true);
            //scanQrCodeButton.setVisibility(View.GONE);
            //hideActionButtons(true);
        }else{
            if (!optMenu.isOpened()){
                //scanQrCodeButton.show(true);
                //scanQrCodeButton.setVisibility(View.GONE);
            }
        }


   /*     if(opt_qrcode==0){

            scanQrCodeButton.setVisibility(View.GONE);
            //hideActionButtons(true);
        }else{
            //hideActionButtons(false);
           if (optMenu.isOpened()){
                 hideActionButtons(true);
                //scanQrCodeButton.setVisibility(View.GONE);
            }else{
               hideActionButtons(false);
                //scanQrCodeButton.setVisibility(View.VISIBLE);
            }

        }
        */
      /*  if(opt_qrcode==1){
            scanQrCodeButton.setVisibility(View.VISIBLE);
        }else{
            scanQrCodeButton.setVisibility(View.GONE);
        }
*/
        //---- durree
        if(opt_duree==0){
            drivingTimeView.setVisibility(View.INVISIBLE);
        }else{
            drivingTimeView.setVisibility(View.VISIBLE);
        }

        //------ Seuille frc
        if(opt_seulforce==0){

            epcSettingsButton.hide(true);
            //scanQrCodeButton.setVisibility(View.GONE);
            //hideActionButtons(true);
        }else{
            if (!optMenu.isOpened()){
                //epcSettingsButton.show(true);
                //scanQrCodeButton.setVisibility(View.GONE);
            }
        }
     /*   if(opt_seulforce== 1){
            epcSettingsButton.setVisibility(View.VISIBLE);
        }else{
            epcSettingsButton.setVisibility(View.GONE);
        }
*/
  /*      if(opt_seulforce== 0){
            epcSettingsButton.setVisibility(View.GONE);
        }else{
            if (optMenu.isOpened()){
                hideActionButtons(true);
                //epcSettingsButton.setVisibility(View.GONE);
            }else{
                hideActionButtons(false);
                //epcSettingsButton.setVisibility(View.VISIBLE);
            }


        }
*/
        //--pin btn

        if(opt_config_type==0){

            menuButtonSettings.setVisibility(View.GONE);
            //scanQrCodeButton.setVisibility(View.GONE);
            //hideActionButtons(true);
        }else{
            if (optMenu.isOpened()){
                menuButtonSettings.setVisibility(View.VISIBLE);
                //menuButtonSettings.show(true);
                //scanQrCodeButton.setVisibility(View.GONE);
            }
        }

      //  if(opt_config_type== 0){
            //menuButtonSettings.setVisibility(View.INVISIBLE);
          //  menuButtonSettings.hide(true);
      //  }else{
            //menuButtonSettings.show(true);
           // menuButtonSettings.setVisibility(View.VISIBLE);
         /*   if (!optMenu.isOpened()){
                //hideActionButtons(true);
                menuButtonSettings.setVisibility(View.VISIBLE);
            }else{
                //hideActionButtons(false);
                menuButtonSettings.setVisibility(View.INVISIBLE);
            }*/

     //   }

        //--langue opt
        if(opt_langue== 1){

            flagView.setVisibility(View.VISIBLE);
        }else{
            flagView.setVisibility(View.GONE);

        }

        //---paneau vitesse

        if(opt_panneau== 1){
            //accForceView.hide(false);
            speedView.hide(false);
        }else{
            //accForceView.hide(true);
            speedView.hide(true);
        }

        //------carte opt ----
        if(opt_carte== 1){

           // nomapFrag.getView().setVisibility(View.GONE);
            no_map.setVisibility(View.GONE);
            mapFrag.getView().setVisibility(View.VISIBLE);


        }else{
            mapFrag.getView().setVisibility(View.GONE);
            no_map.setVisibility(View.VISIBLE);
           // nomapFrag.getView().setVisibility(View.VISIBLE);
        }

        //------carte opt ----
        if(opt_screen_size== 4){
            //scanQrCodeButton.layout();

        }else{

        }



    }

    private void alertopt(){


        final AlertDialog.Builder optDisableAlert = new AlertDialog.Builder(MainActivity.this);
        optDisableAlert.setCancelable(false);
        optDisableAlert.setMessage(getString(R.string.progress_inactive_opt_string));

        optDisableAlert.setNegativeButton(getString(R.string.close_string), null);
        optDisableAlert.create().show();
        //  cancel(true);

      /*            if (progress != null) {

                        progress.show();
                        progress.setMessage(getString(R.string.progress_inactive_opt_string) );
                    }
                    */

    }


    private void notification(String msg, int Idnotif){

        Uri soundNotif = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
     /*   if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notif.setSmallIcon(R.drawable.icon_transperent);
            //notif.setColor(getResources().getColor(R.color.notification_color));
        } else {
            notif.setSmallIcon(R.mipmap.ic_launcher);
        }
        */
        notif.setSmallIcon(R.mipmap.ic_launcher);
        notif.setTicker("Preventium");
        notif.setWhen(System.currentTimeMillis());
        notif.setContentTitle("Preventium");
        notif.setContentText(msg);



        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.setContentIntent(pendingIntent);
        notif.setSound(soundNotif);

        NotificationManager nm = (NotificationManager ) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(Idnotif, notif.build());

    }


    private void compare(int opt , int obj, String msgActiv, String msgDesactiv, int notifiId){

        if(opt == 99){


            Log.e("aopt99  : ", "99");

        }else {

            if ((opt != obj) && opt < obj){

                notification(msgActiv, notifiId);

                Log.e("aopt0  : ", "0->1");

            }else if ((opt != obj) && opt > obj){

                notification(msgDesactiv+"\n"+getString(R.string.opt_desactivated_global), notifiId);

                Log.e("aopt1  : ", "1->=0");
            }


        }

    }

    ///###### chek protocol
  /*  public boolean checkProtocol(String url){
     String http = (url.substring(0, 4)).trim();
        Log.e("Protocol za : ", String.valueOf(http));
        if (http== "http"){
            return true;

        }
        else {
            return false;
        }


    }
*/

//##### Get screen size #######
    public int getScreen() {

        if (opt_screen_size==99){
            return 4;
        }else {

            return opt_screen_size;
        }

    }

    //##### Get seuil check value #######
    public int getSeuil() {
        if (opt_seulforce==99){
            return 0;
        }else {
            return opt_seulforce;
        }

    }


    //##### Get map check value #######
    public int getMap() {
        if(opt_carte==99){
            return 0;
        }else {
            return opt_carte;
        }

    }

    public void onForceView(){
        MainActivity.this.forceView = (TextView) findViewById(R.id.mark);
        if (ComonUtils.LoadPreferences("force", "force", this) == 0) {
            MainActivity.this.forceView.setVisibility(View.INVISIBLE);
        } else {
            MainActivity.this.forceView.setVisibility(View.VISIBLE);
        }
    }


    //####### Loop for check value web option ########
    public void setRepeatingAsyncTask() {

        final Handler handler = new Handler();
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {

  /*      runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
*/


                              boolean load_cfg = sharedPref.getBoolean(getString(R.string.load_alert_cfg_key), true);
                              boolean activeopt = Connectivity.isConnected(getApplicationContext());
                              Log.e("connect za : ", String.valueOf(Connectivity.isConnected(getApplicationContext())));
                              if( activeopt != internet_activeopt ) {
                                  internet_activeopt = activeopt;
                              }

                              Log.e("connect zaInter : ", String.valueOf(internet_activeopt));

                              if (internet_activeopt) {

                                  onForceView();

                                  srcFileName = ComonUtils.getIMEInumber(getApplicationContext()) + ".CFG";
                                  desFileName = String.format(Locale.getDefault(), "%s/%s", getApplicationContext().getFilesDir(), srcFileName);
                                  cfgi = reader1.read(desFileName);  // santooo
                                  Log.e("FTP cfgBol : ", String.valueOf(cfgi));

                                  Log.e("connect : ", String.valueOf(Connectivity.isConnected(getApplicationContext())));

                                  //reader1.read(desFileName);
                                  if(cfgi){


                                      //URL uValide = new URL(serveur);
                                      //Log.e("protocole : ",String.valueOf(uValide.getProtocol()));
                                      serveur = reader1.getServerUrl();
                                      if(serveur != "" && serveur!="tsis"){

                                        // boolean check=  checkProtocol(serveur);
                                         // Log.e("akor vrai : ", String.valueOf(check));

                                       //  if (check){



                                          // serveur = encode(serveur, "UTF-8");

                                          Log.e("url boucle : ",serveur);

                                          new ParseJson().execute();


                                          hideOPT();

                                          if (progressOPT != null) {
                                              progressOPT.hide();
                                          }

                            /*             }else {
                                             //#### Erreur protocol
                                             if(!load_cfg){

                                                 if (progressOPT != null) {

                                                     progressOPT.show();
                                                     progressOPT.setMessage(getString(R.string.erreur_protocol) );
                                                 }
                                             }
                                         }
*/

                                      }
                                  }else{
                                      //progresse fichier cfg introuvable


                                      if(!load_cfg){

                                          if (progressOPT != null) {

                                              progressOPT.show();
                                              progressOPT.setMessage(getString(R.string.progress_cfg_opt_string) );
                                          }
                                      }



                                  }
                              }
/*
                          }

        });
*/




                        } catch (Exception e) {
                            // error, do something
                        }

                    }
                });
            }
        };

        timer.schedule(task, 0, 1*1000);  // interval of one minute (1 sec)

    }

    // -------------------------------------------------------------------------------------------- //
    //instancier handler box
    HandlerBox box_leurre = new HandlerBox(this);
    //-------------------//

    @SuppressLint("StaticFieldLeak")
    class ParseJson extends AsyncTask<String, String, Integer> {

        @Override
        protected Integer doInBackground(String... param) {

            //--------debut
            Log.e("CFG ok","Chargement sur le sous-domaine "+serveur+" ...");

            String imei = StatsLastDriving.getIMEI(MainActivity.this);
            ParseJsonData jsonData = new ParseJsonData();

            boolean connected = Connectivity.isConnected(getApplicationContext());
            if( connected ) {
                String jsonString1 = jsonData.makeServiceCall(serveur + "/index.php/get_config/" + imei);

                if( jsonString1 != null && jsonString1.toString().length() > 0) {
                    try {
                        JSONObject conf = new JSONObject(jsonString1);
                        JSONObject config = conf.getJSONObject("config");

                        int opt_qrcode_web = Integer.parseInt(config.optString("qrcode"));
                        int opt_carte_web = Integer.parseInt(config.optString("affiche_carte"));
                        int opt_panneau_web = Integer.parseInt(config.optString("paneau_vitesse_droite"));
                        int opt_note_web = Integer.parseInt(config.optString("note_sur_20"));
                        int opt_VFAM_web = Integer.parseInt(config.optString("VFAM"));
                        int opt_duree_web = Integer.parseInt(config.optString("duree"));
                        int opt_seulforce_web = Integer.parseInt(config.optString("seulforce"));
                        int opt_config_type_web = Integer.parseInt(config.optString("config_type"));
                        int opt_langue_web = Integer.parseInt(config.optString("langue"));
                        int opt_screen_size_web = Integer.parseInt(config.optString("taille_ecran"));

                        int opt_leurre = Integer.parseInt(config.optString("leurre"));


                        compare(opt_qrcode, opt_qrcode_web, getString(R.string.opt_qrcod_activated) + "\n", getString(R.string.opt_qrcod_desactivated), id_notif_qr);
                        compare(opt_carte, opt_carte_web, getString(R.string.opt_carte_activated), getString(R.string.opt_carte_desactivated) + "\n", id_notif_map);
                        compare(opt_panneau, opt_panneau_web, getString(R.string.opt_panneau_activated), getString(R.string.opt_panneau_desactivated) + "\n", id_notif_pan);
                        compare(opt_note, opt_note_web, getString(R.string.opt_note_activated), getString(R.string.opt_note_desactivated) + "\n", id_notif_note);
                        compare(opt_VFAM, opt_VFAM_web, getString(R.string.opt_VFAM_activated), getString(R.string.opt_VFAM_desactivated) + "\n", id_notif_avfm);
                        compare(opt_duree, opt_duree_web, getString(R.string.opt_duree_activated), getString(R.string.opt_duree_desactivated) + "\n", id_notif_duree);
                        compare(opt_seulforce, opt_seulforce_web, getString(R.string.opt_seulforce_activated), getString(R.string.opt_seulforce_desactivated) + "\n", id_notif_seuil);
                        compare(opt_config_type, opt_config_type_web, getString(R.string.opt_config_type_activated), getString(R.string.opt_config_type_desactivated) + "\n", id_notif_conf);
                        compare(opt_langue, opt_langue_web, getString(R.string.opt_langue_desactivated), getString(R.string.opt_langue_desactivated) + "\n", id_notif_lang);

                        opt_qrcode = opt_qrcode_web;
                        opt_carte = opt_carte_web;
                        opt_panneau = opt_panneau_web;
                        opt_note = opt_note_web;
                        opt_VFAM = opt_VFAM_web;
                        opt_seulforce = opt_seulforce_web;
                        opt_config_type = opt_config_type_web;
                        opt_langue = opt_langue_web;
                        opt_duree = opt_duree_web;
                        opt_screen_size = opt_screen_size_web;

                        //get value of leurre by francisco

                        box_leurre.set_active_from_serveur(opt_leurre);
                        Log.d("HandlerBox","activation leurre  : " +opt_leurre);

                        return opt_qrcode;
                    } catch (JSONException e) {}
                }
            }

            return null;
        }  ///-----faran doInBack
    }

    // -------------------------------------------------------------------------------------------- //
//=======

    //############### Language manager #################
    private void localizationFlag(){

        SharedPreferences preferences = getSharedPreferences(APPPREFERENCES, Context.MODE_PRIVATE);
       // String language = preferences.getString("language", null);
        String language = preferences.getString(getString(R.string.select_language_key), null);

        Log.e("langue azo : ", String.valueOf(language));

        if(language==null){
            language = Locale.getDefault().getLanguage();
        }
        switch (language){
            case "en":
                flagView.setImageResource(R.drawable.ic_us_flag);
                break;
            case "fr":
                flagView.setImageResource(R.drawable.ic_fr_flag);
                break;
            case "es":
                flagView.setImageResource(R.drawable.ic_es_flag);
                break;
            case "pl":
                flagView.setImageResource(R.drawable.ic_pl_flag);
                break;
        }

        Runnable displayFlag = new Runnable() {
            @Override
            public void run() {
                flagView.setVisibility(View.INVISIBLE);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(displayFlag, DURATION);

    }


    public static MainActivity instance() {
        return activity;
    }

    public void updateText(final String newSms){
        smsMessageView.setVisibility(View.VISIBLE);
        smsMessageView.setTextColor(getResources().getColor(R.color.colorAppRed));

        if(smsMessageView.getText().toString().equals(getString(R.string.no_pending_message_string))){
            smsMessageView.setText(newSms);
        }
        else{
            smsMessageView.setText(smsMessageView.getText().toString() + "            " + newSms);
        }

        smsMessageView.setSelected(true);

        Runnable dismissMessage = new Runnable() {
            @Override
            public void run() {
                smsMessageView.setVisibility(View.INVISIBLE);
                smsMessageView.setTextColor(getResources().getColor(R.color.colorAppGrey));
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(dismissMessage, MESSAGE_DURATION);
    }

}
