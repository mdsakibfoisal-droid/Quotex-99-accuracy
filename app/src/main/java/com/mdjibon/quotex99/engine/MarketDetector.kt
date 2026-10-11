package com.mdjibon.quotex99.engine

class MarketDetector {
    fun detectMarketName(rawMarket: String): String {
        val normalized = rawMarket.trim()
        return if (normalized.isNotEmpty()) normalized.uppercase() else "UNKNOWN"
    }
}
