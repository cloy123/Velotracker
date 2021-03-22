package com.govnokoder.velotracker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.govnokoder.velotracker.R;

public class TrainingServiceNotificationManager {
    public static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = TrainingServiceNotificationManager.class.getSimpleName();
    private final NotificationCompat.Builder notificationBuilder;
    private final NotificationManager notificationManager;

    public TrainingServiceNotificationManager(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                notificationChannel.setAllowBubbles(true);
            }

            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationBuilder
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.notification_icon);
    }

    public void updateContent(String content) {
        notificationBuilder.setSubText(content);
        updateNotification();
    }

    public void updateNotificationText(String title, String text){
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(text);
        updateNotification();
        notificationBuilder.setOnlyAlertOnce(true);
    }

    public void updatePendingIntent(PendingIntent pendingIntent) {
        notificationBuilder.setContentIntent(pendingIntent);
        updateNotification();
    }

    public void cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public Notification getNotification() {
        return notificationBuilder.build();
    }

    private void updateNotification() {
        notificationManager.notify(NOTIFICATION_ID, getNotification());
    }
}

