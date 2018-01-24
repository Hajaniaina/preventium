package com.preventium.boxpreventium.utils.superclass.ftp;

import android.util.Log;

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

public class FTPClientIO {
    private static final String TAG = "FTPClientIO";
    private FTPClient mFTPClient = null;
    private CopyStreamAdapter streamListener;

    public boolean ftpConnect(FTPConfig config, int timeout) {
        return ftpConnect(config.getFtpServer(), config.getUsername(), config.getPassword(), config.getPort().intValue(), timeout);
    }

    public boolean ftpConnect(String host, String username, String password, int port, int timeout) {
        try {
            this.mFTPClient = new FTPClient();
            this.mFTPClient.setConnectTimeout(timeout);
            this.mFTPClient.connect(host, port);
            if (!FTPReply.isPositiveCompletion(this.mFTPClient.getReplyCode())) {
                return false;
            }
            boolean status = this.mFTPClient.login(username, password);
            this.mFTPClient.setFileType(2);
            this.mFTPClient.enterLocalPassiveMode();
            return status;
        } catch (Exception e) {
            Log.d(TAG, "Error: could not connect to host " + host);
            return false;
        }
    }

    public boolean ftpDisconnect() {
        try {
            this.mFTPClient.logout();
            this.mFTPClient.disconnect();
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Error occurred while disconnecting from ftp server.");
            return false;
        }
    }

    public boolean ftpIsConnected() {
        return this.mFTPClient != null && this.mFTPClient.isConnected();
    }

