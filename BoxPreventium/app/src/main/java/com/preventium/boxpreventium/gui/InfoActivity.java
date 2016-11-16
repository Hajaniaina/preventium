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

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import java.util.ArrayList;

public class InfoActivity extends AppCompatActivity {

    private PieChart[] chartArr;
    private int[] colors;

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        colors = new int[5];
        colors[0] = ContextCompat.getColor(this, R.color.colorAppGreen);
        colors[1] = ContextCompat.getColor(this, R.color.colorAppBlue);
        colors[2] = ContextCompat.getColor(this, R.color.colorAppYellow);
        colors[3] = ContextCompat.getColor(this, R.color.colorAppOrange);
        colors[4] = ContextCompat.getColor(this, R.color.colorAppRed);

        chartArr = new PieChart[6];

        chartArr[0] = (PieChart) findViewById(R.id.pie_chart1);
        chartArr[1] = (PieChart) findViewById(R.id.pie_chart2);
        chartArr[2] = (PieChart) findViewById(R.id.pie_chart3);
        chartArr[3] = (PieChart) findViewById(R.id.pie_chart4);
        chartArr[4] = (PieChart) findViewById(R.id.pie_chart5);
        chartArr[5] = (PieChart) findViewById(R.id.pie_chart6);

        for (int i = 0; i < chartArr.length; i++) {

            chartArr[i].getLegend().setEnabled(false);
            chartArr[i].setRotationEnabled(false);
            chartArr[i].setTouchEnabled(false);
            chartArr[i].setDescription("");
            chartArr[i].setBackgroundColor(Color.WHITE);
            chartArr[i].setHoleColor(Color.WHITE);
            chartArr[i].setTransparentCircleColor(Color.WHITE);
            chartArr[i].setUsePercentValues(true);
            chartArr[i].setDrawHoleEnabled(true);
            chartArr[i].setTransparentCircleAlpha(110);
            chartArr[i].setHoleRadius(58f);
            chartArr[i].setTransparentCircleRadius(61f);
            chartArr[i].setCenterTextSize(20f);
            chartArr[i].setCenterText("Hello!");

            setData(i, 5, 100);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void setData (int id, int count, float range) {

        ArrayList<PieEntry> values = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            values.add(new PieEntry((float) ((Math.random() * range) + range / 5), ""));
        }

        PieDataSet dataSet = new PieDataSet(values, "Election Results");

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);

        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(18f);
        data.setValueTextColor(Color.WHITE);

        chartArr[id].setData(data);
        chartArr[id].invalidate();
    }
}
