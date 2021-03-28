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
import com.govnokoder.velotracker.BL.LineList;
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

public class PageStatistics extends Fragment implements OnMapReadyCallback {
    private int pageNumber;

    private MapViewInScroll mapView;
    private MapboxMap mapboxMap;
    private LineManager lineManager;

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

        //TODo тут заполнять всё
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
