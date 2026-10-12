package com.mdjibon.quotex99.engine

import android.util.Log

class TimingEngine {

    private val TAG = "TimingEngine"
    private var candleStartTime = System.currentTimeMillis()
    private val CANDLE_DURATION = 15000L // 15 seconds in milliseconds
    private val TRADE_WINDOW_START = 10000L // Start trading at 10 seconds

    fun resetCandleTimer() {
        candleStartTime = System.currentTimeMillis()
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
        val timeRemaining = getTimeRemaining()
        val isValid = timeRemaining in 1..5000L
        Log.d(TAG, "Time remaining: ${timeRemaining}ms, Valid trade window: $isValid")
        return isValid
    }

    fun getCandleProgress(): Float {
        val elapsed = getTimeInCandle()
        return (elapsed.toFloat() / CANDLE_DURATION) * 100f
    }
}
