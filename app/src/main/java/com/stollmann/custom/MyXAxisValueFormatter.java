package com.stollmann.custom;

import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;


public class MyXAxisValueFormatter implements XAxisValueFormatter {

    private DecimalFormat mFormat;

    public MyXAxisValueFormatter() {
        mFormat = new DecimalFormat("###,##0.00");
    }

    /*public MyXAxisValueFormatter() {
        mFormat = new DecimalFormat("###,##0");
    }*/

    @Override
    public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {
        return mFormat.format(index);
    }
}
