package com.coursework.velotracker

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.coursework.velotracker.BL.Controller.TrainingController
import com.coursework.velotracker.BL.Model.Extensions.round
import com.coursework.velotracker.BL.Model.Extensions.toString
import com.coursework.velotracker.BL.Model.Extensions.toStringExtension
import com.coursework.velotracker.BL.Model.Line
import com.coursework.velotracker.BL.Model.Training.TrainingStatistics
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions





class LookTraining(): AppCompatActivity(), OnMapReadyCallback {

    private lateinit var averageSpeedText:TextView
    private lateinit var timeText:TextView
    private lateinit var maxSpeedText:TextView
    private lateinit var distanceText:TextView
    private lateinit var averageHeightText:TextView
    private lateinit var tempText:TextView
    private lateinit var maxHeightText:TextView
    private lateinit var minHeightText:TextView

    private lateinit var mapView: MapViewInScroll
    private lateinit var mapboxMap: MapboxMap
    private lateinit var lineManager: LineManager
    private lateinit var deleteButton: AppCompatButton
    private var trainingController: TrainingController = TrainingController(this)

    private var index: Int = 0

    private lateinit var currentTraining: TrainingStatistics

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_look_training)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        distanceText = findViewById(R.id.distanceText);
        maxSpeedText = findViewById(R.id.maxSpeedText);
        averageSpeedText = findViewById(R.id.averageSpeedText);
        timeText = findViewById(R.id.timeText);
        averageHeightText = findViewById(R.id.averageHeightText);
        tempText = findViewById(R.id.tempText);
        maxHeightText = findViewById(R.id.maxHeightText);
        minHeightText = findViewById(R.id.minHeightText);

        val bundle = intent.extras
        index = bundle!!["INDEX"] as Int
        trainingController = TrainingController(applicationContext)
        val trainings = trainingController.loadTrainings()
        currentTraining = trainings[index]

        distanceText.text = round(currentTraining.totalDistance, 2).toString() + " " + getString(R.string.km)
        maxSpeedText.text = round(currentTraining.maxSpeed, 1).toString() + " " + getString(R.string.kph)
        averageSpeedText.text = round(currentTraining.averageSpeed, 1).toString() + " " + getString(R.string.kph)
        timeText.text = currentTraining.totalTime.toStringExtension()
        averageHeightText.text = currentTraining.averageHeight.toString() + " " + getString(R.string.m)
        tempText.text = currentTraining.temp.toStringExtension() + " /" + getString(R.string.km)
        maxHeightText.text = currentTraining.maxHeight.toString() + " " + getString(R.string.m)
        minHeightText.text = currentTraining.minHeight.toString() + " " + getString(R.string.m)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = currentTraining.date.toString(AppConstants.DATE_FORMAT)

        deleteButton = findViewById<View>(R.id.deleteButton) as AppCompatButton
        deleteButton.setOnClickListener {
            dialogRemoveTraining()
        }
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
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
            if (currentTraining.lines.isNotEmpty()) {
                lineManager = LineManager(mapView, mapboxMap, mapboxMap.style!!)
                for (line in currentTraining.lines) {
                    drawLine(line)
                }
            }
        })
    }

    private fun drawLine(line: Line) {
        val lineOptions = LineOptions()
        lineOptions.withLatLngs(line)
        lineOptions.withLineColor(AppConstants.LINE_COLOR)
        lineOptions.withLineWidth(AppConstants.LINE_WIDTH)
        lineManager.create(lineOptions)
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        mapView.onStart()
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