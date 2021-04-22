package com.coursework.velotracker.BL.Model.Training

import android.content.Context
import android.location.Location
import com.coursework.velotracker.BL.Controller.TrainingController
import com.coursework.velotracker.BL.Model.Extensions.getAllSeconds
import com.coursework.velotracker.BL.Model.Line
import com.coursework.velotracker.BL.Model.UnitsConverter
import com.mapbox.mapboxsdk.geometry.LatLng
import java.time.LocalDate
import java.time.LocalTime

class TrainingRecorder {

    var date: LocalDate = LocalDate.now()
    var totalTime: LocalTime = LocalTime.MIN
    var units: UnitsConverter.Units = UnitsConverter.Units.METERS

    var totalDistance : Double = 0.0
        get() = UnitsConverter().convertKilometersToUnits(field, units)

    var maxSpeed : Double = 0.0
        get() = UnitsConverter().convertKilometersToUnits(field, units)

    var averageSpeed : Double = 0.0
        get() = UnitsConverter().convertKilometersToUnits(field, units)

    var currentSpeed : Double = 0.0
        get() = UnitsConverter().convertKilometersToUnits(field, units)

    var heights: MutableList<Long> = ArrayList<Long>()

    var currentHeight: Long = 0
        get() = UnitsConverter().convertMetersToUnits(field, units)

    var maxHeight: Long = Long.MIN_VALUE
        get() = UnitsConverter().convertMetersToUnits(field, units)

    var minHeight: Long = Long.MAX_VALUE
        get() = UnitsConverter().convertMetersToUnits(field, units)

    var averageHeight: Long = Long.MIN_VALUE
        get() = UnitsConverter().convertMetersToUnits(field, units)

    var sumHeight: Long = 0

    var isRunning = false

    var originLocation: Location? = null

    private var sumSpeed : Double = 0.0

    var lines: MutableList<Line> = ArrayList()
    var currentLine: Line = Line()

    fun pause() {
        isRunning = false
        val line = Line()
        for (latlng in currentLine) {
            line.add(LatLng(latlng.latitude, latlng.longitude))
        }
        lines.add(line)
        currentLine = Line()
    }

    fun resume() {
        isRunning = true
    }

    fun stopAndSave(context: Context) {
        val trainingController = TrainingController(context)
        if (heights.size == 0) {
            maxHeight = 0
            minHeight = 0
            averageHeight = 0
        }
        if (lines.size == 0) {
            return
        }
        trainingController.setNewTrainingData(this)
    }

    fun setValuesFromLocation(location: Location) {
        setSpeedValues(location)
        setHeightValues(location)
        setDistanceValues(location)
        setLinesValues(location)
        if (location.hasAccuracy()) {
            originLocation = location
        }
    }

    private fun setLinesValues(location: Location){
        if (location.hasAccuracy() && isRunning) {
            val latLng = LatLng(location.latitude, location.longitude)
            currentLine.add(latLng)
        }
    }

    private fun setDistanceValues(location: Location){
        if (location.hasAccuracy() && isRunning) {
            var distance = 0.0
            if (originLocation != null) {
                distance = (originLocation!!.distanceTo(location) / 1000).toDouble()
            }
            totalDistance += distance
        }
    }

    private fun setSpeedValues(location: Location){
        var speed = 0.0
        if (location.hasSpeed()) {
            speed = location.speed * 3.6
        }
        currentSpeed = speed
        if (isRunning) {
            if (speed > maxSpeed) {
                maxSpeed = speed
            }
            sumSpeed += speed
            averageSpeed = totalDistance / (getAllSeconds(totalTime).toDouble() / 3600)
        }
    }

    private fun setHeightValues(location: Location){
        if (location.hasAltitude()) {//location.hasVerticalAccuracy() &&
            val height = location.altitude.toLong()
                currentHeight = height
                heights.add(height)
                minHeight = java.lang.Long.min(height, minHeight)
                maxHeight = java.lang.Long.max(height, maxHeight)
                sumHeight += height
                averageHeight = sumHeight / heights.size
        }
    }
}