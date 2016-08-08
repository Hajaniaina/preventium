package com.preventium.boxpreventium;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.preventium.boxpreventium.ftp.FtpConfig;

import java.io.File;

/**
 * Created by Franck on 08/08/2016.
 */

public class ConfigUtils {

    private static final String HOST = "ftp.ikalogic.com";
    private static final String LOGIN = "ikalogic";
    private static final String PASS = "Tecteca1";
    public static final String DIR = "www/tmp/";

    public static FtpConfig server_ftp_config() {
        FtpConfig ret = new FtpConfig(HOST,LOGIN,PASS);
        return ret;
    }

    @NonNull
    public static String server_cfg_filename() {
        return "test.CFG";
    }

    @NonNull public static String get_cfg_dir(Context ctx){
        File folder = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "Preventium" );
        if (!folder.exists()) folder.mkdir();
        // initiate media scan and put the new things into the path array to
        // make the scanner aware of the location and the files you want to see
        MediaScannerConnection.scanFile(ctx, new String[]{folder.toString()}, null, null);
        return folder.getPath();
    }

    public static String get_cfg_path(Context ctx){
        String filepath = get_cfg_dir(ctx) + "/" + "config.CFG";
        return filepath;
    }

    public static boolean cfg_exist(Context ctx){
        File f = new File( get_cfg_path(ctx) );
        return f.exists();
    }


}

