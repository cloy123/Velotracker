package com.coursework.velotracker.BL.Model.Training

import com.coursework.velotracker.BL.Model.Extensions.getAllSeconds
import com.coursework.velotracker.BL.Model.Extensions.timeFromSeconds
import com.coursework.velotracker.BL.Model.Line
import com.coursework.velotracker.BL.Model.UnitsConverter
import com.mapbox.mapboxsdk.geometry.LatLng
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TrainingStatistics() {

    var date: LocalDate = LocalDate.now()
    var totalTime: LocalTime = LocalTime.MIN

    var totalDistance : Double = 0.0
        get() = UnitsConverter().convertKilometersToUnits(field, units)

    var maxSpeed : Double = 0.0
        get() = UnitsConverter().convertKilometersToUnits(field, units)

    var averageSpeed : Double = 0.0
        get() = UnitsConverter().convertKilometersToUnits(field, units)

    var maxHeight: Long = 0
        get() = UnitsConverter().convertMetersToUnits(field, units)

    var minHeight: Long = 0
        get() = UnitsConverter().convertMetersToUnits(field, units)

    var averageHeight: Long = 0
        get() = UnitsConverter().convertMetersToUnits(field, units)

    var heights: MutableList<Long> = ArrayList()
    get(){
        val heightList: MutableList<Long> = ArrayList()
        for(i in 0 until field.size){
            heightList.add(UnitsConverter().convertMetersToUnits(field[i], units))
        }
        return heightList
    }

    var lines: List<Line> = ArrayList()

    var units: UnitsConverter.Units = UnitsConverter.Units.METERS


    var temp:LocalTime = LocalTime.MIN
    get(){
        calculateTemp()
        return field
    }

    constructor(trainingRecorder: TrainingRecorder) : this() {
        date = trainingRecorder.date
        totalTime = trainingRecorder.totalTime
        totalDistance = trainingRecorder.totalDistance
        maxSpeed = trainingRecorder.maxSpeed
        averageSpeed = trainingRecorder.averageSpeed
        maxHeight = trainingRecorder.maxHeight
        minHeight = trainingRecorder.minHeight
        averageHeight = trainingRecorder.averageHeight
        lines = trainingRecorder.lines
    }

    private fun calculateTemp(){
        val allSeconds = getAllSeconds(totalTime)
        if(totalDistance > 0  && allSeconds > 0){
            temp = timeFromSeconds((allSeconds / totalDistance).toInt())
        }
    }

    fun getStartPoint(): LatLng {
        if (lines.isNotEmpty()) {
            if (lines[0].size > 0) {
                return LatLng(lines[0][0])
            }
        }
        return LatLng(0.0, 0.0)
    }

}