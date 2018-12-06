package com.preventium.boxpreventium.location;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.gui.MainActivity;

import java.io.File;

/**
 * Created by tog on 27/11/2018.
 */

public class FileManagerMarker {

    private final String TAG = "FileManagermarker";
    private final int TAILLE = 8000000 ;
    private Context context;
    private File file;

    public File getFile() {
        return file;
    }

    public boolean setFile(File file) {

        Long octects = file.length();
        if( octects > TAILLE ) {
            double taille = octects / 1000000;
            getActivity().getMarkerView().alert(((Activity)context).getString(R.string.file_exceeded) + " < " + Math.round(taille) + "MB");
            Log.i(TAG, "Taille dépassé");
            return false;
        }
        Log.i("Taille du fichier", String.valueOf(octects));

        this.file = file;
        return true;
    }

    public FileManagerMarker(Context context) {
        this.context = context;
    }

    private MainActivity getActivity () {
        return (MainActivity) this.context;
    }

    public void init () {
        new MaterialFilePicker()
                .withActivity(this.getActivity())
                .withRequestCode(7010)
                .start();
    }

    public void run (CustomMarker customMarker) {
        String filename = customMarker.getPos().latitude + "_" + customMarker.getPos().longitude;
        new FileManager(getActivity(), file).toCopy(customMarker, filename);
    }
}
