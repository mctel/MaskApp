package com.stollmann.tiov2sample;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by marcoscandelaboti on 15/12/15.
 */
public class CalorieMeasurement {

    double Volume, O2Expired, VO2, VCO2, VI, RER, RERCalEq, time, kCAl;

    SpiroMeasurement measurement = new SpiroMeasurement();
    ArrayList<SpiroDataPointCore> dataPoints;
    double volumeAtFlowChange=0.0;

    public void CaloriesCalc (short data){

        measurement.Append(data);
        setCalories();

    }

    public void setCalories(){

        double deltaVF;

        if(measurement.getLastValue().getFlow() == 0.0) {

            Volume = measurement.getLastValue().getVolume();
            time = measurement.getLastValue().getTime();
            O2Expired = 0.1658;

            deltaVF = Volume - volumeAtFlowChange;
            volumeAtFlowChange = Volume;

            Log.i("Volume", String.valueOf(Volume));
            Log.i("volumeAtFlowChange", String.valueOf(volumeAtFlowChange));


            VI = deltaVF * ((0.99063 - (O2Expired + O2Expired)) / 0.7808);

            VO2 = (VI * 0.209) - (deltaVF - O2Expired);
            VCO2 = deltaVF * 0.0003 - VI * 0.0003;

            RER = VCO2 / VO2;
            RERCalEq = 3.815 + 1.232 * RER;

            kCAl = VO2 * RERCalEq * time / 60.0;

            SpiroDataPointCore spiroDataPointCore = new SpiroDataPointCore(deltaVF, time);
            dataPoints.add(spiroDataPointCore);

        }
    }

}
