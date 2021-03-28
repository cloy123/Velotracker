package com.govnokoder.velotracker;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.govnokoder.velotracker.BL.Controller.TrainingController;
import com.govnokoder.velotracker.BL.LineList;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.ui.training.MapViewInScroll;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;

import java.util.List;

public class LookTraining extends AppCompatActivity
        implements OnMapReadyCallback {

    private TextView averageSpeedText, timeText, maxSpeedText, distanceText,
    averageHeightText, tempText, maxHeightText, minHeightText;
    private int index;
    List<Training> trainings;
    Training CurrentTraining;

    private MapViewInScroll mapView;
    private MapboxMap mapboxMap;

    private LineManager lineManager;

    private AppCompatButton DeleteButton;

    private TrainingController trainingController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_look_training);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        distanceText = (TextView) findViewById(R.id.distanceText);
        maxSpeedText = (TextView) findViewById(R.id.maxSpeedText);
        averageSpeedText = (TextView) findViewById(R.id.averageSpeedText);
        timeText = (TextView) findViewById(R.id.timeText);
        averageHeightText = findViewById(R.id.averageHeightText);
        tempText = findViewById(R.id.tempText);
        maxHeightText = findViewById(R.id.maxHeightText);
        minHeightText = findViewById(R.id.minHeightText);

        Bundle bundle = getIntent().getExtras();
        index = (int)bundle.get("INDEX");
        trainingController = new TrainingController(getApplicationContext());
        trainings = trainingController.LoadTrainingsData();
        CurrentTraining = trainings.get(index);

        distanceText.setText(Double.toString(Training.round(CurrentTraining.Distance, 2)) + " " + getString(R.string.km));
        maxSpeedText.setText(Double.toString(Training.round(CurrentTraining.MaxSpeed, 1)) + " " + getString(R.string.kph));
        averageSpeedText.setText(Double.toString(Training.round(CurrentTraining.AverageSpeed, 1)) + " " + getString(R.string.kph));
        timeText.setText(CurrentTraining.Time.toString());
        averageHeightText.setText(Long.toString(CurrentTraining.AverageHeight) + " " + getString(R.string.m));
        tempText.setText(CurrentTraining.getTemp("k").toString() + " /" + getString(R.string.km));
        maxHeightText.setText(Long.toString(CurrentTraining.MaxHeight) + " " + getString(R.string.m));
        minHeightText.setText(Long.toString(CurrentTraining.MinHeight) + " " + getString(R.string.m));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(CurrentTraining.Date.toString());

        DeleteButton = findViewById(R.id.deleteButton);
        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogRemoveTraining();
            }
        });
    }

    private void dialogRemoveTraining(){
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        ConstraintLayout cl  = (ConstraintLayout)getLayoutInflater().inflate(R.layout.dialog_remove_training, null);
        cl.getViewById(R.id.yesB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trainingController.RemoveTraining(index);
                dialog.dismiss();
                finish();
            }
        });
        cl.getViewById(R.id.cancelB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(cl);
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(AppConstants.MAP_STYLE, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                if(CurrentTraining.getStartPoint() != null ){
                    LatLng latLng = CurrentTraining.getStartPoint();
                    CameraPosition position = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(15)
                            .build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
                    mapboxMap.moveCamera(cameraUpdate);
                }

                if(CurrentTraining.Lines != null && CurrentTraining.Lines.size() > 0){
                    lineManager = new LineManager(mapView, mapboxMap, mapboxMap.getStyle());
                    for (LineList line: CurrentTraining.Lines) {
                        drawLine(line);
                    }
                }
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
    @SuppressLint("MissingPermission")
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
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
        super.onDestroy();
        mapView.onDestroy();
    }
}