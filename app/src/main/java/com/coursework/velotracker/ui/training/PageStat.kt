package com.coursework.velotracker.ui.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.coursework.velotracker.Messages.SharedViewModel
import com.coursework.velotracker.R

class PageStat(): Fragment() {
    private var pageNumber: Int = 1

    private lateinit var timeText: TextView

    private lateinit var wayLengthText: TextView
    private lateinit var speedText: TextView
    private lateinit var averageSpeedText: TextView
    private lateinit var maxSpeedText: TextView
    private lateinit var heightText: TextView
    private lateinit var averageHeightText: TextView
    private lateinit var minHeightText: TextView
    private lateinit var maxHeightText: TextView

    companion object{
        fun newInstance(page: Int):PageStat{
            val fragment:PageStat = PageStat()
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
        val result: View = inflater.inflate(R.layout.training_stat_page, container, false)
        timeText = result.findViewById(R.id.TimeText)
        wayLengthText = result.findViewById(R.id.wayLengthText)
        speedText = result.findViewById(R.id.speedText)
        averageSpeedText = result.findViewById(R.id.averageSpeedText)
        maxSpeedText = result.findViewById(R.id.maxSpeedText)
        heightText = result.findViewById(R.id.heightText)
        averageHeightText = result.findViewById(R.id.averageHeightText)
        minHeightText = result.findViewById(R.id.minHeightText)
        maxHeightText = result.findViewById(R.id.maxHeightText)
        return result
    }

    override fun onResume() {
        super.onResume()
        //val model: SharedViewModel = ViewModelProvider(requireActivity())
        TODO("доделать ViewModel")
    }
}