package com.preventium.boxpreventium.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by ARNAUD on 08/03/2018.
 */

public class FileCEP {

    public static void generateNoteOnSD (String suffix, String content) {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //handle case of no SDCARD present
        } else {

            try {
                //download folder
                File folder = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), "");

                //create file
                File file = new File(folder, "CEPCouleur_" + suffix + ".txt");
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(content);
                bw.close();

            }catch(Exception e) {}

        }
    }
}
