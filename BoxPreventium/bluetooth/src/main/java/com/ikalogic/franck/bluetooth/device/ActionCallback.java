package com.ikalogic.franck.bluetooth.device;

/**
 * Created by franck on 6/18/16.
 */
public interface ActionCallback {
    public void onSuccess(Object data);
    public void onFail(int errorCode, String msg);
}
