package com.preventium.boxpreventium.bluetooth.device;

/**
 * Created by Franck on 08/08/2016.
 */

public interface ActionCallback {
    public void onSuccess(Object data);
    public void onFail(int errorCode, String msg);
}
