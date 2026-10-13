package com.mdjibon.quotex99.engine

import android.util.Log

/**
 * Returns null until genuine chart candles can be extracted.
 * Callers must treat null as NO SIGNAL.
 */
class PatternDetector {
    fun detectPattern(): PatternSignature? {
        val analyzer = CandleAnalyzer()
        val candles = analyzer.detectCandles()
        if (candles.size < 2) {
            Log.d("PatternDetector", "Insufficient verified candles; signal=NO SIGNAL")
            return null
        }
        return runCatching { analyzer.createPatternSignature(candles) }
            .onFailure { Log.w("PatternDetector", "Pattern signature unavailable", it) }
            .getOrNull()
    }
}
