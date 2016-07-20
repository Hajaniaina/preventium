package com.preventium.boxpreventium;

import android.content.Context;
import android.util.Log;

import com.ikalogic.franck.ftp.FtpClientIO;
import com.ikalogic.franck.ftp.listeners.FileTransfertListener;
import com.preventium.boxpreventium.utils.CommonUtils;
import com.preventium.boxpreventium.utils.ConfigUtils;

import org.apache.commons.net.ftp.FTPFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 29/06/2016.
 */

public class InitializeThread extends ThreadDefault implements ThreadDefault.NotifyListener {

    private static final String TAG = "InitializeThread";

    private ThreadMode mode = ThreadMode.NONE;
    private Context context;
    private final DataTransfertCallBack callback;

    public InitializeThread(Context ctx, final DataTransfertCallBack callback) {
        super(null);
        this.context = ctx;
        this.callback = callback;
        super.setNotify( this );
    }

    @Override
    public void myRun() {
        super.myRun();

        long ms = 500;

        callBackChecking( CheckingFlag.PERMISSIONS, CheckingState.CHECHING, 0, ms );
        boolean ready = CommonUtils.havePermissionsReady( context );
        Log.d(TAG, "Checking permissions.................. " + ready );
        callBackChecking( CheckingFlag.PERMISSIONS, ready ? CheckingState.TRUE : CheckingState.FALSE, 0, ms );
        if( !ready ) return;

        callBackChecking( CheckingFlag.BLUETOOTH_SUPPORT, CheckingState.CHECHING, 0, ms );
        ready = CommonUtils.haveBluetoothSupport( context );
        Log.d(TAG, "Checking bluetooth support............ " + ready );
        callBackChecking( CheckingFlag.BLUETOOTH_SUPPORT, ready ? CheckingState.TRUE : CheckingState.FALSE, 0, ms );
        if( !ready ) return;

        callBackChecking( CheckingFlag.BLUETOOTH_LE_SUPPORT, CheckingState.CHECHING, 0, ms );
        ready = CommonUtils.haveBluetoothLESupport( context );
        Log.d(TAG, "Checking bluetooth LE support......... " + ready );
        callBackChecking( CheckingFlag.BLUETOOTH_LE_SUPPORT, ready ? CheckingState.TRUE : CheckingState.FALSE, 0, ms );
        if( !ready ) return;

        callBackChecking( CheckingFlag.LOCATION_SUPPORT, CheckingState.CHECHING, 0, ms );
        ready = CommonUtils.haveLocationSupport( context );
        Log.d(TAG, "Checking location service support..... " + ready );
        callBackChecking( CheckingFlag.LOCATION_SUPPORT, ready ? CheckingState.TRUE : CheckingState.FALSE, 0, ms );
        if( !ready ) return;

        callBackChecking( CheckingFlag.BLUETOOTH_ENABLED, CheckingState.CHECHING, 0, ms );
        if( !CommonUtils.haveBluetoothEnabled() ) CommonUtils.setBluetoothEnabled( true );
        try {Thread.sleep( 1000 ); } catch (InterruptedException e) { e.printStackTrace();}
        ready = CommonUtils.haveBluetoothEnabled();
        Log.d(TAG, "Bluetooth adapter switch ON........... " + ready );
        callBackChecking( CheckingFlag.BLUETOOTH_ENABLED, ready ? CheckingState.TRUE : CheckingState.FALSE, 0, ms );
        if( !ready ) return;

        callBackChecking( CheckingFlag.LOCATION_ENABLED, CheckingState.CHECHING, 0, ms );
        ready = CommonUtils.haveLocationEnabled( context );
        Log.d(TAG, "Location service switch ON............ " + ready );
        callBackChecking( CheckingFlag.LOCATION_ENABLED, ready ? CheckingState.TRUE : CheckingState.FALSE, 0, ms );
        if( !ready ) return;

        callBackChecking( CheckingFlag.INTERNET_ENABLED, CheckingState.CHECHING, 0, ms );
        ready = CommonUtils.haveInternetConnected( context );
        Log.d(TAG, "Checking internet connection.......... " + ready );
        callBackChecking( CheckingFlag.INTERNET_ENABLED, ready ? CheckingState.TRUE : CheckingState.FALSE, 0, ms );
        if( !ready ) return;

        callBackChecking( CheckingFlag.CONFIG_READY, CheckingState.CHECHING, 0, ms );
        ready = ConfigUtils.cfg_exist( context );
        if( !ready ) {
            final String filename = ConfigUtils.server_cfg_filename();
            while (!ready) {
                Log.d(TAG, "Trying to retrieve .CFG file..............." );
                FtpClientIO ftpClientIO = new FtpClientIO();
                if (!ftpClientIO.connectTo(ConfigUtils.server_ftp_config(), 30000)) {
                    Log.w(TAG, "FTP connection timeout!");
                } else {
                    if (!ftpClientIO.changeWorkingDirectory(ConfigUtils.DIR)) {
                        Log.w(TAG, "FTP error while trying to set new current working directory!");
                    } else {
                        FTPFile[] files = ftpClientIO.printFilesList("");
                        for (FTPFile file : files) {
                            if (file.getName().equals(filename)) {
                                Log.d(TAG, "AAAAAAAAAAAAAAAA " + filename + " " + ConfigUtils.get_cfg_path(context));
                                //ready = true;

                                boolean retreive = ftpClientIO.retreiveFile(filename, ConfigUtils.get_cfg_path(context), new FileTransfertListener() {
                                    @Override
                                    public void onStart() {
                                        Log.d(TAG, "AAAAAAAAAAAAAAAA START");
                                    }

                                    @Override
                                    public void onProgress(int percent) {
                                        Log.d(TAG, "AAAAAAAAAAAAAAAA " + percent + " %");
                                    }

                                    @Override
                                    public void onByteTransferred(long totalBytesTransferred, long totalBytes) {

                                    }

                                    @Override
                                    public void onFinish() {
                                        Log.d(TAG, "AAAAAAAAAAAAAAAA FINISH %");
                                    }
                                });
                                Log.d(TAG, "AAAAAAAAAAAAAAAA retreive = " + retreive);

                                if (retreive){
                                    Log.d(TAG, "AAAAAAAAAAAAAAAA sleep retrieve...");
                                    try {
                                        Thread.sleep(30000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    //ftpClientIO.disconnect();
                }

                if (!ready) {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Log.d(TAG, "Checking serveur configuration........ " + ready );
        callBackChecking( CheckingFlag.CONFIG_READY, ready ? CheckingState.TRUE : CheckingState.FALSE, 0, ms );

//        while( !ready ) {
//
//            FtpConfig ftpConfig = new FtpConfig( CommonUtils.FPT_HOST, CommonUtils.FPT_LOGIN, CommonUtils.FPT_PASS );
//            FtpClientIO clientIO = new FtpClientIO();
//            if( !clientIO.connectTo( ftpConfig, 20000 ) ) {
//                Log.d(TAG, "Client connection timeout!");
//            } else {
//                boolean cd = clientIO.changeWorkingDirectory("www/tmp/");
//                if( !cd ) {
//                    Log.d(TAG, "CD cmd return " + cd );
//                } else {
//                    Log.d(TAG, clientIO.printWorkingDirectory());
//
//                    FTPFile[] files = clientIO.printFilesList("");
//                    //Log.d(TAG, "Files count: " + files.length );
//                    for (FTPFile file : files ) {
//                        Log.d(TAG, file.getName() );
//                        if (file.getName().equals(filename) ) {
//                            boolean retreive = clientIO.retreiveFile(filename,CommonUtils.get_cfg_path(context),new FileTransfertListener() {
//
//                                @Override
//                                public void onStart() {
//                                    Log.d(TAG,"Downoading " + filename + " ...");
//                                }
//
//                                @Override
//                                public void onProgress(int percent) {
//                                    Log.d(TAG,"Downloading... " + percent + "%");
//                                }
//
//                                @Override
//                                public void onByteTransferred(long totalBytesTransferred, long totalBytes) {}
//
//                                @Override
//                                public void onFinish() {
//                                    Log.d(TAG,"Downoading " + filename + " finish.");
//                                }
//
//                            });
//                            if( !retreive ) {
//                                Log.d(TAG, "Error while trying to retreive " + filename );
//                            } else {
//                                Log.d(TAG, filename + "is retreive." );
//                                ready = true;
//                            }
//                            break;
//                        }
//                    }
//                }
//            }
//
//            try {
//                Thread.sleep( 30000 );
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            ready = CommonUtils.cfg_exist(context);
//        }

    }

    @Override
    public void onThreadStatusChanged(ThreadStatus status) {
        if( callback != null ) callback.onThreadStatusChanged(status);
    }

    public boolean setInit() {
        boolean ret = false;
        if( isRunning() ) {
            mode = ThreadMode.SET_INIT;
            start();
        }
        return ret;
    }

    public boolean getConfig() {
        boolean ret = false;
        if( isRunning() ) {
            mode = ThreadMode.GET_CONFIG;
            start();
        }
        return ret;
    }

    public interface DataTransfertCallBack {
        void onThreadStatusChanged( ThreadStatus status );
        void onCheckingProgress( CheckingFlag flag, CheckingState status );
    }
    public enum ThreadMode {
        NONE(0),
        SET_INIT(1),
        GET_CONFIG(2);
        private int value;
        private static Map map = new HashMap<>();
        private ThreadMode(int value) {
            this.value = value;
        }
        static {
            for (ThreadMode modeType : ThreadMode.values()) {
                map.put(modeType.value, modeType);
            }
        }
        public static ThreadMode valueOf(int modeType) {
            return (ThreadMode) map.get(modeType);
        }
        public int getValue() {
            return value;
        }
    }
    public enum CheckingFlag {
        PERMISSIONS(0),
        BLUETOOTH_SUPPORT(1),
        BLUETOOTH_LE_SUPPORT(2),
        LOCATION_SUPPORT(3),
        BLUETOOTH_ENABLED(4),
        LOCATION_ENABLED(5),
        INTERNET_ENABLED(6),
        CONFIG_READY(6);
        private int value;
        private static Map map = new HashMap<>();
        private CheckingFlag(int value) {
            this.value = value;
        }
        static {
            for (CheckingFlag modeType : CheckingFlag.values()) {
                map.put(modeType.value, modeType);
            }
        }
        public static CheckingFlag valueOf(int modeType) {
            return (CheckingFlag) map.get(modeType);
        }
        public int getValue() {
            return value;
        }
    }
    public enum CheckingState {
        CHECHING(0),
        TRUE(1),
        FALSE(2);
        private int value;
        private static Map map = new HashMap<>();
        private CheckingState(int value) {
            this.value = value;
        }
        static {
            for (CheckingState modeType : CheckingState.values()) {
                map.put(modeType.value, modeType);
            }
        }
        public static CheckingState valueOf(int modeType) {
            return (CheckingState) map.get(modeType);
        }
        public int getValue() {
            return value;
        }
    }

    private void callBackChecking( CheckingFlag flag, CheckingState status, long before, long after ) {
        try {Thread.sleep( before ); } catch (InterruptedException e) { e.printStackTrace(); }
        if( callback != null ) callback.onCheckingProgress( flag, status );
        try {Thread.sleep( after ); } catch (InterruptedException e) { e.printStackTrace(); }
    }

}
