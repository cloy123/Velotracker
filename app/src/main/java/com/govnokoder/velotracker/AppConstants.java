package com.govnokoder.velotracker;

import android.graphics.Color;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.utils.ColorUtils;

public class AppConstants {

    public static int GPS_REQUEST = 3424;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 500L;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static final long MAX_WAIT_TIME_IN_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS*2;

    public static final String LINE_COLOR = ColorUtils.colorToRgbaString(Color.argb(255, 0, 0, 0));
    public static final float LINE_WIDTH = 5;
    public static String MAP_STYLE = Style.OUTDOORS;
}
