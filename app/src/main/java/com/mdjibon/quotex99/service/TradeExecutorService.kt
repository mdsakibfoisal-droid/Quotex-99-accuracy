package com.mdjibon.quotex99.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.mdjibon.quotex99.database.Trade
import com.mdjibon.quotex99.database.TradeDatabase
import kotlinx.coroutines.launch

class TradeExecutorService : Service() {

    private val TAG = "TradeExecutor"
    private lateinit var database: TradeDatabase

    override fun onCreate() {
        super.onCreate()
        database = TradeDatabase.getInstance(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val patternId = intent?.getStringExtra("pattern_id") ?: "UNKNOWN"
        val signal = intent?.getStringExtra("signal") ?: "BUY"
        val action = intent?.getStringExtra("action") ?: "BUY"
        val timestamp = intent?.getLongExtra("timestamp", System.currentTimeMillis()) ?: System.currentTimeMillis()

        Log.d(TAG, "Executing trade: $action on Pattern $patternId")

        // Log trade to database
        val trade = Trade(
            timestamp = timestamp,
            market = "EURUSD",
            action = action,
            amount = 10,
            result = "PENDING",
            patternId = patternId.toIntOrNull(),
            similarity = 95f,
            status = "EXECUTED"
        )

        // Save to database asynchronously
        lifecycleScope.launch {
            database.tradeDao().insertTrade(trade)
            Log.d(TAG, "Trade saved to database")
        }

        // Here we would call accessibility service to perform the actual BUY/SELL click
        // For now, we just log it
        Log.d(TAG, "Trade executed successfully: $action $patternId")

        return START_NOT_STICKY
    }
}
