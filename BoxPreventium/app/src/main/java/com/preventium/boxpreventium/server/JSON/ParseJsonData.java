package com.preventium.boxpreventium.server.JSON;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by FOLIO on 12/1/2017.
 */

public class ParseJsonData {

    private static final String TAG = ParseJsonData.class.getSimpleName();
    private HashMap<String, Object> data;

    public ParseJsonData() {
    }

    public ParseJsonData(HashMap<String, Object> data) {
        this.data = data;
    }

    public String makeServiceCall(String reqUrl) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Cache-Control", "no-cache");

            /* pour post ou get */
            if( this.data == null ) {
                conn.setRequestMethod("GET");
            } else {
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                conn.setDoOutput(true);
                OutputStream outputPost = new BufferedOutputStream(conn.getOutputStream());

                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    str.append(entry.getKey() + "=" + entry.getValue() + "&");
                }

                String strs = str.toString();
                strs = strs.substring(0, str.length() - 1);
                outputPost.write(String.valueOf(strs).getBytes());
                outputPost.flush();
                outputPost.close();
            }

            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
            in.close();
            conn.disconnect();

        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.e("av @class v  : ", String.valueOf(sb));

        return sb.toString();
    }
}
