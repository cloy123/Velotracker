package com.coursework.velotracker.BL.Model

import android.os.Parcelable
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
class Line : ArrayList<LatLng>(), Parcelable {
}