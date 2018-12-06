package com.preventium.boxpreventium.module.Load;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.preventium.boxpreventium.location.CustomMarkerManager;
import com.preventium.boxpreventium.manager.AppManager;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.DataLocal;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;

/**
 * Created by tog on 28/11/2018.
 */

public class Load {
    private static Load instance;
    private Context context;
    private DataLocal local;
    private AppManager appManager;
    private boolean blocked;

    public Load (Context context, AppManager app) {
        this.context = context;
        this.local = new DataLocal(context);
        this.appManager = app;
        this.blocked = false;
    }

    public boolean isBlocked() {
        return blocked;
    }

    private float timeConfigExceeded () {
        boolean isFirst = (boolean)local.getValue("isFirstRelance", true);
        float beginTime = local.getFloat("currentTime", 0);
        float currentTime = System.currentTimeMillis();
        return (currentTime - beginTime) / 3600000;
    }

    public void onLoad () {
        float calcHours = timeConfigExceeded();
        int opt = (int)local.getValue("timer", 0);
        boolean isConfigChanged = Math.round(calcHours) > opt;

        if( opt == 0 || isConfigChanged ) { // > Hours or first_dem
            try {
                // imei check actif
                appManager.IMEI_is_actif();
                // cfg
                appManager.download_cfg(false);
                // dobj
                appManager.download_dobj(false);

                // créer ou mettre à jour
                this.local.setValue("currentTime", (float) System.currentTimeMillis());
                this.local.setValue("isFirstRelance", false);
                this.local.apply();

                appManager.set_timer(false);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }  else
            appManager.set_timer(true);

        // epc
        try {
            appManager.download_epc(false);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void onUpdate () {
        String url_update = "https://test.preventium.fr/index.php/get_change/" + ComonUtils.getIMEInumber(context);
        connect(url_update, new ConnectListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getBoolean("succes")) {
                        // {"succes":true,"EPC":"0","DOBJ":"0","OPTION":"0","cfg":"0"}

                        // check for cfg
                        if( response.getString("cfg") != "" ) {
                            int epc = Integer.getInteger(response.getString("cfg"));
                            switch(epc) {
                                case 0:
                                    break;
                                case 1:
                                    // load cfg
                                    appManager.download_cfg(true);
                                    break;
                                case 2:
                                    // bloque app
                                    blocked = true;
                                    break;
                            }
                        }

                        // check for EPC
                        if( response.getString("EPC") != "" ) {
                            int epc = Integer.getInteger(response.getString("EPC"));
                            switch(epc) {
                                case 0:
                                    break;
                                case 1:
                                    // load EPC
                                    appManager.download_epc(true);
                                    break;
                                case 2:
                                    // bloque app
                                    blocked = true;
                                    break;
                            }
                        }

                        // check for DOBJ
                        if( response.getString("DOBJ") != "" ) {
                            int epc = Integer.getInteger(response.getString("DOBJ"));
                            switch(epc) {
                                case 0:
                                    break;
                                case 1:
                                    // load DOBJ
                                    appManager.download_dobj(true);
                                    break;
                                case 2:
                                    // bloque app
                                    blocked = true;
                                    break;
                            }
                        }

                        // check for DOBJ
                        if( response.getString("OPTION") != "" ) {
                            int epc = Integer.getInteger(response.getString("OPTION"));
                            switch(epc) {
                                case 0:
                                    break;
                                case 1:
                                    // load OPTION
                                    String server_url = ComonUtils.getCFG(context).getServerUrl();
                                    new LoadOption(context).getOption(server_url == "" ? "https://test.preventium.fr" : server_url);
                                    break;
                                case 2:
                                    // bloque app
                                    blocked = true;
                                    break;
                            }
                        }

                        // check for DOBJ
                        if( response.getString("marqueur") != "" ) {
                            int epc = Integer.getInteger(response.getString("marqueur"));
                            switch(epc) {
                                case 0:
                                    break;
                                case 1:
                                    // load marqueur
                                    // custom manager marker
                                    new CustomMarkerManager(context).getMarker();
                                    break;
                                case 2:
                                    // bloque app
                                    blocked = true;
                                    break;
                            }
                        }
                    } else { // succes false
                        blocked = true;
                    }

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(JSONObject response) {

            }
        });
    }

    private void connect (final String url, final ConnectListener connectListener) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.i("Error----> ", ""+statusCode+" ------ "+ response);
            }
        });
    }

    private String ByteToString (byte[] retours) {
        return new String(retours, StandardCharsets.UTF_8);
    }

    private interface ConnectListener {
        void onSuccess(JSONObject response);
        void onFailure(JSONObject response);
    }
}
