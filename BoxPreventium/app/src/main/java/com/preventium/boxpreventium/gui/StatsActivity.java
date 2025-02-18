package com.preventium.boxpreventium.gui;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.SCORE_t;
import com.preventium.boxpreventium.location.PositionManager;
import com.preventium.boxpreventium.manager.StatsLastDriving;
import com.preventium.boxpreventium.utils.ComonUtils;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return true;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        TextView appVerView = (TextView) findViewById(R.id.textview_app_ver);
        TextView imeiView = (TextView) findViewById(R.id.textview_imei);
        TextView driverIdView = (TextView) findViewById(R.id.textview_driver_id);
        TextView driverNameView = (TextView) findViewById(R.id.textview_driver_name);
        TextView startTimeView = (TextView) findViewById(R.id.textview_start_time);
        TextView timeElapsedView = (TextView) findViewById(R.id.textview_time_elapsed);
        TextView distanceView = (TextView) findViewById(R.id.textview_distance);
        TextView avgSpeedView = (TextView) findViewById(R.id.textview_avg_speed);
        TextView avgScoreView = (TextView) findViewById(R.id.textview_avg_score);

        appVerView.setText("App Version: " + ComonUtils.getVersionName(getApplicationContext()));
        imeiView.setText("IMEI: " + StatsLastDriving.getIMEI(getApplicationContext()));

        long driverId = sharedPref.getLong(getString(R.string.driver_id_key), 0);
        driverIdView.setText(getString(R.string.driver_id_string) + ": " + driverId);

        String driverName = sharedPref.getString(getString(R.string.driver_name_key), "");
        driverNameView.setText(getString(R.string.driver_name_string) + ": " + driverName);

        long timestamp = StatsLastDriving.get_start_at(getApplicationContext());
        String startTime = "00:00:00";

        if (timestamp > 0) {

            startTime = getDate(StatsLastDriving.get_start_at(getApplicationContext()));
        }

        startTimeView.setText(getString(R.string.start_time_string) + ": " + startTime);

        String timeElapsed = getTime(StatsLastDriving.get_times(getApplicationContext()));
        timeElapsedView.setText(getString(R.string.time_elapsed_string) + ": " + timeElapsed);

        float distanceMeters = StatsLastDriving.get_distance(getApplicationContext());
        String distance = getString(R.string.distance_string) + ": ";

        if (distanceMeters < 1000) {

            distance += String.valueOf(ComonUtils.round(distanceMeters)) + "m";
        }
        else {

            distanceMeters = distanceMeters / (float) 1000.0;
            distance += String.valueOf(ComonUtils.round(distanceMeters)) + "km";
        }

        distanceView.setText(distance);

        float speed = StatsLastDriving.get_speed_avg(getApplicationContext()) * PositionManager.MS_TO_KMPH;
        String speedStr = String.valueOf(ComonUtils.round(speed)) + " km/h";
        avgSpeedView.setText(getString(R.string.avg_speed_string) + ": " + speedStr);

        float score = StatsLastDriving.get_note(getApplicationContext(), SCORE_t.FINAL);
        String scoreStr = String.valueOf(ComonUtils.round(score));
        avgScoreView.setText(getString(R.string.avg_score_string) + ": " + scoreStr);

        colors = new int[5];

        colors[0] = ContextCompat.getColor(getApplicationContext(), R.color.colorAppGreen);
        colors[1] = ContextCompat.getColor(getApplicationContext(), R.color.colorAppBlue);
        colors[2] = ContextCompat.getColor(getApplicationContext(), R.color.colorAppYellow);
        colors[3] = ContextCompat.getColor(getApplicationContext(), R.color.colorAppOrange);
        colors[4] = ContextCompat.getColor(getApplicationContext(), R.color.colorAppRed);

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
            chart.setCenterTextSize(16f);
        }

        chartArr[ACC_OBJ].setCenterText(objStr + "\n" + accStr);
        score = StatsLastDriving.get_note(getApplicationContext(), SCORE_t.ACCELERATING);
        scoreStr = String.valueOf(ComonUtils.round(score));
        chartArr[ACC_RES].setCenterText(resStr + "\n" + accStr + ":" + "\n" + scoreStr);

        chartArr[BRK_OBJ].setCenterText(objStr + "\n" + brakeStr);
        score = StatsLastDriving.get_note(getApplicationContext(), SCORE_t.BRAKING);
        scoreStr = String.valueOf(ComonUtils.round(score));
        chartArr[BRK_RES].setCenterText(resStr + "\n" + brakeStr + ":" + "\n" + scoreStr);

        chartArr[CRN_OBJ].setCenterText(objStr + "\n" + cornerStr);
        score = StatsLastDriving.get_note(getApplicationContext(), SCORE_t.CORNERING);
        scoreStr = String.valueOf(ComonUtils.round(score));
        chartArr[CRN_RES].setCenterText(resStr + "\n" + cornerStr + ":" + "\n" + scoreStr);

        for (int i = 0; i < 6; i++) {

            updateData(i);
        }
    }

    private void updateData (int id) {

        if (id < ACC_OBJ) id = ACC_OBJ;
        if (id > CRN_RES) id = CRN_RES;

        int i = 0;
        float[] values = new float[5];

        for (LEVEL_t level : LEVEL_t.values()) {

            if (level != LEVEL_t.LEVEL_UNKNOW) {

                switch (id) {

                    case ACC_OBJ: values[i] = ComonUtils.round(StatsLastDriving.get_objectif_A(getApplicationContext(), level));
                        break;

                    case ACC_RES: values[i] = ComonUtils.round(StatsLastDriving.get_resultat_A(getApplicationContext(), level));
                        break;

                    case BRK_OBJ: values[i] = ComonUtils.round(StatsLastDriving.get_objectif_F(getApplicationContext(), level));
                        break;

                    case BRK_RES: values[i] = ComonUtils.round(StatsLastDriving.get_resultat_F(getApplicationContext(), level));
                        break;

                    case CRN_OBJ: values[i] = ComonUtils.round(StatsLastDriving.get_objectif_V(getApplicationContext(), level));
                        break;

                    case CRN_RES: values[i] = ComonUtils.round(StatsLastDriving.get_resultat_V(getApplicationContext(), level));
                        break;
                }

                i++;
            }
        }

        ArrayList<PieEntry> arrayList = new ArrayList<>();

        for (int k = 0; k < 5; k++) {
            arrayList.add(new PieEntry(values[k], ""));
        }

        PieDataSet pieDataSet = new PieDataSet(arrayList, "");
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);
        pieDataSet.setColors(colors);

        PieData pieData = new PieData(pieDataSet);
        //pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if( value <= 2.0 )
                    return "";
                else if( value < 5.0 )
                    return String.format(Locale.getDefault(),"%.0f", value);

                return String.format(Locale.getDefault(),"%.1f%%", value);
            }
        });
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
