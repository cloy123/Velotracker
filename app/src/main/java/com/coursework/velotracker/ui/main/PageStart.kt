package com.coursework.velotracker.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.coursework.velotracker.AppConstants
import com.coursework.velotracker.BL.Controller.TrainingController
import com.coursework.velotracker.BL.Model.Training.TrainingStatistics
import com.coursework.velotracker.BL.Model.Training.round
import com.coursework.velotracker.BL.Model.Training.toString
import com.coursework.velotracker.R


class PageStart(): Fragment() {

    private var pageNumber = 1
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
         fun newInstance(page: Int): PageStart{
            val fragment = PageStart()
            val args:Bundle = Bundle()
            args.putInt("num", page)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageNumber = arguments?.getInt("num")?:1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val result = inflater.inflate(R.layout.start_page, container, false)
        linearLastTraining = result.findViewById(R.id.linearLastTraining)
        linearLastTraining.setOnClickListener { onSomeEventListener.openLastTraining() }
        startButton = result.findViewById(R.id.startButton)
        startButton.setOnClickListener(View.OnClickListener { onSomeEventListener.startTraining() })
        linearStat = result.findViewById(R.id.linearStat)
        linearStat.setOnClickListener(View.OnClickListener { onSomeEventListener.openStatistic() })
        totalDistanceText = result.findViewById(R.id.totalDistanceTextV)
        lastTrainingDateText = result.findViewById(R.id.lastTrainingDateTextV)
        lastTrainingDistanceText = result.findViewById(R.id.lastDistanceTextV)
        lastTrainingAverageSpeedText = result.findViewById(R.id.lastAverageSpeedTextV)
        lastTrainingTimeText = result.findViewById(R.id.lastTimeTextV)
        return result;
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            onSomeEventListener = context as OnSomeEventListener
        }catch (e: java.lang.ClassCastException){
            throw ClassCastException("$context must implement OnSomeEventListener")
        }
    }

    override fun onResume() {
        super.onResume()
        val trainings = loadTrainingStatistics()
        if(trainings?.size!! > 0){
            setTrainingValues(trainings)
        }else{
            setDefaultTrainingValues()
        }
    }

    private fun loadTrainingStatistics(): MutableList<TrainingStatistics>? {
        val trainingController = context?.let { TrainingController(it) }
        return trainingController?.loadTrainings()
    }

    @SuppressLint("SetTextI18n")
    fun setTrainingValues(trainings: MutableList<TrainingStatistics>){
        val lastTraining = trainings[trainings.size - 1]
        var totalDistance = 0.0
        for(training in trainings)
        {
            totalDistance+=training.totalDistance
        }
        totalDistanceText.text = round(totalDistance, 2).toString() + getString(R.string.km)
        lastTrainingTimeText.text = lastTraining.totalTime.toString(AppConstants.TIME_FORMAT)
        lastTrainingDateText.text = lastTraining.date.toString(AppConstants.DATE_FORMAT)
        lastTrainingAverageSpeedText.text = round(lastTraining.averageSpeed, 1).toString() + getString(R.string.kph)
        lastTrainingDistanceText.text = round(lastTraining.totalDistance, 2).toString() + getString(R.string.km)
    }

    @SuppressLint("SetTextI18n")
    fun setDefaultTrainingValues(){
        lastTrainingTimeText.text = "00:00"
        lastTrainingAverageSpeedText.text = "0 " + getString(R.string.kph)
        lastTrainingDistanceText.text = "0 " + getString(R.string.km)
        lastTrainingDateText.text = "00/00/0000";
        totalDistanceText.text = "0 " + getString(R.string.km)
    }
}