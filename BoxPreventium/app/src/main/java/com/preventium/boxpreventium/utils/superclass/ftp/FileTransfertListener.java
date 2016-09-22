package com.preventium.boxpreventium.utils.superclass.ftp;

/**
 * Created by Franck on 21/09/2016.
 */

public interface FileTransfertListener {
    void onStart();
    void onProgress(int percent);
    void onByteTransferred(long totalBytesTransferred, long totalBytes);
    void onFinish();
}
