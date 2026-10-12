package com.mdjibon.quotex99.utils

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.mdjibon.quotex99.data.*

class QuotexUiParser(private val context: Context) {

    fun parseQuotexScreen(rootNode: AccessibilityNodeInfo?): QuotexUiState? {
        if (rootNode == null) return null

        try {
            val currentCandle = extractCurrentCandle(rootNode)
            val previousCandle = extractPreviousCandle(rootNode)
            val currentPrice = extractCurrentPrice(rootNode)
            val balance = extractBalance(rootNode)
            val timeRemaining = extractTimeRemaining(rootNode)
            val assetName = extractAssetName(rootNode)
            val trend = determineMarketTrend(currentCandle, previousCandle)
            val isPriceMovingUp = currentPrice > (currentCandle?.open ?: currentPrice)
            val confidence = calculateConfidence(currentCandle, previousCandle, trend)

            if (currentCandle != null && currentPrice > 0 && balance > 0) {
                return QuotexUiState(
                    currentCandle = currentCandle,
                    previousCandle = previousCandle,
                    marketTrend = trend,
                    currentPrice = currentPrice,
                    accountBalance = balance,
                    timeRemaining = timeRemaining,
                    assetName = assetName,
                    isPriceMovingUp = isPriceMovingUp,
                    confidence = confidence
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun extractCurrentCandle(rootNode: AccessibilityNodeInfo): CandleData? {
        // Search for candle data in accessibility tree
        val candleInfo = searchNodeByText(rootNode, "Open|High|Low|Close")
        
        return try {
            var open = 0.0
            var high = 0.0
            var low = 0.0
            var close = 0.0

            // Parse values from accessibility tree
            val textContent = getAccessibilityTreeText(rootNode)
            
            // Extract using regex patterns
            open = extractNumberFromText(textContent, "Open[\\s:]*([0-9.]+)") ?: open
            high = extractNumberFromText(textContent, "High[\\s:]*([0-9.]+)") ?: high
            low = extractNumberFromText(textContent, "Low[\\s:]*([0-9.]+)") ?: low
            close = extractNumberFromText(textContent, "Close[\\s:]*([0-9.]+)") ?: close

            if (open > 0 && high > 0 && low > 0 && close > 0) {
                val color = if (close >= open) CandleColor.GREEN else CandleColor.RED
                CandleData(
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    color = color
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun extractPreviousCandle(rootNode: AccessibilityNodeInfo): CandleData? {
        // Similar logic but for previous candle
        // This would need to maintain state or extract from chart history
        return null
    }

    private fun extractCurrentPrice(rootNode: AccessibilityNodeInfo): Double {
        val textContent = getAccessibilityTreeText(rootNode)
        return extractNumberFromText(textContent, "Price[\\s:]*([0-9.]+)|([0-9.]+)\\s*USD") ?: 0.0
    }

    private fun extractBalance(rootNode: AccessibilityNodeInfo): Double {
        val textContent = getAccessibilityTreeText(rootNode)
        return extractNumberFromText(textContent, "Balance[\\s:]*\\$?([0-9.]+)|Баланс[\\s:]*([0-9.]+)") ?: 0.0
    }

    private fun extractTimeRemaining(rootNode: AccessibilityNodeInfo): Int {
        val textContent = getAccessibilityTreeText(rootNode)
        // Extract MM:SS format
        val regex = Regex("(\\d+):(\\d{2})")
        val match = regex.find(textContent)
        return if (match != null) {
            val minutes = match.groupValues[1].toIntOrNull() ?: 0
            val seconds = match.groupValues[2].toIntOrNull() ?: 0
            minutes * 60 + seconds
        } else {
            0
        }
    }

    private fun extractAssetName(rootNode: AccessibilityNodeInfo): String {
        val textContent = getAccessibilityTreeText(rootNode)
        // Look for common asset patterns
        val assetPatterns = listOf(
            "EUR/USD", "GBP/USD", "USD/JPY", "BTC/USD", "ETH/USD",
            "GOLD", "OIL", "USDJPY", "EURUSD"
        )
        
        for (pattern in assetPatterns) {
            if (textContent.contains(pattern, ignoreCase = true)) {
                return pattern
            }
        }
        return "UNKNOWN"
    }

    private fun determineMarketTrend(
        currentCandle: CandleData?,
        previousCandle: CandleData?
    ): MarketTrend {
        if (currentCandle == null) return MarketTrend.NEUTRAL

        // Simple trend calculation based on candle color and size
        val bodyRatio = if (currentCandle.body > 0) {
            currentCandle.body / currentCandle.wick
        } else {
            0.0
        }

        return when {
            currentCandle.isGreen && bodyRatio > 0.7 -> MarketTrend.STRONGLY_UP
            currentCandle.isGreen -> MarketTrend.UP
            currentCandle.isRed && bodyRatio > 0.7 -> MarketTrend.STRONGLY_DOWN
            currentCandle.isRed -> MarketTrend.DOWN
            else -> MarketTrend.NEUTRAL
        }
    }

    private fun calculateConfidence(
        currentCandle: CandleData?,
        previousCandle: CandleData?,
        trend: MarketTrend
    ): Float {
        if (currentCandle == null) return 0f

        var confidence = 50f

        // Boost confidence based on candle strength
        val candleStrength = (currentCandle.body / currentCandle.wick).coerceIn(0.0, 1.0)
        confidence += (candleStrength * 20).toFloat()

        // Boost confidence based on trend continuation
        if (previousCandle != null) {
            val sameColor = currentCandle.color == previousCandle.color
            if (sameColor) confidence += 20f
        }

        // Trend strength indicator
        if (trend == MarketTrend.STRONGLY_UP || trend == MarketTrend.STRONGLY_DOWN) {
            confidence += 10f
        }

        return confidence.coerceIn(0f, 100f)
    }

    private fun searchNodeByText(node: AccessibilityNodeInfo?, searchText: String): AccessibilityNodeInfo? {
        if (node == null) return null

        val text = node.text?.toString() ?: ""
        if (text.contains(searchText, ignoreCase = true)) {
            return node
        }

        for (i in 0 until node.childCount) {
            val result = searchNodeByText(node.getChild(i), searchText)
            if (result != null) return result
        }

        return null
    }

    private fun getAccessibilityTreeText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""

        val sb = StringBuilder()
        
        if (!node.text.isNullOrEmpty()) {
            sb.append(node.text).append(" ")
        }

        if (!node.contentDescription.isNullOrEmpty()) {
            sb.append(node.contentDescription).append(" ")
        }

        // Recursively collect text from children (limited depth)
        for (i in 0 until minOf(node.childCount, 20)) {
            sb.append(getAccessibilityTreeText(node.getChild(i)))
        }

        return sb.toString()
    }

    private fun extractNumberFromText(text: String, pattern: String): Double? {
        try {
            val regex = Regex(pattern)
            val match = regex.find(text)
            return if (match != null && match.groupValues.size > 1) {
                match.groupValues[1].toDoubleOrNull() ?: match.groupValues[2].toDoubleOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }
}
