package com.preventium.boxpreventium.gui;

import android.graphics.Color;
import android.os.Bundle;

import com.preventium.boxpreventium.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.location.PositionManager;
import com.preventium.boxpreventium.manager.StatsLastDriving;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity {

    private static final String TAG = "StatsActivity";

    private static final int ACC_OBJ = 0;
    private static final int ACC_RES = 1;
    private static final int BRK_OBJ = 2;
    private static final int BRK_RES = 3;
    private static final int CRN_OBJ = 4;
    private static final int CRN_RES = 5;

    private PieChart[] chartArr;
    private int[] colors;

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        TextView imeiView = (TextView) findViewById(R.id.textview_imei);
        TextView startTimeView = (TextView) findViewById(R.id.textview_start_time);
        TextView timeElapsedView = (TextView) findViewById(R.id.textview_time_elapsed);
        TextView distanceView = (TextView) findViewById(R.id.textview_distance);
        TextView avgSpeedView = (TextView) findViewById(R.id.textview_avg_speed);
        TextView avgScoreView = (TextView) findViewById(R.id.textview_avg_score);

        imeiView.setText("IMEI: " + StatsLastDriving.getIMEI(this));

        long timestamp = StatsLastDriving.get_start_at(this);
        String startTime = "00:00:00";

        if (timestamp > 0) {

            startTime = getDate(StatsLastDriving.get_start_at(this));
        }

        startTimeView.setText(getString(R.string.start_time_string) + ": " + startTime);

        String timeElapsed = getTime(StatsLastDriving.get_times(this));
        timeElapsedView.setText(getString(R.string.time_elapsed_string) + ": " + timeElapsed);

        float distanceMeters = StatsLastDriving.get_distance(this);
        String distance = getString(R.string.distance_string) + ": ";

        if (distanceMeters < 1000) {

            distance += String.valueOf(distanceMeters) + "m";
        }
        else {

            distanceMeters = distanceMeters / (float) 1000.0;
            distance += String.valueOf(distanceMeters) + "km";
        }

        distanceView.setText(distance);

        float speed = StatsLastDriving.get_speed_avg(this) * PositionManager.MS_TO_KMPH;
        String avgSpeed = String.valueOf(speed) + " km/h";

        avgSpeedView.setText(getString(R.string.avg_speed_string) + ": " + avgSpeed);
        avgScoreView.setText(getString(R.string.avg_score_string) + ": " + String.valueOf(StatsLastDriving.get_note(this)));

        colors = new int[5];

        colors[0] = ContextCompat.getColor(this, R.color.colorAppGreen);
        colors[1] = ContextCompat.getColor(this, R.color.colorAppBlue);
        colors[2] = ContextCompat.getColor(this, R.color.colorAppYellow);
        colors[3] = ContextCompat.getColor(this, R.color.colorAppOrange);
        colors[4] = ContextCompat.getColor(this, R.color.colorAppRed);

        chartArr = new PieChart[6];

        chartArr[ACC_OBJ] = (PieChart) findViewById(R.id.pie_chart_acc_obj);
        chartArr[ACC_RES] = (PieChart) findViewById(R.id.pie_chart_acc_result);
        chartArr[BRK_OBJ] = (PieChart) findViewById(R.id.pie_chart_brake_obj);
        chartArr[BRK_RES] = (PieChart) findViewById(R.id.pie_chart_brake_result);
        chartArr[CRN_OBJ] = (PieChart) findViewById(R.id.pie_chart_corner_obj);
        chartArr[CRN_RES] = (PieChart) findViewById(R.id.pie_chart_corner_result);

        String objStr = getString(R.string.obj_string);
        String resStr = getString(R.string.results_string);
        String accStr = getString(R.string.acc_string);
        String brakeStr = getString(R.string.brakes_string);
        String cornerStr = getString(R.string.corners_string);

        for (PieChart chart : chartArr) {

            chart.getLegend().setEnabled(false);
            chart.setRotationEnabled(false);
            chart.setTouchEnabled(true);
            chart.setDescription("");
            chart.setBackgroundColor(Color.WHITE);
            chart.setHoleColor(Color.WHITE);
            chart.setTransparentCircleColor(Color.WHITE);
            chart.setUsePercentValues(false);
            chart.setDrawHoleEnabled(true);
            chart.setDrawSlicesUnderHole(false);
            chart.setHoleRadius(58f);
            chart.setTransparentCircleAlpha(110);
            chart.setTransparentCircleRadius(61f);
            chart.setCenterTextSize(18f);
        }

        chartArr[ACC_OBJ].setCenterText(accStr + System.getProperty("line.separator") + objStr);
        chartArr[ACC_RES].setCenterText(accStr + System.getProperty("line.separator") + resStr);
        chartArr[BRK_OBJ].setCenterText(brakeStr + System.getProperty("line.separator") + objStr);
        chartArr[BRK_RES].setCenterText(brakeStr + System.getProperty("line.separator") + resStr);
        chartArr[CRN_OBJ].setCenterText(cornerStr + System.getProperty("line.separator") + objStr);
        chartArr[CRN_RES].setCenterText(cornerStr + System.getProperty("line.separator") + resStr);

        for (int i = 0; i < 6; i++) {

            updateData(i);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });
    }

    private void updateData (int id) {

        if (id < ACC_OBJ) id = ACC_OBJ;
        if (id > CRN_RES) id = CRN_RES;

        int i = 0;
        int[] values = new int[5];

        for (LEVEL_t level : LEVEL_t.values()) {

            if (level != LEVEL_t.LEVEL_UNKNOW) {

                switch (id) {

                    case ACC_OBJ: values[i] = StatsLastDriving.get_objectif_A(this, level);
                        break;

                    case ACC_RES: values[i] = StatsLastDriving.get_resultat_A(this, level);
                        break;

                    case BRK_OBJ: values[i] = StatsLastDriving.get_objectif_F(this, level);
                        break;

                    case BRK_RES: values[i] = StatsLastDriving.get_resultat_F(this, level);
                        break;

                    case CRN_OBJ: values[i] = StatsLastDriving.get_objectif_V(this, level);
                        break;

                    case CRN_RES: values[i] = StatsLastDriving.get_resultat_V(this, level);
                        break;
                }

                i++;
            }
        }

        ArrayList<PieEntry> arrayList = new ArrayList<>();

        for (int k = 0; k < 5; k++) {

            if (values[k] > 0) {                                                                    // REMOVE 0% RESULTS. TO BE TESTED

                arrayList.add(new PieEntry(values[k], ""));
            }
        }

        PieDataSet pieDataSet = new PieDataSet(arrayList, "");

        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);
        pieDataSet.setColors(colors);

        PieData pieData = new PieData(pieDataSet);

        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(16f);
        pieData.setValueTextColor(Color.WHITE);

        chartArr[id].setData(pieData);
        chartArr[id].invalidate();
    }

    public static String getTime (long tempsS) {

        int h = (int) (tempsS / 3600);
        int m = (int) ((tempsS % 3600) / 60);
        int s = (int) (tempsS % 60);

        return String.format(Locale.getDefault(),"%02d:%02d:%02d",h,m,s);
    }

    private String getDate (long time) {

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time);

        return DateFormat.format("d MMM HH:mm:ss", cal).toString();
    }
}
