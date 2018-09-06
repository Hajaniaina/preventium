package com.preventium.boxpreventium.module.device;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import com.preventium.boxpreventium.module.device.BluetoothBoxIO.TramesNotifyListener;
import com.preventium.boxpreventium.module.enums.CMD_t;
import com.preventium.boxpreventium.module.enums.CONNEXION_STATE_t;
import com.preventium.boxpreventium.module.trames.BatteryInfo;
import com.preventium.boxpreventium.module.trames.SensorShockAccelerometerInfo;
import com.preventium.boxpreventium.module.trames.SensorSmoothAccelerometerInfo;
import com.preventium.boxpreventium.utils.superclass.bluetooth.device.ActionCallback;
import com.preventium.boxpreventium.utils.superclass.bluetooth.device.NotifyListener;

public class BluetoothBox implements ActionCallback, TramesNotifyListener, NotifyListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "BluetoothBox";
    private BluetoothBoxIO io = null;
    private BatteryInfo last_battery_info = null;
    private Integer last_rssi = null;
    private SensorShockAccelerometerInfo last_shock_info = null;
    private SensorSmoothAccelerometerInfo last_smooth_info = null;
    private CONNEXION_STATE_t state = CONNEXION_STATE_t.DISCONNECTED;
    private boolean leurre = false;

    public boolean getIsLeurre () {
        return this.leurre;
    }

    public void setLeurre (boolean leurre) {
        this.leurre = leurre;
    }

    class C01161 implements Runnable {
        C01161() {
        }

        public void run() {
            BluetoothBox.this.io.command(CmdBox.newInstance(CMD_t.PAUSE_MEASURING));
            BluetoothBox.this.io.disconnect();
        }
    }

    class C01172 implements Runnable {
        C01172() {
        }

        public void run() {
            BluetoothBox.this.io.command(CmdBox.newInstance(CMD_t.FIRST_CALIBRATION));
        }
    }

    class C01183 implements Runnable {
        C01183() {
        }

        public void run() {
            BluetoothBox.this.io.command(CmdBox.newInstance(CMD_t.SECOND_CALIBRATION));
        }
    }
    //By Francisco
    public void connect_leurre(BluetoothDevice device){
        updateState( CONNEXION_STATE_t.CONNECTED );
        io.connect( device, this );
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                io.setTramesListener( BluetoothBox.this );
                io.command(CmdBox.newInstance(CMD_t.START_MEASURING));
                Log.d("HandlerBox","Connect leurre");
            }
        }).start();
        readRSSI();*/
    }
    //Fin Modif Francisco

    class C01194 implements Runnable {
        C01194() {
        }

        public void run() {
            BluetoothBox.this.io.command(CmdBox.newInstance(CMD_t.RAZ_CALIBRATION));
        }
    }

    class C01205 implements Runnable {
        C01205() {
        }

        public void run() {
            BluetoothBox.this.io.setTramesListener(BluetoothBox.this);
            BluetoothBox.this.io.command(CmdBox.newInstance(CMD_t.START_MEASURING));
        }
    }

    class C01226 implements Runnable {

        class C01211 implements ActionCallback {
            C01211() {
            }

            public void onSuccess(Object data) {
                BluetoothBox.this.last_rssi = (Integer) data;
            }

            public void onFail(int errorCode, String msg) {
            }
        }

        C01226() {
        }

        public void run() {
            BluetoothBox.this.io.readRssi(new C01211());
        }
    }

    public BluetoothBox(Context context) {
        this.io = new BluetoothBoxIO(context);
        this.io.setDisconnectedListener(this);

        if( this.io.readAddress().toString() != "" )
            this.leurre = false;
    }

    public void connect(BluetoothDevice device) {
        updateState(CONNEXION_STATE_t.CONNECTING);
        this.io.connect(device, (ActionCallback) this);
    }

    public void connect(String address) {
        updateState(CONNEXION_STATE_t.CONNECTING);
        this.io.connect(address, (ActionCallback) this);
    }

    public void close() {
        new Thread(new C01161()).start();
    }

    public void calibrate_if_constant_speed() {
        new Thread(new C01172()).start();
    }

    public void calibrate_if_acceleration() {
        new Thread(new C01183()).start();
    }

    public void calibrate_raz() {
        new Thread(new C01194()).start();
    }

    public CONNEXION_STATE_t getConnectionState() {
        return this.state;
    }

    public String getMacAddr() {
        String ret = "";
        if (this.io == null) {
            return ret;
        }
        BluetoothDevice dev = this.io.getDevice();
        if (dev != null) {
            return dev.getAddress();
        }
        return ret;
    }

    public Integer getRSSI() {
        if (this.last_rssi == null) {
            readRSSI();
        }
        return this.last_rssi;
    }

    public BatteryInfo getBat() {
        return this.last_battery_info;
    }

    public SensorSmoothAccelerometerInfo getSmooth() {
        return this.last_smooth_info;
    }

    public SensorShockAccelerometerInfo getShock() {
        return this.last_shock_info;
    }

    public void onSuccess(Object data) {
        updateState( CONNEXION_STATE_t.CONNECTED );
        new Thread(new Runnable() {
            @Override
            public void run() {
                io.setTramesListener( BluetoothBox.this );
                io.command(CmdBox.newInstance(CMD_t.START_MEASURING));
                Log.d("HandlerBox","succees");
            }
        }).start();
        updateState(CONNEXION_STATE_t.CONNECTED);
        new Thread(new C01205()).start();
        readRSSI();
    }

    public void onFail(int errorCode, String msg) {
        updateState( CONNEXION_STATE_t.DISCONNECTED );
        Log.d("HandlerBox","faill be:" + msg);
        // updateState(CONNEXION_STATE_t.DISCONNECTED);
    }

    public void onBatteryInfoReceived(BatteryInfo info) {
        this.last_battery_info = info;
    }

    public void onShockInfoReceived(SensorShockAccelerometerInfo info) {
        this.last_shock_info = info;
    }

    public void onSmoothInfoReceived(SensorSmoothAccelerometerInfo info) {
        this.last_smooth_info = info;
    }

    public void readRSSI() {
        new Thread(new C01226()).start();
    }

    public void onNotify(byte[] data) {
        updateState(CONNEXION_STATE_t.DISCONNECTED);
    }

    private void updateState(CONNEXION_STATE_t state) {
        this.state = state;
        Log.d(TAG, "State changed: " + state);
    }

    public void setConnectionState(CONNEXION_STATE_t state){
        this.state = state;
    }
}
