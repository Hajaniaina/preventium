package com.preventium.boxpreventium.location;

import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by tog on 27/11/2018.
 */

public class FileManager {
    final private String TAG = "FileManager";
    final private String URL = "https://test.preventium.fr/index.php/position/set_position";
    final private long TAILLE = 8000000;

    private Context context;
    private File dirpath;
    private File file;
    private CustomMarker customMarker;

    public FileManager(Context context, File file) {
        dirpath = new File(context.getFilesDir().getAbsolutePath(), "marker");
        if( !dirpath.isDirectory() ) dirpath.mkdir();
        // file
        this.file = file;
        this.context = context;

        StringBuilder m = new StringBuilder();
        String[] list = dirpath.list();
        for(int i=0;i<list.length;i++) m.append(list[i]);
        Log.i("File exist", m.toString());
    }

    public FileManager toCopy(CustomMarker customMarker, String filename) {
        this.customMarker = customMarker;

        try {
            // condition
            filename = filename + "." + DatasMarker.getExtension(file.getAbsolutePath());
            if( copy(file, new File(dirpath, filename)) ) toSend();
            else Toast.makeText(context, "non pas copié", Toast.LENGTH_LONG).show();
        }catch(Exception e) {
            e.printStackTrace();
        }
        // return me
        return this;
    }

    /* Copy */
    private static boolean copy(File src, File dst) throws IOException {

        /* if not exist */
        File file = dst;
        if( !file.isFile() ) file.createNewFile();
        /* end exist */

        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[(int)src.length()];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    protected FileManager toSend () {

        SendMarker sendMarker = new SendMarker(context);
        sendMarker.send(file, customMarker);

        // return me
        return this;
    }

    private String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
}
