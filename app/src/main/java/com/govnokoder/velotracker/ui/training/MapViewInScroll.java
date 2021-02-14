package com.govnokoder.velotracker.ui.training;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;

public class MapViewInScroll extends MapView {
    public MapViewInScroll(@NonNull Context context) {
        super(context);
    }

    public MapViewInScroll(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public MapViewInScroll(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public MapViewInScroll(Context context, MapboxMapOptions mapboxMapOptions) {
        super(context, mapboxMapOptions);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}
