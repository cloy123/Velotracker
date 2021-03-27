package com.govnokoder.velotracker.BL.Controller;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.govnokoder.velotracker.BL.LineList;
import com.govnokoder.velotracker.BL.Model.Date;
import com.govnokoder.velotracker.BL.Model.Time;
import com.govnokoder.velotracker.BL.Model.Training;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class TrainingController {
    private final String TRAINING_FILE_NAME = "data.json";
    private List<Training> trainings;
    private Training currentTraining;

    public TrainingController(Context context){
        trainings = LoadTrainingsData(context);
        if(trainings == null)
        {
            trainings = new ArrayList<>();
        }
    }

    public void DeleteTraining(int index){
        //Todo удаление конкретной записи по позиции в списке
    }

    public void DeleteAll(Context context) {
        trainings.clear();
        Save(context);
    }

    public void setNewTrainingData(Context context, Date date, Time time, double distance,
                                   double maxSpeed, double averageSpeed, List<LineList> lines,
                                   LatLng startPoint, List<Long> heights, long averageHeight, long maxHeight, long minHeight){
        currentTraining = new Training();
        currentTraining.Date = date;
        currentTraining.Time = time;
        currentTraining.Distance = distance;
        currentTraining.MaxSpeed = maxSpeed;
        currentTraining.AverageSpeed = averageSpeed;
        currentTraining.Lines = lines;

        currentTraining.setStartPoint(startPoint);

        currentTraining.MaxHeight = maxHeight;
        currentTraining.MinHeight = minHeight;
        currentTraining.AverageHeight = averageHeight;
        currentTraining.Heights = heights;

        trainings.add(currentTraining);
        Save(context);
    }

    public List<Training>  LoadTrainingsData(Context context)
    {
        InputStreamReader streamReader = null;
        FileInputStream fileInputStream = null;
        try{
            fileInputStream = context.openFileInput(TRAINING_FILE_NAME);
            streamReader = new InputStreamReader(fileInputStream);
            Gson gson = new Gson();
            DataItems dataItems = gson.fromJson(streamReader, DataItems.class);
            return  dataItems.getTrainings();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        finally {
            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private boolean Save(Context context)
    {
        Gson gson = new Gson();
        DataItems dataItems = new DataItems();
        dataItems.setTrainings(trainings);
        String jsonString = gson.toJson(dataItems);

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = context.openFileOutput(TRAINING_FILE_NAME, Context.MODE_PRIVATE);
            fileOutputStream.write(jsonString.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private static class DataItems {
        private List<Training> trainings;
        List<Training> getTrainings() {
            return trainings;
        }
        void setTrainings(List<Training> trainings) {
            this.trainings = trainings;
        }
    }
}


