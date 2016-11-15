package com.preventium.boxpreventium.utils.superclass.ftp;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamAdapter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Franck on 21/09/2016.
 */

public class FTPClientIO {

    private final static String TAG = "FTPClientIO";

    private FTPClient mFTPClient = null;
    private CopyStreamAdapter streamListener;

    public boolean ftpConnect(FTPConfig config, int timeout ) {
        return ftpConnect( config.getFtpServer(), config.getUsername(),
                config.getPassword(), config.getPort(), timeout );
    }

    public boolean ftpConnect(String host, String username, String password, int port, int timeout) {
        boolean ret = false;
        try {
            mFTPClient = new FTPClient();
            // Set connection timeout
            mFTPClient.setConnectTimeout( timeout );
            // connecting to the host
            mFTPClient.connect(host, port);
            // now check the reply code, if positive mean connection success
            if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
                // login using username & password
                boolean status = mFTPClient.login(username, password);
                /*
                * Set File Transfer Mode
                * To avoid corruption issue you must specified a correct
                * transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,
                * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE for
                * transferring text, image, and compressed files.
                */
                mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                mFTPClient.enterLocalPassiveMode();
                ret = status;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error: could not connect to host " + host);
        }
        return ret;
    }

    public boolean ftpDisconnect() {
        try {
            mFTPClient.logout();
            mFTPClient.disconnect();
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Error occurred while disconnecting from ftp server.");
        }
        return false;
    }

    public boolean ftpIsConnected() { return mFTPClient != null && mFTPClient.isConnected(); }

    public boolean checkFileExists(String filePath) {
        boolean fileExists = false;
        InputStream inputStream = null;
        try {
            inputStream = mFTPClient.retrieveFileStream(filePath);
            fileExists = inputStream != null && mFTPClient.getReplyCode() != 550;
            if (inputStream != null) {
                inputStream.close();
                mFTPClient.completePendingCommand();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileExists;
    }

    public String ftpPrintWorkingDirectory() {
        String ret = "";
        if( ftpIsConnected() ) {
            try {
                ret = mFTPClient.printWorkingDirectory();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public FTPFile[] ftpPrintFiles() {
        FTPFile[] ret = null;
        if( ftpIsConnected() ) {
            try {
                ret = mFTPClient.listFiles();
                if( ret != null && ret.length > 0 ) {
                    for( int i = 0; i < ret.length; i++ ) {
                        String name = ret[i].getName();
                        boolean isFile = ret[i].isFile();
                        if (isFile) Log.i(TAG, "File : " + name);
                        else Log.i(TAG, "Directory : " + name);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public FTPFile[] ftpPrintFiles(String dirPath) {
        FTPFile[] ret = null;
        if( ftpIsConnected() ) {
            try {
                ret = mFTPClient.listFiles(dirPath);
                if( ret != null && ret.length > 0 ) {
                    for( int i = 0; i < ret.length; i++ ) {
                        String name = ret[i].getName();
                        boolean isFile = ret[i].isFile();
                        if (isFile) Log.i(TAG, "File : " + name);
                        else Log.i(TAG, "Directory : " + name);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean changeToParentDirectory() {
        boolean ret = false;
        if( ftpIsConnected() ) {
            try {
                ret = mFTPClient.changeToParentDirectory();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean changeWorkingDirectory(String dir_path) {
        boolean ret = false;
        if( ftpIsConnected() ) {
            try {
                ret = mFTPClient.changeWorkingDirectory(dir_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean makeDirectory(String dir_path) {
        boolean ret = false;
        if( ftpIsConnected() ) {
            try {
                ret = mFTPClient.makeDirectory(dir_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean removeDirectory(String dir_path) {
        boolean ret = false;
        if( ftpIsConnected() ) {
            try {
                ret = mFTPClient.removeDirectory(dir_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean deleteFile(String file_path) {
        boolean ret = false;
        if( ftpIsConnected() ) {
            try {
                ret = mFTPClient.deleteFile(file_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean renameFile(String from, String to) {
        boolean ret = false;
        if( ftpIsConnected() ) {
            try {
                ret = mFTPClient.rename(from,to);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean ftpUpload(String srcFilePath, String desFileName) {
        boolean status = false;
        try {
            FileInputStream srcFileStream = new FileInputStream(srcFilePath);
            status = mFTPClient.storeFile(desFileName, srcFileStream);
            srcFileStream.close();
            return status;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "upload failed: " + e);
        }
        return status;
    }

    public boolean ftpUpload(String srcFilePath, String desFileName, final FileTransfertListener listener){
        boolean ret = false;
        if( ftpIsConnected() ) {
            BufferedInputStream buffIn = null;
            final File file = new File(srcFilePath);
            try {
                buffIn = new BufferedInputStream(new FileInputStream(file), 8192);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            streamListener = new CopyStreamAdapter() {
                @Override
                public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {

                    int percent = (int) (totalBytesTransferred * 100 / file.length());
                    if( listener != null ) {
                        listener.onProgress(percent);
                        listener.onByteTransferred(totalBytesTransferred, file.length());
                    }
                    if (totalBytesTransferred == file.length()) {
                        if( listener != null ) listener.onFinish();
                        removeCopyStreamListener(streamListener);
                    }
                }
            };
            mFTPClient.setCopyStreamListener( streamListener );
            try {
                if( listener != null ) listener.onStart();
                ret = mFTPClient.storeFile( desFileName, buffIn );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    public boolean ftpDownload(String srcFileName, String desFileName) {
        boolean ret = false;
        if( ftpIsConnected() ) {
            FileOutputStream fileOutputStream = null;
            final File file = new File(desFileName);
            try {
                if( file.exists() ) file.delete();
                if( !file.createNewFile() ){
                    Log.w(TAG,"Error while trying to create new file");
                } else {
                    fileOutputStream = new FileOutputStream(file);
                    FTPFile f = mFTPClient.mlistFile(srcFileName);
                    if( f == null ) {
                        Log.w(TAG,"Error src file not found!");
                    } else {
                        ret = mFTPClient.retrieveFile(srcFileName,fileOutputStream);
                        fileOutputStream.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean ftpDownload(String srcFileName, String desFileName, final FileTransfertListener listener ) {
        boolean ret = false;
        if( ftpIsConnected() ) {

            FileOutputStream fileOutputStream = null;
            final File file = new File(desFileName);
            try {
                if( !file.createNewFile() ){
                    Log.w(TAG,"Error while trying to create new file");
                } else {
                    fileOutputStream = new FileOutputStream(file);
                    FTPFile f = mFTPClient.mlistFile(srcFileName);
                    if( f == null ) {
                        Log.w(TAG,"Error src file not found!");
                    } else {
                        final long file_size = f.getSize();
                        streamListener = new CopyStreamAdapter() {
                            @Override
                            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {

                                int percent = (int) (totalBytesTransferred * 100 / file_size);
                                if( listener != null ) {
                                    listener.onProgress(percent);
                                    listener.onByteTransferred(totalBytesTransferred, file_size);
                                }


                                if (totalBytesTransferred == file_size) {
                                    if( listener != null ) listener.onFinish();
                                    removeCopyStreamListener(streamListener);
                                }
                            }
                        };
                        mFTPClient.setCopyStreamListener( streamListener );
                        if( listener != null ) listener.onStart();
                        ret = mFTPClient.retrieveFile(srcFileName,fileOutputStream);
                        fileOutputStream.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}
