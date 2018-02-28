package com.preventium.boxpreventium.module;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import com.preventium.boxpreventium.enums.ENGINE_t;
import com.preventium.boxpreventium.module.device.BluetoothBox;
import com.preventium.boxpreventium.module.enums.CONNEXION_STATE_t;
import com.preventium.boxpreventium.module.trames.BatteryInfo;
import com.preventium.boxpreventium.module.trames.SensorShockAccelerometerInfo;
import com.preventium.boxpreventium.module.trames.SensorSmoothAccelerometerInfo;
import com.preventium.boxpreventium.utils.Chrono;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.ThreadDefault;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Franck on 14/09/2016.
 */

public class HandlerBox extends ThreadDefault
        implements DiscoverBox.DiscoverBoxNotify{

    private final static boolean DEBUG = true;
    private final static String TAG = "HandlerBox";

    private  boolean firstscanVirtuel = false;

    private int one_leurre =0;
    private  int nombre_scan_found = 0;
    private int durre_scan_max = 0;

    public interface NotifyListener {
        void onScanState(boolean scanning);
        void onDeviceState( String device_mac, boolean connected );
        void onNumberOfBox(int nb);
        void onForceChanged(Pair<Long,Short> mG_smooth, Pair<Long,Short> mG_shock);
        void onEngineStateChanged(ENGINE_t state);
        void onCalibrateOnConstantSpeed();
        void onCalibrateOnAcceleration();
        void onCalibrateRAZ();

    }

    private Context context = null;
    private DiscoverBox discoverBox = null;
    private NotifyListener listener = null;

    private boolean scanning = false;
    private ArrayList<BluetoothDevice> proximityDevices = new ArrayList<BluetoothDevice>();

    private ArrayList<BluetoothBox> mBoxList = new ArrayList<BluetoothBox>();

    private boolean calibrate_1 = false;
    private boolean calibrate_2 = false;
    private boolean calibrate_3 = false;

    private Pair<Long,Short> last_smooth; // mG, RAW
    private Pair<Long,Short> curr_smooth; // mG, RAW
    private Pair<Long,Short> last_shock; // mG, RAW
    private Pair<Long,Short> curr_shock; // mG, RAW
    private ENGINE_t last_engine_t = ENGINE_t.UNKNOW;

    public HandlerBox(Context ctx, NotifyListener listener) {
        super(null);
        this.context = ctx;
        this.listener = listener;
        this.discoverBox = new DiscoverBox(context,this);
    }
    //by francisco
    public HandlerBox(Context ctx){
        super(null);
        this.context=ctx;
    }
    //---------------//

    public boolean setActive( boolean enable ) {
        if( enable )
            return activate();
        desactivate();
        return true;
    }

    public boolean activate() {
        boolean ret = false;
        if( !isRunning()  ) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HandlerBox.this.run();
                }
            }).start();
            ret = true;
        }
        return ret;
    }

    public void desactivate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HandlerBox.this.setStop();
            }
        }).start();
    }

    public int getNumberOfBoxConnected(){
        int nb = 0;
        for( int i = 0; i < mBoxList.size(); i++ ){
            switch ( mBoxList.get(i).getConnectionState() ){
                case CONNECTING: break;
                case CONNECTED: nb++; break;
                case DISCONNECTED: break;
            }
        }
        return nb;
    }

    public int getNumberOfBoxConnectedOrConnecting(){
        int nb = 0;
        for( int i = 0; i < mBoxList.size(); i++ ){
            switch ( mBoxList.get(i).getConnectionState() ){
                case CONNECTING: nb++; break;
                case CONNECTED: nb++; break;
                case DISCONNECTED: break;
            }
        }
        return nb;
    }

    @Override
    public void nombreDiviceFound(int nombreDivice) {
        nombre_scan_found = nombreDivice;
    }

    public ENGINE_t getLastEngine(){ return last_engine_t; }



    public void on_constant_speed(){ calibrate_1 = true; }

    public void on_acceleration(){ calibrate_2 = true; }

    public void on_raz_calibration() { calibrate_3 = true; }

    @Override
    public void onScanChanged(boolean scanning, ArrayList<BluetoothDevice> devices) {
        this.scanning = scanning;
        this.proximityDevices = devices;
        if( this.listener != null ) listener.onScanState( scanning );
        if( DEBUG ) Log.d(TAG, "on scan state changed: " + scanning );
    }

    @Override
    public void myRun() throws InterruptedException {
        super.myRun();
        //log

        Log.d(TAG, "discovered");
        Chrono chrono = new Chrono();
        chrono.start();
        discoverBox.scan();

        curr_smooth = Pair.create((long)0,(short)0);
        last_smooth = Pair.create((long)0,(short)0);
        curr_shock = Pair.create((long)0,(short)0);
        last_shock = Pair.create((long)0,(short)0);
        last_engine_t = ENGINE_t.UNKNOW;

        int nb = mBoxList.size();
        if( listener != null ) {
            listener.onNumberOfBox( nb ); //santoo
            listener.onForceChanged( last_smooth, last_shock);
            listener.onEngineStateChanged( last_engine_t );
        }

        while( isRunning() ) {

            sleep(1000);

            int active = get_active_from_serveur();

            // WHEN SCANNING
            if( scanning ) { // If scanning is in progress.
                chrono.start(); // Restart chrono who indicate the elapsed time since the last scan
            }

            curr_smooth = Pair.create((long)0,(short)0);
            curr_shock = Pair.create((long)0,(short)0);
            for( int i = mBoxList.size()-1; i >= 0; i-- ) {

                if (mBoxList.get(i).getConnectionState() == CONNEXION_STATE_t.DISCONNECTED) {
                    Log.d(TAG,"deconnecté box");
                    if( listener != null ) listener.onDeviceState( mBoxList.get(i).getMacAddr(), false );
                    if( DEBUG ) Log.d(TAG,"LOST " + mBoxList.get(i).getMacAddr());
                    mBoxList.remove(i);

                } else {
                    SensorSmoothAccelerometerInfo smooth = mBoxList.get(i).getSmooth();
                    Log.d(TAG,"smotth : "+smooth);
                    if( smooth != null ) {
                        if( ComonUtils.difference(0,smooth.value()) >= ComonUtils.difference(0,curr_smooth.first) ) {
                            curr_smooth = Pair.create(smooth.value(),smooth.value_raw());
                        }
                    }

                    SensorShockAccelerometerInfo shock = mBoxList.get(i).getShock();
                    if( shock != null ) {
                        if( ComonUtils.difference(0,shock.value()) >= ComonUtils.difference(0,curr_shock.first) ) {
                            curr_shock = Pair.create(shock.value(),shock.value_raw());
                        }
                    }

                }
            }

            boolean change = false;
            if( !Objects.equals(last_smooth, curr_smooth) ) {
                last_smooth = Pair.create(curr_smooth.first,curr_smooth.second);
                change = true;
            }
            if( !Objects.equals(last_shock, curr_shock) ) {
                last_shock = Pair.create(curr_shock.first,curr_shock.second);
                change = true;
            }
            if( change ) {
                if( listener != null ) listener.onForceChanged( curr_smooth, curr_shock );
            }

            // Calibration 'k': RAZ calibration
            if( calibrate_3 ) {
                if( DEBUG ) Log.d(TAG,"RAZ calibration");
                for( int i = mBoxList.size()-1; i >= 0; i-- )
                    mBoxList.get(i).calibrate_raz();
                calibrate_3 = false;
                if( listener != null ) listener.onCalibrateRAZ();
            }

            // Calibration 'g' on constant speed
            if( calibrate_1 ) {
                if( DEBUG ) Log.d(TAG,"Calibrate triggered by constant speed.");
                for( int i = mBoxList.size()-1; i >= 0; i-- )
                    mBoxList.get(i).calibrate_if_constant_speed();
                calibrate_1 = false;
                if( listener != null ) listener.onCalibrateOnConstantSpeed();
            }

            // Calibration 'o' on acceleration
            if( calibrate_2 ) {
                if( DEBUG ) Log.d(TAG,"Calibrate triggered by acceleration.");
                for( int i = mBoxList.size()-1; i >= 0; i-- )
                    mBoxList.get(i).calibrate_if_acceleration();
                calibrate_2 = false;
                if( listener != null ) listener.onCalibrateOnAcceleration();
            }

            // Battery info
            ENGINE_t engine_t = ENGINE_t.OFF;
            if( !mBoxList.isEmpty() ) {
                BatteryInfo bat = mBoxList.get(0).getBat();
                if( bat != null ) engine_t = (bat.running()) ? ENGINE_t.ON : ENGINE_t.OFF;
            }
            if( listener != null && engine_t != last_engine_t ) {
                listener.onEngineStateChanged(engine_t);
                last_engine_t = engine_t;
            }
            // WHEN NOT SCANNING
            if( !scanning ) {
                if( DEBUG ) Log.d(TAG,"scan :" +scanning);
                // If the result of the scan is not empty
                if( !proximityDevices.isEmpty() ) {
                    // Trying to connect to the BoxPreventium devices
                    for( int i = 0; i < proximityDevices.size(); i++ ){
                        add( proximityDevices.get(i) );
                    }
                    proximityDevices.clear();
                    if(one_leurre<=1){
                        orderBox();
                    }

                    //setSacningState(true);

                } else {
                    // Wainting before rescan
                    Log.d(TAG,"Seconde :" +  chrono.getSeconds());
                    Log.d(TAG,"minute :" +  chrono.getMinutes());
                    Log.d(TAG, "  nombre boitier : "+ getNumberOfBoxConnected());
                    if( (mBoxList.size() == 0 && chrono.getSeconds() > 15.0)
                            || (mBoxList.size() == 1 && chrono.getMinutes() > 1.0)
                            || (mBoxList.size() == 2 && chrono.getMinutes() > 3.0)
                            ) {


                        Log.d(TAG,"Rescan box   " +one_leurre );

                        discoverBox.scan(); // Restart scanning

                        Log.d(TAG,"Box found    " +nombre_scan_found );

                        if(active == 1 && nombre_scan_found==0){
                            if(bleutoothVirtuelLeurre()) {
                                firstscanVirtuel = true;
                                one_leurre++;
                                Log.d(TAG, "Box virtuel bien cree " + active);
                            }
                        }

                    }
                }
            }
            //si non leurre et non device
            if(nombre_scan_found ==0 && active ==0 && chrono.getSeconds() > 30.0 ){
               // HandlerBox.this.setStop();
               /// listener.onEngineStateChanged(ENGINE_t.OFF);
                Log.d(TAG,"STOP because no leurre and no divice");
            }
            // NUMBER OF CONNECTED DEVICE CHANGED
            if( nb != mBoxList.size() ) {
                nb = mBoxList.size();
                if( listener != null ) listener.onNumberOfBox( nb );  //sss
                //si leurre active et divice not found//
                if(nombre_scan_found ==0 && active ==1 && listener != null ){
                    listener.onNumberOfBox( 0 );
                }
                if( DEBUG ) Log.d(TAG,"Number of connected device changed: " + nb );
            }
        }

        discoverBox.stop();
        // DISCONNECT ALL DEVICE
        while( nb > 0 ) {
            for (int i = mBoxList.size() - 1; i >= 0; i--) {
                if (mBoxList.get(i).getConnectionState() == CONNEXION_STATE_t.DISCONNECTED) {
                    if (listener != null)
                        listener.onDeviceState(mBoxList.get(i).getMacAddr(), false);
                    if (DEBUG) Log.d(TAG, "LOST " + mBoxList.get(i).getMacAddr());
                    mBoxList.remove(i);
                } else {
                    mBoxList.get(i).close();
                }
            }
            nb = mBoxList.size();
            if( nb > 0 ) sleep(200);
            // NUMBER OF CONNECTED DEVICE CHANGED
            if( nb != mBoxList.size() ) {
                nb = mBoxList.size();
                if( listener != null ) listener.onNumberOfBox( nb ); //sss
                if( DEBUG ) Log.d(TAG,"Number of connected device changed: " + nb );
            }
        }
    }

    private void add( BluetoothDevice device ) throws InterruptedException {
        if( mBoxList.size() < 3 ) {
            BluetoothBox box = new BluetoothBox(context);
            if( listener != null ) listener.onDeviceState( device.getAddress(), true );
            if( DEBUG ) Log.d(TAG,"FIND " + device.getAddress() );

            // On ferme l'ancienne instance de ce divice s'il est déjà dans la list
            // (car si il existe déjà, c'est qu'il n'est plus connecté mais que sont status n'est
            // pas encore actualisé, donc on le ferme et on le supprime, pour eviter des conflit)

            for( int i = mBoxList.size()-1; i >= 0; i-- ){
                if( mBoxList.get(i).getMacAddr().equals(device.getAddress()) ) {
                    mBoxList.get(i).close();
                    while( isRunning() && mBoxList.get(i).getConnectionState() != CONNEXION_STATE_t.DISCONNECTED ) sleep(50);
                    mBoxList.remove(i);
                }
            }

            // Connection + ajout à la liste du nouveau device
            box.connect(device);
            if(firstscanVirtuel){

                box.connect_leurre(device);
            }
            while( isRunning() && box.getConnectionState() == CONNEXION_STATE_t.CONNECTING ) sleep(50);
            if( box.getConnectionState() == CONNEXION_STATE_t.CONNECTED ) {
                mBoxList.add(box);
                if( DEBUG ) Log.d(TAG,"ADDED " + device.getAddress() );
            }
        }
    }

    private void orderBox() throws InterruptedException {

        if (mBoxList.size() > 1) {

            // Update RSSI if needed
            boolean rssi_ok = false;
            while (isRunning() && !rssi_ok) {
                sleep(1000);
                rssi_ok = true;
                for (int i = 0; i < mBoxList.size(); i++) {
                    if (mBoxList.get(i).getRSSI() == null) {
                        rssi_ok = false;
                        mBoxList.get(i).readRSSI();
                    }
                }
            }

            // Order device
            if (isRunning()) {
                // ORDER RSSI BY asc ...
                boolean tab_en_ordre = false;
                int taille = mBoxList.size();
                while (!tab_en_ordre) {
                    tab_en_ordre = true;
                    for (int i = 0; i < taille - 1; i++) {
                        if (mBoxList.get(i).getRSSI() < mBoxList.get(i + 1).getRSSI()) {
                            BluetoothBox tmp_device = mBoxList.get(i);
                            mBoxList.set(i, mBoxList.get(i + 1));
                            mBoxList.set(i + 1, tmp_device);
                            tab_en_ordre = false;
                        }
                    }
                    taille--;
                }
            }
        }
    }

    //By francisco

    public boolean bleutoothVirtuelLeurre(){
        try{
            BluetoothDevice Bdivice = null;
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            adapter.setName("Preventium");
            Bdivice = adapter.getRemoteDevice(adapter.getAddress());
            //Bdivice.
            BluetoothBox box = new BluetoothBox(context);
            box.setConnectionState( CONNEXION_STATE_t.CONNECTED);

            listener.onEngineStateChanged(ENGINE_t.ON);

            ArrayList<BluetoothDevice> devices = new ArrayList<>();
            //proximityDevices.clear();
            //mBoxList.clear();
            devices.add(Bdivice);

            onScanChanged(false,devices);


            //box.connect(Bdivice);
            // mBoxList.add(box);
            return  true;

        }catch (Exception e){
            Log.e("Erreur de box virtuel", e.getMessage());
            return false;
        }
    }

    public void setSacningState(boolean scanNew){
        scanning = scanNew;
    }

    public int get_active_from_serveur(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("leurre", Context.MODE_PRIVATE);
        int value =sharedPreferences.getInt("value_leurre",0);
        return value;
    }
    public void set_active_from_serveur(int new_option){
        SharedPreferences sharedPreferences = context.getSharedPreferences("leurre", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("value_leurre",new_option);
        editor.apply();
    }
    //-------- fin francisco----------//
}

