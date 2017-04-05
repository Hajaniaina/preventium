package com.preventium.boxpreventium.server.POSS;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.preventium.boxpreventium.location.CustomMarker;
import com.preventium.boxpreventium.location.CustomMarkerData;
import com.preventium.boxpreventium.utils.ComonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.R.attr.country;

/**
 * Created by Franck on 31/03/2017.
 */

public class ReaderPOSSFile {

    public ReaderPOSSFile(){}

    public static boolean existLocalFile(Context ctx) {
        String filename = getOBJFileName(ctx,false);
        String path = String.format(Locale.getDefault(), "%s/%s", ctx.getFilesDir(), filename);
        File file = new File(path);
        return file.exists();
    }

    public static String getOBJFileName(Context ctx, boolean acknowledge ) {
        if( acknowledge )return ComonUtils.getIMEInumber(ctx) + "_ok.POSS";
        return ComonUtils.getIMEInumber(ctx) + ".POSS";
    }

    public static List<CustomMarkerData> readFile(String csvFile) {
        boolean ret = false;
        String line = "";
        String cvsSplitBy = ";";
        List<CustomMarkerData> list = new ArrayList<CustomMarkerData>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] column = line.split(cvsSplitBy);
                if( column.length == 10 ) {
                    CustomMarkerData mk = new CustomMarkerData();
                    mk.type = Integer.getInteger( column[2] );
                    mk.position = new LatLng( Float.valueOf(column[0]), Float.valueOf(column[1]) );
                    mk.title = column[5];
                    mk.alert = column[3].equals("1");
                    mk.alertRadius = 0;
                    mk.alertAttachments = null;
                    mk.alertMsg = null;
                    mk.alertReqSignature = column[7].equals("1");;
                    mk.shared = column[6].equals("1");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }
}
