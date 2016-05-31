package com.preventium.boxpreventium.connectivity.listeners;

/**
 * Created by Franck on 31/05/2016.
 */

public interface NotifyListener {
    void onNotify(int status);
    void onBluetoothStateChanged(int state);
    void onWifiStateChanged(int state);
    void onConnectivityChanged(boolean no_connectivity);
}
