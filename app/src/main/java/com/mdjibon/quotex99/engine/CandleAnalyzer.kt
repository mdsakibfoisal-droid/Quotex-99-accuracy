package com.mdjibon.quotex99.engine

import android.util.Log

data class CandleInfo(
    val index: Int,
    val color: String,
    val bodySize: String,
    val upperWick: String,
    val lowerWick: String,
    val market: String = "EURUSD"
)

data class PatternSignature(
    val candle1: CandleInfo,
    val candle2: CandleInfo,
    val trend: String,
    val patterns: List<String>
)

/**
 * Live chart extraction is not implemented yet.
 * Fail closed: never manufacture candles or a pattern signal from demo values.
 */
class CandleAnalyzer {
    private val tag = "CandleAnalyzer"

    fun detectCandles(): List<CandleInfo> {
        Log.w(tag, "Live chart candle detection is not implemented; no candles returned")
        return emptyList()
    }

    fun createPatternSignature(candles: List<CandleInfo>): PatternSignature {
        require(candles.size >= 2) {
            "At least two real, detected candles are required to create a pattern signature"
        }
        return PatternSignature(
            candle1 = candles[candles.size - 2],
            candle2 = candles[candles.size - 1],
            trend = "UNKNOWN",
            patterns = emptyList()
        )
    }

    fun analyzeChartImage(chartBytes: ByteArray?): List<CandleInfo>? {
        if (chartBytes.isNullOrEmpty()) {
            Log.w(tag, "No chart image supplied")
        } else {
            Log.w(tag, "Chart-image candle extraction is not implemented; refusing to invent candle data")
        }
        return null
    }
}