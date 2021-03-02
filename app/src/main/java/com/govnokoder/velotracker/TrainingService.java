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
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Chronometer;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.govnokoder.velotracker.BL.CurrentTraining;
import com.govnokoder.velotracker.BL.Model.Time;
import com.govnokoder.velotracker.BL.LocListenerInterface;
import com.govnokoder.velotracker.BL.MyLocListener;
import com.govnokoder.velotracker.messages.MessageEvent;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class TrainingService extends Service implements LocListenerInterface {
    private static final int ONGOING_NOTIFICATION_ID = 333;
    private Icon myIcon = null;

    //Location
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

        if (currentTraining != null) {
            if (location != null) {
                if(location.hasSpeed()){

                    double speed = location.getSpeed() * 3.6;
                    double distance = 0;
                    if(currentTraining.originLocation != null) {
                        distance = currentTraining.originLocation.distanceTo(location)/1000;
                    }
                    long height = (long) location.getAltitude();


                    if(currentTraining.isRunning) {
                        //скорость
                        if(speed > currentTraining.MaxSpeed) {
                            currentTraining.MaxSpeed = speed;
                        }
                        currentTraining.CurrentSpeed = speed;
                        //длина пути
                        currentTraining.WayLength += distance;
                        //путь
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        currentTraining.CurrentLine.add(latLng);

                        //startForeground(Double.toString(location.getLongitude()));
                        //Высота
                        currentTraining.Heights.add(height);
                        currentTraining.MinHeight = Long.min(height, currentTraining.MinHeight);
                        currentTraining.MaxHeight = Long.max(height, currentTraining.MaxHeight);
                        currentTraining.SumHeight += height;
                        currentTraining.AverageHeight = currentTraining.SumHeight / currentTraining.Heights.size();
                    }
                }else {
                    //скорость
                    currentTraining.CurrentSpeed = 0;
                }
                currentTraining.originLocation = location;
            }
        }
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
        myIcon = Icon.createWithResource(this, R.mipmap.ic_launcher);
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(currentTraining != null){
                    if(currentTraining.isRunning){
                        currentTraining.Time.addSecond();
                    }
                    startForeground(currentTraining.Time.toString());
                }
            }
            @Override
            public void onFinish() {
                if(currentTraining.isRunning){
                    countDownTimer.start();
                }
            }
        };
        if(currentTraining != null && currentTraining.isRunning){
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
        startForeground("start");
        init();
        return START_STICKY;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.POSTING)
    public void onEvent(MessageEvent event) {
        currentTraining = event.currentTraining;
        EventBus.getDefault().unregister(this);
        startForeground("Сервис работает");
    }


    public void startForeground(String string) {
        String CHANNEL_DEFAULT_IMPORTANCE = createChannel(this);

        Intent notificationIntent = new Intent(this, TrainingActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1010, notificationIntent, 0);

        //Intent intent = new Intent(this, MainActivity.class);
        //PendingIntent pendingIntent1 = PendingIntent.getActivity(this, ONGOING_NOTIFICATION_ID, intent, 0);

        //Notification.Action action1 = new Notification.Action.Builder(myIcon, "Stop", pendingIntent).build();


//        Notification.Action action =  new Notification.Action.Builder(R.drawable.tracking_off,
//                "stop",
//                PendingIntent.getService(this, ONGOING_NOTIFICATION_ID, new Intent(this, TrainingActivity.class),0)).build();
        Notification notification =
                new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)//CHANNEL_DEFAULT_IMPORTANCE
                        .setContentTitle("Идёт запись Training")
                        .setContentText(string)
                        .setSmallIcon(myIcon)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.ticker_text))
                        .build();
// Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    private String createChannel(Service context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String id = context.getString(R.string.notification_channel_id);
        String name = context.getString(R.string.notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;//.IMPORTANCE_DEFAULT IMPORTANCE_HIGH
        notificationManager.createNotificationChannel(new NotificationChannel(id, name, importance));
        return id;
    }
}

