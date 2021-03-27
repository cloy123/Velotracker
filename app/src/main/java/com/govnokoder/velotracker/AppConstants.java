package com.govnokoder.velotracker;

import android.graphics.Color;

import com.mapbox.mapboxsdk.utils.ColorUtils;

public class AppConstants {

    public static int GPS_REQUEST = 3424;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 500L;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    //TODO получать из настроек
    public static final String LINE_COLOR = ColorUtils.colorToRgbaString(Color.argb(255, 255, 0, 0));
    public static final float LINE_WIDTH = 5;
}