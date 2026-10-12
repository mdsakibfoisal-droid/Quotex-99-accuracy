package com.mdjibon.quotex99.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat

class NotificationService(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "trade_channel",
                "Trade Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for executed trades"
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    audioAttributes
                )
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showTradeNotification(market: String, action: String, score: Float) {
        val notification = NotificationCompat.Builder(context, "trade_channel")
            .setContentTitle("✅ Trade Executed")
            .setContentText("$action on $market (Score: ${"%2.1f".format(score)}%)")
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500))
            .build()

        notificationManager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    fun vibratePhone() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(longArrayOf(0, 200, 100, 200), -1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
