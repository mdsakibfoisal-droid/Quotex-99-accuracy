package com.mdjibon.quotex99.engine

import android.util.Log
import java.util.Locale

/**
 * 15-second wall-clock cycle helper.
 *
 * The cycle is aligned to Unix wall-clock 15-second boundaries rather than
 * resetting whenever the app/service starts. This is suitable only when the
 * target chart uses the same wall-clock boundary; the UI must still verify
 * the chart's own candle countdown before treating a window as actionable.
 */
class TimingEngine(
    private val clockMillis: () -> Long = { System.currentTimeMillis() }
) {
    private val tag = "TimingEngine"

    companion object {
        const val CANDLE_DURATION_MS = 15_000L
        const val TRADE_WINDOW_START_MS = 10_000L
        const val TRADE_WINDOW_END_MS = 14_999L

        fun elapsedInCycle(nowMillis: Long): Long =
            Math.floorMod(nowMillis, CANDLE_DURATION_MS)

        fun remainingInCycle(nowMillis: Long): Long =
            CANDLE_DURATION_MS - elapsedInCycle(nowMillis)

        fun isInLastFiveSeconds(nowMillis: Long): Boolean {
            val elapsed = elapsedInCycle(nowMillis)
            return elapsed in TRADE_WINDOW_START_MS..TRADE_WINDOW_END_MS
        }
    }

    fun resetCandleTimer() {
        // Kept for compatibility with existing callers. A wall-clock aligned
        // cycle must not be reset to the moment the user tapped the icon.
        Log.d(tag, "Using wall-clock aligned 15-second cycle")
    }

    fun getTimeInCandle(): Long = elapsedInCycle(clockMillis())

    fun getTimeRemaining(): Long = remainingInCycle(clockMillis())

    fun isValidTradingWindow(): Boolean {
        val elapsed = getTimeInCandle()
        val valid = elapsed in TRADE_WINDOW_START_MS..TRADE_WINDOW_END_MS
        Log.d(tag, "Elapsed: ${elapsed}ms, Last-five-second window: $valid")
        return valid
    }

    fun getCandleProgress(): Float =
        (getTimeInCandle().toFloat() / CANDLE_DURATION_MS) * 100f

    fun getFormattedTimeRemaining(): String {
        val remaining = getTimeRemaining()
        val seconds = (remaining / 1000).toInt()
        val tenths = ((remaining % 1000) / 100).toInt()
        return String.format(Locale.US, "%02d:%01d", seconds, tenths)
    }
}
