package com.preventium.boxpreventium.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {

    private String[] filePath;

    public Zipper (String[] filePath) {
        this.filePath = filePath;
    }

    public String putSample () {
        if( filePath.length > 0 ) {
            String zipname = filePath[0];
            return put(zipname);
        }
        return null;
    }

    public String put (String zipname) {
        String filename = zipname == null ? "archive.zip" : zipname + ".zip";
        try {
            File zip = new File(filename);
            zip.createNewFile();

            FileOutputStream fos = new FileOutputStream(zip.getCanonicalFile());
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            for (String srcFile : filePath) {
                File fileToZip = new File(srcFile);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
            zipOut.close();
            fos.close();

            return zip.getCanonicalFile().toString();
        }catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void read () {}
}
