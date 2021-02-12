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
import androidx.appcompat.app.AppCompatActivity;

import com.govnokoder.velotracker.BL.Controller.TrainingController;
import com.govnokoder.velotracker.BL.Model.Training;
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

    private TextView WayLength;
    private TextView MaxSpeed;
    private TextView AverageSpeed;
    private TextView Time;
    private int Index;
    List<Training> trainings;
    Training CurrentTraining;

    private  MapView mapView;
    private MapboxMap mapboxMap;

    private LineManager lineManager;
    private LineOptions lineOptions;

    private Button Delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_look_training);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        WayLength = (TextView) findViewById(R.id.WayLengthText);
        MaxSpeed = (TextView) findViewById(R.id.MaxSpeedText);
        AverageSpeed = (TextView) findViewById(R.id.AverageSpeedText);
        Time = (TextView) findViewById(R.id.TimeText);

        Bundle bundle = getIntent().getExtras();
        Index = (int)bundle.get("INDEX");
        TrainingController trainingController = new TrainingController(getApplicationContext());
        trainings = trainingController.LoadTrainingsData(getApplicationContext());
        CurrentTraining = trainings.get(Index);

        WayLength.setText(Double.toString(CurrentTraining.WayLength));
        MaxSpeed.setText(Double.toString(CurrentTraining.MaxSpeed));
        AverageSpeed.setText(Double.toString(CurrentTraining.AverageSpeed));

        Time.setText(CurrentTraining.AllTime.toString());



        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(CurrentTraining.Date.toString());




        Delete = (Button) findViewById(R.id.Delete);
        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO добавить нормальное удаление и  отображение пути

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCameraPosition(Location location) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17.0));
    }

    private void setCameraPositionLoc(LatLng latLng) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0));
    }

    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;


        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments


                LatLng latLng = CurrentTraining.getStartPoint();
                CameraPosition position = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(15)
                        .tilt(20)
                        .build();
                //mapboxMap.setCameraPosition(position);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
                mapboxMap.moveCamera(cameraUpdate);


                lineManager = new LineManager(mapView, mapboxMap, mapboxMap.getStyle());
                lineOptions = new LineOptions();
                for (List<LatLng> line: CurrentTraining.Lines) {
                    lineManager.create(new LineOptions().withLatLngs(line));
                }



            }
        });


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