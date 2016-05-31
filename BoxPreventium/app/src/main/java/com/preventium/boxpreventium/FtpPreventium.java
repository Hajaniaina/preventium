package com.preventium.boxpreventium;

import android.util.Log;

import com.preventium.boxpreventium.ftp.FtpClientIO;
import com.preventium.boxpreventium.ftp.FtpConfig;

public class FtpPreventium extends Thread{

    private FtpClientIO clientIO;

    public FtpPreventium(){

    }

    @Override
    public void run() {

        FtpConfig config = new FtpConfig("ftp.ikalogic.com","ikalogic","Tecteca1");
        clientIO = new FtpClientIO();
        if ( clientIO.connectTo(config,20000) ) {
            Log.d("TEST","FTP CONNECTING...OK");

            clientIO.printWorkingDirectory();
            clientIO.printFilesList("");

        }else{
            Log.d("TEST","FTP CONNECTING...FAIL!");
        }
        clientIO.disconnect();
        Log.d("TEST","FTP CONNECTED:"+clientIO.isConnected());
    }


}
