package com.preventium.boxpreventium.server.JSON;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by FOLIO on 12/1/2017.
 */

public class ParseJsonData {

    private static final String TAG = ParseJsonData.class.getSimpleName();

    public ParseJsonData() {
    }

    public String makeServiceCall(String reqUrl) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            System.out.println("URLo AVC S: " + url.getProtocol());
            Log.e("URLo LOG S  : ", String.valueOf(url.getProtocol()));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
           // conn.setDefaultUseCaches(false);
            //conn.setUseCaches(false);
            conn.setRequestProperty("Cache-Control", "no-cache");
            //conn.setInstanceFollowRedirects(true);
  /*          conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
            conn.setRequestProperty("User-Agent", "Mozilla/5.0...");

            switch (conn.getResponseCode())
            {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    location = conn.getHeaderField("Location");
                    location = URLDecoder.decode(location, "UTF-8");
                    base     = new URL(url);
                    next     = new URL(base, location);  // Deal with relative URLs
                    url      = next.toExternalForm();
                    continue;
            }

            break;
*/
           // URL urlWithScheme = new URL("https://www.google.com");
            //URL urlWithoutScheme = new URL("www.google.com");

           //System.out.println("URL AVC S: " + urlWithScheme.getProtocol());

            //System.out.println("URL NO S: " + urlWithoutScheme.getProtocol());

            //Log.e("URL LOG Ns  : ", String.valueOf(urlWithoutScheme.getProtocol()));
            //Log.e("URL LOG S  : ", String.valueOf(urlWithScheme.getProtocol()));

            conn.setRequestMethod("GET");
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
