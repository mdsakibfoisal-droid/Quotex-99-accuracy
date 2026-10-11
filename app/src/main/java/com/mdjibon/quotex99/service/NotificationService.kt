package com.mdjibon.quotex99.service

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

class NotificationService(private val context: Context) {
    fun showTradeNotification(market: String, action: String) {
        val notification = NotificationCompat.Builder(context, "trade_channel")
            .setContentTitle("Trade Executed")
            .setContentText("$action on $market")
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java) as NotificationManager
        manager.notify(1, notification)
    }
}
