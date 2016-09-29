package com.preventium.boxpreventium.manager;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.preventium.boxpreventium.database.DBHelper;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.module.HandlerBox;
import com.preventium.boxpreventium.server.CFG.DataCFG;
import com.preventium.boxpreventium.server.ECA.ECALine;
import com.preventium.boxpreventium.server.EPC.ForceSeuil;
import com.preventium.boxpreventium.server.EPC.ReaderEPCFile;
import com.preventium.boxpreventium.server.FilesDownloader;
import com.preventium.boxpreventium.utils.Chrono;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.ThreadDefault;

import java.util.Locale;

/**
 * Created by Franck on 23/09/2016.
 */

public class AppManager extends ThreadDefault
    implements FilesDownloader.FilesDowloaderListener, HandlerBox.NotifyListener{

    private final static String TAG = "AppManager";
    private final static boolean DEBUG = true;

    public interface AppManagerListener {
        void onNumberOfBoxChanged( int nb );
        void onChronoRideChanged( String txt );
        void onForceChanged( FORCE_t type, LEVEL_t level );
    }

    private Context ctx = null;
    private AppManagerListener listener = null;
    private FilesDownloader downloader = null;
    private ReaderEPCFile readerEPCFile = new ReaderEPCFile();
    private HandlerBox modules = null;
    private DBHelper database = null;

    private FilesDownloader.MODE_t mode = FilesDownloader.MODE_t.NONE;

    private double XmG = 0.0;
    private double YmG = 0.0;
    private Chrono seuil_chrono = new Chrono();
    ForceSeuil seuil_ui = null;
    ForceSeuil seuil_prev_read = null;
    ForceSeuil seuil_curr_read = null;

    ECALine ecaLine_read = null;
    ECALine ecaLine_save = null;

    private Location location = null;
    private Location lastLocation = null;

    private Chrono chronoRide = new Chrono();
    private String chronoRideTxt = "";

    private AlertForce alertForce = null;

    public AppManager(Context ctx, AppManagerListener listener) {
        super(null);
        this.ctx = ctx;
        this.listener = listener;
        this.downloader = new FilesDownloader(ctx,this);
        this.modules = new HandlerBox(ctx,this);
        this.database = new DBHelper(ctx);
    }

    public boolean startMoving(){
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

    public void pauseMoving(){ setPause(); }

    public void resumeMoving(){setResume();}

    public void stopMoving(){setStop();}

    public void setLocation( Location location ) {
        // Pour calculer l'accélération longitudinale (accélération ou freinage) avec comme unité le g :
        // il faut connaître : la vitesse (v(t)) à l'instant t et à l'instant précédent(v(t-1)) et le delta t entre ces deux mesures.
        // a = ( v(t) - v(t-1) )/(9.81*( t - (t-1) ) )
        if( location != null && lastLocation != null ) {
            double mG =
                    ( ( location.getSpeed() - lastLocation.getSpeed() )
                            / ( 9.81 * ( (location.getTime()-lastLocation.getSpeed())*0.001) ) )
                    * 1000.0;
            this.XmG = mG;
        }
        this.lastLocation = this.location;
        this.location = location;
    }

    public void on_constant_speed(){ modules.on_constant_speed(); }

    public void on_acceleration(){ modules.on_acceleration(); }


    @Override
    public void myRun() throws InterruptedException {
        super.myRun();

        if( DEBUG ) Log.d(TAG,"AppManager thread begin.");

        database.clearAll();

        lastLocation = null;
        chronoRideTxt = "";
        chronoRide.start();

        run_get_cfg();
        run_get_epc();

        modules.setActive( true );

        while ( isRunning() ) {

            sleep(500);
            updateRideTime();

            switch ( mode ){
                case NONE:
                    updateAlertForce();
                    break;
                case CFG:
                    Log.d(TAG,"Download CFG...");
                    break;
                case EPC:
                    Log.d(TAG,"Download EPC...");
                    break;
            }
        }

        modules.setActive( false );
Log.d("AAA","database.alertList().size() = " + database.alertList().size() );
Log.d("AAA","database.boxEventsData().length = " + database.boxEventsData().length );
        if( DEBUG ) Log.d(TAG,"AppManager thread end.");

    }

    // FILES DOWNLOADER

    @Override
    public void onModeChanged(FilesDownloader.MODE_t mode_t) {
        Log.d(TAG,"Download mode changed: " + mode_t );
        this.mode = mode_t;
    }

    // HANDLER BOX

    @Override
    public void onScanState(boolean scanning) {
        if( DEBUG ) Log.d(TAG,"Searching preventium box is enable: " + scanning );
    }

    @Override
    public void onDeviceState(String device_mac, boolean connected) {
        database.addCEP( location, device_mac, connected );
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

    // PRIVATE

    private void run_get_cfg() throws InterruptedException {
        if( isRunning() ) {
            if (DEBUG) Log.d(TAG, " Download CFG... BEGIN");
            while (isRunning() && DataCFG.getFptConfig(ctx) == null) {
                downloader.downloadCFG();
                sleep(1000);
            }
            Log.d(TAG, "Download CFG... STOP");
        }
    }

    private void run_get_epc() throws InterruptedException {
        if( isRunning() ) {
            if (DEBUG) Log.d(TAG, " Download EPC... BEGIN");
            boolean read = readerEPCFile.read(ctx, 1);
            while (isRunning() && !read) {
                downloader.downloadEPC();
                sleep(5000);
                read = readerEPCFile.read(ctx, 1);
            }
            if (read) readerEPCFile.print();
            Log.d(TAG, "Download EPC... STOP");
        }
    }

    private void updateRideTime() {
        String txt = String.format(Locale.getDefault(),"%d:%02d",(int)chronoRide.getHours(),(int)chronoRide.getMinutes());
        if( !chronoRideTxt.equals(txt) ) {
            if( listener != null ) listener.onChronoRideChanged( txt );
            chronoRideTxt = txt;
            //if( DEBUG ) Log.d(TAG,"Chrono ride changed: " + txt );
        }
    }

    private void updateAlertForce(){

        ecaLine_read = null;
        // Read the runtime value force
        seuil_curr_read = null;
        if( YmG != 0.0 || XmG != 0.0 )
            seuil_curr_read = readerEPCFile.getForceSeuil(XmG, YmG);
        if( seuil_curr_read != null ) {
            // Compare the runtime value force with the prevent value force
            if( !seuil_curr_read.equals(seuil_prev_read) ) {
                seuil_chrono.start();
            }
            if( seuil_chrono.getSeconds() >= seuil_curr_read.TPS ) {
                seuil_chrono.start();
                Log.d(TAG,"ALERT...." + seuil_curr_read.toString() );
                ecaLine_read = ECALine.newInstance( seuil_curr_read.IDAlert, location, null );
                // Update UI
                if( seuil_ui == null || !seuil_ui.equals(seuil_curr_read) ) {
                    if( listener != null ) listener.onForceChanged( seuil_curr_read.type, seuil_curr_read.level );
                    seuil_ui = seuil_curr_read;
                }

            }
        } else {
            if( seuil_ui != null && seuil_chrono.getSeconds() > 3 ) {
                Log.d(TAG,"ALERT.... ALL IS OK");
                if( listener != null )
                    listener.onForceChanged( FORCE_t.UNKNOW, LEVEL_t.LEVEL_UNKNOW );
                seuil_ui = null;
            }
        }

        if( location != null ) {
            if (ecaLine_read != null) {
                Log.d(TAG, "ADD " + ecaLine_read.toString());
                database.addECA( ecaLine_read );
            } else {
                ecaLine_read = ECALine.newInstance(location, null);
                if (ecaLine_read != null) {
                    if (!ecaLine_read.equals(ecaLine_save)) {
                        if (DataCFG.get_SEND_ALL_GPS_POINTS(ctx)) {
                            Log.d(TAG, "ADD " + ecaLine_read.toString());
                            database.addECA( ecaLine_read );
                            ecaLine_save = ecaLine_read;
                        }
                    }
                }
            }
        }

        seuil_prev_read = seuil_curr_read;

    }

}
