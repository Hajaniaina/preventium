package com.preventium.boxpreventium;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Franck on 29/06/2016.
 */

public class ThreadDefault extends Thread{

    private static final String TAG = "ThreadDefault";
    private NotifyListener notify;
    private AtomicBoolean isThreadRunning = new AtomicBoolean(false);
    private AtomicBoolean isThreadPausing = new AtomicBoolean(false);

    public ThreadDefault( NotifyListener notify ) {
        super(TAG);
        setNotify( notify );
    }

    public void setNotify( NotifyListener notify ) { this.notify = notify; }

    public boolean isRunning() { return isThreadRunning.get(); };

    public boolean isPausing() { return (isThreadRunning.get() && isThreadPausing.get()); }

    public void setPause(){
        this.isThreadRunning.set(true);
        this.isThreadPausing.set(true);
    }

    public void setResume() {
        this.isThreadRunning.set(true);
        this.isThreadPausing.set(false);
    }

    public void setStop() {
        if( isThreadRunning.getAndSet(false) ) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.isThreadPausing.set(false);
    }

    @Override
    public void run() {
        if( notify != null ) notify.onThreadStatusChanged( ThreadStatus.START );
        isThreadRunning.set(true);
        isThreadPausing.set(false);
        myRun();
        isThreadRunning.set(false);
        isThreadPausing.set(false);
        if( notify != null ) notify.onThreadStatusChanged( ThreadStatus.FINISH );
        interrupt();
    }

    private void runPause() {
        if( isThreadPausing.get() == true ) {
            if( notify != null ) notify.onThreadStatusChanged( ThreadStatus.PAUSE );
            while ( isThreadPausing.get() == true ) {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if( notify != null ) notify.onThreadStatusChanged( ThreadStatus.RESUME );
        }
    }

    public void myRun() {
    }

    public interface NotifyListener {
        void onThreadStatusChanged(ThreadStatus status);
    }
    public enum ThreadStatus {
        START(0),
        PAUSE(1),
        RESUME(2),
        FINISH(3);
        private int value;
        private static Map map = new HashMap<>();
        private ThreadStatus(int value) {
            this.value = value;
        }
        static {
            for (ThreadStatus statusType : ThreadStatus.values()) {
                map.put(statusType.value, statusType);
            }
        }
        public static ThreadStatus valueOf(int statusType) {
            return (ThreadStatus) map.get(statusType);
        }
        public int getValue() {
            return value;
        }
    }

}

