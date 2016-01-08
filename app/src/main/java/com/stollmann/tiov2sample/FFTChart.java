package com.stollmann.tiov2sample;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.stollmann.custom.MyYAxisValueFormatter;

import org.jtransforms.fft.DoubleFFT_1D;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by marcoscandelaboti on 07/12/15.
 */
public class FFTChart extends DemoBase implements OnChartValueSelectedListener {

    protected BarChart mChart;

    private Typeface mTf;

    private int fftWindowSize=0;
    double[] fftArray;
    double[]spectrum;
    double[] frequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barchart);
        mChart = (BarChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);

        mChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        // mChart.setDrawYLabels(false);

        mTf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTf);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);

        YAxisValueFormatter custom = new MyYAxisValueFormatter();

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(mTf);
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);

        mChart.getAxisRight().setEnabled(false);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        SpiroMeasurement measurement= PeripheralActivity.measurement1;
        fftWindowSize = measurement.dataPoints.size();
        DoubleFFT_1D fft = new DoubleFFT_1D(fftWindowSize);
        fftArray = new double[fftWindowSize];
        frequency= new double[fftWindowSize];
        spectrum = new double[fftWindowSize/2];

        for (int i=0; i<fftWindowSize; i++){
            fftArray[i]=measurement.getValue(i).getFlow();
            Log.i("frequency: ", String.valueOf(frequency[i]));
        }
        //The data is overwritten on the fftArray
        fft.realForward(fftArray);

        //We do this because in the fftArray there are real and imaginary values intercalated
        for (int i=0; i<fftWindowSize/2-1;i++){
            spectrum[i]= Math.sqrt(Math.pow(fftArray[2 * i],2)+Math.pow(fftArray[2 * i + 1],2));
            frequency[i] = (i * 100.0)/ (double)(fftWindowSize/2-1);
        }

        setData(fftWindowSize/2-1);
        // mChart.setDrawLegend(false);
    }

    private void setData(int count) {

        DecimalFormat df = new DecimalFormat("0.00");

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            if(frequency[i]>0.1 && frequency[i]<30.0){
                xVals.add(df.format(frequency[i]));
            }
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < count; i++) {
            if(frequency[i]>0.1 && frequency[i]<30.0){
                float val = (float) (spectrum[i]);
                yVals1.add(new BarEntry(val, i));
            }
        }

        BarDataSet set1 = new BarDataSet(yVals1, "DataSet");
        set1.setBarSpacePercent(35f);
        set1.setColor(Color.BLUE);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10f);
        data.setValueTypeface(mTf);

        mChart.setData(data);
    }

    @SuppressLint("NewApi")
    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

        if (e == null)
            return;

        RectF bounds = mChart.getBarBounds((BarEntry) e);
        PointF position = mChart.getPosition(e, YAxis.AxisDependency.LEFT);

        Log.i("bounds", bounds.toString());
        Log.i("position", position.toString());

        Log.i("x-index",
                "low: " + mChart.getLowestVisibleXIndex() + ", high: "
                        + mChart.getHighestVisibleXIndex());
    }

    public void onNothingSelected() {
    };
}
