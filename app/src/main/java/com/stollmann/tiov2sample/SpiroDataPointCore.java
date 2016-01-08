package com.stollmann.tiov2sample;


public class SpiroDataPointCore {

    private double time;
    private double volume;
    private double flow;
    private double deltaVolume;
    private double calorie;


    public SpiroDataPointCore(double flow, double time){
        super();
        this.time = time;
        this.flow = flow;
    }

    public SpiroDataPointCore(double data, double timeSpiro, double volume){
        super();
        this.time = timeSpiro;
        this.flow = data;
        this.volume = volume;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getFlow() {
        return flow;
    }

    public void setFlow(double flow) {
        this.flow = flow;
    }

    public double getDeltaVolume() {
        return deltaVolume;
    }

    public void setDeltaVolume(double deltaVolume) {
        this.deltaVolume = deltaVolume;
    }

    public double getCalorie() {
        return calorie;
    }

    public void setCalorie(double calorie) {
        this.calorie = calorie;
    }
}

