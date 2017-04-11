package com.preventium.boxpreventium.server.POSS;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.preventium.boxpreventium.location.CustomMarker;
import com.preventium.boxpreventium.location.CustomMarkerData;
import com.preventium.boxpreventium.utils.ComonUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.color;
import static android.R.attr.country;

/**
 * Created by Franck on 31/03/2017.
 */

public class ReaderPOSSFile {

    public ReaderPOSSFile(){}

    public static boolean existLocalFile(Context ctx) {
        String filename = getFileName(ctx,false);
        String path = String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), filename);
        File file = new File(path);
        return file.exists();
    }

    public static String getFileName(Context ctx, boolean acknowledge ) {
        if( acknowledge )return ComonUtils.getIMEInumber(ctx) + "_ok.POSS";
        return ComonUtils.getIMEInumber(ctx) + ".POSS";
    }

    public static List<CustomMarkerData> readFile(String csvFile) {
        String line = "";
        String cvsSplitBy = ";";
        List<CustomMarkerData> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                line = line.replaceAll(";;","; ;");
                String[] column = line.split(cvsSplitBy);
                if( column.length == 10 ) {
                    CustomMarkerData mk = new CustomMarkerData();
                    mk.type = Integer.valueOf( column[2] );
                    mk.position = new LatLng( Float.valueOf(column[0]), Float.valueOf(column[1]) );
                    mk.title = column[5];
                    mk.alert = column[3].equals("1");
                    mk.alertRadius =  Integer.valueOf( column[4] );;
                    mk.alertAttachments = ReaderPOSSFile.extractListOfHref( column[9] );
                    mk.alertMsg = column[8];
                    mk.alertReqSignature = column[7].equals("1");;
                    mk.shared = column[6].equals("1");
                    list.add(mk);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }



    public static ArrayList<String> extractListOfHref( String txt ) {

        ArrayList<String> ret = new ArrayList<>();
        for ( String href: txt.split(",") )
        {
            if( isHref(href) )
            {
                ret.add( href );
            }
        }
        return ret;
    }

    public static boolean isHref( String href ) {
        Pattern p = Pattern.compile("<a href=\"(.*)\">(.*)</a>");
        Matcher m = p.matcher(href);
        return m.find();
    }

    public static String getHrefLink( String href ) {
        Document doc = Jsoup.parse(href);
        Element link = doc.select("a").first();
        return link.attr("href"); // "http://example.com/"
    }

    public static String getHrefName( String href ) {
        Document doc = Jsoup.parse(href);
        Element link = doc.select("a").first();
        return link.text(); // "example""
    }
}
