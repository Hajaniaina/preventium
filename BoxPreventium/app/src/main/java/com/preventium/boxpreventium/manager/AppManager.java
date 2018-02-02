package com.preventium.boxpreventium.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.database.Database;
import com.preventium.boxpreventium.enums.ENGINE_t;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.MOVING_t;
import com.preventium.boxpreventium.enums.SCORE_t;
import com.preventium.boxpreventium.enums.SPEED_t;
import com.preventium.boxpreventium.enums.STATUS_t;
import com.preventium.boxpreventium.location.CustomMarkerData;
import com.preventium.boxpreventium.module.HandlerBox;
import com.preventium.boxpreventium.module.HandlerBox.NotifyListener;
import com.preventium.boxpreventium.server.CFG.DataCFG;
import com.preventium.boxpreventium.server.CFG.ReaderCFGFile;
import com.preventium.boxpreventium.server.DOBJ.DataDOBJ;
import com.preventium.boxpreventium.server.DOBJ.ReaderDOBJFile;
import com.preventium.boxpreventium.server.ECA.ECALine;
import com.preventium.boxpreventium.server.EPC.DataEPC;
import com.preventium.boxpreventium.server.EPC.ForceSeuil;
import com.preventium.boxpreventium.server.EPC.ReaderEPCFile;
import com.preventium.boxpreventium.server.POSS.ReaderPOSSFile;
import com.preventium.boxpreventium.utils.Chrono;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.Connectivity;
import com.preventium.boxpreventium.utils.ThreadDefault;
import com.preventium.boxpreventium.utils.Zipper;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPClientIO;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPConfig;

