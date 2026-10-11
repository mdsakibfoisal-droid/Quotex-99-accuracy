package com.mdjibon.quotex99.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.mdjibon.quotex99.engine.CandleAnalyzer
import com.mdjibon.quotex99.engine.PatternMatcher
import com.mdjibon.quotex99.engine.PatternSignature
import com.mdjibon.quotex99.engine.PatternStore

class ScanningService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val analyzer = CandleAnalyzer()
    private val matcher = PatternMatcher()
    private val patternStore = PatternStore()
    private var scanningRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLoop()
        return START_STICKY
    }

    private fun startLoop() {
        scanningRunnable = object : Runnable {
            override fun run() {
                val current = analyzer.detectCandles()
                val signature = analyzer.createPatternSignature(current)
                val bestMatch = matcher.findBestMatch(signature, patternStore.patterns)

                if (bestMatch != null && bestMatch.score >= 90f) {
                    val tradeIntent = Intent(this@ScanningService, TradeExecutorService::class.java).apply {
                        putExtra("market", bestMatch.pattern.market)
                        putExtra("action", bestMatch.pattern.action)
                        putExtra("score", bestMatch.score)
                    }
                    startService(tradeIntent)
                }

                handler.postDelayed(this, 1200)
            }
        }
        handler.post(scanningRunnable!!)
    }

    override fun onDestroy() {
        scanningRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }
}
