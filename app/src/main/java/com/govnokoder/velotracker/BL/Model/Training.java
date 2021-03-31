package com.govnokoder.velotracker.BL.Model;

import com.govnokoder.velotracker.BL.LineList;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Training {
    public Date Date = new Date(0,0,0);
    public Time Time = new Time(0,0,0);
    public double Distance = 0;
    public double MaxSpeed = 0;
    public double AverageSpeed = 0;
    public List<LineList> Lines = new ArrayList<>();

    private double startLatitude;
    private double startLongitude;

    public long MaxHeight = 0;
    public long MinHeight = 0;
    public long AverageHeight = 0;
    public List<Long> Heights = new ArrayList<>();
    public List<Double> Speeds = new ArrayList<>();

    public static enum  Units{
        MILES,
        KILOMETERS,
        METERS
    }

    public Time getTemp(Units unit){
        if(Distance > 0 && Time != null && Time.getAllSeconds() > 0) {
            long allSeconds = Time.getAllSeconds();;
            long tempSeconds;
            switch (unit) {
                case MILES:
                    tempSeconds = (long) (allSeconds / convertToMiles(Distance));
                    return Time.getTimeFromSeconds(tempSeconds);
                case KILOMETERS:
                    tempSeconds = (long) (allSeconds / Distance);
                    return Time.getTimeFromSeconds(tempSeconds);
            }
        }
        return new Time(0,0,0);
    }

    public double convertToMiles(double value) {
        return value / 1.609344;
    }


    public LatLng getStartPoint(){
        return new LatLng(startLatitude, startLongitude);
    }

    public void  setStartPoint(LatLng latLng) {
        startLatitude = latLng.getLatitude() ;
        startLongitude = latLng.getLongitude();
    }

    public Training(Date date, Time time, double distance,
                    double maxSpeed, double averageSpeed,
                    List<LineList> lines, LatLng startPoint,
                    long maxHeight, long minHeight, long averageHeight) {
        Date = date;
        Time = time;
        Distance = distance;
        MaxSpeed = maxSpeed;
        AverageSpeed = averageSpeed;
        Lines = lines;
        startLatitude = startPoint.getLatitude();
        startLongitude = startPoint.getLongitude();
        MaxHeight = maxHeight;
        MinHeight = minHeight;
        AverageHeight = averageHeight;
    }

    public Training() { }

    public static double round(double value, int places) {
        if(Double.isNaN(value)) { return 0; }
        if (places < 0) { throw new IllegalArgumentException(); }
        if(value == 0){return 0;}
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}



