package com.coursework.velotracker.BL.Model

class UnitsConverter {

    enum class Units(){
        FEET, METERS
    }

    private fun convertKilometersToMiles(value: Double): Double {
        return value / 1.609344
    }

    private fun convertMetersToFeet(value: Long, ): Long {
        return (value.toDouble() / 0.3048).toLong()
    }

    fun convertKilometersToUnits(value: Double, units: Units):Double{
        return when(units){
            Units.FEET -> UnitsConverter().convertKilometersToMiles(value)
            Units.METERS -> value
        }
    }

    fun convertMetersToUnits(value: Long, units: Units):Long{
        return when(units){
            Units.FEET -> UnitsConverter().convertMetersToFeet(value)
            Units.METERS -> value
        }
    }
}

