package com.mdjibon.quotex99.engine

import android.util.Log
import java.util.*

class TimingEngine {
    private val TAG = "TimingEngine"
    private var candleStartTime = System.currentTimeMillis()
    private val CANDLE_DURATION = 15000L // 15 seconds
    private val TRADE_WINDOW_START = 10000L // Last 5 seconds (10s to 15s)
    private val TRADE_WINDOW_END = 15000L

    init {
        resetCandleTimer()
    }

    fun resetCandleTimer() {
        candleStartTime = System.currentTimeMillis()
        Log.d(TAG, "Candle timer reset")
    }

    fun getTimeInCandle(): Long {
        return System.currentTimeMillis() - candleStartTime
    }

    fun getTimeRemaining(): Long {
        val elapsed = getTimeInCandle()
        val remaining = CANDLE_DURATION - elapsed
        return if (remaining < 0) 0 else remaining
    }

    fun isValidTradingWindow(): Boolean {
        val elapsed = getTimeInCandle()
        val isValid = elapsed >= TRADE_WINDOW_START && elapsed <= TRADE_WINDOW_END
        Log.d(TAG, "Elapsed: ${elapsed}ms, Valid window: $isValid")
        return isValid
    }

    fun getCandleProgress(): Float {
        val elapsed = getTimeInCandle().toFloat()
        return (elapsed / CANDLE_DURATION) * 100f
    }

    fun getFormattedTimeRemaining(): String {
        val timeRemaining = getTimeRemaining()
        val seconds = (timeRemaining / 1000).toInt()
        val millis = ((timeRemaining % 1000) / 10).toInt()
        return String.format(Locale.US, "%02d:%02d", seconds, millis)
    }
}
