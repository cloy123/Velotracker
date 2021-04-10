package com.coursework.velotracker

import android.graphics.Color
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.utils.ColorUtils

object AppConstants {
    const val GPS_REQUEST = 3424
    const val UPDATE_INTERVAL_IN_MILLISECONDS = 500L
    const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    const val MAX_WAIT_TIME_IN_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS * 2

    val LINE_COLOR = ColorUtils.colorToRgbaString(Color.argb(255, 0, 0, 0))
    const val LINE_WIDTH = 5f
    const val MAP_STYLE = Style.OUTDOORS

    const val TIME_FORMAT = "00:00:00"
    const val DATE_FORMAT = "00/00/0000"
}