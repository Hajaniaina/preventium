package com.preventium.boxpreventium.manager;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.location.AlertForce;
import com.preventium.boxpreventium.module.HandlerBox;
import com.preventium.boxpreventium.server.CFG.DataCFG;
import com.preventium.boxpreventium.server.EPC.ForceSeuil;
import com.preventium.boxpreventium.server.EPC.ReaderEPCFile;
import com.preventium.boxpreventium.server.FilesDownloader;
import com.preventium.boxpreventium.utils.Chrono;
import com.preventium.boxpreventium.utils.ThreadDefault;

import java.util.Locale;

/**
 * Created by Franck on 23/09/2016.
 */

public class Manager extends ThreadDefault
    implements FilesDownloader.FilesDowloaderListener, HandlerBox.NotifyListener{

    private final static String TAG = "Manager";
    private final static boolean DEBUG = true;

    public interface ManagerListener {
        void onNumberOfBoxChanged( int nb );
        void onChronoRideChanged( String txt );
    }

    private Context ctx = null;
    private ManagerListener listener = null;
    private FilesDownloader downloader = null;
    private ReaderEPCFile readerEPCFile = new ReaderEPCFile();
    private HandlerBox modules = null;

    private FilesDownloader.MODE_t mode = FilesDownloader.MODE_t.NONE;

    private double XmG = 0.0;
    private double YmG = 0.0;
    private Location location = null;
    private Location lastLocation = null;

    private Chrono chronoRide = new Chrono();
    private String chronoRideTxt = "";

    private AlertForce alertForce = null;

    public Manager(Context ctx, ManagerListener listener) {
        super(null);
        this.ctx = ctx;
        this.listener = listener;
        this.downloader = new FilesDownloader(ctx,this);
        this.modules = new HandlerBox(ctx,this);
    }

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

    @Override
    public void myRun() throws InterruptedException {
        super.myRun();

        if( DEBUG ) Log.d(TAG,"Manager thread begin.");

        lastLocation = null;
        chronoRideTxt = "";

        modules.setActive( true );
        chronoRide.start();

        run_get_cfg();
        run_get_epc();

        while ( isRunning() ) {

            sleep(100);
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
        if( DEBUG ) Log.d(TAG,"Manager thread end.");

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
    public void onNumberOfBox(int nb) {
        if( DEBUG ) Log.d(TAG,"Number of preventium device connected changed: " + nb );
        if( listener != null ) listener.onNumberOfBoxChanged( nb );
    }

    @Override
    public void onForceChanged(double mG) {
        if( DEBUG ) Log.d(TAG,"mG = " + mG );
        this.YmG = mG;
    }

    // PRIVATE

    private void run_get_cfg() throws InterruptedException {
        if( DEBUG ) Log.d(TAG," Download CFG... BEGIN");
        while( isRunning() && DataCFG.getFptConfig(ctx) == null ) {
            downloader.downloadCFG();
            sleep(1000);
        }
        Log.d(TAG, "Download CFG... STOP" );
    }

    private void run_get_epc() throws InterruptedException {
        if( DEBUG ) Log.d(TAG," Download EPC... BEGIN");
        boolean read = readerEPCFile.read( ctx, 1 );
        while( isRunning() && !read ) {
            downloader.downloadEPC();
            sleep(1000);
        }
        Log.d(TAG, "Download EPC... STOP" );
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
        
        if( YmG != 0.0 && XmG != 0.0 ) {
            Log.d(TAG, "XmG = " + XmG + " / YmG = " + YmG);
            ForceSeuil seuil = readerEPCFile.getForceSeuil(XmG, YmG);
            if (seuil != null) Log.d(TAG, seuil.toString());
        }
    }


}
