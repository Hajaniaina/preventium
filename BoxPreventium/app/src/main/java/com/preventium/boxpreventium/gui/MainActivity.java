package com.preventium.boxpreventium.gui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.STATUS_t;
import com.preventium.boxpreventium.enums.SPEED_t;
import com.preventium.boxpreventium.location.PositionManager;
import com.preventium.boxpreventium.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.preventium.boxpreventium.manager.AppManager;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, AppManager.AppManagerListener {

    private static final String TAG = "MainActivity";

    private PositionManager posManager;
    private MarkerManager markerManager;
    private ScoreView scoreView;
    private SpeedView speedView;
    private AccForceView accForceView;
    private AppManager appManager;

    private TextView debugView;
    private TextView boxNumView;
    private TextView drivingTimeView;
    private ScrollView debugScroll;

    private FloatingActionMenu optMenu;
    private FloatingActionButton infoButton;
    private FloatingActionButton callButton;
    private FloatingActionButton formButton;
    private FloatingActionButton settingsButton;
    private FloatingActionButton menuButton1;
    private FloatingActionButton menuButton2;
    private FloatingActionButton menuButton3;
    private FloatingActionButton menuButton4;
    private FloatingActionButton menuButton5;

    private GoogleMap googleMap;
    private ProgressDialog progress;
    private SupportMapFragment mapFrag;
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;
    private boolean startMarkerAdded = false;
    private boolean mapReady = false;
    private Intent pinLockIntent;
    private AppColor appColor;
    STATUS_t globalStatus;
    int maxSpeed = 0;
    LatLng lastPos;

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapReady = false;
        appManager = new AppManager(this, this);
        pinLockIntent = new Intent(MainActivity.this, PinLockActivity.class);
        appColor = new AppColor(this);

        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.progress_map_string));
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setCancelable(true);
        progress.setProgressNumberFormat(null);
        progress.setProgressPercentFormat(null);

        if (!isFinishing()) {

            progress.show();
        }

        markerManager = new MarkerManager(this);
        speedView = new SpeedView(this);
        scoreView = new ScoreView(this);
        accForceView = new AccForceView(this);
        accForceView.hide(true);

        boxNumView = (TextView) findViewById(R.id.box_num_connected);
        drivingTimeView = (TextView) findViewById(R.id.driving_time_text);

        debugView = (TextView) findViewById(R.id.debug_view);
        debugView.setVisibility(View.GONE);
        debugScroll = (ScrollView) findViewById(R.id.debugScroller);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (savedInstanceState == null) {

            mapFrag.setRetainInstance(true);
        }

        mapFrag.getMapAsync(this);
    }

    @Override
    public void onPause() {

        Log.d(TAG, "onPause");
        progress.dismiss();
        super.onPause();
    }

    @Override
    public void onStop() {

        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {

        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {

        // outState.putParcelable("accForceView", accForceView);
        // outState.putParcelable("scoreView", scoreView);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

    @Override
    public void onMapReady (GoogleMap googleMap) {

        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        this.googleMap.getUiSettings().setAllGesturesEnabled(true);
        mapReady = true;

        if (progress != null && progress.isShowing()) {

            progress.setMessage(getString(R.string.progress_location_string));
        }

        posManager = new PositionManager(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            return;
        }

        this.googleMap.setMyLocationEnabled(true);
        markerManager.setMap(this.googleMap);

        setMapListeners();
        setButtonListeners();
        setPositionListeners();
    }

    @Override
    public void onNumberOfBoxChanged (final int nb) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (nb > 0) {

                    if (nb > 1) {

                        changeViewColorFilter(boxNumView, AppColor.GREEN);
                    }
                    else {

                        changeViewColorFilter(boxNumView, AppColor.BLUE);
                    }
                }
                else {

                    changeViewColorFilter(boxNumView, AppColor.RED);
                }

                boxNumView.setText(String.valueOf(nb));
            }
        });
    }

    @Override
    public void onChronoRideChanged (final String txt) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (txt.equalsIgnoreCase("0:00")) {

                    changeViewColorFilter(drivingTimeView, AppColor.GREY);
                }
                else {

                    changeViewColorFilter(drivingTimeView, AppColor.GREEN);
                }

                drivingTimeView.setText(txt);
            }
        });
    }

    @Override
    public void onForceChanged (final FORCE_t type, final LEVEL_t level) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (globalStatus == STATUS_t.CAR_MOVING) {

                    if (type != FORCE_t.UNKNOW) {

                        accForceView.hide(false);
                        accForceView.setValue(type, level);
                    }
                    else {

                        accForceView.hide(true);
                    }
                }
            }
        });
    }

    @Override
    public void onStatusChanged (final STATUS_t status) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                globalStatus = status;

                switch (status) {

                    case GETTING_CFG:

                        if (progress != null) {

                            progress.show();
                            progress.setMessage(getString(R.string.progress_cfg_string));
                        }

                        break;

                    case GETTING_EPC:

                        if (progress != null) {

                            progress.show();
                            progress.setMessage(getString(R.string.progress_epc_string));
                        }

                        break;

                    case CAR_STOPPED:

                        speedView.setText(SPEED_t.IN_STRAIGHT_LINE, "STOP");

                        if (progress != null) {

                            progress.setMessage(getString(R.string.progress_ready_string));
                            progress.hide();
                        }

                        break;

                    case CAR_MOVING:

                        speedView.setText(SPEED_t.IN_STRAIGHT_LINE, "MOVE");

                        //if (mapReady) {

                            // googleMap.getUiSettings().setAllGesturesEnabled(false);
                        //}

                        // disableActionButtons(true);
                        // speedView.hide(false);
                        // scoreView.hide(false);

                        break;

                    case CAR_PAUSING:

                        speedView.setText(SPEED_t.IN_STRAIGHT_LINE, "PAUSE");

                        //if (mapReady) {

                            // CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPos).zoom(15).bearing(0).tilt(0).build();
                            // googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            // googleMap.getUiSettings().setAllGesturesEnabled(true);
                        //}

                        // changeViewColorFilter(drivingTimeView, AppColor.ORANGE);
                        // disableActionButtons(false);
                        // speedView.hide(true);
                        // accForceView.hide(true);
                        // scoreView.disable(true);

                        break;
                }
            }
        });
    }

    @Override
    public void onDebugLog (final String txt) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (txt.isEmpty()) {

                    debugView.setVisibility(View.GONE);

                }
                else {

                    debugView.setVisibility(View.VISIBLE);
                }

                debugView.setText(txt);
            }
        });
    }

    private void changeViewColorFilter (View view, int color) {

        Drawable background = view.getBackground();
        background.setColorFilter(appColor.getColor(color), PorterDuff.Mode.SRC_ATOP);
        view.setBackground(background);
    }

    private void makeCall (String number) {

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number.trim()));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "CALL_PHONE permission disabled");
            return;
        }

        startActivity(intent);

    }

    private void drawLine (LatLng startPoint, LatLng endPoint) {

        PolylineOptions opt = new PolylineOptions();

        opt.width(3);
        opt.geodesic(true);
        opt.color(Color.rgb(0, 160, 255));
        opt.add(startPoint, endPoint);

        // roadLinesList.add(googleMap.addPolyline(opt));
        googleMap.addPolyline(opt);
    }

    private void disableActionButtons (boolean disable) {

        FloatingActionButton[] actionBtnArray = {infoButton, callButton, formButton, settingsButton};

        for (int i = 0; i < actionBtnArray.length; i++) {

            if (disable) {

                actionBtnArray[i].setEnabled(false);
            }
            else {

                actionBtnArray[i].setEnabled(true);
            }
        }
    }

    private void hideActionButtons (boolean hide) {

        FloatingActionButton[] actionBtnArray = {infoButton, callButton, formButton, settingsButton};

        for (int i = 0; i < actionBtnArray.length; i++) {

            if (hide) {

                actionBtnArray[i].hide(true);
            }
            else {

                actionBtnArray[i].show(true);
            }
        }
    }

    protected void showCallDialog() {

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setMessage(getString(R.string.make_call_confirma_string));
        alertBuilder.setCancelable(false);
        alertBuilder.setNegativeButton(getString(R.string.cancel_string), null);
        alertBuilder.setPositiveButton(getString(R.string.call_string), new DialogInterface.OnClickListener() {

            @Override
            public void onClick (DialogInterface dialogInterface, int i) {

                makeCall("0623141536");
            }
        });

        alertBuilder.create().show();

        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        Drawable drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_phone_black_24p);
        int color = ContextCompat.getColor(MainActivity.this, R.color.material_teal500);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        builder.setIcon(drawable);
        builder.setTitle("  " + getString(R.string.make_call_string));

        final CharSequence[] callLsit = new CharSequence[5];

        for (int i = 0; i < callLsit.length; i++) {

            callLsit[i] = getString(R.string.number_string) + " " + String.valueOf(i + 1);
        }

        builder.setSingleChoiceItems(callLsit, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick (DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton(getString(R.string.call_string), new DialogInterface.OnClickListener() {

            @Override
            public void onClick (DialogInterface dialog, int id) {

            }
        });

        builder.setNegativeButton(getString(R.string.cancel_string), null);

        AlertDialog alertDlg = builder.create();
        alertDlg.show();
        */
    }

    protected void showMarkerEditDialog (final Marker marker, final boolean creation) {

        final CustomMarker customMarker = markerManager.getMarker(marker);

        if (customMarker == null) {

            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.marker_edit_dialog, null));
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton(getString(R.string.cancel_string), null);

        final AlertDialog alertDlg = builder.create();

        if (creation) {

            alertDlg.setTitle(getString(R.string.marker_create_string));
            alertDlg.setCancelable(false);
        }
        else {

            alertDlg.setTitle(getString(R.string.marker_edit_string));
        }

        alertDlg.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow (DialogInterface dialogInterface) {

                // MARKER DELETE BUTTON
                Button delMarkerButton = (Button) alertDlg.findViewById(R.id.marker_delete_button);

                assert delMarkerButton != null;

                if (creation) {

                    delMarkerButton.setEnabled(false);
                    delMarkerButton.setVisibility(View.GONE);
                }

                delMarkerButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick (View view) {

                        // Show confirmation dialog
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertBuilder.setMessage(getString(R.string.validate_marker_delete_string));

                        alertBuilder.setNegativeButton(getString(R.string.cancel_string), null);
                        alertBuilder.setPositiveButton(getString(R.string.delete_string), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                markerManager.remove(customMarker);
                                marker.remove();
                                alertDlg.dismiss();
                            }
                        });

                        alertBuilder.create().show();
                    }
                });

                // MARKER TYPE SPINNER
                Spinner markerTypeSpinner = (Spinner) alertDlg.findViewById(R.id.marker_type_spinner);
                assert markerTypeSpinner != null;

                if (!creation) {

                    if (customMarker.getType() == CustomMarker.MARKER_INFO) {

                        markerTypeSpinner.setSelection(0);
                    }
                    else {

                        markerTypeSpinner.setSelection(1);
                    }
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

                // MARKER TITLE EDIT
                EditText editTitle = (EditText) alertDlg.findViewById(R.id.marker_title);

                final String titleBefore = customMarker.getTitle();
                assert editTitle != null;
                editTitle.setText(titleBefore);

                Button btnOk = alertDlg.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnCancel = alertDlg.getButton(AlertDialog.BUTTON_NEGATIVE);

                btnOk.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick (View view) {

                        EditText editTitle = (EditText) alertDlg.findViewById(R.id.marker_title);

                        assert editTitle != null;
                        String currTitle = editTitle.getText().toString();

                        if (titleBefore != currTitle) {

                            if (currTitle.length() > 0) {

                                customMarker.setTitle(currTitle);

                                // Refresh marker's title
                                marker.hideInfoWindow();
                                marker.showInfoWindow();

                                alertDlg.dismiss();
                            }
                            else {

                                editTitle.setError(getString(R.string.marker_tittle_invalid_string));
                            }
                        }
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick (View view) {

                        if (creation) {

                            markerManager.remove(customMarker);
                            marker.remove();
                        }

                        alertDlg.dismiss();
                    }
                });
            }
        });

        alertDlg.show();
    }

    private void setMapListeners() {

        // MAP SHORT CLICK
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick (LatLng latLng) {

            }
        });

        // MAP LONG CLICK
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick (LatLng latLng) {

                Marker marker = markerManager.addMarker("", latLng, CustomMarker.MARKER_INFO);
                showMarkerEditDialog(marker, true);
            }
        });

        // MARKER CLICK
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick (Marker marker) {

                    return false;
                }
            }
        );

        // MARKER INFO WINDOW CLICK
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick (Marker marker) {

                CustomMarker customMarker = markerManager.getMarker(marker);

                if (customMarker.editable()) {

                    showMarkerEditDialog(marker, false);
                }
            }
        });

        // MARKER DRAG
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart (Marker marker) {

            }

            @Override
            public void onMarkerDrag (Marker marker) {

            }

            @Override
            public void onMarkerDragEnd (Marker marker) {

            }
        });
    }

    private void setPositionListeners() {

        posManager.setPositionChangedListener(new PositionManager.PositionListener() {

            @Override
            public void onRawPositionUpdate (Location location) {

                appManager.setLocation(location);
                lastPos = new LatLng(location.getLatitude(), location.getLongitude());

                if (!startMarkerAdded) {

                    startMarkerAdded = true;
                    markerManager.addMarker(getString(R.string.start_marker_string), lastPos, CustomMarker.MARKER_START);

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPos).zoom(15).bearing(0).tilt(0).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    if (progress != null && progress.isShowing()) {

                        progress.setMessage(getString(R.string.progress_loading_string));
                    }
                }

                // if (globalStatus == STATUS_t.CAR_MOVING) {

                    int currSpeed = posManager.getInstantSpeed();

                    if (currSpeed > maxSpeed) {

                        maxSpeed = currSpeed;
                    }

                    speedView.setSpeed(SPEED_t.IN_CORNERS, LEVEL_t.LEVEL_1, currSpeed);
                    // speedView.setSpeed(SPEED_t.IN_STRAIGHT_LINE, LEVEL_t.LEVEL_4, maxSpeed);

                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(lastPos));
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(lastPos).zoom(18).bearing(0).tilt(30).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                //}
            }

            @Override
            public void onPositionUpdate (Location prevLoc, Location currLoc) {

                if (globalStatus == STATUS_t.CAR_MOVING) {

                    LatLng prevPos = new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude());
                    LatLng currPos = new LatLng(currLoc.getLatitude(), currLoc.getLongitude());

                    drawLine(prevPos, currPos);
                }
            }
        });
    }

    private void setButtonListeners() {

        infoButton = (FloatingActionButton) findViewById(R.id.button_info);
        callButton = (FloatingActionButton) findViewById(R.id.button_call);
        formButton = (FloatingActionButton) findViewById(R.id.button_formation);
        settingsButton = (FloatingActionButton) findViewById(R.id.button_settings);

        // INFO
        infoButton.setOnClickListener (new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });

        // CALL
        callButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                showCallDialog();
            }
        });

        // FORM
        formButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (final View view) {

                startActivity(pinLockIntent);
            }
        });

        // SETTINGS
        settingsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (final View view) {

                // startActivity(pinLockIntent);
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        // MENU BUTTON
        optMenu = (FloatingActionMenu) findViewById(R.id.opt_menu);
        optMenu.setClosedOnTouchOutside(false);

        optMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {

            @Override
            public void onMenuToggle(boolean opened) {

                if (optMenu.isOpened()) {

                    hideActionButtons(true);
                }
                else {

                    hideActionButtons(false);
                }
            }
        });

        menuButton1 = (FloatingActionButton) findViewById(R.id.menu_button1);
        menuButton2 = (FloatingActionButton) findViewById(R.id.menu_button2);
        menuButton3 = (FloatingActionButton) findViewById(R.id.menu_button3);
        menuButton4 = (FloatingActionButton) findViewById(R.id.menu_button4);
        menuButton5 = (FloatingActionButton) findViewById(R.id.menu_button5);

        menuButton1.setImageResource(R.drawable.ic_action_play);

        menuButton1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                if (globalStatus == STATUS_t.CAR_PAUSING) {

                    menuButton1.setImageResource(R.drawable.ic_stop);
                }
                else if (globalStatus == STATUS_t.CAR_MOVING) {

                    menuButton1.setImageResource(R.drawable.ic_play);
                }

                optMenu.close(true);
            }
        });

        menuButton2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                if (mapType > GoogleMap.MAP_TYPE_HYBRID)
                {
                    mapType = GoogleMap.MAP_TYPE_NORMAL;
                }
                else
                {
                    mapType++;
                }

                googleMap.setMapType(mapType);
            }
        });

        menuButton3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

                optMenu.close(true);
            }
        });

        menuButton4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                appManager.on_constant_speed();
                optMenu.close(true);
            }
        });

        menuButton5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                appManager.on_acceleration();
                optMenu.close(true);
            }
        });
    }
}
