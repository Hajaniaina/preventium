package com.preventium.boxpreventium.manager;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.preventium.boxpreventium.server.CFG.DataCFG;
import com.preventium.boxpreventium.server.CFG.ReaderCFGFile;
import com.preventium.boxpreventium.server.ECA.ECALine;
import com.preventium.boxpreventium.server.EPC.DataEPC;
import com.preventium.boxpreventium.server.EPC.ForceSeuil;
import com.preventium.boxpreventium.server.EPC.ReaderEPCFile;
import com.preventium.boxpreventium.server.DOBJ.DataDOBJ;
import com.preventium.boxpreventium.server.DOBJ.ReaderDOBJFile;
import com.preventium.boxpreventium.server.POSS.ReaderPOSSFile;
import com.preventium.boxpreventium.utils.Chrono;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.Connectivity;
import com.preventium.boxpreventium.utils.ThreadDefault;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPClientIO;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Franck on 23/09/2016.
 */

public class AppManager extends ThreadDefault
        implements HandlerBox.NotifyListener{

    private final static String TAG = "AppManager";
    private final static boolean DEBUG = true;
    private static final float MS_TO_KMH = 3.6f;
    private static final int SECS_TO_SET_PARCOURS_START = 3;
    private static final int SECS_TO_SET_PARCOURS_PAUSE = 20; // 4 minutes = 4 * 60 secs = 240 secs
    private static final int SECS_TO_SET_PARCOURS_RESUME = SECS_TO_SET_PARCOURS_START;
    private static final int SECS_TO_SET_PARCOURS_STOPPED = 25200; // 7 hours = 7 * 3600 secs = 25200 secs

    public interface AppManagerListener {
        void onNumberOfBoxChanged( int nb );
        void onDrivingTimeChanged(String txt );
        void onForceChanged( FORCE_t type, LEVEL_t level );
        void onDebugLog(String txt);
        void onStatusChanged(STATUS_t status);
        void onCustomMarkerDataListGet();
        void onSharedPositionsChanged(List<CustomMarkerData> list);
        void onUiTimeout(int timer_id, STATUS_t status);

        void onNoteChanged( int note_par, LEVEL_t level_par, LEVEL_t level_5_days );
        void onScoreChanged(SCORE_t type, LEVEL_t level);
        void onShock(double mG, short raw);
        void onRecommendedSpeedChanged(SPEED_t speed_t, int kmh, LEVEL_t level, boolean valid);
        void onInternetConnectionChanged();

        void onCalibrateOnConstantSpeed();
        void onCalibrateOnAcceleration();
        void onCalibrateRAZ();
    }

    /// ============================================================================================
    /// AppManager
    /// ============================================================================================

    private ENGINE_t engine_t = ENGINE_t.UNKNOW;
    private long engine_t_changed_at = 0;
    private Context ctx = null;
    private AppManagerListener listener = null;
    private HandlerBox modules = null;
    private Database database = null;

    public AppManager(Context ctx, AppManagerListener listener) {
        super(null);
        this.ctx = ctx;
        this.listener = listener;
        this.modules = new HandlerBox(ctx,this);
        this.database = new Database(ctx);
        this.fileSender = new FilesSender(ctx);
    }

    private void switchON( boolean on ){
        if( on ) {
            if (!isRunning()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppManager.this.run();
                    }
                }).start();
            }
        } else {
            setStop();
        }
    }

    public void raz_calibration(){
        if( modules != null ) {
             modules.on_raz_calibration();
        }
    }

    @Override
    public void myRun() throws InterruptedException {
        super.myRun();
        setLog("");

        database.clear_obselete_data();

        download_cfg();
        IMEI_is_actif();
        download_shared_pos();
        download_epc();
        download_dobj();
        modules.setActive( true );

        STATUS_t status = first_init();
        upload_eca(true);

        while( isRunning() ) {
            check_internet_is_active();
            update_tracking_status();

            modules.setActive( true );
            sleep(500);
            database.clear_obselete_data();
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

            listen_timers( status );
        }
        modules.setActive(false);

    }

    // HANDLER BOX

    @Override
    public void onScanState(boolean scanning) {
        if( DEBUG ) Log.d(TAG,"Searching preventium box is enable: " + scanning );
    }

    @Override
    public void onDeviceState(String device_mac, boolean connected) {
        if( connected ) addLog( "Device " + device_mac + " connected.");
        else  addLog( "Device " + device_mac + " disconnected.");

        Location location = get_last_location();
        database.addCEP( location, device_mac, connected );
    }

    @Override
    public void onNumberOfBox(int nb) {
        if( DEBUG ) Log.d(TAG,"Number of preventium device connected changed: " + nb );
        if( listener != null ) listener.onNumberOfBoxChanged( nb );
        //if( nb <= 0 ) engine_t = ENGINE_t.OFF;
    }

    @Override
    public void onForceChanged(Pair<Double, Short> smooth, Pair<Double, Short> shock) {
        this.smooth = smooth;
        this.shock = shock;
    }

    @Override
    synchronized public void onEngineStateChanged(ENGINE_t state) {
        this.engine_t = state;
        engine_t_changed_at = System.currentTimeMillis();
    }

    @Override
    public void onCalibrateOnConstantSpeed() {
        if( listener != null ) listener.onCalibrateOnConstantSpeed();
    }

    @Override
    public void onCalibrateOnAcceleration() {
        if( listener != null ) listener.onCalibrateOnAcceleration();
    }

    @Override
    public void onCalibrateRAZ() {
        onForceChanged( Pair.create(0.0,(short)0),Pair.create(0.0,(short)0));
        if( listener != null ) listener.onCalibrateRAZ();
    }

    // PRIVATE

    /// ============================================================================================
    /// UI
    /// ============================================================================================

    private String chronoRideTxt = "";

    private STATUS_t first_init() throws InterruptedException {
        button_stop = false;
        internet_active = true;
        mov_t_last = MOVING_t.UNKNOW;
        mov_t = MOVING_t.UNKNOW;
        onEngineStateChanged(ENGINE_t.OFF);
        chronoRideTxt = "0:00";

        if( listener != null ){
            // For update UI correctly
            listener.onDebugLog("");
            listener.onStatusChanged( STATUS_t.PAR_PAUSING );
            listener.onStatusChanged( STATUS_t.PAR_STOPPED );
            listener.onDrivingTimeChanged(chronoRideTxt);
            listener.onNoteChanged(20,LEVEL_t.LEVEL_1,LEVEL_t.LEVEL_1);
            listener.onScoreChanged(SCORE_t.ACCELERATING,LEVEL_t.LEVEL_1);
            listener.onScoreChanged(SCORE_t.BRAKING,LEVEL_t.LEVEL_1);
            listener.onScoreChanged(SCORE_t.CORNERING,LEVEL_t.LEVEL_1);
            listener.onScoreChanged(SCORE_t.AVERAGE,LEVEL_t.LEVEL_1);
        }
        alertX_add_at = 0;
        alertY_add_at = 0;
        alertPos_add_at = 0;
        lastLocSend = null;
        try_send_eca_at  = 0;
        parcour_id = get_current_parcours_id(true);

        STATUS_t ret = STATUS_t.PAR_STOPPED;
        if( parcour_id > 0 ) {
            loading_epc( database.get_num_epc(parcour_id) );
            update_parcour_note(true);
            update_force_note(true);
            update_recommended_speed(true);
            ret = STATUS_t.PAR_PAUSING;
            addLog( "Phone launch: Resume parcours with status PAUSE.");
            addLog( "Status change to PAUSE. (" + ComonUtils.currentDateTime() + ")" );
        } else {
            loading_epc();
            addLog( "Phone launch: No parcours.");
            addLog( "Status change to STOP. (" + ComonUtils.currentDateTime() + ")" );
        }

        if (listener != null) listener.onStatusChanged(ret);

        return ret;
    }

    private void update_driving_time() {
        if( listener != null ) {
            long id = get_current_parcours_id(false);
            long ms = (id > 0) ? System.currentTimeMillis() - id : 0;
            String txt = String.format(Locale.getDefault(), "%02d:%02d", 0, 0);
            if (ms > 0) {
                long h = ms / 3600000;
                long m = (ms % 3600000) > 0 ? (ms % 3600000) / 60000 : 0;
                txt = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                StatsLastDriving.set_times(ctx, (long) (ms * 0.001));
            }
            if( !chronoRideTxt.equals(txt) ) {
                listener.onDrivingTimeChanged( txt );
                chronoRideTxt = txt;
            }
        }
    }

    private void clear_force_ui(){
        if( seuil_ui != null
                && seuil_chrono_x.getSeconds() > 3 && seuil_chrono_y.getSeconds() > 3 ){
            if( listener != null ) listener.onForceChanged( FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW );
            seuil_ui = null;
        }
    }

    /// ============================================================================================
    /// Driver ID
    /// ============================================================================================

    private long driver_id = 0;

    public long get_driver_id(){ return driver_id; }

    public void set_driver_id( long driver_id ){ this.driver_id = driver_id; }

    /// ============================================================================================
    /// UI Timers
    /// ============================================================================================

    // Timers list -> Long: timestamp, Integer: timer id
    private List<Pair<Long,Integer>> ui_timers = new ArrayList<>();
    private final Lock lock_timers = new ReentrantLock();

    /// Add timer to timer list
    public void add_ui_timer(long secs, int timer_id){
        long timestamp = System.currentTimeMillis() + (secs*1000);
        lock_timers.lock();
        ui_timers.add( Pair.create(timestamp,timer_id) );
        lock_timers.unlock();
    }

    /// Remove all timers
    public void clear_ui_timer(){
        lock_timers.lock();
        ui_timers.clear();
        lock_timers.unlock();
    }

    /// Listening timeout
    synchronized private void listen_timers(STATUS_t status){
        long timestamp = System.currentTimeMillis();
        lock_timers.lock();
        if( !ui_timers.isEmpty() ) {
            Pair<Long, Integer> timer;
            long timeout_at;
            int timer_id;
            for (int i = ui_timers.size() - 1; i >= 0; i--) {
                timer = ui_timers.get(i);
                timeout_at = timer.first;
                timer_id = timer.second;
                if( timestamp >= timeout_at ){
                    if( listener != null ) listener.onUiTimeout( timer_id, status );
                    ui_timers.remove(i);
                }
            }
        }
        lock_timers.unlock();
    }

    /// ============================================================================================
    /// INTERNET CONNECTION
    /// ============================================================================================

    private boolean internet_active = true;

    private void check_internet_is_active(){
        boolean active = Connectivity.isConnected(ctx);
        if( active != internet_active ) {
            internet_active = active;
            if( listener != null ) listener.onInternetConnectionChanged();
        }
    }

    /// ============================================================================================
    /// .CHECK IF ACTIF
    /// ============================================================================================

    private boolean IMEI_is_actif() {
        boolean exist_actif = false;

        if( listener != null ) listener.onStatusChanged( STATUS_t.CHECK_ACTIF );

        FTPConfig config = DataCFG.getFptConfig(ctx);
        FTPClientIO ftp = new FTPClientIO();

        do {
            // Trying to connect to FTP server...
            if( !ftp.ftpConnect(config, 5000) ) {
                check_internet_is_active();
            } else {
                exist_actif = false;
                if (!ftp.changeWorkingDirectory("/ACTIFS")) {
                    Log.d(TAG, "Error while trying to change working directory to \"/ACTIFS\"");
                } else {
                    // Checking if .ACTIVE file is in FTP server ?
                    String srcFileName = ComonUtils.getIMEInumber(ctx);
                    exist_actif = ftp.checkFileExists(srcFileName);
                    if( !exist_actif ) {
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                        if( listener != null ) listener.onStatusChanged( STATUS_t.IMEI_INACTIF );
                    }
                }
            }

        } while (!exist_actif && isRunning() );


        return exist_actif;
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
        //FTPConfig config = new FTPConfig("ftp.ikalogic.com","ikalogic","Tecteca1",21);
        FTPConfig config = new FTPConfig("www.preventium.fr","box.preventium","Box*16/64/prev",21);
        FTPClientIO ftp = new FTPClientIO();

        while ( isRunning() && !cfg  ){
            if( listener != null )listener.onStatusChanged( STATUS_t.GETTING_CFG );

            // Trying to connect to FTP server...
            if( !ftp.ftpConnect(config, 5000) ) {
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
                                cfg = reader.read(desFileName);
                                if (cfg) {
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
                                    new File(desFileName).delete();
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
        FTPConfig config = DataCFG.getFptConfig(ctx);
        FTPClientIO ftp = new FTPClientIO();

        while( isRunning() && !ready ) {
            if( listener != null ) listener.onStatusChanged( STATUS_t.GETTING_EPC );

            // Trying to connect to FTP server...
            if( !ftp.ftpConnect(config, 5000) ) {
                check_internet_is_active();
            } else {

                // Changing working directory if needed
                boolean change_directory = true;
                if (!config.getWorkDirectory().isEmpty() && !config.getWorkDirectory().equals("/"))
                    change_directory = ftp.changeWorkingDirectory(config.getWorkDirectory());

                if( !change_directory ) {
                    Log.w(TAG, "Error while trying to change working directory!");
                } else {
                    boolean epc;
                    boolean exist_server_epc = false;
                    boolean exist_server_ack = false;
                    String srcFileName = "";
                    String srcAckName = "";
                    String desFileName = "";
                    int i = 1;
                    while( i <= 5 && isRunning() ) {

                        // Checking if .EPC file is in FTP server ?
                        srcFileName = reader.getEPCFileName(ctx, i, false);
                        srcAckName = reader.getEPCFileName(ctx, i, true);
                        exist_server_epc = ftp.checkFileExists( srcFileName );
                        exist_server_ack = ftp.checkFileExists( srcAckName );

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

    // Load EPC file
    private boolean loading_epc() throws InterruptedException {
        boolean ready = false;

        while( isRunning() && !ready ) {
            selected_epc = 0;
            ready = readerEPCFile.loadFromApp(ctx);

            if( ready )
                selected_epc = readerEPCFile.selectedEPC(ctx);
            else {
                List<Integer> available = DataEPC.getAppEpcExist(ctx);
                for (Integer i : available) {
                    ready = readerEPCFile.loadFromApp(ctx, i);
                    if( ready ){
                        selected_epc = i;
                        break;
                    }
                }
            }
            if( !ready ) download_epc();
        }
        return ready;
    }

    private boolean loading_epc(int num) throws InterruptedException {
        boolean ready = false;
        if( num < 1 ) num = 1;
        if( num > 5 ) num = 5;
        while( isRunning() && !ready ) {
            ready = readerEPCFile.loadFromApp(ctx, num);
            if( !ready ) download_epc();
        }
        selected_epc = ( ready ) ? num : 0;
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
        FTPConfig config = DataCFG.getFptConfig(ctx);
        FTPClientIO ftp = new FTPClientIO();

        while( isRunning() && !ready ) {
            if( listener != null ) listener.onStatusChanged( STATUS_t.GETTING_DOBJ );

            // Trying to connect to FTP server...
            if( !ftp.ftpConnect(config, 5000) ) {
                check_internet_is_active();
            } else {

                // Changing working directory if needed
                boolean change_directory = true;
                if (!config.getWorkDirectory().isEmpty() && !config.getWorkDirectory().equals("/"))
                    change_directory = ftp.changeWorkingDirectory(config.getWorkDirectory());

                if( !change_directory ) {
                    Log.w(TAG, "Error while trying to change working directory!");
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

    private FilesSender fileSender = null;
    private long try_send_eca_at  = 0;

    /// Uploading .ECA file if is needed
    private boolean upload_eca( boolean now ){
        boolean ret = false;

        // If now is true or elapsed time > 1 minutes
        if( now
                || try_send_eca_at + 60000 < System.currentTimeMillis() ){

            // Update driver id table if necessary
            database.add_driver(parcour_id,driver_id);

            // Trying to send file
            fileSender.startThread();
            try_send_eca_at = System.currentTimeMillis();
            ret = true;
        }
        return ret;
    }

    /// ============================================================================================
    /// .CEP
    /// ============================================================================================

    /// Create .CEP file (Connections Events of Preventium's devices) and uploading to the server.
    private void upload_cep(){
        if( isRunning() ) {
            if (listener != null) listener.onStatusChanged(STATUS_t.SETTING_CEP);

            if (parcour_id > 0) {
                database.create_cep_file(parcour_id);
                database.clear_cep_data();
            }

            // UPLOAD .CEP FILES
            File folder = new File(ctx.getFilesDir(), "CEP");
            if (folder.exists()) {
                File[] listOfFiles = folder.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toUpperCase().endsWith(".CEP");
                    }
                });
                if (listOfFiles != null && listOfFiles.length > 0) {
                    FTPConfig config = DataCFG.getFptConfig(ctx);
                    FTPClientIO ftp = new FTPClientIO();
                    if (config != null && ftp.ftpConnect(config, 5000)) {
                        boolean change_directory = true;
                        if (!config.getWorkDirectory().isEmpty() && !config.getWorkDirectory().equals("/"))
                            change_directory = ftp.changeWorkingDirectory(config.getWorkDirectory());
                        if (!change_directory) {
                            Log.w(TAG, "Error while trying to change working directory!");
                        } else {
                            for (File file : listOfFiles) {
                                if (ftp.ftpUpload(file.getAbsolutePath(), file.getName())) {
                                    file.delete();
                                }
                            }
                        }
                    }
                }
            }

            if (listener != null) listener.onStatusChanged(STATUS_t.PAR_STOPPED);
        }
    }

    /// ============================================================================================
    /// .POS (Map markers)
    /// ============================================================================================

    private boolean customMarkerList_Received = false;
    private ArrayList<CustomMarkerData> customMarkerList = null;

    // Set list of map markers
    public void setCustomMarkerDataList(ArrayList<CustomMarkerData> list){
        customMarkerList = list;
        customMarkerList_Received = true;
    }

    // Create .POS files (Position of map markers) and uploading to the server.
    private void upload_custom_markers() throws InterruptedException {

        if( isRunning() ) {
            if (listener != null) {
                customMarkerList_Received = false;
                customMarkerList = null;
                listener.onCustomMarkerDataListGet();
                Chrono chrono = Chrono.newInstance();
                chrono.start();
                while (isRunning() && chrono.getSeconds() < 10 && !customMarkerList_Received) {
                    sleep(500);
                }
                listener.onStatusChanged(STATUS_t.SETTING_MARKERS);
                if (customMarkerList_Received) {
                    if (parcour_id > 0 && customMarkerList != null && customMarkerList.size() > 0) {
                        // CREATE FILE
                        File folder = new File(ctx.getFilesDir(), "POS");
                        // Create folder if not exist
                        if (!folder.exists())
                            if (!folder.mkdirs())
                                Log.w(TAG, "Error while trying to create new folder!");
                        if (folder.exists()) {
                            String filename = String.format(Locale.getDefault(), "%s_%s.POS",
                                    ComonUtils.getIMEInumber(ctx), parcour_id);
                            File file = new File(folder.getAbsolutePath(), filename);
                            try {
                                if (file.createNewFile()) {
                                    FileWriter fileWriter = new FileWriter(file);
                                    String line = "";
                                    for (CustomMarkerData mk : customMarkerList) {
                                        line = String.format(Locale.getDefault(),
                                                "%f;%f;%d;%d;%d;%s;%d;\n",
                                                mk.position.longitude,
                                                mk.position.latitude,
                                                mk.type,
                                                (mk.alert ? 1 : 0),
                                                mk.alertRadius,
                                                mk.title,
                                                (mk.shared ? 1 : 0) );
                                        fileWriter.write(line);
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
                }

                // UPLOAD .POS FILES
                File folder = new File(ctx.getFilesDir(), "POS");
                if (folder.exists()) {
                    File[] listOfFiles = folder.listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.toUpperCase().endsWith(".POS");
                        }
                    });
                    if (listOfFiles != null && listOfFiles.length > 0) {
                        FTPConfig config = DataCFG.getFptConfig(ctx);
                        FTPClientIO ftp = new FTPClientIO();
                        if (config != null && ftp.ftpConnect(config, 5000)) {
                            boolean change_directory = true;
                            if (!config.getWorkDirectory().isEmpty() && !config.getWorkDirectory().equals("/"))
                                change_directory = ftp.changeWorkingDirectory(config.getWorkDirectory());
                            if (!change_directory) {
                                Log.w(TAG, "Error while trying to change working directory!");
                            } else {
                                for (File file : listOfFiles) {
                                    if (ftp.ftpUpload(file.getAbsolutePath(), file.getName())) {
                                        file.delete();
                                    }
                                }
                            }
                        }
                    }
                }

                listener.onStatusChanged(STATUS_t.PAR_STOPPED);
            }
        }
    }

    // Dowloading shared positions
    private boolean download_shared_pos()  throws InterruptedException {
        boolean ready = false;

        if( listener != null ) listener.onStatusChanged( STATUS_t.GETTING_MARKERS_SHARED );

        File folder = new File(ctx.getFilesDir(), "");
        FTPConfig config = DataCFG.getFptConfig(ctx);
        FTPClientIO ftp = new FTPClientIO();

        while( isRunning() && !ready ) {
            if (listener != null) listener.onStatusChanged(STATUS_t.GETTING_DOBJ);

            // Trying to connect to FTP server...
            if( !ftp.ftpConnect(config, 5000) ) {
                check_internet_is_active();
            } else {
                // Changing working directory if needed
                boolean change_directory = true;
                if (!config.getWorkDirectory().isEmpty() && !config.getWorkDirectory().equals("/"))
                    change_directory = ftp.changeWorkingDirectory(config.getWorkDirectory());

                if( !change_directory ) {
                    Log.w(TAG, "Error while trying to change working directory!");
                } else {

                    boolean poss = false;

                    // Checking if .POSS file is in FTP server ?
                    String srcFileName = ReaderPOSSFile.getFileName(ctx, false);
                    String srcAckName = ReaderPOSSFile.getFileName(ctx, true);
                    boolean exist_server_poss = ftp.checkFileExists( srcFileName );
                    boolean exist_server_ack = ftp.checkFileExists( srcAckName );

                    // If .POSS file exist in the FTP server
                    poss = ( exist_server_ack && ReaderPOSSFile.existLocalFile(ctx) );
                    if( !poss ) {
                        if( exist_server_poss ) {
                            // Create folder if not exist
                            if (!folder.exists())
                                if (!folder.mkdirs())
                                    Log.w(TAG, "Error while trying to create new folder!");
                            // Trying to download .POSS file...
                            String desFileName = String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), srcFileName);
                            if (ftp.ftpDownload(srcFileName, desFileName))
                            {
                                List<CustomMarkerData> list = ReaderPOSSFile.readFile(desFileName);
                                poss = (list != null);
                                if( poss )
                                {
                                    ready = true;
                                    // envoi acknowledge
                                    try {
                                        File temp = File.createTempFile("temp-file-name", ".tmp");
                                        ftp.ftpUpload(temp.getPath(), srcAckName);
                                        temp.delete();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    new File(desFileName).delete();

                                    if( listener != null ) {
                                        listener.onSharedPositionsChanged(list);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Disconnect from FTP server.
            ftp.ftpDisconnect();

            if( isRunning() && !ready ) sleep(1000);
        }
        return ready;
    }

    /// ============================================================================================
    /// PARCOURS TYPE
    /// ============================================================================================

    private String parcoursTypeName = null;

    // Set current parcours is a parcours type,
    // if parcoursName is null, do not set this parcours is a parcours type
    public void set_parcours_type(@Nullable  String parcoursName ){
        if( parcoursName == null ) parcoursName = "";
        parcoursTypeName = parcoursName;
    }

    // Create .PT file (Parcours type) and uploading to the server.
    private void upload_parcours_type() throws InterruptedException {

        if( listener != null ){

            parcoursTypeName = null;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
            String key = ctx.getResources().getString(R.string.parcours_type_enabled_key);
            if( sp.getBoolean(key,false) ){
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(key,false);
                editor.apply();
                parcoursTypeName = sp.getString(key,"");
            }

            listener.onStatusChanged(STATUS_t.SETTING_PARCOUR_TYPE);

            if( parcoursTypeName != null ){
                if( parcour_id > 0 && !parcoursTypeName.isEmpty() ){

                    // CREATE FILE
                    File folder = new File(ctx.getFilesDir(), "PT");
                    // Create folder if not exist
                    if (!folder.exists())
                        if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
                    if( folder.exists() ) {
                        String filename = String.format(Locale.getDefault(), "%s_%d.PT",
                                ComonUtils.getIMEInumber(ctx), parcour_id);
                        File file = new File(folder.getAbsolutePath(), filename );
                        try {
                            if( file.createNewFile() ){
                                FileWriter fileWriter = new FileWriter(file);
                                fileWriter.write( String.format(Locale.getDefault(),
                                        "%s;%d;%s",
                                        ComonUtils.getIMEInumber(ctx), parcour_id, parcoursTypeName) );
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
            }

            // UPLOAD .PT FILES
            File folder = new File(ctx.getFilesDir(), "PT");
            if ( folder.exists() ){
                File[] listOfFiles = folder.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toUpperCase().endsWith(".PT");
                    }
                });

                if( listOfFiles != null && listOfFiles.length > 0 ){
                    FTPConfig config = DataCFG.getFptConfig(ctx);
                    FTPClientIO ftp = new FTPClientIO();
                    if( config != null && ftp.ftpConnect(config, 5000) ) {
                        boolean change_directory = true;
                        if (!config.getWorkDirectory().isEmpty() && !config.getWorkDirectory().equals("/"))
                            change_directory = ftp.changeWorkingDirectory(config.getWorkDirectory());
                        if (!change_directory) {
                            Log.w(TAG, "Error while trying to change working directory!");
                        } else {
                            for ( File file : listOfFiles ) {
                                if( ftp.ftpUpload(file.getAbsolutePath(),file.getName()) ){
                                    file.delete();
                                }
                            }
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

    private boolean _tracking = true;

    private void update_tracking_status(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String key = ctx.getResources().getString(R.string.tracking_activated_key);
        _tracking = sp.getBoolean(key,true);
    }

    /// ============================================================================================
    /// CALCUL
    /// ============================================================================================

    private MOVING_t mov_t = MOVING_t.STP;
    private MOVING_t mov_t_last = MOVING_t.UNKNOW;
    private long alertX_add_at = 0;
    private long alertY_add_at = 0;
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

    private void calc_movements(){

        this.mov_t = MOVING_t.UNKNOW;
        this.XmG = 0f;
        boolean rightRoad = false;

        List<Location> list = get_location_list(3,5000);
//if( list != null ) Log.d("AAAA","sss" + list.size()); else Log.d("AAAA","sss null");
        if( list != null && list.size() >= 3 ){

            int i = list.size()-1;

            rightRoad = isRightRoad( list.get(i-2), list.get(i-1), list.get(i) );
            boolean acceleration = true;
            boolean freinage = true;
            float speed_min = list.get(0).getSpeed();
            float speed_max = list.get(0).getSpeed();
            for (i = 0; i < list.size(); i++) {// i is more recent than (i+1)
                // Calculate minimum and maximum value
                if (list.get(i).getSpeed() < speed_min) speed_min = list.get(i).getSpeed();
                if (list.get(i).getSpeed() > speed_max) speed_max = list.get(i).getSpeed();
                // Checking acceleration and braking
                if (i < list.size() - 1) {
                    if (list.get(i).getSpeed() <= list.get(i + 1).getSpeed())acceleration = false;
                    if (list.get(i).getSpeed() >= list.get(i + 1).getSpeed())freinage = false;
                }
            }

            if (speed_max * MS_TO_KMH <= 3f) mov_t = MOVING_t.STP;
            else if ((speed_max - speed_min) * MS_TO_KMH < 2f) mov_t = MOVING_t.CST;
            else if (acceleration) mov_t = MOVING_t.ACC;
            else if (freinage) mov_t = MOVING_t.BRK;
            else mov_t = MOVING_t.NCS;
        }

        if ( mov_t != mov_t_last )
        {
            mov_t_last_chrono.start();
            mov_chrono.start();
            mov_t_last = mov_t;
        }
        else {
            if ( mov_chrono.isStarted() ) {
                switch (mov_t_last) {
                    case UNKNOW:
                        break;
                    case STP:
                        break;
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
                    case CST:
                        break;
                    case NCS:
                        break;
                }
            }
        }
    }

    private double LocationsToXmG( @NonNull Location l0, @NonNull Location l1 ) {
        // Pour calculer l'accélération longitudinale (accélération ou freinage) avec comme unité le mG :
        // il faut connaître : la vitesse (v(t)) à l'instant t et à l'instant précédent(v(t-1)) et le delta t entre ces deux mesures.
        // a = ( v(t) - v(t-1) )/(9.81*( t - (t-1) ) )
        return ((l0.getSpeed() - l1.getSpeed())
                / (9.81 * ((l0.getTime() - l1.getTime()) * 0.001)))
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

    synchronized private void calc_eca(){

// TEST
List<Location> list_loc = get_location_list(5,5000);
List<Double> list_XmG = new ArrayList<>();
this.XmG = 0.0;
if(list_loc != null && list_loc.size() >= 2){
    this.XmG = LocationsToXmG( list_loc.get(0), list_loc.get(1) );
    for (int i = 0; i < list_loc.size()-1; i++)
        list_XmG.add(i, LocationsToXmG( list_loc.get(i), list_loc.get(i+1) ) );

    if( list_XmG.size() >= 2 ){
        // Compare difference between the current and the prevent XmG
        double diff = Math.abs( list_XmG.get(0) - list_XmG.get(1) );

        if( diff <= 10.0 ) this.XmG = list_XmG.get(0);
    }
}

// TEST

        Location loc = get_last_location();

        LEVEL_t alertX = LEVEL_t.LEVEL_UNKNOW;
        LEVEL_t alertY = LEVEL_t.LEVEL_UNKNOW;
        // Read the runtime value force
        ForceSeuil seuil_x = readerEPCFile.getForceSeuilForX(XmG);
        ForceSeuil seuil_y = readerEPCFile.getForceSeuilForY(smooth.first);
        if( loc == null || (loc.getSpeed() * MS_TO_KMH) < 20.0 ) {
            seuil_x = null;
            seuil_y = null;
        }

        // Compare the runtime X value force with the prevent X value force, and add alert to ECA database
        if( seuil_x != null ) {
            alertX = seuil_x.level;
            if( !seuil_x.equals(seuil_last_x) ) seuil_chrono_x.start();
            if( seuil_chrono_x.getSeconds() >= seuil_x.TPS ) {
                seuil_chrono_x.start();
//                // If elapsed time > 2 seconds
//                if(System.currentTimeMillis() - alertX_add_at >= 500) {
//                    if( _tracking ) database.addECA( parcour_id, ECALine.newInstance(seuil_x.IDAlert, loc, null ) );
//                    alertX_add_at = System.currentTimeMillis();
//                }
                if( _tracking ) {
                    database.addECA(parcour_id, ECALine.newInstance(seuil_last_x.IDAlert, loc, null));
                    alertX_add_at = System.currentTimeMillis();
                    lastLocSend = loc;
                }
            }
        }
        seuil_last_x = seuil_x;

        // Compare the runtime Y value force with the prevent Y value force, and add alert to ECA database
        if( seuil_y != null ) {
            alertY = seuil_y.level;
            if( !seuil_y.equals(seuil_last_y) )seuil_chrono_y.start();
            if( seuil_chrono_y.getSeconds() >= seuil_y.TPS ) {
                seuil_chrono_y.start();
//                // If elapsed time > 2 seconds
//                if(System.currentTimeMillis() - alertY_add_at >= 500) {
//                    //if( _tracking ) database.addECA( parcour_id, ECALine.newInstance(seuil_y.IDAlert, loc, null ) );
//                    alertY_add_at = System.currentTimeMillis();
//                }
                if( _tracking ) {
                    database.addECA(parcour_id, ECALine.newInstance(seuil_last_y.IDAlert, loc, null));
                    alertY_add_at = System.currentTimeMillis();
                    lastLocSend = loc;
                }
            }
        }
        seuil_last_y = seuil_y;

        // Add location to ECA database
//        if( _tracking &&
//                alertX == LEVEL_t.LEVEL_UNKNOW
//                && alertY == LEVEL_t.LEVEL_UNKNOW  ){
        if( _tracking ) {

            List<Location> locations = get_location_list(1);
            if (locations != null && locations.size() >= 1) {
                float min_meters = ( (locations.get(0).getSpeed()*MS_TO_KMH) < 70f ) ? 5f : 15f;
                if( lastLocSend == null
                        || locations.get(0).distanceTo(lastLocSend) > min_meters ) {

                    if( lastLocSend == null ) lastLocSend = new Location(locations.get(0));
                    database.addECA(parcour_id, ECALine.newInstance(locations.get(0), lastLocSend));
                    lastLocSend = new Location(locations.get(0));
                }
            }
        }

        // Update ui interface
        ForceSeuil seuil = null;
        if( alertX.getValue() > alertY.getValue() ) {
            seuil = seuil_x;
        } else {
            seuil = seuil_y;
        }


// TEST
if( seuil != null
        && (seuil.type == FORCE_t.ACCELERATION || seuil.type == FORCE_t.BRAKING) ) {
//    String txt = (seuil.type == FORCE_t.ACCELERATION) ? "ACC " : "BRK ";
//    switch ( seuil.level ) {
//        case LEVEL_UNKNOW: txt += "LVL_? "; break;
//        case LEVEL_1: txt += "LVL_1 "; break;
//        case LEVEL_2: txt += "LVL_2 "; break;
//        case LEVEL_3: txt += "LVL_3 "; break;
//        case LEVEL_4: txt += "LVL_4 "; break;
//        case LEVEL_5: txt += "LVL_5 "; break;
//    }
    String txt = "";
    double sum = 0.0;
    for( int i = 0; i < list_XmG.size(); i++ ) {
        sum += list_XmG.get(i);
        txt += (int)( sum/(i+1) ) + "; ";
    }
    setLog( txt );
}
// TEST
        boolean change = false;
        if( (seuil_ui == null) != (seuil == null) ) change = true;
        if( seuil_ui != null  ) change = !seuil_ui.equals( seuil );
        if( seuil != null  ) change = !seuil.equals( seuil_ui );

        if( change ) {
            if( seuil != null ) {

                int t = 0;
                if( seuil_ui != null ) {
                    switch ( seuil_ui.level ) {
                        case LEVEL_UNKNOW: t = 0; break;
                        case LEVEL_1: t = 1000; break;
                        case LEVEL_2: t = 2000; break;
                        case LEVEL_3: t = 2000; break;
                        case LEVEL_4: t = 3000; break;
                        case LEVEL_5: t = 3000; break;
                    }
                }
                if( seuil_ui == null
                        || seuil.level.getValue() >= seuil_ui.level.getValue()
                        || alertUI_add_at + t < System.currentTimeMillis() ){
                    if( listener != null )listener.onForceChanged(seuil.type, seuil.level);

                    alertUI_add_at = System.currentTimeMillis();
                    seuil_ui = seuil;
                }
//                if( listener != null )listener.onForceChanged(seuil.type, seuil.level);
//                alertUI_add_at = System.currentTimeMillis();
//                seuil_ui = seuil;
            }
            else if( alertUI_add_at + 3000 < System.currentTimeMillis() ) {
                if( listener != null ) listener.onForceChanged(FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW);
                alertUI_add_at = System.currentTimeMillis();
                seuil_ui = null;
            }
        } else {
            alertUI_add_at = System.currentTimeMillis();
        }
    }

    /// ============================================================================================
    /// CALCUL COTATION
    /// ============================================================================================

    private long note_parcour_update_at = 0;
    private long note_forces_update_at = 0;

    private float calc_note_by_force_type( String type, long parcour_id, long begin, long end ) {
        float ret = 20f;
        if( !DataDOBJ.ACCELERATIONS.equals(type)
                && !DataDOBJ.FREINAGES.equals(type)
                && !DataDOBJ.VIRAGES.equals(type) ) return ret;

        // COEFFICIENTS
        float coeff_general = DataDOBJ.get_coefficient_general(ctx, type);
        int[] coeff_force = {
                DataDOBJ.get_coefficient(ctx, type, DataDOBJ.VERT),
                DataDOBJ.get_coefficient(ctx, type, DataDOBJ.BLEU),
                DataDOBJ.get_coefficient(ctx, type, DataDOBJ.JAUNE),
                DataDOBJ.get_coefficient(ctx, type, DataDOBJ.ORANGE),
                DataDOBJ.get_coefficient(ctx, type, DataDOBJ.ROUGE)
        };

        // NUNBER OF EVENEMENTS
        int[] nb_evt = new int[5];
        if( DataDOBJ.ACCELERATIONS.equals(type) ){
            nb_evt[0] = database.countNbEvent(readerEPCFile.getForceSeuil(0).IDAlert, parcour_id, begin, end);
            nb_evt[1] = database.countNbEvent(readerEPCFile.getForceSeuil(1).IDAlert, parcour_id, begin, end);
            nb_evt[2] = database.countNbEvent(readerEPCFile.getForceSeuil(2).IDAlert, parcour_id, begin, end);
            nb_evt[3] = database.countNbEvent(readerEPCFile.getForceSeuil(3).IDAlert, parcour_id, begin, end);
            nb_evt[4] = database.countNbEvent(readerEPCFile.getForceSeuil(4).IDAlert, parcour_id, begin, end);
        } else if( DataDOBJ.FREINAGES.equals(type) ){
            nb_evt[0] = database.countNbEvent(readerEPCFile.getForceSeuil(5).IDAlert, parcour_id, begin, end);
            nb_evt[1] = database.countNbEvent(readerEPCFile.getForceSeuil(6).IDAlert, parcour_id, begin, end);
            nb_evt[2] = database.countNbEvent(readerEPCFile.getForceSeuil(7).IDAlert, parcour_id, begin, end);
            nb_evt[3] = database.countNbEvent(readerEPCFile.getForceSeuil(8).IDAlert, parcour_id, begin, end);
            nb_evt[4] = database.countNbEvent(readerEPCFile.getForceSeuil(9).IDAlert, parcour_id, begin, end);
        } else {//if( DataDOBJ.VIRAGES.equals(type) ){
            nb_evt[0] = database.countNbEvent(readerEPCFile.getForceSeuil(10).IDAlert, parcour_id, begin, end)
                    + database.countNbEvent(readerEPCFile.getForceSeuil(15).IDAlert, parcour_id, begin, end);
            nb_evt[1] = database.countNbEvent(readerEPCFile.getForceSeuil(11).IDAlert, parcour_id, begin, end)
                    + database.countNbEvent(readerEPCFile.getForceSeuil(16).IDAlert, parcour_id, begin, end);
            nb_evt[2] = database.countNbEvent(readerEPCFile.getForceSeuil(12).IDAlert, parcour_id, begin, end)
                    + database.countNbEvent(readerEPCFile.getForceSeuil(17).IDAlert, parcour_id, begin, end);
            nb_evt[3] = database.countNbEvent(readerEPCFile.getForceSeuil(13).IDAlert, parcour_id, begin, end)
                    + database.countNbEvent(readerEPCFile.getForceSeuil(18).IDAlert, parcour_id, begin, end);
            nb_evt[4] = database.countNbEvent(readerEPCFile.getForceSeuil(14).IDAlert, parcour_id, begin, end)
                    + database.countNbEvent(readerEPCFile.getForceSeuil(19).IDAlert, parcour_id, begin, end);
        }

//        float coeff_general = 1;
//        int[] coeff_force = {1,1,1,1,1};
//        int[] nb_evt = {8,5,1,0,0};
        int evt_sum = nb_evt[0] + nb_evt[1] + nb_evt[2] + nb_evt[3] + nb_evt[4];
        if( evt_sum <= 0 ) {
            ret = 20f;
        } else {

            // CALCUL
            int coeff_sum = coeff_force[0] + coeff_force[1] + coeff_force[2] + coeff_force[3] + coeff_force[4];
            float coeff_percent = (float) (coeff_sum * 0.01);
            float[] interm_1 = {
                    ( coeff_sum * coeff_force[0] ) / ( 100f * coeff_percent * coeff_general ),
                    ( coeff_sum * coeff_force[1] ) / ( 100f * coeff_percent * coeff_general ),
                    ( coeff_sum * coeff_force[2] ) / ( 100f * coeff_percent * coeff_general ),
                    ( coeff_sum * coeff_force[3] ) / ( 100f * coeff_percent * coeff_general ),
                    ( coeff_sum * coeff_force[4] ) / ( 100f * coeff_percent * coeff_general )
            };

            float coeff_evt = (float) (evt_sum * 0.01);
            float[] interm_2 = {
                    (evt_sum * nb_evt[0]) / (100f * coeff_evt * coeff_evt),
                    (evt_sum * nb_evt[1]) / (100f * coeff_evt * coeff_evt),
                    (evt_sum * nb_evt[2]) / (100f * coeff_evt * coeff_evt),
                    (evt_sum * nb_evt[3]) / (100f * coeff_evt * coeff_evt),
                    (evt_sum * nb_evt[4]) / (100f * coeff_evt * coeff_evt)
            };

            float[] interm_3 = {
                    interm_1[0] * interm_2[0],
                    interm_1[1] * interm_2[1],
                    interm_1[2] * interm_2[2],
                    interm_1[3] * interm_2[3],
                    interm_1[4] * interm_2[4]
            };

            float interm_3_sum = interm_3[0] + interm_3[1] + interm_3[2] + interm_3[3] + interm_3[4];

            float[] interm_4 = {
                    interm_3[0] / (interm_3_sum * 0.01f),
                    interm_3[1] / (interm_3_sum * 0.01f),
                    interm_3[2] / (interm_3_sum * 0.01f),
                    interm_3[3] / (interm_3_sum * 0.01f),
                    interm_3[4] / (interm_3_sum * 0.01f)
            };

            ret = (interm_4[0] + interm_4[1] - interm_4[2] - interm_4[3] - interm_4[4]) * (1f / 5f);

            if (ret < 0f) ret = 0f;
            if (ret > 20f) ret = 20f;

            // Update statictics data
            if( StatsLastDriving.get_start_at(ctx) == parcour_id ) {

                if( DataDOBJ.ACCELERATIONS.equals(type) ) {
                    StatsLastDriving.set_resultat_A(ctx, LEVEL_t.LEVEL_1, (int) interm_1[0]);
                    StatsLastDriving.set_resultat_A(ctx, LEVEL_t.LEVEL_2, (int) interm_1[1]);
                    StatsLastDriving.set_resultat_A(ctx, LEVEL_t.LEVEL_3, (int) interm_1[2]);
                    StatsLastDriving.set_resultat_A(ctx, LEVEL_t.LEVEL_4, (int) interm_1[3]);
                    StatsLastDriving.set_resultat_A(ctx, LEVEL_t.LEVEL_5, (int) interm_1[4]);
                } else if( DataDOBJ.FREINAGES.equals(type) ) {
                    StatsLastDriving.set_resultat_F(ctx, LEVEL_t.LEVEL_1, (int) interm_1[0]);
                    StatsLastDriving.set_resultat_F(ctx, LEVEL_t.LEVEL_2, (int) interm_1[1]);
                    StatsLastDriving.set_resultat_F(ctx, LEVEL_t.LEVEL_3, (int) interm_1[2]);
                    StatsLastDriving.set_resultat_F(ctx, LEVEL_t.LEVEL_4, (int) interm_1[3]);
                    StatsLastDriving.set_resultat_F(ctx, LEVEL_t.LEVEL_5, (int) interm_1[4]);
                } else if( DataDOBJ.VIRAGES.equals(type) ) {
                    StatsLastDriving.set_resultat_V(ctx, LEVEL_t.LEVEL_1, (int) interm_1[0]);
                    StatsLastDriving.set_resultat_V(ctx, LEVEL_t.LEVEL_2, (int) interm_1[1]);
                    StatsLastDriving.set_resultat_V(ctx, LEVEL_t.LEVEL_3, (int) interm_1[2]);
                    StatsLastDriving.set_resultat_V(ctx, LEVEL_t.LEVEL_4, (int) interm_1[3]);
                    StatsLastDriving.set_resultat_V(ctx, LEVEL_t.LEVEL_5, (int) interm_1[4]);
                }
            }
        }
        return ret;
    }

    private float calc_note_by_force_type( String type, long parcour_id ) {
        long begin = 0;
        long end = System.currentTimeMillis() + 3600;
        return calc_note_by_force_type( type, parcour_id, begin, end );
    }

    private float calc_note( long parcour_id, long begin, long end ) {
        float Note_A = calc_note_by_force_type(DataDOBJ.ACCELERATIONS, parcour_id, begin, end);
        float Note_F = calc_note_by_force_type(DataDOBJ.FREINAGES,parcour_id, begin, end);
        float Note_V = calc_note_by_force_type(DataDOBJ.VIRAGES,parcour_id, begin, end);
        float Coeff_General_A = DataDOBJ.get_coefficient_general(ctx, DataDOBJ.ACCELERATIONS);
        float Coeff_General_F = DataDOBJ.get_coefficient_general(ctx, DataDOBJ.FREINAGES);
        float Coeff_General_V = DataDOBJ.get_coefficient_general(ctx, DataDOBJ.VIRAGES);
        return ( (Note_A*Coeff_General_A) + (Note_F*Coeff_General_F) + (Note_V*Coeff_General_V) )
                / ( Coeff_General_A + Coeff_General_F + Coeff_General_V );
    }

    private float calc_note( long parcour_id ) {
        long begin = 0;
        long end = System.currentTimeMillis() + 3600;
        return calc_note( parcour_id, begin, end );
    }

    private void update_parcour_note(boolean force) {
        // Every 3 minutes
        if( force || note_parcour_update_at + (3 * 60 * 1000) < System.currentTimeMillis() )
        {
            note_parcour_update_at = System.currentTimeMillis();

            float parcour_note = calc_note(parcour_id);

            LEVEL_t parcour_level = LEVEL_t.LEVEL_5;
            if( parcour_note >= 16f ) parcour_level = LEVEL_t.LEVEL_1;
            else if( parcour_note >= 13f ) parcour_level = LEVEL_t.LEVEL_2;
            else if( parcour_note >= 9f ) parcour_level = LEVEL_t.LEVEL_3;
            else if( parcour_note >= 6f ) parcour_level = LEVEL_t.LEVEL_4;

            long end = startOfDays(System.currentTimeMillis());
            long begin = end - (5 * 24 * 3600 * 1000);
            float last_5_days_note = calc_note( -1, begin, end );

            LEVEL_t last_5_days_level = LEVEL_t.LEVEL_5;
            if( last_5_days_note >= 16f ) last_5_days_level = LEVEL_t.LEVEL_1;
            else if( last_5_days_note >= 13f ) last_5_days_level = LEVEL_t.LEVEL_2;
            else if( last_5_days_note >= 9f ) last_5_days_level = LEVEL_t.LEVEL_3;
            else if( last_5_days_note >= 6f ) last_5_days_level = LEVEL_t.LEVEL_4;

            StatsLastDriving.set_note(ctx,SCORE_t.FINAL,parcour_note);
            Log.d(TAG,"Parcours " + parcour_id + " note: " + parcour_note );
            if( listener != null ) listener.onNoteChanged( (int)parcour_note, parcour_level, last_5_days_level );
        }
    }

    private void update_force_note(boolean force) {

        // Every 1 minutes
        if( force || note_forces_update_at + (60 * 1000) < System.currentTimeMillis() ) {

            note_forces_update_at = System.currentTimeMillis();

            float Note_A = calc_note_by_force_type(DataDOBJ.ACCELERATIONS, parcour_id);
            LEVEL_t level_A = note2level( Note_A );

            float Note_F = calc_note_by_force_type(DataDOBJ.FREINAGES, parcour_id);
            LEVEL_t level_F = note2level( Note_F );

            float Note_V = calc_note_by_force_type(DataDOBJ.VIRAGES, parcour_id);
            LEVEL_t level_V = note2level( Note_V );

            float Note_M = (Note_A + Note_F + Note_V) * (1f / 3f);
            LEVEL_t level_M = note2level( Note_M );

            StatsLastDriving.set_note(ctx,SCORE_t.ACCELERATING,Note_A);
            StatsLastDriving.set_note(ctx,SCORE_t.BRAKING,Note_F);
            StatsLastDriving.set_note(ctx,SCORE_t.CORNERING,Note_V);

            Log.d(TAG,"Parcours " + parcour_id + " note A: " + Note_A );
            Log.d(TAG,"Parcours " + parcour_id + " note F: " + Note_F );
            Log.d(TAG,"Parcours " + parcour_id + " note V: " + Note_V );
            Log.d(TAG,"Parcours " + parcour_id + " note M: " + Note_M );

            float speed_avg = database.speed_avg(parcour_id, System.currentTimeMillis(), 0f);
            StatsLastDriving.set_speed_avg(ctx,speed_avg);

            if (listener != null) {
                listener.onScoreChanged(SCORE_t.ACCELERATING, level_A);
                listener.onScoreChanged(SCORE_t.BRAKING, level_F);
                listener.onScoreChanged(SCORE_t.CORNERING, level_V);
                listener.onScoreChanged(SCORE_t.AVERAGE, level_M);
            }
        }

    }

    private LEVEL_t note2level(float note) {
        LEVEL_t level = LEVEL_t.LEVEL_5;
        if (note >= 16f) level = LEVEL_t.LEVEL_1;
        else if (note >= 13f) level = LEVEL_t.LEVEL_2;
        else if (note >= 9f) level = LEVEL_t.LEVEL_3;
        else if (note >= 6f) level = LEVEL_t.LEVEL_4;
        return level;
    }

    private long startOfDays(long timestamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis( timestamp );
        cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        cal.set(Calendar.MINUTE, 0); // set minutes to zero
        cal.set(Calendar.SECOND, 0); //set seconds to zero
        return cal.getTimeInMillis();
    }

    /// ============================================================================================
    /// CALCUL RECOMMENDED SPEED
    /// ============================================================================================

    private long recommended_speed_update_at = 0;
    private float speed_H = 0f;
    private float speed_V = 0f;
    private float speed_max = 0f;

    /// Calculate recommended speed
    private void update_recommended_speed(boolean force) {

        // Calculate recommended val and get speed maximum since XX secondes
        // 10 minutes
        if( force || recommended_speed_update_at + (600*1000) < System.currentTimeMillis()) {
            if (readerEPCFile != null) {

                // Period pour calucl: (utiliser les X derniere secondes)
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
                String key = ctx.getResources().getString(R.string.recommended_speed_time_key);
                long delay_sec = sp.getInt(key,30) * 60;

                // Get the horizontal maximum speed since
                speed_H = database.speed_max(parcour_id, delay_sec,
                        readerEPCFile.getForceSeuil(0).IDAlert, // IDAlert +X1
                        readerEPCFile.getForceSeuil(1).IDAlert, // IDAlert +X2
                        readerEPCFile.getForceSeuil(5).IDAlert, // IDAlert -X1
                        readerEPCFile.getForceSeuil(6).IDAlert  // IDAlert -X2
                );
                // Get the vertical maximum speed since
                speed_V = database.speed_max(parcour_id, delay_sec,
                        readerEPCFile.getForceSeuil(10).IDAlert,    // IDAlert +Y1
                        readerEPCFile.getForceSeuil(11).IDAlert,    // IDAlert +Y2
                        readerEPCFile.getForceSeuil(15).IDAlert,    // IDAlert -Y1
                        readerEPCFile.getForceSeuil(16).IDAlert     // IDAlert -Y2
                );
                // Get the max speed
                speed_max = database.speed_max(parcour_id, delay_sec);
                // Set calculate at
                recommended_speed_update_at = System.currentTimeMillis();
            }
        }
        // Update the UI
        if( listener != null ) {
            // Get the horizontal level
            LEVEL_t level_H = LEVEL_t.LEVEL_UNKNOW;
            if( speed_H > 0f ) {
                if (speed_max < speed_H) level_H = LEVEL_t.LEVEL_1;
                else if (speed_max < speed_H * 1.1) level_H = LEVEL_t.LEVEL_2;
                else if (speed_max < speed_H * 1.2) level_H = LEVEL_t.LEVEL_3;
                else if (speed_max < speed_H * 1.35) level_H = LEVEL_t.LEVEL_4;
                else level_H = LEVEL_t.LEVEL_5;
            }
            // Get the vertical level
            LEVEL_t level_V = LEVEL_t.LEVEL_UNKNOW;
            if( speed_V > 0f ) {
                if (speed_max < speed_V) level_V = LEVEL_t.LEVEL_1;
                else if (speed_max < speed_V * 1.1) level_V = LEVEL_t.LEVEL_2;
                else if (speed_max < speed_V * 1.2) level_V = LEVEL_t.LEVEL_3;
                else if (speed_max < speed_V * 1.35) level_V = LEVEL_t.LEVEL_4;
                else level_V = LEVEL_t.LEVEL_5;
            }
            Location location = get_last_location();
            float speed_observed = ( location != null ) ?  location.getSpeed() : 0f;
            // Send results to UI
            listener.onRecommendedSpeedChanged( SPEED_t.IN_STRAIGHT_LINE, (int)(speed_H*MS_TO_KMH),
                    level_H, speed_H <= speed_observed);
            listener.onRecommendedSpeedChanged( SPEED_t.IN_CORNERS, (int)(speed_V*MS_TO_KMH),
                    level_V, speed_V <= speed_observed);
        }
    }

    /// ============================================================================================
    /// SHOCK
    /// ============================================================================================

    /// Check shock
    private void check_shock() {
        if( listener != null ) {

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
            String key = ctx.getResources().getString(R.string.shock_trigger_mG_key);
            int val = sp.getInt(key,1000);
            if( interval(0.0, shock.first) > val ) {
                listener.onShock( shock.first, shock.second );
            }
        }
    }

    private double interval(double d1, double d2){
        double ret = d1 - d2;
        if( ret < 0.0 ) ret = -ret;
        return ret;
    }

    /// ============================================================================================
    /// PARCOUR
    /// ============================================================================================

    private Chrono chrono_ready_to_start = Chrono.newInstance();
    private long parcour_id = 0;
    private boolean button_stop = false;

    public long get_current_parcours_id(boolean debug) {

        long ret = -1;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String key = ctx.getResources().getString(R.string.stop_trigger_time_key);
        long delay = sp.getInt(key,420) * 60 * 1000;

        ret = database.get_last_parcours_id();

        if( ret == -1 ) {
            if( debug ) Log.d(TAG, "Current parcours ID: empty. ");
        } else if( database.parcour_is_closed(ret) ) {
            if( debug ) Log.d(TAG, "Current parcours ID: " + ret + " is closed.");
            ret = -1;
        } else if( database.parcour_expired(ret,delay) ) {
            if( debug ) Log.d(TAG, "Current parcours ID: " + ret + " expired.");
            database.close_last_parcour();
            ret = -1;
        } else {
            if( debug ) Log.d(TAG, "Current parcours ID: " + ret + "." );
        }

        return ret;
    }

    public boolean init_parcours_id() throws InterruptedException {
        long id = get_current_parcours_id(true);
        if( id > 0 )
        {

            parcour_id = id;
            if( !loading_epc( database.get_num_epc(parcour_id) ) ) return false;
            Log.d(TAG,"Initialize parcours ( Resume parcours " + parcour_id + " )" );
            Log.d(TAG,"Add ECA to resume parcous.");
            Location loc = get_last_location();
            database.addECA(parcour_id, ECALine.newInstance(ECALine.ID_RESUME, loc, null));

        }
        else
        {
            parcour_id = System.currentTimeMillis();
            if( !loading_epc() ) return false;
            database.set_num_epc(parcour_id, selected_epc);

            Log.d(TAG, "Initialize parcours: Create parcours " + parcour_id + " )");
            Log.d(TAG, "Add ECA to start parcous.");
            Location loc = get_last_location();
            database.addECA(parcour_id, ECALine.newInstance(ECALine.ID_BEGIN, loc, null));

        }
        StatsLastDriving.startDriving(ctx,parcour_id);
        update_parcour_note(true);
        update_force_note(true);
        update_recommended_speed(true);

        return true;
    }

    public boolean close_parcours(boolean force) throws InterruptedException {

        boolean ret = false;

        String reasons = " FORCED";
        boolean stop = force;
        if( !stop ) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
            String key = ctx.getResources().getString(R.string.stop_trigger_time_key);
            long delay_stop = sp.getInt(key, 420) * 60 * 1000;

            // Checking if car is stopped
            boolean is_closed = database.parcour_is_closed(parcour_id);
            boolean has_expired  = database.parcour_expired(parcour_id,delay_stop);
            stop = (button_stop || is_closed || has_expired);

            reasons = " ";
            if( button_stop ) reasons += "Btn stp pressed; ";
            if( is_closed ) reasons += "Already closed; ";
            if( has_expired ) reasons += "Has expired; ";

        }

        if( stop ) {

            addLog( "Close parcours reasons:" + reasons );

            // Force calcul note
            update_parcour_note(true);
            update_force_note(true);
            update_recommended_speed(true);
            // ADD ECA Line when stopped
            Location loc = get_last_location();
            database.addECA(parcour_id, ECALine.newInstance(ECALine.ID_END, loc, null));
            StatsLastDriving.set_distance(ctx, database.get_distance(parcour_id));
            clear_force_ui();
            // Sending files
            upload_cep();
            upload_custom_markers();
            upload_parcours_type();
            parcour_id = -1;

            ret = true;

        }

        return ret;
    }

    public void setStopped(){ button_stop = true; }

    private STATUS_t on_stopped() throws InterruptedException {
        STATUS_t ret = STATUS_t.PAR_STOPPED;

        // Clear UI
        clear_force_ui();

        // Checking if ready to start a new parcours
        boolean ready_to_started = (modules.getNumberOfBoxConnected() >= 1
                && mov_t_last != MOVING_t.STP
                && mov_t_last != MOVING_t.UNKNOW
                && engine_t == ENGINE_t.ON
        );

        if ( !ready_to_started ) {
            chrono_ready_to_start.stop();
        } else {

            if ( !chrono_ready_to_start.isStarted() ) chrono_ready_to_start.start();
            if ( chrono_ready_to_start.getSeconds() > SECS_TO_SET_PARCOURS_START ) {

                button_stop = false;
                note_parcour_update_at = 0;
                note_forces_update_at = 0;
                alertX_add_at = 0;
                alertY_add_at = 0;
                alertPos_add_at = 0;
                lastLocSend = null;
                recommended_speed_update_at = 0;
                download_shared_pos();

                if( init_parcours_id() ) {
                    ret = STATUS_t.PAR_STARTED;
                    if (listener != null) {
                        listener.onForceChanged(FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW);
                        listener.onRecommendedSpeedChanged(SPEED_t.IN_STRAIGHT_LINE, 0, LEVEL_t.LEVEL_UNKNOW, true);
                        listener.onRecommendedSpeedChanged(SPEED_t.IN_CORNERS, 0, LEVEL_t.LEVEL_UNKNOW, true);
                        listener.onStatusChanged(ret);
                    }
                    addLog("Status change to START. (" + ComonUtils.currentDateTime() + ")");
                }
            }
        }
        return ret;
    }

    private STATUS_t on_paused(STATUS_t status) throws InterruptedException {

        STATUS_t ret = status;

        if( status == STATUS_t.PAR_PAUSING ) {

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
            String key = ctx.getResources().getString(R.string.pause_trigger_time_key);
            long delay_pause = sp.getInt(key,4) * 60 * 1000;
            if( database.parcour_expired(parcour_id,delay_pause) ) {
                ret = STATUS_t.PAR_PAUSING_WITH_STOP;
                if (listener != null) listener.onStatusChanged(ret);
                addLog( "Status change to PAUSE (show button stop). (" + ComonUtils.currentDateTime() + ")" );
            }
        }

        // Close parcours if neccessary
        boolean stop = close_parcours(false);
        if( stop ) {
            ret = STATUS_t.PAR_STOPPED;
            if (listener != null) listener.onStatusChanged(ret);
            addLog( "Status change to STOP. (" + ComonUtils.currentDateTime() + ")" );
        }
        // Or checking if car re-moving
        else if ( mov_t_last != MOVING_t.STP
                && mov_t_last_chrono.getSeconds() > SECS_TO_SET_PARCOURS_RESUME
                && engine_t == ENGINE_t.ON) {
            ret = STATUS_t.PAR_RESUME;
            if (listener != null) listener.onStatusChanged(ret);

            // ADD ECA Line when resuming
            Location loc = get_last_location();
            database.addECA(parcour_id, ECALine.newInstance(ECALine.ID_RESUME, loc, null));

            clear_force_ui();

            addLog( "Status change to RESUME. (" + ComonUtils.currentDateTime() + ")" );
        }

        return ret;
    }

    private STATUS_t on_moved(STATUS_t status){

        STATUS_t ret = status;

        calc_eca();
        update_parcour_note(false);
        update_force_note(false);
        check_shock();
        update_recommended_speed(false);

        // Checking if car is in pause
        if ( engine_t != ENGINE_t.ON ) {

            ret = STATUS_t.PAR_PAUSING;

            // ADD ECA Line when pausing
            Location loc = get_last_location();
            database.addECA(parcour_id, ECALine.newInstance(ECALine.ID_PAUSE, loc, null));
            if (listener != null) listener.onStatusChanged(ret);

            clear_force_ui();

            addLog( "Status change to PAUSE. (" + ComonUtils.currentDateTime() + ")" );
        }

        return ret;
    }

    /// ============================================================================================
    /// LOCATIONS
    /// ============================================================================================

    private final Lock lock = new ReentrantLock();

    private List<Location> locations = new ArrayList<Location>();
    private boolean gps = false;

    public void setLocation( Location location ) {

        // Clear obselete location and limit list size
        clear_obselete_location();

        if( location != null ) {

            // Important, use systeme time
            location.setTime( System.currentTimeMillis() );

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

    private synchronized  void clear_obselete_location() {
        clear_location(50,15000);
    }

    private synchronized void clear_location(int max, int ms) {

        lock.lock();

        long timeMS = System.currentTimeMillis() - ms;
        int i;
        for( i = 0; i < locations.size(); i++ ) {
            if( i == max
                    || locations.get(i).getTime() < timeMS )
                break;
        }

        locations.subList(i, locations.size()).clear();

        lock.unlock();

    }

    private synchronized List<Location> get_location_list(int length){
        return  get_location_list(length,5000);
    }

    private synchronized List<Location> get_location_list(int length, int ms){

        // Clear obselete location and limit list size
        clear_obselete_location();

        List<Location> list = null;
        if( gps_is_ready() ) {

            lock.lock();

            long timeMS = System.currentTimeMillis() - ms;
            int i;
            for( i = 0; i < locations.size(); i++ ) {
                if( i == length
                        || locations.get(i).getTime() < timeMS )
                    break;
            }

//Log.d("AAAA","Number of points over the last " + ms+ " ms seconds: " + i );
            // Get sublist
            if (locations.size() >= i) {
                list = new ArrayList<Location>(this.locations.subList(0, i));
            }

            lock.unlock();
        }
        return list;
    }

    private synchronized Location get_last_location(){
        Location ret = null;
        if( gps_is_ready() ) {
            lock.lock();

            if (locations.size() > 0) ret = new Location(locations.get(0));

            lock.unlock();
        }
        return ret;
    }

    private boolean gps_is_ready() {
        boolean ret;
        lock.lock();
        ret = gps;
        lock.unlock();
        ret = true;
        return ret;
    }

    /// ============================================================================================
    /// DEBUG
    /// ============================================================================================

    private String log = "";

    private void setLog( String txt ){
        log = txt;
        if( listener != null ) listener.onDebugLog( log );
    }

    private void addLog( String txt ){
        if( !log.isEmpty() ) log += System.getProperty("line.separator");
        log += txt;
        if( listener != null ) listener.onDebugLog( log );
    }

}