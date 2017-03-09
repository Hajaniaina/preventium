package com.preventium.boxpreventium.gui;

import android.location.Location;
import com.preventium.boxpreventium.location.PositionManager;

public class ModeManager {

    private static final String TAG = "ModeManager";

    public static final int STOP   = 0;
    public static final int PAUSE  = 1;
    public static final int MOVING = 2;

    private int currMode = STOP;
    private PositionManager posManager;
    private ModeChangeListener modeChangeListener;

    public interface ModeChangeListener {

        public void onModeChanged (int mode);
        public void onStopMode();
        public void onPauseMode();
        public void onMovingMode();
    }

    public ModeManager (PositionManager manager) {

        if (manager != null) {

            posManager = manager;

            posManager.setPositionChangedListener(new PositionManager.PositionListener() {

                @Override
                public void onPositionUpdate(Location prevLoc, Location currLoc) {

                }

                @Override
                public void onRawPositionUpdate (Location location) {

                }

                @Override
                public void onGpsStatusChange (boolean gpsFix) {

                }
            });

            posManager.setMovingListener(new PositionManager.MovingStateListener() {

                @Override
                public void onStartMoving() {

                    setMode(MOVING);
                }

                @Override
                public void onStopMoving() {

                    setMode(STOP);
                }
            });
        }
    }

    public void setMode (int mode) {

        if (currMode != mode) {

            currMode = mode;

            if (modeChangeListener != null) {

                modeChangeListener.onModeChanged(mode);

                switch (currMode) {

                    case STOP: modeChangeListener.onStopMode();
                        break;

                    case PAUSE: modeChangeListener.onPauseMode();
                        break;

                    case MOVING: modeChangeListener.onMovingMode();
                        break;
                }
            }
        }
    }

    public int getMode() {

        return currMode;
    }

    public void setModeChangeListener (ModeChangeListener listener) {

        modeChangeListener = listener;
    }
}
