package com.stollmann.tiov2sample;


public class ZeroRespiration {

    public double FloatingIndex;
    public double Volume;
    public ZeroRespiration Previous;
    public ZeroRespiration Next;

    public ZeroRespiration (double floatingIndex, double volume){
        this.FloatingIndex = floatingIndex;
        this.Volume = volume;

    }

    public double Time(){
        return FloatingIndex*0.01;
    }

    public double VolumeBefore(){
        if (Previous == null) return 0;
        else return Volume-Previous.Volume;
    }

    public double VolumeAfter(){
        if(Next == null) return 0;
        else return Next.Volume - this.Volume;
    }

    //TimeToLast, TimeToNext


    public boolean IsSignificant(){
        if (Previous == null || Next == null) return true;
        else return (IsBeginningOfInspiration() || IsEndOfInspiration() || IsBeginningOfExpiration() || IsEndOfExpiration());
    }

    public boolean IsBeginningOfExpiration(){
        return VolumeAfter() > 0.25;
    }

    public boolean IsEndOfExpiration(){
        return VolumeBefore() > 0.25;
    }

    public boolean IsBeginningOfInspiration(){
        return VolumeAfter() > -0.25;
    }

    public boolean IsEndOfInspiration(){
        return VolumeBefore() > -0.25;
    }

    public boolean IsDirectionChange(){
        return (IsEndOfExpiration() && IsBeginningOfInspiration()) || (IsBeginningOfExpiration() && IsEndOfInspiration());
    }

    public boolean Equals(double time){
        return (Math.abs(time-FloatingIndex*0.01)<0.01);
    }

}
