package com.govnokoder.velotracker.ui.training;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.govnokoder.velotracker.AppConstants;
import com.govnokoder.velotracker.BL.LineList;
import com.govnokoder.velotracker.BL.ParcelableTraining;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.MainActivity;
import com.govnokoder.velotracker.R;
import com.govnokoder.velotracker.messages.SharedViewModel;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
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
import com.mapbox.mapboxsdk.plugins.annotation.Annotation;
import com.mapbox.mapboxsdk.plugins.annotation.AnnotationManager;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnAnnotationDragListener;

import java.util.List;

import static com.govnokoder.velotracker.R.drawable.tracking_on;


public class PageMap extends Fragment implements OnMapReadyCallback, OnCameraTrackingChangedListener{
    private int pageNumber;

    private MapView mapView;
    private MapboxMap mapboxMap;

    private TextView CurrentSpeedTextView, WayLengthTextView, TimeTextView;

    public Button PauseButton, ResumeButton, StopButton;

    private ImageButton LocationButton;

    private LineManager lineManager;

    private boolean isInTrackingMode;

    private LocationComponent locationComponent;

    public static final String TAG = "TrainingActivity";

    private boolean isFinish = false;

    private boolean isLineDrawn = false;

    private ParcelableTraining mParcelableTraining;

    public interface onSomeEventListener {
        public void onPauseTraining();
        public void onStopTraining(boolean isSave);
        public void onResumeTraining();
    }
    onSomeEventListener someEventListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            someEventListener = (onSomeEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }


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
        TimeTextView = result.findViewById(R.id.TimeText);
        CurrentSpeedTextView = (TextView) result.findViewById(R.id.CurrentSpeedText);
        WayLengthTextView = (TextView) result.findViewById(R.id.WayLengthText);
        PauseButton = (Button) result.findViewById(R.id.PauseButton);
        PauseButton.setOnClickListener(this::onClickPauseButton);
        ResumeButton = (Button) result.findViewById(R.id.ResumeButton);
        ResumeButton.setOnClickListener(this::onClickResumeButton);
        StopButton = (Button) result.findViewById(R.id.StopButton);
        StopButton.setOnClickListener(this::onClickStopButton);
        LocationButton = result.findViewById(R.id.locationButton);
        return result;
    }

    private void onClickPauseButton(View v){
        if(mParcelableTraining != null){
            someEventListener.onPauseTraining();
            setButtonsState(false);
        }
    }

    private void onClickResumeButton(View v){
        if(mParcelableTraining != null){
            someEventListener.onResumeTraining();
            setButtonsState(true);
        }
    }

    private void onClickStopButton(View v){
        AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        ConstraintLayout cl  = (ConstraintLayout)getLayoutInflater().inflate(R.layout.dialog_save_and_exit, null);
        cl.getViewById(R.id.saveAndExitB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFinish = true;
                someEventListener.onStopTraining(true);
                dialog.dismiss();
                getActivity().finish();
            }
        });
        cl.getViewById(R.id.exitWithoutSavingB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFinish = true;
                someEventListener.onStopTraining(false);
                dialog.dismiss();
                getActivity().finish();
            }
        });
        cl.getViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(cl);
        dialog.show();
    }

    private void setButtonsState(boolean isRunning){
        if(isRunning){
            PauseButton.setVisibility(View.VISIBLE);
            PauseButton.setEnabled(true);
            ResumeButton.setVisibility(View.INVISIBLE);
            ResumeButton.setEnabled(false);
            StopButton.setVisibility(View.INVISIBLE);
            StopButton.setEnabled(false);
        }else {
            PauseButton.setVisibility(View.INVISIBLE);
            PauseButton.setEnabled(false);
            ResumeButton.setEnabled(true);
            ResumeButton.setVisibility(View.VISIBLE);
            StopButton.setEnabled(true);
            StopButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(AppConstants.MAP_STYLE, new Style.OnStyleLoaded() {//MAPBOX_STREETS
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
            locationComponent.setRenderMode(RenderMode.NORMAL);
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
        }

        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.messagesParcelableTraining.observe(getViewLifecycleOwner(), new Observer<ParcelableTraining>() {
            @Override
            public void onChanged(ParcelableTraining parcelableTraining) {
                if(parcelableTraining != null && parcelableTraining.originLocation != null){
                    if(mParcelableTraining == null){
                        setButtonsState(parcelableTraining.isRunning);
                    }
                    mParcelableTraining = parcelableTraining;
                    if(locationComponent != null && locationComponent.isLocationComponentActivated()){
                        locationComponent.forceLocationUpdate(parcelableTraining.originLocation);
                        //mapboxMap.getLocationComponent().forceLocationUpdate(parcelableTraining.originLocation);
                    }
                    TimeTextView.setText(parcelableTraining.time.toString());
                    CurrentSpeedTextView.setText(String.valueOf(Training.round(parcelableTraining.originLocation.getSpeed() * 3.6, 1)) + " " + getString(R.string.kph));
                    WayLengthTextView.setText(String.valueOf(Training.round(parcelableTraining.distance, 2)) + " " + getString(R.string.km));
                    //отрисовка путей
                    if (lineManager != null){
                        if(parcelableTraining.getLines().size() > 0) {
                            lineManager.deleteAll();
                            for (LineList line: parcelableTraining.getLines()) {
                                drawLine(line);
                            }
                        }
                        if(parcelableTraining.currentLine.size() > 0){
                            drawLine(parcelableTraining.currentLine);
                        }
                    }
                }
            }
        });

        model.messagesLocation.observe(getViewLifecycleOwner(), new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if(location != null){
                    locationComponent.forceLocationUpdate(location);
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
    public void onStart() {
        super.onStart();
        mapView.onStart();
        isLineDrawn = false;
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
        if(!isFinish){
            getActivity().finish();
        } else {
            Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
            startActivity(intent);
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
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onCameraTrackingDismissed() {
        isInTrackingMode = false;
        LocationButton.setImageResource(R.drawable.tracking_off);
    }

    @Override
    public void onCameraTrackingChanged(int currentMode) { }

}
