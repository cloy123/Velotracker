package com.coursework.velotracker.BL.Controller

import android.content.Context
import com.coursework.velotracker.BL.Model.TrainingRecorder
import com.coursework.velotracker.BL.Model.TrainingStatistics
import com.google.gson.Gson
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

class TrainingController(context: Context?) {
    private val TRAINING_FILE_NAME = "data.json"
    private var trainings: MutableList<TrainingStatistics> = ArrayList()
    private var currentTraining: TrainingStatistics = TrainingStatistics()
    private var context: Context? = context

    private var streamReader: InputStreamReader? = null
    private var fileInputStream: FileInputStream? = null
    private var fileOutputStream: FileOutputStream? = null


    fun RemoveTraining(index: Int) {
        trainings.removeAt(index)
        save()
    }

    fun DeleteAll() {
        trainings.clear()
        save()
    }

    fun setNewTrainingData(trainingRecorder: TrainingRecorder) {
        currentTraining = TrainingStatistics(trainingRecorder)
        trainings.add(currentTraining)
        save()
    }

    fun loadTrainings(): MutableList<TrainingStatistics> {
        fileInputStream = null
        streamReader = null
        try {
            return getTrainingsFromJson()
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            closeStreamReader()
            closeFileInputStream()
        }
        return ArrayList()
    }

    private fun save(): Boolean {
        fileOutputStream = null
        try {
            saveTrainingsToGson()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            closeFileOutputStream()
        }
        return false
    }


    private fun getTrainingsFromJson():MutableList<TrainingStatistics>{
        fileInputStream = context?.openFileInput(TRAINING_FILE_NAME)
        streamReader = InputStreamReader(fileInputStream)
        val gson = Gson()
        val dataItems: DataItems = gson.fromJson(streamReader, DataItems::class.java)
        return dataItems.trainings
    }

    private fun saveTrainingsToGson(){
        val gson = Gson()
        val dataItems = DataItems()
        dataItems.trainings = trainings
        val jsonString = gson.toJson(dataItems)
        val fileOutputStream = context?.openFileOutput(TRAINING_FILE_NAME, Context.MODE_PRIVATE)
        fileOutputStream?.write(jsonString.toByteArray())
    }

    private fun closeStreamReader(){
        try {
            streamReader?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun closeFileInputStream(){
        try {
            fileInputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun closeFileOutputStream(){
        try {
            fileOutputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private class DataItems {
        var trainings: MutableList<TrainingStatistics> = ArrayList()
    }
}