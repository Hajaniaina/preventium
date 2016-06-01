package com.preventium.boxpreventium;

import android.util.Log;

import com.preventium.boxpreventium.ftp.FtpClientIO;
import com.preventium.boxpreventium.ftp.FtpConfig;
import com.preventium.boxpreventium.ftp.listeners.FileTransfertListener;

public class FtpPreventium extends Thread{

    private final static String TAG = "FtpPreventium";
    private FtpClientIO clientIO;

    public FtpPreventium(){}

    @Override
    public void run() {

        FtpConfig config = new FtpConfig("ftp.ikalogic.com","ikalogic","Tecteca1");
        clientIO = new FtpClientIO();
        if ( clientIO.connectTo(config,20000) ) {
            Log.d("TEST","FTP CONNECTING...OK");

            Log.d("TEST","=================");
            clientIO.printWorkingDirectory();
            clientIO.printFilesList("");
            Log.d("TEST","=================");
            boolean cd = clientIO.changeWorkingDirectory("www/tmp/");
            Log.d("TEST","cd = " + cd );
            clientIO.printWorkingDirectory();
            clientIO.printFilesList("");

            boolean store = clientIO.storeFile("/storage/emulated/0/Download/WAV/tank/anti char.wav", "anti_char.wav", new FileTransfertListener() {
                @Override
                public void onStart() {
                    Log.d(TAG,"Upload start");
                }

                @Override
                public void onProgress(int percent) {
                    Log.d(TAG,"Uploading... " + percent + "%");
                }

                @Override
                public void onByteTransferred(long totalBytesTransferred, long totalBytes) {
                    Log.d(TAG,"Uploading... (" + totalBytesTransferred + " / " + totalBytes + ")" );
                }

                @Override
                public void onFinish() {
                    Log.d(TAG,"Upload finish");
                }
            });
            Log.d("TEST","store = " + store );


            boolean retreive = clientIO.retreiveFile("anti_char.wav", "/storage/emulated/0/Download/WAV/tank/anti_char_1.wav", new FileTransfertListener() {
                @Override
                public void onStart() {
                    Log.d(TAG,"Download start");
                }

                @Override
                public void onProgress(int percent) {
                    Log.d(TAG,"Downloading... " + percent + "%");
                }

                @Override
                public void onByteTransferred(long totalBytesTransferred, long totalBytes) {
                    Log.d(TAG,"Downloading... (" + totalBytesTransferred + " / " + totalBytes + ")" );
                }

                @Override
                public void onFinish() {
                    Log.d(TAG,"Downloading finish");
                }
            });
            Log.d("TEST","retreive = " + retreive );

        }else{
            Log.d("TEST","FTP CONNECTING...FAIL!");
        }
        clientIO.disconnect();
        Log.d("TEST","FTP CONNECTED:"+clientIO.isConnected());
    }


}
