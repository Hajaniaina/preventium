package com.preventium.boxpreventium.module.Load;

import android.content.Context;
import android.os.AsyncTask;

import com.preventium.boxpreventium.server.JSON.ParseJsonData;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.Connectivity;
import com.preventium.boxpreventium.utils.DataLocal;

import org.json.JSONObject;

/**
 * Created by tog on 21/10/2018.
 */

public class LoadConfig {
    // var
    private DataLocal local;
    private Context context;
    private String imei;
    private String server = "https://test.preventium.fr";

    public LoadConfig (Context context) {
        this.context = context;
        this.imei = ComonUtils.getIMEInumber(context);
        this.local = DataLocal.get(context);

        // LoadConfig
        new Load().execute();
    }

    public static synchronized LoadConfig init (Context context) {
        return new LoadConfig(context);
    }

    final class Load extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            ParseJsonData jsonData = new ParseJsonData();
            boolean connected = Connectivity.isConnected(context);
            if( connected ) {
                String json = jsonData.makeServiceCall(server + "/index.php/get_attribution/" + imei);
                if( json != null && json.toString().length() > 0) {
                    try
                    {
                        JSONObject cfg = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                        // pour cfg
                        JSONObject config = cfg.getJSONObject("cfg");
                        local.setValue("cfg_ftp_host", config.optString("FTP"));
                        local.setValue("cfg_ftp_login", config.optString("FTP_login"));
                        local.setValue("cfg_ftp_pwd", config.optString("FTP_pwd"));
                        local.apply();

                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}
