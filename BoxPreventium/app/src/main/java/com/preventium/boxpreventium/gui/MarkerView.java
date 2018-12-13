package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.ScrollBar;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.gregacucnik.EditableSeekBar;
import com.jsibbold.zoomage.ZoomageView;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.location.CustomMarker;
import com.preventium.boxpreventium.location.DatasMarker;
import com.preventium.boxpreventium.location.FileManagerMarker;
import com.preventium.boxpreventium.location.MarkerData;
import com.preventium.boxpreventium.location.MarkerManager;
import com.preventium.boxpreventium.module.Load.LoadImage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
        this.main = getMain();
    }

    private MainActivity getMain() {
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
    private Map<String, String> customMarkers = new HashMap<>();
    private Dialog dialog;
    public void setDialogMarker (CustomMarker customMarker) {
        if( dialog != null && dialog.isShowing() ) return;

        if( customMarker.isNear() && customMarkers.containsKey(customMarker.getTitle()) ) {
            float time = Float.parseFloat(customMarkers.get(customMarker.getTitle()));
            if( time / 60000 < 2 ) return;
            customMarkers.put(customMarker.getTitle(), String.valueOf(System.currentTimeMillis()));
        }

        dialog = new Dialog(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen); //Theme_Black_NoTitleBar_Fullscreen
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
                        Toast.makeText(getMain(), String.valueOf(nbPages), Toast.LENGTH_LONG).show();
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

    private AlertDialog alertDlg;
    private EditText editTitle;
    private CheckBox shareCheckBox;
    private CheckBox alarmCheckBox;
    private Button parcourir;
    private TextView text_file;
    private Spinner markerTypeSpinner;
    private EditableSeekBar alertRadiusSeekBar;

    public AlertDialog getDialogEdit() {
        return alertDlg;
    }

    public void setUpdateFile (File file) {
        if( text_file != null ) {
            text_file.setText(file.getName());
        }

        if( parcourir != null ) {
            parcourir.setText(getMain().getString(R.string.browse_edit));
        }
    }

    public void showMarkerEditDialog (final Marker marker, final boolean creation, final MarkerManager markerManager, final GoogleMap googleMap, final FileManagerMarker fileManagerMarker) {

        final CustomMarker customMarker = markerManager.getMarker(marker);

        if (customMarker == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this.context);

        LayoutInflater inflater = getMain().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.marker_edit_dialog, null));

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton(getMain().getString(R.string.cancel_string), null);

        if (!creation) {
            builder.setNeutralButton(getMain().getString(R.string.delete_string), null);
        }

        alertDlg = builder.create();

        if (creation) {
            alertDlg.setCancelable(false);
        }

        alertDlg.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow (DialogInterface dialogInterface) {

                editTitle = (EditText) alertDlg.findViewById(R.id.marker_title);
                shareCheckBox = (CheckBox) alertDlg.findViewById(R.id.marker_share_checkbox);
                alarmCheckBox = (CheckBox) alertDlg.findViewById(R.id.marker_alarm_checkbox);
                parcourir = (Button) alertDlg.findViewById(R.id.browse);
                text_file = (TextView) alertDlg.findViewById(R.id.text_file);
                markerTypeSpinner = (Spinner) alertDlg.findViewById(R.id.marker_type_spinner);
                alertRadiusSeekBar = (EditableSeekBar) alertDlg.findViewById(R.id.marker_radius_seekbar);

                final String titleBefore = customMarker.getTitle();
                editTitle.setText(titleBefore);

                if (!creation) {

                    if (customMarker.getType() == CustomMarker.MARKER_INFO) {

                        markerTypeSpinner.setSelection(0);
                    }
                    else {

                        markerTypeSpinner.setSelection(1);
                    }

                    if (customMarker.isAlertEnabled()) {

                        alarmCheckBox.setChecked(true);
                        alertRadiusSeekBar.setVisibility(View.VISIBLE);
                    }
                    else {

                        alarmCheckBox.setChecked(false);
                        alertRadiusSeekBar.setVisibility(View.GONE);
                    }

                    if (customMarker.isShared()) {

                        shareCheckBox.setChecked(true);
                    }
                    else {

                        shareCheckBox.setChecked(false);
                    }

                    alertRadiusSeekBar.setValue(customMarker.getAlertRadius());
                }

                markerTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {

                        if (position == 0) {

                            customMarker.setType(CustomMarker.MARKER_INFO);
                        }
                        else {

                            customMarker.setType(CustomMarker.MARKER_DANGER);
                        }
                    }

                    @Override
                    public void onNothingSelected (AdapterView<?> parent) {}
                });

                shareCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged (CompoundButton compoundButton, boolean b) {

                        if (b) {
                            customMarker.share(true);
                            // rendre visible au bouton parcourir
                            parcourir.setVisibility(View.VISIBLE);
                            text_file.setVisibility(View.VISIBLE);
                        }
                        else {
                            customMarker.share(false);
                            // rendre visible au bouton parcourir
                            parcourir.setVisibility(View.GONE);
                            text_file.setVisibility(View.GONE);
                        }
                    }
                });

                parcourir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileManagerMarker.init();
                    }
                });

                alarmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged (CompoundButton compoundButton, boolean b) {

                        if (b) {

                            customMarker.enableAlert(true);
                            customMarker.showAlertCircle(true);

                            alertRadiusSeekBar.setVisibility(View.VISIBLE);
                        }
                        else {

                            customMarker.enableAlert(false);
                            customMarker.showAlertCircle(false);

                            alertRadiusSeekBar.setVisibility(View.GONE);
                        }
                    }
                });

                Button btnOk = alertDlg.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnCancel = alertDlg.getButton(AlertDialog.BUTTON_NEGATIVE);
                Button btnDelete = alertDlg.getButton(AlertDialog.BUTTON_NEUTRAL);

                btnOk.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick (View view) {

                        EditText editTitle = (EditText) alertDlg.findViewById(R.id.marker_title);

                        assert editTitle != null;
                        String currTitle = editTitle.getText().toString();

                        if (customMarker.isAlertEnabled())
                        {
                            customMarker.setAlertRadius(alertRadiusSeekBar.getValue());
                            markerManager.addAlertCircle(googleMap, customMarker);
                        }

                        if (titleBefore != currTitle) {

                            if (currTitle.length() > 0) {

                                customMarker.setTitle(currTitle);

                                marker.hideInfoWindow();    // Refresh marker's title
                                marker.showInfoWindow();

                                // adding FILE and send
                                if( customMarker.isShared() ) {

                                    if( getMain().getFileManagerMarker().getFile() == null ) {
                                        alert(getMain().getString(R.string.no_file_selected));
                                        return;
                                    }

                                    DatasMarker dataMarker = new DatasMarker(context);
                                    dataMarker.addBddMarker(customMarker); // add to bdd
                                    getMain().getFileManagerMarker().run(customMarker); // send data and file
                                }

                                // dismiss
                                alertDlg.dismiss();
                            }
                            else {

                                editTitle.setError(getMain().getString(R.string.marker_tittle_invalid_string));
                            }
                        }
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick (View view) {

                        if (creation) {

                            markerManager.removeMarker(customMarker);
                            marker.remove();
                        }

                        alertDlg.dismiss();
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick (View view) {

                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                        alertBuilder.setMessage(getMain().getString(R.string.validate_marker_delete_string));

                        alertBuilder.setNegativeButton(getMain().getString(R.string.cancel_string), null);
                        alertBuilder.setPositiveButton(getMain().getString(R.string.delete_string), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick (DialogInterface dialogInterface, int i) {

                                markerManager.removeMarker(customMarker);
                                marker.remove();
                                alertDlg.dismiss();
                            }
                        });

                        alertBuilder.create().show();
                    }
                });
            }
        });

        alertDlg.show();
    }

    public void alert(final String message) {
        final Activity main = (Activity) context;
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
