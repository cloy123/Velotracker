package com.coursework.velotracker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.coursework.velotracker.BL.Controller.TrainingController
import com.coursework.velotracker.BL.Model.Extensions.round
import com.coursework.velotracker.BL.Model.Extensions.toStringExtension
import com.coursework.velotracker.BL.Model.Line
import com.coursework.velotracker.BL.Model.Training.TrainingStatistics
import com.coursework.velotracker.databinding.ActivityLookTrainingBinding
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LookTraining(): AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLookTrainingBinding

    private lateinit var mapView: MapViewInScroll
    private lateinit var mapboxMap: MapboxMap
    private lateinit var lineManager: LineManager
    private var trainingController: TrainingController = TrainingController(this)

    private var index: Int = 0

    private lateinit var currentTraining: TrainingStatistics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        binding = ActivityLookTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        val bundle = intent.extras
        index = bundle!!["INDEX"] as Int
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        mapView.onStart()
        loadTraining()
        initFunc()
    }

    private fun loadTraining(){
        trainingController = TrainingController(applicationContext)
        val trainings = trainingController.loadTrainings()
        currentTraining = trainings[index]
    }

    private fun initFunc(){
        GlobalScope.launch {
            setValues()
        }
        createActionBar()
        binding.deleteButton.setOnClickListener {
            dialogRemoveTraining()
        }
    }

    private fun createActionBar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = currentTraining.date.toStringExtension(AppConstants.DATE_FORMAT)
    }

    @SuppressLint("SetTextI18n")
    fun setValues(){
        binding.distanceText.text = round(currentTraining.totalDistance, 2).toString() + " " + getString(R.string.km)
        binding.maxSpeedText.text = round(currentTraining.maxSpeed, 1).toString() + " " + getString(R.string.kph)
        binding.averageSpeedText.text = round(currentTraining.averageSpeed, 1).toString() + " " + getString(R.string.kph)
        binding.timeText.text = currentTraining.totalTime.toStringExtension()
        binding.averageHeightText.text = currentTraining.averageHeight.toString() + " " + getString(R.string.m)
        binding.tempText.text = currentTraining.temp.toStringExtension() + " /" + getString(R.string.km)
        binding.maxHeightText.text = currentTraining.maxHeight.toString() + " " + getString(R.string.m)
        binding.minHeightText.text = currentTraining.minHeight.toString() + " " + getString(R.string.m)
    }

    private fun dialogRemoveTraining() {
        val dialog: AlertDialog = AlertDialog.Builder(this).create()
        val cl = layoutInflater.inflate(R.layout.dialog_remove_training, null) as ConstraintLayout
        cl.getViewById(R.id.yesB).setOnClickListener {
            trainingController.removeTraining(index)
            dialog.dismiss()
            finish()
        }
        cl.getViewById(R.id.cancelB).setOnClickListener { dialog.dismiss() }
        dialog.setView(cl)
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(AppConstants.MAP_STYLE, Style.OnStyleLoaded {
            val latLng: LatLng = currentTraining.getStartPoint()
            val position = CameraPosition.Builder()
                .target(latLng)
                .zoom(15.0)
                .build()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(position)
            mapboxMap.moveCamera(cameraUpdate)
            drawLines()
        })
    }

    private fun drawLines(){
        if (currentTraining.lines.isNotEmpty()) {
            lineManager = LineManager(mapView, mapboxMap, mapboxMap.style!!)
            currentTraining.lines.forEach {
                drawLine(it)
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

    override fun onResume() {
        super.onResume()
        mapView.onResume()
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
        super.onDestroy()
        mapView.onDestroy()
    }
}