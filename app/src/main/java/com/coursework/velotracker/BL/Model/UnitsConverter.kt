package com.coursework.velotracker.BL.Model

class UnitsConverter {

    enum class Units(){
        FEET, METERS
    }

    fun convertKilometersToMiles(value: Double): Double {
        return value / 1.609344
    }

    fun convertMetersToFeet(value: Long, ): Long {
        return (value.toDouble() / 0.3048).toLong()
    }

    fun convertKilometersToUnits(value: Double, units: Units):Double{
        return when(units){
            UnitsConverter.Units.FEET -> UnitsConverter().convertKilometersToMiles(value)
            UnitsConverter.Units.METERS -> value
        }
    }

    fun convertMetersToUnits(value: Long, units: Units):Long{
        return when(units){
            UnitsConverter.Units.FEET -> UnitsConverter().convertMetersToFeet(value)
            UnitsConverter.Units.METERS -> value
        }
    }
}

