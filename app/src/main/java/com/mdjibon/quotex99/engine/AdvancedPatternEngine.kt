package com.mdjibon.quotex99.engine

import com.mdjibon.quotex99.data.*
import kotlin.math.abs

class AdvancedPatternEngine {

    private val patternHistory = mutableListOf<CandleData>()
    private val signalHistory = mutableListOf<TradeSignal>()
    private val priceHistory = mutableListOf<Double>()

    fun analyzeAndGenerateSignal(uiState: QuotexUiState): TradeSignal? {
        if (uiState.currentCandle == null) return null

        patternHistory.add(uiState.currentCandle)
        priceHistory.add(uiState.currentPrice)

        // Keep history limited to last 100 candles
        if (patternHistory.size > 100) patternHistory.removeAt(0)
        if (priceHistory.size > 100) priceHistory.removeAt(0)

        // Analyze multiple patterns
        val engulfingSignal = detectEngulfingPattern(uiState)
        val pinBarSignal = detectPinBarPattern(uiState)
        val trendFollowingSignal = detectTrendFollowing(uiState)
        val momentumSignal = detectMomentum(uiState)
        val supportResistanceSignal = detectSupportResistance(uiState)

        // Combine all signals
        val allSignals = listOfNotNull(
            engulfingSignal,
            pinBarSignal,
            trendFollowingSignal,
            momentumSignal,
            supportResistanceSignal
        )

        if (allSignals.isEmpty()) return null

        // Score and select best signal
        val bestSignal = allSignals.maxByOrNull { it.confidence } ?: return null
        
        // Only trade if confidence is above threshold
        if (bestSignal.confidence >= 60f) {
            signalHistory.add(bestSignal)
            return bestSignal
        }

        return null
    }

    private fun detectEngulfingPattern(uiState: QuotexUiState): TradeSignal? {
        if (patternHistory.size < 2) return null

        val current = uiState.currentCandle ?: return null
        val previous = patternHistory[patternHistory.size - 2]

        // Bullish engulfing: current candle (green) completely contains previous (red)
        if (current.isGreen && previous.isRed &&
            current.open <= previous.close &&
            current.close >= previous.open &&
            current.body > previous.body * 0.8
        ) {
            return TradeSignal(
                direction = TradeDirection.BUY,
                confidence = calculateEngulfingConfidence(current, previous),
                pattern = "Bullish Engulfing",
                reason = "Current green candle fully engulfs previous red candle"
            )
        }

        // Bearish engulfing: current candle (red) completely contains previous (green)
        if (current.isRed && previous.isGreen &&
            current.open >= previous.close &&
            current.close <= previous.open &&
            current.body > previous.body * 0.8
        ) {
            return TradeSignal(
                direction = TradeDirection.SELL,
                confidence = calculateEngulfingConfidence(current, previous),
                pattern = "Bearish Engulfing",
                reason = "Current red candle fully engulfs previous green candle"
            )
        }

        return null
    }

    private fun calculateEngulfingConfidence(current: CandleData, previous: CandleData): Float {
        val bodyRatio = (current.body / maxOf(previous.body, 0.1)).toFloat()
        val sizeBonus = minOf((bodyRatio - 0.8f) * 50f, 25f)
        return minOf(75f + sizeBonus, 95f)
    }

    private fun detectPinBarPattern(uiState: QuotexUiState): TradeSignal? {
        if (patternHistory.size < 2) return null

        val current = uiState.currentCandle ?: return null
        val previous = patternHistory[patternHistory.size - 2]

        // Pin bar: small body with long wick on one side
        val bodyPercent = current.body / current.wick

        if (bodyPercent < 0.3 && current.wick > 0) {
            // Bullish pin bar: long lower wick, red candle
            if (current.isRed && (current.open - current.low) > current.wick * 0.6) {
                return TradeSignal(
                    direction = TradeDirection.BUY,
                    confidence = 72f,
                    pattern = "Bullish Pin Bar",
                    reason = "Long lower wick indicates rejected downward movement"
                )
            }

            // Bearish pin bar: long upper wick, green candle
            if (current.isGreen && (current.high - current.close) > current.wick * 0.6) {
                return TradeSignal(
                    direction = TradeDirection.SELL,
                    confidence = 72f,
                    pattern = "Bearish Pin Bar",
                    reason = "Long upper wick indicates rejected upward movement"
                )
            }
        }

        return null
    }

