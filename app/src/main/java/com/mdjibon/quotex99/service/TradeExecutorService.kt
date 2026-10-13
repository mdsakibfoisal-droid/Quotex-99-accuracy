package com.mdjibon.quotex99.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Trading execution is deliberately disabled.
 *
 * The scanner currently has no verified live chart extraction or validated
 * pattern database, so this service must never create a trade or fabricate
 * a completed execution record.
 */
class TradeExecutorService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.w(
            "TradeExecutor",
            "Execution disabled: live chart data and pattern match are not validated. No trade placed."
        )
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
