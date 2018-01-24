package com.preventium.boxpreventium.utils.superclass.ftp;

public interface FileTransfertListener {
    void onByteTransferred(long j, long j2);

    void onFinish();

    void onProgress(int i);

    void onStart();
}
