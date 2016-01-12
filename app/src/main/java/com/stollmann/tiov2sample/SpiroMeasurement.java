package com.stollmann.tiov2sample;

import android.util.Log;

import java.util.ArrayList;

public class SpiroMeasurement{

    private static double SampleTime = 0.01;
    private static double LPMfactor = 1.0 / 60.0;
    //private static double TOLERANCE = 0.0001;

    private double volumeAtLastZero = 0.0, slope = 0.0;

    ArrayList<SpiroDataPointCore> dataPoints;
    LinkedListBreathing listBreathing;

    //Initialialization first value of the array
    public SpiroMeasurement(){
        dataPoints = new ArrayList<SpiroDataPointCore>();
        SpiroDataPointCore firstDataPoint = new SpiroDataPointCore(0,0);
        firstDataPoint.setVolume(0.0);
        firstDataPoint.setTime(0.0);
        firstDataPoint.setFlow(0.0);
        dataPoints.add(firstDataPoint);
    }

    public void Append(double flow){

        SpiroDataPointCore lastDataPoint = getLastValue();
        listBreathing = new LinkedListBreathing();

        double deltaVolume = (0.5*SampleTime*LPMfactor)*(flow + lastDataPoint.getFlow());
        double currentTime = lastDataPoint.getTime() + SampleTime;
        double Volume = lastDataPoint.getVolume()+deltaVolume;

        //Log.i("TTflow", "       "+String.valueOf(flow));
        //Log.i("TTcurrentTime"," "+ String.valueOf(currentTime));
        //Log.i("TTVolume", String.valueOf(Volume));

        SpiroDataPointCore currentDataPoint = new SpiroDataPointCore(flow,currentTime,Volume);

        //Log.i("Flow: ", String.valueOf((lastDataPoint.getFlow() * flow)));
        //Log.i("FlowAbs: ", String.valueOf(Math.abs(flow)));

        //WHEN WE WORK WITH REAL DATA WE HAVE TO CHANGE THE SECOND THRESHOLD
        if(lastDataPoint.getFlow() * flow < 0 || Math.abs(flow) < 10){ //sign change && flow almost equals to 0
            slope = Volume-lastDataPoint.getVolume();
            //We just calculate the volume exhaled because it's enough to get the kCal
            if (slope > 0.0){ //This will mean that the slope of the volume curve is decreasing
                volumeAtLastZero = Volume;
            } else {
                Log.i("volume1", String.valueOf(volumeAtLastZero));
                volumeAtLastZero = Volume - volumeAtLastZero;
                Log.i("volume2", String.valueOf(volumeAtLastZero));
                listBreathing.addVolume(currentTime, volumeAtLastZero);
                listBreathing.addKcal(currentTime, kCalcalc(currentTime, volumeAtLastZero));
                Log.i("kCal", listBreathing.toStringKcal());
                //Log.i("kCal", String.valueOf(kCalcalc(currentTime, volumeAtLastZero)));
                Log.i("kCalTime", listBreathing.toStringKcalTime());
            }

        }

        dataPoints.add(currentDataPoint);

    }



    public void Append (short flow){
        double realFlow = (double)flow / 10.0;
        Append(realFlow);
    }

    public SpiroDataPointCore getLastValue(){
        return dataPoints.get(dataPoints.size()-1);
    }

    public SpiroDataPointCore getValue (int index){
        return dataPoints.get(index);
    }

    public double kCalcalc (double time, double VE){
        double kCal;
        double O2Expired = 0.1658;
        double VI, VO2, VCO2, RER, RERCalEq;

        VI = VE * ((0.99063 - (O2Expired + O2Expired)) / 0.7808);
        VO2 = (VI * 0.209) - (VE - O2Expired);
        VCO2 = VE * 0.0003 - VI * 0.0003;

        RER = VCO2 / VO2;
        RERCalEq = 3.815 + 1.232 * RER;

        kCal = VO2 * RERCalEq * time / 60.0;

        return kCal;
    }


}
