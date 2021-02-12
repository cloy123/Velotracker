package com.govnokoder.velotracker.BL.Controller;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
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

    }

    public void DeleteAll(Context context) {
        trainings.clear();
        Save(context);
    }




    public void setNewTrainingData(Context context, Date date, Time time, double wayLength,
                                   double maxSpeed, double averageSpeed, List<List<LatLng>> lines,
                                   LatLng startPoint, List<Integer> heights){
        currentTraining = new Training();
        currentTraining.Date = date;
        currentTraining.AllTime = time;
        currentTraining.WayLength = wayLength;
        currentTraining.MaxSpeed = maxSpeed;
        currentTraining.AverageSpeed = averageSpeed;
        currentTraining.Lines = lines;

        currentTraining.setStartPoint(startPoint);

        currentTraining.MaxHeight = Collections.max(heights);
        currentTraining.MinHeight = Collections.min(heights);
        int averageHeight = 0;
        for (int h: heights) {
            averageHeight += h;
        }
        averageHeight = averageHeight / heights.size();
        currentTraining.AverageHeight = averageHeight;

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

//    String currentPhotoPath;
//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//        // Save a file: path for use with ACTION_VIEW intents
//        currentPhotoPath = image.getAbsolutePath();
//        return image;
//    }



//    private static void CreateProgramFiles()
//    {
//        try {
//            File dir = new File(TESS_PATH);
//            dir.mkdirs();
//        }
//        catch (Exception e)
//        {
//            Log.e("", e.getMessage());
//        }
//
//        try {
//            String fileList[] = getAssets().list(TESSDATA);
//
//            for(String fileName: fileList)
//            {
//                String pathToDateFile = DATA_PATH + TESSDATA + "/" + fileName;
//                if (!(new File(pathToDateFile).exists()))
//                {
//                    InputStream in = getAssets().open(TESSDATA + "/" + fileName);
//                    OutputStream out = new FileOutputStream(pathToDateFile);
//                    byte[] buf = new byte[1024];
//                    int len = in.read(buf);
//                    while (len > 0)
//                    {
//                        out.write(buf, 0, len);
//                        len = in.read(buf);
//                    }
//                    in.close();
//                    out.close();
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            Log.e("", "Unable to copy files to tessdata " + e.toString());
//        }
//    }

}


