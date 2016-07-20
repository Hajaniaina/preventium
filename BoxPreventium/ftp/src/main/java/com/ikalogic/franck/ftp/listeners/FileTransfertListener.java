package com.ikalogic.franck.ftp.listeners;

/**
 * Created by Franck on 28/06/2016.
 */

public interface FileTransfertListener {
    void onStart();
    void onProgress(int percent);
    void onByteTransferred(long totalBytesTransferred, long totalBytes);
    void onFinish();
}
