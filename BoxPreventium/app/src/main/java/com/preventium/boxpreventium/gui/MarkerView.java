package com.preventium.boxpreventium.gui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.ScrollBar;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.jsibbold.zoomage.ZoomageView;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.location.CustomMarker;
import com.preventium.boxpreventium.location.MarkerData;
import com.preventium.boxpreventium.module.Load.LoadImage;

import java.io.File;

/**
 * Created by tog on 06/11/2018.
 */

public class MarkerView implements GoogleMap.OnMarkerClickListener {
    // variable
    private Context context;
    private MainActivity main;

    // constrctor
    public MarkerView (Context context) {
        this.context = context;
        this.main = getMain(context);
    }

    private MainActivity getMain(Context context) {
        return (MainActivity) context;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        CustomMarker customMarker = main.getMarkerManager().getMarker(marker);
        if (customMarker.isAlertEnabled()) {
            main.getMarkerManager().showAlertCircle(customMarker, true);
        }
        if (customMarker.isEditable()) {
            marker.setSnippet(context.getResources().getString(R.string.more_string));
        }
        // show dialog
        setDialogMarker(customMarker);
        return false;
    }

    /* pour ajouter un custom marker sur la map */
    public void addMarker (final MarkerData markerData) {
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng latLng = new LatLng(markerData.COLUMN_MARKER_LAT, markerData.COLUMN_MARKER_LONG);
                Marker marker = main.getMarkerManager().addMarker(main.getGoogleMap(), markerData.COLUMN_MARKER_TITRE, markerData.COLUMN_MARKER_LABEL, latLng, CustomMarker.MARKER_INFO, true, markerData.COLUMN_MARKER_PERIMETRE);
                marker.showInfoWindow();
            }
        });
    }

    /* pour le dialog de marker */
    public void setDialogMarker (CustomMarker customMarker) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen); //Theme_Black_NoTitleBar_Fullscreen
        boolean is_pdf = false;
        String dir = context.getFilesDir() + "/marker";

        // IMAGE
        File file = new File(dir, String.valueOf(customMarker.getPos().latitude) + "_" + String.valueOf(customMarker.getPos().longitude) + ".png");
        if( !file.isFile() ) {
            file = new File(dir, String.valueOf(customMarker.getPos().latitude) + "_" + String.valueOf(customMarker.getPos().longitude) + ".jpg");
        }
        if( !file.isFile() ) {
            file = new File(dir, String.valueOf(customMarker.getPos().latitude) + "_" + String.valueOf(customMarker.getPos().longitude) + ".gif");
        }

        // PDF
        if( !file.isFile() ) {
            file = new File(dir, String.valueOf(customMarker.getPos().latitude) + "_" + String.valueOf(customMarker.getPos().longitude) + ".pdf");
            is_pdf = true;
        }

        // on ne trouve pas le fichier
        if( !file.isFile() ) return;

        Log.v("Nom du fichier", file.getAbsolutePath());
        // Toast.makeText(context, file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        if( !is_pdf  ) {
            dialog.setContentView(R.layout.dialog_marker_image);

            ZoomageView image = (ZoomageView) dialog.findViewById(R.id.image_marker);
            LoadImage loadImage = new LoadImage(context);
            Bitmap bitImage = loadImage.fileToBitmap(file.getAbsolutePath());
            image.setImageBitmap(bitImage);
            // Toast.makeText(context, "dialog image, ce qui veut dire que l'image existe", Toast.LENGTH_LONG).show();
        } else {
            dialog.setContentView(R.layout.dialog_marker_pdf);

            final String pdf_dir = dialog.getContext().getFilesDir() + "/marker";

            PDFView pdfView = (PDFView) dialog.findViewById(R.id.pdfView);
            ScrollBar scrollBar = (ScrollBar) dialog.findViewById(R.id.scrollBar);
            scrollBar.setHorizontal(false);
            pdfView.setScrollBar(scrollBar);

            if(file.canRead())
            {
                // load now
                pdfView.fromFile(file).defaultPage(1).onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        Toast.makeText(getMain(context), String.valueOf(nbPages), Toast.LENGTH_LONG).show();
                    }
                }).load();
            }

            // Toast.makeText(context, "dialog pdf, ce qui veut dire que le doc pdf existe", Toast.LENGTH_LONG).show();
            Log.v("PDF", "Pdf à charge ou chargé");
        }

        TextView close = (TextView) dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
