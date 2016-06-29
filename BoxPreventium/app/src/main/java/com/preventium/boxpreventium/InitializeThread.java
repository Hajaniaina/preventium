package com.preventium.boxpreventium;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franck on 29/06/2016.
 */

public class InitializeThread extends ThreadDefault implements ThreadDefault.NotifyListener {

    private static final String TAG = "InitializeThread";
    private static final String HOST = "ftp.ikalogic.com";
    private static final String LOGIN = "ikalogic";
    private static final String PASS = "Tecteca1";
    private static final String filename = "test.CFG";

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
    }

    @Override
    public void onThreadStatusChanged(ThreadStatus status) {
        if( callback != null ) callback.onThreadStatusChanged(status);
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
        void onThreadStatusChanged(ThreadStatus status);
    }
    public enum ThreadMode {
        NONE(0),
        GET_CONFIG(4);
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
}
