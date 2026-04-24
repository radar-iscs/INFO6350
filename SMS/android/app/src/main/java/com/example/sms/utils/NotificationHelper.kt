package com.example.sms.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.sms.R

object NotificationHelper {
    private const val CHANNEL_ID = "sms_channel"

    fun showNotification(context: Context, message: String) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "SMS Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("RadarApp SMS Detected")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        manager.notify(1, notification)
    }
}