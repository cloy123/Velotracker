package com.coursework.velotracker.BL.Model.Extensions

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun LocalDate.toStringExtension(format: String):String{
    val formatter = DateTimeFormatter.ofPattern(format)
    return format(formatter)
}

fun LocalTime.toStringExtension():String{
    var format = "mm:ss"
    if(this.hour > 0){
        format = "HH:mm:ss"
    }
    val formatter = DateTimeFormatter.ofPattern(format)
    return format(formatter)
}

fun getAllSeconds(localTime: LocalTime): Int {
    return localTime.second + localTime.minute * 60 + localTime.hour * 3600
}

fun timeFromSeconds(seconds: Int): LocalTime {
    val horses = seconds / 3600
    val minutes = (seconds - horses * 3600) / 60
    return LocalTime.of(horses, minutes, seconds - (horses * 3600 + minutes * 60))
}

fun round(value: Double, places: Int): Double {
    if (java.lang.Double.isNaN(value))
        return 0.0
    require(places >= 0)
    if (value == 0.0 || value.isInfinite())
        return 0.0
    var bd = BigDecimal(value.toString())
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    return bd.toDouble()
}