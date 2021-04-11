package com.coursework.velotracker

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMapOptions

class MapViewInScroll: MapView {

    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, i: Int): super(context, attributeSet, i)
    constructor(context: Context, mapboxMapOptions: MapboxMapOptions): super(context, mapboxMapOptions)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }
}