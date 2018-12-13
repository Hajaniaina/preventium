package com.preventium.boxpreventium.module.Load;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.preventium.boxpreventium.utils.ComonUtils;
import com.preventium.boxpreventium.utils.DataLocal;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by tog on 21/10/2018.
 */

public class LoadConfig {
    // var
    private DataLocal local;
    private Context context;
    private String imei;
    private String server = "https://test.preventium.fr";

    private LoadConfig (Context context) {
        this.context = context;
        this.imei = ComonUtils.getIMEInumber(context);
        this.local = DataLocal.get(context);
    }

    public static synchronized LoadConfig init (Context context) {
        return new LoadConfig(context);
    }

    public void load ()
    {
        String url = server + "/index.php/get_attribution/" + imei;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                if( json != null ) {
                    try
                    {
                        // pour cfg
                        JSONObject config = json.getJSONObject("cfg");
                        local.setValue("cfg_ftp_host", config.optString("FTP"));
                        local.setValue("cfg_ftp_login", config.optString("FTP_login"));
                        local.setValue("cfg_ftp_pwd", config.optString("FTP_pwd"));
                        local.setValue("cfg_server", config.optString("url_to_call"));
                        local.apply();

                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.i("Error----> ", ""+statusCode+" ------ "+ response);
            }
        });
    }
}
