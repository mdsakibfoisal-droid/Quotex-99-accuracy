package com.mdjibon.quotex99.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.mdjibon.quotex99.engine.CandleAnalyzer

/**
 * Safe scan worker. It must not create trade signals from absent or demo data.
 * Real screen capture and validated pattern records are not implemented yet.
 */
class ScanningService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val analyzer = CandleAnalyzer()
    private var scanRunnable: Runnable? = null
    private var running = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!running) {
            running = true
            scanRunnable = object : Runnable {
                override fun run() {
                    if (!running) return
                    val candles = analyzer.detectCandles()
                    Log.d("ScanningService", "No verified live chart data; candles=${candles.size}; signal=NO SIGNAL")
                    handler.postDelayed(this, 1200L)
                }
            }
            handler.post(scanRunnable!!)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        running = false
        scanRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }
}