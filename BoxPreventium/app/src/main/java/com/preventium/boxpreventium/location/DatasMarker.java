package com.preventium.boxpreventium.location;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.preventium.boxpreventium.gui.MainActivity;
import com.preventium.boxpreventium.gui.MarkerView;
import com.preventium.boxpreventium.manager.AppManager;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPClientIO;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_MARKER_ATTACHMENT;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_MARKER_CREATEUR_ID;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_MARKER_DATE;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_MARKER_ENTERPRISE_ID;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_MARKER_ID;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_MARKER_IMEI;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_MARKER_LABEL;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_MARKER_LAT;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_MARKER_LONG;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_MARKER_PERIMETRE;
import static com.preventium.boxpreventium.database.DatabaseHelper.COLUMN_MARKER_TITRE;
import static com.preventium.boxpreventium.database.DatabaseHelper.TABLE_MARKER;
import static com.preventium.boxpreventium.database.DatabaseManager.getInstance;

/**
 * Created by tog on 05/11/2018.
 */

public class DatasMarker {

    private static String TAG = "DatasMarker";
    public static String FOLDER = "marker";
    private Handler handler;
    private Context context;
    private MainActivity main;
    private MarkerView markerView;
    private File dir;

    public DatasMarker(Context context) {
        this.context = context;
        this.main = getMain(context);
        this.markerView = main.getMarkerView();
        this.handler = new Handler(Looper.getMainLooper());
        this.dir = new File(context.getFilesDir().getAbsolutePath() + "/" + FOLDER);
        if( !dir.isDirectory() ) dir.mkdir();
    }

    private MainActivity getMain(Context context) {
        return (MainActivity) context;
    }

