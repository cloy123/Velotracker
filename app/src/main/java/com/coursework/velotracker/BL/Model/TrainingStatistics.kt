package com.coursework.velotracker.BL.Model

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

    var lines: List<Line> = ArrayList()

    var units: UnitsConverter.Units = UnitsConverter.Units.METERS


    var temp:LocalTime = LocalTime.MIN
    get(){
        calculateTemp()
        return temp
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
        val allSeconds = totalTime.getAllSeconds()
        if(totalDistance > 0  && allSeconds > 0){
            temp.timeFromSeconds((allSeconds / totalDistance).toInt())
        }
    }

    fun getStartPoint(): LatLng {
        if (lines?.size > 0) {
            if (lines[0]?.size > 0) {
                return LatLng(lines[0][0])
            }
        }
        return LatLng(0.0, 0.0)
    }

}

fun LocalDate.toString(format: String):String{
    val formatter = DateTimeFormatter.ofPattern(format)
    return format(formatter)
}

fun LocalTime.toString(format: String):String{
    val formatter = DateTimeFormatter.ofPattern(format)
    return format(formatter)
}

fun LocalTime.getAllSeconds(): Int {
    return this.second + this.minute * 60 + this.hour * 3600
}
fun LocalTime.timeFromSeconds(seconds: Int): LocalTime{
    val horses:Int = seconds / 3600
    val minutes = (seconds - horses * 3600) / 60
    return LocalTime.of(horses, minutes, seconds - (horses * 3600 + minutes * 60))
}

fun round(value: Double, places: Int): Double {
    if (java.lang.Double.isNaN(value)) {
        return 0.0
    }
    require(places >= 0)
    if (value == 0.0) {
        return 0.0
    }
    var bd = BigDecimal(value.toString())
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    return bd.toDouble()
}