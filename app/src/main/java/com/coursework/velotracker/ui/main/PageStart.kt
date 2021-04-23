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
import com.coursework.velotracker.BL.Model.Extensions.round
import com.coursework.velotracker.BL.Model.Extensions.toStringExtension
import com.coursework.velotracker.BL.Model.Training.TrainingStatistics
import com.coursework.velotracker.R
import com.coursework.velotracker.databinding.HistoryPageBinding
import com.coursework.velotracker.databinding.StartPageBinding


class PageStart(): Fragment() {

    private var _binding: StartPageBinding? = null
    private val binding get() = _binding!!
    private var pageNumber = 1
    private lateinit var onSomeEventListener: OnSomeEventListener

    interface OnSomeEventListener{
        fun startTraining()
        fun openLastTraining()
        fun openStatistic()
    }

    companion object{
         fun newInstance(page: Int): PageStart{
            val fragment = PageStart()
            val args = Bundle()
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
        _binding = StartPageBinding.inflate(inflater, container, false)
        initFunc()
        return binding.root
    }

    private fun initFunc(){
        binding.linearLastTraining.setOnClickListener { onSomeEventListener.openLastTraining() }
        binding.startButton.setOnClickListener(View.OnClickListener { onSomeEventListener.startTraining() })
        binding.linearStat.setOnClickListener(View.OnClickListener { onSomeEventListener.openStatistic() })
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
        trainings.forEach{
            totalDistance+= it.totalDistance
        }
        binding.totalDistanceTextV.text = round(totalDistance, 2).toString() + getString(R.string.km)
        binding.lastTimeTextV.text = lastTraining.totalTime.toStringExtension()
        binding.lastTrainingDateTextV.text = lastTraining.date.toStringExtension(AppConstants.DATE_FORMAT)
        binding.lastAverageSpeedTextV.text = round(lastTraining.averageSpeed, 1).toString() + getString(R.string.kph)
        binding.lastDistanceTextV.text = round(lastTraining.totalDistance, 2).toString() + getString(R.string.km)
    }

    @SuppressLint("SetTextI18n")
    fun setDefaultTrainingValues(){
        binding.lastTimeTextV.text = "00:00"
        binding.lastAverageSpeedTextV.text = "0 " + getString(R.string.kph)
        binding.lastDistanceTextV.text = "0 " + getString(R.string.km)
        binding.lastTrainingDateTextV.text = "00/00/00";
        binding.totalDistanceTextV.text = "0 " + getString(R.string.km)
    }
}