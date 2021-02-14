package com.govnokoder.velotracker.BL;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

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
    public Time AllTime = new Time(0,0,0);

    public double WayLength = 0;//в *.***км
    public double MaxSpeed = 0;
    public double AverageSpeed = 0;

    public List<List<LatLng>> Lines = new ArrayList<>();
    public List<LatLng> currentLine = new ArrayList<>();

    public List<Integer> heights = new ArrayList<>();

    public boolean isRunning = true;

    public Location originLocation = null;

    public void Pause(){
        isRunning = false;
        List<LatLng> q = new ArrayList<>();
        for (LatLng latlng: currentLine) {
            q.add(new LatLng(latlng.getLatitude(), latlng.getLongitude()));
        }Lines.add(q);
        currentLine = new ArrayList<>();
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
        trainingController.setNewTrainingData(context, Date, time, WayLength, MaxSpeed, AverageSpeed, Lines, startPoint, heights);
    }


    public CurrentTraining() {
    }
}
