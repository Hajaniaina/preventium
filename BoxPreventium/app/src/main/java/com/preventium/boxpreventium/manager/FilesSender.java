package com.preventium.boxpreventium.manager;

import android.content.Context;
import android.util.Log;

import com.preventium.boxpreventium.database.DBHelper;
import com.preventium.boxpreventium.server.CFG.DataCFG;
import com.preventium.boxpreventium.utils.ThreadDefault;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPClientIO;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPConfig;

import java.io.File;

/**
 * Created by Franck on 08/11/2016.
 */

public class FilesSender extends ThreadDefault {

    private final static String TAG = FilesSender.class.getSimpleName();
//    public FilesSender(NotifyListener notify) {
//        super(notify);
//    }

    private Context _ctx;

    public FilesSender(Context context) {
        super(null);
        _ctx = context;
    }


    public boolean startThread(){
        boolean ret = false;
        if( !isRunning()  ) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FilesSender.this.run();
                }
            }).start();
            ret = true;
        }
        return ret;
    }

    public void stopThread(){setStop();}

    @Override
    public void myRun() throws InterruptedException {
        super.myRun();

        DBHelper db = new DBHelper(_ctx);

        // REMOVE ECA FILE
        db.remove_eca_file(_ctx);
        // CREATE ECA FILE
        long last_time = DBHelper.get_last_eca_time_send(_ctx);
        long last_time_temp = db.create_eca_file( _ctx, last_time );
Log.d("AAA","last_time " + last_time + " last_time_temp " + last_time_temp );
        if( last_time_temp > last_time ){

            // SENDING FILE
            boolean success = false;
            File folder = new File(_ctx.getFilesDir(), "ECA");
            File[] listOfFiles = folder.listFiles();
            if( listOfFiles != null && listOfFiles.length > 0 ){
                FTPConfig config = DataCFG.getFptConfig(_ctx);
                FTPClientIO ftp = new FTPClientIO();
                if( config != null && ftp.ftpConnect(config, 5000) ) {
                    boolean change_directory = true;
                    if (!config.getWorkDirectory().isEmpty() && !config.getWorkDirectory().equals("/"))
                        change_directory = ftp.changeWorkingDirectory(config.getWorkDirectory());
                    if (!change_directory) {
                        Log.w(TAG, "Error while trying to change working directory!");
                    } else {
                        success = ftp.ftpUpload(listOfFiles[0].getAbsolutePath(),listOfFiles[0].getName());
                    }
                    ftp.ftpDisconnect();
                }
            }
Log.d("AAA","success: " + success );
            // UPDATE LAST SENDING INFO
            if( success )
                Log.d(TAG,"update_last_send: " + db.update_last_send( _ctx, last_time_temp ) );

        }
        // REMOVE ECA FILE
        db.remove_eca_file(_ctx);

    }
}
