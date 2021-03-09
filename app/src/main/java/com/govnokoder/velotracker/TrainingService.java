package com.govnokoder.velotracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Chronometer;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.govnokoder.velotracker.BL.CurrentTraining;
import com.govnokoder.velotracker.BL.Model.Time;
import com.govnokoder.velotracker.BL.LocListenerInterface;
import com.govnokoder.velotracker.BL.Model.Training;
import com.govnokoder.velotracker.BL.MyLocListener;
import com.govnokoder.velotracker.messages.MessageEvent;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class TrainingService extends Service implements LocListenerInterface {
    private static final int ONGOING_NOTIFICATION_ID = 333;

    private Icon myIcon = null;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 500L;
    CurrentTraining currentTraining;
    private MyLocListener myLocListener;
    private LocationManager locationManager;
    private CountDownTimer countDownTimer;

    private void init() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocListener = new MyLocListener();
        myLocListener.setLocListenerInterface(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL_IN_MILLISECONDS, 1, myLocListener);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (currentTraining != null) { currentTraining.setValuesFromLocation(location);}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        currentTraining = EventBus.getDefault().getStickyEvent(MessageEvent.class).currentTraining;
        if(currentTraining != null){
            EventBus.getDefault().removeAllStickyEvents();
            EventBus.getDefault().unregister(this);
        }
        myIcon = Icon.createWithResource(this, R.drawable.notification_icon);
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(currentTraining != null){
                    if(currentTraining.isRunning){
                        currentTraining.Time.addSecond();
                        startForeground(getApplicationContext().getString(R.string.app_name),
                                currentTraining.Time.toString() + " | " +
                                        Training.round(currentTraining.CurrentSpeed, 1) + " " +
                                        getApplicationContext().getString(R.string.kph) + " | " +
                                        Training.round(currentTraining.Distance,2) + " " +
                                        getApplicationContext().getString(R.string.km));
                    }else {
                        startForeground(getApplicationContext().getString(R.string.pause_button),currentTraining.Time.toString() + " | " +
                                Training.round(currentTraining.CurrentSpeed, 1) +  " " +
                                getApplicationContext().getString(R.string.kph) + " | " +
                                Training.round(currentTraining.Distance,2) + " " +
                                getApplicationContext().getString(R.string.km));
                    }
                }
            }
            @Override
            public void onFinish() {
                if(currentTraining.isRunning){
                    countDownTimer.start();
                }
            }
        };
        if(currentTraining != null){
            countDownTimer.start();
        }
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
        EventBus.getDefault().postSticky(new MessageEvent(currentTraining));
        locationManager.removeUpdates(myLocListener);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return START_STICKY;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.POSTING)
    public void onEvent(MessageEvent event) {
        currentTraining = event.currentTraining;
        EventBus.getDefault().unregister(this);
    }

    public void startForeground(String title, String text) {
        String CHANNEL_DEFAULT_IMPORTANCE = createChannel(this);
        Intent notificationIntent = new Intent(this, TrainingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1010, notificationIntent, 0);
        Notification notification =
                new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)//CHANNEL_DEFAULT_IMPORTANCE
                        .setContentTitle(title)
                        .setContentText(text)
                        .setSmallIcon(myIcon)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.ticker_text))
                        .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    private String createChannel(Service context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String id = context.getString(R.string.notification_channel_id);
        String name = context.getString(R.string.notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT                                                                                                                                                                                                                                                         ;//.IMPORTANCE_DEFAULT IMPORTANCE_HIGH
        NotificationChannel notificationChannel = new NotificationChannel(id, name, importance);
        notificationChannel.enableVibration(false);
        notificationChannel.enableLights(false);
       // notificationChannel.setSound(null, AudioAttributes.);
        notificationManager.createNotificationChannel(notificationChannel);
        return id;
    }
}