    public void addBddMarker (CustomMarker custom) {
        MarkerData marker = new MarkerData();
        marker.COLUMN_MARKER_ID = (int)System.currentTimeMillis();
        marker.COLUMN_MARKER_IMEI = ComonUtils.getIMEInumber(this.context) + ", " + ComonUtils.getIMEInumber(this.context);
        marker.COLUMN_MARKER_DATE = (String) (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        marker.COLUMN_MARKER_LAT = custom.getPos().latitude;
        marker.COLUMN_MARKER_LONG = custom.getPos().longitude;
        marker.COLUMN_MARKER_LABEL = custom.getTitle();
        marker.COLUMN_MARKER_ATTACHMENT = "";
        marker.COLUMN_MARKER_ENTERPRISE_ID = 0;
        marker.COLUMN_MARKER_CREATEUR_ID = 0;
        marker.COLUMN_MARKER_PERIMETRE = 100;
        marker.COLUMN_MARKER_TITRE = custom.getTitle();

        MarkerData[] mark = new MarkerData[1];
        mark[0] = marker;
        this.addMarker(mark);
    }

    // add or update
    public void addMarker ( MarkerData[] marker ) {
        SQLiteDatabase db = getInstance().openDatabase();
        ContentValues contentValues;
        List<MarkerData> marker2 = new ArrayList<>();
        String imei = ComonUtils.getIMEInumber(context);

        for(int i=0;i<marker.length;i++) {

            /* data */
            MarkerData mark = marker[i];
            /*
            String[] imeis = mark.COLUMN_MARKER_IMEI.toString().split(",");
            String ime = mark.toString();
            List<String> list = Arrays.asList(imeis);
            if( !list.contains(imei) ) continue;
            */

                contentValues = new ContentValues(); // init à chaque boucle
                contentValues.put(COLUMN_MARKER_ID, System.currentTimeMillis());
                contentValues.put(COLUMN_MARKER_IMEI, mark.COLUMN_MARKER_IMEI);
                contentValues.put(COLUMN_MARKER_DATE, mark.COLUMN_MARKER_DATE);
                contentValues.put(COLUMN_MARKER_LAT, mark.COLUMN_MARKER_LAT);
                contentValues.put(COLUMN_MARKER_LONG, mark.COLUMN_MARKER_LONG);
                contentValues.put(COLUMN_MARKER_LABEL, mark.COLUMN_MARKER_LABEL);
                contentValues.put(COLUMN_MARKER_ATTACHMENT, mark.COLUMN_MARKER_ATTACHMENT);
                contentValues.put(COLUMN_MARKER_ENTERPRISE_ID, mark.COLUMN_MARKER_ENTERPRISE_ID);
                contentValues.put(COLUMN_MARKER_CREATEUR_ID, mark.COLUMN_MARKER_CREATEUR_ID);
                contentValues.put(COLUMN_MARKER_PERIMETRE, mark.COLUMN_MARKER_PERIMETRE);
                contentValues.put(COLUMN_MARKER_TITRE, mark.COLUMN_MARKER_TITRE);

            /* select d'abord */
            List<MarkerData> marker3 = getMarker(mark.COLUMN_MARKER_LAT, mark.COLUMN_MARKER_LONG);

            /* si existe et date différent donc maj*/
            MarkerData marker4 = marker3.size() > 0 ? marker3.get(0) : new MarkerData();
            if (marker4.COLUMN_MARKER_ID != 0 && marker4.COLUMN_MARKER_DATE != mark.COLUMN_MARKER_DATE) {
                db.update(TABLE_MARKER, contentValues, COLUMN_MARKER_LAT + " = " + mark.COLUMN_MARKER_LAT + " AND " +
                        COLUMN_MARKER_LONG + " = " + mark.COLUMN_MARKER_LONG, null);
                Log.v(TAG, "Update marqueur !!");
            } else if (marker4.COLUMN_MARKER_ID == 0) { // donc création si existe, on spécifie ici que ID == 0 car si ID == 0 et date == date c'est boom
                db.insert(TABLE_MARKER, null, contentValues);
                Log.v(TAG, "Ajout marqueur !!");
            }
            /* end enregistrement */
            marker2.add(mark);
        }

        // pas de donnée vers le web
        getInstance().closeDatabase();

        // showmarker
        showMarker();
    }

    public List<MarkerData> getMarker (double lat, double lng) {
        SQLiteDatabase db = getInstance().openDatabase();
        List<MarkerData> marker = new ArrayList<>();

        String request = "SELECT * FROM " + TABLE_MARKER + " WHERE " +
                COLUMN_MARKER_LAT + " = " + lat +" AND " +
                COLUMN_MARKER_LONG + " = " + lng;
        Cursor cursor =  db.rawQuery( request, null );
        if( cursor != null  && cursor.moveToFirst() ) {
            while ( !cursor.isAfterLast() ) {
                MarkerData mark = new MarkerData();
                mark.COLUMN_MARKER_ID = cursor.getInt(cursor.getColumnIndex(COLUMN_MARKER_ID));
                mark.COLUMN_MARKER_IMEI = cursor.getString(cursor.getColumnIndex(COLUMN_MARKER_IMEI));
                mark.COLUMN_MARKER_DATE = cursor.getString(cursor.getColumnIndex(COLUMN_MARKER_DATE));
                mark.COLUMN_MARKER_LAT = cursor.getFloat(cursor.getColumnIndex(COLUMN_MARKER_LAT));
                mark.COLUMN_MARKER_LONG = cursor.getFloat(cursor.getColumnIndex(COLUMN_MARKER_LONG));
                mark.COLUMN_MARKER_LABEL = cursor.getString(cursor.getColumnIndex(COLUMN_MARKER_LABEL));
                mark.COLUMN_MARKER_ATTACHMENT = cursor.getString(cursor.getColumnIndex(COLUMN_MARKER_ATTACHMENT));
                mark.COLUMN_MARKER_ENTERPRISE_ID = cursor.getInt(cursor.getColumnIndex(COLUMN_MARKER_ENTERPRISE_ID));
                mark.COLUMN_MARKER_CREATEUR_ID = cursor.getInt(cursor.getColumnIndex(COLUMN_MARKER_CREATEUR_ID));
                mark.COLUMN_MARKER_PERIMETRE = cursor.getInt(cursor.getColumnIndex(COLUMN_MARKER_PERIMETRE));
                mark.COLUMN_MARKER_TITRE = cursor.getString(cursor.getColumnIndex(COLUMN_MARKER_TITRE));
                marker.add(mark);

                // next
                cursor.moveToNext();
            }
            cursor.close();
        }

        // close connexion
        getInstance().closeDatabase();

        return marker;
    }

    public void deleteMarker() {
        SQLiteDatabase db = getInstance().openDatabase();
        db.delete(TABLE_MARKER, null, null);
        // close connexion
        getInstance().closeDatabase();
    }

    public void deleteFiles (String folder) {
        String marker = "marker";
        File folders = new File(context.getFilesDir() + "/" + folder);
        File[] _folders = folders.listFiles();
        try {
            for(int i=0; i<_folders.length; i++) {
                File file = _folders[i];
                Log.v("Delete file", file.getAbsolutePath() + " est supprimé");
                file.delete();
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.v("Delete file", "Erreur de suppression");
        }
    }

    public List<MarkerData> getMarker () {
        SQLiteDatabase db = getInstance().openDatabase();
        List<MarkerData> marker = new ArrayList<>();

        String request = "SELECT * FROM " + TABLE_MARKER;
        Cursor cursor =  db.rawQuery( request, null );
        if( cursor != null  && cursor.moveToFirst() ) {
            while ( !cursor.isAfterLast() ) {
                MarkerData mark = new MarkerData();
                mark.COLUMN_MARKER_ID = cursor.getInt(cursor.getColumnIndex(COLUMN_MARKER_ID));
                mark.COLUMN_MARKER_IMEI = cursor.getString(cursor.getColumnIndex(COLUMN_MARKER_IMEI));
                mark.COLUMN_MARKER_DATE = cursor.getString(cursor.getColumnIndex(COLUMN_MARKER_DATE));
                mark.COLUMN_MARKER_LAT = cursor.getFloat(cursor.getColumnIndex(COLUMN_MARKER_LAT));
                mark.COLUMN_MARKER_LONG = cursor.getFloat(cursor.getColumnIndex(COLUMN_MARKER_LONG));
                mark.COLUMN_MARKER_LABEL = cursor.getString(cursor.getColumnIndex(COLUMN_MARKER_LABEL));
                mark.COLUMN_MARKER_ATTACHMENT = cursor.getString(cursor.getColumnIndex(COLUMN_MARKER_ATTACHMENT));
                mark.COLUMN_MARKER_ENTERPRISE_ID = cursor.getInt(cursor.getColumnIndex(COLUMN_MARKER_ENTERPRISE_ID));
                mark.COLUMN_MARKER_CREATEUR_ID = cursor.getInt(cursor.getColumnIndex(COLUMN_MARKER_CREATEUR_ID));
                mark.COLUMN_MARKER_PERIMETRE = cursor.getInt(cursor.getColumnIndex(COLUMN_MARKER_PERIMETRE));
                mark.COLUMN_MARKER_TITRE = cursor.getString(cursor.getColumnIndex(COLUMN_MARKER_TITRE));
                Log.w("Marqueur", mark.COLUMN_MARKER_TITRE + ": " + mark.COLUMN_MARKER_ATTACHMENT + " => " + mark.COLUMN_MARKER_LAT + "," + mark.COLUMN_MARKER_LONG + ", r: " + mark.COLUMN_MARKER_PERIMETRE);
                marker.add(mark);

                // next
                cursor.moveToNext();
            }
            cursor.close();
        }
        // close connexion
        getInstance().closeDatabase();

        return marker;
    }

    public void showMarker () {
        // list of marker
        List<MarkerData> marker = getMarker();
        // download data if not exist
        downloadData(getMarker());
    }

    public static String getExtension (String file) {
        String[] file_extension = file.toString().split("\\.");
        return file_extension.length > 1 ? file_extension[file_extension.length-1].toLowerCase() : "";
    }

    // pour télcharger les données
    public void downloadData ( List<MarkerData> marker ) {
        handler.post(new Task(marker));
    }

    AtomicBoolean isRunning = new AtomicBoolean(false);
    int index = 0;
    int last_index = 0;
    private class Task implements Runnable {
        private List<MarkerData> marker;

        public Task (List<MarkerData> marker) {
            this.marker = marker;
        }

        @Override
        public void run() {
            if( this.marker.size() > 0 ) {
                // download
                if (!isRunning.get()) {
                    if(index < this.marker.size() ) {
                        // c'est en fin de boucle que cela va s'executer donc on prends le dernier index soit last_index
                        if( last_index < index ) {
                            markerView.addMarker(marker.get(last_index));
                        }

                        // sinon on télécharge le fichier
                        String extension = getExtension(marker.get(index).COLUMN_MARKER_ATTACHMENT);
                        String filename = marker.get(index).COLUMN_MARKER_LAT + "_" + marker.get(index).COLUMN_MARKER_LONG + "." + extension;
                        File file = new File(dir, filename);
                        if( !file.isFile() ) {
                            new Download().execute(marker.get(index).COLUMN_MARKER_ATTACHMENT, filename);
                        }

                        // on met à jours les données
                        last_index = index;
                        index++;
                    }else {
                        /// Toast.makeText(context, "Fin d'affichage de marqueur", Toast.LENGTH_LONG).show();
                        this.marker.clear();
                        handler.removeCallbacks(this);
                    }
                }
                handler.postDelayed(this, 3000); // 3000ms pour verifier
            }
        }
    }

    /* download */
    private class Download extends AsyncTask<String, Integer, Long> {

        private File file;
        private String repertory = "marker";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isRunning.set(true);
        }

        @Override
        protected Long doInBackground(String... strings) {
            int count;
            try {
                String _url = strings[0];
                String filename = strings[1];
                file = new File(dir, filename);

                if( !file.isFile() )
                {
                    URL url = new URL(_url);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    // getting file length
                    int lenghtOfFile = connection.getContentLength();

                    // input stream to read file - with 8k buffer
                    InputStream input = new BufferedInputStream(url.openStream(), lenghtOfFile);

                    // Output stream to write file
                    OutputStream output = new FileOutputStream(file.getAbsolutePath());
                    byte data[] = new byte[lenghtOfFile];

                    long total = 0;
                    // 8192 => 100%
                    // count => %
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress((int) (total * 100) / lenghtOfFile);
                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();
                }
            }catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            // moins d'erreur
            isRunning.set(false);
        }
    }

    // shareAndSend
    /* public void uploadShare (CustomMarker custom) {
        SendMarker sendMarker = new SendMarker(context);
        sendMarker.send(custom);
    }
    */

    /* sharePos */
    public void sharePos () {
        new UploadPos().execute();
    }

    /* upload postion share to ftp */
    public boolean is_send = false;
    private class UploadPos extends AsyncTask<String, Integer, Long> {

        private File dir;
        private File folder;
        private FTPConfig FTP_POS;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dir = new File(context.getFilesDir().getAbsolutePath() );
            folder = new File(dir.getAbsolutePath(), "SHARE");
            if( !folder.isDirectory() ) folder.mkdir();
            FTP_POS = AppManager.FTP_POS;
        }

        @Override
        protected Long doInBackground(String... strings) {
            int count;
            try {
                if (folder.exists()) {
                    File[] listOfFiles = folder.listFiles(new subShareFilter());
                    if (listOfFiles != null && listOfFiles.length > 0 && FTP_POS != null ) {
                        FTPClientIO ftp = new FTPClientIO();
                        if (ftp.ftpConnect(FTP_POS, 5000)) {
                            boolean change_directory = true;
                            if (!(FTP_POS.getWorkDirectory() == null || FTP_POS.getWorkDirectory().isEmpty() || FTP_POS.getWorkDirectory().equals("/"))) {
                                change_directory = ftp.changeWorkingDirectory(FTP_POS.getWorkDirectory());
                            }
                            if (change_directory) {
                                for (File file2 : listOfFiles) {
                                    if (ftp.ftpUpload(file2.getAbsolutePath(), file2.getName())) {
                                        file2.delete();
                                    }
                                }
                            }
                        }
                    }
                }

            }catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            is_send = true;
        }
    }

