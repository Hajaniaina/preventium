package com.preventium.boxpreventium.ftp;

import android.util.Log;

import org.apache.commons.net.ftp.*;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Franck on 31/05/2016.
 */

public class FtpClientIO {

    private final static String TAG = "FtpClientIO";
    private FTPClient client;

    public FtpClientIO() { }

    // Connect

    public boolean connectTo( String hostname, String username, String password, int timeout ) {
        FtpConfig config = new FtpConfig(hostname,username,password);
        return connectTo( config, timeout );
    }

    public boolean connectTo( String hostname, int port, String username, String password, int timeout ) {
        FtpConfig config = new FtpConfig(hostname,username,password,port);
        return connectTo( config, timeout );
    }

    public boolean connectTo( FtpConfig config, int timeout ) {
        boolean ret = false;
        try {
            client = new FTPClient();
            client.setConnectTimeout(timeout);
            client.connect( config.getFtpServer(), config.getPort() );
            int reply = client.getReplyCode();
            if( !FTPReply.isPositiveCompletion(reply) ) {
                client.disconnect();
                Log.d(TAG,"Connexion refusée.");
            } else {
                if( client.login( config.getUsername(), config.getPassword() ) ){
                    ret = true;
                } else {
                    Log.d(TAG,"Login refusée.");
                    client.disconnect();
                }
            }
            System.out.println("status :: " + client.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public boolean isConnected() {
        boolean ret = false;
        if( client != null ) ret = client.isConnected();
        return ret;
    }

    public void disconnect() {
        if( client != null ) {
            if (this.client.isConnected()) {
                try {
                    this.client.logout();
                    this.client.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String printWorkingDirectory() {
        String ret = "";
        if( isConnected() ) {
            try {
                ret = client.printWorkingDirectory();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public FTPFile[] printFilesList(String dir_path) {
        FTPFile[] ftpFiles = null;
        if( isConnected() ) {
            try {
                ftpFiles = client.listFiles(dir_path);
                int length = ftpFiles.length;
                for (int i = 0; i < length; i++) {
                    String name = ftpFiles[i].getName();
                    boolean isFile = ftpFiles[i].isFile();
                    if (isFile) Log.i(TAG, "File : " + name);
                    else Log.i(TAG, "Directory : " + name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ftpFiles;
    }

    public boolean changeToParentDirectory() {
        boolean ret = false;
        if( isConnected() ) {
            try {
                ret = client.changeToParentDirectory();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean changeWorkingDirectory(String dir_path) {
        boolean ret = false;
        if( isConnected() ) {
            try {
                ret = client.changeWorkingDirectory(dir_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean makeDirectory(String dir_path) {
        boolean ret = false;
        if( isConnected() ) {
            try {
                ret = client.makeDirectory(dir_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean removeDirectory(String dir_path) {
        boolean ret = false;
        if( isConnected() ) {
            try {
                ret = client.removeDirectory(dir_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean deleteFile(String file_path) {
        boolean ret = false;
        if( isConnected() ) {
            try {
                ret = client.deleteFile(file_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean renameFile(String from, String to) {
        boolean ret = false;
        if( isConnected() ) {
            try {
                ret = client.rename(from,to);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}
