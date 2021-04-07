package com.coursework.velotracker.Services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.coursework.velotracker.R

class MyNotificationManager(context: Context) {

    companion object{
        private val CHANNEL_ID: String = MyNotificationManager::class.java.simpleName
        private var notificationBuilder: NotificationCompat.Builder? = null
        private var notificationManager: NotificationManager? = null
    }

    init{
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                notificationChannel.setAllowBubbles(true)
            }
            notificationManager!!.createNotificationChannel(notificationChannel)
        }

        notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        notificationBuilder!!
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentTitle(context.getString(R.string.app_name))
            .setSmallIcon(R.drawable.notification_icon)
    }

    fun updateNotificationText(title: String?, text: String?) {
        notificationBuilder!!.setContentTitle(title)
        notificationBuilder!!.setContentText(text)
        updateNotification()
        notificationBuilder!!.setOnlyAlertOnce(true)
    }

    fun updatePendingIntent(pendingIntent: PendingIntent?) {
        notificationBuilder!!.setContentIntent(pendingIntent)
        updateNotification()
    }

    fun cancelNotification() {
        notificationManager!!.cancel(NOTIFICATION_ID)
    }

    fun getNotification(): Notification? {
        return notificationBuilder!!.build()
    }

    private fun updateNotification() {
        notificationManager!!.notify(NOTIFICATION_ID, getNotification())
    }
}

val NOTIFICATION_ID = 123