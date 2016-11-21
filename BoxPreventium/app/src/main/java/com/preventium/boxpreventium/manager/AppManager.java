package com.preventium.boxpreventium.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.database.DBHelper;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Franck on 23/09/2016.
 */

public class AppManager extends ThreadDefault
        implements HandlerBox.NotifyListener{

    private final static String TAG = "AppManager";
    private final static boolean DEBUG = true;
    private static final float MS_TO_KMH = 3.6f;
    private static final int SECS_TO_SET_PARCOURS_START = 5;
    private static final int SECS_TO_SET_PARCOURS_PAUSE = 240; // 4 minutes = 4 * 60 secs = 240 secs
    private static final int SECS_TO_SET_PARCOURS_RESUME = 10;
    private static final int SECS_TO_SET_PARCOURS_STOPPED = 25200; // 7 hours = 7 * 3600 secs = 25200 secs

    public interface AppManagerListener {
        void onNumberOfBoxChanged( int nb );
        void onDrivingTimeChanged(String txt );
        void onForceChanged( FORCE_t type, LEVEL_t level );
        void onDebugLog(String txt);
        void onStatusChanged(STATUS_t status);
        void onCustomMarkerDataListGet();
        void onParcoursTypeGet();
        void onUiTimeout(int timer_id, STATUS_t status);

        void onNoteChanged( int note_par, LEVEL_t level_par, LEVEL_t level_5_days );
        void onScoreChanged(SCORE_t type, LEVEL_t level);
        void onShock();
        void onRecommendedSpeedChanged(SPEED_t speed_t, int kmh, LEVEL_t level, boolean valid);
        void onInternetConnectionChanged();
    }

    /// ============================================================================================
    /// AppManager
    /// ============================================================================================

    private ENGINE_t engine_t = ENGINE_t.UNKNOW;
    private Context ctx = null;
    private AppManagerListener listener = null;
    private HandlerBox modules = null;
    private DBHelper database = null;

    public AppManager(Context ctx, AppManagerListener listener) {
        super(null);
        this.ctx = ctx;
        this.listener = listener;
        this.modules = new HandlerBox(ctx,this);
        this.database = new DBHelper(ctx);
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


    @Override
    public void myRun() throws InterruptedException {
        super.myRun();
        setLog( "AppManager begin..." );
        modules.setActive( true );
        database.clear_obselete_data();
        download_cfg();
        download_epc();
        download_dobj();
        STATUS_t status = first_init();
        upload_eca(true);

        while( isRunning() ) {
            check_internet_is_active();

            modules.setActive( true );
            sleep(500);
            database.clear_obselete_data();
            upload_eca(false);
            change_driving_time();
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
                    status = on_paused();
                    break;
            }

            listen_timers( status );
        }
        modules.setActive(false);

        addLog( "AppManager end.");
    }

    // HANDLER BOX

    @Override
    public void onScanState(boolean scanning) {
        if( DEBUG ) Log.d(TAG,"Searching preventium box is enable: " + scanning );
    }

    @Override
    public void onDeviceState(String device_mac, boolean connected) {
        addLog( device_mac + " is connected: " + connected );
        Location location = get_last_location();
        database.addCEP( location, device_mac, connected );
    }

    @Override
    public void onNumberOfBox(int nb) {
        if( DEBUG ) Log.d(TAG,"Number of preventium device connected changed: " + nb );
        if( listener != null ) listener.onNumberOfBoxChanged( nb );
    }

    @Override
    public void onForceChanged(double mG_smooth, double mG_shock) {
        this.YmG_smooth = mG_smooth;
        this.YmG_shock = mG_shock;
    }

    @Override
    public void onEngineStateChanged(ENGINE_t state) {
        this.engine_t = state;
    }

    // PRIVATE

    /// ============================================================================================
    /// UI
    /// ============================================================================================

    private Chrono chronoRide = Chrono.newInstance();
    private String chronoRideTxt = "";

    private STATUS_t first_init(){
        button_stop = false;
        internet_active = true;
        mov_t_last = MOVING_t.UNKNOW;
        mov_t = MOVING_t.UNKNOW;
        engine_t = ENGINE_t.UNKNOW;
        chronoRideTxt = "0:00";
        chronoRide = Chrono.newInstance();
        if( listener != null ){
            // For update UI correctly
            listener.onDebugLog("");
            listener.onDrivingTimeChanged(chronoRideTxt);
            listener.onNoteChanged(20,LEVEL_t.LEVEL_UNKNOW,LEVEL_t.LEVEL_UNKNOW);
            listener.onScoreChanged(SCORE_t.ACCELERATING,LEVEL_t.LEVEL_UNKNOW);
            listener.onScoreChanged(SCORE_t.BRAKING,LEVEL_t.LEVEL_UNKNOW);
            listener.onScoreChanged(SCORE_t.CORNERING,LEVEL_t.LEVEL_UNKNOW);
            listener.onScoreChanged(SCORE_t.AVERAGE,LEVEL_t.LEVEL_UNKNOW);
            listener.onStatusChanged( STATUS_t.PAR_PAUSING);
            listener.onStatusChanged( STATUS_t.PAR_STOPPED);
        }
        parcour_id = 0;
        cotation_update_at = 0;
        forces_update_at = 0;
        alertX_add_at = 0;
        alertY_add_at = 0;
        alertPos_add_at = 0;
        try_send_eca_at  = 0;
        recommended_speed_update_at = 0;
        return STATUS_t.PAR_STOPPED;
    }

    private void change_driving_time() {
        String txt = String.format(Locale.getDefault(),"%d:%02d",(int)chronoRide.getHours(),(int)chronoRide.getMinutes());
        if( !chronoRideTxt.equals(txt) ) {
            if( listener != null ) listener.onDrivingTimeChanged( txt );
            chronoRideTxt = txt;
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

    public long get_driver_id(){ return driver_id; };

    public void set_driver_id( long driver_id ){ this.driver_id = driver_id; }

    /// ============================================================================================
    /// UI Timers
    /// ============================================================================================

    // Timers list -> Long: timestamp, Integer: timer id
    private List<Pair<Long,Integer>> ui_timers = new ArrayList<>();

    /// Add timer to timer list
    public void add_ui_timer(long secs, int timer_id){
        long timestamp = System.currentTimeMillis() + (secs*1000);
        ui_timers.add( Pair.create(timestamp,timer_id) );
    }

    /// Remove all timers
    public void clear_ui_timer(){ ui_timers.clear(); }

    /// Listening timeout
    private void listen_timers(STATUS_t status){
        long timestamp = System.currentTimeMillis();
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
    /// .CFG
    /// ============================================================================================

    // Downloading .cfg file if is needed
    private boolean download_cfg() throws InterruptedException {
        boolean cfg = false;

        if( listener != null )listener.onStatusChanged( STATUS_t.GETTING_CFG );

        File folder = new File(ctx.getFilesDir(), "");
        ReaderCFGFile reader = new ReaderCFGFile();
        FTPConfig config = new FTPConfig("ftp.ikalogic.com","ikalogic","Tecteca1",21);
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
                    change_directory = ftp.makeDirectory(config.getWorkDirectory());

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
                    change_directory = ftp.makeDirectory(config.getWorkDirectory());

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
        if( parcour_id > 0 ) {
            database.create_cep_file(parcour_id);
            database.clear_cep_data();
        }

        // UPLOAD .CEP FILES
        File folder = new File(ctx.getFilesDir(), "CEP");
        if ( folder.exists() ){
            File[] listOfFiles = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toUpperCase().endsWith(".CEP");
                }
            });

            if( listOfFiles != null && listOfFiles.length > 0 ){
                FTPConfig config = DataCFG.getFptConfig(ctx);
                FTPClientIO ftp = new FTPClientIO();
                if( config != null && ftp.ftpConnect(config, 5000) ) {
                    boolean change_directory = true;
                    if (!config.getWorkDirectory().isEmpty() && !config.getWorkDirectory().equals("/"))
                        change_directory = ftp.makeDirectory(config.getWorkDirectory());
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

        if( listener != null ){
            customMarkerList_Received = false;
            customMarkerList = null;
            listener.onCustomMarkerDataListGet();
            Chrono chrono = Chrono.newInstance();
            chrono.start();
            while( chrono.getSeconds() < 5 && !customMarkerList_Received ){
                sleep(500);
            }
            if( customMarkerList_Received ){
                if( parcour_id > 0 && customMarkerList != null && customMarkerList.size() > 0 ){
                    // CREATE FILE
                    File folder = new File(ctx.getFilesDir(), "POS");
                    // Create folder if not exist
                    if (!folder.exists())
                        if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
                    if( folder.exists() ) {
                        String filename = String.format(Locale.getDefault(),"%s_%s.POS",
                                ComonUtils.getIMEInumber(ctx), parcour_id );
                        File file = new File(folder.getAbsolutePath(), filename );
                        try {
                            if( file.createNewFile() ){
                                FileWriter fileWriter = new FileWriter(file);
                                String line = "";
                                for ( CustomMarkerData mk : customMarkerList ) {
                                    line = String.format(Locale.getDefault(),
                                            "%f;%f;%d;%d;%d;%s;\n",
                                            mk.position.longitude,
                                            mk.position.latitude,
                                            mk.type,
                                            (mk.alert ? 1 : 0),
                                            mk.perimeter,
                                            mk.title);
                                    fileWriter.write( line );
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
            if ( folder.exists() ){
                File[] listOfFiles = folder.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toUpperCase().endsWith(".POS");
                    }
                });

                if( listOfFiles != null && listOfFiles.length > 0 ){
                    FTPConfig config = DataCFG.getFptConfig(ctx);
                    FTPClientIO ftp = new FTPClientIO();
                    if( config != null && ftp.ftpConnect(config, 5000) ) {
                        boolean change_directory = true;
                        if (!config.getWorkDirectory().isEmpty() && !config.getWorkDirectory().equals("/"))
                            change_directory = ftp.makeDirectory(config.getWorkDirectory());
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
        }
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
            listener.onParcoursTypeGet();
            Chrono chrono = Chrono.newInstance();
            chrono.start();
            while( chrono.getSeconds() < 60 && parcoursTypeName == null ){
                sleep(500);
            }
            if( parcoursTypeName != null ){
                if( parcour_id > 0 && !parcoursTypeName.isEmpty() ){
                    // CREATE FILE
                    File folder = new File(ctx.getFilesDir(), "PT");
                    // Create folder if not exist
                    if (!folder.exists())
                        if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
                    if( folder.exists() ) {
                        String filename = String.format(Locale.getDefault(), "%s_%s.PT",
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
                            change_directory = ftp.makeDirectory(config.getWorkDirectory());
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
        }
    }

    /// ============================================================================================
    /// TRACKING
    /// ============================================================================================

    private boolean _tracking = true;

    public void setTracking( boolean enabled ){ _tracking = enabled; }

    public boolean getTracking(){ return _tracking; }

    /// ============================================================================================
    /// CALCUL
    /// ============================================================================================

    private MOVING_t mov_t = MOVING_t.STP;
    private MOVING_t mov_t_last = MOVING_t.UNKNOW;
    private long alertX_add_at = 0;
    private long alertY_add_at = 0;
    private long alertPos_add_at = 0;
    private Chrono mov_chrono = new Chrono();
    private Chrono mov_t_last_chrono = new Chrono();
    private ForceSeuil seuil_ui = null;
    private Chrono seuil_chrono_x = new Chrono();
    private Chrono seuil_chrono_y = new Chrono();
    private ForceSeuil seuil_last_x = null;
    private ForceSeuil seuil_last_y = null;
    private double XmG = 0.0;
    private double YmG_smooth = 0.0;
    private double YmG_shock = 0.0;

    private void calc_movements(){
        this.mov_t = MOVING_t.UNKNOW;
        this.XmG = 0f;
        boolean rightRoad = false;

        List<Location> list = get_location_list(3);
        if( list != null && list.size() >= 3 ){
            rightRoad = isRightRoad( list.get(0), list.get(1), list.get(2) );
            boolean acceleration = true;
            boolean freinage = true;
            float speed_min = 0f;
            float speed_max = 0f;
            for (int i = 0; i < list.size(); i++) {// i is more recent than (i+1)
                // Calculate minimum and maximum value
                if (list.get(i).getSpeed() < speed_min) speed_min = list.get(i).getSpeed();
                if (list.get(i).getSpeed() > speed_max) speed_max = list.get(i).getSpeed();
                // Checking acceleration and braking
                if (i < list.size() - 1) {
                    if (list.get(i).getSpeed() < list.get(i + 1).getSpeed())acceleration = false;
                    if (list.get(i).getSpeed() > list.get(i + 1).getSpeed())freinage = false;
                }
            }
            if (speed_max * MS_TO_KMH <= 3f) mov_t = MOVING_t.STP;
            else if ((speed_max - speed_min) * MS_TO_KMH < 3f) mov_t = MOVING_t.CST;
            else if (acceleration) mov_t = MOVING_t.ACC;
            else if (freinage) mov_t = MOVING_t.BRK;
            else mov_t = MOVING_t.NCS;
            // CALCULATE FORCE X
            // Pour calculer l'accélération longitudinale (accélération ou freinage) avec comme unité le mG :
            // il faut connaître : la vitesse (v(t)) à l'instant t et à l'instant précédent(v(t-1)) et le delta t entre ces deux mesures.
            // a = ( v(t) - v(t-1) )/(9.81*( t - (t-1) ) )
            this.XmG = ((list.get(0).getSpeed() - list.get(1).getSpeed())
                    / (9.81 * ((list.get(0).getTime() - list.get(1).getTime()) * 0.001)))
                    * 1000.0;
        }

        if ( mov_t != mov_t_last)
        {
            mov_t_last_chrono.start();
            mov_chrono.start();
            mov_t_last = mov_t;
            addLog("Moving status changed: " + mov_t.toString());
        }
        else {
            if ( mov_chrono.isStarted() ) {
                switch (mov_t_last) {
                    case UNKNOW:
                        break;
                    case STP:
                        if (mov_chrono.getSeconds() > 3) {
                            mov_chrono.stop();
                            addLog("Calibrate on constant speed (no moving)");
                            modules.on_constant_speed();
                        }
                        break;
                    case ACC:
                        if (rightRoad) {
                            mov_chrono.stop();
                            addLog("Calibrate on acceleration");
                            modules.on_acceleration();
                        }
                        break;
                    case BRK:
                        break;
                    case CST:
                        if (rightRoad) {
                            mov_chrono.stop();
                            addLog("Calibrate on constant speed");
                            modules.on_constant_speed();
                        }
                        break;
                    case NCS:
                        break;
                }
            }
        }
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

    private void calc_eca(){
        List<Location> loc = get_location_list(2);
        if( loc != null && loc.size() >= 2 ) {

            boolean alertX = false;
            boolean alertY = false;

            // Read the runtime value force
            ForceSeuil seuil_x = readerEPCFile.getForceSeuilForX(XmG);
            ForceSeuil seuil_y = readerEPCFile.getForceSeuilForY(YmG_smooth);
if( seuil_y != null ) Log.d("AA","SEUIL Y: " + seuil_y.toString() );
            // Compare the runtime X value force with the prevent X value force, and add alert to ECA database
            if( seuil_x != null ) {
                if( !seuil_x.equals(seuil_last_x) ) seuil_chrono_x.start();
                if( seuil_chrono_x.getSeconds() >= seuil_x.TPS ) {
                    seuil_chrono_x.start();
                    // If elapsed time > 2 seconds
                    if( System.currentTimeMillis() - alertX_add_at >= 2000 ) {
                        if( _tracking ) database.addECA(parcour_id, ECALine.newInstance(seuil_x.IDAlert, loc.get(0), null));
                        alertX_add_at = System.currentTimeMillis();
                    }
                    alertX = true;
                }
            }

            // Compare the runtime Y value force with the prevent Y value force, and add alert to ECA database
            if( seuil_y != null ) {
                if( !seuil_y.equals(seuil_last_y) ) seuil_chrono_y.start();
                if( seuil_chrono_y.getSeconds() >= seuil_y.TPS ) {
                    seuil_chrono_y.start();
                    // If elapsed time > 2 seconds
                    if( System.currentTimeMillis() - alertY_add_at >= 2000 ) {
                        if( _tracking ) database.addECA( parcour_id, ECALine.newInstance(seuil_y.IDAlert, loc.get(0), null ) );
                        alertY_add_at = System.currentTimeMillis();
                    }
                    alertY = true;
                }
            }

            // Add location to ECA database
            if( !alertX && !alertY ){
                // If elapsed time > 2 seconds
                if( System.currentTimeMillis() - alertPos_add_at >= 2000  ) {
// A TESTER
                    if( loc.get(0).distanceTo(loc.get(1)) > 10 ) {
                        if( _tracking ) database.addECA(parcour_id, ECALine.newInstance(loc.get(0), loc.get(1)));
                        alertPos_add_at = System.currentTimeMillis();
                    }
                }
            }

            // Update ui interface
            ForceSeuil seuil = null;
            if( alertX && alertY ) {
                if( seuil_x.level.getValue() > seuil_y.level.getValue() ) alertY = false;
                else  alertX = false;
            }
            if( alertX ) seuil = seuil_x; else if( alertY ) seuil = seuil_y;
if( seuil != null ) Log.d("AA","SEUIL: " + seuil.toString() );
            if( seuil != null ) {
                if (seuil_ui == null || !seuil_ui.equals(seuil)) {
                    if (listener != null) listener.onForceChanged(seuil.type, seuil.level);
                    seuil_ui = seuil;
                }
            } else {
                clear_force_ui();
            }

        }
    }

    /// ============================================================================================
    /// CALCUL COTATION
    /// ============================================================================================

    private long cotation_update_at = 0;
    private long forces_update_at = 0;

    // Calculate note of the current parcours
    private void calc_parcour_cotation() {

        if( listener != null ){
            // If elapsed time > 5 minutes
            if( cotation_update_at + (5*60*1000) < System.currentTimeMillis()){
                if( readerEPCFile != null ){
addLog( "calc_parcour_cotation" );
                    cotation_update_at = System.currentTimeMillis();
                    float cotation_A = get_cotation_force(DataDOBJ.ACCELERATIONS,parcour_id);
                    float cotation_F = get_cotation_force(DataDOBJ.FREINAGES,parcour_id);
                    float cotation_V = get_cotation_force(DataDOBJ.VIRAGES,parcour_id);
                    float coeff_A = DataDOBJ.get_coefficient_general(ctx,DataDOBJ.ACCELERATIONS);
                    float coeff_F = DataDOBJ.get_coefficient_general(ctx,DataDOBJ.FREINAGES);
                    float coeff_V = DataDOBJ.get_coefficient_general(ctx,DataDOBJ.VIRAGES);
                    float cotation = ((cotation_A + cotation_F + cotation_V)/3)
                            / ((coeff_A + coeff_F + coeff_V)/3);

                    LEVEL_t level_note;
                    if( cotation >= 16f ) level_note = LEVEL_t.LEVEL_1;
                    else if( cotation >= 13f ) level_note = LEVEL_t.LEVEL_2;
                    else if( cotation >= 9f ) level_note = LEVEL_t.LEVEL_3;
                    else if( cotation >= 6f ) level_note = LEVEL_t.LEVEL_4;
                    else level_note = LEVEL_t.LEVEL_5;

                    long end = startOfDays(System.currentTimeMillis());
                    long begin = end - (5 * 24 * 3600 * 1000);
                    cotation_A = get_cotation_force(DataDOBJ.ACCELERATIONS,-1,begin,end);
                    cotation_F = get_cotation_force(DataDOBJ.FREINAGES,-1,begin,end);
                    cotation_V = get_cotation_force(DataDOBJ.VIRAGES,-1,begin,end);
                    coeff_A = DataDOBJ.get_coefficient_general(ctx,DataDOBJ.ACCELERATIONS);
                    coeff_F = DataDOBJ.get_coefficient_general(ctx,DataDOBJ.FREINAGES);
                    coeff_V = DataDOBJ.get_coefficient_general(ctx,DataDOBJ.VIRAGES);
                    cotation = ((cotation_A + cotation_F + cotation_V)/3)
                            / ((coeff_A + coeff_F + coeff_V)/3);

                    LEVEL_t level_5_days;
                    if( cotation >= 16f ) level_5_days = LEVEL_t.LEVEL_1;
                    else if( cotation >= 13f ) level_5_days = LEVEL_t.LEVEL_2;
                    else if( cotation >= 9f ) level_5_days = LEVEL_t.LEVEL_3;
                    else if( cotation >= 6f ) level_5_days = LEVEL_t.LEVEL_4;
                    else level_5_days = LEVEL_t.LEVEL_5;

                    listener.onNoteChanged( (int)cotation, level_note, level_5_days );
                }
            }
        }
    }

    // Calculate note of the forces for the current parcours ( A, V, F and M )
    private void calc_cotation_forces() {

        if( listener != null ){
            // If elapsed time > 5 minutes
            if( forces_update_at + (5*60*1000) < System.currentTimeMillis()){
                if( readerEPCFile != null ){
addLog( "calc_cotation_forces" );
                    // Calcul force note: 10 minutes = 600 seondes
                    forces_update_at = System.currentTimeMillis();
                    float A = get_cotation_force(DataDOBJ.ACCELERATIONS,parcour_id,600);
                    float F = get_cotation_force(DataDOBJ.FREINAGES,parcour_id,600);
                    float V = get_cotation_force(DataDOBJ.VIRAGES,parcour_id,600);
                    float M = (A+F+V) > 0 ? (A+F+V)/3 : 0;

addLog( "Cotation A: " + A );
addLog( "Cotation F: " + F );
addLog( "Cotation V: " + V );
addLog( "Cotation M: " + M );
                    // Update note for "Accelerations"
                    LEVEL_t level;
                    if( A >= 16f ) level = LEVEL_t.LEVEL_1;
                    else if( A >= 13f ) level = LEVEL_t.LEVEL_2;
                    else if( A >= 9f ) level = LEVEL_t.LEVEL_3;
                    else if( A >= 6f ) level = LEVEL_t.LEVEL_4;
                    else level = LEVEL_t.LEVEL_5;
                    listener.onScoreChanged( SCORE_t.ACCELERATING, level );

                    // Update note for "Freinages"
                    if( F >= 16f ) level = LEVEL_t.LEVEL_1;
                    else if( F >= 13f ) level = LEVEL_t.LEVEL_2;
                    else if( F >= 9f ) level = LEVEL_t.LEVEL_3;
                    else if( F >= 6f ) level = LEVEL_t.LEVEL_4;
                    else level = LEVEL_t.LEVEL_5;
                    listener.onScoreChanged( SCORE_t.BRAKING, level );

                    // Update note for "Virage"
                    if( V >= 16f ) level = LEVEL_t.LEVEL_1;
                    else if( V >= 13f ) level = LEVEL_t.LEVEL_2;
                    else if( V >= 9f ) level = LEVEL_t.LEVEL_3;
                    else if( V >= 6f ) level = LEVEL_t.LEVEL_4;
                    else level = LEVEL_t.LEVEL_5;
                    listener.onScoreChanged( SCORE_t.CORNERING, level );

                    // Update force averages, "MOYENNE"
                    if( M >= 16f ) level = LEVEL_t.LEVEL_1;
                    else if( M >= 13f ) level = LEVEL_t.LEVEL_2;
                    else if( M >= 9f ) level = LEVEL_t.LEVEL_3;
                    else if( M >= 6f ) level = LEVEL_t.LEVEL_4;
                    else level = LEVEL_t.LEVEL_5;
                    listener.onScoreChanged( SCORE_t.AVERAGE, level );
                }
            }
        }
    }

    /// Get force cotation (A,F,V)per parcours (per all parcours if parcour_id <= 0 )
    /// between timespamp
    private float get_cotation_force( String type, long parcour_id, long begin, long end ){
        float ret = 20f;

        if( !DataDOBJ.ACCELERATIONS.equals(type)
                && !DataDOBJ.FREINAGES.equals(type)
                && !DataDOBJ.VIRAGES.equals(type) ) return ret;


        // COEFFICIENTS
        float coeff_general = DataDOBJ.get_coefficient_general(ctx,type);
        int coeff_vert = DataDOBJ.get_coefficient(ctx,type,DataDOBJ.VERT);
        int coeff_bleu = DataDOBJ.get_coefficient(ctx,type,DataDOBJ.BLEU);
        int coeff_jaune = DataDOBJ.get_coefficient(ctx,type,DataDOBJ.JAUNE);
        int coeff_orange = DataDOBJ.get_coefficient(ctx,type,DataDOBJ.ORANGE);
        int coeff_rouge = DataDOBJ.get_coefficient(ctx,type,DataDOBJ.ROUGE);

        // OBJECTIFS FIXES AU CONDUCTEUR (in percent)
        int obj_vert = DataDOBJ.get_objectif(ctx,type,DataDOBJ.VERT);
        int obj_bleu = DataDOBJ.get_objectif(ctx,type,DataDOBJ.BLEU);
        int obj_jaune = DataDOBJ.get_objectif(ctx,type,DataDOBJ.JAUNE);
        int obj_orange = DataDOBJ.get_objectif(ctx,type,DataDOBJ.ORANGE);
        int obj_rouge = DataDOBJ.get_objectif(ctx,type,DataDOBJ.ROUGE);

        // VALEUR DU PARCOURS (Par seuil, en nombre d evenement)
        int nb_vert, nb_bleu, nb_jaune, nb_orange, nb_rouge;
        if( DataDOBJ.ACCELERATIONS.equals(type) ){
            nb_vert = database.countNbEvent(readerEPCFile.getForceSeuil(0).IDAlert, parcour_id, begin, end);
            nb_bleu = database.countNbEvent(readerEPCFile.getForceSeuil(1).IDAlert, parcour_id, begin, end);
            nb_jaune = database.countNbEvent(readerEPCFile.getForceSeuil(2).IDAlert, parcour_id, begin, end);
            nb_orange = database.countNbEvent(readerEPCFile.getForceSeuil(3).IDAlert, parcour_id, begin, end);
            nb_rouge = database.countNbEvent(readerEPCFile.getForceSeuil(4).IDAlert, parcour_id, begin, end);
        } else if( DataDOBJ.FREINAGES.equals(type) ){
            nb_vert = database.countNbEvent(readerEPCFile.getForceSeuil(5).IDAlert, parcour_id, begin, end);
            nb_bleu = database.countNbEvent(readerEPCFile.getForceSeuil(6).IDAlert, parcour_id, begin, end);
            nb_jaune = database.countNbEvent(readerEPCFile.getForceSeuil(7).IDAlert, parcour_id, begin, end);
            nb_orange = database.countNbEvent(readerEPCFile.getForceSeuil(8).IDAlert, parcour_id, begin, end);
            nb_rouge = database.countNbEvent(readerEPCFile.getForceSeuil(9).IDAlert, parcour_id, begin, end);
        } else {//if( DataDOBJ.VIRAGES.equals(type) ){
            nb_vert = database.countNbEvent(readerEPCFile.getForceSeuil(10).IDAlert, parcour_id, begin, end);
            nb_bleu = database.countNbEvent(readerEPCFile.getForceSeuil(11).IDAlert, parcour_id, begin, end);
            nb_jaune = database.countNbEvent(readerEPCFile.getForceSeuil(12).IDAlert, parcour_id, begin, end);
            nb_orange = database.countNbEvent(readerEPCFile.getForceSeuil(13).IDAlert, parcour_id, begin, end);
            nb_rouge = database.countNbEvent(readerEPCFile.getForceSeuil(14).IDAlert, parcour_id, begin, end);
            nb_vert += database.countNbEvent(readerEPCFile.getForceSeuil(15).IDAlert, parcour_id, begin, end);
            nb_bleu += database.countNbEvent(readerEPCFile.getForceSeuil(16).IDAlert, parcour_id, begin, end);
            nb_jaune += database.countNbEvent(readerEPCFile.getForceSeuil(17).IDAlert, parcour_id, begin, end);
            nb_orange += database.countNbEvent(readerEPCFile.getForceSeuil(18).IDAlert, parcour_id, begin, end);
            nb_rouge += database.countNbEvent(readerEPCFile.getForceSeuil(19).IDAlert, parcour_id, begin, end);
        }
        int nb_total = nb_vert + nb_bleu + nb_jaune + nb_orange + nb_rouge;
addLog("NB_TOTAL: " + nb_total);

        // VALEUR DU PARCOURS (Par seuil, en pourcentage)
        int evt_vert = ( nb_vert > 0 ) ? nb_vert * 100 / nb_total : 0;
        int evt_bleu = ( nb_bleu > 0 ) ? nb_bleu * 100 / nb_total : 0;
        int evt_jaune = ( nb_jaune > 0 ) ? nb_jaune * 100 / nb_total : 0;
        int evt_orange = ( nb_orange > 0 ) ? nb_orange * 100 / nb_total : 0;
        int evt_rouge = ( nb_rouge > 0 ) ? nb_rouge * 100 / nb_total : 0;

        // TEST
//        if( DataDOBJ.ACCELERATIONS.equals(type) ){
//            evt_vert = 46;
//            evt_bleu = 21;
//            evt_jaune = 16;
//            evt_orange = 11;
//            evt_rouge = 6;
//        } else if( DataDOBJ.FREINAGES.equals(type) ){
//            evt_vert = 93;
//            evt_bleu = 0;
//            evt_jaune = 1;
//            evt_orange = 4;
//            evt_rouge = 2;
//        } else if( DataDOBJ.VIRAGES.equals(type) ){
//            evt_vert = 46;
//            evt_bleu = 21;
//            evt_jaune = 16;
//            evt_orange = 11;
//            evt_rouge = 6;
//        }


        /// UPDATE STATS OF THE LAST DRIVING
        if( parcour_id == StatsLastDriving.get_start_at(ctx) ) {
            if( DataDOBJ.ACCELERATIONS.equals(type) ){
                StatsLastDriving.init_objectif(ctx);
                StatsLastDriving.set_resultat_A(ctx,LEVEL_t.LEVEL_1,evt_vert);
                StatsLastDriving.set_resultat_A(ctx,LEVEL_t.LEVEL_2,evt_vert);
                StatsLastDriving.set_resultat_A(ctx,LEVEL_t.LEVEL_3,evt_vert);
                StatsLastDriving.set_resultat_A(ctx,LEVEL_t.LEVEL_4,evt_vert);
                StatsLastDriving.set_resultat_A(ctx,LEVEL_t.LEVEL_5,evt_vert);
            } else if( DataDOBJ.FREINAGES.equals(type) ){
                StatsLastDriving.init_objectif(ctx);
                StatsLastDriving.set_resultat_F(ctx,LEVEL_t.LEVEL_1,evt_vert);
                StatsLastDriving.set_resultat_F(ctx,LEVEL_t.LEVEL_2,evt_vert);
                StatsLastDriving.set_resultat_F(ctx,LEVEL_t.LEVEL_3,evt_vert);
                StatsLastDriving.set_resultat_F(ctx,LEVEL_t.LEVEL_4,evt_vert);
                StatsLastDriving.set_resultat_F(ctx,LEVEL_t.LEVEL_5,evt_vert);
            } else {//if( DataDOBJ.VIRAGES.equals(type) ){
                StatsLastDriving.init_objectif(ctx);
                StatsLastDriving.set_resultat_V(ctx,LEVEL_t.LEVEL_1,evt_vert);
                StatsLastDriving.set_resultat_V(ctx,LEVEL_t.LEVEL_2,evt_vert);
                StatsLastDriving.set_resultat_V(ctx,LEVEL_t.LEVEL_3,evt_vert);
                StatsLastDriving.set_resultat_V(ctx,LEVEL_t.LEVEL_4,evt_vert);
                StatsLastDriving.set_resultat_V(ctx,LEVEL_t.LEVEL_5,evt_vert);
            }
            StatsLastDriving.set_distance( ctx, database.get_distance(parcour_id) );
            StatsLastDriving.set_times( ctx, (long) chronoRide.getSeconds());
        }

        // CALCUL INTERMEDIARE PAR SEUIL
        int calc_vert = ( evt_vert >= obj_vert ) ? 20*coeff_vert : 0;
        int calc_jaune = ( evt_jaune <= obj_jaune ) ? 0 : (obj_jaune-evt_jaune)*coeff_jaune;
        int calc_orange = ( evt_orange <= obj_orange ) ? 0 : (obj_orange-evt_orange)*coeff_orange;
        int calc_rouge = ( evt_rouge <= obj_rouge ) ? 0 : (obj_rouge-evt_rouge)*coeff_rouge;

        // CALCUL MOYENNE POUR CETTE FORCE
        int tmp = (calc_vert+(calc_jaune+calc_orange+calc_rouge));
        ret = tmp*coeff_general;

        return ret;
    }

    // Get force cotation by parcours, or all parcours (parcours_id = -1), in the last X seconds
    private float get_cotation_force( String type, long parcour_id, long secs  ){
        long end = System.currentTimeMillis();
        long begin = end - (secs*1000);
        return get_cotation_force(type, parcour_id, begin, end);
    }

    // Get force cotation per parcours, or all parcours (parcours_id = -1);
    private float get_cotation_force( String type, long parcour_id ){
        long begin = 0;
        long end = System.currentTimeMillis() + 10000;
        return get_cotation_force(type, parcour_id, begin, end);
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
    private void calc_recommended_speed() {

        // Calculate recommended val and get speed maximum since XX secondes
        //if( recommended_speed_update_at + (5*60*1000) < System.currentTimeMillis()) {
        if( recommended_speed_update_at + (30*1000) < System.currentTimeMillis()) {
            if (readerEPCFile != null) {
addLog("calc_recommended_speed");
                // Get the horizontal maximum speed since
                speed_H = database.speed_avg(parcour_id, 10 * 60,
                        readerEPCFile.getForceSeuil(0).IDAlert, // IDAlert +X1
                        readerEPCFile.getForceSeuil(1).IDAlert, // IDAlert +X2
                        readerEPCFile.getForceSeuil(5).IDAlert, // IDAlert -X1
                        readerEPCFile.getForceSeuil(6).IDAlert  // IDAlert -X2
                );
                // Get the vertical maximum speed since
                speed_V = database.speed_avg(parcour_id, 5 * 60,
                        readerEPCFile.getForceSeuil(10).IDAlert,    // IDAlert +Y1
                        readerEPCFile.getForceSeuil(11).IDAlert,    // IDAlert +Y2
                        readerEPCFile.getForceSeuil(15).IDAlert,    // IDAlert -Y1
                        readerEPCFile.getForceSeuil(16).IDAlert     // IDAlert -Y2
                );
                // Get the average speed
                speed_max = database.speed_max(parcour_id, 5 * 60);
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
            String key = ctx.getResources().getString(R.string.shock_trigger_mG);
            int val = ctx.getResources().getInteger(R.integer.shock_trigger_mG_def);
            val = sp.getInt(key,val);

            if( interval(0.0, XmG ) > val
                    || interval(0.0, YmG_shock) > val ) {
                listener.onShock();
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

    public void setStopped(){ button_stop = true; }

    private STATUS_t on_stopped(){
        STATUS_t ret = STATUS_t.PAR_STOPPED;

        // Clear UI
        clear_force_ui();
        button_stop = false;

        // Checking if ready to start a new parcours
        boolean ready_to_started = (modules.getNumberOfBoxConnected() >= 1
                && mov_t_last != MOVING_t.STP
                && mov_t_last != MOVING_t.UNKNOW
                /*&& engine_t == ENGINE_t.ON*/);

        if ( !ready_to_started ) {
            chrono_ready_to_start.stop();
        } else {
            if ( !chrono_ready_to_start.isStarted() ) chrono_ready_to_start.start();
            if ( chrono_ready_to_start.getSeconds() > SECS_TO_SET_PARCOURS_START ) {
                cotation_update_at = 0;
                forces_update_at = 0;
                alertX_add_at = 0;
                alertY_add_at = 0;
                alertPos_add_at = 0;
                recommended_speed_update_at = 0;
                readerEPCFile.loadFromApp(ctx);

                addLog("START PARCOURS");
                parcour_id = System.currentTimeMillis();
                StatsLastDriving.startDriving(ctx,parcour_id);
                chronoRide.start();
                ret = STATUS_t.PAR_STARTED;
                if (listener != null){
                    listener.onForceChanged(FORCE_t.UNKNOW,LEVEL_t.LEVEL_UNKNOW);
                    listener.onNoteChanged(20,LEVEL_t.LEVEL_1, LEVEL_t.LEVEL_UNKNOW);
                    listener.onScoreChanged(SCORE_t.ACCELERATING,LEVEL_t.LEVEL_1);
                    listener.onScoreChanged(SCORE_t.BRAKING,LEVEL_t.LEVEL_1);
                    listener.onScoreChanged(SCORE_t.CORNERING,LEVEL_t.LEVEL_1);
                    listener.onScoreChanged(SCORE_t.AVERAGE,LEVEL_t.LEVEL_1);
                    listener.onRecommendedSpeedChanged(SPEED_t.IN_STRAIGHT_LINE,0,LEVEL_t.LEVEL_UNKNOW,true);
                    listener.onRecommendedSpeedChanged(SPEED_t.IN_CORNERS,0,LEVEL_t.LEVEL_UNKNOW,true);
                    listener.onStatusChanged(ret);
                }
            }
        }
        return ret;
    }

    private STATUS_t on_paused() throws InterruptedException {
        STATUS_t ret = STATUS_t.PAR_PAUSING;

        // Checking if car is stopped
        if ( button_stop ||
                ( mov_t_last == MOVING_t.STP
                        && mov_t_last_chrono.getSeconds() > SECS_TO_SET_PARCOURS_STOPPED ) )
        {

            chronoRide.stop();
            StatsLastDriving.set_times(ctx, (long) chronoRide.getSeconds());
            StatsLastDriving.set_distance(ctx,database.get_distance(parcour_id));
            upload_cep();
            upload_custom_markers();
            upload_parcours_type();
            ret = STATUS_t.PAR_STOPPED;
            if (listener != null) listener.onStatusChanged(ret);
            addLog("STOP PARCOURS");
            clear_force_ui();
        }
        // Or checking if car re-moving
        else if (mov_t_last != MOVING_t.STP
                && mov_t_last_chrono.getSeconds() > SECS_TO_SET_PARCOURS_RESUME) {
            ret = STATUS_t.PAR_RESUME;
            if (listener != null) listener.onStatusChanged(ret);

            // ADD ECA Line when resuming
            if( _tracking ) {
                Location loc = get_last_location();
                database.addECA(parcour_id, ECALine.newInstance(ECALine.ID_RESUME, loc, null));
            }

            addLog("RESUME PARCOURS");
            clear_force_ui();
        }

        return ret;
    }

    private STATUS_t on_moved(STATUS_t status){
        STATUS_t ret = status;

        calc_eca();
        calc_parcour_cotation();
        calc_cotation_forces();
        check_shock();
        calc_recommended_speed();

        // Checking if car is in pause
        if (mov_t_last == MOVING_t.STP
                && mov_t_last_chrono.getSeconds() > SECS_TO_SET_PARCOURS_PAUSE) {
            ret = STATUS_t.PAR_PAUSING;

            // ADD ECA Line when pausing
            if( _tracking ) {
                Location loc = get_last_location();
                database.addECA(parcour_id, ECALine.newInstance(ECALine.ID_PAUSE, loc, null));
            }

            if (listener != null) listener.onStatusChanged(ret);
            addLog("PAUSE PARCOURS");

            clear_force_ui();
        }

        return ret;
    }

    /// ============================================================================================
    /// LOCATIONS
    /// ============================================================================================

    private List<Location> locations = new ArrayList<Location>();

    public void setLocation( Location location ) {
        if( location != null )
        {
            synchronized ( this ) {
                // Clear locations list
                if (locations.size() > 0) {
                    if (System.currentTimeMillis() - locations.get(0).getTime() > 15000) {
                        locations.clear();
                    }
                }
                // Add new location
                if( !locations.isEmpty() ){
                    if( locations.get(0).distanceTo( location ) < 10 ){
                        locations.remove(0);
                    }
                }
                this.locations.add(0, location);
                // Limit list size
                while (this.locations.size() > 10) this.locations.remove(this.locations.size() - 1);
            }

            switchON( true );
        }
    }

    private synchronized List<Location> get_location_list(int length){

        List<Location> list = null;

        // Clear locations list
        if (locations.size() > 0) {
            if (System.currentTimeMillis() - locations.get(0).getTime() > 15000) {
                locations.clear();
            }
        }
        // Get sublist
        if( locations.size() >= length ) {
            list = this.locations.subList(0,length);
        }
        return list;
    }

    private synchronized Location get_last_location(){
        Location ret = null;
        if (locations.size() > 0) ret = locations.get(0);
        return ret;
    }

    /// ============================================================================================
    /// DEBUG
    /// ============================================================================================

    private String log = "";

    private void setLog( String txt ){
        log = txt;
    }

    private void addLog( String txt ){
        if( !log.isEmpty() ) log += System.getProperty("line.separator");
        log += txt;
        if( listener != null ) listener.onDebugLog( log );
    }

}