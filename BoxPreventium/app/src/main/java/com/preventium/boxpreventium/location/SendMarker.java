package com.preventium.boxpreventium.location;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.gui.MainActivity;
import com.preventium.boxpreventium.utils.ComonUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by tog on 15/11/2018.
 */

public class SendMarker {
    private final String TAG = "SendMarker";
    private final String URL = "https://test.preventium.fr/index.php/position/set_position";
    private Context context;
    private String server;
    private HashMap<String, Object> data;
    private File file;

    public SendMarker (Context context) {
        this.context = context;
    }

    public void send (File file, CustomMarker marker) {
        // les données
        data = new HashMap<>();
        data.put("titre", marker.getTitle());
        data.put("perimetre", marker.getAlertRadius());
        data.put("IMEI", ComonUtils.getIMEInumber(context));
        data.put("message", marker.getAlertMsg());
        data.put("latitude", marker.getPos().latitude);
        data.put("longitude", marker.getPos().longitude);
        this.file = file;

        Log.v("share marqueur", String.valueOf(marker.getPos().latitude) + ", " + String.valueOf(marker.getPos().longitude));

        // on envoye
        AsyncUploader uploader = new AsyncUploader(data);
        uploader.startTransfer();
    }

    ProgressDialog dialog;
    private void logMessage(final String message) {
        Activity activity = (Activity)context;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if( dialog != null && dialog.isShowing() ) dialog.hide();
                // new dialog
                dialog = new ProgressDialog(context);
                dialog.setMessage(context.getString(R.string.wait_please));
                dialog.show();
            }
        });
    }

    private class AsyncUploader {
        private String mName;
        private String mPath;
        private AsyncHttpClient mClient;
        private Map<String, Object> data;

        public AsyncUploader(Map<String, Object>data) {
            this.data = data;
        }

        public void startTransfer() {
            mClient = new AsyncHttpClient();
            RequestParams params = new RequestParams();

            try {
                for(Map.Entry d : data.entrySet()) {
                    params.put(String.valueOf(d.getKey()), d.getValue());
                }
                params.put("attachement", file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // send
            logMessage(context.getString(R.string.wait_please));

            // mClient.setTimeout(50000);
            mClient.post(context, URL, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if( response.getBoolean("succes") ) {
                            dialog.dismiss();
                            ((MainActivity)context).getMarkerView().alert("Ficher téléversé");
                            Log.i(TAG, "Fichier téléversé");
                        }
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                    // super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.i("Error----> ", ""+statusCode+" ------ "+ response);
                }
            });
        }

        /**
         * Cancel upload by calling this method
         */
        public void cancel() {
            mClient.cancelAllRequests(true);
        }
    }
}
