package com.preventium.boxpreventium.location;

import android.content.Context;
import android.os.AsyncTask;

import com.preventium.boxpreventium.server.JSON.ParseJsonData;
import com.preventium.boxpreventium.utils.ComonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by tog on 05/11/2018.
 */

public class CustomMarkerManager {
    // https://test.preventium.fr
    private Context context;
    private String server;
    private MarkerData[] markerData;
    private DatasMarker dataMarker;

    public CustomMarkerManager (Context context) {
        this.context = context;
        this.dataMarker = new DatasMarker(context);
        server = ComonUtils.getCFG(context).getServerUrl();
    }

    // on obtient le json data ppour marqeur
    public void getMarker () {
        // show marker
        dataMarker.showMarker();
        // on l'execute
        new getMarker().execute();
    }

    // adding to bdd
    class getMarker extends AsyncTask<String, Void, Boolean> {

        private ParseJsonData jsonData = new ParseJsonData();
        private String url;
        private boolean error = false;
        private JSONArray array;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            url = "/index.php/get_marqueurs/" + ComonUtils.getIMEInumber(context);
             markerData = new MarkerData[0]; // init
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String json = jsonData.makeServiceCall(server + url);
            if( json != null && json.toString().length() > 0) {
                try {
                    JSONObject conf = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                    if( conf.optBoolean("succes") ) {
                        JSONArray marqueur = conf.getJSONArray("marqueur");
                        markerData = new MarkerData[marqueur.length()];
                        int length = marqueur.length();
                        for(int i=0; i<marqueur.length();i++) {
                            JSONObject obj = marqueur.getJSONObject(i);
                            MarkerData marker = new MarkerData();
                            marker.COLUMN_MARKER_ID = Integer.parseInt(obj.getString("id"));
                            marker.COLUMN_MARKER_IMEI = obj.getString("IMEI");
                            marker.COLUMN_MARKER_DATE = obj.getString("date");
                            marker.COLUMN_MARKER_LAT = Float.parseFloat(obj.getString("latitude"));
                            marker.COLUMN_MARKER_LONG = Float.parseFloat(obj.getString("longitude"));
                            marker.COLUMN_MARKER_LABEL = obj.getString("libelle");
                            marker.COLUMN_MARKER_ATTACHMENT = obj.getString("attachement");
                            marker.COLUMN_MARKER_ENTERPRISE_ID = Integer.parseInt(obj.getString("entreprise"));
                            marker.COLUMN_MARKER_CREATEUR_ID = Integer.parseInt(obj.getString("createur"));
                            marker.COLUMN_MARKER_PERIMETRE = Integer.parseInt(obj.getString("perimetre"));
                            marker.COLUMN_MARKER_TITRE = obj.getString("titre");
                            markerData[i] = marker;
                        }
                    } else
                        error = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    error = true;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean v) {
            super.onPostExecute(v);

            // on sauve dans bdd
            if( !error || markerData.length > 0 ) {
                dataMarker.addMarker(markerData);
            }
         }
    }
}
