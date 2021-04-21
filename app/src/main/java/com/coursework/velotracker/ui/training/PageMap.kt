package com.coursework.velotracker.ui.training

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.coursework.velotracker.AppConstants
import com.coursework.velotracker.BL.Model.Extensions.round
import com.coursework.velotracker.BL.Model.Extensions.toStringExtension
import com.coursework.velotracker.BL.Model.Line
import com.coursework.velotracker.BL.Model.Training.ParcelableTraining
import com.coursework.velotracker.MainActivity
import com.coursework.velotracker.MapViewInScroll
import com.coursework.velotracker.ViewModels.SharedViewModel
import com.coursework.velotracker.R
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ClassCastException


class PageMap(): Fragment(), OnMapReadyCallback, OnCameraTrackingChangedListener {

    private var pageNumber: Int = 0
    private lateinit var mapView: MapViewInScroll
    private lateinit var mapboxMap: MapboxMap
    private lateinit var currentSpeedTextView: TextView
    private lateinit var wayLengthTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var pauseButton: Button
    private lateinit var resumeButton: Button
    private lateinit var stopButton: Button
    private lateinit var locationButton: ImageButton
    private var lineManager: LineManager? = null
    var isInTrackingMode = false
    private lateinit var locationComponent: LocationComponent
    val TAG = "TrainingActivity"
    private var isFinish = false
    private var mParcelableTraining: ParcelableTraining? = null
    private lateinit var onSomeEventListener: OnSomeEventListener

    interface OnSomeEventListener {
        fun onPauseTraining()
        fun onStopTraining(isSave: Boolean)
        fun onResumeTraining()
    }

    companion object{
        fun newInstance(page: Int): PageMap{
            val fragment = PageMap()
            val args = Bundle()
            args.putInt("num", page)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onSomeEventListener = context as OnSomeEventListener
        }catch (e:ClassCastException){
            throw ClassCastException("$context must implement onSomeEventListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), getString(R.string.access_token))
        pageNumber = arguments?.getInt("num")?:0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val result = inflater.inflate(R.layout.training_map_page, container, false)
        mapView = result.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        timeTextView = result.findViewById(R.id.TimeText)
        currentSpeedTextView = result.findViewById(R.id.CurrentSpeedText)
        wayLengthTextView = result.findViewById(R.id.WayLengthText)
        pauseButton = result.findViewById(R.id.PauseButton)
        pauseButton.setOnClickListener(this::onClickPauseButton)
        resumeButton = result.findViewById(R.id.ResumeButton)
        resumeButton.setOnClickListener(this::onClickResumeButton)
        stopButton = result.findViewById(R.id.StopButton)
        stopButton.setOnClickListener(this::onClickStopButton)
        locationButton = result.findViewById(R.id.locationButton)
        return result
    }

    private fun onClickPauseButton(v: View) {
        if (mParcelableTraining != null) {
            onSomeEventListener.onPauseTraining()
            setButtonsStatePause()
        }
    }

    private fun setButtonsStatePause(){
        pauseButton.visibility = View.INVISIBLE;
        pauseButton.isEnabled = false;
        resumeButton.isEnabled = true;
        resumeButton.visibility = View.VISIBLE;
        stopButton.isEnabled = true;
        stopButton.visibility = View.VISIBLE;
    }

    private fun onClickResumeButton(v: View) {
        if (mParcelableTraining != null) {
            onSomeEventListener.onResumeTraining()
            setButtonsStateResume()
        }
    }

    private fun setButtonsStateResume(){
        pauseButton.visibility = View.VISIBLE;
        pauseButton.isEnabled = true;
        resumeButton.visibility = View.INVISIBLE;
        resumeButton.isEnabled = false;
        stopButton.visibility = View.INVISIBLE;
        stopButton.isEnabled = false;
    }

    private fun onClickStopButton(v: View) {
        showExitDialog()
    }

    private fun showExitDialog(){
        val dialog: AlertDialog = AlertDialog.Builder(requireContext()).create()
        val cl = layoutInflater.inflate(R.layout.dialog_save_and_exit, null) as ConstraintLayout
        cl.getViewById(R.id.saveAndExitB).setOnClickListener {
            isFinish = true
            onSomeEventListener.onStopTraining(true)
            dialog.dismiss()
            requireActivity().finish()
        }
        cl.getViewById(R.id.exitWithoutSavingB).setOnClickListener {
            isFinish = true
            onSomeEventListener.onStopTraining(false)
            dialog.dismiss()
            requireActivity().finish()
        }
        cl.getViewById(R.id.cancel).setOnClickListener { dialog.dismiss() }
        dialog.setView(cl)
        dialog.show()
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style){
        if(!PermissionsManager.areLocationPermissionsGranted(context)){
            return
        }
        locationComponent = mapboxMap.locationComponent
        val locationComponentActivationOptions = LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle)
                .useDefaultLocationEngine(true)
                .build()
        locationComponent.activateLocationComponent(locationComponentActivationOptions)
        locationComponent.isLocationComponentEnabled = true
        locationComponent.cameraMode = CameraMode.TRACKING
        locationComponent.renderMode = RenderMode.NORMAL
        locationComponent.zoomWhileTracking(16.0)
        locationComponent.addOnCameraTrackingChangedListener(this)
        locationButton.setOnClickListener(View.OnClickListener {
            if (!isInTrackingMode) {
                locationComponent.cameraMode = CameraMode.TRACKING
                locationComponent.zoomWhileTracking(16.0)
                locationButton.setImageResource(R.drawable.tracking_on)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        val model: SharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        model.parcelableTraining.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                GlobalScope.launch {
                    mParcelableTraining = it
                    if (it.isRunning) {
                        setButtonsStateResume()
                    } else {
                        setButtonsStatePause()
                    }
                    setValues()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    fun setValues(){
        if(mParcelableTraining != null){
            timeTextView.text = mParcelableTraining!!.time.toStringExtension()
            currentSpeedTextView.text = round(mParcelableTraining!!.currentSpeed, 1).toString() + " " + getString(R.string.kph)
            wayLengthTextView.text = round(mParcelableTraining!!.totalDistance, 2).toString() + " " + getString(R.string.km)
            drawLines()
        }
    }

    private fun drawLines(){
        if(mParcelableTraining != null){
            if (mParcelableTraining!!.lines.size > 0) {
                lineManager?.deleteAll()
                for (line in mParcelableTraining!!.lines) {
                    drawLine(line)
                }
            }
            if (mParcelableTraining!!.currentLine.size > 0) {
                drawLine(mParcelableTraining!!.currentLine)
            }
        }
    }

    private fun drawLine(line: Line) {
        val lineOptions = LineOptions()
        lineOptions.withLatLngs(line)
        lineOptions.withLineColor(AppConstants.LINE_COLOR)
        lineOptions.withLineWidth(AppConstants.LINE_WIDTH)
        lineManager?.create(lineOptions)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        if(!isFinish){
            activity?.finish()
        }else run {
            val intent: Intent = Intent(activity?.applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
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

    override fun onCameraTrackingDismissed() {
        isInTrackingMode = false
        locationButton.setImageResource(R.drawable.tracking_off)
    }

    override fun onCameraTrackingChanged(currentMode: Int) { }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(AppConstants.MAP_STYLE, Style.OnStyleLoaded {
            enableLocationComponent(it)
            lineManager = LineManager(mapView, mapboxMap, it)
        })
    }
}