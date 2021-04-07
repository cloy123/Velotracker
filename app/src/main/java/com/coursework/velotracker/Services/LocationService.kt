package com.coursework.velotracker.Services

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.*
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.coursework.velotracker.AppConstants
import com.coursework.velotracker.BL.Model.*
import com.coursework.velotracker.R
import com.coursework.velotracker.Timer.Timer
import com.google.android.gms.location.*
import java.time.LocalDate
import java.time.LocalTime

class LocationService: Service(), Timer.OnTickListener {

    companion object {
        private val PACKAGE_NAME = "com.govnokoder.velotracker.services.locationservice"
        val TABLE_USER_ATTRIBUTE_DATA = "data"
        private var TAG = LocationService::class.java.simpleName
        val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
        val EXTRA_PARCELABLE_TRAINING = "$PACKAGE_NAME.parcelabletraining"
    }

    private val mBinder: IBinder = LocalBinder()

    private var mLocationRequest: LocationRequest? = null

    private var locationsBeforeStart = 1
    private var secondsBeforeStart = 5
    private var isStart = false

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var mLocationCallback: LocationCallback? = null

    var trainingRecorder: TrainingRecorder? = null
    private var timer: Timer? = null

    private var myNotificationManager: MyNotificationManager? = null

    private var mServiceHandler: Handler? = null

    private var wakeLock: WakeLock? = null

    fun onPause() {
        if (trainingRecorder!!.isRunning) {
            trainingRecorder!!.Pause()
        }
    }

    fun onResume() {
        if (!trainingRecorder!!.isRunning) {
            trainingRecorder!!.Resume()
        }
    }

    fun onStop(isSave: Boolean) {
        timer!!.stop()
        if (isSave) {
            trainingRecorder!!.StopAndSave(applicationContext)
        }
        releaseWakeLock()
        stopSelf()
    }


    @SuppressLint("ServiceCast")
    override fun onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        trainingRecorder = TrainingRecorder()
        trainingRecorder!!.date = LocalDate.now()
        trainingRecorder!!.isRunning = true

        timer = Timer(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (locationResult.lastLocation == null) {
                    return
                }
                if (locationsBeforeStart != 0) {
                    locationsBeforeStart -= 1
                    return
                } else if (!isStart) {
                    isStart = true
                    trainingRecorder!!.isRunning = true
                }
                trainingRecorder!!.setValuesFromLocation(locationResult.lastLocation)
            }
        }
        createLocationRequest()
        getLastLocation()
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        myNotificationManager = MyNotificationManager(this)
        startNotification()
        timer!!.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service started")
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig!!)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onRebind(intent: Intent?) {
        Log.i(TAG, "in onRebind()")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Last client unbound from service")
        return true
    }

    override fun onDestroy() {
        mServiceHandler!!.removeCallbacksAndMessages(null)
        releaseWakeLock()
    }

    fun releaseWakeLock() {
        if (wakeLock!!.isHeld) {
            wakeLock!!.release()
        }
    }

    @SuppressLint("WakelockTimeout")
    fun acquireWakeLock() {
        Log.i(TAG, "Acquiring wake lock.")
        val context = applicationContext
        try {
            val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
            if (powerManager == null) {
                Log.e(TAG, "Power manager null.")
                return
            }
            if (wakeLock == null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
                if (wakeLock == null) {
                    Log.e(TAG, "Cannot create a new wake lock.")
                    return
                }
            }
            if (!wakeLock!!.isHeld()) {
                wakeLock!!.acquire()
                if (!wakeLock!!.isHeld()) {
                    Log.e(TAG, "Cannot acquire wake lock.")
                }
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, e.message, e)
        }
    }

    fun startNotification() {
        val notificationIntent = Intent(this, TrainingActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1010, notificationIntent, 0)
        myNotificationManager!!.updatePendingIntent(pendingIntent)
        startForeground(
            NOTIFICATION_ID, myNotificationManager!!.getNotification()
        )
    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient!!.lastLocation
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful || task.result == null) {
                        Log.w(TAG, "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.setInterval(AppConstants.UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setFastestInterval(AppConstants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        //mLocationRequest.setMaxWaitTime(AppConstants.MAX_WAIT_TIME_IN_IN_MILLISECONDS);
        mLocationRequest!!.setWaitForAccurateLocation(true)
        mLocationRequest!!.setSmallestDisplacement(1f)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            mainLooper
        )
        acquireWakeLock()
    }

    inner class LocalBinder : Binder() {
        fun getService() : LocationService?{
            return this@LocationService
        }
    }

    private fun isCanStart():Boolean{
        if(isStart){
            return true
        }
        return if (secondsBeforeStart != 0) {
            secondsBeforeStart -= 1
            false
        } else {
            true
        }
    }

    override fun onTick(time: LocalTime) {
        if(!isStart){
            if (isCanStart()) {
                isStart = true
            }
        }
        if (trainingRecorder != null) {
            updateNotificationText()
        }
        sendMessageToActivity()
    }

    private fun updateNotificationText(){
        if (trainingRecorder!!.isRunning) {
            myNotificationManager!!.updateNotificationText(applicationContext.getString(R.string.pause_button), getNotificationTextIfNotRunning())
        }else{
            myNotificationManager!!.updateNotificationText(applicationContext.getString(R.string.pause_button), getNotificationTextIfRunning())
        }
    }

    private fun getNotificationTextIfRunning():String{
        return  trainingRecorder!!.totalTime.toString().toString() + " | " +
                 round(trainingRecorder!!.currentSpeed, 1) + " " +
                 applicationContext.getString(R.string.kph) + " | " +
                 round(trainingRecorder!!.totalDistance, 2) + " " +
                 applicationContext.getString(R.string.km)
    }

    private fun getNotificationTextIfNotRunning():String{
        return trainingRecorder!!.totalTime.toString(AppConstants.TIME_FORMAT) + " | " +
                round(trainingRecorder!!.currentSpeed, 1) + " " +
                applicationContext.getString(R.string.kph) + " | " +
                round(trainingRecorder!!.totalDistance, 2) + " " +
                applicationContext.getString(R.string.km)
    }

    private fun sendMessageToActivity() {
        val intent = Intent(ACTION_BROADCAST)
        var height: Long = 0
        if (trainingRecorder!!.heights.size > 0) {
            height = trainingRecorder!!.heights[trainingRecorder!!.heights.size - 1]
        }
        val parcelableTraining = ParcelableTraining(trainingRecorder!!)
        intent.putExtra(EXTRA_PARCELABLE_TRAINING, parcelableTraining)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }
}