    class subShareFilter implements FilenameFilter {
        subShareFilter() {
        }

        public boolean accept(File dir, String name) {
            return name.toUpperCase().endsWith(".SHARE");
        }
    }

    /* crete file position share */
    public void create_sharePos_file (ArrayList<CustomMarkerData> customMarkerList, File folder) {
        String imei = ComonUtils.getIMEInumber(context);
        File file = new File(folder.getAbsolutePath(), String.format(Locale.getDefault(), "%s.SHARE", new Object[]{imei}));
        try {
            if (file.createNewFile()) {
                FileWriter fileWriter = new FileWriter(file);
                Iterator it = customMarkerList.iterator();
                while (it.hasNext()) {
                    CustomMarkerData mk = (CustomMarkerData) it.next();
                    Locale locale = Locale.getDefault();
                    String str = "%f;%f;%d;%d;%d;%s;%d;%d;%s;%s;%s;\n";
                    Object[] objArr = new Object[11];
                    objArr[0] = Double.valueOf(mk.position.longitude);
                    objArr[1] = Double.valueOf(mk.position.latitude);
                    objArr[2] = Integer.valueOf(mk.type);
                    objArr[3] = Integer.valueOf(mk.alert ? 1 : 0);
                    objArr[4] = Integer.valueOf(mk.alertRadius);
                    objArr[5] = mk.title;
                    objArr[6] = Integer.valueOf(mk.shared ? 1 : 0);
                    objArr[7] = Integer.valueOf(0);
                    String str2 = (mk.alertMsg == null || !mk.alertMsg.isEmpty()) ? "" : mk.alertMsg;
                    objArr[8] = str2;
                    objArr[9] = imei;
                    objArr[10] = "";
                    fileWriter.write(String.format(locale, str, objArr));
                }
                fileWriter.flush();
                fileWriter.close();
            } else {
                Log.w(TAG, "FILE NOT CREATED:" + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
