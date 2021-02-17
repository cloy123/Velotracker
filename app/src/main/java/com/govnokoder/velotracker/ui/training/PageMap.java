package com.govnokoder.velotracker.ui.training;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.govnokoder.velotracker.BL.CurrentTraining;
import com.govnokoder.velotracker.BL.Model.Time;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.BL.MyChronometer;
import com.govnokoder.velotracker.MainActivity;
import com.govnokoder.velotracker.R;
import com.govnokoder.velotracker.TrainingService;
import com.govnokoder.velotracker.messages.MessageEvent;
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
import java.util.List;

import android.os.CountDownTimer;

import static com.govnokoder.velotracker.R.drawable.tracking_on;


public class PageMap extends Fragment implements OnMapReadyCallback, OnCameraTrackingChangedListener {
    private int pageNumber;

    private MapView mapView;
    private MapboxMap mapboxMap;

    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 500L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    private TextView CurrentSpeedTextView, WayLengthTextView;

    private LocationListeningCallback callback = new LocationListeningCallback(this);

    public Button PauseButton, ResumeButton, StopButton;

    private ImageButton LocationButton;

    private LineManager lineManager;

    private MyChronometer myChronometer;

    private CountDownTimer countDownTimer;

    private long lastPause;

    private boolean isInTrackingMode;

    private LocationComponent locationComponent;

    public static final String TAG = "TrainingActivity";

    private CurrentTraining currentTraining = new CurrentTraining();

    private boolean isFinish = false;

    //private Location originLocation;

    public static PageMap newInstance(int page){
        PageMap fragment = new PageMap();
        Bundle args=new Bundle();
        args.putInt("num", page);
        fragment.setArguments(args);
        return fragment;
    }

    public PageMap(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(), getString(R.string.access_token));
        pageNumber = getArguments() != null ? getArguments().getInt("num"):1;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.training_map_page, container, false);
        mapView = (MapView) result.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        myChronometer = result.findViewById(R.id.chronometer);
        myChronometer.Start();

        CurrentSpeedTextView = (TextView) result.findViewById(R.id.CurrentSpeedText);
        WayLengthTextView = (TextView) result.findViewById(R.id.WayLengthText);

        PauseButton = (Button) result.findViewById(R.id.PauseButton);
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
                myChronometer.Stop();
                currentTraining.Pause();
            }
        });

        ResumeButton = (Button) result.findViewById(R.id.ResumeButton);
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
                myChronometer.Start();
            }
        });

        StopButton = (Button) result.findViewById(R.id.StopButton);
        StopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO проверка на то сколько прошло времени
                Time time = myChronometer.getTime();
                myChronometer.Stop();
                currentTraining.StopAndSave(getContext(), time);
                isFinish = true;
                getActivity().finish();
            }
        });
        currentTraining.Date.setCurrentDate();


        currentTraining.isRunning = true;

        LocationButton = result.findViewById(R.id.locationButton);
        return result;
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
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            // Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(getContext(), loadedMapStyle)
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
        locationEngine = LocationEngineProvider.getBestLocationEngine(getContext());

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setDisplacement(1)//TODO возможно можно будет сделать точнее
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, Looper.getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if(locationEngine != null)
        {
            LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
            locationEngine.requestLocationUpdates(request, callback, Looper.getMainLooper());
            locationEngine.getLastLocation(callback);
        }
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        Intent intent = new Intent(getActivity().getApplicationContext(), TrainingService.class);
        getActivity().stopService(intent);
        try {
            currentTraining = EventBus.getDefault().getStickyEvent(MessageEvent.class).currentTraining;
            if(currentTraining != null){
                EventBus.getDefault().removeAllStickyEvents();
                EventBus.getDefault().unregister(this);
                myChronometer.setTime(currentTraining.Time);
                myChronometer.Start();
                Toast.makeText(getContext(), String.valueOf(currentTraining.chronometerTime + SystemClock.elapsedRealtime() - currentTraining.startServiceTime), Toast.LENGTH_LONG).show();
            }
        }catch (Exception ignored){
        }
        //TODO тут забрать у сервиса экземпляр currentActivity
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        //TODO тут передать сервису экземпляр currentActivity
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.POSTING)
    public void onEvent(MessageEvent event) {
        currentTraining = event.currentTraining;
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!isFinish){
            currentTraining.Time = myChronometer.getTime();
            Intent intent = new Intent(getActivity().getApplicationContext(), TrainingService.class);
            getActivity().startForegroundService(intent);
            EventBus.getDefault().postSticky(new MessageEvent(currentTraining));
            getActivity().finish();
        }
        else {
            currentTraining = new CurrentTraining();
            myChronometer.Start();
            EventBus.getDefault().removeAllStickyEvents();
            Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        //TODO тут передать сервису экземпляр currentActivity
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
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
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
        super.onDestroy();
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

        private final WeakReference<PageMap> fragmentWeakReference;

        LocationListeningCallback(PageMap fragment) {
            this.fragmentWeakReference = new WeakReference<>(fragment);
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
            PageMap fragment = fragmentWeakReference.get();

            if (fragment != null && fragment.currentTraining != null) {
                Location location = result.getLastLocation();
                if (fragment.mapboxMap != null && location != null) {

                    //TODO доделать нормально
                    if(location.hasSpeed()){

                        double speed = location.getSpeed() * 3.6;
                        double distance = 0;
                        if(fragment.currentTraining.originLocation != null) {
                            distance = fragment.currentTraining.originLocation.distanceTo(location)/1000;
                        }
                        int height = (int)location.getAltitude();





                        if(fragment.currentTraining.isRunning) {
                            //скорость
                            if(speed > fragment.currentTraining.MaxSpeed) {
                                fragment.currentTraining.MaxSpeed = speed;
                            }
                            fragment.currentTraining.currentSpeed = speed;
                            //длина пути
                            fragment.currentTraining.WayLength += distance;
                            //путь
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            fragment.currentTraining.currentLine.add(latLng);
                            //Высота
                            fragment.currentTraining.heights.add(height);



                            fragment.WayLengthTextView.setText(Double.toString(Training.round(fragment.currentTraining.WayLength, 2)));
                        }
                        fragment.CurrentSpeedTextView.setText(Double.toString(Training.round(speed, 1)));

                        //отрисовка путей
                        if (fragment.lineManager != null) {
                            fragment.lineManager.deleteAll();
                            for (List<LatLng> line: fragment.currentTraining.Lines) {
                                fragment.lineManager.create(new LineOptions().withLatLngs(line));
                            }
                            fragment.lineManager.create(new LineOptions().withLatLngs(fragment.currentTraining.currentLine));
                        }
                    }else {
                        //скорость
                        fragment.currentTraining.currentSpeed = 0;
                        fragment.CurrentSpeedTextView.setText("0");

                    }
                    fragment.currentTraining.originLocation = location;
                    //местоположение
                    fragment.mapboxMap.getLocationComponent().forceLocationUpdate(location);
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
            PageMap fragment = fragmentWeakReference.get();
            if (fragment != null) {
                Toast.makeText(fragment.getContext(), exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


}
