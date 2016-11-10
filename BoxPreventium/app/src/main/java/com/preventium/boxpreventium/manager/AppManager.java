package com.preventium.boxpreventium.manager;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.preventium.boxpreventium.database.DBHelper;
import com.preventium.boxpreventium.enums.ENGINE_t;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.MOVING_t;
import com.preventium.boxpreventium.enums.STATUS_t;
import com.preventium.boxpreventium.location.CustomMarkerData;
import com.preventium.boxpreventium.module.HandlerBox;
import com.preventium.boxpreventium.server.CFG.DataCFG;
import com.preventium.boxpreventium.server.CFG.ReaderCFGFile;
import com.preventium.boxpreventium.server.ECA.ECALine;
import com.preventium.boxpreventium.server.EPC.DataEPC;
import com.preventium.boxpreventium.server.EPC.ForceSeuil;
import com.preventium.boxpreventium.server.EPC.ReaderEPCFile;
import com.preventium.boxpreventium.utils.Chrono;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.ThreadDefault;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPClientIO;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPConfig;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private static final int SECS_TO_SET_PARCOURS_START = 10;
    private static final int SECS_TO_SET_PARCOURS_PAUSE = 10;
    private static final int SECS_TO_SET_PARCOURS_RESUME = 10;
    private static final int SECS_TO_SET_PARCOURS_STOPPED = 60;

    private FilesSender fileSender = null;

    private String log = "";
    private ENGINE_t engine_t = ENGINE_t.UNKNOW;

    public interface AppManagerListener {
        void onNumberOfBoxChanged( int nb );
        void onDrivingTimeChanged(String txt );
        void onForceChanged( FORCE_t type, LEVEL_t level );
        void onDebugLog(String txt);
        void onStatusChanged(STATUS_t status);
        void onDriveScoreChanged( float score );
        void onCustomMarkerDataListGet();
    }

    private Context ctx = null;
    private AppManagerListener listener = null;

    private HandlerBox modules = null;
    private DBHelper database = null;

    private ReaderEPCFile readerEPCFile = new ReaderEPCFile();
    private double XmG = 0.0;
    private double YmG = 0.0;
    ForceSeuil seuil_ui = null;
    private Chrono seuil_chrono_x = new Chrono();
    private Chrono seuil_chrono_y = new Chrono();
    ForceSeuil seuil_last_x = null;
    ForceSeuil seuil_last_y = null;

    private boolean customMarkerList_Received = false;
    private ArrayList<CustomMarkerData> customMarkerList = null;

    private MOVING_t mov_t = MOVING_t.STP;
    private MOVING_t mov_t_last = MOVING_t.UNKNOW;
    private Chrono mov_chrono = new Chrono();
    private Chrono mov_t_last_chrono = new Chrono();

    private long parcour_id = 0;
    private long cotation_update_at = 0;
    private long alertX_add_at = 0;
    private long alertY_add_at = 0;
    private long alertPos_add_at = 0;
    private long try_send_eca_at  = 0;


    private List<Location> locations = new ArrayList<Location>();

    private Chrono chronoRide = new Chrono();
    private String chronoRideTxt = "";

    private Chrono chrono_ready_to_start = Chrono.newInstance();

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

        setLog( "AppManager begin...");

        download_cfg();
        download_epc();
        STATUS_t status = first_init();
        upload_eca(true);

        while( isRunning() ) {
            modules.setActive( true );
            sleep(500);
            upload_eca(false);
            change_driving_time();
            calc_movements();

            switch ( status ) {
                case GETTING_CFG:
                case GETTING_EPC:
                    break;
                case CAR_STOPPED:
                    status = on_stopped();
                    break;
                case CAR_MOVING:
                    status = on_moved();
                    break;
                case CAR_PAUSING:
                    status = on_paused();
                    break;
            }

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
        database.addCEP( locations.get(0), device_mac, connected );
    }

    @Override
    public void onNumberOfBox(int nb) {
        if( DEBUG ) Log.d(TAG,"Number of preventium device connected changed: " + nb );
        if( listener != null ) listener.onNumberOfBoxChanged( nb );
    }

    @Override
    public void onForceChanged(double mG) {
        //if( DEBUG ) Log.d(TAG,"mG = " + mG );
        this.YmG = mG;
    }

    @Override
    public void onEngineStateChanged(ENGINE_t state) {
        this.engine_t = state;
    }

    // PRIVATE

    /// ============================================================================================
    /// UI
    /// ============================================================================================
    private STATUS_t first_init(){
        mov_t_last = MOVING_t.UNKNOW;
        mov_t = MOVING_t.UNKNOW;
        engine_t = ENGINE_t.UNKNOW;
        chronoRideTxt = "0:00";
        chronoRide = Chrono.newInstance();
        if( listener != null ){
            // For update UI correctly
            listener.onDebugLog("");
            listener.onDrivingTimeChanged(chronoRideTxt);
            listener.onDriveScoreChanged( 0f );
            listener.onStatusChanged( STATUS_t.CAR_PAUSING );
            listener.onStatusChanged( STATUS_t.CAR_STOPPED );
        }
        parcour_id = 0;
        cotation_update_at = 0;
        alertX_add_at = 0;
        alertY_add_at = 0;
        alertPos_add_at = 0;
        try_send_eca_at  = 0;
        return STATUS_t.CAR_STOPPED;
    }

    private void change_driving_time() {
        String txt = String.format(Locale.getDefault(),"%d:%02d",(int)chronoRide.getHours(),(int)chronoRide.getMinutes());
        if( !chronoRideTxt.equals(txt) ) {
            if( listener != null ) listener.onDrivingTimeChanged( txt );
            chronoRideTxt = txt;
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
            if( ftp.ftpConnect(config, 5000) ) {

                // Checking if .CFG file is in FTP server ?
                boolean exist_server_cfg = false;
                boolean exist_server_ack = false;
                String srcFileName = ComonUtils.getIMEInumber(ctx) + ".CFG";
                String srcAckName = ComonUtils.getIMEInumber(ctx) + "_ok.CFG";
                FTPFile[] files = ftp.ftpPrintFiles();
                for ( FTPFile f : files ) {
                    if( f.isFile() ) {
                        if (f.getName().equals(srcFileName)) exist_server_cfg = true;
                        if (f.getName().equals(srcAckName)) exist_server_ack = true;
                    }
                    if( exist_server_ack && exist_server_cfg ) break;
                }
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
            if( ftp.ftpConnect(config, 5000) ) {

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
                    FTPFile[] files = ftp.ftpPrintFiles();
                    String srcFileName = "";
                    String srcAckName = "";
                    String desFileName = "";
                    int i = 1;
                    while( i <= 5 && isRunning() ) {

                        exist_server_epc = false;
                        exist_server_ack = false;

                        // Checking if .EPC file is in FTP server ?
                        srcFileName = reader.getEPCFileName(ctx, i, false);
                        srcAckName = reader.getEPCFileName(ctx, i, true);
                        for ( FTPFile f : files ) {
                            if( f.isFile() ) {
                                if (f.getName().equals(srcFileName)) exist_server_epc = true;
                                if (f.getName().equals(srcAckName)) exist_server_ack = true;
                            }
                            if( exist_server_ack && exist_server_epc ) break;
                        }

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
    /// .ECA
    /// ============================================================================================

    /// Uploading .ECA file if is needed
    private boolean upload_eca( boolean now ){
        boolean ret = false;
        // If now is true or elapsed time > 1 minutes
        if( now
                || try_send_eca_at + 60000 < System.currentTimeMillis() ){
            fileSender.startThread();
            try_send_eca_at = System.currentTimeMillis();
            ret = true;
        }
        return ret;
    }

    /// ============================================================================================
    /// .POS (Map markers)
    /// ============================================================================================

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
                if( customMarkerList != null && customMarkerList.size() > 0 ){

                }
            }
        }
    }

    /// ============================================================================================
    /// CALCUL
    /// ============================================================================================

    private void calc_movements(){
        this.mov_t = MOVING_t.UNKNOW;
        this.XmG = 0f;
        boolean rightRoad = false;
        if( locations.size() >= 3 ) {
            List<Location> list = this.locations.subList(0,3);
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
            // Pour calculer l'accélération longitudinale (accélération ou freinage) avec comme unité le g :
            // il faut connaître : la vitesse (v(t)) à l'instant t et à l'instant précédent(v(t-1)) et le delta t entre ces deux mesures.
            // a = ( v(t) - v(t-1) )/(9.81*( t - (t-1) ) )
            this.XmG = ((locations.get(0).getSpeed() - locations.get(1).getSpeed())
                    / (9.81 * ((locations.get(0).getTime() - locations.get(1).getTime()) * 0.001)))
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

    private void calc_parcour_cotation() {

        if( listener != null ){
            // If elapsed time > 5 minutes
            if( cotation_update_at + (5*60*1000) < System.currentTimeMillis()){
                if( readerEPCFile != null ){

                    cotation_update_at = System.currentTimeMillis();

                    float coeff_general;
                    long coeff_vert, coeff_bleu, coeff_jaune, coeff_orange, coeff_rouge;
                    long nb_vert, nb_bleu, nb_jaune, nb_orange, nb_rouge;

                    // Cotation accélération
                    coeff_general = 0.1f;
                    coeff_vert = 1;
                    coeff_bleu = 5;
                    coeff_jaune = 10;
                    coeff_orange = 15;
                    coeff_rouge = 20;
                    nb_vert = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(0).IDAlert);
                    nb_bleu = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(1).IDAlert);
                    nb_jaune = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(2).IDAlert);
                    nb_orange = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(3).IDAlert);
                    nb_rouge = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(4).IDAlert);
                    float cotation_A = (
                            (nb_vert * coeff_vert) +
                                    (nb_bleu * coeff_bleu) +
                                    (nb_jaune * coeff_jaune) +
                                    (nb_orange * coeff_orange) +
                                    (nb_rouge * coeff_rouge) ) * coeff_general;

                    // Cotation freinage
                    coeff_general = 0.5f;
                    coeff_vert = 1;
                    coeff_bleu = 5;
                    coeff_jaune = 10;
                    coeff_orange = 15;
                    coeff_rouge = 20;
                    nb_vert = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(5).IDAlert);
                    nb_bleu = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(6).IDAlert);
                    nb_jaune = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(7).IDAlert);
                    nb_orange = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(8).IDAlert);
                    nb_rouge = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(9).IDAlert);
                    float cotation_F = (
                            (nb_vert * coeff_vert) +
                                    (nb_bleu * coeff_bleu) +
                                    (nb_jaune * coeff_jaune) +
                                    (nb_orange * coeff_orange) +
                                    (nb_rouge * coeff_rouge) ) * coeff_general;

                    // Cotation virage
                    coeff_general = 0.4f;
                    coeff_vert = 1;
                    coeff_bleu = 5;
                    coeff_jaune = 10;
                    coeff_orange = 15;
                    coeff_rouge = 20;
                    nb_vert = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(10).IDAlert)
                            + database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(15).IDAlert);
                    nb_bleu = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(11).IDAlert)
                            + database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(16).IDAlert);
                    nb_jaune = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(12).IDAlert)
                            + database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(17).IDAlert);
                    nb_orange = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(13).IDAlert)
                            + database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(18).IDAlert);
                    nb_rouge = database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(14).IDAlert)
                            + database.countNbEvent(parcour_id, readerEPCFile.getForceSeuil(19).IDAlert);
                    float cotation_V = (
                            (nb_vert * coeff_vert) +
                                    (nb_bleu * coeff_bleu) +
                                    (nb_jaune * coeff_jaune) +
                                    (nb_orange * coeff_orange) +
                                    (nb_rouge * coeff_rouge) ) * coeff_general;


                    float cotation = cotation_A + cotation_F + cotation_V;
                    listener.onDriveScoreChanged( cotation );
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
        if( locations.size() > 3 ) {

            List<Location> loc = locations.subList(0,2);

            boolean alertX = false;
            boolean alertY = false;

            // Read the runtime value force
            ForceSeuil seuil_x = readerEPCFile.getForceSeuilForX(XmG);
            ForceSeuil seuil_y = readerEPCFile.getForceSeuilForY(YmG);

            // Compare the runtime X value force with the prevent X value force, and add alert to ECA database
            if( seuil_x != null ) {
                if( !seuil_x.equals(seuil_last_x) ) seuil_chrono_x.start();
                if( seuil_chrono_x.getSeconds() >= seuil_x.TPS ) {
                    seuil_chrono_x.start();
                    // If elapsed time > 2 seconds
                    if( alertX_add_at + 2000 < System.currentTimeMillis()) {
                        database.addECA(parcour_id, ECALine.newInstance(seuil_x.IDAlert, loc.get(0), null));
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
                    if( alertY_add_at + 2000 < System.currentTimeMillis()) {
                        database.addECA( parcour_id, ECALine.newInstance(seuil_y.IDAlert, loc.get(0), null ) );
                        alertY_add_at = System.currentTimeMillis();
                    }
                    alertY = true;
                }
            }

            // Add location to ECA database
            if( !alertX && !alertY ){
                // If elapsed time > 2 seconds
                if( alertPos_add_at + 2000 < System.currentTimeMillis()) {
                    database.addECA( parcour_id, ECALine.newInstance( loc.get(0), loc.get(1) ) );
                    alertPos_add_at = System.currentTimeMillis();
                }
            }

            // Update ui interface
            ForceSeuil seuil = null;
            if( alertX && alertY ) {
                if( seuil_x.level.getValue() > seuil_y.level.getValue() ) alertY = false;
                else  alertX = false;
            }
            if( alertX ) seuil = seuil_x; else if( alertY ) seuil = seuil_y;
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
    /// PARCOUR
    /// ============================================================================================

    private STATUS_t on_stopped(){
        STATUS_t ret = STATUS_t.CAR_STOPPED;

        // Clear UI
        clear_force_ui();

        // Checking if ready to start a new parcours
        boolean ready_to_started = (modules.getNumberOfBoxConnected() >= 0
                && mov_t_last != MOVING_t.STP
                && mov_t_last != MOVING_t.UNKNOW /*&& engine_t == ENGINE_t.ON*/);

        if ( !ready_to_started ) {
            chrono_ready_to_start.stop();
        } else {
            if ( !chrono_ready_to_start.isStarted() ) chrono_ready_to_start.start();
            if ( chrono_ready_to_start.getSeconds() > SECS_TO_SET_PARCOURS_START ) {
                // ....
                cotation_update_at = 0;
                alertX_add_at = 0;
                alertY_add_at = 0;
                alertPos_add_at = 0;
                readerEPCFile.loadFromApp(ctx);
                addLog("START PARCOURS");
                parcour_id = System.currentTimeMillis();
                chronoRide.start();
                ret = STATUS_t.CAR_MOVING;
                if (listener != null) listener.onStatusChanged(ret);
            }
        }
        return ret;
    }

    private STATUS_t on_paused() throws InterruptedException {
        STATUS_t ret = STATUS_t.CAR_PAUSING;

        // Checking if car is stopped
        if (mov_t_last == MOVING_t.STP
                && mov_t_last_chrono.getSeconds() > SECS_TO_SET_PARCOURS_STOPPED) {

            chronoRide.stop();
            upload_custom_markers();
            ret = STATUS_t.CAR_STOPPED;
            if (listener != null) listener.onStatusChanged(ret);
            addLog("STOP PARCOURS");
            clear_force_ui();
        }
        // Or checking if car re-moving
        else if (mov_t_last != MOVING_t.STP
                && mov_t_last_chrono.getSeconds() > SECS_TO_SET_PARCOURS_RESUME) {
            ret = STATUS_t.CAR_MOVING;
            if (listener != null) listener.onStatusChanged(ret);
            addLog("RESUME PARCOURS");
            clear_force_ui();
        }

        return ret;
    }

    private STATUS_t on_moved(){
        STATUS_t ret = STATUS_t.CAR_MOVING;

        calc_eca();
        calc_parcour_cotation();

        // Checking if car is in pause
        if (mov_t_last == MOVING_t.STP
                && mov_t_last_chrono.getSeconds() > SECS_TO_SET_PARCOURS_PAUSE) {
            ret = STATUS_t.CAR_PAUSING;
            if (listener != null) listener.onStatusChanged(ret);
            addLog("PAUSE PARCOURS");
            clear_force_ui();
        }

        return ret;
    }

    /// ============================================================================================
    /// LOCATIONS
    /// ============================================================================================

    public void setLocation( Location location ) {
        if( location != null )
        {
            this.locations.add(0, location);
            while (this.locations.size() > 10) this.locations.remove(this.locations.size() - 1);

            switchON( true );
        }
    }













    private void clear_force_ui(){
        if( seuil_ui != null
                && seuil_chrono_x.getSeconds() > 3 && seuil_chrono_y.getSeconds() > 3 ){
            if( listener != null ) listener.onForceChanged( FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW );
            seuil_ui = null;
        }
    }

    private void setLog( String txt ){
        log = txt;
    }

    private void addLog( String txt ){
        if( !log.isEmpty() ) log += System.getProperty("line.separator");
        log += txt;
        if( listener != null ) listener.onDebugLog( log );
    }



}
