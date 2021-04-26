package com.coursework.velotracker.ui.fragments.training

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.coursework.velotracker.databinding.TrainingMapPageBinding
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
import java.lang.ClassCastException


class PageMap(): Fragment(), OnMapReadyCallback, OnCameraTrackingChangedListener {

    private var _binding: TrainingMapPageBinding? = null
    private val binding get() = _binding!!

    private var pageNumber: Int = 0
    private lateinit var mapView: MapViewInScroll
    private lateinit var mapboxMap: MapboxMap
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
        fun newInstance(page: Int): PageMap {
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
        _binding = TrainingMapPageBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        initFunc()
        return binding.root
    }

    private fun initFunc(){
        binding.pauseButton.setOnClickListener(this::onClickPauseButton)
        binding.resumeButton.setOnClickListener(this::onClickResumeButton)
        binding.stopButton.setOnClickListener(this::onClickStopButton)
    }

    private fun onClickPauseButton(v: View) {
        if (mParcelableTraining != null) {
            onSomeEventListener.onPauseTraining()
            setButtonsStatePause()
        }
    }

    private fun setButtonsStatePause(){
        binding.pauseButton.visibility = View.INVISIBLE;
        binding.pauseButton.isEnabled = false;
        binding.resumeButton.isEnabled = true;
        binding.resumeButton.visibility = View.VISIBLE;
        binding.stopButton.isEnabled = true;
        binding.stopButton.visibility = View.VISIBLE;
    }

    private fun onClickResumeButton(v: View) {
        if (mParcelableTraining != null) {
            onSomeEventListener.onResumeTraining()
            setButtonsStateResume()
        }
    }

    private fun setButtonsStateResume(){
        binding.pauseButton.visibility = View.VISIBLE;
        binding.pauseButton.isEnabled = true;
        binding.resumeButton.visibility = View.INVISIBLE;
        binding.resumeButton.isEnabled = false;
        binding.stopButton.visibility = View.INVISIBLE;
        binding.stopButton.isEnabled = false;
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
        binding.locationButton.setOnClickListener(View.OnClickListener {
            if (!isInTrackingMode) {
                locationComponent.cameraMode = CameraMode.TRACKING
                locationComponent.zoomWhileTracking(16.0)
                binding.locationButton.setImageResource(R.drawable.tracking_on)
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
                    mParcelableTraining = it
                    if (it.isRunning) {
                        setButtonsStateResume()
                    } else {
                        setButtonsStatePause()
                    }
                    setValues()
                drawLines()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    fun setValues(){
        if(mParcelableTraining != null){
            binding.timeText.text = mParcelableTraining!!.time.toStringExtension()
            binding.currentSpeedText.text = round(mParcelableTraining!!.currentSpeed, 1).toString() + " " + getString(R.string.kph)
            binding.wayLengthText.text = round(mParcelableTraining!!.totalDistance, 2).toString() + " " + getString(R.string.km)
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
        binding.locationButton.setImageResource(R.drawable.tracking_off)
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