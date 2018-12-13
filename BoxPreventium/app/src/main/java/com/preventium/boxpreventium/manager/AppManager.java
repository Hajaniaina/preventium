package com.preventium.boxpreventium.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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
import com.preventium.boxpreventium.gui.MainActivity;
import com.preventium.boxpreventium.location.CustomMarkerData;
import com.preventium.boxpreventium.location.CustomMarkerManager;
import com.preventium.boxpreventium.location.DatasMarker;
import com.preventium.boxpreventium.manager.interfaces.AppManagerListener;
import com.preventium.boxpreventium.module.HandlerBox;
import com.preventium.boxpreventium.module.HandlerBox.NotifyListener;
import com.preventium.boxpreventium.module.Load.Load;
import com.preventium.boxpreventium.module.Load.LoadConfig;
import com.preventium.boxpreventium.module.Load.LoadServer;
import com.preventium.boxpreventium.module.Upload.UploadCEP;
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
import com.preventium.boxpreventium.utils.ColorCEP;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.Connectivity;
import com.preventium.boxpreventium.utils.DataLocal;
import com.preventium.boxpreventium.utils.ThreadDefault;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPClientIO;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPConfig;

import org.apache.commons.net.nntp.NNTPReply;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AppManager extends ThreadDefault implements NotifyListener {

    // private static final String HOSTNAME = "www.preventium.fr";
    private final static boolean DEBUG = true;
    private static final float MS_TO_KMH = 3.6f;
    private static final int PORTNUM = 21;
    private static final int SECS_TO_SET_PARCOURS_PAUSE = 20;
    private static final int SECS_TO_SET_PARCOURS_RESUME = 3;
    private static final int SECS_TO_SET_PARCOURS_START = 3;
    private static final int SECS_TO_SET_PARCOURS_STOPPED = 25200;
    private static final String TAG = "AppManager";

    public static FTPConfig FTP_ACTIF;
    public static FTPConfig FTP_CFG;
    public static FTPConfig FTP_DOBJ;
    public static FTPConfig FTP_EPC;
    public static FTPConfig FTP_POS;

    private static long duration = 0;
    private static int nb_box = 0;
    private static float speed_H = 0.0f;
    private static float speed_V = 0.0f;
    private long[] f6X = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private long[] f7Y = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private boolean _tracking = DEBUG;
    private boolean button_stop = false;
    private boolean button_start = false;
    private Chrono chrono_ready_to_start = Chrono.newInstance();
    private ArrayList<CustomMarkerData> customMarkerList = null;
    private boolean customMarkerList_Received = false;
    private Database database = null;
    private long driver_id = 0;
    private ENGINE_t engine_t = ENGINE_t.UNKNOW;
    private long engine_t_changed_at = 0;
    private FilesSender fileSender = null;
    private boolean gps = false;
    private List<Location> locations = new ArrayList();
    private final Lock lock = new ReentrantLock();
    private final Lock lock_timers = new ReentrantLock();
    private String log = "";
    private long note_forces_update_at = 0;
    private long note_parcour_update_at = 0;
    private long parcour_id = 0;
    private String parcoursTypeName = null;
    private long recommended_speed_update_at = 0;
    private float speed_max = 0.0f;
    private long try_send_eca_at = 0;
    private List<Pair<Long, Integer>> ui_timers = new ArrayList();

    private int a, v, f, m;

    private boolean bm = false;
    private Chrono chrono;
    private Chrono chrono_cep;
    private boolean isRestart = false;
    private boolean initWorkTime = false;

    // Data custom
    private HandlerBox modules = null;
    private DataLocal local;
    private Chrono stress;
    private Chrono pChrono;
    private boolean quit = false;

    // reference
    private WeakReference<Context> weakReference;
    private WeakReference<AppManagerListener> listenerWeakReference;

    private static class C01051 implements Runnable {
        AppManager app;
        C01051(AppManager app) {
            this.app = app;
        }

        public void run() {
            this.app.run();
        }
    }



    private static class C01084 implements FilenameFilter {
        C01084() {
        }

        public boolean accept(File dir, String name) {
            return name.toUpperCase().endsWith(".PT");
        }
    }

    public AppManager(Context ctx, AppManagerListener listener) {
        super(null);

        weakReference = new WeakReference<Context>(ctx);
        Context context = weakReference.get();
        if( context != null ) {
            this.modules = new HandlerBox(context, this);
            this.database = new Database(context);
            this.fileSender = new FilesSender(context);
            this.local = DataLocal.get(context);
            this.load = new Load(context, this);
        }

        this.listenerWeakReference = new WeakReference<AppManagerListener>(listener);
        this.chrono = Chrono.newInstance();
        this.chrono.start();
        this.stress = Chrono.newInstance();
        this.stress.start();
        this.chrono_cep = Chrono.newInstance();
        this.chrono_cep.start();
    }

    private void switchON(boolean on) {
        if (!on) {
            setStop();
        } else if (!isRunning()) {
            new Thread(new C01051(this)).start();
        }
    }

    public void raz_calibration() {
        if (this.modules != null) {
            this.modules.on_raz_calibration();
        }
    }

    private int  note_to_score(float note) {
        int score = 5;
        if (note >= 16.0f) {
            return 1;
        }
        if (note >= 13.0f) {
            return 2;
        }
        if (note >= 9.0f) {
            return 3;
        }
        if (note >= 6.0f) {
            return 4;
        }
        return score;
    }

    private boolean _timer = false;
    private STATUS_t status;
    private Load load;
    public void myRun() throws InterruptedException {
        super.myRun();
        Looper.prepare();

        // clear database
        this.database.clear_obselete_data();

        // chargement des données server
        Context context = weakReference.get();
        if( context != null ) {
            new LoadServer(context).Init();
        }

        // crash init
        AppManagerListener listener = listenerWeakReference.get();
        if( listener != null )
            if( (boolean)local.getValue("crashApp", false) && listener != null ) {
                listener.onCrash();
            }

        // load config
        if( context != null ) {
            LoadConfig.init(context);
        }

        // load
        load.onLoad();

        // custom manager marker
        if( context != null ) {
            CustomMarkerManager custom = new CustomMarkerManager(context);
            custom.getMarker();
        }

        // autre
        this.modules.setActive(DEBUG);
        status = first_init();
        upload_eca(DEBUG);

        // init parcours
        pChrono = Chrono.newInstance();
        pChrono.start();

        while(isRunning()) {
            // horodatage actuel
            AppManagerListener _listener = listenerWeakReference.get();
            if (Build.VERSION_CODES.M == Build.VERSION.SDK_INT) {
                int relance = local.getInt("relance", 30);
                relance = relance == 0 ? 30 : relance;
                if (_listener != null && chrono.getMinutes() >= relance && !isRestart) { // >= relance mn
                    chrono.stop();
                    isRestart = true;
                    _listener.checkRestart();
                }
            }

            // 10mn sont écoulé on check
            if (pChrono.getMinutes() >= 10){
                load.onUpdate(); // update
                pChrono.stop();
                pChrono.start();
            }

            // l'app est bloqué
            if( load.isBlocked() ) {
                status = STATUS_t.PAR_STOPPED;
                if( _listener != null ) {
                    _listener.onStatusChanged(STATUS_t.CHECK_ACTIF); // et on bloque toujours
                }
            }

            // test de connexion tout le temps
            check_internet_is_active();
            // savoir le tracking de parcours est actif
            update_tracking_status();
            // detection BM ou/et Leurre
            modules.setActive(DEBUG);

            // efface les donnée eca 5jrs avant
            database.clear_obselete_data();

            sleep(500);
            // envoie des eca tout les 1mn
            upload_eca(false);
            // maj du temps de conduite
            update_driving_time();
            // calcul si un mouvement est déclenché
            calc_movements();

            // MainActivity.instance().Alert(status.toString(), Toast.LENGTH_SHORT);
            if (button_stop) {
                status = STATUS_t.PAR_PAUSING;
            }

            // send cep in 30mn
            if (chrono_cep.getMinutes() >= 30) {
                // send cep
                upload_cep();
                // stop and start again
                chrono_cep.stop();
                chrono_cep.start();
            }

            switch (status) {
                case GETTING_CFG:
                case GETTING_EPC:
                case GETTING_DOBJ:
                    break;
                case PAR_STOPPED:
                    try {
                        status = on_stopped();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case PAR_STARTED:
                case PAR_RESUME:
                    status = on_moved(status);
                    initWorkTime();
                    break;
                case PAR_PAUSING:
                case PAR_PAUSING_WITH_STOP:
                    try {
                        status = on_paused(status);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            listen_timers(status);
            button_start = false;
        }

        this.modules.setActive(false);
        Looper.loop();
    }

    public boolean is_timer() {
        return _timer;
    }

    public void set_timer(boolean _timer) {
        this._timer = _timer;
    }

    public boolean isWorkTimeOver () {
        int timeSave = local.getInt("workTime", 0);
        float beginTime = local.getFloat("WorkBeginTime", 0);
        float currentTime = System.currentTimeMillis();
        float calcHours = (currentTime - beginTime) / 3600000;
        return Math.round(calcHours) >= timeSave;
    }

    public void initWorkTime () {
        // if( !initWorkTime ) {
        // si dépassé et n'est pas terminé, on demande toujours
        // si pas encore initié on initie
        float beginTime = local.getFloat("WorkBeginTime", 0);
        if( !initWorkTime  && (beginTime == 0 || this.isWorkTimeOver()) ) {
            local.setValue("WorkBeginTime", (float)System.currentTimeMillis());
            local.apply();
            initWorkTime = true;
            Log.v("BeginWork Time", "initialisation");
        }
    }

    // HANDLER BOX
    @Override
    public void onScanState(boolean scanning) {
        Log.d(TAG, "Searching preventium box is enable: " + scanning);
    }

    public void onDeviceState(String device_mac, boolean connected) {

        Location location = get_last_location();
        int note = (int) calc_note(this.parcour_id);
        //int vitesse_ld = (int) (speed_H * 3.6f);
        //int vitesse_vr = (int) (speed_V * 3.6f);

        Context context = weakReference.get();
        if( context != null ) {
            MainActivity main = (MainActivity) context;
            int vitesse_ld = main.vitesse_ld;
            int vitesse_vr = main.vitesse_vr;

            int nbBox = nb_box;
            long distance_covered = this.database.get_distance(this.parcour_id);
            long parcour_duration = (long) (((double) duration) * 0.001d);
            Database database = this.database;
            ColorCEP color = ColorCEP.getInstance();
            this.database.addCEP(location, device_mac, note, vitesse_ld, vitesse_vr, distance_covered, parcour_duration, Database.get_eca_counter(context, this.parcour_id), nbBox, color.getA(), color.getV(), color.getF(), color.getM(), connected);
        }
    }

    @Override
    public void onBMExist (boolean bm) {
        this.bm = bm;
    }

    public boolean getBMExist () {
        return this.bm;
    }

    public void onNumberOfBox(int nb) {
        nb_box = nb;
        Log.d(TAG, "Number of preventium device connected changed: " + nb);

        AppManagerListener listener = listenerWeakReference.get();
        if (listener != null) {
            listener.onNumberOfBoxChanged(nb, this.bm);
        }
    }

    @Override
    public void onForceChanged(Pair<Long, Short> smooth, Pair<Long, Short> shock) {
        this.smooth = smooth;
        this.shock = shock;
    }

    public synchronized void onEngineStateChanged(ENGINE_t state) {
        this.engine_t = state;
        this.engine_t_changed_at = System.currentTimeMillis();
    }

    public void onCalibrateOnConstantSpeed() {
        AppManagerListener listener = listenerWeakReference.get();
        if (listener != null) {
            listener.onCalibrateOnConstantSpeed();
        }
    }

    public void onCalibrateOnAcceleration() {
        AppManagerListener listener = listenerWeakReference.get();
        if (listener != null) {
            listener.onCalibrateOnAcceleration();
        }
    }

    public void onCalibrateRAZ() {
        onForceChanged( Pair.create((long)0, (short)0),Pair.create((long)0,(short)0));
        AppManagerListener listener = listenerWeakReference.get();
        if( listener != null ) listener.onCalibrateRAZ();
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

        AppManagerListener listener = listenerWeakReference.get();
        if (listener != null) {
            listener.onDebugLog("");
            //####santo
            listener.onStatusChanged( STATUS_t.PAR_PAUSING );
            listener.onStatusChanged( STATUS_t.PAR_STOPPED );

            listener.onDrivingTimeChanged(this.chronoRideTxt);
            listener.onNoteChanged(20, LEVEL_t.LEVEL_1, LEVEL_t.LEVEL_1);
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
        Context context = weakReference.get();
        AppManagerListener listener = listenerWeakReference.get();
        if (listener != null) {
            long id = get_current_parcours_id(false);
            long ms = id > 0 ? System.currentTimeMillis() - id : 0;
            duration = ms;
            String txt = String.format(Locale.getDefault(), "%02d:%02d", new Object[]{Integer.valueOf(0), Integer.valueOf(0)});
            if (ms > 0) {
                long h = ms / 3600000;
                long m = ms % 3600000 > 0 ? (ms % 3600000) / 60000 : 0;
                txt = String.format(Locale.getDefault(), "%02d:%02d", new Object[]{Long.valueOf(h), Long.valueOf(m)});
                if( context != null ) StatsLastDriving.set_times(context, (long) (((double) ms) * 0.001d));
            }
            if ( !this.chronoRideTxt.equals(txt) && listener != null ) {
                listener.onDrivingTimeChanged(txt);
                this.chronoRideTxt = txt;
            }
        }
    }

    private void clear_force_ui() {
        if (this.seuil_ui != null && this.seuil_chrono_x.getSeconds() > 3.0d && this.seuil_chrono_y.getSeconds() > 3.0d) {
            AppManagerListener listener = listenerWeakReference.get();
            if (listener != null) {
                listener.onForceChanged(FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW, 0.0d, 0.0f, 0.0f);
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
                    AppManagerListener listener = listenerWeakReference.get();
                    if (listener != null) {
                        listener.onUiTimeout(timer_id, status);
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
        Context context = weakReference.get();
        if( context != null ) {
            boolean active = Connectivity.isConnected(context);
            if (active != this.internet_active) {
                this.internet_active = active;
                AppManagerListener listener = listenerWeakReference.get();
                if (listener != null) {
                    listener.onInternetConnectionChanged();
                }
            }
        }
    }

    /// ============================================================================================
    /// .CFG
    /// ============================================================================================

    // Downloading .cfg file if is needed

    public boolean download_cfg(boolean display) throws InterruptedException {
        boolean cfg = false;

        Context context = weakReference.get();
        if( context == null ) return false;

        File folder = new File(context.getFilesDir(), "");
        ReaderCFGFile reader = new ReaderCFGFile();

        FTPClientIO ftp = new FTPClientIO();

        while ( isRunning() && !cfg  ){
            // Trying to connect to FTP server...
            if( FTP_CFG == null || !ftp.ftpConnect(FTP_CFG, 5000) ) {
                check_internet_is_active();
            } else {
                // Checking if .CFG file is in FTP server ?
                String srcFileName = ComonUtils.getIMEInumber(context) + ".CFG";
                String srcAckName = ComonUtils.getIMEInumber(context) + "_ok.CFG";
                boolean exist_server_cfg = ftp.checkFileExists( srcFileName );
                boolean exist_server_ack = ftp.checkFileExists( srcAckName );

                // If .CFG file exist in the FTP server
                cfg = ( exist_server_ack && reader.loadFromApp(context) );
                if( !cfg ) {
                    if (exist_server_cfg) {
                        // Create folder if not exist
                        if (!folder.exists())
                            if (!folder.mkdirs())
                                Log.w(TAG, "Error while trying to create new folder!");
                        if (folder.exists()) {
                            // Trying to download .CFG file...
                            String desFileName = String.format(Locale.getDefault(), "%s/%s", context.getFilesDir(), srcFileName);
                            if (ftp.ftpDownload(srcFileName, desFileName)) {
                                cfg = reader.read(desFileName);  // santooo
                                Log.e("FTP cfg : ", String.valueOf(cfg));
                                if (cfg) {
                                    String serv = reader.getServerUrl();
                                    Log.e("FTP azo : ", serv);
                                    reader.applyToApp(context);
                                    // envoi acknowledge
                                    try {
                                        File temp = File.createTempFile("temp-file-name", ".tmp");
                                        String ackFileName = ComonUtils.getIMEInumber(context) + "_ok.CFG";
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
                        cfg = reader.loadFromApp(context);
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

    public boolean IMEI_is_actif() {
        boolean exist_actif = false;
        AppManagerListener listener = listenerWeakReference.get();
        if (listener != null) {
            listener.onStatusChanged(STATUS_t.CHECK_ACTIF);
        }
        FTPClientIO ftp = new FTPClientIO();
        do {
            if (FTP_ACTIF != null && ftp.ftpConnect(FTP_ACTIF, 5000)) {
                exist_actif = false;
                if (ftp.changeWorkingDirectory(FTP_ACTIF.getWorkDirectory())) {
                    Context context = weakReference.get();
                    exist_actif = ftp.checkFileExists(ComonUtils.getIMEInumber(context));
                    // exist_actif = true;

                    if (exist_actif) {
                        try {
                            sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
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
    public boolean download_epc(boolean display) throws InterruptedException {
        // var
        boolean ready = false;
        String srcFileName = "";
        String srcAckName = "";
        String srcNameName = "";
        String desFileName = "";

        Context context = weakReference.get();
        if( context == null ) return false;

        File dir = context.getFilesDir();
        if( !dir.isDirectory() ) dir.mkdir();

        // var system
        File folder = new File(context.getFilesDir(), "");
        ReaderEPCFile reader = new ReaderEPCFile();
        FTPClientIO ftp = new FTPClientIO();

        if( _timer ) { // à condition que le timer
            // on reload les données
            if( dir.exists() && dir.isDirectory() ) {
                for (int i = 1; i < 6; i++) {
                    srcFileName = reader.getEPCFileName(context, i, false);
                    desFileName = String.format(Locale.getDefault(), "%s/%s", dir.getAbsolutePath(), srcFileName); // ctx.getFilesDir()
                    boolean read = reader.read(desFileName);
                    if (read) reader.applyToApp(context, i);
                }

                // normal name
                srcNameName = reader.getNameFileName(context);
                desFileName = String.format(Locale.getDefault(), "%s/%s", dir.getAbsolutePath(), srcNameName);
                if (reader.readname(desFileName)) reader.applyNameToApp(context);
            }
            return true;
        }

        // status
        AppManagerListener listener = listenerWeakReference.get();
        if( listener != null && !display ) listener.onStatusChanged( STATUS_t.GETTING_EPC );
        while( isRunning() && !ready ) {
            if( listener != null && !display ) listener.onStatusChanged( STATUS_t.GETTING_EPC );

            // Trying to connect to FTP server...
            if( FTP_EPC == null || !ftp.ftpConnect(FTP_EPC, 5000) ) {
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

                    int i = 1;
                    while( i <= 5 && isRunning() ) {

                        // Checking if .EPC file is in FTP server ?
                        srcFileName = reader.getEPCFileName(context, i, false);
                        srcAckName = reader.getEPCFileName(context, i, true);
                        srcNameName = reader.getNameFileName(context);
                        exist_server_epc = ftp.checkFileExists( srcFileName );
                        exist_server_ack = ftp.checkFileExists( srcAckName );
                        exist_server_name = ftp.checkFileExists( srcNameName );

                        // If .EPC file exist in the FTP server
                        epc = ( exist_server_ack && reader.loadFromApp(context,i) );
                        if( !epc ) {
                            if (exist_server_epc) {
                                // Create folder if not exist
                                if (!folder.exists())
                                    if (!folder.mkdirs())
                                        Log.w(TAG, "Error while trying to create new folder!");
                                if (folder.exists()) {
                                    // Trying to download .EPC file...
                                    desFileName = String.format(Locale.getDefault(), "%s/%s", dir.getAbsolutePath(), srcFileName);
                                    if (ftp.ftpDownload(srcFileName, desFileName)) {
                                        epc = reader.read(desFileName);
                                        if( epc ) {
                                            reader.applyToApp(context,i);
                                            // envoi acknowledge
                                            try {
                                                File temp = File.createTempFile("temp-file-name", ".tmp");
                                                ftp.ftpUpload(temp.getPath(), srcAckName);
                                                temp.delete();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            /**
                                             * le code ci-dessous supprime le fichier imei_%d.EPC du système android
                                             * après avoir recupérer le contenu
                                             */
                                            // new File(desFileName).delete();
                                        }
                                    }
                                    //get the EPC.NAME info in server
                                    if (exist_server_name) {
                                        if (!folder.exists())
                                            if (!folder.mkdirs())
                                                Log.w(TAG, "Error while trying to create new folder!");
                                        if (folder.exists()) {
                                            // Trying to download EPC.NAME file...
                                            desFileName = String.format(Locale.getDefault(), "%s/%s", dir.getAbsolutePath(), srcNameName);
                                            if (ftp.ftpDownload(srcNameName, desFileName)) {
                                                epc_name = reader.readname(desFileName);
                                                if( epc_name ) {
                                                    reader.applyNameToApp(context);
                                                }
                                            }
                                        }
                                    } else{
                                        reader.loadNameFromApp(context);
                                    }

                                }
                            }
                        } else {
                            epc = reader.loadFromApp(context,i);
                        }
                        i++;
                    }
                }
                // Disconnect from FTP server.
                ftp.ftpDisconnect();
            }

            ready = !DataEPC.getAppEpcExist(context).isEmpty();
            if( isRunning() && !ready ) sleep(100);
        }

        return ready;
    }

    private boolean loading_epc() throws InterruptedException {
        boolean ready = false;
        while (isRunning() && !ready) {
            this.selected_epc = 0;
            Context context = weakReference.get();
            ready = this.readerEPCFile.loadFromApp(context);
            if (!ready) {
                for (Integer i : DataEPC.getAppEpcExist(context)) {
                    ready = this.readerEPCFile.loadFromApp(context, i.intValue());
                    if (ready) {
                        this.selected_epc = i.intValue();
                        break;
                    }
                }
            }
            this.selected_epc = this.readerEPCFile.selectedEPC(context);
            if (!ready) {
                download_epc(false);
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
            Context context = weakReference.get();
            if( context != null ) {
                ready = this.readerEPCFile.loadFromApp(context, num);
                if (!ready) {
                    download_epc(false);
                }
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
    public boolean download_dobj(boolean display) throws InterruptedException {
        boolean ready = false;
        Context context = weakReference.get();
        if( context == null ) return false;

        File folder = new File(context.getFilesDir(), "");
        ReaderDOBJFile reader = new ReaderDOBJFile();
        FTPClientIO ftp = new FTPClientIO();

        while( isRunning() && !ready ) {
            // if( listener != null ) listener.onStatusChanged( STATUS_t.GETTING_DOBJ );

            // Trying to connect to FTP server...
            if( FTP_DOBJ == null || !ftp.ftpConnect(FTP_DOBJ, 5000) ) {
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
                    String srcFileName = ReaderDOBJFile.getOBJFileName(context, false);
                    String srcAckName = ReaderDOBJFile.getOBJFileName(context, true);
                    boolean exist_server_dobj = ftp.checkFileExists( srcFileName );
                    boolean exist_server_ack = ftp.checkFileExists( srcAckName );

                    // If .DOBJ file exist in the FTP server
                    dobj = ( exist_server_ack && DataDOBJ.preferenceFileExist(context) );
                    if( !dobj ) {
                        if (exist_server_dobj) {
                            // Create folder if not exist
                            if (!folder.exists())
                                if (!folder.mkdirs())
                                    Log.w(TAG, "Error while trying to create new folder!");
                            if (folder.exists()) {
                                // Trying to download .OBJ file...
                                String desFileName = String.format(Locale.getDefault(), "%s/%s", context.getFilesDir(), srcFileName);
                                if (ftp.ftpDownload(srcFileName, desFileName)) {
                                    dobj = reader.read(context,desFileName,false);
                                    if( dobj ) {
                                        ready = reader.read(context,desFileName,true);
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

            if( !ready ) ready = DataDOBJ.preferenceFileExist(context);
            if( isRunning() && !ready ) sleep(1000);
        }
        return ready;
    }

    /// ============================================================================================
    /// .ECA
    /// ============================================================================================

    private boolean upload_eca(boolean now) {
        if (!now && this.try_send_eca_at + 60000 >= System.currentTimeMillis()) { // envoie tout les 1mn de plus
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
        AppManagerListener listener = listenerWeakReference.get();
        Context context = weakReference.get();
        if (isRunning()) {
            if (listener != null) {
                listener.onStatusChanged(STATUS_t.SETTING_CEP);
            }
            if (this.parcour_id > 0) {
                this.database.create_cep_file(this.parcour_id);
            }
            // UPLOAD .CEP FILES
            new UploadCEP(context, listener).setQuite(quit);
        }
    }

    private boolean sent = false;



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
        final Context context = weakReference.get();
        final AppManagerListener listener = listenerWeakReference.get();
        if (isRunning() && listener != null && context != null) {
            final DatasMarker markerData = new DatasMarker(context);
            this.customMarkerList_Received = false;
            this.customMarkerList = null;
            listener.onCustomMarkerDataListGet();
            Chrono chrono = Chrono.newInstance();
            chrono.start();
            while (isRunning() && chrono.getSeconds() < 10.0d && !this.customMarkerList_Received) {
                sleep(500);
            }
            listener.onStatusChanged(STATUS_t.SETTING_MARKERS);

            // CREATE FILE
            File folder = new File(context.getFilesDir(), "SHARE");
            if (this.customMarkerList_Received && this.parcour_id > 0 && this.customMarkerList != null && this.customMarkerList.size() > 0) {
                if (!(folder.exists() || folder.mkdirs())) {
                    Log.w(TAG, "Error while trying to create new folder!");
                }
                if (folder.exists()) {
                    markerData.create_sharePos_file(this.customMarkerList, folder);
                }
            }

            // UPLOAD .SHARE FILES
            final Handler handler = new Handler();
            handler.post(new Runnable () {
                boolean send = false;
                @Override
                public void run() {
                    if( !send && ComonUtils.haveInternetConnected(context) ) {
                        markerData.sharePos();
                        send = true;
                    }

                    if( markerData.is_send ) {
                        listener.onStatusChanged(STATUS_t.PAR_STOPPED);
                        handler.removeCallbacks(this);
                    } else
                        handler.postDelayed(this, 1000);
                }
            });
        }
    }

    // Dowloading shared positions
    private boolean download_shared_pos() throws InterruptedException {
        boolean ready = false;
        final AppManagerListener listener = listenerWeakReference.get();
        final Context context = weakReference.get();
        if (listener != null) {
            listener.onStatusChanged(STATUS_t.GETTING_MARKERS_SHARED);
        }

        if( context != null ) {
            File folder = new File(context.getFilesDir(), "");
            FTPClientIO ftp = new FTPClientIO();
            while (isRunning() && !ready) {
                if (listener != null) {
                    listener.onStatusChanged(STATUS_t.GETTING_MARKERS_SHARED);
                }
                if (ftp.ftpConnect(FTP_POS, 5000)) {
                    boolean change_directory = DEBUG;
                    if (!(FTP_POS.getWorkDirectory() == null || FTP_POS.getWorkDirectory().isEmpty() || FTP_POS.getWorkDirectory().equals("/"))) {
                        change_directory = ftp.changeWorkingDirectory(FTP_POS.getWorkDirectory());
                    }
                    if (change_directory) {
                        String srcFileName = ReaderPOSSFile.getFileName(context, false);
                        String srcAckName = ReaderPOSSFile.getFileName(context, DEBUG);
                        boolean exist_server_poss = ftp.checkFileExists(srcFileName);
                        boolean exist_server_ack = ftp.checkFileExists(srcAckName);
                        boolean need_download = ((!exist_server_poss || exist_server_ack) && (!exist_server_poss || ReaderPOSSFile.existLocalFile(context))) ? false : DEBUG;
                        if (!(folder.exists() || folder.mkdirs())) {
                            Log.w(TAG, "Error while trying to create new folder!");
                        }
                        String desFileName = String.format(Locale.getDefault(), "%s/%s", new Object[]{context.getFilesDir(), srcFileName});
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
                            if (!ReaderPOSSFile.existLocalFile(context) || (!exist_server_poss && !exist_server_ack)) {
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
        final AppManagerListener listener = listenerWeakReference.get();
        final Context context = weakReference.get();
        if (listener != null) {
            File folder;
            File file;
            this.parcoursTypeName = null;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String key = context.getResources().getString(R.string.parcours_type_enabled_key);
            if (sp.getBoolean(key, false)) {
                Editor editor = sp.edit();
                editor.putBoolean(key, false);
                editor.apply();
                this.parcoursTypeName = sp.getString(key, "");
            }
            listener.onStatusChanged(STATUS_t.SETTING_PARCOUR_TYPE);
            if (!(this.parcoursTypeName == null || this.parcour_id <= 0 || this.parcoursTypeName.isEmpty())) {
                folder = new File(context.getFilesDir(), "PT");
                if (!(folder.exists() || folder.mkdirs())) {
                    Log.w(TAG, "Error while trying to create new folder!");
                }
                if (folder.exists()) {
                    file = new File(folder.getAbsolutePath(), String.format(Locale.getDefault(), "%s_%d.PT", new Object[]{ComonUtils.getIMEInumber(context), Long.valueOf(this.parcour_id)}));
                    try {
                        if (file.createNewFile()) {
                            FileWriter fileWriter = new FileWriter(file);
                            fileWriter.write(String.format(Locale.getDefault(), "%s;%d;%s", new Object[]{ComonUtils.getIMEInumber(context), Long.valueOf(this.parcour_id), this.parcoursTypeName}));
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
            folder = new File(context.getFilesDir(), "PT");
            if (folder.exists()) {
                File[] listOfFiles = folder.listFiles(new C01084());
                if (listOfFiles != null && listOfFiles.length > 0) {
                    FTPConfig config = DataCFG.getFptConfig(context);
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
            listener.onStatusChanged(STATUS_t.PAR_STOPPED);
        }
    }


    /// ============================================================================================
    /// TRACKING
    /// ============================================================================================

    private void update_tracking_status() {
        Context context = weakReference.get();
        if( context != null ) {
            this._tracking = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.tracking_activated_key), DEBUG);
        }
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
    Pair<Long,Short> smooth = Pair.create((long)0,(short)0);
    Pair<Long,Short> shock = Pair.create((long)0,(short)0);


    private static float median(float[] m) {
        Arrays.sort(m);
        float median;
        if (m.length % 2 == 0)
            median = (m[m.length/2] + m[m.length/2 - 1])/2f;
        else
            median = m[m.length/2];
        return median;
    }

    private double SpeedToXmG( @NonNull float v1, @NonNull float v2, @NonNull long t1, @NonNull long t2 ) {
        // Pour calculer l'accélération longitudinale (accélération ou freinage) avec comme unité le mG :
        // il faut connaître : la vitesse (v(t)) à l'instant t et à l'instant précédent(v(t-1)) et le delta t entre ces deux mesures.
        // a = ( v(t) - v(t-1) )/(9.81*( t - (t-1) ) )
        return ((v2 - v1)
                / (9.81 * ((t2 - t1) * 0.001)))
                * 1000.0;
    }

    private boolean isRightRoad( Location location_1, Location location_2, Location location_3 ) {
        double Lat_rad_1 = location_1.getLatitude() * Math.PI / 180.0;
        double Lat_rad_2 = location_2.getLatitude() * Math.PI / 180.0;
        double Lat_rad_3 = location_3.getLatitude() * Math.PI / 180.0;

        double Long_rad_1 = location_1.getLongitude() * Math.PI / 180.0;
        double Long_rad_2 = location_2.getLongitude() * Math.PI / 180.0;
        double Long_rad_3 = location_3.getLongitude() * Math.PI / 180.0;

        double Delta_L_rad_1 = Lat_rad_2 - Lat_rad_1;
        double Delta_L_rad_2 = Lat_rad_3 - Lat_rad_2;

        double X_1 = Math.cos(Long_rad_2) * Math.sin(Delta_L_rad_1);
        double X_2 = Math.cos(Long_rad_3) * Math.sin(Delta_L_rad_2);

        double Y_1 = (Math.cos(Long_rad_1) * Math.sin(Long_rad_2))
                - (Math.sin(Long_rad_1) * Math.cos(Long_rad_2) * Math.cos(Delta_L_rad_1) );
        double Y_2 = (Math.cos(Long_rad_2) * Math.sin(Long_rad_3))
                - (Math.sin(Long_rad_2) * Math.cos(Long_rad_3) * Math.cos(Delta_L_rad_2) );

        double A_rad_1 = Math.atan2( X_1, Y_1 );
        double A_rad_2 = Math.atan2( X_2, Y_2 );

        double A_deg_1 = A_rad_1 * 360.0 / Math.PI;
        double A_deg_2 = A_rad_2 * 360.0 / Math.PI;

        return ( (Math.max(A_deg_1,A_deg_2) - Math.min(A_deg_1,A_deg_2) ) < 3.0 );
    }

    private float V_median = 0f;
    private long T_median = System.currentTimeMillis();
    private long XmG = 0;
    private long XmG_elapsed = 0;

    private void calc_movements() {
        // Important, reset XmG for not re-count in longitudinal counter
        this.XmG = 0;  // Important, Reset XmG value
        this.XmG_elapsed = 0;  // Important, Reset XmG elapsed

        // Check if ready to update force
        Location loc = get_last_location();
        if (loc == null || loc.getTime() <= T_median ) return;

        // Clear variable
        boolean rightRoad = false;
        float acceleration = 0f;
        this.mov_t = MOVING_t.UNKNOW;

        // Get locations and the longitudinal acceleration in mG
        List<Location> list = get_location_list(3,5000);
        if( list != null && list.size() >= 3 )
        {
            // check if the road is straight and get speed values
            int i = list.size()-1;
            rightRoad = isRightRoad( list.get(i-2), list.get(i-1), list.get(i) );
            float v[] = { list.get(i-2).getSpeed() , list.get(i-1).getSpeed() , list.get(i).getSpeed() };

            // Calculate median of the speed values
            float V_median_next = median(v);
            long T_median_next = list.get(i-2).getTime();
            // Calculate acceleration
            acceleration = V_median_next - V_median;
            // Calculate force longitudinal
            this.XmG = Math.round( SpeedToXmG(V_median_next,V_median,T_median_next,T_median) );
            this.XmG_elapsed = T_median_next - T_median;
            // Save median result
            this.V_median = V_median_next;
            this.T_median = T_median_next;
        }
        else
        {
            this.V_median = 0f;
            this.T_median = System.currentTimeMillis();
        }

        // Set moving status
        if (V_median * MS_TO_KMH <= 3f) mov_t = MOVING_t.STP; // 3f
        else if ( Math.abs( 0f - (acceleration * MS_TO_KMH) ) < 2f ) mov_t = MOVING_t.CST; // 2f
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

    private long longitudinal_elapsed[] = { 0,0,0,0,0,0,0,0,0,0 };
    private static void update_longitudinal_elapsed( long tab[], FORCE_t t, LEVEL_t l, long elapsed ){

        if( elapsed <= 0 ) return;

    /*private double LocationsToXmG(@NonNull Location l0, @NonNull Location l1) {
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
    private static void update_tab_X( long tab[], FORCE_t t, LEVEL_t l, long s ){*/
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
                tab[i] = (tab[i] + elapsed);
            } else {
                tab[i] = 0;
            }
        }
    }
    private ForceSeuil get_longitudial_seuil( long tab[] ){
        ForceSeuil ret = null;
        // 0 to 4: LEVEL_1 to LEVEL_5 for A
        // 5 to 9: LEVEL_1 to LEVEL_5 for F
        int a = -1;
        long tps;
        for( int i = 0; i < 10; i++ ) {
            if( tab[i] > 0 ) {
                if( tab[i] >= readerEPCFile.get_TPS_ms(i) ) {
                    a = i;
                }
            }
        }
        if( a >= 0 && a < 10 ) {
            ret = readerEPCFile.getForceSeuil(a);
        }
        return ret;
    }

    private long lateral_time[] = { 0,0,0,0,0,0,0,0,0,0 };
    private static void update_lateral_elapsed( long tab[], FORCE_t t, LEVEL_t l, long s ){
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
    private ForceSeuil get_lateral_seuil( long tab[], long s ){
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

    public double getStress () {
        return this.stress.getMinutes();
    }

    synchronized private void calculate_eca() {
        // Get seuils of runtime alert
        long ST = System.currentTimeMillis();
        Location loc = get_last_location();
        boolean pass = false;
        // boolean _stress = this.stress.getMinutes() >= 9; // n'affiche les evalueation que touts les 10 minutes

        ForceSeuil seuil_x = readerEPCFile.getForceSeuilForX(XmG);
        ForceSeuil seuil_y = readerEPCFile.getForceSeuilForY(smooth.first);
        if( loc == null || (loc.getSpeed() * MS_TO_KMH) < 0.0 )
        {
            seuil_x = null;
            seuil_y = null;
        }
        else
        {
            // LONGITUDINAL
            FORCE_t type_X = FORCE_t.UNKNOW;
            FORCE_t type_Y = FORCE_t.UNKNOW;
            LEVEL_t level_X = LEVEL_t.LEVEL_UNKNOW;
            if (seuil_x != null)
            {
                type_X = seuil_x.type;
                level_X = seuil_x.level;
            }
            // Update start at for all alerts
            update_longitudinal_elapsed(longitudinal_elapsed, type_X, level_X, XmG_elapsed);
            // Gettings alerts
            seuil_x = get_longitudial_seuil(longitudinal_elapsed);

            // ADD ECA? ( location with X alert )
            boolean add_eca = false;
            if (seuil_x != null) {
                if (_tracking) {
                    if( alertX_add_id != seuil_x.IDAlert || // ça peut se passer car id peut toujour être différent à l'id courant
                            ST > alertX_add_at + (seuil_x.TPS * 1000) ) {

                        database.addECA(parcour_id, ECALine.newInstance(seuil_x.IDAlert, loc, null));
                        alertX_add_at = ST;
                        alertX_add_id = seuil_x.IDAlert;
                        lastLocSend = loc;
                        add_eca = true;
                        pass = true;
                    }
                }
            }

            // LATERAL
            // FORCE_t type_Y = FORCE_t.UNKNOW;
            LEVEL_t level_Y = LEVEL_t.LEVEL_UNKNOW;
            if (seuil_y != null)
            {
                type_Y = seuil_y.type;
                level_Y = seuil_y.level;
            }
            // Update start at for all alerts
            update_lateral_elapsed(lateral_time, type_Y, level_Y, ST);
            // Gettings alerts
            seuil_y = get_lateral_seuil(lateral_time, ST);
            // ADD ECA? ( location with Y alert )
            if (seuil_y != null) {
                if (_tracking) {
                    if( alertY_add_id != seuil_y.IDAlert || // ça peut se passer car id peut toujour être différent à l'id courant
                            ST > alertY_add_at + (seuil_y.TPS * 1000)  ) {
                        database.addECA(parcour_id, ECALine.newInstance(seuil_y.IDAlert, loc, null));
                        //Log.d("AAA", "ADD ECA " + seuil_y.toString());
                        alertY_add_at = ST;
                        alertY_add_id = seuil_y.IDAlert;
                        lastLocSend = loc;
                        pass = true;
                    }
                }
            }


            // ADD ECA? (simple location without alert)
            if (_tracking) {
                List<Location> locations = get_location_list(1);
                if (locations != null && locations.size() >= 1) {
                    float min_meters = ((locations.get(0).getSpeed() * MS_TO_KMH) < 70f) ? 5f : 15f;
                    if (lastLocSend == null
                            || locations.get(0).distanceTo(lastLocSend) > min_meters) {

                        if (lastLocSend == null) lastLocSend = new Location(locations.get(0));
                        database.addECA(parcour_id, ECALine.newInstance(locations.get(0), lastLocSend));
                        lastLocSend = new Location(locations.get(0));
                        pass = true;
                    }
                }
            }
        }

        // Update UI interface
        AppManagerListener listener = listenerWeakReference.get();
        if( listener != null ) {
            // Select seuil for ui
            ForceSeuil seuil = null;
            double force;
            if (seuil_x == null) {
                seuil = seuil_y;
                force = this.smooth.first;
            } else if (seuil_y == null) {
                seuil = seuil_x;
                force = this.XmG;
            } else {
                seuil = (seuil_x.level.getValue() >= seuil_y.level.getValue()) ? seuil_x : seuil_y;
                force = (seuil_x.level.getValue() >= seuil_y.level.getValue()) ? this.XmG : this.smooth.first;
            }

            int t_ms = ( seuil_ui == null ) ? 0 : seuil_ui.level.get_ui_time_ms();

            if( seuil_ui == null )
            {
                if( seuil == null )
                {
                    alertUI_add_at = ST;
                }
                else
                {
                    alertUI_add_at = ST;
                    seuil_ui = seuil;
                    listener.onForceChanged(seuil.type, seuil.level, force, speed_H * 3.6f, speed_V * 3.6f);
                    listener.onLevelNotified(seuil.level);
                    listener.onForceDisplayed(force);
                }
            }
            else
            {
                if( seuil == null )
                {
                    if( alertUI_add_at + t_ms < ST ) // ne se déclenche pas car inférieur au temps déclenchement
                    {
                        alertUI_add_at = ST;
                        seuil_ui = null;
                        listener.onForceChanged(FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW, 0.0d, 0.0f, 0.0f);
                    }
                }
                else
                {
                    if( (seuil.type.getAxe() == seuil_ui.type.getAxe())
                            && (seuil.level.getValue() > seuil_ui.level.getValue()) )
                    {
                        alertUI_add_at = ST;
                        seuil_ui = seuil;
                        listener.onForceChanged(seuil.type, seuil.level, force, speed_H * 3.6f, speed_V * 3.6f);
                        listener.onLevelNotified(seuil.level);
                        listener.onForceDisplayed(force);
                    }
                    else
                    {
                        if( alertUI_add_at + t_ms < ST )
                        {
                            alertUI_add_at = ST;
                            seuil_ui = seuil;
                            listener.onForceChanged(seuil.type, seuil.level, force, speed_H * 3.6f, speed_V * 3.6f);
                            listener.onLevelNotified(seuil.level);
                            listener.onForceDisplayed(force);
                        }
                    }
                }
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
        final Context context = weakReference.get();
        if( context == null ) return 20.0f;

        float ret;
        float coeff_general = DataDOBJ.get_coefficient_general(context, type);
        int[] coeff_force = new int[5];
        coeff_force[0] = DataDOBJ.get_coefficient(context, type, "V");
        coeff_force[1] = DataDOBJ.get_coefficient(context, type, "B");
        coeff_force[2] = DataDOBJ.get_coefficient(context, type, "J");
        coeff_force[3] = DataDOBJ.get_coefficient(context, type, "O");
        coeff_force[4] = DataDOBJ.get_coefficient(context, type, "R");
        int[] nb_evt = new int[5];

        /* bug EPC File */
        if( this.readerEPCFile.getForceSeuil(0) == null ) return 20.0f;

        // Log.v("EPCFILE", )
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
            if (StatsLastDriving.get_start_at(context) == parcour_id) {
                if ("A".equals(type)) {
                    StatsLastDriving.set_resultat_A(context, LEVEL_t.LEVEL_1, interm_4[0]);
                    StatsLastDriving.set_resultat_A(context, LEVEL_t.LEVEL_2, interm_4[1]);
                    StatsLastDriving.set_resultat_A(context, LEVEL_t.LEVEL_3, interm_4[2]);
                    StatsLastDriving.set_resultat_A(context, LEVEL_t.LEVEL_4, interm_4[3]);
                    StatsLastDriving.set_resultat_A(context, LEVEL_t.LEVEL_5, interm_4[4]);
                } else if ("F".equals(type)) {
                    StatsLastDriving.set_resultat_F(context, LEVEL_t.LEVEL_1, interm_4[0]);
                    StatsLastDriving.set_resultat_F(context, LEVEL_t.LEVEL_2, interm_4[1]);
                    StatsLastDriving.set_resultat_F(context, LEVEL_t.LEVEL_3, interm_4[2]);
                    StatsLastDriving.set_resultat_F(context, LEVEL_t.LEVEL_4, interm_4[3]);
                    StatsLastDriving.set_resultat_F(context, LEVEL_t.LEVEL_5, interm_4[4]);
                } else if ("V".equals(type)) {
                    StatsLastDriving.set_resultat_V(context, LEVEL_t.LEVEL_1, interm_4[0]);
                    StatsLastDriving.set_resultat_V(context, LEVEL_t.LEVEL_2, interm_4[1]);
                    StatsLastDriving.set_resultat_V(context, LEVEL_t.LEVEL_3, interm_4[2]);
                    StatsLastDriving.set_resultat_V(context, LEVEL_t.LEVEL_4, interm_4[3]);
                    StatsLastDriving.set_resultat_V(context, LEVEL_t.LEVEL_5, interm_4[4]);
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
        Context context = weakReference.get();
        if( context != null ) {
            float Coeff_General_A = DataDOBJ.get_coefficient_general(context, "A");
            float Coeff_General_F = DataDOBJ.get_coefficient_general(context, "F");
            float Coeff_General_V = DataDOBJ.get_coefficient_general(context, "V");
            return (((Note_A * Coeff_General_A) + (Note_F * Coeff_General_F)) + (Note_V * Coeff_General_V)) / ((Coeff_General_A + Coeff_General_F) + Coeff_General_V);
        }
        return 0.0f;
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

            Context context = weakReference.get();
            if( context != null ) {
                StatsLastDriving.set_note(context, SCORE_t.FINAL, parcour_note);
            }
            Log.d(TAG, "Parcours " + this.parcour_id + " note: " + parcour_note);

            AppManagerListener listener = listenerWeakReference.get();
            if (listener != null) {
                listener.onNoteChanged((int) parcour_note, parcour_level, last_5_days_level);
                listener.onLevelNotified(parcour_level);
            }
        }
    }

    private void update_force_note(boolean force) {
        if (force || this.note_forces_update_at + 60000 < System.currentTimeMillis()) {
            this.note_forces_update_at = System.currentTimeMillis();
            float Note_A = calc_note_by_force_type("A", this.parcour_id);
            //a--------
            a = note_to_score(Note_A);
            //------
            LEVEL_t level_A = note2level(Note_A);
            float Note_F = calc_note_by_force_type("F", this.parcour_id);
            //f--------
            f = note_to_score(Note_F);
            //------
            LEVEL_t level_F = note2level(Note_F);
            float Note_V = calc_note_by_force_type("V", this.parcour_id);
            //v--------
            v = note_to_score(Note_V);
            //------
            LEVEL_t level_V = note2level(Note_V);
            float Note_M = ((Note_A + Note_F) + Note_V) * 0.33333334f;
            //v--------
            m = note_to_score(Note_M);
            //------

            ColorCEP.getInstance().addColors(note_to_score(Note_A), note_to_score(Note_V), note_to_score(Note_F), note_to_score(Note_M));
            LEVEL_t level_M = note2level(Note_M);
            Context context = weakReference.get();
            AppManagerListener listener = listenerWeakReference.get();
            if( context != null ) {
                StatsLastDriving.set_note(context, SCORE_t.ACCELERATING, Note_A);
                StatsLastDriving.set_note(context, SCORE_t.BRAKING, Note_F);
                StatsLastDriving.set_note(context, SCORE_t.CORNERING, Note_V);
                Log.d(TAG, "Parcours " + this.parcour_id + " note A: " + Note_A);
                Log.d(TAG, "Parcours " + this.parcour_id + " note F: " + Note_F);
                Log.d(TAG, "Parcours " + this.parcour_id + " note V: " + Note_V);
                Log.d(TAG, "Parcours " + this.parcour_id + " note M: " + Note_M);

                StatsLastDriving.set_speed_avg(context, this.database.speed_avg(this.parcour_id, System.currentTimeMillis(), 0.0f, new int[0]));
                if (listener != null && this.stress.getMinutes() >= 10) {
                    listener.onScoreChanged(SCORE_t.ACCELERATING, level_A);
                    listener.onScoreChanged(SCORE_t.BRAKING, level_F);
                    listener.onScoreChanged(SCORE_t.CORNERING, level_V);
                    listener.onScoreChanged(SCORE_t.AVERAGE, level_M);
                }
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
        Context context = weakReference.get();
        if (context != null && (force || this.recommended_speed_update_at + 240000 < System.currentTimeMillis()) && this.readerEPCFile != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            long delay_sec = (long) (sp.getInt(context.getResources().getString(R.string.recommended_speed_time_key), 30) * 60);
            long max_delay_sec = (long) (sp.getInt(context.getResources().getString(R.string.stop_trigger_time_key), NNTPReply.NO_CURRENT_ARTICLE_SELECTED) * 60);
            speed_H = this.database.speed_max_test(this.parcour_id, delay_sec, 50, max_delay_sec, this.readerEPCFile.getForceSeuil(0).IDAlert, this.readerEPCFile.getForceSeuil(1).IDAlert, this.readerEPCFile.getForceSeuil(5).IDAlert, this.readerEPCFile.getForceSeuil(6).IDAlert);
            speed_V = this.database.speed_max_test(this.parcour_id, delay_sec, 50, max_delay_sec, this.readerEPCFile.getForceSeuil(10).IDAlert, this.readerEPCFile.getForceSeuil(11).IDAlert, this.readerEPCFile.getForceSeuil(15).IDAlert, this.readerEPCFile.getForceSeuil(16).IDAlert);
            this.speed_max = this.database.speed_max_test(this.parcour_id, delay_sec, 50, max_delay_sec, this.readerEPCFile.get_all_alertID());
            this.recommended_speed_update_at = System.currentTimeMillis();
        }
        AppManagerListener listener = listenerWeakReference.get();
        if (listener != null) {
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
            listener.onRecommendedSpeedChanged(SPEED_t.IN_STRAIGHT_LINE, (int) (speed_H * 3.6f), level_H, speed_H <= speed_observed ? DEBUG : false);
            listener.onLevelNotified(level_H);
            listener.onRecommendedSpeedChanged(SPEED_t.IN_CORNERS, (int) (speed_V * 3.6f), level_V, speed_V <= speed_observed ? DEBUG : false);
            listener.onLevelNotified(level_V);
            listener.onSpeedLineKept((int) (speed_H * 3.6f), level_H);
            listener.onSpeedCornerKept((int) (speed_V * 3.6f), level_V);
        }
    }

    /// ============================================================================================
    /// SHOCK
    /// ============================================================================================

    /// Check shock
    private void check_shock() {
        AppManagerListener listener = listenerWeakReference.get();
        Context context = weakReference.get();
        if (listener != null && context != null ) {
            if (interval(0.0d, ((Long) this.shock.first).doubleValue()) > ((double) PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getResources().getString(R.string.shock_trigger_mG_key), 1000))) {
                listener.onShock(((Long) this.shock.first).doubleValue(), ((Short) this.shock.second).shortValue());
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
        long ret = this.database.get_last_parcours_id();
        Log.w("Ret", String.valueOf(ret));

        Context context = weakReference.get();
        if( context != null ) {
            long delay = (long) ((PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getResources().getString(R.string.stop_trigger_time_key), NNTPReply.NO_CURRENT_ARTICLE_SELECTED) * 60) * 1000);
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
        return ret;
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
        Context context = weakReference.get();
        if( context != null ) {
            StatsLastDriving.startDriving(context, this.parcour_id);
        }
        update_parcour_note(DEBUG);
        update_force_note(DEBUG);
        update_recommended_speed(DEBUG);
        return DEBUG;
    }

    public boolean close_parcours(boolean force) throws InterruptedException {
        String reasons = " FORCED";
        boolean stop = force;
        Context context = weakReference.get();
        if (!stop && context != null ) {
            long delay_stop = (long) ((PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getResources().getString(R.string.stop_trigger_time_key), NNTPReply.NO_CURRENT_ARTICLE_SELECTED) * 60) * 1000);
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
        // upload_cep();
        this.database.addECA(this.parcour_id, ECALine.newInstance(255, get_last_location(), null));
        if( context != null )
            StatsLastDriving.set_distance(context, this.database.get_distance(this.parcour_id));
        clear_force_ui();

        // à connaitre, car pas evident en 2 fois
        // ceci est vide
        upload_cep();
        upload_shared_pos();
        upload_parcours_type();
        this.parcour_id = -1;
        this.button_stop = false;
        return DEBUG;
    }

    public void setStopped() {
        this.button_stop = true;
    }
    public void setStopped(boolean quit) {
        this.button_stop = true;
        this.quit = true;
    }

    public void setStarted() {
        this.button_start = DEBUG;
    }

    private STATUS_t on_stopped() throws InterruptedException {
        boolean ready_to_started = DEBUG;
        STATUS_t ret = STATUS_t.PAR_STOPPED;

        // on efface les force qui traine
        clear_force_ui();

        boolean m = this.mov_t_last == MOVING_t.STP,
                l = this.mov_t_last == MOVING_t.UNKNOW,
                e = this.engine_t != ENGINE_t.ON;

        // this.mov_t_last == MOVING_t.STP
        // if( Build.VERSION.SDK_INT != Build.VERSION_CODES.N && this.mov_t_last == MOVING_t.STP )
           //  ready_to_started = false;

        if (this.modules.getNumberOfBoxConnected() < 1 || this.mov_t_last == MOVING_t.UNKNOW || this.engine_t != ENGINE_t.ON) {
            ready_to_started = false;
        }

        Log.v("Ready Started" , String.valueOf(ready_to_started));

        // ready_to_started = true;
        // MainActivity.instance().Alert("Ready_to_started: " + (ready_to_started ? "oui" : "non"), Toast.LENGTH_LONG);

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
                    AppManagerListener listener = listenerWeakReference.get();
                    if (listener != null) {
                        listener.onForceChanged(FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW, 0.0d, 0.0f, 0.0f);
                        listener.onForceDisplayed(0.0d);
                        listener.onSpeedLine();
                        listener.onSpeedCorner();
                        listener.onStatusChanged(ret);
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
        Context context = weakReference.get();
        if (status == STATUS_t.PAR_PAUSING && context != null ) {
            if (this.database.parcour_expired(this.parcour_id, (long) ((PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getResources().getString(R.string.pause_trigger_time_key), 4) * 60) * 1000))) {
                ret = STATUS_t.PAR_PAUSING_WITH_STOP;
                AppManagerListener listener = listenerWeakReference.get();
                if (listener != null) {
                    listener.onStatusChanged(ret);
                }
                addLog("Status change to PAUSE (show button stop). (" + ComonUtils.currentDateTime() + ")");
            }
        }
        boolean cp = close_parcours(false);
        AppManagerListener listener = listenerWeakReference.get();
        if (cp) {
            ret = STATUS_t.PAR_STOPPED;
            if (listener != null) {
                listener.onStatusChanged(ret);
            }
            addLog("Status change to STOP. (" + ComonUtils.currentDateTime() + ")");
        } else if (this.mov_t_last != MOVING_t.STP && this.mov_t_last_chrono.getSeconds() > 3.0d && this.engine_t == ENGINE_t.ON) {
            ret = STATUS_t.PAR_RESUME;
            if (listener != null) {
                listener.onStatusChanged(ret);
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
            AppManagerListener listener = listenerWeakReference.get();
            if (listener != null) {
                listener.onStatusChanged(ret);
            }
            clear_force_ui();
            addLog("Status change to PAUSE. (" + ComonUtils.currentDateTime() + ")");
        }
        return ret;
    }

    /// ============================================================================================
    /// LOCATIONS
    /// ============================================================================================

    public void setLocation( Location location ) {

        // Clear obselete location and limit list size
        clear_obselete_location();

        if( location != null ) {

            lock.lock();

            // Add new location
            this.locations.add(0, new Location(location) );

            lock.unlock();

            switchON( true );
        }
    }

    public void setGpsStatus( boolean active ) {
        lock.lock();
        gps = active;
        lock.unlock();
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
        AppManagerListener listener = listenerWeakReference.get();
        if (listener != null) {
            listener.onDebugLog(this.log);
        }
    }

    private void addLog(String txt) {
        if (!this.log.isEmpty()) {
            this.log += System.getProperty("line.separator");
        }
        this.log += txt;
        AppManagerListener listener = listenerWeakReference.get();
        if (listener != null) {
            listener.onDebugLog(this.log);
        }
    }
}
