package com.govnokoder.velotracker.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.govnokoder.velotracker.AppConstants;
import com.govnokoder.velotracker.BL.Controller.TrainingController;
import com.govnokoder.velotracker.BL.LineList;
import com.govnokoder.velotracker.BL.Model.Time;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.MainActivity;
import com.govnokoder.velotracker.R;
import com.govnokoder.velotracker.ui.training.MapViewInScroll;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;

import java.util.List;

public class PageStatistics extends Fragment implements OnMapReadyCallback {
    private int pageNumber;

    private MapViewInScroll mapView;
    private MapboxMap mapboxMap;
    private LineManager lineManager;

    private TextView totalDistanceTextView, totalRecordsTextView, totalTimeTextView,
                    maxSpeedTextView, averageSpeedTextView, tempTextView, maxTempTextView,
                    averageHeightTextView, maxHeightTextView, minHeightTextView;

    public static PageStatistics newInstance(int page) {
        PageStatistics fragment = new PageStatistics();
        Bundle args=new Bundle();
        args.putInt("num", page);
        fragment.setArguments(args);
        return fragment;
    }

    public PageStatistics() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(), getString(R.string.access_token));
        pageNumber = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result=inflater.inflate(R.layout.statistics_page, container, false);
        mapView = (MapViewInScroll) result.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        totalDistanceTextView = (TextView)result.findViewById(R.id.totalDistanceText);
        totalRecordsTextView = (TextView)result.findViewById(R.id.totalRecordsText);
        totalTimeTextView = (TextView)result.findViewById(R.id.totalTimeText);
        maxSpeedTextView = (TextView)result.findViewById(R.id.maxSpeedText);
        averageSpeedTextView = (TextView)result.findViewById(R.id.averageSpeedText);
        tempTextView = (TextView)result.findViewById(R.id.tempText);
        maxTempTextView = (TextView)result.findViewById(R.id.maxTempText);
        averageHeightTextView = (TextView)result.findViewById(R.id.averageHeightText);
        maxHeightTextView = (TextView)result.findViewById(R.id.maxHeightText);
        minHeightTextView = (TextView)result.findViewById(R.id.minHeightText);
        return result;
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(AppConstants.MAP_STYLE, new Style.OnStyleLoaded() {//MAPBOX_STREETS
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                lineManager = new LineManager(mapView, mapboxMap, style);
            }
        });
    }

    private void drawLine(LineList line){
        LineOptions lineOptions = new LineOptions();
        lineOptions.withLatLngs(line);
        lineOptions.withLineColor(AppConstants.LINE_COLOR);
        lineOptions.withLineWidth(AppConstants.LINE_WIDTH);
        lineManager.create(lineOptions);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        List<Training> trainings = new TrainingController(getContext()).LoadTrainingsData();
        if(trainings == null || trainings.size() == 0){
            totalDistanceTextView.setText("0" + " " + getString(R.string.km));
            totalRecordsTextView.setText("0");
            totalTimeTextView.setText("00:00");
            maxSpeedTextView.setText("0" + " " + getString(R.string.kph));
            averageSpeedTextView.setText("0" + " " + getString(R.string.kph));
            tempTextView.setText("00:00" + " /" + getString(R.string.km));
            maxTempTextView.setText("00:00" + " /" + getString(R.string.km));
            averageHeightTextView.setText("0" + " " + getString(R.string.m));
            minHeightTextView.setText("");
            maxHeightTextView.setText("");
            return;
        }
        double totalDist = 0;
        long totalSeconds = 0;
        double maxSpeed = trainings.get(0).MaxSpeed;
        long maxHeight = trainings.get(0).MaxHeight;
        long minHeight = trainings.get(0).MinHeight;
        int totalRecords = trainings.size();
        long totalHeights = 0;
        long totalHeightsValues = 0;
        long totalTempSeconds = 0;
        Time maxTemp = trainings.get(0).getTemp(Training.Units.KILOMETERS);
        for (Training training : trainings) {
            totalDist += training.Distance;
            totalSeconds += training.Time.getAllSeconds();
            totalHeights += training.AverageHeight * training.Heights.size();
            totalHeightsValues += training.Heights.size();
            totalTempSeconds += training.getTemp(Training.Units.KILOMETERS).getAllSeconds();
            if(training.MaxSpeed > maxSpeed){
                maxSpeed = training.MaxSpeed;
            }
            if(training.MaxHeight > maxHeight){
                maxHeight = training.MaxHeight;
            }
            if(training.MinHeight < minHeight){
                minHeight = training.MinHeight;
            }
            if(training.getTemp(Training.Units.KILOMETERS).getAllSeconds() < maxTemp.getAllSeconds()){
                maxTemp = training.getTemp(Training.Units.KILOMETERS);
            }
            if(lineManager != null && training.Lines.size() > 0){
                for (LineList line: training.Lines) {
                    drawLine(line);
                }
            }
        }
        double averageSpeed = totalDist / ((double)totalSeconds/3600);
        Time averageTemp = Time.getTimeFromSeconds(totalTempSeconds / trainings.size());
        long averageHeight = totalHeights / totalHeightsValues;

        String maxHeightStr = Long.toString(maxHeight) + " " + getString(R.string.m);
        if(maxHeight == Long.MIN_VALUE){maxHeightStr = "";}
        String minHeightStr = Long.toString(minHeight) + " " + getString(R.string.m);
        if(minHeight == Long.MAX_VALUE){minHeightStr = "";}

        totalDistanceTextView.setText(Double.toString(Training.round(totalDist, 2)) + " " + getString(R.string.km));
        totalRecordsTextView.setText(Integer.toString(totalRecords));
        totalTimeTextView.setText(Time.getTimeFromSeconds(totalSeconds).toString());
        maxSpeedTextView.setText(Double.toString(Training.round(maxSpeed, 1)) + " " + getString(R.string.kph));
        averageSpeedTextView.setText(Double.toString(Training.round(averageSpeed, 1)) + " " + getString(R.string.kph));
        tempTextView.setText(averageTemp.toString() + " /" + getString(R.string.km));
        maxTempTextView.setText(maxTemp.toString() + " /" + getString(R.string.km));
        averageHeightTextView.setText(Long.toString(averageHeight) + " " + getString(R.string.m));
        minHeightTextView.setText(minHeightStr);
        maxHeightTextView.setText(maxHeightStr);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

}
