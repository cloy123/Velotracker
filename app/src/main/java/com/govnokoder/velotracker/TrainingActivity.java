package com.govnokoder.velotracker;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.govnokoder.velotracker.BL.Controller.TrainingController;
import com.govnokoder.velotracker.BL.CurrentTraining;
import com.govnokoder.velotracker.BL.Model.Date;
import com.govnokoder.velotracker.BL.Model.Time;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.messages.MessageActivityToService;
import com.govnokoder.velotracker.messages.MessageServiceToActivity;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.govnokoder.velotracker.R.drawable.abc_ic_menu_cut_mtrl_alpha;
import static com.govnokoder.velotracker.R.drawable.tracking_on;

public class TrainingActivity extends AppCompatActivity implements OnMapReadyCallback, OnCameraTrackingChangedListener{

    private MapView mapView;
    private MapboxMap mapboxMap;

    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 500L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    private TextView CurrentSpeedTextView, WayLengthTextView;

    private LocationListeningCallback callback = new LocationListeningCallback(this);

    public Button PauseButton, ResumeButton, StopButton;

    private ImageButton  LocationButton;

    private LineManager lineManager;

    private Chronometer chronometer;

    private long lastPause;

    private boolean isInTrackingMode;

    private LocationComponent locationComponent;

    public static final String TAG = "TrainingActivity";

    private CurrentTraining currentTraining = new CurrentTraining();

    private boolean isFinish = false;

    private Location originLocation;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getApplicationContext(), getString(R.string.access_token));
        setContentView(R.layout.activity_training);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        CurrentSpeedTextView = (TextView) findViewById(R.id.CurrentSpeedText);
        WayLengthTextView = (TextView) findViewById(R.id.WayLengthText);

        PauseButton = (Button) findViewById(R.id.PauseButton);
        PauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PauseButton.setVisibility(View.INVISIBLE);
                PauseButton.setEnabled(false);

                ResumeButton.setEnabled(true);
                ResumeButton.setVisibility(View.VISIBLE);

                StopButton.setEnabled(true);
                StopButton.setVisibility(View.VISIBLE);

                lastPause = SystemClock.elapsedRealtime();
                chronometer.stop();
                currentTraining.Pause();
            }
        });

        ResumeButton = (Button) findViewById(R.id.ResumeButton);
        ResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PauseButton.setVisibility(View.VISIBLE);
                PauseButton.setEnabled(true);
                ResumeButton.setVisibility(View.INVISIBLE);
                ResumeButton.setEnabled(false);
                StopButton.setVisibility(View.INVISIBLE);
                StopButton.setEnabled(false);

                currentTraining.Resume();
                chronometer.setBase(chronometer.getBase() + SystemClock.elapsedRealtime() - lastPause);
                chronometer.start();
            }
        });

        StopButton = (Button) findViewById(R.id.StopButton);
        StopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO проверка на то сколько прошло времени
                Time time = Time.getSecondsFromDurationString(chronometer.getText().toString());
                currentTraining.StopAndSave(getApplicationContext(), time);
                isFinish = true;
                finish();
            }
        });
        currentTraining.Date.setCurrentDate();

        chronometer = (Chronometer) findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        currentTraining.isRunning = true;
    }



    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageServiceToActivity event) {
        currentTraining = event.currentTraining;
        chronometer.setBase(currentTraining.Time);
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                enableLocationComponent(style);
                lineManager = new LineManager(mapView, mapboxMap, style);
            }
        });
    }

    private void setCameraPosition(Location location) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17.0));
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            locationComponent.zoomWhileTracking(16f);

            locationComponent.addOnCameraTrackingChangedListener(this);

            LocationButton = findViewById(R.id.locationButton);
            LocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isInTrackingMode) {
                        locationComponent.setCameraMode(CameraMode.TRACKING);
                        locationComponent.zoomWhileTracking(16f);
                        LocationButton.setImageResource(tracking_on);
                    }
                }
            });

            initLocationEngine();
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setDisplacement(1)//TODO возможно можно будет сделать точнее
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    @SuppressLint("MissingPermission")
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if(locationEngine != null)
        {
            LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
            locationEngine.requestLocationUpdates(request, callback, getMainLooper());
            locationEngine.getLastLocation(callback);
        }
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        //EventBus.getDefault().post(new MessageActivityToService(currentTraining));
        Intent intent = new Intent(this, TrainingService.class);
        stopService(intent);
        //TODO тут забрать у сервиса экземпляр currentActivity
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if(!isFinish){
            currentTraining.Time = chronometer.getBase();
            CurrentTraining.Save(this, currentTraining);
            Intent intent = new Intent(this, TrainingService.class);
            startForegroundService(intent);
            //EventBus.getDefault().post(new MessageActivityToService(currentTraining));
        }
        else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
            //TODO тут передать сервису экземпляр currentActivity
    }

    @Override
    protected void onStop() {
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onStop();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    public void onCameraTrackingDismissed() {
        isInTrackingMode = false;
        LocationButton.setImageResource(R.drawable.tracking_off);
    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {
    }

    private static class LocationListeningCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<TrainingActivity> activityWeakReference;

        LocationListeningCallback(com.govnokoder.velotracker.TrainingActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */

        @SuppressLint("StringFormatInvalid")
        @Override
        public void onSuccess(LocationEngineResult result) {
            // The LocationEngineCallback interface's method which fires when the device's location has changed.
            //при изменениии местоположения
            com.govnokoder.velotracker.TrainingActivity activity = activityWeakReference.get();

            if (activity != null && activity.currentTraining != null) {
                Location location = result.getLastLocation();
                if (activity.mapboxMap != null && location != null) {

                    //TODO доделать нормально
                    if(location.hasSpeed()){

                        double speed = location.getSpeed() * 3.6;
                        double distance = 0;
                        if(activity.originLocation != null) {
                            distance = activity.originLocation.distanceTo(location)/1000;
                        }
                        int height = (int)location.getAltitude();





                        if(activity.currentTraining.isRunning) {
                            //скорость
                            if(speed > activity.currentTraining.MaxSpeed) {
                                activity.currentTraining.MaxSpeed = speed;
                            }
                            activity.currentTraining.AverageSpeed = speed;
                            //длина пути
                            activity.currentTraining.WayLength += distance;
                            //путь
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            activity.currentTraining.currentLine.add(latLng);
                            //Высота
                            activity.currentTraining.heights.add(height);



                            activity.WayLengthTextView.setText(Double.toString(Training.round(activity.currentTraining.WayLength, 2)));
                        }
                        activity.CurrentSpeedTextView.setText(Double.toString(Training.round(speed, 1)));

                        //отрисовка путей
                        if (activity.lineManager != null) {
                            activity.lineManager.deleteAll();
                            for (List<LatLng> line: activity.currentTraining.Lines) {
                                activity.lineManager.create(new LineOptions().withLatLngs(line));
                            }
                            activity.lineManager.create(new LineOptions().withLatLngs(activity.currentTraining.currentLine));
                        }
                    }else {
                        //скорость
                        activity.currentTraining.AverageSpeed = 0;
                        activity.CurrentSpeedTextView.setText("0");

                    }
                    activity.originLocation = location;
                    //местоположение
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(location);
                }
            }

        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */

        @Override
        public void onFailure(@NonNull Exception exception) {
            // The LocationEngineCallback interface's method which fires when the device's location can not be captured
            //когда не может определить местоположение
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            com.govnokoder.velotracker.TrainingActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}