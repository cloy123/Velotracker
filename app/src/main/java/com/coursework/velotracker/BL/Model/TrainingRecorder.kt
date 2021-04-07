package com.coursework.velotracker.BL.Model

import android.content.Context
import android.location.Location
import com.coursework.velotracker.BL.Controller.TrainingController
import com.mapbox.mapboxsdk.geometry.LatLng
import java.time.LocalDate
import java.time.LocalTime

class TrainingRecorder {

    var date: LocalDate = LocalDate.now()
    var totalTime: LocalTime = LocalTime.MIN
    var units: UnitsConverter.Units = UnitsConverter.Units.METERS

    var totalDistance : Double = 0.0
        get(){
            return UnitsConverter().convertKilometersToUnits(totalDistance, units)
        }

    var maxSpeed : Double = 0.0
        get(){
            return UnitsConverter().convertKilometersToUnits(maxSpeed, units)
        }

    var averageSpeed : Double = 0.0
        get(){
            return UnitsConverter().convertKilometersToUnits(averageSpeed, units)
        }

    var currentSpeed : Double = 0.0
        get(){
            return UnitsConverter().convertKilometersToUnits(currentSpeed, units)
        }

    var heights: MutableList<Long> = ArrayList<Long>()

    var currentHeight: Long = 0
        get(){
            return UnitsConverter().convertMetersToUnits(currentHeight, units)
        }

    var maxHeight: Long = 0
        get(){
            return UnitsConverter().convertMetersToUnits(maxHeight, units)
        }

    var minHeight: Long = 0
        get(){
            return UnitsConverter().convertMetersToUnits(minHeight, units)
        }

    var averageHeight: Long = 0
        get(){
            return UnitsConverter().convertMetersToUnits(averageHeight, units)
        }

    var sumHeight: Long = 0

    var isRunning = false

    var originLocation: Location? = null

    private var sumSpeed : Double = 0.0

    var lines: MutableList<Line> = ArrayList()
    var currentLine: Line = Line()

    fun Pause() {
        isRunning = false
        val line = Line()
        for (latlng in currentLine) {
            line.add(LatLng(latlng.latitude, latlng.longitude))
        }
        lines.add(line)
        currentLine = Line()
    }

    fun Resume() {
        isRunning = true
    }

    fun StopAndSave(context: Context?) {
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

    fun setValuesFromLocation(location: Location?) {
        if (location != null) {
            setSpeedValues(location)
            setHeightValues(location)
            setDistanceValues(location)
            setLinesValues(location)
            if (location.hasAccuracy()) {
                originLocation = location
            }
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
            averageSpeed = totalDistance / (totalTime.getAllSeconds() as Double / 3600)
        }
    }

    private fun setHeightValues(location: Location){
        if (location.hasVerticalAccuracy() && location.hasAltitude()) {
            val height = location.altitude.toLong()
            if (location.hasAltitude()) {
                currentHeight = height
                heights.add(height)
                minHeight = java.lang.Long.min(height, minHeight)
                maxHeight = java.lang.Long.max(height, maxHeight)
                sumHeight += height
                averageHeight = sumHeight / heights.size
            }
        }
    }
}