package com.preventium.boxpreventium.manager;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.preventium.boxpreventium.database.DBHelper;
import com.preventium.boxpreventium.enums.ENGINE_t;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.MOVING_t;
import com.preventium.boxpreventium.enums.STATUS_t;
import com.preventium.boxpreventium.module.HandlerBox;
import com.preventium.boxpreventium.server.CFG.DataCFG;
import com.preventium.boxpreventium.server.CFG.ReaderCFGFile;
import com.preventium.boxpreventium.server.ECA.ECALine;
import com.preventium.boxpreventium.server.EPC.DataEPC;
import com.preventium.boxpreventium.server.EPC.ForceSeuil;
import com.preventium.boxpreventium.server.EPC.ReaderEPCFile;
import com.preventium.boxpreventium.server.FilesDownloader;
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
    private String log = "";
    private ENGINE_t engine_t = ENGINE_t.UNKNOW;

    public interface AppManagerListener {
        void onNumberOfBoxChanged( int nb );
        void onChronoRideChanged( String txt );
        void onForceChanged( FORCE_t type, LEVEL_t level );
        void onDebugLog(String txt);
        void onStatusChanged(STATUS_t status);
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


    private MOVING_t mov_t = MOVING_t.STP;
    private MOVING_t mov_t_last = MOVING_t.UNKNOW;
    private Chrono mov_chrono = new Chrono();
    private Chrono mov_t_last_chrono = new Chrono();

    private long parcour_id = 0;


    private List<Location> locations = new ArrayList<Location>();

    private Chrono chronoRide = new Chrono();
    private String chronoRideTxt = "";

    public AppManager(Context ctx, AppManagerListener listener) {
        super(null);
        this.ctx = ctx;
        this.listener = listener;
        this.modules = new HandlerBox(ctx,this);
        this.database = new DBHelper(ctx);
    }

    public boolean startThread(){
        boolean ret = false;
        if( !isRunning()  ) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AppManager.this.run();
                }
            }).start();
            ret = true;
        }
        return ret;
    }

    public void stopThread(){setStop();}

    public void setLocation( Location location ) {
        if( location != null ) {
            startThread();
            //addLog("NEW POS: " + location.getLatitude() + " " + location.getLongitude() );
            this.locations.add( 0, location );
            while ( this.locations.size() > 10 ) this.locations.remove( this.locations.size() - 1 );
        }
    }

    public void on_constant_speed(){ modules.on_constant_speed(); }

    public void on_acceleration(){ modules.on_acceleration(); }

    @Override
    public void myRun() throws InterruptedException {
        super.myRun();

        setLog( "AppManager begin...");

        STATUS_t status = STATUS_t.CAR_STOPPED;
        if( listener != null )listener.onStatusChanged( status );

        status = STATUS_t.GETTING_CFG;
        if( listener != null )listener.onStatusChanged( status );
        boolean cfg = getting_cfg();
        while ( isRunning() && !cfg  ){
            sleep(1000);
            cfg = getting_cfg();
        }

        status = STATUS_t.GETTING_EPC;
        if( listener != null )listener.onStatusChanged( status );
        boolean epc = getting_epc();
        while( isRunning() && !epc ) {
            sleep(1000);
            epc = getting_epc();
        }

        status = STATUS_t.CAR_STOPPED;
        if( listener != null )listener.onStatusChanged( status );

        if( isRunning() ) {
            database.clearAll();
            engine_t = ENGINE_t.UNKNOW;
            chronoRideTxt = "0:00";
            chronoRide = new Chrono();

            Chrono mov_t_chrono = new Chrono();

            while (isRunning()) {

                updateRideTime();
                modules.setActive(true);

                sleep(500);

                calculateMovements();

                if( status == STATUS_t.CAR_STOPPED ) {

                    clear_force_ui();

                    boolean ready_to_started = (modules.getNumberOfBoxConnected() > 0
                            && mov_t_last != MOVING_t.STP /*&& engine_t == ENGINE_t.ON*/);
                    if (!ready_to_started) {
                        mov_t_chrono.stop();
                    } else {
                        if (!mov_t_chrono.isStarted()) mov_t_chrono.start();
                        if (mov_t_chrono.getSeconds() > 10) {

                            // ....
                            readerEPCFile.loadFromApp(ctx,1);

                            database.clearAll();

                            addLog("START PARCOURS");
                            status = STATUS_t.CAR_MOVING;
                            if (listener != null) listener.onStatusChanged(status);
                            parcour_id = System.currentTimeMillis();

                            // MISE A JOUR DU CHRONO
                            chronoRide.start();
                            updateRideTime();

                        }

                    }

                } else if( status == STATUS_t.CAR_MOVING || status == STATUS_t.CAR_PAUSING ) {

                    // MISE A JOUR DU CHRONO
                    updateRideTime();


                    // SET PAUSING
                    if( mov_t_last == MOVING_t.STP
                            && mov_t_last_chrono.getSeconds() > 10
                            && status == STATUS_t.CAR_MOVING ) {
                        status = STATUS_t.CAR_PAUSING;
                        if( listener != null ) listener.onStatusChanged( status );
                        addLog("PAUSE PARCOURS");
                        clear_force_ui();
                    }
                    // SET STOPPED
                    else if( mov_t_last == MOVING_t.STP
                            && mov_t_last_chrono.getSeconds() > 20
                            && status == STATUS_t.CAR_PAUSING ) {
                        status = STATUS_t.CAR_STOPPED;
                        if( listener != null ) listener.onStatusChanged( status );
                        addLog("STOP PARCOURS");
                        clear_force_ui();
                    }
                    // SET RESUME
                    else if( mov_t_last != MOVING_t.STP
                            && mov_t_last_chrono.getSeconds() > 10
                            && status == STATUS_t.CAR_PAUSING ) {
                        status = STATUS_t.CAR_MOVING;
                        if( listener != null ) listener.onStatusChanged( status );
                        addLog("RESUME PARCOURS");
                        clear_force_ui();
                    } else if( status == STATUS_t.CAR_MOVING ){
                        addLog("IN PARCOURS");
                        prepare_eca();
                    }

                }
            }
        }

        modules.setActive( false );

        addLog( "AppManager end.");
//        if( DEBUG ) Log.d(TAG,"AppManager thread begin.");
//
//        database.clearAll();
//
//        chronoRideTxt = "";
//        chronoRide.start();
//        setLog("");
//
//        addLog( "Download cfg..." );
//        run_get_cfg();
//        addLog( "Download epc..." );
//        run_get_epc();
//        addLog( "Activate HandlerBox..." );
//        modules.setActive( true );
//
//        while ( isRunning() ) {
//
//            sleep(500);
//            updateRideTime();
//
//            switch ( mode ) {
//                case NONE:
//                    calculateMovements();
//                    updateAlertForce();
//                    break;
//                case CFG:
//                    Log.d(TAG,"Download CFG...");
//                    break;
//                case EPC:
//                    Log.d(TAG,"Download EPC...");
//                    break;
//            }
//        }
//
//        addLog( "Desactivate HandlerBox..." );
//        modules.setActive( false );
//
//        ArrayList<ECALine> alert = database.alertList();
//        addLog( "Alert list size: " + alert.size() );
//        addLog( "Box event data size: " + database.boxEventsData().length );
//
//        if( DEBUG ) Log.d(TAG,"AppManager thread end.");

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

    private boolean getting_cfg() throws InterruptedException {
        addLog( "Trying to prepare cfg_file..." );
        boolean cfg_file_ready = false;
        if( isRunning() ) {
            while ( isRunning() && !cfg_file_ready ) {
                ReaderCFGFile reader_cfg = new ReaderCFGFile();
                FTPConfig config = new FTPConfig("ftp.ikalogic.com","ikalogic","Tecteca1",21);
                FTPClientIO ftp = new FTPClientIO();
                File folder = new File(ctx.getFilesDir(), "");
                if( ftp.ftpConnect(config, 5000) ) {

                    boolean exist_server_cfg = false;
                    FTPFile[] files = ftp.ftpPrintFiles();
                    String srcFileName = ComonUtils.getIMEInumber(ctx) + ".CFG";
                    for ( FTPFile f : files ) {
                        if( f.isFile() && f.getName().equals(srcFileName) ){
                            exist_server_cfg = true;
                            break;
                        }
                    }
                    if( exist_server_cfg ){
                        // Create folder if not exist
                        if (!folder.exists())
                            if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
                        if( folder.exists() ) {
                            String desFileName = String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), srcFileName);
                            if( ftp.ftpDownload(srcFileName, desFileName) ) {
                                cfg_file_ready = reader_cfg.read(desFileName);
                                if( cfg_file_ready ) {
                                    // envoi acknowledge
                                    try {
                                        File temp = File.createTempFile("temp-file-name", ".tmp");
                                        String ackFileName = ComonUtils.getIMEInumber(ctx) + "_ok.CFG";
                                        ftp.ftpUpload(temp.getPath(), ackFileName);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } else {
                        cfg_file_ready = reader_cfg.loadFromApp(ctx);
                    }

                    ftp.ftpDisconnect();

                    if( isRunning() && !cfg_file_ready ) sleep(1000);
                }
            }
        }
        addLog( "CFG is ready: " + cfg_file_ready);
        return cfg_file_ready;
    }

    private boolean getting_epc(){
        addLog( "Trying to prepare epc_file..." );
        boolean epc_file_ready = false;
        if( isRunning() ) {
            ReaderEPCFile reader_epc = new ReaderEPCFile();
            FTPConfig config = DataCFG.getFptConfig(ctx);
            FTPClientIO ftp = new FTPClientIO();
            File folder = new File(ctx.getFilesDir(), "");
            if( config != null && ftp.ftpConnect(config, 5000) ) {

                boolean error = false;
                boolean exist_server_epc = false;
                FTPFile[] files = ftp.ftpPrintFiles();
                String srcFileName = "";
                String desFileName = "";
                int i = 1;
                while (i <= 5 && isRunning()) {

                    srcFileName = reader_epc.getEPCFileName(ctx,i,false);

                    exist_server_epc = false;
                    for ( FTPFile f : files ) {
                        if( f.isFile() && f.getName().equals(srcFileName) ){
                            exist_server_epc = true;
                            break;
                        }
                    }

                    if( exist_server_epc ) {
                        // Create folder if not exist
                        if (!folder.exists())
                            if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
                        if( folder.exists() ) {
                            desFileName = reader_epc.getEPCFilePath(ctx,i);
                            if( ftp.ftpDownload(srcFileName, desFileName) ) {
                                epc_file_ready = reader_epc.read(desFileName);
                                if( epc_file_ready ) {

                                    if( reader_epc.applyToApp( ctx, i ) ) {
                                        // envoi acknowledge
                                        try {
                                            File temp = File.createTempFile("temp-file-name", ".tmp");
                                            String ackFileName = reader_epc.getEPCFileName(ctx, i, true);
                                            ftp.ftpUpload(temp.getPath(), ackFileName);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    error = true;
                                }

                            }
                        } else {
                            error  = true;
                        }
                    }

                    i++;
                }
                epc_file_ready = !error;
                if( epc_file_ready ) {
                    // Counting epc files...
                    epc_file_ready = !DataEPC.getAppEpcExist(ctx).isEmpty();
                }

            }
        }
        addLog( "EPC is ready: " + epc_file_ready );
        return epc_file_ready;
    }

    private void updateRideTime() {
        String txt = String.format(Locale.getDefault(),"%d:%02d",(int)chronoRide.getHours(),(int)chronoRide.getMinutes());
        if( !chronoRideTxt.equals(txt) ) {
            if( listener != null ) listener.onChronoRideChanged( txt );
            chronoRideTxt = txt;
            //if( DEBUG ) Log.d(TAG,"Chrono ride changed: " + txt );
        }
    }

    private void calculateMovements(){
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
                    if (list.get(i).getSpeed() < list.get(i + 1).getSpeed()) {
                        acceleration = false;
                    }
                    if (list.get(i).getSpeed() > list.get(i + 1).getSpeed()) {
                        freinage = false;
                    }
                }
            }
            if (speed_max * MS_TO_KMH <= 1f) mov_t = MOVING_t.STP;
            else if ((speed_max - speed_min) * MS_TO_KMH < 3f) mov_t = MOVING_t.CST;
            else if (acceleration) mov_t = MOVING_t.ACC;
            else if (freinage) mov_t = MOVING_t.BRK;
            else mov_t = MOVING_t.NCS;

            // CALCULATE FORCE X
            // Pour calculer l'accélération longitudinale (accélération ou freinage) avec comme unité le g :
            // il faut connaître : la vitesse (v(t)) à l'instant t et à l'instant précédent(v(t-1)) et le delta t entre ces deux mesures.
            // a = ( v(t) - v(t-1) )/(9.81*( t - (t-1) ) )
            double mG = ((locations.get(0).getSpeed() - locations.get(1).getSpeed())
                    / (9.81 * ((locations.get(0).getTime() - locations.get(1).getTime()) * 0.001)))
                    * 1000.0;
            this.XmG = mG;
        }

        if (mov_t != mov_t_last)
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

    private void prepare_eca(){
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
                    database.addECA( parcour_id, ECALine.newInstance(seuil_x.IDAlert, loc.get(0), null ) );
                    alertX = true;
                }
            }

            // Compare the runtime Y value force with the prevent Y value force, and add alert to ECA database
            if( seuil_y != null ) {
                if( !seuil_y.equals(seuil_last_y) ) seuil_chrono_y.start();
                if( seuil_chrono_y.getSeconds() >= seuil_y.TPS ) {
                    seuil_chrono_y.start();
                    database.addECA( parcour_id, ECALine.newInstance(seuil_y.IDAlert, loc.get(0), null ) );
                    alertY = true;
                }
            }

            // Add location to ECA database
            if( !alertX && !alertY ){
                database.addECA( parcour_id, ECALine.newInstance( loc.get(0), loc.get(1) ) );
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

    private void clear_force_ui(){
        if( seuil_ui != null
                && seuil_chrono_x.getSeconds() > 3 && seuil_chrono_y.getSeconds() > 3 ){
            if( listener != null ) listener.onForceChanged( FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW );
            seuil_ui = null;
        }
    }

//    private void updateAlertForce(){
//
//        if( !locations.isEmpty() ) {
//
//            boolean alertX = false;
//            boolean alertY = false;
//
//            ecaLine_read = null;
//            // Read the runtime value force
//            seuil_curr_read_x = readerEPCFile.getForceSeuilForX(XmG);
//            seuil_curr_read_y = readerEPCFile.getForceSeuilForY(YmG);
//
//            // Compare the runtime X value force with the prevent X value force
//            if (seuil_curr_read_x != null) {
//                if (!seuil_curr_read_x.equals(seuil_prev_read_x)) {
//                    seuil_chrono_x.start();
//                }
//                if (seuil_chrono_x.getSeconds() >= seuil_curr_read_x.TPS) {
//                    seuil_chrono_x.start();
//                    Log.d(TAG, "ALERT...." + seuil_curr_read_x.toString());
//                    addLog( "ALERT...." + seuil_curr_read_y.toString() );
//                    ecaLine_read = ECALine.newInstance(seuil_curr_read_x.IDAlert, locations.get(0), null);
//                    database.addECA(ecaLine_read);
//                    alertX = true;
//                }
//            }
//
//            // Compare the runtime Y value force with the prevent Y value force
//            if (seuil_curr_read_y != null) {
//                if (!seuil_curr_read_y.equals(seuil_prev_read_y)) {
//                    seuil_chrono_y.start();
//                }
//                if (seuil_chrono_y.getSeconds() >= seuil_curr_read_y.TPS) {
//                    seuil_chrono_y.start();
//                    Log.d(TAG, "ALERT...." + seuil_curr_read_y.toString());
//                    addLog( "ALERT...." + seuil_curr_read_y.toString() );
//                    ecaLine_read = ECALine.newInstance(seuil_curr_read_y.IDAlert, locations.get(0), null);
//                    database.addECA(ecaLine_read);
//                    alertY = true;
//                }
//            }
//
//            // Update UI
//            if (alertX && alertY && seuil_curr_read_x != null && seuil_curr_read_y != null) {
//                if (seuil_curr_read_x.level.getValue() > seuil_curr_read_y.level.getValue()) {
//                    if (seuil_ui == null || !seuil_ui.equals(seuil_curr_read_x)) {
//                        if (listener != null)
//                            listener.onForceChanged(seuil_curr_read_x.type, seuil_curr_read_x.level);
//                        seuil_ui = seuil_curr_read_x;
//                    }
//                } else {
//                    if (seuil_ui == null || !seuil_ui.equals(seuil_curr_read_y)) {
//                        if (listener != null)
//                            listener.onForceChanged(seuil_curr_read_y.type, seuil_curr_read_y.level);
//                        seuil_ui = seuil_curr_read_y;
//                    }
//                }
//            } else if (alertX && seuil_curr_read_x != null) {
//                if (seuil_ui == null || !seuil_ui.equals(seuil_curr_read_x)) {
//                    if (listener != null)
//                        listener.onForceChanged(seuil_curr_read_x.type, seuil_curr_read_x.level);
//                    seuil_ui = seuil_curr_read_x;
//                }
//            } else if (alertY && seuil_curr_read_y != null) {
//                if (seuil_ui == null || !seuil_ui.equals(seuil_curr_read_y)) {
//                    if (listener != null)
//                        listener.onForceChanged(seuil_curr_read_y.type, seuil_curr_read_y.level);
//                    seuil_ui = seuil_curr_read_y;
//                }
//            } else {
//                if (seuil_ui != null && seuil_chrono_x.getSeconds() > 3 && seuil_chrono_y.getSeconds() > 3) {
//                    Log.d(TAG, "ALERT.... ALL IS OK");
//                    if (listener != null)
//                        listener.onForceChanged(FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW);
//                    seuil_ui = null;
//                }
//            }
//
//            if (ecaLine_read == null && DataCFG.get_SEND_ALL_GPS_POINTS(ctx)) {
//                ecaLine_read = ECALine.newInstance(locations.get(0), null);
//                if (ecaLine_read != null && !ecaLine_read.equals(ecaLine_save)) {
//                    //addLog( ecaLine_read.toString() );
//                    database.addECA(ecaLine_read);
//                    ecaLine_save = ecaLine_read;
//                }
//
//            }
//        }
//
//        seuil_prev_read_x = seuil_curr_read_x;
//        seuil_prev_read_y = seuil_curr_read_y;
//    }

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

    private void setLog( String txt ){
        log = txt;
    }

    private void addLog( String txt ){
        if( !log.isEmpty() ) log += System.getProperty("line.separator");
        log += txt;
        if( listener != null ) listener.onDebugLog( log );
    }

}
