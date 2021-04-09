package com.coursework.velotracker.ui.main

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.coursework.velotracker.AppConstants
import com.coursework.velotracker.BL.Controller.TrainingController
import com.coursework.velotracker.BL.Model.TrainingStatistics
import com.coursework.velotracker.BL.Model.round
import com.coursework.velotracker.BL.Model.toString


class PageStart(): Fragment() {
    private val pageNumber = 1
    private lateinit var startButton: Button
    private lateinit var linearStat: LinearLayout
    private lateinit var linearLastTraining:LinearLayout
    private lateinit var totalDistanceText: TextView
    private lateinit var lastTrainingDateText: TextView
    private lateinit var lastTrainingTimeText: TextView
    private lateinit var lastTrainingDistanceText:TextView
    private lateinit var lastTrainingAverageSpeedText:TextView
    private lateinit var onSomeEventListener: OnSomeEventListener

    public interface OnSomeEventListener{
        public fun startTraining()
        public fun openLastTraining()
        public fun openStatistic()
    }

    companion object{
         fun newInstance(page: Int):PageStart{
            val fragment = PageStart()
            val args:Bundle = Bundle()
            args.putInt("num", page)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            onSomeEventListener = context as OnSomeEventListener
        }catch (e:java.lang.ClassCastException){
            throw ClassCastException(context.toString() + " must implement OnSomeEventListener")
        }
    }

    override fun onResume() {
        super.onResume()
        val trainingController = TrainingController(context)
        val trainings = trainingController.loadTrainings()
        if(trainings != null && trainings!!.size > 0){


        }
    }

    fun setTrainingValues(trainings: MutableList<TrainingStatistics>){
        val lastTraining = trainings[trainings.size - 1]
        var totalDistance = 0.0
        for(training in trainings)
        {
            totalDistance+=training.totalDistance
        }
        totalDistanceText.text = round(totalDistance, 2).toString()
        lastTrainingTimeText.text = lastTraining.totalTime.toString(AppConstants.TIME_FORMAT)
        lastTrainingDateText.text = lastTraining.date.toString(AppConstants.DATE_FORMAT)
        lastTrainingAverageSpeedText.text = round(lastTraining.averageSpeed, 1).toString()
        lastTrainingDistanceText.text = round(lastTraining.totalDistance, 2).toString()


        totalDistanceText.text = round()
    }
}