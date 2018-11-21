package com.preventium.boxpreventium.module.Load;

import android.content.Context;
import android.os.AsyncTask;

import com.preventium.boxpreventium.gui.MainActivity;
import com.preventium.boxpreventium.manager.AppManager;
import com.preventium.boxpreventium.manager.StatsLastDriving;
import com.preventium.boxpreventium.server.JSON.ParseJsonData;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.superclass.ftp.FTPConfig;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tog on 08/11/2018.
 */

public class LoadServer {

    private boolean obtain;
    private Context context;

    public LoadServer (Context context) {
        this.obtain = false;
        this.context = context;
    }

    /**
     * le but c'est obtenir les donnée server
     * si pas de donnée, l'application de devrait pas fonctionner
     * dans une autre version ...
     * on essaye de l'obtenir tous les 2s
     * cause: connexion lent, aucune connexion
     */
    private AtomicBoolean run = new AtomicBoolean(false);
    public void Init () {
        while (!this.run.get()) {
            if( !ComonUtils.haveInternetConnected(this.context)) {
                try {
                    Thread.sleep(1000);
                }catch(Exception e) {}
            } else {
                new Load().execute();
                run.set(true);
            }
        }
    }

    public class Load extends AsyncTask<String, String, Integer> {

        private String serveur = "https://test.preventium.fr/index.php/get_activation/";
        private String key;
        private String HOSTNAME;
        private String USERNAME;
        private String PASSWORD;
        private String url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            key = ComonUtils.randomString(8);
        }

        @Override
        protected Integer doInBackground(String... param) {

            String imei = StatsLastDriving.getIMEI(MainActivity.instance());
            url = serveur + imei + "/" + key;
            String json = new ParseJsonData().makeServiceCall(url);

            if( json != null && json.toString().length() > 0) {
                try {
                    JSONObject config = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                    HOSTNAME = config.optString("hostname");
                    USERNAME = config.optString("username");
                    PASSWORD = ComonUtils.decryt(config.optString("password"), key);
                    LoadServer.this.obtain = true; // loaded
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            AppManager.FTP_ACTIF = new FTPConfig(HOSTNAME, USERNAME, PASSWORD, 21, "/ACTIFS");
            AppManager.FTP_CFG = new FTPConfig(HOSTNAME, USERNAME, PASSWORD, 21);
            AppManager.FTP_DOBJ = new FTPConfig(HOSTNAME, USERNAME, PASSWORD, 21);
            AppManager.FTP_EPC = new FTPConfig(HOSTNAME, USERNAME, PASSWORD, 21);
            AppManager.FTP_POS = new FTPConfig(HOSTNAME, USERNAME, PASSWORD, 21, "/POSS");
        }
    }
}
