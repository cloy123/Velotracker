package com.govnokoder.velotracker.BL.Model;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Training {
    public Date Date = new Date(0,0,0);
    public Time AllTime = new Time(0,0,0);
    public double WayLength = 0;
    public double MaxSpeed = 0;
    public double AverageSpeed = 0;
    public List<List<LatLng>> Lines = new ArrayList<>();

    private double startLatitude;
    private double startLongitude;

    public long MaxHeight;
    public long MinHeight;
    public long AverageHeight;
    public List<Long> Heights = new ArrayList<>();

    public Time getTemp(String unit){
        if(WayLength > 0 && AllTime != null && AllTime.getAllSeconds() > 0) {
            int allSeconds;
            int tempSeconds;
            switch (unit) {
                case "m":
                    allSeconds = AllTime.getAllSeconds();
                    tempSeconds = (int) (allSeconds / convertToMiles(WayLength));
                    return Time.getTimeFromSeconds(tempSeconds);
                case "k":
                    allSeconds = AllTime.getAllSeconds();
                    tempSeconds = (int) (allSeconds / WayLength);
                    return Time.getTimeFromSeconds(tempSeconds);
            }
        }
        return null;
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

    public Training(Date date, Time time, double wayLength,
                    double maxSpeed, double averageSpeed,
                    List<List<LatLng>> lines, LatLng startPoint,
                    long maxHeight, long minHeight, long averageHeight) {
        Date = date;
        AllTime = time;
        WayLength = wayLength;
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
        if (places < 0) { throw new IllegalArgumentException(); }
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}



