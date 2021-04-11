package com.coursework.velotracker.BL.Model.Training

import android.location.Location
import android.os.Parcelable
import com.coursework.velotracker.BL.Model.Line
import kotlinx.parcelize.Parcelize

import java.time.LocalTime

@Parcelize
data class ParcelableTraining(val time: LocalTime,
                              val totalDistance: Double,
                              val maxSpeed: Double,
                              val averageSpeed: Double,
                              val currentSpeed: Double,
                              val lines: MutableList<Line>,
                              val currentLine: Line,
                              val currentHeight: Long,
                              val maxHeight: Long,
                              val minHeight: Long,
                              val averageHeight: Long,
                              val isRunning: Boolean,
                              val originLocation: Location?) : Parcelable {

    constructor(trainingRecorder: TrainingRecorder) : this(
            trainingRecorder.totalTime,
            trainingRecorder.totalDistance,
            trainingRecorder.maxSpeed,
            trainingRecorder.averageSpeed,
            trainingRecorder.currentSpeed,
            trainingRecorder.lines,
            trainingRecorder.currentLine,
            trainingRecorder.currentHeight,
            trainingRecorder.maxHeight,
            trainingRecorder.minHeight,
            trainingRecorder.averageHeight,
            trainingRecorder.isRunning,
            trainingRecorder.originLocation)
}