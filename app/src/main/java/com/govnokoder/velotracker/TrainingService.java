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
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Chronometer;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.govnokoder.velotracker.BL.CurrentTraining;
import com.govnokoder.velotracker.BL.LocListenerInterface;
import com.govnokoder.velotracker.BL.MyLocListener;
import com.govnokoder.velotracker.messages.MessageEvent;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class TrainingService extends Service implements LocListenerInterface {

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";
    private static final int ONGOING_NOTIFICATION_ID = 333;

    private Location originLocation;

    private Icon myIcon = null;

    //Training
    public Chronometer chronometer;

    //Location
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 500L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    CurrentTraining currentTraining;

    private MyLocListener myLocListener;
    private LocationManager locationManager;

    private void init() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocListener = new MyLocListener();
        myLocListener.setLocListenerInterface(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL_IN_MILLISECONDS, 1, myLocListener);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (currentTraining != null) {
            if (location != null) {

                //TODO доделать нормально
                if(location.hasSpeed()){

                    double speed = location.getSpeed() * 3.6;
                    double distance = 0;
                    if(originLocation != null) {
                        distance = originLocation.distanceTo(location)/1000;
                    }
                    int height = (int)location.getAltitude();


                    if(currentTraining.isRunning) {
                        //скорость
                        if(speed > currentTraining.MaxSpeed) {
                            currentTraining.MaxSpeed = speed;
                        }
                        currentTraining.AverageSpeed = speed;
                        //длина пути
                        currentTraining.WayLength += distance;
                        //путь
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        currentTraining.currentLine.add(latLng);

                        startForeground(Double.toString(location.getLongitude()));
                        //Высота
                        currentTraining.heights.add(height);
                    }
                }else {
                    //скорость
                    currentTraining.AverageSpeed = 0;

                }
                originLocation = location;
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
        myIcon = Icon.createWithResource(this, R.drawable.ic_launcher_foreground);
        chronometer = new Chronometer(this);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }



    @Override
    public void onDestroy() {
        EventBus.getDefault().post(new MessageEvent(currentTraining));
        locationManager.removeUpdates(myLocListener);
        //EventBus.getDefault().unregister(this);
        super.onDestroy();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground("start");
        init();
        return START_STICKY;
    }

    @Subscribe//(threadMode = ThreadMode.MAIN)
    public void onEvent(MessageEvent event) {
        currentTraining = event.currentTraining;
        EventBus.getDefault().unregister(this);
        startForeground("Сервис работает");
    }


    public void startForeground(String string) {
        String CHANNEL_DEFAULT_IMPORTANCE = createChannel(this);

        Intent notificationIntent = new Intent(this, TrainingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1010, notificationIntent, 0);

        Intent intent = new Intent(this, TrainingActivity.class);
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
        int importance = NotificationManager.IMPORTANCE_HIGH;//.IMPORTANCE_DEFAULT
        notificationManager.createNotificationChannel(new NotificationChannel(id, name, importance));
        return id;
    }
}

