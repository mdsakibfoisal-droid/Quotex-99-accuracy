package com.mdjibon.quotex99.data

import com.mdjibon.quotex99.engine.PatternSignature

data class QuotexUiState(
    val currentCandle: CandleData?,
    val previousCandle: CandleData?,
    val marketTrend: MarketTrend,
    val currentPrice: Double,
    val accountBalance: Double,
    val timeRemaining: Int, // seconds
    val assetName: String,
    val isPriceMovingUp: Boolean,
    val confidence: Float, // 0-100
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QuotexUiState) return false
        
        return currentCandle == other.currentCandle &&
                previousCandle == other.previousCandle &&
                marketTrend == other.marketTrend &&
                currentPrice == other.currentPrice &&
                assetName == other.assetName &&
                timeRemaining == other.timeRemaining
    }

    override fun hashCode(): Int {
        var result = currentCandle?.hashCode() ?: 0
        result = 31 * result + (previousCandle?.hashCode() ?: 0)
        result = 31 * result + marketTrend.hashCode()
        result = 31 * result + currentPrice.hashCode()
        result = 31 * result + assetName.hashCode()
        return result
    }
}

data class CandleData(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val color: CandleColor,
    val volume: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
) {
    val body: Double get() = kotlin.math.abs(close - open)
    val wick: Double get() = high - low
    val isGreen: Boolean get() = color == CandleColor.GREEN
    val isRed: Boolean get() = color == CandleColor.RED
}

enum class CandleColor {
    GREEN, RED, DOJI
}

enum class MarketTrend {
    STRONGLY_UP,
    UP,
    NEUTRAL,
    DOWN,
    STRONGLY_DOWN
}

data class TradeSignal(
    val direction: TradeDirection,
    val confidence: Float, // 0-100
    val pattern: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TradeDirection {
    BUY, SELL, HOLD
}

data class TradeResult(
    val id: String = java.util.UUID.randomUUID().toString(),
    val direction: TradeDirection,
    val entryPrice: Double,
    var exitPrice: Double = 0.0,
    val amount: Int,
    var status: TradeStatus = TradeStatus.PENDING,
    var profit: Double = 0.0,
    var profitPercent: Double = 0.0,
    val pattern: String = "",
    val confidence: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    var closedAt: Long = 0L
)

enum class TradeStatus {
    PENDING, EXECUTED, WON, LOST, CANCELLED
}
