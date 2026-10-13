package com.mdjibon.quotex99.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.mdjibon.quotex99.engine.CandleAnalyzer

/**
 * Safe scanning loop. Until real screen capture and verified pattern matching
 * are wired in, this service must not create trades from fabricated data.
 */
class ScanningService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val analyzer = CandleAnalyzer()
    private var scanningRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (scanningRunnable == null) startLoop()
        return START_STICKY
    }

    private fun startLoop() {
        scanningRunnable = object : Runnable {
            override fun run() {
                val candles = analyzer.detectCandles()
                if (candles.size < 2) {
                    Log.w("ScanningService", "NO SIGNAL: live chart candles are not available")
                } else {
                    // Deliberately do not emit a trade signal until validated pattern data
                    // and a real screen-capture pipeline are connected.
                    Log.i("ScanningService", "Candles detected, but pattern database is not validated; NO SIGNAL")
                }
                handler.postDelayed(this, 1200L)
            }
        }
        handler.post(scanningRunnable!!)
    }

    override fun onDestroy() {
        scanningRunnable?.let { handler.removeCallbacks(it) }
        scanningRunnable = null
        super.onDestroy()
    }
}