    public boolean checkFileExists(String filePath) {
        boolean fileExists = false;
        try {
            InputStream inputStream = this.mFTPClient.retrieveFileStream(filePath);
            fileExists = (inputStream == null || this.mFTPClient.getReplyCode() == 550) ? false : true;
            if (inputStream != null) {
                inputStream.close();
                this.mFTPClient.completePendingCommand();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileExists;
    }

    public String ftpPrintWorkingDirectory() {
        String ret = "";
        if (ftpIsConnected()) {
            try {
                ret = this.mFTPClient.printWorkingDirectory();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public FTPFile[] ftpPrintFiles() {
        FTPFile[] ret = null;
        if (ftpIsConnected()) {
            try {
                ret = this.mFTPClient.listFiles();
                if (ret != null && ret.length > 0) {
                    for (int i = 0; i < ret.length; i++) {
                        String name = ret[i].getName();
                        if (ret[i].isFile()) {
                            Log.i(TAG, "File : " + name);
                        } else {
                            Log.i(TAG, "Directory : " + name);
                        }
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
        if (ftpIsConnected()) {
            try {
                ret = this.mFTPClient.listFiles(dirPath);
                if (ret != null && ret.length > 0) {
                    for (int i = 0; i < ret.length; i++) {
                        String name = ret[i].getName();
                        if (ret[i].isFile()) {
                            Log.i(TAG, "File : " + name);
                        } else {
                            Log.i(TAG, "Directory : " + name);
                        }
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
        if (ftpIsConnected()) {
            try {
                ret = this.mFTPClient.changeToParentDirectory();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean changeWorkingDirectory(String dir_path) {
        boolean ret = false;
        if (ftpIsConnected()) {
            try {
                ret = this.mFTPClient.changeWorkingDirectory(dir_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean makeDirectory(String dir_path) {
        boolean ret = false;
        if (ftpIsConnected()) {
            try {
                ret = this.mFTPClient.makeDirectory(dir_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean removeDirectory(String dir_path) {
        boolean ret = false;
        if (ftpIsConnected()) {
            try {
                ret = this.mFTPClient.removeDirectory(dir_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean deleteFile(String file_path) {
        boolean ret = false;
        if (ftpIsConnected()) {
            try {
                ret = this.mFTPClient.deleteFile(file_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean renameFile(String from, String to) {
        boolean ret = false;
        if (ftpIsConnected()) {
            try {
                ret = this.mFTPClient.rename(from, to);
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
            status = this.mFTPClient.storeFile(desFileName, srcFileStream);
            srcFileStream.close();
            return status;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "upload failed: " + e);
            return status;
        }
    }

    public boolean ftpUpload(String srcFilePath, String desFileName, final FileTransfertListener listener) throws IOException {
        boolean ret = false;
        if (ftpIsConnected()) {
            BufferedInputStream buffIn = null;
            final File file = new File(srcFilePath);
            try {
                buffIn = new BufferedInputStream(new FileInputStream(file), 8192);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            this.streamListener = new CopyStreamAdapter() {
                public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                    int percent = (int) ((100 * totalBytesTransferred) / file.length());
                    if (listener != null) {
                        listener.onProgress(percent);
                        listener.onByteTransferred(totalBytesTransferred, file.length());
                    }
                    if (totalBytesTransferred == file.length()) {
                        if (listener != null) {
                            listener.onFinish();
                        }
                        removeCopyStreamListener(FTPClientIO.this.streamListener);
                    }
                }
            };
            this.mFTPClient.setCopyStreamListener(this.streamListener);
            if (listener != null) {
                listener.onStart();
            }
            ret = this.mFTPClient.storeFile(desFileName, buffIn);
        }
        return ret;
    }

    public boolean ftpDownload(String srcFileName, String desFileName) {
        FileOutputStream fileOutputStream;
        boolean ret = false;
        if (ftpIsConnected()) {
            File file = new File(desFileName);
            try {
                if (file.exists()) {
                    file.delete();
                }
                if (file.createNewFile()) {
                    FileOutputStream fileOutputStream2 = new FileOutputStream(file);
                    try {
                        if (this.mFTPClient.mlistFile(srcFileName) == null) {
                            Log.w(TAG, "Error src file not found!");
                            fileOutputStream = fileOutputStream2;
                        } else {
                            ret = this.mFTPClient.retrieveFile(srcFileName, fileOutputStream2);
                            fileOutputStream2.close();
                            fileOutputStream = fileOutputStream2;
                        }
                    } catch (IOException e) {
                        IOException e2 = e;
                        fileOutputStream = fileOutputStream2;
                        e2.printStackTrace();
                        return ret;
                    }
                }
                Log.w(TAG, "Error while trying to create new file");
            } catch (IOException e3) {
                IOException e2 = e3;
                e2.printStackTrace();
                return ret;
            }
        }
        return ret;
    }

    public boolean ftpDownload(String srcFileName, String desFileName, final FileTransfertListener listener) {
        boolean ret = false;
        if (ftpIsConnected()) {
            File file = new File(desFileName);
            try {
                if (file.createNewFile()) {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    FileOutputStream fileOutputStream2;
                    try {
                        FTPFile f = this.mFTPClient.mlistFile(srcFileName);
                        if (f == null) {
                            Log.w(TAG, "Error src file not found!");
                            fileOutputStream2 = fileOutputStream;
                        } else {
                            final long file_size = f.getSize();
                            this.streamListener = new CopyStreamAdapter() {
                                public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                                    int percent = (int) ((100 * totalBytesTransferred) / file_size);
                                    if (listener != null) {
                                        listener.onProgress(percent);
                                        listener.onByteTransferred(totalBytesTransferred, file_size);
                                    }
                                    if (totalBytesTransferred == file_size) {
                                        if (listener != null) {
                                            listener.onFinish();
                                        }
                                        removeCopyStreamListener(FTPClientIO.this.streamListener);
                                    }
                                }
                            };
                            this.mFTPClient.setCopyStreamListener(this.streamListener);
                            if (listener != null) {
                                listener.onStart();
                            }
                            ret = this.mFTPClient.retrieveFile(srcFileName, fileOutputStream);
                            fileOutputStream.close();
                            fileOutputStream2 = fileOutputStream;
                        }
                    } catch (IOException e) {
                        IOException e2 = e;
                        fileOutputStream2 = fileOutputStream;
                        e2.printStackTrace();
                        return ret;
                    }
                }
                Log.w(TAG, "Error while trying to create new file");
            } catch (IOException e3) {
                IOException e2 = e3;
                e2.printStackTrace();
                return ret;
            }
        }
        return ret;
    }
}
