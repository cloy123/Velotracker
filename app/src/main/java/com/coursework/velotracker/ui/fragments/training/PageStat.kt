package com.coursework.velotracker.ui.fragments.training

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.coursework.velotracker.BL.Model.Extensions.round
import com.coursework.velotracker.BL.Model.Extensions.toStringExtension
import com.coursework.velotracker.BL.Model.Training.ParcelableTraining
import com.coursework.velotracker.ViewModels.SharedViewModel
import com.coursework.velotracker.R
import com.coursework.velotracker.databinding.TrainingStatPageBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PageStat(): Fragment() {

    private var _binding: TrainingStatPageBinding? = null
    private val binding get() = _binding!!
    private var pageNumber: Int = 1

    companion object{
        fun newInstance(page: Int): PageStat {
            val fragment = PageStat()
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
        _binding = TrainingStatPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val model: SharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        model.parcelableTraining.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                GlobalScope.launch {
                    setValues(it)
                }
            }
        })
    }
    @SuppressLint("SetTextI18n")
    fun setValues(parcelableTraining: ParcelableTraining){
        binding.timeText.text = parcelableTraining.time.toStringExtension()
        binding.wayLengthText.text = round(parcelableTraining.totalDistance, 2).toString() + " " + getString(R.string.km)
        binding.speedText.text = round(parcelableTraining.currentSpeed, 1).toString() + " " + getString(R.string.kph)
        binding.averageSpeedText.text = round(parcelableTraining.averageSpeed, 1).toString() + " " + getString(R.string.kph)
        binding.maxSpeedText.text = round(parcelableTraining.maxSpeed, 1).toString() + " " + getString(R.string.kph)
        binding.heightText.text = parcelableTraining.currentHeight.toString() + " " + getString(R.string.m)
        var maxHeight = parcelableTraining.maxHeight.toString() + " " + getString(R.string.m)
        if (parcelableTraining.maxHeight == Long.MIN_VALUE) {
            maxHeight = ""
        }
        var minHeight = parcelableTraining.minHeight.toString() + " " + getString(R.string.m)
        if (parcelableTraining.minHeight == Long.MAX_VALUE) {
            minHeight = ""
        }
        var averageHeight = parcelableTraining.averageHeight.toString() + " " + getString(R.string.m)
        if(parcelableTraining.averageHeight == Long.MIN_VALUE){
            averageHeight = " "
        }
        binding.maxHeightText.text = maxHeight
        binding.minHeightText.text = minHeight
        binding.averageHeightText.text = averageHeight
    }
}