import org.apache.commons.net.nntp.NNTPReply;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AppManager extends ThreadDefault implements NotifyListener {
    private static final boolean DEBUG = true;

    private static final String HOSTNAME = "www.preventium.fr";
    private static final float MS_TO_KMH = 3.6f;
    private static final String PASSWORD = "Box*16/64/prev";
    private static final int PORTNUM = 21;
    private static final int SECS_TO_SET_PARCOURS_PAUSE = 20;
    private static final int SECS_TO_SET_PARCOURS_RESUME = 3;
    private static final int SECS_TO_SET_PARCOURS_START = 3;
    private static final int SECS_TO_SET_PARCOURS_STOPPED = 25200;
    private static final String TAG = "AppManager";
    private static final String USERNAME = "box.preventium";

    private static final FTPConfig FTP_ACTIF = new FTPConfig(HOSTNAME, USERNAME, PASSWORD, 21, "/ACTIFS");
    private static final FTPConfig FTP_CFG = new FTPConfig(HOSTNAME, USERNAME, PASSWORD, 21);
    private static final FTPConfig FTP_DOBJ = new FTPConfig(HOSTNAME, USERNAME, PASSWORD, 21);
    private static final FTPConfig FTP_EPC = new FTPConfig(HOSTNAME, USERNAME, PASSWORD, 21);
    private static final FTPConfig FTP_POS = new FTPConfig(HOSTNAME, USERNAME, PASSWORD, 21, "/POSS");
    private final static FTPConfig FTP_NAME = new FTPConfig(HOSTNAME,USERNAME,PASSWORD,PORTNUM);

    private static long duration = 0;
    private static int nb_box = 0;
    private static float speed_H = 0.0f;
    private static float speed_V = 0.0f;
    //private long Tavg = System.currentTimeMillis();
    //private float Vavg = 0.0f;
    private long[] f6X = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    //private double XmG = 0.0d;
    private long[] f7Y = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private boolean _tracking = DEBUG;
    //private long alertPos_add_at = 0;
    //private long alertUI_add_at = 0;
    //private long alertX_add_at = 0;
    //private long alertX_add_id = -1;
    //private long alertY_add_at = 0;
    //private long alertY_add_id = -1;
    private boolean button_stop = false;
    //private String chronoRideTxt = "";
    private Chrono chrono_ready_to_start = Chrono.newInstance();
    private Context ctx = null;
    private ArrayList<CustomMarkerData> customMarkerList = null;
    private boolean customMarkerList_Received = false;
    private Database database = null;
    private long driver_id = 0;
    private ENGINE_t engine_t = ENGINE_t.UNKNOW;
    private long engine_t_changed_at = 0;
    private FilesSender fileSender = null;
    private boolean gps = false;
    //private boolean internet_active = DEBUG;
    //private Location lastLocSend = null;
    private AppManagerListener listener = null;
    private List<Location> locations = new ArrayList();
    private final Lock lock = new ReentrantLock();
    private final Lock lock_timers = new ReentrantLock();
    private String log = "";
    private HandlerBox modules = null;
    //private Chrono mov_chrono = new Chrono();
    //private MOVING_t mov_t = MOVING_t.STP;
    //private MOVING_t mov_t_last = MOVING_t.UNKNOW;
    //private Chrono mov_t_last_chrono = new Chrono();
    private long note_forces_update_at = 0;
    private long note_parcour_update_at = 0;
    private long parcour_id = 0;
    private String parcoursTypeName = null;

    private long recommended_speed_update_at = 0;

    //private Chrono seuil_chrono_x = new Chrono();
    //private Chrono seuil_chrono_y = new Chrono();
    //private ForceSeuil seuil_last_x = null;
    //private ForceSeuil seuil_last_y = null;
    //private ForceSeuil seuil_ui = null;
    //Pair<Double, Short> shock = Pair.create(Double.valueOf(0.0d), Short.valueOf((short) 0));
    //Pair<Double, Short> smooth = Pair.create(Double.valueOf(0.0d), Short.valueOf((short) 0));
    private float speed_max = 0.0f;
    private long try_send_eca_at = 0;
    private List<Pair<Long, Integer>> ui_timers = new ArrayList();

    private final static FTPConfig FTP_FORM = new FTPConfig(HOSTNAME,USERNAME,PASSWORD,PORTNUM, "/FORM");

    public interface AppManagerListener {
        void onCalibrateOnAcceleration();

        void onCalibrateOnConstantSpeed();

        void onCalibrateRAZ();

        void onCustomMarkerDataListGet();

        void onDebugLog(String str);

        void onDrivingTimeChanged(String str);

        void onForceChanged(FORCE_t fORCE_t, LEVEL_t lEVEL_t, double d, float f, float f2);

        void onForceDisplayed(double d);

        void onInternetConnectionChanged();

        void onLevelNotified(LEVEL_t lEVEL_t);

        void onNoteChanged(int i, LEVEL_t lEVEL_t, LEVEL_t lEVEL_t2);

        void onNumberOfBoxChanged(int i);

        void onRecommendedSpeedChanged(SPEED_t sPEED_t, int i, LEVEL_t lEVEL_t, boolean z);

        void onScoreChanged(SCORE_t sCORE_t, LEVEL_t lEVEL_t);

        void onShock(double d, short s);

        void onSpeedCorner();

        void onSpeedCornerKept(int i, LEVEL_t lEVEL_t);

        void onSpeedLine();

        void onSpeedLineKept(int i, LEVEL_t lEVEL_t);

        void onStatusChanged(STATUS_t sTATUS_t);

        void onUiTimeout(int i, STATUS_t sTATUS_t);
    }

    class C01051 implements Runnable {
        C01051() {
        }

        public void run() {
            AppManager.this.run();
        }
    }

    class C01062 implements FilenameFilter {
        C01062() {
        }

        public boolean accept(File dir, String name) {
            return name.toUpperCase().endsWith(".CEP");
        }
    }

    class C01073 implements FilenameFilter {
        C01073() {
        }

        public boolean accept(File dir, String name) {
            return name.toUpperCase().endsWith(".SHARE");
        }
    }

    class C01084 implements FilenameFilter {
        C01084() {
        }

        public boolean accept(File dir, String name) {
            return name.toUpperCase().endsWith(".PT");
        }
    }

    public AppManager(Context ctx, AppManagerListener listener) {
        super(null);
        this.ctx = ctx;
        this.listener = listener;
        this.modules = new HandlerBox(ctx, this);
        this.database = new Database(ctx);
        this.fileSender = new FilesSender(ctx);

    }

    private void switchON(boolean on) {
        if (!on) {
            setStop();
        } else if (!isRunning()) {
            new Thread(new C01051()).start();
        }
    }

    public void raz_calibration() {
        if (this.modules != null) {
            this.modules.on_raz_calibration();
        }
    }

    public void myRun() throws InterruptedException {
        super.myRun();
        setLog("");
        this.database.clear_obselete_data();
        IMEI_is_actif();
        download_cfg();
        download_epc();
        download_dobj();

        this.modules.setActive(DEBUG);

        STATUS_t status = first_init();

        upload_eca(DEBUG);

        while (isRunning()) {
            check_internet_is_active();
            update_tracking_status();
            this.modules.setActive(DEBUG);
            sleep(500);
            this.database.clear_obselete_data();
            upload_eca(false);
            update_driving_time();
            calc_movements();

            switch ( status ) {
                case GETTING_CFG:
                case GETTING_EPC:
                case GETTING_DOBJ:
                    break;
                case PAR_STOPPED:
                    status = on_stopped();
                    break;
                case PAR_STARTED:
                case PAR_RESUME:
                    status = on_moved(status);
                    break;
                case PAR_PAUSING:
                case PAR_PAUSING_WITH_STOP:
                    status = on_paused(status);
                    break;
            }
            listen_timers(status);
        }
        this.modules.setActive(false);
    }

    // HANDLER BOX

    @Override
    public void onScanState(boolean scanning) {
        Log.d(TAG, "Searching preventium box is enable: " + scanning);
    }

    public void onDeviceState(String device_mac, boolean connected) {
        if (connected) {
            addLog("Device " + device_mac + " connected.");
        } else {
            addLog("Device " + device_mac + " disconnected.");
        }
        Location location = get_last_location();
        int note = (int) calc_note(this.parcour_id);
        int vitesse_ld = (int) (speed_H * 3.6f);
        int vitesse_vr = (int) (speed_V * 3.6f);
        int nbBox = nb_box;
        long distance_covered = this.database.get_distance(this.parcour_id);
        long parcour_duration = (long) (((double) duration) * 0.001d);
        Database database = this.database;
        this.database.addCEP(location, device_mac, note, vitesse_ld, vitesse_vr, distance_covered, parcour_duration, Database.get_eca_counter(this.ctx, this.parcour_id), nbBox, connected);
    }

    public void onNumberOfBox(int nb) {
        nb_box = nb;
        Log.d(TAG, "Number of preventium device connected changed: " + nb);
        if (this.listener != null) {
            this.listener.onNumberOfBoxChanged(nb);
        }
    }

    public void onForceChanged(Pair<Double, Short> smooth, Pair<Double, Short> shock) {
        this.smooth = smooth;
        this.shock = shock;
    }

    public synchronized void onEngineStateChanged(ENGINE_t state) {
        this.engine_t = state;
        this.engine_t_changed_at = System.currentTimeMillis();
    }

    public void onCalibrateOnConstantSpeed() {
        if (this.listener != null) {
            this.listener.onCalibrateOnConstantSpeed();
        }
    }

    public void onCalibrateOnAcceleration() {
        if (this.listener != null) {
            this.listener.onCalibrateOnAcceleration();
        }
    }

    public void onCalibrateRAZ() {
        onForceChanged(Pair.create(Double.valueOf(0.0d), Short.valueOf((short) 0)), Pair.create(Double.valueOf(0.0d), Short.valueOf((short) 0)));
        if (this.listener != null) {
            this.listener.onCalibrateRAZ();
        }
    }

    // PRIVATE

    /// ============================================================================================
    /// UI
    /// ============================================================================================

    private String chronoRideTxt = "";

    private STATUS_t first_init() throws InterruptedException {
        this.button_stop = false;
        this.internet_active = DEBUG;
        this.mov_t_last = MOVING_t.UNKNOW;
        this.mov_t = MOVING_t.UNKNOW;
        onEngineStateChanged(ENGINE_t.OFF);
        this.chronoRideTxt = "0:00";
        if (this.listener != null) {

            this.listener.onDebugLog("");

            //####santo
            listener.onStatusChanged( STATUS_t.PAR_PAUSING );
            listener.onStatusChanged( STATUS_t.PAR_STOPPED );



            this.listener.onDrivingTimeChanged(this.chronoRideTxt);
            this.listener.onNoteChanged(20, LEVEL_t.LEVEL_1, LEVEL_t.LEVEL_1);
            this.listener.onScoreChanged(SCORE_t.ACCELERATING, LEVEL_t.LEVEL_1);
            this.listener.onScoreChanged(SCORE_t.BRAKING, LEVEL_t.LEVEL_1);
            this.listener.onScoreChanged(SCORE_t.CORNERING, LEVEL_t.LEVEL_1);
            this.listener.onScoreChanged(SCORE_t.AVERAGE, LEVEL_t.LEVEL_1);
        }
        this.alertX_add_at = 0;
        this.alertY_add_at = 0;
        this.alertPos_add_at = 0;
        this.lastLocSend = null;
        this.try_send_eca_at = 0;
        this.parcour_id = get_current_parcours_id(DEBUG);
        STATUS_t ret = STATUS_t.PAR_STOPPED;
        if (this.parcour_id > 0) {
            loading_epc(this.database.get_num_epc(this.parcour_id));
            update_parcour_note(DEBUG);
            update_force_note(DEBUG);
            update_recommended_speed(DEBUG);
            ret = STATUS_t.PAR_PAUSING;
            addLog("Phone launch: Resume parcours with status PAUSE.");
            addLog("Status change to PAUSE. (" + ComonUtils.currentDateTime() + ")");
            return ret;
        }
        loading_epc();
        addLog("Phone launch: No parcours.");
        addLog("Status change to STOP. (" + ComonUtils.currentDateTime() + ")");

        //####vao santo
        if (listener != null) listener.onStatusChanged(ret);

        return ret;
    }

    private void update_driving_time() {
        if (this.listener != null) {
            long id = get_current_parcours_id(false);
            long ms = id > 0 ? System.currentTimeMillis() - id : 0;
            duration = ms;
            String txt = String.format(Locale.getDefault(), "%02d:%02d", new Object[]{Integer.valueOf(0), Integer.valueOf(0)});
            if (ms > 0) {
                long h = ms / 3600000;
                long m = ms % 3600000 > 0 ? (ms % 3600000) / 60000 : 0;
                txt = String.format(Locale.getDefault(), "%02d:%02d", new Object[]{Long.valueOf(h), Long.valueOf(m)});
                StatsLastDriving.set_times(this.ctx, (long) (((double) ms) * 0.001d));
            }
            if (!this.chronoRideTxt.equals(txt)) {
                this.listener.onDrivingTimeChanged(txt);
                this.chronoRideTxt = txt;
            }
        }
    }

    private void clear_force_ui() {
        if (this.seuil_ui != null && this.seuil_chrono_x.getSeconds() > 3.0d && this.seuil_chrono_y.getSeconds() > 3.0d) {
            if (this.listener != null) {
                this.listener.onForceChanged(FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW, 0.0d, 0.0f, 0.0f);
            }
            this.seuil_ui = null;
        }
    }
    /// ============================================================================================
    /// Driver ID
    /// ===========================================================================================
    public long get_driver_id() {
        return this.driver_id;
    }



    public void set_driver_id(long driver_id) {
        this.driver_id = driver_id;
    }


    /// ============================================================================================
    /// UI Timers
    /// ============================================================================================

    public void add_ui_timer(long secs, int timer_id) {
        long timestamp = System.currentTimeMillis() + (1000 * secs);
        this.lock_timers.lock();
        this.ui_timers.add(Pair.create(Long.valueOf(timestamp), Integer.valueOf(timer_id)));
        this.lock_timers.unlock();
    }

    /// Remove all timers
    public void clear_ui_timer() {
        this.lock_timers.lock();
        this.ui_timers.clear();
        this.lock_timers.unlock();
    }

    /// Listening timeout
    private synchronized void listen_timers(STATUS_t status) {
        long timestamp = System.currentTimeMillis();
        this.lock_timers.lock();
        if (!this.ui_timers.isEmpty()) {
            for (int i = this.ui_timers.size() - 1; i >= 0; i--) {
                Pair<Long, Integer> timer = (Pair) this.ui_timers.get(i);
                long timeout_at = ((Long) timer.first).longValue();
                int timer_id = ((Integer) timer.second).intValue();
                if (timestamp >= timeout_at) {
                    if (this.listener != null) {
                        this.listener.onUiTimeout(timer_id, status);
                    }
                    this.ui_timers.remove(i);
                }
            }
        }
        this.lock_timers.unlock();
    }

    /// ============================================================================================
    /// INTERNET CONNECTION
    /// ============================================================================================

    private boolean internet_active = true;

    private void check_internet_is_active() {
        boolean active = Connectivity.isConnected(this.ctx);
        if (active != this.internet_active) {
            this.internet_active = active;
            if (this.listener != null) {
                this.listener.onInternetConnectionChanged();
            }
        }
    }

    /// ============================================================================================
    /// .CFG
    /// ============================================================================================

    // Downloading .cfg file if is needed
    private boolean download_cfg() throws InterruptedException {
        boolean cfg = false;

        if( listener != null )listener.onStatusChanged( STATUS_t.GETTING_CFG );

        File folder = new File(ctx.getFilesDir(), "");
        ReaderCFGFile reader = new ReaderCFGFile();

        FTPClientIO ftp = new FTPClientIO();

        while ( isRunning() && !cfg  ){
            if( listener != null )listener.onStatusChanged( STATUS_t.GETTING_CFG );

            // Trying to connect to FTP server...
            if( !ftp.ftpConnect(FTP_CFG, 5000) ) {
                check_internet_is_active();
            } else {
                // Checking if .CFG file is in FTP server ?
                String srcFileName = ComonUtils.getIMEInumber(ctx) + ".CFG";
                String srcAckName = ComonUtils.getIMEInumber(ctx) + "_ok.CFG";
                boolean exist_server_cfg = ftp.checkFileExists( srcFileName );
                boolean exist_server_ack = ftp.checkFileExists( srcAckName );

                // If .CFG file exist in the FTP server
                cfg = ( exist_server_ack && reader.loadFromApp(ctx) );
                if( !cfg ) {
                    if (exist_server_cfg) {
                        // Create folder if not exist
                        if (!folder.exists())
                            if (!folder.mkdirs())
                                Log.w(TAG, "Error while trying to create new folder!");
                        if (folder.exists()) {
                            // Trying to download .CFG file...
                            String desFileName = String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), srcFileName);
                            if (ftp.ftpDownload(srcFileName, desFileName)) {
                                cfg = reader.read(desFileName);  // santooo
                                Log.e("FTP cfg : ", String.valueOf(cfg));
                                if (cfg) {
                                    String serv = reader.getServerUrl();
                                    Log.e("FTP azo : ", serv);
                                    reader.applyToApp(ctx);
                                    // envoi acknowledge
                                    try {
                                        File temp = File.createTempFile("temp-file-name", ".tmp");
                                        String ackFileName = ComonUtils.getIMEInumber(ctx) + "_ok.CFG";
                                        ftp.ftpUpload(temp.getPath(), ackFileName);
                                        temp.delete();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    // new File(desFileName).delete();  //delete source cfg santoni
                                }
                            }
                        }
                    } else {
                        cfg = reader.loadFromApp(ctx);
                    }
                }
                // Disconnect from FTP server.
                ftp.ftpDisconnect();
            }
            if( isRunning() && !cfg ) sleep(1000);
        }
        return cfg;
    }

    /// ============================================================================================
    /// .CHECK IF ACTIF
    /// ============================================================================================

    private boolean IMEI_is_actif() {
        boolean exist_actif = false;
        if (this.listener != null) {
            this.listener.onStatusChanged(STATUS_t.CHECK_ACTIF);
        }
        FTPClientIO ftp = new FTPClientIO();
        do {
            if (ftp.ftpConnect(FTP_ACTIF, 5000)) {
                exist_actif = false;
                if (ftp.changeWorkingDirectory(FTP_ACTIF.getWorkDirectory())) {
                    exist_actif = ftp.checkFileExists(ComonUtils.getIMEInumber(this.ctx));
                    if (exist_actif) {
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (this.listener != null) {
                            this.listener.onStatusChanged(STATUS_t.IMEI_INACTIF);
                        }
                    }
                } else {
                    Log.d(TAG, "ACTIFS: Error while trying to change working directory to \"" + FTP_ACTIF.getWorkDirectory() + "\"");
                }
            } else {
                check_internet_is_active();
            }
            if (exist_actif) {
                break;
            }
        } while (isRunning());
        return exist_actif;
    }

    /// ============================================================================================
    /// .EPC
    /// ============================================================================================

    private int selected_epc = 1;
    private ReaderEPCFile readerEPCFile = new ReaderEPCFile();

    // Downloading .EPC files if is needed
    private boolean download_epc() throws InterruptedException {
        boolean ready = false;

        if( listener != null ) listener.onStatusChanged( STATUS_t.GETTING_EPC );

        File folder = new File(ctx.getFilesDir(), "");
        ReaderEPCFile reader = new ReaderEPCFile();
        FTPClientIO ftp = new FTPClientIO();

        while( isRunning() && !ready ) {
            if( listener != null ) listener.onStatusChanged( STATUS_t.GETTING_EPC );

            // Trying to connect to FTP server...
            if( !ftp.ftpConnect(FTP_EPC, 5000) ) {
                check_internet_is_active();
            } else {

                // Changing working directory if needed
                boolean change_directory = true;
                if ( FTP_EPC.getWorkDirectory() != null && !FTP_EPC.getWorkDirectory().isEmpty() && !FTP_EPC.getWorkDirectory().equals("/"))
                    change_directory = ftp.changeWorkingDirectory(FTP_EPC.getWorkDirectory());

                if( !change_directory ) {
                    Log.w(TAG, "EPC: Error while trying to change working directory to \"" + FTP_EPC.getWorkDirectory() + "\"");
                } else {
                    boolean epc;
                    boolean epc_name;
                    boolean exist_server_epc = false;
                    boolean exist_server_ack = false;
                    boolean exist_server_name = false;
                    String srcFileName = "";
                    String srcAckName = "";
                    String srcNameName = "";
                    String desFileName = "";
                    int i = 1;
                    while( i <= 5 && isRunning() ) {

                        // Checking if .EPC file is in FTP server ?
                        srcFileName = reader.getEPCFileName(ctx, i, false);
                        srcAckName = reader.getEPCFileName(ctx, i, true);
                        srcNameName = reader.getNameFileName(ctx);
                        exist_server_epc = ftp.checkFileExists( srcFileName );
                        exist_server_ack = ftp.checkFileExists( srcAckName );
                        exist_server_name = ftp.checkFileExists( srcNameName );

                        // If .EPC file exist in the FTP server
                        epc = ( exist_server_ack && reader.loadFromApp(ctx,i) );
                        if( !epc ) {
                            if (exist_server_epc) {
                                // Create folder if not exist
                                if (!folder.exists())
                                    if (!folder.mkdirs())
                                        Log.w(TAG, "Error while trying to create new folder!");
                                if (folder.exists()) {
                                    // Trying to download .EPC file...
                                    desFileName = String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), srcFileName);
                                    if (ftp.ftpDownload(srcFileName, desFileName)) {
                                        epc = reader.read(desFileName);
                                        if( epc ) {
                                            reader.applyToApp(ctx,i);
                                            // envoi acknowledge
                                            try {
                                                File temp = File.createTempFile("temp-file-name", ".tmp");
                                                ftp.ftpUpload(temp.getPath(), srcAckName);
                                                temp.delete();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            new File(desFileName).delete();
                                        }
                                    }
                                    //get the EPC.NAME info in server
                                    if (exist_server_name) {
                                        if (!folder.exists())
                                            if (!folder.mkdirs())
                                                Log.w(TAG, "Error while trying to create new folder!");
                                        if (folder.exists()) {
                                            // Trying to download EPC.NAME file...
                                            desFileName = String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), srcNameName);
                                            if (ftp.ftpDownload(srcNameName, desFileName)) {
                                                epc_name = reader.readname(desFileName);
                                                if( epc_name ) {
                                                    reader.applyNameToApp(ctx);

                                                    new File(desFileName).delete();
                                                }
                                            }
                                        }

                                    }else{
                                        reader.loadNameFromApp(ctx);
                                    }

                                }
                            }
                        } else {
                            epc = reader.loadFromApp(ctx,i);
                        }

                        i++;
                    }



                }
                // Disconnect from FTP server.
                ftp.ftpDisconnect();
            }

            ready = !DataEPC.getAppEpcExist(ctx).isEmpty();
            if( isRunning() && !ready ) sleep(1000);
        }

        return ready;
    }
    private boolean loading_epc() throws InterruptedException {
        boolean ready = false;
        while (isRunning() && !ready) {
            this.selected_epc = 0;
            ready = this.readerEPCFile.loadFromApp(this.ctx);
            if (!ready) {
                for (Integer i : DataEPC.getAppEpcExist(this.ctx)) {
                    ready = this.readerEPCFile.loadFromApp(this.ctx, i.intValue());
                    if (ready) {
                        this.selected_epc = i.intValue();
                        break;
                    }
                }
            }
            this.selected_epc = this.readerEPCFile.selectedEPC(this.ctx);
            if (!ready) {
                download_epc();
            }
        }
        return ready;
    }

    private boolean loading_epc(int num) throws InterruptedException {
        boolean ready = false;
        if (num < 1) {
            num = 1;
        }
        if (num > 5) {
            num = 5;
        }
        while (isRunning() && !ready) {
            ready = this.readerEPCFile.loadFromApp(this.ctx, num);
            if (!ready) {
                download_epc();
            }
        }
        if (!ready) {
            num = 0;
        }
        this.selected_epc = num;
        return ready;
    }

    /// ============================================================================================
    /// .DOBJ
    /// ============================================================================================

    // Downloading .DOBJ files if is needed
    private boolean download_dobj() throws InterruptedException {
        boolean ready = false;

        if( listener != null ) listener.onStatusChanged( STATUS_t.GETTING_DOBJ );

        File folder = new File(ctx.getFilesDir(), "");
        ReaderDOBJFile reader = new ReaderDOBJFile();
        FTPClientIO ftp = new FTPClientIO();

        while( isRunning() && !ready ) {
            if( listener != null ) listener.onStatusChanged( STATUS_t.GETTING_DOBJ );

            // Trying to connect to FTP server...
            if( !ftp.ftpConnect(FTP_DOBJ, 5000) ) {
                check_internet_is_active();
            } else {

                // Changing working directory if needed
                boolean change_directory = true;
                if (FTP_DOBJ.getWorkDirectory() != null && !FTP_DOBJ.getWorkDirectory().isEmpty() && !FTP_DOBJ.getWorkDirectory().equals("/"))
                    change_directory = ftp.changeWorkingDirectory(FTP_DOBJ.getWorkDirectory());

                if( !change_directory ) {
                    Log.w(TAG, "DOBJ: Error while trying to change working directory to \"" + FTP_DOBJ.getWorkDirectory() + "\"!");
                } else {

                    boolean dobj = false;

                    // Checking if .DOBJ file is in FTP server ?
                    String srcFileName = ReaderDOBJFile.getOBJFileName(ctx, false);
                    String srcAckName = ReaderDOBJFile.getOBJFileName(ctx, true);
                    boolean exist_server_dobj = ftp.checkFileExists( srcFileName );
                    boolean exist_server_ack = ftp.checkFileExists( srcAckName );

                    // If .DOBJ file exist in the FTP server
                    dobj = ( exist_server_ack && DataDOBJ.preferenceFileExist(ctx) );
                    if( !dobj ) {
                        if (exist_server_dobj) {
                            // Create folder if not exist
                            if (!folder.exists())
                                if (!folder.mkdirs())
                                    Log.w(TAG, "Error while trying to create new folder!");
                            if (folder.exists()) {
                                // Trying to download .OBJ file...
                                String desFileName = String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), srcFileName);
                                if (ftp.ftpDownload(srcFileName, desFileName)) {
                                    dobj = reader.read(ctx,desFileName,false);
                                    if( dobj ) {
                                        ready = reader.read(ctx,desFileName,true);
                                        // envoi acknowledge
                                        try {
                                            File temp = File.createTempFile("temp-file-name", ".tmp");
                                            ftp.ftpUpload(temp.getPath(), srcAckName);
                                            temp.delete();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        new File(desFileName).delete();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Disconnect from FTP server.
            ftp.ftpDisconnect();

            if( !ready ) ready = DataDOBJ.preferenceFileExist(ctx);
            if( isRunning() && !ready ) sleep(1000);
        }
        return ready;
    }

    /// ============================================================================================
    /// .ECA
    /// ============================================================================================


    private boolean upload_eca(boolean now) {
        if (!now && this.try_send_eca_at + 60000 >= System.currentTimeMillis()) {
            return false;
        }
        this.database.add_driver(this.parcour_id, this.driver_id);
        this.fileSender.startThread();
        this.try_send_eca_at = System.currentTimeMillis();
        return DEBUG;
    }


    /// ============================================================================================
    /// .CEP
    /// ============================================================================================

    /// Create .CEP file (Connections Events of Preventium's devices) and uploading to the server.

    private void upload_cep() {
        if (isRunning()) {
            if (this.listener != null) {
                this.listener.onStatusChanged(STATUS_t.SETTING_CEP);
            }
            if (this.parcour_id > 0) {
                this.database.create_cep_file(this.parcour_id);
                this.database.clear_cep_data();
            }
            // UPLOAD .CEP FILES
            File folder = new File(this.ctx.getFilesDir(), "CEP");
            if (folder.exists()) {
                File[] listOfFiles = folder.listFiles(new C01062());
                if (listOfFiles != null && listOfFiles.length > 0) {
                    FTPConfig config = DataCFG.getFptConfig(this.ctx);
                    FTPClientIO ftp = new FTPClientIO();
                    if (config != null && ftp.ftpConnect(config, 5000)) {
                        boolean change_directory = DEBUG;
                        if (!(config.getWorkDirectory() == null || config.getWorkDirectory().isEmpty() || config.getWorkDirectory().equals("/"))) {
                            change_directory = ftp.changeWorkingDirectory(config.getWorkDirectory());
                        }
                        if (change_directory) {
                            for (File file : listOfFiles) {
                                if (ftp.ftpUpload(file.getAbsolutePath(), file.getName())) {
                                    file.delete();
                                }
                            }
                        } else {
                            Log.w(TAG, "CEP: Error while trying to change working directory to \"" + config.getWorkDirectory() + "\"!");
                        }
                    }
                }
            }
            if (this.listener != null) {
                this.listener.onStatusChanged(STATUS_t.PAR_STOPPED);
            }
        }
    }


    /// ============================================================================================
    /// .POS (Map markers)
    /// ============================================================================================
// Set list of map markers
    public void setCustomMarkerDataList(ArrayList<CustomMarkerData> list) {
        this.customMarkerList = list;
        this.customMarkerList_Received = DEBUG;
    }


    // Create .SHARE file (Position of map markers) and uploading to the server.

    private void upload_shared_pos() throws InterruptedException {
        if (isRunning() && this.listener != null) {
            File file;
            this.customMarkerList_Received = false;
            this.customMarkerList = null;
            this.listener.onCustomMarkerDataListGet();
            Chrono chrono = Chrono.newInstance();
            chrono.start();
            while (isRunning() && chrono.getSeconds() < 10.0d && !this.customMarkerList_Received) {
                sleep(500);
            }
            this.listener.onStatusChanged(STATUS_t.SETTING_MARKERS);

            // CREATE FILE
            File folder = new File(this.ctx.getFilesDir(), "SHARE");
            if (this.customMarkerList_Received && this.parcour_id > 0 && this.customMarkerList != null && this.customMarkerList.size() > 0) {
                if (!(folder.exists() || folder.mkdirs())) {
                    Log.w(TAG, "Error while trying to create new folder!");
                }
                if (folder.exists()) {
                    file = new File(folder.getAbsolutePath(), String.format(Locale.getDefault(), "%s.SHARE", new Object[]{ComonUtils.getIMEInumber(this.ctx)}));
                    try {
                        if (file.createNewFile()) {
                            FileWriter fileWriter = new FileWriter(file);
                            String line = "";
                            Iterator it = this.customMarkerList.iterator();
                            while (it.hasNext()) {
                                CustomMarkerData mk = (CustomMarkerData) it.next();
                                Locale locale = Locale.getDefault();
                                String str = "%f;%f;%d;%d;%d;%s;%d;%d;%s;%s;%s;\n";
                                Object[] objArr = new Object[11];
                                objArr[0] = Double.valueOf(mk.position.longitude);
                                objArr[1] = Double.valueOf(mk.position.latitude);
                                objArr[2] = Integer.valueOf(mk.type);
                                objArr[3] = Integer.valueOf(mk.alert ? 1 : 0);
                                objArr[4] = Integer.valueOf(mk.alertRadius);
                                objArr[5] = mk.title;
                                objArr[6] = Integer.valueOf(mk.shared ? 1 : 0);
                                objArr[7] = Integer.valueOf(0);
                                String str2 = (mk.alertMsg == null || !mk.alertMsg.isEmpty()) ? "" : mk.alertMsg;
                                objArr[8] = str2;
                                objArr[9] = ComonUtils.getIMEInumber(this.ctx);
                                objArr[10] = "";
                                fileWriter.write(String.format(locale, str, objArr));
                            }
                            fileWriter.flush();
                            fileWriter.close();
                        } else {
                            Log.w(TAG, "FILE NOT CREATED:" + file.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // UPLOAD .SHARE FILES
            if (folder.exists()) {
                File[] listOfFiles = folder.listFiles(new C01073());
                if (listOfFiles != null && listOfFiles.length > 0) {
                    FTPClientIO ftp = new FTPClientIO();
                    if (ftp.ftpConnect(FTP_POS, 5000)) {
                        boolean change_directory = DEBUG;
                        if (!(FTP_POS.getWorkDirectory() == null || FTP_POS.getWorkDirectory().isEmpty() || FTP_POS.getWorkDirectory().equals("/"))) {
                            change_directory = ftp.changeWorkingDirectory(FTP_POS.getWorkDirectory());
                        }
                        if (change_directory) {
                            for (File file2 : listOfFiles) {
                                if (ftp.ftpUpload(file2.getAbsolutePath(), file2.getName())) {
                                    file2.delete();
                                }
                            }
                        } else {
                            Log.w(TAG, "SHARE: Error while trying to change working directory to \"" + FTP_POS.getWorkDirectory() + "\"!");
                        }
                    }
                }
            }
            this.listener.onStatusChanged(STATUS_t.PAR_STOPPED);
        }
    }

    // Dowloading shared positions
    private boolean download_shared_pos() throws InterruptedException {
        boolean ready = false;
        if (this.listener != null) {
            this.listener.onStatusChanged(STATUS_t.GETTING_MARKERS_SHARED);
        }
        File folder = new File(this.ctx.getFilesDir(), "");
        FTPClientIO ftp = new FTPClientIO();
        while (isRunning() && !ready) {
            if (this.listener != null) {
                this.listener.onStatusChanged(STATUS_t.GETTING_MARKERS_SHARED);
            }
            if (ftp.ftpConnect(FTP_POS, 5000)) {
                boolean change_directory = DEBUG;
                if (!(FTP_POS.getWorkDirectory() == null || FTP_POS.getWorkDirectory().isEmpty() || FTP_POS.getWorkDirectory().equals("/"))) {
                    change_directory = ftp.changeWorkingDirectory(FTP_POS.getWorkDirectory());
                }
                if (change_directory) {
                    String srcFileName = ReaderPOSSFile.getFileName(this.ctx, false);
                    String srcAckName = ReaderPOSSFile.getFileName(this.ctx, DEBUG);
                    boolean exist_server_poss = ftp.checkFileExists(srcFileName);
                    boolean exist_server_ack = ftp.checkFileExists(srcAckName);
                    boolean need_download = ((!exist_server_poss || exist_server_ack) && (!exist_server_poss || ReaderPOSSFile.existLocalFile(this.ctx))) ? false : DEBUG;
                    if (!(folder.exists() || folder.mkdirs())) {
                        Log.w(TAG, "Error while trying to create new folder!");
                    }
                    String desFileName = String.format(Locale.getDefault(), "%s/%s", new Object[]{this.ctx.getFilesDir(), srcFileName});
                    if (need_download) {
                        try {
                            File local_file = new File(desFileName);
                            if (local_file.delete()) {
                                System.out.println(local_file.getName() + " is deleted!");
                            } else {
                                System.out.println("Delete operation is failed.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (ftp.ftpDownload(srcFileName, desFileName)) {
                            need_download = false;
                            try {
                                File temp = File.createTempFile("temp-file-name", ".tmp");
                                ftp.ftpUpload(temp.getPath(), srcAckName);
                                temp.delete();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                    if (!need_download) {
                        if (!ReaderPOSSFile.existLocalFile(this.ctx) || (!exist_server_poss && !exist_server_ack)) {
                            ready = DEBUG;
                        } else if (ReaderPOSSFile.readFile(desFileName) != null) {
                            ready = DEBUG;
                        }
                    }
                } else {
                    Log.w(TAG, "POSS: Error while trying to change working directory to \"" + FTP_POS + "\"!");
                }
            } else {
                check_internet_is_active();
            }
            ftp.ftpDisconnect();
            if (isRunning() && !ready) {
                sleep(1000);
            }
        }
        return ready;
    }


    /// ============================================================================================
    /// PARCOURS TYPE
    /// ============================================================================================
// Set current parcours is a parcours type,
    // if parcoursName is null, do not set this parcours is a parcours type

    public void set_parcours_type(@Nullable String parcoursName) {
        if (parcoursName == null) {
            parcoursName = "";
        }
        this.parcoursTypeName = parcoursName;
    }

    // Create .PT file (Parcours type) and uploading to the server.
    private void upload_parcours_type() throws InterruptedException {
        if (this.listener != null) {
            File folder;
            File file;
            this.parcoursTypeName = null;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.ctx);
            String key = this.ctx.getResources().getString(R.string.parcours_type_enabled_key);
            if (sp.getBoolean(key, false)) {
                Editor editor = sp.edit();
                editor.putBoolean(key, false);
                editor.apply();
                this.parcoursTypeName = sp.getString(key, "");
            }
            this.listener.onStatusChanged(STATUS_t.SETTING_PARCOUR_TYPE);
            if (!(this.parcoursTypeName == null || this.parcour_id <= 0 || this.parcoursTypeName.isEmpty())) {
                folder = new File(this.ctx.getFilesDir(), "PT");
                if (!(folder.exists() || folder.mkdirs())) {
                    Log.w(TAG, "Error while trying to create new folder!");
                }
                if (folder.exists()) {
                    file = new File(folder.getAbsolutePath(), String.format(Locale.getDefault(), "%s_%d.PT", new Object[]{ComonUtils.getIMEInumber(this.ctx), Long.valueOf(this.parcour_id)}));
                    try {
                        if (file.createNewFile()) {
                            FileWriter fileWriter = new FileWriter(file);
                            fileWriter.write(String.format(Locale.getDefault(), "%s;%d;%s", new Object[]{ComonUtils.getIMEInumber(this.ctx), Long.valueOf(this.parcour_id), this.parcoursTypeName}));
                            fileWriter.flush();
                            fileWriter.close();
                        } else {
                            Log.w(TAG, "FILE NOT CREATED:" + file.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // UPLOAD .PT FILES
            folder = new File(this.ctx.getFilesDir(), "PT");
            if (folder.exists()) {
                File[] listOfFiles = folder.listFiles(new C01084());
                if (listOfFiles != null && listOfFiles.length > 0) {
                    FTPConfig config = DataCFG.getFptConfig(this.ctx);
                    FTPClientIO ftp = new FTPClientIO();
                    if (config != null && ftp.ftpConnect(config, 5000)) {
                        boolean change_directory = DEBUG;
                        if (!(config.getWorkDirectory() == null || config.getWorkDirectory().isEmpty() || config.getWorkDirectory().equals("/"))) {
                            change_directory = ftp.changeWorkingDirectory(config.getWorkDirectory());
                        }
                        if (change_directory) {
                            for (File file2 : listOfFiles) {
                                if (ftp.ftpUpload(file2.getAbsolutePath(), file2.getName())) {
                                    file2.delete();
                                }
                            }
                        } else {
                            Log.w(TAG, "PT: Error while trying to change working directory to \"" + config.getWorkDirectory() + "\"!");
                        }
                    }
                }
            }
            this.listener.onStatusChanged(STATUS_t.PAR_STOPPED);
        }
    }


    /// ============================================================================================
    /// TRACKING
    /// ============================================================================================

    private void update_tracking_status() {
        this._tracking = PreferenceManager.getDefaultSharedPreferences(this.ctx).getBoolean(this.ctx.getResources().getString(R.string.tracking_activated_key), DEBUG);
    }


    /// ============================================================================================
    /// CALCUL
    /// ============================================================================================

    private MOVING_t mov_t = MOVING_t.STP;
    private MOVING_t mov_t_last = MOVING_t.UNKNOW;
    private long alertX_add_at = 0;
    private long alertX_add_id = -1;
    private long alertY_add_at = 0;
    private long alertY_add_id = -1;
    private long alertPos_add_at = 0;
    private Location lastLocSend = null;
    private Chrono mov_chrono = new Chrono();
    private Chrono mov_t_last_chrono = new Chrono();
    private ForceSeuil seuil_ui = null;
    private long alertUI_add_at = 0;
    private Chrono seuil_chrono_x = new Chrono();
    private Chrono seuil_chrono_y = new Chrono();
    private ForceSeuil seuil_last_x = null;
    private ForceSeuil seuil_last_y = null;
    private double XmG = 0.0;
    Pair<Double,Short> smooth = Pair.create(0.0,(short)0);
    Pair<Double,Short> shock = Pair.create(0.0,(short)0);

    private float Vavg = 0f;
    private long Tavg = System.currentTimeMillis();

    private void calc_movements() {

        if(  System.currentTimeMillis() - Tavg < 1000 ) return;

        this.mov_t = MOVING_t.UNKNOW;
        this.XmG = 0f;
        boolean rightRoad = false;

        // Calculate Vavg and Acceleration (m/s)
        List<Location> list = get_location_list(3,5000);
        float acceleration = 0f;
        if( list != null && list.size() >= 3 ) {
            int i = list.size()-1;
            rightRoad = isRightRoad( list.get(i-2), list.get(i-1), list.get(i) );
            float Vavg_next = ( list.get(i-2).getSpeed() + list.get(i-1).getSpeed() + list.get(i).getSpeed() ) * (1f/3f);
            long Tavg_next = System.currentTimeMillis();

            // Pour calculer l'accélération longitudinale (accélération ou freinage) avec comme unité le mG :
            // il faut connaître : la vitesse (v(t)) à l'instant t et à l'instant précédent(v(t-1)) et le delta t entre ces deux mesures.
            // a = ( v(t) - v(t-1) )/(9.81*( t - (t-1) ) )
            XmG = SpeedToXmG(Vavg,Vavg_next,Tavg,Tavg_next);

            acceleration = Vavg_next - Vavg ;
            Vavg = Vavg_next;
            Tavg = Tavg_next;
        } else {
            acceleration = 0f;
            Vavg = 0f;
            Tavg = System.currentTimeMillis();
        }

        // Set moving status
        if (Vavg * MS_TO_KMH <= 3f) mov_t = MOVING_t.STP;
        else if ( Math.abs( 0f - (acceleration * MS_TO_KMH) ) < 2f ) mov_t = MOVING_t.CST;
        else if (acceleration > 0f ) mov_t = MOVING_t.ACC;
        else if (acceleration < 0f) mov_t = MOVING_t.BRK;
        else mov_t = MOVING_t.NCS;

        // Set move chrono and calibration if necessary
        if ( mov_t != mov_t_last )
        {
            mov_t_last_chrono.start();
            mov_chrono.start();
            mov_t_last = mov_t;
        }
        else
        {
            if ( mov_chrono.isStarted() ) {
                switch (mov_t_last) {
                    case ACC:
                        if (rightRoad) {
                            mov_chrono.stop();
                            modules.on_acceleration();
                        }
                        break;
                    case BRK:
                        if (rightRoad) {
                            mov_chrono.stop();
                            modules.on_constant_speed();
                        }
                        break;
                    default:
                        break;
                }
            }
        }


    }

    private double SpeedToXmG(@NonNull float v1, @NonNull float v2, @NonNull long t1, @NonNull long t2) {
        // Pour calculer l'accélération longitudinale (accélération ou freinage) avec comme unité le mG :
        // il faut connaître : la vitesse (v(t)) à l'instant t et à l'instant précédent(v(t-1)) et le delta t entre ces deux mesures.
        // a = ( v(t) - v(t-1) )/(9.81*( t - (t-1) ) )

        return (((double) (v2 - v1)) / (9.81d * (((double) (t2 - t1)) * 0.001d))) * 1000.0d;
    }

    private double LocationsToXmG(@NonNull Location l0, @NonNull Location l1) {
        // Pour calculer l'accélération longitudinale (accélération ou freinage) avec comme unité le mG :
        // il faut connaître : la vitesse (v(t)) à l'instant t et à l'instant précédent(v(t-1)) et le delta t entre ces deux mesures.
        // a = ( v(t) - v(t-1) )/(9.81*( t - (t-1) ) )

        return (((double) (l0.getSpeed() - l1.getSpeed())) / (9.81d * (((double) (l0.getTime() - l1.getTime())) * 0.001d))) * 1000.0d;
    }

    private boolean isRightRoad(Location location_1, Location location_2, Location location_3) {
        double Lat_rad_2 = (location_2.getLatitude() * 3.141592653589793d) / 180.0d;
        double Long_rad_1 = (location_1.getLongitude() * 3.141592653589793d) / 180.0d;
        double Long_rad_2 = (location_2.getLongitude() * 3.141592653589793d) / 180.0d;
        double Long_rad_3 = (location_3.getLongitude() * 3.141592653589793d) / 180.0d;
        double Delta_L_rad_1 = Lat_rad_2 - ((location_1.getLatitude() * 3.141592653589793d) / 180.0d);
        double Delta_L_rad_2 = ((location_3.getLatitude() * 3.141592653589793d) / 180.0d) - Lat_rad_2;
        double A_deg_1 = (360.0d * Math.atan2(Math.cos(Long_rad_2) * Math.sin(Delta_L_rad_1), (Math.cos(Long_rad_1) * Math.sin(Long_rad_2)) - ((Math.sin(Long_rad_1) * Math.cos(Long_rad_2)) * Math.cos(Delta_L_rad_1)))) / 3.141592653589793d;
        double A_deg_2 = (360.0d * Math.atan2(Math.cos(Long_rad_3) * Math.sin(Delta_L_rad_2), (Math.cos(Long_rad_2) * Math.sin(Long_rad_3)) - ((Math.sin(Long_rad_2) * Math.cos(Long_rad_3)) * Math.cos(Delta_L_rad_2)))) / 3.141592653589793d;
        return Math.max(A_deg_1, A_deg_2) - Math.min(A_deg_1, A_deg_2) < 3.0d ? DEBUG : false;
    }

// ==============================================================

    private long X[] = { 0,0,0,0,0,0,0,0,0,0 };
    private static void update_tab_X( long tab[], FORCE_t t, LEVEL_t l, long s ){
        int i1 = -1;
        int i2 = -1;

        switch ( t ) {
            case UNKNOW:
            case TURN_LEFT:
            case TURN_RIGHT:
                break;
            case ACCELERATION:
                switch ( l ) {
                    case LEVEL_UNKNOW: break;
                    case LEVEL_1: i1 = 0; i2 = 0; break;
                    case LEVEL_2: i1 = 0; i2 = 1; break;
                    case LEVEL_3: i1 = 0; i2 = 2; break;
                    case LEVEL_4: i1 = 0; i2 = 3; break;
                    case LEVEL_5: i1 = 0; i2 = 4; break;
                }
                break;
            case BRAKING:
                switch ( l ) {
                    case LEVEL_UNKNOW: break;
                    case LEVEL_1: i1 = 5; i2 = 5; break;
                    case LEVEL_2: i1 = 5; i2 = 6; break;
                    case LEVEL_3: i1 = 5; i2 = 7; break;
                    case LEVEL_4: i1 = 5; i2 = 8; break;
                    case LEVEL_5: i1 = 5; i2 = 9; break;
                }
                break;
        }
        for( int i = 0; i < 10; i++ ) {
            if( i >= i1 && i <= i2 ) {
                if( tab[i] <= 0 ) tab[i] = s;
            } else {
                tab[i] = 0;
            }
        }
    }
    private ForceSeuil read_tab_X( long tab[], long s ){
        ForceSeuil ret = null;
        // 0 to 4: LEVEL_1 to LEVEL_5 for A
        // 5 to 9: LEVEL_1 to LEVEL_5 for F
        int a = -1;
        long tps;
        for( int i = 0; i < 10; i++ ) {
            if( tab[i] > 0 ) {
                tps = s - tab[i];
                if( tps >= readerEPCFile.get_TPS_ms(i) ) {
                    a = i;
                }
            }
        }
        if( a >= 0 && a < 10 ) {
            ret = readerEPCFile.getForceSeuil(a);
        }
        return ret;
    }
    private long Y[] = { 0,0,0,0,0,0,0,0,0,0 };
    private static void update_tab_Y( long tab[], FORCE_t t, LEVEL_t l, long s ){
        int i1 = -1;
        int i2 = -1;

        switch ( t ) {
            case UNKNOW:
            case ACCELERATION:
            case BRAKING:
                break;
            case TURN_RIGHT:
                switch ( l ) {
                    case LEVEL_UNKNOW: break;
                    case LEVEL_1: i1 = 0; i2 = 0; break;
                    case LEVEL_2: i1 = 0; i2 = 1; break;
                    case LEVEL_3: i1 = 0; i2 = 2; break;
                    case LEVEL_4: i1 = 0; i2 = 3; break;
                    case LEVEL_5: i1 = 0; i2 = 4; break;
                }
                break;
            case TURN_LEFT:
                switch ( l ) {
                    case LEVEL_UNKNOW: break;
                    case LEVEL_1: i1 = 5; i2 = 5; break;
                    case LEVEL_2: i1 = 5; i2 = 6; break;
                    case LEVEL_3: i1 = 5; i2 = 7; break;
                    case LEVEL_4: i1 = 5; i2 = 8; break;
                    case LEVEL_5: i1 = 5; i2 = 9; break;
                }
                break;
        }
        for( int i = 0; i < 10; i++ ) {
            if( i >= i1 && i <= i2 ) {
                if( tab[i] <= 0 ) tab[i] = s;
            } else {
                tab[i] = 0;
            }
        }
    }
    private ForceSeuil read_tab_Y( long tab[], long s ){
        ForceSeuil ret = null;
        // 0 to 4: LEVEL_1 to LEVEL_5 for A
        // 5 to 9: LEVEL_1 to LEVEL_5 for F
        int a = -1;
        long tps;
        for( int i = 0; i < 10; i++ ) {
            if( tab[i] > 0 ) {
                tps = s - tab[i];
                if( tps >= readerEPCFile.get_TPS_ms(i+10) ) {
                    a = i;
                }
            }
        }
        if( a >= 0 && a < 10 ) {
            ret = readerEPCFile.getForceSeuil(a+10);
        }
        return ret;
    }

    private synchronized void calculate_eca() {
        long ST = System.currentTimeMillis();
        Location loc = get_last_location();
        ForceSeuil seuil_x = this.readerEPCFile.getForceSeuilForX(this.XmG);
        ForceSeuil seuil_y = this.readerEPCFile.getForceSeuilForY(((Double) this.smooth.first).doubleValue());
        if (loc == null || ((double) (loc.getSpeed() * 3.6f)) < 20.0d) {
            seuil_x = null;
            seuil_y = null;
        } else {
            FORCE_t type_X = FORCE_t.UNKNOW;
            FORCE_t type_Y = FORCE_t.UNKNOW;
            LEVEL_t level_X = LEVEL_t.LEVEL_UNKNOW;
            LEVEL_t level_Y = LEVEL_t.LEVEL_UNKNOW;
            if (seuil_x != null) {
                type_X = seuil_x.type;
                level_X = seuil_x.level;
            }
            if (seuil_y != null) {
                type_Y = seuil_y.type;
                level_Y = seuil_y.level;
            }
            update_tab_X(this.f6X, type_X, level_X, ST);
            update_tab_Y(this.f7Y, type_Y, level_Y, ST);
            seuil_x = read_tab_X(this.f6X, ST);
            seuil_y = read_tab_Y(this.f7Y, ST);
            if (seuil_x != null && this._tracking && (this.alertX_add_id != ((long) seuil_x.IDAlert) || ST > this.alertX_add_at + ((long) (seuil_x.TPS * 1000)))) {
                this.database.addECA(this.parcour_id, ECALine.newInstance(seuil_x.IDAlert, loc, null));
                this.alertX_add_at = ST;
                this.alertX_add_id = (long) seuil_x.IDAlert;
                this.lastLocSend = loc;
            }
            if (seuil_y != null && this._tracking && (this.alertY_add_id != ((long) seuil_y.IDAlert) || ST > this.alertY_add_at + ((long) (seuil_y.TPS * 1000)))) {
                this.database.addECA(this.parcour_id, ECALine.newInstance(seuil_y.IDAlert, loc, null));
                this.alertY_add_at = ST;
                this.alertY_add_id = (long) seuil_y.IDAlert;
                this.lastLocSend = loc;
            }
            if (this._tracking) {
                List<Location> locations = get_location_list(1);
                if (locations != null && locations.size() >= 1) {
                    float min_meters = ((Location) locations.get(0)).getSpeed() * 3.6f < 70.0f ? 5.0f : 15.0f;
                    if (this.lastLocSend == null || ((Location) locations.get(0)).distanceTo(this.lastLocSend) > min_meters) {
                        if (this.lastLocSend == null) {
                            this.lastLocSend = new Location((Location) locations.get(0));
                        }
                        this.database.addECA(this.parcour_id, ECALine.newInstance((Location) locations.get(0), this.lastLocSend));
                        this.lastLocSend = new Location((Location) locations.get(0));
                    }
                }
            }
        }
        if (this.listener != null) {
            ForceSeuil seuil;
            double force;
            if (seuil_x == null) {
                seuil = seuil_y;
                force = ((Double) this.smooth.first).doubleValue();
            } else if (seuil_y == null) {
                seuil = seuil_x;
                force = this.XmG;
            } else {
                if (seuil_x.level.getValue() >= seuil_y.level.getValue()) {
                    seuil = seuil_x;
                } else {
                    seuil = seuil_y;
                }
                force = seuil_x.level.getValue() >= seuil_y.level.getValue() ? this.XmG : ((Double) this.smooth.first).doubleValue();
            }
            int t_ms = this.seuil_ui == null ? 0 : this.seuil_ui.level.get_ui_time_ms();
            if (this.seuil_ui == null) {
                if (seuil == null) {
                    this.alertUI_add_at = ST;
                } else {
                    this.alertUI_add_at = ST;
                    this.seuil_ui = seuil;
                    this.listener.onForceChanged(seuil.type, seuil.level, force, speed_H * 3.6f, speed_V * 3.6f);
                    this.listener.onLevelNotified(seuil.level);
                    this.listener.onForceDisplayed(force);
                }
            } else if (seuil == null) {
                if (this.alertUI_add_at + ((long) t_ms) < ST) {
                    this.alertUI_add_at = ST;
                    this.seuil_ui = null;
                    this.listener.onForceChanged(FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW, 0.0d, 0.0f, 0.0f);
                }
            } else if (seuil.type.getAxe() == this.seuil_ui.type.getAxe() && seuil.level.getValue() > this.seuil_ui.level.getValue()) {
                this.alertUI_add_at = ST;
                this.seuil_ui = seuil;
                this.listener.onForceChanged(seuil.type, seuil.level, force, speed_H * 3.6f, speed_V * 3.6f);
                this.listener.onLevelNotified(seuil.level);
                this.listener.onForceDisplayed(force);
            } else if (this.alertUI_add_at + ((long) t_ms) < ST) {
                this.alertUI_add_at = ST;
                this.seuil_ui = seuil;
                this.listener.onForceChanged(seuil.type, seuil.level, force, speed_H * 3.6f, speed_V * 3.6f);
                this.listener.onLevelNotified(seuil.level);
                this.listener.onForceDisplayed(force);
            }
        }
    }

    /// ============================================================================================
    /// CALCUL COTATION
    /// ============================================================================================

    private float calc_note_by_force_type(String type, long parcour_id, long begin, long end) {
        if (!"A".equals(type) && !"F".equals(type) && !"V".equals(type)) {
            return 20.0f;
        }
        float ret;
        float coeff_general = DataDOBJ.get_coefficient_general(this.ctx, type);
        int[] coeff_force = new int[5];
        coeff_force[0] = DataDOBJ.get_coefficient(this.ctx, type, "V");
        coeff_force[1] = DataDOBJ.get_coefficient(this.ctx, type, "B");
        coeff_force[2] = DataDOBJ.get_coefficient(this.ctx, type, "J");
        coeff_force[3] = DataDOBJ.get_coefficient(this.ctx, type, "O");
        coeff_force[4] = DataDOBJ.get_coefficient(this.ctx, type, "R");
        int[] nb_evt = new int[5];
        if ("A".equals(type)) {
            nb_evt[0] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(0).IDAlert, parcour_id, begin, end);
            nb_evt[1] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(1).IDAlert, parcour_id, begin, end);
            nb_evt[2] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(2).IDAlert, parcour_id, begin, end);
            nb_evt[3] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(3).IDAlert, parcour_id, begin, end);
            nb_evt[4] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(4).IDAlert, parcour_id, begin, end);
        } else if ("F".equals(type)) {
            nb_evt[0] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(5).IDAlert, parcour_id, begin, end);
            nb_evt[1] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(6).IDAlert, parcour_id, begin, end);
            nb_evt[2] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(7).IDAlert, parcour_id, begin, end);
            nb_evt[3] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(8).IDAlert, parcour_id, begin, end);
            nb_evt[4] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(9).IDAlert, parcour_id, begin, end);
        } else {
            nb_evt[0] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(15).IDAlert, parcour_id, begin, end) + this.database.countNbEvent(this.readerEPCFile.getForceSeuil(10).IDAlert, parcour_id, begin, end);
            nb_evt[1] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(16).IDAlert, parcour_id, begin, end) + this.database.countNbEvent(this.readerEPCFile.getForceSeuil(11).IDAlert, parcour_id, begin, end);
            nb_evt[2] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(17).IDAlert, parcour_id, begin, end) + this.database.countNbEvent(this.readerEPCFile.getForceSeuil(12).IDAlert, parcour_id, begin, end);
            nb_evt[3] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(18).IDAlert, parcour_id, begin, end) + this.database.countNbEvent(this.readerEPCFile.getForceSeuil(13).IDAlert, parcour_id, begin, end);
            nb_evt[4] = this.database.countNbEvent(this.readerEPCFile.getForceSeuil(19).IDAlert, parcour_id, begin, end) + this.database.countNbEvent(this.readerEPCFile.getForceSeuil(14).IDAlert, parcour_id, begin, end);
        }
        Log.d("AAA", "+++++ CALCUL FOR " + type + "+++++");
        Log.d("AAA", "COEFFICIENTS");
        Log.d("AAA", "- General: " + coeff_general);
        Log.d("AAA", "- Vert: " + coeff_force[0]);
        Log.d("AAA", "- Bleu: " + coeff_force[1]);
        Log.d("AAA", "- Jaune: " + coeff_force[2]);
        Log.d("AAA", "- Orange: " + coeff_force[3]);
        Log.d("AAA", "- Rouge: " + coeff_force[4]);
        Log.d("AAA", "EVENEMENTS CONSTATEES");
        Log.d("AAA", "- Vert: " + nb_evt[0]);
        Log.d("AAA", "- Bleu: " + nb_evt[1]);
        Log.d("AAA", "- Jaune: " + nb_evt[2]);
        Log.d("AAA", "- Orange: " + nb_evt[3]);
        Log.d("AAA", "- Rouge: " + nb_evt[4]);
        Log.d("AAA", "ETAPES DE CALCULS");
        int evt_sum = (((nb_evt[0] + nb_evt[1]) + nb_evt[2]) + nb_evt[3]) + nb_evt[4];
        Log.d("AAA", "Somme des evenements: " + evt_sum);
        if (evt_sum <= 0) {
            ret = 20.0f;
        } else {
            int coeff_sum = (((coeff_force[0] + coeff_force[1]) + coeff_force[2]) + coeff_force[3]) + coeff_force[4];
            float coeff_percent = (float) (coeff_sum * 0.01);
            float[] interm_1 = new float[]{((float) (coeff_force[0] * coeff_sum)) / ((100.0f * coeff_percent) * coeff_general), ((float) (coeff_force[1] * coeff_sum)) / ((100.0f * coeff_percent) * coeff_general), ((float) (coeff_force[2] * coeff_sum)) / ((100.0f * coeff_percent) * coeff_general), ((float) (coeff_force[3] * coeff_sum)) / ((100.0f * coeff_percent) * coeff_general), ((float) (coeff_force[4] * coeff_sum)) / ((100.0f * ((float) (((double) coeff_sum) * 0.01d))) * coeff_general)};
            Log.d("AAA", "coeff_sum: " + coeff_sum);
            Log.d("AAA", "coeff_percent: " + coeff_percent);
            Log.d("AAA", "interm_1:");
            Log.d("AAA", "- Vert: " + interm_1[0]);
            Log.d("AAA", "- Bleu: " + interm_1[1]);
            Log.d("AAA", "- Jaune: " + interm_1[2]);
            Log.d("AAA", "- Orange: " + interm_1[3]);
            Log.d("AAA", "- Rouge: " + interm_1[4]);
            float coeff_evt = (float) (evt_sum * 0.01);
            float[] interm_2 = new float[]{((float) (nb_evt[0] * evt_sum)) / ((100.0f * coeff_evt) * coeff_evt), ((float) (nb_evt[1] * evt_sum)) / ((100.0f * coeff_evt) * coeff_evt), ((float) (nb_evt[2] * evt_sum)) / ((100.0f * coeff_evt) * coeff_evt), ((float) (nb_evt[3] * evt_sum)) / ((100.0f * coeff_evt) * coeff_evt), ((float) (nb_evt[4] * evt_sum)) / ((100.0f * ((float) (((double) evt_sum) * 0.01d))) * coeff_evt)};
            Log.d("AAA", "coeff_evt: " + ((float) (((double) evt_sum) * 0.01d)));
            Log.d("AAA", "interm_2:");
            Log.d("AAA", "- Vert: " + interm_2[0]);
            Log.d("AAA", "- Bleu: " + interm_2[1]);
            Log.d("AAA", "- Jaune: " + interm_2[2]);
            Log.d("AAA", "- Orange: " + interm_2[3]);
            Log.d("AAA", "- Rouge: " + interm_2[4]);
            float[] interm_3 = new float[]{interm_1[0] * interm_2[0], interm_1[1] * interm_2[1], interm_1[2] * interm_2[2], interm_1[3] * interm_2[3], interm_1[4] * interm_2[4]};
            Log.d("AAA", "interm_3:");
            Log.d("AAA", "- Vert: " + interm_3[0]);
            Log.d("AAA", "- Bleu: " + interm_3[1]);
            Log.d("AAA", "- Jaune: " + interm_3[2]);
            Log.d("AAA", "- Orange: " + interm_3[3]);
            Log.d("AAA", "- Rouge: " + interm_3[4]);
            float interm_3_sum = interm_3[0] + interm_3[1] + interm_3[2] + interm_3[3] + interm_3[4];
            float[] interm_4 = new float[]{interm_3[0] / (0.01f * interm_3_sum), interm_3[1] / (0.01f * interm_3_sum), interm_3[2] / (0.01f * interm_3_sum), interm_3[3] / (0.01f * interm_3_sum), interm_3[4] / (0.01f * ((((interm_3[0] + interm_3[1]) + interm_3[2]) + interm_3[3]) + interm_3[4]))};
            Log.d("AAA", "interm_3_sum: " + ((((interm_3[0] + interm_3[1]) + interm_3[2]) + interm_3[3]) + interm_3[4]));
            Log.d("AAA", "interm_4:");
            Log.d("AAA", "- Vert: " + interm_4[0]);
            Log.d("AAA", "- Bleu: " + interm_4[1]);
            Log.d("AAA", "- Jaune: " + interm_4[2]);
            Log.d("AAA", "- Orange: " + interm_4[3]);
            Log.d("AAA", "- Rouge: " + interm_4[4]);
            ret = ((((interm_4[0] + interm_4[1]) - interm_4[2]) - interm_4[3]) - interm_4[4]) * 0.2f;
            Log.d("AAA", "RET = " + ret);
            if (ret < 0.0f) {
                ret = 0.0f;
            }
            if (ret > 20.0f) {
                ret = 20.0f;
            }
            if (StatsLastDriving.get_start_at(this.ctx) == parcour_id) {
                if ("A".equals(type)) {
                    StatsLastDriving.set_resultat_A(this.ctx, LEVEL_t.LEVEL_1, interm_4[0]);
                    StatsLastDriving.set_resultat_A(this.ctx, LEVEL_t.LEVEL_2, interm_4[1]);
                    StatsLastDriving.set_resultat_A(this.ctx, LEVEL_t.LEVEL_3, interm_4[2]);
                    StatsLastDriving.set_resultat_A(this.ctx, LEVEL_t.LEVEL_4, interm_4[3]);
                    StatsLastDriving.set_resultat_A(this.ctx, LEVEL_t.LEVEL_5, interm_4[4]);
                } else if ("F".equals(type)) {
                    StatsLastDriving.set_resultat_F(this.ctx, LEVEL_t.LEVEL_1, interm_4[0]);
                    StatsLastDriving.set_resultat_F(this.ctx, LEVEL_t.LEVEL_2, interm_4[1]);
                    StatsLastDriving.set_resultat_F(this.ctx, LEVEL_t.LEVEL_3, interm_4[2]);
                    StatsLastDriving.set_resultat_F(this.ctx, LEVEL_t.LEVEL_4, interm_4[3]);
                    StatsLastDriving.set_resultat_F(this.ctx, LEVEL_t.LEVEL_5, interm_4[4]);
                } else if ("V".equals(type)) {
                    StatsLastDriving.set_resultat_V(this.ctx, LEVEL_t.LEVEL_1, interm_4[0]);
                    StatsLastDriving.set_resultat_V(this.ctx, LEVEL_t.LEVEL_2, interm_4[1]);
                    StatsLastDriving.set_resultat_V(this.ctx, LEVEL_t.LEVEL_3, interm_4[2]);
                    StatsLastDriving.set_resultat_V(this.ctx, LEVEL_t.LEVEL_4, interm_4[3]);
                    StatsLastDriving.set_resultat_V(this.ctx, LEVEL_t.LEVEL_5, interm_4[4]);
                }
            }
        }
        Log.d("AAA", "----- CALCUL FOR " + type + "-----");
        return ret;
    }

    private float calc_note_by_force_type(String type, long parcour_id) {
        return calc_note_by_force_type(type, parcour_id, 0, System.currentTimeMillis() + 3600);
    }

    private float calc_note(long parcour_id, long begin, long end) {
        float Note_A = calc_note_by_force_type("A", parcour_id, begin, end);
        float Note_F = calc_note_by_force_type("F", parcour_id, begin, end);
        float Note_V = calc_note_by_force_type("V", parcour_id, begin, end);
        float Coeff_General_A = DataDOBJ.get_coefficient_general(this.ctx, "A");
        float Coeff_General_F = DataDOBJ.get_coefficient_general(this.ctx, "F");
        float Coeff_General_V = DataDOBJ.get_coefficient_general(this.ctx, "V");
        return (((Note_A * Coeff_General_A) + (Note_F * Coeff_General_F)) + (Note_V * Coeff_General_V)) / ((Coeff_General_A + Coeff_General_F) + Coeff_General_V);
    }

    private float calc_note(long parcour_id) {
        return calc_note(parcour_id, 0, System.currentTimeMillis() + 3600);
    }

    private void update_parcour_note(boolean force) {
        if (force || this.note_parcour_update_at + 180000 < System.currentTimeMillis()) {
            this.note_parcour_update_at = System.currentTimeMillis();
            float parcour_note = calc_note(this.parcour_id);
            LEVEL_t parcour_level = LEVEL_t.LEVEL_5;
            if (parcour_note >= 16.0f) {
                parcour_level = LEVEL_t.LEVEL_1;
            } else if (parcour_note >= 13.0f) {
                parcour_level = LEVEL_t.LEVEL_2;
            } else if (parcour_note >= 9.0f) {
                parcour_level = LEVEL_t.LEVEL_3;
            } else if (parcour_note >= 6.0f) {
                parcour_level = LEVEL_t.LEVEL_4;
            }
            long end = startOfDays(System.currentTimeMillis());
            float last_5_days_note = calc_note(-1, end - 432000000, end);
            LEVEL_t last_5_days_level = LEVEL_t.LEVEL_5;
            if (last_5_days_note >= 16.0f) {
                last_5_days_level = LEVEL_t.LEVEL_1;
            } else if (last_5_days_note >= 13.0f) {
                last_5_days_level = LEVEL_t.LEVEL_2;
            } else if (last_5_days_note >= 9.0f) {
                last_5_days_level = LEVEL_t.LEVEL_3;
            } else if (last_5_days_note >= 6.0f) {
                last_5_days_level = LEVEL_t.LEVEL_4;
            }
            StatsLastDriving.set_note(this.ctx, SCORE_t.FINAL, parcour_note);
            Log.d(TAG, "Parcours " + this.parcour_id + " note: " + parcour_note);
            if (this.listener != null) {
                this.listener.onNoteChanged((int) parcour_note, parcour_level, last_5_days_level);
                this.listener.onLevelNotified(parcour_level);
            }
        }
    }

    private void update_force_note(boolean force) {
        if (force || this.note_forces_update_at + 60000 < System.currentTimeMillis()) {
            this.note_forces_update_at = System.currentTimeMillis();
            float Note_A = calc_note_by_force_type("A", this.parcour_id);
            LEVEL_t level_A = note2level(Note_A);
            float Note_F = calc_note_by_force_type("F", this.parcour_id);
            LEVEL_t level_F = note2level(Note_F);
            float Note_V = calc_note_by_force_type("V", this.parcour_id);
            LEVEL_t level_V = note2level(Note_V);
            float Note_M = ((Note_A + Note_F) + Note_V) * 0.33333334f;
            LEVEL_t level_M = note2level(Note_M);
            StatsLastDriving.set_note(this.ctx, SCORE_t.ACCELERATING, Note_A);
            StatsLastDriving.set_note(this.ctx, SCORE_t.BRAKING, Note_F);
            StatsLastDriving.set_note(this.ctx, SCORE_t.CORNERING, Note_V);
            Log.d(TAG, "Parcours " + this.parcour_id + " note A: " + Note_A);
            Log.d(TAG, "Parcours " + this.parcour_id + " note F: " + Note_F);
            Log.d(TAG, "Parcours " + this.parcour_id + " note V: " + Note_V);
            Log.d(TAG, "Parcours " + this.parcour_id + " note M: " + Note_M);
            StatsLastDriving.set_speed_avg(this.ctx, this.database.speed_avg(this.parcour_id, System.currentTimeMillis(), 0.0f, new int[0]));
            if (this.listener != null) {
                this.listener.onScoreChanged(SCORE_t.ACCELERATING, level_A);
                this.listener.onScoreChanged(SCORE_t.BRAKING, level_F);
                this.listener.onScoreChanged(SCORE_t.CORNERING, level_V);
                this.listener.onScoreChanged(SCORE_t.AVERAGE, level_M);
            }
        }
    }

    private LEVEL_t note2level(float note) {
        LEVEL_t level = LEVEL_t.LEVEL_5;
        if (note >= 16.0f) {
            return LEVEL_t.LEVEL_1;
        }
        if (note >= 13.0f) {
            return LEVEL_t.LEVEL_2;
        }
        if (note >= 9.0f) {
            return LEVEL_t.LEVEL_3;
        }
        if (note >= 6.0f) {
            return LEVEL_t.LEVEL_4;
        }
        return level;
    }

    private long startOfDays(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        cal.set(Calendar.MINUTE, 0); // set minutes to zero
        cal.set(Calendar.SECOND, 0); //set seconds to zero
        return cal.getTimeInMillis();
    }

/// ============================================================================================
    /// CALCUL RECOMMENDED SPEED
    /// ============================================================================================

    private void update_recommended_speed(boolean force) {
        if ((force || this.recommended_speed_update_at + 240000 < System.currentTimeMillis()) && this.readerEPCFile != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.ctx);
            long delay_sec = (long) (sp.getInt(this.ctx.getResources().getString(R.string.recommended_speed_time_key), 30) * 60);
            long max_delay_sec = (long) (sp.getInt(this.ctx.getResources().getString(R.string.stop_trigger_time_key), NNTPReply.NO_CURRENT_ARTICLE_SELECTED) * 60);
            speed_H = this.database.speed_max_test(this.parcour_id, delay_sec, 50, max_delay_sec, this.readerEPCFile.getForceSeuil(0).IDAlert, this.readerEPCFile.getForceSeuil(1).IDAlert, this.readerEPCFile.getForceSeuil(5).IDAlert, this.readerEPCFile.getForceSeuil(6).IDAlert);
            speed_V = this.database.speed_max_test(this.parcour_id, delay_sec, 50, max_delay_sec, this.readerEPCFile.getForceSeuil(10).IDAlert, this.readerEPCFile.getForceSeuil(11).IDAlert, this.readerEPCFile.getForceSeuil(15).IDAlert, this.readerEPCFile.getForceSeuil(16).IDAlert);
            this.speed_max = this.database.speed_max_test(this.parcour_id, delay_sec, 50, max_delay_sec, this.readerEPCFile.get_all_alertID());
            this.recommended_speed_update_at = System.currentTimeMillis();
        }
        if (this.listener != null) {
            LEVEL_t level_H = LEVEL_t.LEVEL_UNKNOW;
            if (speed_H > 0.0f) {
                if (this.speed_max < speed_H) {
                    level_H = LEVEL_t.LEVEL_1;
                } else if (((double) this.speed_max) < ((double) speed_H) * 1.1d) {
                    level_H = LEVEL_t.LEVEL_2;
                } else if (((double) this.speed_max) < ((double) speed_H) * 1.2d) {
                    level_H = LEVEL_t.LEVEL_3;
                } else if (((double) this.speed_max) < ((double) speed_H) * 1.35d) {
                    level_H = LEVEL_t.LEVEL_4;
                } else {
                    level_H = LEVEL_t.LEVEL_5;
                }
            }
            LEVEL_t level_V = LEVEL_t.LEVEL_UNKNOW;
            if (speed_V > 0.0f) {
                if (this.speed_max < speed_V) {
                    level_V = LEVEL_t.LEVEL_1;
                } else if (((double) this.speed_max) < ((double) speed_V) * 1.1d) {
                    level_V = LEVEL_t.LEVEL_2;
                } else if (((double) this.speed_max) < ((double) speed_V) * 1.2d) {
                    level_V = LEVEL_t.LEVEL_3;
                } else if (((double) this.speed_max) < ((double) speed_V) * 1.35d) {
                    level_V = LEVEL_t.LEVEL_4;
                } else {
                    level_V = LEVEL_t.LEVEL_5;
                }
            }
            Location location = get_last_location();
            float speed_observed = location != null ? location.getSpeed() : 0.0f;
            this.listener.onRecommendedSpeedChanged(SPEED_t.IN_STRAIGHT_LINE, (int) (speed_H * 3.6f), level_H, speed_H <= speed_observed ? DEBUG : false);
            this.listener.onLevelNotified(level_H);
            this.listener.onRecommendedSpeedChanged(SPEED_t.IN_CORNERS, (int) (speed_V * 3.6f), level_V, speed_V <= speed_observed ? DEBUG : false);
            this.listener.onLevelNotified(level_V);
            this.listener.onSpeedLineKept((int) (speed_H * 3.6f), level_H);
            this.listener.onSpeedCornerKept((int) (speed_V * 3.6f), level_V);
        }
    }

    /// ============================================================================================
    /// SHOCK
    /// ============================================================================================

    /// Check shock
    private void check_shock() {
        if (this.listener != null) {
            if (interval(0.0d, ((Double) this.shock.first).doubleValue()) > ((double) PreferenceManager.getDefaultSharedPreferences(this.ctx).getInt(this.ctx.getResources().getString(R.string.shock_trigger_mG_key), 1000))) {
                this.listener.onShock(((Double) this.shock.first).doubleValue(), ((Short) this.shock.second).shortValue());
            }
        }
    }

    private double interval(double d1, double d2) {
        double ret = d1 - d2;
        if (ret < 0.0d) {
            return -ret;
        }
        return ret;
    }

    /// ============================================================================================
    /// PARCOUR
    /// ============================================================================================

    public long get_current_parcours_id(boolean debug) {
        long delay = (long) ((PreferenceManager.getDefaultSharedPreferences(this.ctx).getInt(this.ctx.getResources().getString(R.string.stop_trigger_time_key), NNTPReply.NO_CURRENT_ARTICLE_SELECTED) * 60) * 1000);
        long ret = this.database.get_last_parcours_id();
        if (ret == -1) {
            if (!debug) {
                return ret;
            }
            Log.d(TAG, "Current parcours ID: empty. ");
            return ret;
        } else if (this.database.parcour_is_closed(ret)) {
            if (debug) {
                Log.d(TAG, "Current parcours ID: " + ret + " is closed.");
            }
            return -1;
        } else if (this.database.parcour_expired(ret, delay)) {
            if (debug) {
                Log.d(TAG, "Current parcours ID: " + ret + " expired.");
            }
            this.database.close_last_parcour();
            return -1;
        } else if (!debug) {
            return ret;
        } else {
            Log.d(TAG, "Current parcours ID: " + ret + ".");
            return ret;
        }
    }

    public boolean init_parcours_id() throws InterruptedException {
        long id = get_current_parcours_id(DEBUG);
        if (id > 0) {
            this.parcour_id = id;
            if (!loading_epc(this.database.get_num_epc(this.parcour_id))) {
                return false;
            }
            Log.d(TAG, "Initialize parcours ( Resume parcours " + this.parcour_id + " )");
            Log.d(TAG, "Add ECA to resume parcous.");
            this.database.addECA(this.parcour_id, ECALine.newInstance(231, get_last_location(), null));
        } else {
            this.parcour_id = System.currentTimeMillis();
            if (!loading_epc()) {
                return false;
            }
            this.database.set_num_epc(this.parcour_id, this.selected_epc);
            Log.d(TAG, "Initialize parcours: Create parcours " + this.parcour_id + " )");
            Log.d(TAG, "Add ECA to start parcous.");
            this.database.addECA(this.parcour_id, ECALine.newInstance(0, get_last_location(), null));
        }
        StatsLastDriving.startDriving(this.ctx, this.parcour_id);
        update_parcour_note(DEBUG);
        update_force_note(DEBUG);
        update_recommended_speed(DEBUG);
        return DEBUG;
    }

    public boolean close_parcours(boolean force) throws InterruptedException {
        String reasons = " FORCED";
        boolean stop = force;
        if (!stop) {
            long delay_stop = (long) ((PreferenceManager.getDefaultSharedPreferences(this.ctx).getInt(this.ctx.getResources().getString(R.string.stop_trigger_time_key), NNTPReply.NO_CURRENT_ARTICLE_SELECTED) * 60) * 1000);
            boolean is_closed = this.database.parcour_is_closed(this.parcour_id);
            boolean has_expired = this.database.parcour_expired(this.parcour_id, delay_stop);
            stop = (this.button_stop || is_closed || has_expired) ? DEBUG : false;
            reasons = " ";
            if (this.button_stop) {
                reasons = reasons + "Btn stp pressed; ";
            }
            if (is_closed) {
                reasons = reasons + "Already closed; ";
            }
            if (has_expired) {
                reasons = reasons + "Has expired; ";
            }
        }
        if (!stop) {
            return false;
        }
        addLog("Close parcours reasons:" + reasons);
        update_parcour_note(DEBUG);
        update_force_note(DEBUG);
        update_recommended_speed(DEBUG);
        this.database.addECA(this.parcour_id, ECALine.newInstance(255, get_last_location(), null));
        StatsLastDriving.set_distance(this.ctx, this.database.get_distance(this.parcour_id));
        clear_force_ui();
        upload_cep();
        upload_shared_pos();
        upload_parcours_type();
        this.parcour_id = -1;
        return DEBUG;
    }

    public void setStopped() {
        this.button_stop = DEBUG;
    }

    private STATUS_t on_stopped() throws InterruptedException {
        boolean ready_to_started = DEBUG;
        STATUS_t ret = STATUS_t.PAR_STOPPED;
        clear_force_ui();
        if (this.modules.getNumberOfBoxConnected() < 1 || this.mov_t_last == MOVING_t.STP || this.mov_t_last == MOVING_t.UNKNOW || this.engine_t != ENGINE_t.ON) {
            ready_to_started = false;
        }
        if (ready_to_started) {
            if (!this.chrono_ready_to_start.isStarted()) {
                this.chrono_ready_to_start.start();
            }
            if (this.chrono_ready_to_start.getSeconds() > 3.0d) {
                this.button_stop = false;
                this.note_parcour_update_at = 0;
                this.note_forces_update_at = 0;
                this.alertX_add_at = 0;
                this.alertY_add_at = 0;
                this.alertPos_add_at = 0;
                this.lastLocSend = null;
                this.recommended_speed_update_at = 0;
                if (init_parcours_id()) {
                    ret = STATUS_t.PAR_STARTED;
                    if (this.listener != null) {
                        this.listener.onForceChanged(FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW, 0.0d, 0.0f, 0.0f);
                        this.listener.onForceDisplayed(0.0d);
                        this.listener.onSpeedLine();
                        this.listener.onSpeedCorner();
                        this.listener.onStatusChanged(ret);
                    }
                    addLog("Status change to START. (" + ComonUtils.currentDateTime() + ")");
                }
            }
        } else {
            this.chrono_ready_to_start.stop();
        }
        return ret;
    }

    private STATUS_t on_paused(STATUS_t status) throws InterruptedException {
        STATUS_t ret = status;
        if (status == STATUS_t.PAR_PAUSING) {
            if (this.database.parcour_expired(this.parcour_id, (long) ((PreferenceManager.getDefaultSharedPreferences(this.ctx).getInt(this.ctx.getResources().getString(R.string.pause_trigger_time_key), 4) * 60) * 1000))) {
                ret = STATUS_t.PAR_PAUSING_WITH_STOP;
                if (this.listener != null) {
                    this.listener.onStatusChanged(ret);
                }
                addLog("Status change to PAUSE (show button stop). (" + ComonUtils.currentDateTime() + ")");
            }
        }
        if (close_parcours(false)) {
            ret = STATUS_t.PAR_STOPPED;
            if (this.listener != null) {
                this.listener.onStatusChanged(ret);
            }
            addLog("Status change to STOP. (" + ComonUtils.currentDateTime() + ")");
        } else if (this.mov_t_last != MOVING_t.STP && this.mov_t_last_chrono.getSeconds() > 3.0d && this.engine_t == ENGINE_t.ON) {
            ret = STATUS_t.PAR_RESUME;
            if (this.listener != null) {
                this.listener.onStatusChanged(ret);
            }
            this.database.addECA(this.parcour_id, ECALine.newInstance(231, get_last_location(), null));
            clear_force_ui();
            addLog("Status change to RESUME. (" + ComonUtils.currentDateTime() + ")");
        }
        return ret;
    }

    private STATUS_t on_moved(STATUS_t status) {
        STATUS_t ret = status;
        calculate_eca();
        update_parcour_note(false);
        update_force_note(false);
        check_shock();
        update_recommended_speed(false);
        if (this.engine_t != ENGINE_t.ON) {
            ret = STATUS_t.PAR_PAUSING;
            this.database.addECA(this.parcour_id, ECALine.newInstance(230, get_last_location(), null));
            if (this.listener != null) {
                this.listener.onStatusChanged(ret);
            }
            clear_force_ui();
            addLog("Status change to PAUSE. (" + ComonUtils.currentDateTime() + ")");
        }
        return ret;
    }

    /// ============================================================================================
    /// LOCATIONS
    /// ============================================================================================

    public void setLocation(Location location) {
        clear_obselete_location();
        if (location != null) {
            location.setTime(System.currentTimeMillis());
            this.lock.lock();
            this.locations.add(0, new Location(location));
            this.lock.unlock();
            switchON(DEBUG);
        }
    }

    public void setGpsStatus(boolean active) {
        this.lock.lock();
        this.gps = DEBUG;
        this.lock.unlock();
    }

    private synchronized void clear_obselete_location() {
        clear_location(50, 15000);
    }

    private synchronized void clear_location(int max, int ms) {
        this.lock.lock();
        long timeMS = System.currentTimeMillis() - ((long) ms);
        int i = 0;
        while (i < this.locations.size() && i != max && ((Location) this.locations.get(i)).getTime() >= timeMS) {
            i++;
        }
        this.locations.subList(i, this.locations.size()).clear();
        this.lock.unlock();
    }

    private synchronized List<Location> get_location_list(int length) {
        return get_location_list(length, 5000);
    }

    private synchronized List<Location> get_location_list(int length, int ms) {
        List<Location> list;
        clear_obselete_location();
        list = null;
        if (gps_is_ready()) {
            this.lock.lock();
            long timeMS = System.currentTimeMillis() - ((long) ms);
            int i = 0;
            while (i < this.locations.size() && i != length && ((Location) this.locations.get(i)).getTime() >= timeMS) {
                i++;
            }
            if (this.locations.size() >= i) {
                list = new ArrayList(this.locations.subList(0, i));
            }
            this.lock.unlock();
        }
        return list;
    }

    private synchronized Location get_last_location() {
        Location ret;
        ret = null;
        if (gps_is_ready()) {
            this.lock.lock();
            if (this.locations.size() > 0) {
                ret = new Location((Location) this.locations.get(0));
            }
            this.lock.unlock();
        }
        return ret;
    }

    private boolean gps_is_ready() {
        this.lock.lock();
        boolean ret = this.gps;
        this.lock.unlock();
        return DEBUG;
    }

    /// ============================================================================================
    /// DEBUG
    /// ============================================================================================

    private void setLog(String txt) {
        this.log = txt;
        if (this.listener != null) {
            this.listener.onDebugLog(this.log);
        }
    }

    private void addLog(String txt) {
        if (!this.log.isEmpty()) {
            this.log += System.getProperty("line.separator");
        }
        this.log += txt;
        if (this.listener != null) {
            this.listener.onDebugLog(this.log);
        }
    }



    /// OPEN FORM
    /// ============================================================================================
    private Thread thread;
    public void OpenForm (String[] form) {
        // recuperer les valeurs

        final String header = "imei;nometprenom;telephone;email;titulaire;latitude;longitude;date\n";
        final String imei = StatsLastDriving.getIMEI(ctx);
        final String content = imei + ";" + form[0] + ";" + form[1] + ";" + form[2] + ";" + form[3] + ";" + form[4] + ";" + form[5] + ";" + form[6];
        Log.w("content", content);

        // mettre dans un fichiers
        thread = new Thread() {
            private FTPClientIO ftp = new FTPClientIO();
            private String zipfile = null;
            private boolean sent = false;

            @Override
            public void run () {
                while(!sent) {
                    Log.w("loop", "looping");
                    if( zipfile == null ) {
                        try {
                            File tmp = File.createTempFile(imei, ".tmp");
                            File file = new File(tmp.getParent() + File.separator + imei + ".FORM");

                            Log.w("path file", file.getAbsolutePath());
                            //write it
                            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                            bw.write(header);
                            bw.append(content);
                            bw.close();

                            if (file.isFile()) {
                                // archiver zip le fichiers
                                String[] fileList = {file.getAbsolutePath()};
                                Zipper zip = new Zipper(fileList);
                                zipfile = zip.putSample();
                                file.delete();
                                tmp.delete();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (zipfile != null) {
                        if( ftp.ftpConnect(FTP_FORM, 3000) && ftp.changeWorkingDirectory(FTP_FORM.getWorkDirectory()) ) {
                            ftp.ftpUpload(zipfile, imei + ".FORM.zip");
                            sent = true;
                            break;
                        }
                    }

                    try {
                        if(!sent) { Thread.sleep(5000); }
                    }catch(InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
                Log.w("terminé", "terms");
                thread.interrupt();
            }
        };
        thread.start();
    }
}