    private fun detectTrendFollowing(uiState: QuotexUiState): TradeSignal? {
        if (patternHistory.size < 5) return null

        // Count consecutive candles of same color
        var bullishCount = 0
        var bearishCount = 0

        for (i in (patternHistory.size - 5) until patternHistory.size) {
            if (patternHistory[i].isGreen) bullishCount++ else bearishCount++
        }

        val confidence = minOf(50f + (bullishCount * 8), 88f)

        return when {
            bullishCount >= 3 -> TradeSignal(
                direction = TradeDirection.BUY,
                confidence = confidence,
                pattern = "Trend Following (Bullish)",
                reason = "$bullishCount consecutive green candles"
            )
            bearishCount >= 3 -> TradeSignal(
                direction = TradeDirection.SELL,
                confidence = confidence,
                pattern = "Trend Following (Bearish)",
                reason = "$bearishCount consecutive red candles"
            )
            else -> null
        }
    }

    private fun detectMomentum(uiState: QuotexUiState): TradeSignal? {
        if (priceHistory.size < 3) return null

        val current = priceHistory.last()
        val previous = priceHistory[priceHistory.size - 2]
        val older = priceHistory[priceHistory.size - 3]

        // Price feeds may contain zero or invalid values; avoid division by zero
        // and suppress signals until valid positive prices are available.
        if (!current.isFinite() || !previous.isFinite() || !older.isFinite() ||
            current <= 0.0 || previous <= 0.0 || older <= 0.0) return null

        val currentChange = ((current - previous) / previous) * 100
        val previousChange = ((previous - older) / older) * 100

        // Accelerating bullish momentum
        if (currentChange > 0 && previousChange > 0 && currentChange > previousChange) {
            return TradeSignal(
                direction = TradeDirection.BUY,
                confidence = 65f,
                pattern = "Accelerating Bullish Momentum",
                reason = "Price acceleration detected in upward direction"
            )
        }

        // Accelerating bearish momentum
        if (currentChange < 0 && previousChange < 0 && currentChange < previousChange) {
            return TradeSignal(
                direction = TradeDirection.SELL,
                confidence = 65f,
                pattern = "Accelerating Bearish Momentum",
                reason = "Price acceleration detected in downward direction"
            )
        }

        return null
    }

    private fun detectSupportResistance(uiState: QuotexUiState): TradeSignal? {
        if (priceHistory.size < 10) return null

        val current = uiState.currentPrice
        if (!current.isFinite() || current <= 0.0) return null
        val candle = uiState.currentCandle ?: return null

        // Find support (local minimum) and resistance (local maximum)
        val support = priceHistory.minOrNull() ?: return null
        val resistance = priceHistory.maxOrNull() ?: return null

        val supportDistance = abs(current - support) / current * 100
        val resistanceDistance = abs(resistance - current) / current * 100

        // Price bouncing from support
        if (supportDistance < 0.5 && candle.isGreen) {
            return TradeSignal(
                direction = TradeDirection.BUY,
                confidence = 68f,
                pattern = "Support Bounce",
                reason = "Price bounced from support level"
            )
        }

        // Price rejected from resistance
        if (resistanceDistance < 0.5 && candle.isRed) {
            return TradeSignal(
                direction = TradeDirection.SELL,
                confidence = 68f,
                pattern = "Resistance Rejection",
                reason = "Price rejected from resistance level"
            )
        }

        return null
    }

    fun getWinRate(): Float {
        if (signalHistory.isEmpty()) return 0f
        
        val wins = signalHistory.count { it.confidence > 70 }
        return (wins.toFloat() / signalHistory.size) * 100
    }

    fun getAverageConfidence(): Float {
        if (signalHistory.isEmpty()) return 0f
        return signalHistory.map { it.confidence }.average().toFloat()
    }

    fun reset() {
        patternHistory.clear()
        signalHistory.clear()
        priceHistory.clear()
    }
}
