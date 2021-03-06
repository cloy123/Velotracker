package com.coursework.velotracker.Services

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.coursework.velotracker.AppConstants
import com.coursework.velotracker.BL.Model.*
import com.coursework.velotracker.BL.Model.Extensions.round
import com.coursework.velotracker.BL.Model.Extensions.toStringExtension
import com.coursework.velotracker.BL.Model.Training.ParcelableTraining
import com.coursework.velotracker.BL.Model.Training.TrainingRecorder
import com.coursework.velotracker.R
import com.coursework.velotracker.Services.MyNotificationManager.Companion.NOTIFICATION_ID
import com.coursework.velotracker.Timer.Timer
import com.coursework.velotracker.TrainingActivity
import com.google.android.gms.location.*
import java.time.LocalDate
import java.time.LocalTime

class LocationService: Service(), Timer.OnTickListener {

    private val mBinder: IBinder = LocalBinder()
    private var secondsBeforeStart = 5
    private var locationsBeforeStart = 1
    private var isStart = false
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    var trainingRecorder: TrainingRecorder = TrainingRecorder()
    private var timer: Timer = Timer(this)
    private lateinit var myNotificationManager: MyNotificationManager
    private lateinit var mServiceHandler: Handler
    private var wakeLock: WakeLock? = null

    companion object {
        private val PACKAGE_NAME = "com.coursework.velotracker.Services.locationservice"
        val TABLE_USER_ATTRIBUTE_DATA = "data"
        private var TAG = LocationService::class.java.simpleName
        val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
        val EXTRA_PARCELABLE_TRAINING = "$PACKAGE_NAME.parcelabletraining"
    }

    fun onPause() {
        if (trainingRecorder.isRunning) {
            trainingRecorder.pause()
            timer.pause()
        }
    }

    fun onResume() {
        if (!trainingRecorder.isRunning) {
            trainingRecorder.resume()
            timer.resume()
        }
    }

    fun onStop(isSave: Boolean) {
        timer.stop()
        if (isSave) {
            trainingRecorder.stopAndSave(applicationContext)
        }
        releaseWakeLock()
        stopSelf()
    }

    @SuppressLint("ServiceCast")
    override fun onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        myNotificationManager = MyNotificationManager(this)

        val accessFineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!accessFineLocation)
            stopSelf()

        mFusedLocationClient.requestLocationUpdates(createLocationRequest(), MyLocationCallback(), mainLooper)

        acquireWakeLock()

        trainingRecorder.date = LocalDate.now()
        trainingRecorder.resume()

        getLastLocation()
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        startNotification()
        timer.start()
        timer.pause()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service started")
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onRebind(intent: Intent) {
        Log.i(TAG, "in onRebind()")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.i(TAG, "Last client unbound from service")
        return true
    }

    override fun onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null)
        releaseWakeLock()
    }

    private fun releaseWakeLock() {
        if (wakeLock!!.isHeld) {
            wakeLock!!.release()
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        Log.i(TAG, "Acquiring wake lock.")
        val context = applicationContext
        try {
            val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
            if (wakeLock == null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
                if (wakeLock == null) {
                    Log.e(TAG, "Cannot create a new wake lock.")
                    return
                }
            }
            if (!wakeLock!!.isHeld) {
                wakeLock!!.acquire()
                if (!wakeLock!!.isHeld) {
                    Log.e(TAG, "Cannot acquire wake lock.")
                }
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, e.message, e)
        }
    }

    private fun startNotification() {
        val notificationIntent = Intent(this, TrainingActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1010, notificationIntent, 0)
        myNotificationManager.updatePendingIntent(pendingIntent)
        startForeground(NOTIFICATION_ID, myNotificationManager.getNotification())
    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful || task.result == null) {
                        Log.w(TAG, "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = AppConstants.UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = AppConstants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.isWaitForAccurateLocation = true
        locationRequest.smallestDisplacement = 1f
        return locationRequest
    }

    inner class LocalBinder : Binder() {
        fun getService() : LocationService {
            return this@LocationService
        }
    }

    override fun onTick(time: LocalTime) {
        if(isCanStart()){
            trainingRecorder.totalTime = time
            updateNotificationText()
            sendMessageToActivity()
        }
    }

    private fun isCanStart():Boolean{
        return if(!isStart){
            if(secondsBeforeStart != 0){
                secondsBeforeStart -=1
            }
            else{
                isStart = true
                timer.resume()
                trainingRecorder.resume()
            }
            false
        }
        else{
            true
        }
    }

    private fun updateNotificationText(){
        if (trainingRecorder.isRunning) {
            myNotificationManager.updateNotificationText(applicationContext.getString(R.string.app_name), getNotificationTextIfNotRunning())
        }else{
            myNotificationManager.updateNotificationText(applicationContext.getString(R.string.pause_button), getNotificationTextIfRunning())
        }
    }

    private fun getNotificationTextIfRunning():String{
        return  trainingRecorder.totalTime.toStringExtension() + " | " +
                 round(trainingRecorder.currentSpeed, 1) + " " + applicationContext.getString(R.string.kph) + " | " +
                 round(trainingRecorder.totalDistance, 2) + " " + applicationContext.getString(R.string.km)
    }

    private fun getNotificationTextIfNotRunning():String{
        return trainingRecorder.totalTime.toStringExtension() + " | " +
                round(trainingRecorder.currentSpeed, 1) + " " + applicationContext.getString(R.string.kph) + " | " +
                round(trainingRecorder.totalDistance, 2) + " " + applicationContext.getString(R.string.km)
    }

    private fun sendMessageToActivity() {
        val intent = Intent(ACTION_BROADCAST)
        val parcelableTraining = ParcelableTraining(trainingRecorder)
        intent.putExtra(EXTRA_PARCELABLE_TRAINING, parcelableTraining)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private inner class MyLocationCallback(): LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            //первое местоположение не считаю т.к. оно может быть совсем не правильным
            //если второе местоположение не приходит то жду 5 секунд и начинаю считать уже все местоположения
            if (!isStart) {
                if(locationsBeforeStart != 0){
                    locationsBeforeStart -=1
                }
                else{
                    isStart = true
                    timer.resume()
                    trainingRecorder.resume()
                    return
                }
            }else{
                trainingRecorder.setValuesFromLocation(locationResult.lastLocation)
            }
        }
    }
}