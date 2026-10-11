package com.mdjibon.quotex99.engine

data class CandleInfo(
    val index: Int,
    val color: String,
    val bodySize: String,
    val upperWick: String,
    val lowerWick: String,
    val market: String = "UNKNOWN"
)

data class PatternSignature(
    val candle1: CandleInfo,
    val candle2: CandleInfo,
    val trend: String,
    val patterns: List<String>
)

class CandleAnalyzer {
    fun detectCandles(): List<CandleInfo> = listOf(
        CandleInfo(1, "GREEN", "MEDIUM", "MEDIUM", "SMALL", "EURUSD"),
        CandleInfo(2, "RED", "MEDIUM", "SMALL", "MEDIUM", "EURUSD")
    )

    fun createPatternSignature(candles: List<CandleInfo>): PatternSignature {
        val first = candles.firstOrNull() ?: CandleInfo(1, "GREEN", "MEDIUM", "MEDIUM", "SMALL", "EURUSD")
        val second = candles.getOrNull(1) ?: CandleInfo(2, "RED", "MEDIUM", "SMALL", "MEDIUM", "EURUSD")
        return PatternSignature(first, second, "REVERSAL", listOf("bullish", "retracement"))
    }
}
