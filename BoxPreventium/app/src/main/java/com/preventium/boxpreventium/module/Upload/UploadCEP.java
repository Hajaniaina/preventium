package com.preventium.boxpreventium.module.Upload;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.preventium.boxpreventium.database.Database;
import com.preventium.boxpreventium.enums.STATUS_t;
import com.preventium.boxpreventium.manager.interfaces.AppManagerListener;
import com.preventium.boxpreventium.server.CFG.DataCFG;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPClientIO;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPConfig;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;

/**
 * Created by tog on 02/12/2018.
 */

public class UploadCEP {

    private Context context;
    private AppManagerListener listener;
    private boolean quite = false;

    public void setQuite(boolean quite) {
        this.quite = quite;
    }

    public UploadCEP (Context context, AppManagerListener listener) {
        this.context = context;
        this.listener = listener;
        new AsyncUploadCEP(this).execute();
    }

    private static class AsyncUploadCEP extends AsyncTask<String, String, Integer> {

        private UploadCEP upload;
        private WeakReference<Context> contextWeakReference;
        private WeakReference<AppManagerListener> listenerWeakReference;

        public AsyncUploadCEP (UploadCEP upload) {
            this.upload = upload;
            this.contextWeakReference = new WeakReference<Context>(upload.context);
            this.listenerWeakReference = new WeakReference<AppManagerListener>(upload.listener);
        }

        @Override
        protected Integer doInBackground(String... param) {
            Context context = contextWeakReference.get();
            if( context != null ) {
                File folder = new File(context.getFilesDir(), "CEP");
                if (folder.exists()) {
                    File[] listOfFiles = folder.listFiles(new C01062());
                    boolean nbf = listOfFiles.length > 0;
                    if (listOfFiles != null && nbf) {
                        FTPConfig config = DataCFG.getFptConfig(context);
                        FTPClientIO ftp = new FTPClientIO();
                        if (config != null && ftp.ftpConnect(config, 5000)) {
                            boolean change_directory = true;
                            if (!(config.getWorkDirectory() == null || config.getWorkDirectory().isEmpty() || config.getWorkDirectory().equals("/"))) {
                                change_directory = ftp.changeWorkingDirectory(config.getWorkDirectory());
                            }
                            if (change_directory) {
                                for (File file : listOfFiles) {
                                    if (ftp.ftpUpload(file.getAbsolutePath(), file.getName())) {
                                        // delete file and erase data
                                        file.delete();
                                        new Database(context).clear_cep_data();
                                    }
                                    // if( fini )
                                }
                            } else {
                                Log.w("CEP", "CEP: Error while trying to change working directory to \"" + config.getWorkDirectory() + "\"!");
                            }
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            AppManagerListener listener = listenerWeakReference.get();
            Context context = contextWeakReference.get();
            if (listener != null && context != null ) {
                listener.onStatusChanged(STATUS_t.PAR_STOPPED);

                // quitter après envoye de CEP
                if( upload.quite ) {
                    // finish it
                    Activity activity = (Activity) context;
                    activity.finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(-1);
                }
            }

            Log.w("CFG loading", "post execute");
            // MainActivity.instance().Alert("CEP envoyé", Toast.LENGTH_LONG);
        }
    }

    private static class C01062 implements FilenameFilter {
        C01062() {
        }

        public boolean accept(File dir, String name) {
            return name.toUpperCase().endsWith(".CEP");
        }
    }
}
