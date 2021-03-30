package com.govnokoder.velotracker.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.govnokoder.velotracker.AppConstants;
import com.govnokoder.velotracker.BL.CurrentTraining;
import com.govnokoder.velotracker.BL.Model.Time;
import com.govnokoder.velotracker.BL.ParcelableTraining;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.R;
import com.govnokoder.velotracker.TrainingActivity;

public class LocationService extends Service {

    private static final String PACKAGE_NAME = "com.govnokoder.velotracker.services.locationservice";

    private static final String TAG = LocationService.class.getSimpleName();

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    public static final String EXTRA_PARCELABLE_TRAINING = PACKAGE_NAME + ".location";

    private final IBinder mBinder = new LocalBinder();

    private LocationRequest mLocationRequest;

    private int locationsBeforeStart = 2;

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationCallback mLocationCallback;

    CurrentTraining currentTraining;
    private CountDownTimer countDownTimer;
    private TrainingServiceNotificationManager notificationManager;

    private Handler mServiceHandler;

    public void onPause() {
        if (currentTraining != null && currentTraining.isRunning) {
            currentTraining.Pause();
        }
    }

    public void onResume() {
        if (currentTraining != null && !currentTraining.isRunning) {
            currentTraining.Resume();
        }
    }

    public void onStop(boolean isSave) {
        countDownTimer.cancel();
        if(isSave){
            currentTraining.StopAndSave(getApplicationContext());
        }
        stopSelf();
    }

    public LocationService() { }

    @SuppressLint("ServiceCast")
    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        currentTraining = new CurrentTraining();
        currentTraining.Date.setCurrentDate();
        currentTraining.isRunning = true;

        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (currentTraining != null) {
                    if (currentTraining.isRunning) {
                        currentTraining.Time.addSecond();
                        notificationManager.updateNotificationText(
                                getApplicationContext().getString(R.string.app_name),
                                currentTraining.Time.toString() + " | " +
                                        Training.round(currentTraining.CurrentSpeed, 1) + " " +
                                        getApplicationContext().getString(R.string.kph) + " | " +
                                        Training.round(currentTraining.Distance, 2) + " " +
                                        getApplicationContext().getString(R.string.km));
                    } else {
                        notificationManager.updateNotificationText(
                                getApplicationContext().getString(R.string.pause_button), currentTraining.Time.toString() + " | " +
                                        Training.round(currentTraining.CurrentSpeed, 1) + " " +
                                        getApplicationContext().getString(R.string.kph) + " | " +
                                        Training.round(currentTraining.Distance, 2) + " " +
                                        getApplicationContext().getString(R.string.km));
                    }
                }
                sendMessageToActivity();
            }

            @Override
            public void onFinish() {
                if (currentTraining.isRunning) {
                    countDownTimer.start();
                }
            }
        };

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationsBeforeStart != 0){
                    locationsBeforeStart -=1 ;
                    return;
                }
                currentTraining.setValuesFromLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        notificationManager = new TrainingServiceNotificationManager(this);
        startNotification();
        if (currentTraining != null) {
            countDownTimer.start();
        }
    }

    private void sendMessageToActivity(){
        Intent intent = new Intent(ACTION_BROADCAST);
        long height = 0;
        if(currentTraining.Heights.size() > 0) {
            height = currentTraining.Heights.get(currentTraining.Heights.size() - 1);
        }
        ParcelableTraining parcelableTraining = new ParcelableTraining(currentTraining.Time, currentTraining.Distance, currentTraining.MaxSpeed,
                currentTraining.AverageSpeed, currentTraining.Lines, currentTraining.CurrentLine, currentTraining.MaxHeight, height,
                currentTraining.MinHeight, currentTraining.AverageHeight, currentTraining.isRunning, currentTraining.originLocation);
        intent.putExtra(EXTRA_PARCELABLE_TRAINING, parcelableTraining);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "in onRebind()");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");
        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    public void startNotification(){
        Intent notificationIntent = new Intent(this, TrainingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1010, notificationIntent, 0);
        notificationManager.updatePendingIntent(pendingIntent);
        startForeground(TrainingServiceNotificationManager.NOTIFICATION_ID, notificationManager.getNotification());
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                //currentTraining.setValuesFromLocation(task.getResult());
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(AppConstants.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(AppConstants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setWaitForAccurateLocation(true);
        mLocationRequest.setSmallestDisplacement(1);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, getMainLooper());
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }
}
