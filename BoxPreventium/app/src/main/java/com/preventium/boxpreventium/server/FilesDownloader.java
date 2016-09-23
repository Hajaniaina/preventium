package com.preventium.boxpreventium.server;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.server.CFG.DataCFG;
import com.preventium.boxpreventium.server.CFG.ReaderCFGFile;
import com.preventium.boxpreventium.server.EPC.ReaderEPCFile;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.ThreadDefault;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPClientIO;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Franck on 23/09/2016.
 */

public class FilesDownloader extends ThreadDefault {

    private final static String TAG = "FilesDownloader";
    private final static Boolean DEBUG = true;

    public enum MODE_t {
        NONE(-1),
        CFG(0),
        EPC(1);
        private int value;
        private static Map<Integer, MODE_t> map = new HashMap<>();
        MODE_t(int value) { this.value = value; }
        static { for (MODE_t e : MODE_t.values() ) map.put(e.value, e); }
        public static MODE_t valueOf(int cmdType) { return map.get(cmdType); }
        public int getValue() {
            return value;
        }
        @NonNull
        public String toString() {
            switch ( valueOf(value) ) {
                case NONE    :
                    return "MODE_t[NONE]";
                case CFG    :
                    return "MODE_t[CFG]";
                case EPC  :
                    return "MODE_t[EPC]";
                default                 :
                    return "MODE_t[???]";
            }
        }
    }
    public interface FilesDowloaderListener{
        void onModeChanged(MODE_t mode_t);
    }

    private FTPClientIO ftp = null;
    private Context ctx = null;
    private FilesDowloaderListener listener = null;
    private MODE_t mode_t = MODE_t.NONE;

    public FilesDownloader(Context ctx, FilesDowloaderListener listener) {
        super(null);
        this.ctx = ctx;
        this.listener = listener;
    }

    public boolean downloadCFG(){
        boolean ret = false;
        if (!isRunning()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mode_t = MODE_t.CFG;
                    FilesDownloader.this.run();
                }
            }).start();
            ret = true;
        }
        return ret;
    }

    public boolean downloadEPC(){
        boolean ret = false;
        if (!isRunning()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mode_t = MODE_t.EPC;
                    FilesDownloader.this.run();
                }
            }).start();
            ret = true;
        }
        return ret;
    }

    @Override
    public void myRun() throws InterruptedException {
        super.myRun();

        if( listener != null ) listener.onModeChanged( mode_t );

        switch ( mode_t ) {
            case CFG:
                run_cfg();
                break;
            case EPC:
                run_epc();
                break;
            case NONE:
                break;
        }

        if( listener != null ) listener.onModeChanged( mode_t );
        mode_t = MODE_t.NONE;

    }

    private void run_cfg() {
        if( DEBUG ) Log.d(TAG,"Connecting...");

        FTPConfig config = new FTPConfig("ftp.ikalogic.com","ikalogic","Tecteca1",21);
        ftp = new FTPClientIO();
        if( !ftp.ftpConnect(config,5000) ) {
            if( DEBUG ) Log.w(TAG,"Connection failed!");
        } else {
            if( DEBUG ) Log.d(TAG,"Connected.");
            // Create folder if not exist
            File folder = new File(ctx.getFilesDir(), "");
            if ( !folder.exists() )
                if( !folder.mkdirs() ) Log.w(TAG, "Error while trying to create new folder!");
            // If folder not exist
            if( !folder.exists() ) {
                if( DEBUG ) Log.w(TAG,"Folder not exist");
            } else { // If folder not exist
                if( DEBUG ) Log.d(TAG,"Retrieving CFG file...");

                String srcFileName = ComonUtils.getIMEInumber(ctx) + ".CFG";
                String desFileName = String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), srcFileName);
                boolean success = ftp.ftpDownload(srcFileName, desFileName);
                if( !success ) {
                    if( DEBUG ) Log.w(TAG,"CFG file not retreive!");
                } else {
                    if( DEBUG ) Log.d(TAG,"CFG file retreive.");
                    ReaderCFGFile reader_cfg = new ReaderCFGFile();
                    success = reader_cfg.read(desFileName);
                    if (!success) {
                        if( DEBUG ) Log.w(TAG,"CFG file corrupted!");
                    } else {
                        if( DEBUG ) Log.d(TAG,"CFG file successfully apply.");
                        reader_cfg.applyToApp(ctx);
                    }
                }
            }
            ftp.ftpDisconnect();
        }

    }

    private void run_epc() {
        FTPConfig config = DataCFG.getFptConfig(ctx);
        if (config == null) {
            if (DEBUG) Log.w(TAG, "CFG file not exist!");
        } else {
            if (DEBUG) Log.d(TAG, "Connecting...");
            ftp = new FTPClientIO();
            if (!ftp.ftpConnect(config, 10000)) {
                if (DEBUG) Log.w(TAG, "Connection failed!");
            } else {
                if (DEBUG) Log.d(TAG, "Connected.");
                // Create folder if not exist
                File folder = new File(ctx.getFilesDir(), "");
                if (!folder.exists())
                    if (!folder.mkdirs()) Log.w(TAG, "Error while trying to create new folder!");
                // If folder not exist
                if (!folder.exists()) {
                    if (DEBUG) Log.w(TAG, "Folder not exist");
                } else { // If folder not exist
                    if (DEBUG) Log.d(TAG, "Retrieving EPC files...");

                    ReaderEPCFile reader_epc = new ReaderEPCFile();
                    for (int i = 1; i <= 5; i++) {
                        String srcFileName = reader_epc.getEPCFileName(ctx,i);
                        String desFileName = reader_epc.getEPCFilePath(ctx,i);
                        boolean success = ftp.ftpDownload(srcFileName, desFileName);
                        if (DEBUG) Log.d(TAG, "Retrieving " + srcFileName + "...");
                        if (!success) {
                            if( DEBUG ) Log.w(TAG,srcFileName + " not retreive!");
                        } else {
                            if( DEBUG ) Log.d(TAG,srcFileName + " retreive.");
                            success = reader_epc.read(desFileName);
                            if ( !success) {
                                if( DEBUG ) Log.w(TAG,srcFileName + " corrupted!");
                            } else {
                                if( DEBUG ) Log.d(TAG,srcFileName + " succefully apply.");
                                reader_epc.print();
                            }
                        }
                    }
                }
                ftp.ftpDisconnect();
            }
        }

    }


}
