package com.preventium.boxpreventium.gui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.guardanis.applock.CreateLockDialogBuilder;
import com.guardanis.applock.UnlockDialogBuilder;
import com.guardanis.applock.locking.ActionLockingHelper;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";

    private PositionManager posManager;
    private MarkerManager markerManager;
    private ScoreViewManager scoreView;
    private SpeedViewManager speedView;
    private ModeManager modeManager;

    private ProgressDialog progress;
    private TextView drivingTimeView;
    private TextView boxNumView;
    private FloatingActionMenu optMenu;
    private FloatingActionButton infoButton;
    private FloatingActionButton callButton;
    private FloatingActionButton formButton;
    private FloatingActionButton settingsButton;
    private FloatingActionButton menuButton1;
    private FloatingActionButton menuButton2;
    private FloatingActionButton menuButton3;

    private GoogleMap mGoogleMap;
    private SupportMapFragment mapFrag;
    private LatLng currPos, lastPos;
    private ArrayList<Polyline> roadLinesArr;
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;
    private boolean startMarkerAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.loading_string));
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setProgressNumberFormat(null);
        progress.setProgressPercentFormat(null);
        progress.show();

        roadLinesArr = new ArrayList<Polyline>();

        scoreView = new ScoreViewManager(this);
        speedView = new SpeedViewManager(this);
        markerManager = new MarkerManager();

        drivingTimeView = (TextView) findViewById(R.id.driving_time_text);
        boxNumView = (TextView) findViewById(R.id.box_num_connected);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public void onMapReady (GoogleMap googleMap) {

        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.getUiSettings().setAllGesturesEnabled(true);

        posManager = new PositionManager(this);
        modeManager = new ModeManager(posManager);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mGoogleMap.setMyLocationEnabled(true);
        markerManager.setMap(mGoogleMap);

        setMapListeners();
        setButtonListeners();
        setPositionListeners();
        setModeListeners();
    }

    private void drawLine (LatLng startPoint, LatLng endPoint) {

        PolylineOptions opt = new PolylineOptions();

        opt.color(Color.rgb(0, 160, 255));
        opt.geodesic(true);
        opt.width(3);
        opt.add(startPoint, endPoint);

        roadLinesArr.add(mGoogleMap.addPolyline(opt));
    }

    private void disableActionButtons(boolean disable) {

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

    protected void showMarkerEditDialog (final CustomMarker customMarker, final boolean creation) {

        if (customMarker == null) {

            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
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
                                alertDlg.dismiss();
                            }
                        });

                        alertBuilder.create().show();
                    }
                });

                // MARKER TYPE SPINNER
                Spinner markerTypeSpinner = (Spinner) alertDlg.findViewById(R.id.marker_type_spinner);

                assert markerTypeSpinner != null;
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
                                customMarker.getMarker().hideInfoWindow();
                                customMarker.getMarker().showInfoWindow();

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
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick (LatLng latLng) {

            }
        });

        // MAP LONG CLICK
        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick (LatLng latLng) {

                CustomMarker customMarker = markerManager.addMarker("", latLng, CustomMarker.MARKER_INFO);
                showMarkerEditDialog(customMarker, true);
            }
        });

        // MARKER CLICK
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick (Marker marker) {

                    return false;
                }
            }
        );

        // MARKER INFO WINDOW CLICK
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick (Marker marker) {

                CustomMarker customMarker = markerManager.getCustomMarker(marker);

                if (customMarker.editable()) {

                    showMarkerEditDialog(customMarker, false);
                }
            }
        });

        // MARKER DRAG
        mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

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

        posManager.setOnPositionChangedListener(new PositionManager.PositionListener() {

            @Override
            public void onRawPositionUpdate (Location location) {

                lastPos = currPos;
                currPos = new LatLng(location.getLatitude(), location.getLongitude());

                if (!startMarkerAdded) {

                    startMarkerAdded = true;
                    markerManager.addMarker(getString(R.string.start_marker_string), currPos, CustomMarker.MARKER_START);

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(currPos).zoom(15).bearing(0).tilt(0).build();
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    progress.hide();
                }
            }

            @Override
            public void onPositionUpdate (Location location) {

                int currMode = modeManager.getMode();

                if (currMode == ModeManager.MOVING) {

                    speedView.setSpeed(SpeedViewManager.SPEED_MAX, posManager.getLastSpeed());
                    drawLine(lastPos, currPos);

                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currPos));
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(currPos).zoom(18).bearing(0).tilt(30).build();
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });
    }

    private void setModeListeners() {

        modeManager.setModeChangeListener(new ModeManager.ModeChangeListener() {

            @Override
            public void onModeChanged (int mode) {}

            @Override
            public void onStopMode() {

                Log.d(TAG, "On Stop");

                CameraPosition cameraPosition = new CameraPosition.Builder().target(currPos).zoom(15).bearing(0).tilt(0).build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                mGoogleMap.getUiSettings().setAllGesturesEnabled(true);

                disableActionButtons(false);
                // scoreView.disable(true);
                // speedView.disable(true);
            }

            @Override
            public void onPauseMode() {

                Log.d(TAG, "On Pause");
            }

            @Override
            public void onMovingMode() {

                Log.d(TAG, "On Move");

                mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
                disableActionButtons(true);
                // scoreView.disable(false);
                // speedView.disable(false);
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

            }
        });

        // FORM
        formButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (final View view) {

            }
        });

        // SETTINGS
        settingsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (final View view) {

                if (ActionLockingHelper.hasSavedPIN(MainActivity.this)) {

                    ActionLockingHelper.unlockIfRequired(MainActivity.this, new UnlockDialogBuilder.UnlockEventListener(){

                        // Dialog was closed without entry
                        public void onCanceled() {}

                        // Not called with default Dialog, instead is handled internally
                        public void onUnlockFailed (String reason) {}

                        public void onUnlockSuccessful() {

                            // Snackbar.make(view, "Unlock Successful", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }
                    });
                }
                else {

                    new CreateLockDialogBuilder(MainActivity.this, new CreateLockDialogBuilder.LockCreationListener() {

                        public void onLockCanceled() {}
                        public void onLockSuccessful() {}

                    }).show();
                }
            }
        });

        // MENU BUTTON
        optMenu = (FloatingActionMenu) findViewById(R.id.opt_menu);
        optMenu.setClosedOnTouchOutside(true);

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

        menuButton1.setImageResource(R.drawable.ic_action_play);
        menuButton1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                int currMode = modeManager.getMode();

                if (currMode == ModeManager.STOP) {

                    menuButton1.setImageResource(R.drawable.ic_action_stop);
                    modeManager.setMode(ModeManager.MOVING);
                }
                else if (currMode == ModeManager.MOVING) {

                    menuButton1.setImageResource(R.drawable.ic_action_play);
                    modeManager.setMode(ModeManager.STOP);
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

                mGoogleMap.setMapType(mapType);
            }
        });

        menuButton3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View view) {

            }
        });
    }
}
