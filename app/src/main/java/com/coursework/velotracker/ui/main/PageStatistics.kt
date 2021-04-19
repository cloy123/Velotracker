package com.coursework.velotracker.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.coursework.velotracker.AppConstants
import com.coursework.velotracker.BL.Controller.TrainingController
import com.coursework.velotracker.BL.Model.Extensions.*
import com.coursework.velotracker.BL.Model.Line
import com.coursework.velotracker.BL.Model.Training.*
import com.coursework.velotracker.MapViewInScroll
//import com.coursework.velotracker.MapViewInScroll
import com.coursework.velotracker.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import java.time.LocalTime


class PageStatistics(): Fragment(), OnMapReadyCallback {

    private var pageNumber = 1
    private lateinit var mapView: MapViewInScroll
    private lateinit var mapboxMap: MapboxMap
    private lateinit var lineManager: LineManager
    private lateinit var symbolManager: SymbolManager

    private lateinit var totalDistanceTextView: TextView
    private lateinit var totalRecordsTextView: TextView
    private lateinit var totalTimeTextView: TextView
    private lateinit var maxSpeedTextView: TextView
    private lateinit var averageSpeedTextView: TextView
    private lateinit var tempTextView: TextView
    private lateinit var maxTempTextView: TextView
    private lateinit var averageHeightTextView: TextView
    private lateinit var maxHeightTextView: TextView
    private lateinit var minHeightTextView: TextView

    private lateinit var trainings: MutableList<TrainingStatistics>

    companion object{
        fun newInstance(page: Int): PageStatistics{
            val fragment = PageStatistics()
            val args: Bundle = Bundle()
            args.putInt("num", page)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), getString(R.string.access_token))
        pageNumber = arguments?.getInt("num")?:1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val result = inflater.inflate(R.layout.statistics_page, container, false)
        mapView = result.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        totalDistanceTextView = result.findViewById(R.id.totalDistanceText)
        totalRecordsTextView = result.findViewById(R.id.totalRecordsText)
        totalTimeTextView = result.findViewById(R.id.totalTimeText)
        maxSpeedTextView = result.findViewById(R.id.maxSpeedText)
        averageSpeedTextView = result.findViewById(R.id.averageSpeedText)
        tempTextView = result.findViewById(R.id.tempText)
        maxTempTextView = result.findViewById(R.id.maxTempText)
        averageHeightTextView = result.findViewById(R.id.averageHeightText)
        maxHeightTextView = result.findViewById(R.id.maxHeightText)
        minHeightTextView = result.findViewById(R.id.minHeightText)
        return result
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(AppConstants.MAP_STYLE) { style ->
            lineManager = LineManager(mapView, mapboxMap, style)
            symbolManager = SymbolManager(mapView, mapboxMap, style)
            symbolManager.iconAllowOverlap = true
            symbolManager.textAllowOverlap = true
            for (training in trainings) {
                if (training.lines.isNotEmpty()) {
                    for (line in training.lines) {
                        drawLine(line)
                    }
                }
                drawMarker(training.getStartPoint(), training.date.toString(AppConstants.DATE_FORMAT))
            }
        }
    }

    private fun drawLine(line: Line) {
        val lineOptions = LineOptions()
        lineOptions.withLatLngs(line)
        lineOptions.withLineColor(AppConstants.LINE_COLOR)
        lineOptions.withLineWidth(AppConstants.LINE_WIDTH)
        lineManager.create(lineOptions)
    }

    private fun drawMarker(latLng: LatLng, text: String) {
        val symbolOptions = SymbolOptions()
        symbolOptions.withLatLng(latLng)
        symbolOptions.withTextAnchor(text)
        symbolOptions.withIconSize(3.0f)
        symbolOptions.withIconColor(AppConstants.LINE_COLOR)
        symbolOptions.withIconImage("marker-15")
        symbolManager.create(symbolOptions)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        trainings = TrainingController(context).loadTrainings()
        if (trainings.size != 0) {
            setValues()
        }else{
            setDefaultValues()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDefaultValues(){
        totalDistanceTextView.text = "0" + " " + getString(R.string.km)
        totalRecordsTextView.text = "0"
        totalTimeTextView.text = "00:00"
        maxSpeedTextView.text = "0" + " " + getString(R.string.kph)
        averageSpeedTextView.text = "0" + " " + getString(R.string.kph)
        tempTextView.text = "00:00" + " /" + getString(R.string.km)
        maxTempTextView.text = "00:00" + " /" + getString(R.string.km)
        averageHeightTextView.text = "0" + " " + getString(R.string.m)
        minHeightTextView.text = ""
        maxHeightTextView.text = ""
    }

    @SuppressLint("SetTextI18n")
    private fun setValues(){
        var totalDist = 0.0
        var totalSeconds: Long = 0
        var maxSpeed: Double = trainings[0].maxSpeed
        var maxHeight: Long = trainings[0].maxHeight
        var minHeight: Long = trainings[0].minHeight
        val totalRecords: Int = trainings.size
        var totalHeights: Long = 0
        var totalHeightsValues: Long = 0
        var totalTempSeconds: Long = 0
        var maxTemp: LocalTime = trainings[0].temp

        for (training in trainings) {
            totalDist += training.totalDistance
            totalSeconds += getAllSeconds(training.totalTime)
            totalHeights += training.averageHeight * training.heights.size
            totalHeightsValues += training.heights.size
            totalTempSeconds += getAllSeconds(training.temp)
            if (training.maxSpeed > maxSpeed) {
                maxSpeed = training.maxSpeed
            }
            if (training.maxHeight > maxHeight) {
                maxHeight = training.maxHeight
            }
            if (training.minHeight < minHeight) {
                minHeight = training.minHeight
            }
            if (getAllSeconds(training.temp) < getAllSeconds(maxTemp)) {
                maxTemp = training.temp
            }
        }

        val averageSpeed = totalDist / (totalSeconds.toDouble() / 3600)
        val averageTemp: LocalTime = timeFromSeconds((totalTempSeconds / trainings.size).toInt())
        val averageHeight = totalHeights / totalHeightsValues
        var maxHeightStr = maxHeight.toString() + " " + getString(R.string.m)
        if (maxHeight == Long.MIN_VALUE) {
            maxHeightStr = ""
        }
        var minHeightStr = minHeight.toString() + " " + getString(R.string.m)
        if (minHeight == Long.MAX_VALUE) {
            minHeightStr = ""
        }

        totalDistanceTextView.text = round(totalDist, 2).toString() + " " + getString(R.string.km)
        totalRecordsTextView.text = totalRecords.toString()
        totalTimeTextView.text = timeFromSeconds(totalSeconds.toInt()).toStringExtension()
        maxSpeedTextView.text = round(maxSpeed, 1).toString() + " " + getString(R.string.kph)
        averageSpeedTextView.text = round(averageSpeed, 1).toString() + " " + getString(R.string.kph)
        tempTextView.text = averageTemp.toStringExtension() + " /" + getString(R.string.km)
        maxTempTextView.text = maxTemp.toStringExtension() + " /" + getString(R.string.km)
        averageHeightTextView.text = averageHeight.toString() + " " + getString(R.string.m)
        minHeightTextView.text = minHeightStr
        maxHeightTextView.text = maxHeightStr
    }


    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}