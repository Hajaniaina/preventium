package com.preventium.boxpreventium.manager.interfaces;

import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.SCORE_t;
import com.preventium.boxpreventium.enums.SPEED_t;
import com.preventium.boxpreventium.enums.STATUS_t;

/**
 * Created by tog on 02/12/2018.
 */

public interface AppManagerListener {

    void onCalibrateOnAcceleration();

    void onCalibrateOnConstantSpeed();

    void onCalibrateRAZ();

    void onCustomMarkerDataListGet();

    void onDebugLog(String str);

    void onDrivingTimeChanged(String str);

    void onForceChanged(FORCE_t fORCE_t, LEVEL_t lEVEL_t, double d, float f, float f2);

    void onForceDisplayed(double d);

    void onInternetConnectionChanged();

    void onLevelNotified(LEVEL_t lEVEL_t);

    void onNoteChanged(int i, LEVEL_t lEVEL_t, LEVEL_t lEVEL_t2);

    void onNumberOfBoxChanged(int i, boolean isBM);

    void onRecommendedSpeedChanged(SPEED_t sPEED_t, int i, LEVEL_t lEVEL_t, boolean z);

    void onScoreChanged(SCORE_t sCORE_t, LEVEL_t lEVEL_t);

    void onShock(double d, short s);

    void onSpeedCorner();

    void onSpeedCornerKept(int i, LEVEL_t lEVEL_t);

    void onSpeedLine();

    void onSpeedLineKept(int i, LEVEL_t lEVEL_t);

    void onStatusChanged(STATUS_t sTATUS_t);

    void onUiTimeout(int i, STATUS_t sTATUS_t);

    void onLastAlertData();

    void onCrash();

    void checkRestart();
}
