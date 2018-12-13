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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firetrap.permissionhelper.helper.PermissionHelper;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.database.DatabaseHelper;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.SCORE_t;
import com.preventium.boxpreventium.enums.SPEED_t;
import com.preventium.boxpreventium.enums.STATUS_t;
import com.preventium.boxpreventium.location.CustomMarker;
import com.preventium.boxpreventium.location.CustomMarkerData;
import com.preventium.boxpreventium.location.FileManagerMarker;
import com.preventium.boxpreventium.location.MarkerManager;
import com.preventium.boxpreventium.location.PositionManager;
import com.preventium.boxpreventium.manager.AppManager;
import com.preventium.boxpreventium.manager.DialogManager;
import com.preventium.boxpreventium.manager.Force;
import com.preventium.boxpreventium.manager.SpeedCorner;
import com.preventium.boxpreventium.manager.SpeedLine;
import com.preventium.boxpreventium.manager.StatsLastDriving;
import com.preventium.boxpreventium.manager.interfaces.AppManagerListener;
import com.preventium.boxpreventium.module.DiscoverBox;
import com.preventium.boxpreventium.module.HandlerBox;
import com.preventium.boxpreventium.module.Load.LoadOption;
import com.preventium.boxpreventium.module.Load.LoadPermission;
import com.preventium.boxpreventium.module.Load.LoadFormateur;
import com.preventium.boxpreventium.server.CFG.ReaderCFGFile;
import com.preventium.boxpreventium.server.EPC.DataEPC;
import com.preventium.boxpreventium.server.EPC.NameEPC;
import com.preventium.boxpreventium.utils.App;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.Connectivity;
import com.preventium.boxpreventium.utils.DataLocal;
import com.preventium.boxpreventium.widget.Widget;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.provider.Telephony.Carriers.PASSWORD;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, AppManagerListener, LocationListener, App.AppListener {

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
    private AppManager appManager;
    private ScoreView scoreView;
    private SpeedView speedView;
    private AccForceView accForceView;
    private MarkerView markerView;
    private MapView mapView;

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
    private FloatingActionButton menuButtonSettings;
    private FloatingActionButton stop_parcour;

    private boolean is_corner_show = false;
    private ImageView acc_image;
    private ImageView corner_image;
    private ImageView brake_image;

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
    private boolean shockDetected = false;
    private SharedPreferences sharedPref;

    private QrScanRequest qrRequest;
    private boolean routeActive = false;
    private boolean routeInPause = false;
    private int qrSmsTimeout = 0;
    private int selectedEpcFile = 0;
    private int locFilterTimeout = 0;

    private boolean stop = false;
    private boolean trackingActivated = false;
    private App app;

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
    private int opt_force_mg = 99;
    private int hide_V_lat = 99;
    private int opt_sonore = 99;
    private int opt_button_parcours = 99;

    private NotificationCompat.Builder notif;

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
    private boolean verifDm = true;
    private String serveur ="";
    ReaderCFGFile reader1 = new ReaderCFGFile();
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
    public boolean gps;

    private String local_file;
    private String[] epc_data = null;
    private String remote_file;
    private static final String USERNAME = "box.preventium";
    private static final String HOSTNAME = "www.preventium.fr";

    private final long MESSAGE_DURATION = 10800000L;
    private TextView smsMessageView;
    private static MainActivity activity;
    private boolean alertqrscan = false;

    public int vitesse_ld;
    public int vitesse_vr;

    public int color_a, color_v, color_f, color_m;
    private String testL = "LD = ", testC = "LC = ", testL1 = "LD1 = ", testC1 = "LC1 = ";

    private FileManagerMarker fileManagerMarker;
    private LoadPermission permission;
    private PermissionHelper.PermissionBuilder permissionRequest;
    private DialogManager dialogManager;

    public void setPermissionRequest(PermissionHelper.PermissionBuilder permissionRequest) {
        this.permissionRequest = permissionRequest;
    }

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

    //######donw s
    @SuppressLint("StaticFieldLeak")
    private static class downloadAndSaveFile extends AsyncTask<String, Integer, Boolean> {
        private WeakReference<Context> contextWeakReference;
        private downloadAndSaveFile(Context context) {
            this.contextWeakReference = new WeakReference<>(context);
        }

        protected Boolean doInBackground(String... params) {
            Context context = contextWeakReference.get();
            if (context != null) {
                if (((MainActivity)context).download().booleanValue()) {
                    return Boolean.valueOf(true);
                }
            }
            return Boolean.valueOf(false);
        }

        protected void onPostExecute(Boolean sucess) {
            Context context = contextWeakReference.get();
            if (context != null) {
                MainActivity main = (MainActivity) context;
                if (sucess.booleanValue()) {
                    main.showEpcSelectDialog();
                } else {
                    main.download();
                }
            }
        }
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //permission
        permission = new LoadPermission(this);
        if (permission.checkPermissions() > 0) {
            if (savedInstanceState == null) {
                init(true);
                notif = new NotificationCompat.Builder(this);
                notif.setAutoCancel(true);

                localizationFlag();
            } else {
                init(false);
            }
        } else {
            permission.requestPermissions(new LoadPermission.CallbackPermission() {
                @Override
                public void onCall() {
                    init(true);
                }
            });
        }

        // haha
        // Toast.makeText(this.getApplicationContext(), "OnCreate", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {

        // onResume load
        get_one_lance_ptions();

        Log.e("VERIFdm_resume : ", String.valueOf(verifDm));
        if (!PositionManager.isLocationEnabled(getApplicationContext())) {
            showLocationAlert();
        }

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
        }

        if( permission == null ) {
            permission = new LoadPermission(this);
        }

        int permissionsGranted = permission.checkPermissions();
        if (permissionsGranted > 0) {
            if (!initDone) {

                init(true);
            }
        }
        else if (permissionsGranted == 0) {
            permission.requestPermissions(new LoadPermission.CallbackPermission() {
                @Override
                public void onCall() {
                    init(true);
                }
            });
        }

        if (Connectivity.isConnected(getApplicationContext())) {
            if (progress != null) {
                progress.setMessage(getString(R.string.network_alert_string));
            }
        }

        super.onResume();

        // haha
        // Toast.makeText(this.getApplicationContext(), "OnResume", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPause() {
        if (progress != null) {
            progress.dismiss();
        }
        // Toast.makeText(this.getApplicationContext(), "OnPause", Toast.LENGTH_LONG).show();
        super.onPause();
    }

    @Override
    public void onStop() {
        verifDm = true;
        Log.e("VERIFdm_stop : ", String.valueOf(verifDm));
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();

        // unreceiver
        DiscoverBox.stopReceiver(this);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
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
            if(this.qrRequest.driverIdEnabled || this.qrRequest.vehicleFrontOnStartEnabled || this.qrRequest.vehicleFrontOnStopEnabled  ){
                this.alertqrscan = true;
            }
        }

        if( requestCode == 7010 && data != null ) {
            File file = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
            // add file
            if( fileManagerMarker.setFile(file) ) {
                // update file
                markerView.setUpdateFile(file);
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

    private void setMapListeners() {
        this.googleMap.setOnMapLongClickListener(mapView);
        this.googleMap.setOnMapClickListener(mapView);
        this.googleMap.setOnInfoWindowClickListener(mapView);
        this.googleMap.setOnMarkerClickListener(markerView);
    }

    public AppManager getAppManager() {
        return appManager;
    }

    public MarkerManager getMarkerManager() {
        return markerManager;
    }

    public MarkerView getMarkerView() {
        return markerView;
    }

    public FileManagerMarker getFileManagerMarker() {
        return fileManagerMarker;
    }

    public GoogleMap getGoogleMap() {
        return googleMap;
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

        if( (status != STATUS_t.CHECK_ACTIF &&
                status != STATUS_t.IMEI_INACTIF &&
                status != STATUS_t.GETTING_CFG &&
                status != STATUS_t.GETTING_EPC &&
                status != STATUS_t.GETTING_DOBJ) && !app.getIs() ) {
            app.init();
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
                            // MainActivity.this.progress.setMessage(MainActivity.this.getString(R.string.loading_string));
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
                        // MainActivity.this.googleMap.clear();
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
                            // MainActivity.this.googleMap.getUiSettings().setAllGesturesEnabled(false);
                            if (MainActivity.this.startMarker == null) {
                                MainActivity.this.startMarker = MainActivity.this.markerManager.addMarker(MainActivity.this.googleMap, "" + ComonUtils.currentTime(), "", MainActivity.this.lastPos, 15, false);
                            }
                        }
                        MainActivity.this.speed_line.clear();
                        MainActivity.this.speed_corner.clear();

                        // last data
                        onLastAlertData();

                        // set to stop
                        // setButtonParcours(R.drawable.ic_stop_black_24dp);
                        // setParcoursActive(true);

                        return;
                    case PAR_RESUME:
                        MainActivity.this.routeActive = true;
                        MainActivity.this.routeInPause = false;
                        if (MainActivity.this.mapReady) {
                            // MainActivity.this.googleMap.getUiSettings().setAllGesturesEnabled(false);
                            if (MainActivity.this.resumeMarker == null) {
                                MainActivity.this.resumeMarker = MainActivity.this.markerManager.addMarker(MainActivity.this.googleMap, "" + ComonUtils.currentTime(), "", MainActivity.this.lastPos, 18, false);
                                return;
                            }

                            // lastAlert
                            onLastAlertData();
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
                                // MainActivity.instance().Alert("Force apparait sur la carte", Toast.LENGTH_LONG);
                                MainActivity.this.displayForceMap();
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

                        // set to stop
                        // setButtonParcours(R.drawable.ic_play_arrow);
                        // setParcoursActive(false);

                        return;
                    default:
                        return;
                }
                if (status == STATUS_t.PAR_PAUSING_WITH_STOP) {
                    if( !stop ) {
                        // check if Over time
                        if( appManager.isWorkTimeOver() ) {
                            dialogManager.askEndParcoursConfirm();
                        }
                    }
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

    @Override
    public void onStatutChanged(final STATUS_t status) {

    }

    private void displayForceMap () {
        try {
            this.force_pref = MainActivity.this.getSharedPreferences("", 0);
            Force[] force = (Force[]) new Gson().fromJson(MainActivity.this.force_pref.getString("force", ""), Force[].class);
            if (force != null) {
                for (int i = 0; i < force.length; i++) {
                    this.forceMarker = MainActivity.this.markerManager.addMarker(
                            this.googleMap,
                            force[i].getForce(),
                            "",
                            new LatLng(force[i].getLoc().latitude, force[i].getLoc().longitude),
                            this.getForceCode(force[i].getType_X(),force[i].getLevel_X()),
                            false
                    );
                    double lat1 = force[i].getLoc().latitude;
                    double lng1 = force[i].getLoc().longitude;
                    double lat2 = force[i + 1].getLoc().latitude;
                    double dLon = force[i + 1].getLoc().longitude - lng1;
                    // flèche grise
                    // MainActivity.this.googleMap.addMarker(new MarkerOptions().position(force[i + 1].getLoc()).rotation((float) Math.toDegrees(Math.atan2(Math.sin(dLon) * Math.cos(lat2), (Math.cos(lat1) * Math.sin(lat2)) - ((Math.sin(lat1) * Math.cos(lat2)) * Math.cos(dLon))))).anchor(-0.65f, 0.5f).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.arrow_head))));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                                if((opt_qrcode == 1) && alertqrscan) {
                                    drawAttention(5);
                                    showQrRequestAlert();

                                    appManager.add_ui_timer(qrSmsTimeout, QR_SEND_ON_START_SMS_TMR);
                                }
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
                            if(opt_qrcode == 1 && alertqrscan) {
                                drawAttention(5);
                                showQrRequestAlert();
                                appManager.add_ui_timer(qrSmsTimeout, QR_SEND_ON_END_SMS_TMR);
                            }
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
    public void onNumberOfBoxChanged (final int nb, final boolean isBM) {
        HandlerBox box_leurre = new HandlerBox(this);
        final int active_leurre =  box_leurre.get_active_from_serveur();

        // skip on no BM
        // if( !isBM ) return;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                // si leurre active
                if( active_leurre == 1 ) {
                    if (nb > 0) {
                        corner_n_v.setVisibility(View.VISIBLE);
                        triangleCorner(true);
                    } else {
                        corner_n_v.setVisibility(View.VISIBLE);
                        triangleCorner(false);
                    }

                    boxNumView.setTextColor(Color.GREEN);

                    if( isBM ) { // BM existe même avec le leurre activé
                        corner_n_v.setVisibility(View.VISIBLE);
                        triangleCorner(true);
                    }
                } else {
                    corner_n_v.setVisibility(View.VISIBLE);
                    triangleCorner(true);

                    if( nb <= 0 )
                        boxNumView.setTextColor(Color.RED);
                    else
                        boxNumView.setTextColor(Color.GRAY);
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

    public void onForceDisplayed(final double force) {
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            public void run() {
                MainActivity.this.forceView.setText("" + ((int) force));
            }
        });
    }

    /**
     * cette fonction procède à l'affichage de force sur l'écran
     * donc, représenté par des flèche
     */
    public void onForceChanged(FORCE_t type, LEVEL_t level, double force, float speed_l, float speed_c) {
        final FORCE_t fORCE_t = type;
        final LEVEL_t lEVEL_t = level;
        final double d = force;
        final float f = speed_l;
        final float f2 = speed_c;

        // init data
        Widget.get().setForce(fORCE_t);
        Widget.get().setForceColor(level);

        runOnUiThread(new Runnable() {
            public void run() {

                Log.v("Force", "onChange");

                if (MainActivity.this.routeActive && !MainActivity.this.routeInPause) {
                    if (fORCE_t != FORCE_t.UNKNOW) {

                        // ne se déclenche que tout les 10mn
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

                // save data
                Widget.get().setNote(note_par);
                Widget.get().setNoteColor(level_par);
                Widget.get().setNoteColorAVG(level_5_days);
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

                    vitesse_ld = speed_l.getSpeed_line();
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

                    vitesse_vr = speed_c.getSpeed_corner();
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
                if(opt_panneau==1)
                    MainActivity.this.speedView.setSpeed(sPEED_t, lEVEL_t, Integer.valueOf(i), z);
            }
        });
    }

    public void onLevelNotified(final LEVEL_t level) {
        runOnUiThread(new Runnable() {
            public void run() {

                // opt: 1 bip {1,2,3}
                // opt: 2 voix {4,5,6}

                if( opt_sonore == -1 ) {
                    if (opt_sonore > 3) { // par voix
                        try {
                            switch (level) {
                                case LEVEL_3:
                                    if (opt_sonore == 6) {
                                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.vigilance);
                                        mediaPlayer.start();
                                    }
                                    return;
                                case LEVEL_4:
                                    if (opt_sonore > 5) {
                                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.attention);
                                        mediaPlayer.start();
                                    }
                                    return;
                                case LEVEL_5:

                                    if (opt_sonore > 3) {
                                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.dangereux);
                                        mediaPlayer.start();
                                    }
                                    return;
                                default:
                                    return;
                            }
                        }catch(Exception e) {
                           // Alert(e.getMessage(), Toast.LENGTH_LONG);
                        }

                    } else {

                        try {
                            switch (level) {
                                case LEVEL_3:
                                    if( opt_sonore > 2) {
                                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.bip);
                                        mediaPlayer.start();
                                    }
                                    return;
                                case LEVEL_4:
                                    if( opt_sonore > 1) {
                                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.bip_bip);
                                        mediaPlayer.start();
                                    }
                                    return;
                                case LEVEL_5:
                                    MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.biiip);
                                    mediaPlayer.start();
                                    return;
                                default:
                                    return;
                            }

                        } catch (Exception e) {
                           // Alert(e.getMessage(), Toast.LENGTH_LONG);
                        }
                    }
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

    public void setGpsStatus(boolean active) {
        this.lock = new ReentrantLock();
        this.lock.lock();
        this.gps = true;
        this.lock.unlock();
    }

    @Override
    public void onCrash () {
        app.appCrash();
    }

    @Override
    public void checkRestart() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // finish it
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(-1);
    }

    // -------------------------------------------------------------------------------------------- //
    private void init (boolean firstLaunch) {

        smsMessageView = (TextView) findViewById(R.id.sms_message_text);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mapReady = false;
        initDone = true;
        appColor = new AppColor(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // manager
        appManager = new AppManager(this, this);
        dialogManager = new DialogManager(this);
        fileManagerMarker = new FileManagerMarker(this);
        app = new App(this, this);

        // view
        markerView = new MarkerView(this);
        mapView = new MapView(this);

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

        /* options web */
        get_one_lance_ptions();

        markerManager = new MarkerManager(this);
        speedView = new SpeedView(this);
        scoreView = new ScoreView(this);
        accForceView = new AccForceView(this);
        accForceView.hide(true, getMap(), getScreen(), getSeuil());

        backgroundView = (ImageView) findViewById(R.id.background_image);
        boxNumView = (TextView) findViewById(R.id.box_num_connected);
        drivingTimeView = (TextView) findViewById(R.id.driving_time_text);
        flagView = (ImageView) findViewById(R.id.localization_flag_image);
        no_map = (ImageView) findViewById(R.id.no_map);

        forceView = (TextView) findViewById(R.id.mark);

        corner_n_v = ((TextView) findViewById(R.id.corner_note_view));
        brake_n_v =((TextView) findViewById(R.id.brake_note_view));
        acc_n_v =((TextView) findViewById(R.id.acc_note_view));
        avg_n_v =((TextView) findViewById(R.id.avg_note_view));
        drive_n_v =(TextView) findViewById(R.id.driving_score_view);

        acc_image = (ImageView) findViewById(R.id.acc_image);
        corner_image = (ImageView) findViewById(R.id.corner_image);
        brake_image = (ImageView) findViewById(R.id.brake_image);
        // pour corner
        corner_n_v.setVisibility(View.INVISIBLE);

        infoButton = (FloatingActionButton) findViewById(R.id.button_info);
        callButton = (FloatingActionButton) findViewById(R.id.button_call);
        scanQrCodeButton = (FloatingActionButton) findViewById(R.id.button_qrcode);
        epcSettingsButton = (FloatingActionButton) findViewById(R.id.button_epc_settings);
        // stopButton = (FloatingActionButton) findViewById(R.id.button_stop);
        // stopButton.setVisibility(View.GONE);

        optMenu = (FloatingActionMenu) findViewById(R.id.opt_menu);
        optMenu.setClosedOnTouchOutside(false);
        menuButtonSos = (FloatingActionButton) findViewById(R.id.menu_button_sos);
        menuButtonMapRecenter = (FloatingActionButton) findViewById(R.id.menu_button_map_recenter);
        menuButtonResetCalib = (FloatingActionButton) findViewById(R.id.menu_button_reset_calibration);
        menuButtonResetCalib.setVisibility(View.GONE);
        //menuButtonTracking = (FloatingActionButton) findViewById(R.id.menu_button_tracking);
        menuButtonSettings = (FloatingActionButton) findViewById(R.id.menu_button_settings);
        stop_parcour = (FloatingActionButton) findViewById(R.id.stop_parcour);
        trackingActivated = sharedPref.getBoolean(getString(R.string.tracking_activated_key), true);

        if (DEBUG_LOGVIEW_ON){
            debugLayout = (LinearLayout) findViewById(R.id.debug_layout);
            debugView = (TextView) findViewById(R.id.debug_view);
        }

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (firstLaunch) {
            mapFrag.setRetainInstance(true);
        }

        mapFrag.getMapAsync(this);
        // orientation
        onOrientationlistener();
    }

    private OrientationEventListener mOrientationListener;
    private boolean orientation = false;
    public void onOrientationlistener () {

        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                if (mOrientationListener.canDetectOrientation() == true) {
                    mOrientationListener.enable();
                    if( MainActivity.this.orientation )
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                } else {
                    mOrientationListener.disable();
                }
            }
        };

        mOrientationListener.onOrientationChanged(getRequestedOrientation());
        if( !orientation ) {
            orientation = true;
        }
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

        if( opt_qrcode != 1 ) return;

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
        String msg = "attention, vous vous approchez du marqueur '" + customMarker.getTitle() + "'\n";

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

    public void askCalibrationConfirm() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage(getString(R.string.calibre_confirm_string));
        builder.setCancelable(false);

        builder.setNegativeButton(getString(R.string.no_string), null);
        builder.setPositiveButton(getString(R.string.yes_string), new DialogInterface.OnClickListener() {

            @Override
            public void onClick (DialogInterface dialogInterface, int i) {

                appManager.raz_calibration();
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

    private ScaleGestureDetector mScaleGestureDetector;

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
        int mWidth = this.getResources().getDisplayMetrics().widthPixels/2;
        int mHeight = this.getResources().getDisplayMetrics().heightPixels/2;

        // "Recenter bouton", Toast.LENGTH_LONG);
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
        try {
            Point targetPoint = new Point(mWidth - offsetX, mHeight - offsetY);
            LatLng targetPosition = projection.fromScreenLocation(targetPoint);
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(targetPosition), 1000, null);
        }catch(Exception e){ e.printStackTrace(); }
    }

    private void hideActionButtons (boolean hide) {

        FloatingActionButton[] actionBtnArray = {menuButtonSos, callButton, menuButtonMapRecenter, stop_parcour}; // menuButtonResetCalib,

        for (int i = 0; i < actionBtnArray.length; i++) {

            if (hide) {
                // actionBtnArray[i].hide(true);
                actionBtnArray[i].setVisibility(View.GONE);
            }
            else {
                /*if( true /*i==2 && opt_qrcode == 0){
                    //qrcode
                    // actionBtnArray[i].hide(true);
                    //actionBtnArray[i].setVisibility(View.GONE);

                } else */ if(i==3 && opt_seulforce== 0){
                    //epcbtn
                    //actionBtnArray[i].hide(true);
                    //actionBtnArray[i].setVisibility(View.GONE);
                }else {
                    // actionBtnArray[i].show(true);
                    actionBtnArray[i].setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void drawAttention (int seconds) {
        if( opt_qrcode != 1 ) return;
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

    private ToneGenerator tone;
    private void beep (int seconds) {
        try {
            int ms = seconds * 1000;
            tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

            new CountDownTimer(ms, 500) {
                public void onTick(long millisUntilFinished) {
                    tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                }

                public void onFinish() {
                }

            }.start();
        }catch(Exception e) {
            /* on passe */
        }
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
                        // on affiche le dialog Marker
                        markerView.setDialogMarker(customMarker);
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



                if(opt_qrcode== 1 /* || opt_qrcode== 99 */)
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

        //#### santo epc test
        this.epcSettingsButton.setOnClickListener(new View.OnClickListener() {

            class C00441 implements DialogInterface.OnClickListener {
                C00441() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }

            public void onClick(View view) {
                MainActivity.this.remote_file = "/" + ComonUtils.getIMEInumber(MainActivity.this) + "_EPC.NAME";
                MainActivity.this.local_file = "/sdcard/" + ComonUtils.getIMEInumber(MainActivity.this) + "_EPC.NAME";
                MainActivity.this.showEpcSelectDialog();
            }
        });

        /*
        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {
                dialogManager.askEndParcoursConfirm();
            }
        });
        */

        stop_parcour.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {
                dialogManager.askEndParcoursConfirm();
            }
        });

        optMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {

            @Override
            public void onMenuToggle(boolean opened) {

                if (optMenu.isOpened()) {
                    if(opt_config_type == 0){
                        menuButtonSettings.setVisibility(View.GONE);
                    }else {
                        menuButtonSettings.setVisibility(View.VISIBLE);
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
                askCalibrationConfirm();
            }
        });

        menuButtonSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                if(opt_config_type== 1 || opt_config_type== 99)
                {
                    startActivity(new Intent(MainActivity.this, PinLockActivity.class));
                    optMenu.close(true);

                }else {
                    alertopt();
                }
            }
        });

        // avfm listener
        acc_n_v.setOnClickListener(new View.OnClickListener(){ // A

            @Override
            public void onClick (View view) {
                bulleToast(getString(R.string.acc_help_string), (LinearLayout)findViewById(R.id.layout_acc));
            }
        });

        corner_n_v.setOnClickListener(new View.OnClickListener(){ // V

            @Override
            public void onClick (View view) {
                bulleToast(getString(R.string.corner_help_string), (LinearLayout)findViewById(R.id.layout_corner));
            }
        });

        brake_n_v.setOnClickListener(new View.OnClickListener(){ // F

            @Override
            public void onClick (View view) {
                bulleToast(getString(R.string.brake_help_string), (LinearLayout)findViewById(R.id.layout_brake));
            }
        });
    }

    // -------------------------------------------------------------------------------------------- //
    public void manageOption(){

        opt_qrcode = sharedPref.getInt("qrcode", 0);
        opt_carte = sharedPref.getInt("affiche_carte", 0);
        opt_panneau = sharedPref.getInt("paneau_vitesse_droite", 0);
        opt_note = sharedPref.getInt("note_sur_20", 0);
        opt_VFAM = sharedPref.getInt("VFAM", opt_VFAM);
        opt_duree = sharedPref.getInt("duree", opt_duree);
        opt_seulforce = sharedPref.getInt("seulforce", opt_seulforce);
        opt_config_type = sharedPref.getInt("config_type", 0);
        opt_langue = sharedPref.getInt("langue", 0);
        opt_screen_size = sharedPref.getInt("taille_ecran", 4);
        opt_force_mg = sharedPref.getInt("force_mg", 0);
        hide_V_lat = sharedPref.getInt("leurre", 0);
        opt_sonore = sharedPref.getInt("voix", 0);
        opt_button_parcours = sharedPref.getInt("btn_menu", 0);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //-----VFAM
                if(opt_VFAM==0){
                    corner_n_v.setVisibility(View.INVISIBLE);
                    brake_n_v.setVisibility(View.INVISIBLE);
                    acc_n_v.setVisibility(View.INVISIBLE);
                    avg_n_v.setVisibility(View.INVISIBLE);
                }else {
                    if( hide_V_lat == 0)
                        corner_n_v.setVisibility(View.VISIBLE);
                    brake_n_v.setVisibility(View.VISIBLE);
                    acc_n_v.setVisibility(View.VISIBLE);
                    avg_n_v.setVisibility(View.VISIBLE);
                }

                if( opt_button_parcours == 0 ) {
                    stop_parcour.setVisibility(View.GONE);
                }

                //----- drive_note
                if(opt_note==0){
                    drive_n_v.setVisibility(View.INVISIBLE);
                }else {
                    drive_n_v.setVisibility(View.VISIBLE);
                }

                //---- qrcode
                if(opt_qrcode != 1 ){
                    scanQrCodeButton.hide(true);
                    scanQrCodeButton.setVisibility(View.GONE);
                }

                //---- durree
                if(opt_duree==0){
                    drivingTimeView.setVisibility(View.INVISIBLE);
                }else{
                    drivingTimeView.setVisibility(View.VISIBLE);
                }

                //------ Seuille frc
                if(opt_seulforce == 0){
                    epcSettingsButton.hide(true);
                }

                if(opt_config_type == 0){
                    menuButtonSettings.setVisibility(View.GONE);
                }else{
                    if (optMenu.isOpened()){
                        menuButtonSettings.setVisibility(View.VISIBLE);
                    }
                }

                //--langue opt
                if(opt_langue== 1 || opt_langue== 99){

                    flagView.setVisibility(View.VISIBLE);
                }else{
                    flagView.setVisibility(View.GONE);

                }

                //---paneau vitesse
                if(opt_panneau == 0){
                    speedView.hide(true);
                }else{
                    speedView.hide(false);
                }

                //------carte opt ----
                if(opt_carte == 1 || opt_carte == 99){
                    no_map.setVisibility(View.GONE);
                    mapFrag.getView().setVisibility(View.VISIBLE);
                }else{
                    mapFrag.getView().setVisibility(View.GONE);
                    no_map.setVisibility(View.VISIBLE);
                }

                //------force mg ----
                if(opt_force_mg== 1){
                    forceView.setVisibility(View.VISIBLE);
                }else{
                    forceView.setVisibility(View.INVISIBLE);
                }
                // pour tablet et formateur
                boolean is_formateur = sharedPref.getInt("formateur", 0) == 1 ? true : false;
                new LoadFormateur(MainActivity.this, is_formateur).init();
            }
        });
    }

    private void alertopt(){
        final AlertDialog.Builder optDisableAlert = new AlertDialog.Builder(MainActivity.this);
        optDisableAlert.setCancelable(false);
        optDisableAlert.setMessage(getString(R.string.progress_inactive_opt_string));

        optDisableAlert.setNegativeButton(getString(R.string.close_string), null);
        optDisableAlert.create().show();
    }


    private void notification(String msg, int Idnotif){

        Uri soundNotif = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

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

    //##### Get config type check value #######
    public int getSet() {
        if (opt_config_type==99){
            return 0;
        }else {
            return opt_config_type;
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

    // -------------------------------------------------------------------------------------------- //
    public void get_one_lance_ptions(){
        boolean opt = false;
        try {

            boolean options = (boolean)DataLocal.get(MainActivity.this).getValue("options_fonc", false);
            boolean load_cfg = sharedPref.getBoolean(getString(R.string.load_alert_cfg_key), true);
            boolean activeopt = Connectivity.isConnected(getApplicationContext());

            if (activeopt) {
                reader1 = ComonUtils.getCFG(MainActivity.this);

                if ( !reader1.getServerUrl().equals("") ) {
                    serveur = reader1.getServerUrl();
                    if ( serveur != "" ) {
                        LoadOption option = new LoadOption(MainActivity.this);
                        option.getOption(serveur);

                        if (progressOPT != null) {
                            progressOPT.hide();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------------------------- //
    //############### Language manager #################
    private void localizationFlag(){
        SharedPreferences preferences = getSharedPreferences(APPPREFERENCES, Context.MODE_PRIVATE);
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

    private Boolean download() {
        try {
            FTPClient ftp = new FTPClient();
            ftp.connect(HOSTNAME, 21);
            ftp.login(USERNAME, PASSWORD);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(2);
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(this.local_file)));
            ftp.retrieveFile(this.remote_file, outputStream);
            outputStream.close();
            ftp.disconnect();
            return Boolean.valueOf(true);
        } catch (Exception e) {
            return Boolean.valueOf(false);
        }
    }

    public void bulleToast (String message, LinearLayout element) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.bulle_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        TextView text = (TextView) layout.findViewById(R.id.message_aide);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.TOP|Gravity.LEFT, element.getLeft() + 75, element.getTop() + 14);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public void onLastAlertData () {

        if( sharedPref.getInt("triangle", 0) == 0 ) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // ici, on prend la valeur venant de shared et puis on le supprime
                // ceci ne sera executé que par started
                int A = sharedPref.getInt(DatabaseHelper.COLUMN_CEP_DEVICE_A + "_triangle", -1);
                int V = sharedPref.getInt(DatabaseHelper.COLUMN_CEP_DEVICE_V + "_triangle", -1);
                int F = sharedPref.getInt(DatabaseHelper.COLUMN_CEP_DEVICE_F + "_triangle", -1);
                int M = sharedPref.getInt(DatabaseHelper.COLUMN_CEP_DEVICE_M + "_triangle", -1);

                // on cache d'abord
                hideTriangle();
                /*
                Alert(
                        "A" + String.valueOf(sharedPref.getInt(DatabaseHelper.COLUMN_CEP_DEVICE_A, -1)) +
                                "V" + String.valueOf(sharedPref.getInt(DatabaseHelper.COLUMN_CEP_DEVICE_V, -1)) +
                                "F" + String.valueOf(sharedPref.getInt(DatabaseHelper.COLUMN_CEP_DEVICE_F, -1)) +
                                "M" + String.valueOf(sharedPref.getInt(DatabaseHelper.COLUMN_CEP_DEVICE_M, -1)),
                        Toast.LENGTH_LONG
                );
                */

                if( A > 2 ) {
                    acc_image.setImageResource(R.drawable.ic_left_arrow);
                    acc_image.setColorFilter(appColor.getColor(LEVEL_t.valueOf(A)));
                    acc_image.setVisibility(View.VISIBLE);
                }

                if( V > 2 ) {
                    corner_image.setImageResource(R.drawable.ic_left_arrow);
                    corner_image.setColorFilter(appColor.getColor(LEVEL_t.valueOf(V)));
                    if( is_corner_show )
                        corner_image.setVisibility(View.VISIBLE);
                }

                if( F > 2 ) {
                    brake_image.setImageResource(R.drawable.ic_left_arrow);
                    brake_image.setColorFilter(appColor.getColor(LEVEL_t.valueOf(F)));
                    brake_image.setVisibility(View.VISIBLE);
                }

                // on les efface après
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove(DatabaseHelper.COLUMN_CEP_DEVICE_A + "_triangle");
                editor.remove(DatabaseHelper.COLUMN_CEP_DEVICE_V + "_triangle");
                editor.remove(DatabaseHelper.COLUMN_CEP_DEVICE_F + "_triangle");
                editor.remove(DatabaseHelper.COLUMN_CEP_DEVICE_M + "_triangle");
                editor.apply();
            }
        });
    }

    // corner triangle
    public void triangleCorner (boolean show) {
        if( show && is_corner_show )
            corner_image.setVisibility(View.VISIBLE);
        else
            corner_image.setVisibility(View.INVISIBLE);
    }

    // hide all triangle image
    public void hideTriangle () {
        acc_image.setVisibility(View.INVISIBLE);
        corner_image.setVisibility(View.INVISIBLE);
        brake_image.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDownloaded(File file) {
        app.InstallApplication(file);
    }
}