package com.preventium.boxpreventium.ftp.listeners;

/**
 * Created by Franck on 01/06/2016.
 */

public interface FileTransfertListener {
    void onStart();
    void onProgress(int percent);
    void onByteTransferred(long totalBytesTransferred, long totalBytes);
    void onFinish();
}
