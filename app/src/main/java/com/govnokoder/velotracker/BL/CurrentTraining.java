package com.govnokoder.velotracker.BL;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.govnokoder.velotracker.BL.Controller.TrainingController;
import com.govnokoder.velotracker.BL.Model.Date;
import com.govnokoder.velotracker.BL.Model.Time;
import com.govnokoder.velotracker.BL.Model.Training;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CurrentTraining{

    private static final String TRAINING_FILE_NAME = "CurrentTraining.json";

    public Date Date = new Date(0,0,0);
    public Time Time = new Time(0,0,0);

    public double WayLength = 0;//в *.***км

    public List<Double> SpeedList = new ArrayList<>();
    public double MaxSpeed = 0;
    public double CurrentSpeed = 0;
    public double SumSpeed = 0;
    public double AverageSpeed = 0;

    public List<List<LatLng>> Lines = new ArrayList<>();
    public List<LatLng> CurrentLine = new ArrayList<>();

    public List<Long> Heights = new ArrayList<>();
    public long MaxHeight = Long.MIN_VALUE;
    public long MinHeight = Long.MAX_VALUE;
    public long SumHeight = 0;
    public long AverageHeight = 0;

    public boolean isRunning = true;

    public Location originLocation = null;

    public void Pause(){
        isRunning = false;
        List<LatLng> q = new ArrayList<>();
        for (LatLng latlng: CurrentLine) {
            q.add(new LatLng(latlng.getLatitude(), latlng.getLongitude()));
        }Lines.add(q);
        CurrentLine = new ArrayList<>();
    }

    public void Resume(){
        isRunning = true;
    }

    public void StopAndSave(Context context, Time time) {
        TrainingController trainingController = new TrainingController(context);
        AverageSpeed = ((WayLength*1000) / (time.Hours*3600 + time.Minutes*60 + time.Seconds))*3.6;
        if(Lines.size() == 0) {
            return;
        }
        LatLng startPoint = new LatLng(Lines.get(0).get(0).getLatitude(), Lines.get(0).get(0).getLongitude());
        trainingController.setNewTrainingData(context, Date, time, WayLength, MaxSpeed, AverageSpeed, Lines, startPoint, Heights, AverageHeight, MaxHeight, MinHeight);
    }


    public CurrentTraining() {
    }

    public void setValuesFromLocation(Location location){
        if (location != null) {
            if (location.hasSpeed()) {
                double speed = location.getSpeed() * 3.6;
                double distance = 0;
                if (originLocation != null) {
                    distance = originLocation.distanceTo(location) / 1000;
                }
                long height = (long) location.getAltitude();
                if (isRunning) {
                    //скорость
                    if (speed > MaxSpeed) {
                        MaxSpeed = speed;
                    }
                    CurrentSpeed = speed;
                    //длина пути
                    WayLength += distance;
                    //путь
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CurrentLine.add(latLng);
                    //Высота
                    Heights.add(height);
                    MinHeight = Long.min(height, MinHeight);
                    MaxHeight = Long.max(height, MaxHeight);
                    SumHeight += height;
                    AverageHeight = SumHeight / Heights.size();
                }
            } else {
                //скорость
                CurrentSpeed = 0;
            }
            originLocation = location;
        }
    }

}
