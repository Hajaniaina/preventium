package com.preventium.boxpreventium.utils.superclass.bluetooth.device;

public interface ActionCallback {
    public void onSuccess(Object data);
    public void onFail(int errorCode, String msg);
}
