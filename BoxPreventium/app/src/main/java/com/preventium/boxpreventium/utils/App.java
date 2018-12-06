package com.preventium.boxpreventium.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v4.content.FileProvider;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.gui.MainActivity;
import com.preventium.boxpreventium.manager.DialogManager;
import com.preventium.boxpreventium.server.CFG.ReaderCFGFile;
import com.preventium.boxpreventium.server.JSON.ParseJsonData;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * Created by Arnaud on 21/08/2018.
 */

public class App {

    private Context context;
    private AppListener listener;
    private String serveur;
    private String imei;
    private String version;
    private DialogManager dialogManager;
    private boolean is = false;

    public App (Context context, AppListener listener)
    {
        this.context = context;
        this.listener = listener;

        // serveur du cfg
        ReaderCFGFile cfg = ComonUtils.getCFG(context);
        this.serveur = cfg.getServerUrl();
        this.imei = ComonUtils.getIMEInumber(context.getApplicationContext());
        this.version = ComonUtils.getVersionName(context);
        this.dialogManager = new DialogManager(context);
    }

    public interface AppListener {
        void onDownloaded(File file);
    }

    public void init() {
        this.is = true;
        new DectectionAndCompare().Detection();
    }

    public boolean getIs () {
        return this.is;
    }

    public static long freeSpaceCalculation (String path) {
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSizeLong();
        long availableSize = stat.getAvailableBlocksLong();
        return availableSize * blockSize / 1024;
    }

    // Dectection et comparaison de version
    public class DectectionAndCompare {

        public void Detection () {
            String _version = version;
            try {
                String json = new ParseJsonData().makeServiceCall(serveur + "/index.php/get_apk/" + imei + "/" + _version);
                if (json != null && json.toString().length() > 0) {
                    JSONObject conf = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                    if ( conf.optBoolean("succes") && conf.optBoolean("update") && listener != null ) {
                        _version = conf.optString("version");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                File file;
                if( Build.VERSION_CODES.M <= Build.VERSION.SDK_INT ) {
                    String dir_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                    file = new File(dir_path);
                    if (!file.exists() || !file.isDirectory()) file.mkdir();
                } else {
                    file = File.createTempFile("test", ".tmp");
                }

                // test d'insuffisance de mémoire
                if( App.freeSpaceCalculation(file.getPath()) < 50000 ) {// 50MB
                    dialogManager.Dialog(context.getString(R.string.storage_problem));
                }

                if ( IsUpdate(version, _version) ) {
                    String url = serveur + "/assets/apk/" + _version + imei + "/" + _version + ".apk";
                    version = _version;
                    new DownloadFileFromURL().execute(url);
                }
            }catch(Exception e) {
                dialogManager.Dialog(context.getString(R.string.storage_problem));
                e.printStackTrace();
            }finally {}
        }
    }

    // check if update is available
    public boolean IsUpdate(String currentVersion, String serveurVersion ) {
        if( currentVersion.equals("") || serveurVersion.equals("") ) return false;
        String[] nV = serveurVersion.split("\\.");
        String[] oV = currentVersion.split("\\.");

        // nouvelle
        int nVersion = Integer.parseInt(nV[0]);
        int nModule = Integer.parseInt(nV[1]);
        int nModif = Integer.parseInt(nV[2]);

        int oVersion = Integer.parseInt(oV[0]);
        int oModule = Integer.parseInt(oV[1]);
        int oModif = Integer.parseInt(oV[2]);

        if( nVersion < oVersion )
	        return false;
        if( nVersion > oVersion )
	        return true;

        if( nModule < oModule )
	        return false;
        if( nModule > oModule )
            return true;

        if( nModif < oModif )
	        return false;
        if( nModif > oModif )
            return true;

        return false;
    }

    class DownloadFileFromURL extends AsyncTask<String, Integer, String>
    {
        ProgressDialog pDialog;
        MainActivity main;
        String _url;
        String dir;
        File file;

        /**
         * Before starting background thread
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            main = (MainActivity) context;

            // file data
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator;
                file = new File(dir, "BoxEode.apk");
            } else {
                try {
                    file = File.createTempFile("BoxEode", ".apk");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            StringBuilder msg = new StringBuilder();
            try {

                _url = f_url[0];
                URL url = new URL(_url);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(file.getAbsolutePath());
                byte data[] = new byte[8192];

                long total = 0;
                // 8192 => 100%
                // count => %
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int)(total * 100) / 8192);
                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                // Log.e("Error: ", e.getMessage());
                msg.append("\n").append(e.getMessage());
                e.printStackTrace();
            }

            return msg.append("\n").toString().trim();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        /**
         * After completing background task
         * **/
        @Override
        protected void onPostExecute(String msg) {
            if( msg.toString().length() <= 0 && file.isFile() ) {
                if( listener != null ) {
                    listener.onDownloaded(file);
                }
            } // autre c'est par silence
        }
    }

    public void InstallApplication(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        int result = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
        if( result == 0 ) {
            intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_SETTINGS);
            context.startActivity(intent);
            return;
        }

        // inférieur à nougat api 24
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        }else {
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = FileProvider.getUriForFile((Activity)context, context.getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }

        context.startActivity(intent);
    }

    public void appCrash() {
        DataLocal local = DataLocal.get(context);
        final int time = (int)local.getValue("crashNumber", 0);

        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (time > 1) {
                    dialogManager.Dialog(context.getString(R.string.crash_app_message_again));
                } else {
                    dialogManager.Dialog(context.getString(R.string.crash_app_message));
                }
            }
        });

        StringBuilder out = new StringBuilder();
        out.append("Imei: " + ComonUtils.getIMEInumber(context) + " \n");
        out.append("VersionApp: " + ComonUtils.getVersionName(context) + " \n");
        out.append("Nombre de crash: " + String.valueOf(time) + " \n");
        out.append("Trace du crash:  \n");
        out.append(local.getValue("crashTrace", "").toString());
        if (ComonUtils.haveInternetConnected(context)) {
            new EmailUtils(out.toString()).send( context.getString(R.string.crash_report) + " " + ComonUtils.getIMEInumber(context));
        }

        local.remValue("crashApp");
        local.remValue("crashSend");
        local.remValue("crashTrace");
        local.commit();
    }
}
