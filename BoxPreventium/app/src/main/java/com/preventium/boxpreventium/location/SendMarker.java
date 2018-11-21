package com.preventium.boxpreventium.location;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.preventium.boxpreventium.server.JSON.ParseJsonData;
import com.preventium.boxpreventium.utils.ComonUtils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by tog on 15/11/2018.
 */

public class SendMarker {

    private Context context;
    private String server;
    private HashMap<String, Object> data;


    public SendMarker (Context context) {
        this.context = context;
        server = "https://test.preventium.fr";
    }

    public void send (CustomMarker marker) {
        // les données
        data = new HashMap<>();
        data.put("titre", marker.getTitle());
        data.put("perimetre", marker.getAlertRadius());
        data.put("IMEI", ComonUtils.getIMEInumber(context));
        data.put("message", marker.getAlertMsg());
        data.put("latitude", marker.getPos().latitude);
        data.put("longitude", marker.getPos().longitude);

        Log.v("share marqueur", String.valueOf(marker.getPos().latitude) + ", " + String.valueOf(marker.getPos().longitude));

        // on envoye
        new Send().execute(server + "/index.php/position/set_position");
    }

    private class Send extends AsyncTask<String, Integer, String> {
        // var
        private ParseJsonData jsonData;
        private boolean IsSuccess;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // json data
            jsonData = new ParseJsonData(data);
            IsSuccess = false;
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            Toast.makeText(context, string, Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            StringBuilder msg = new StringBuilder();

            try {
                String json = jsonData.makeServiceCall(url);
                if ( json != null ) {
                    JSONObject conf = new JSONObject(json);
                    if( conf.optBoolean("succes") ) {
                        msg.append("Succes de l'envoye");
                        IsSuccess = true;
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
                msg.append("Echec de l'envoye");
            }

            return msg.toString();
        }
    }
}
