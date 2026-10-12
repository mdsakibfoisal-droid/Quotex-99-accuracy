package com.mdjibon.quotex99.engine

import android.content.Context
import android.util.Log

class MarketDetector(private val context: Context) {

    private val TAG = "MarketDetector"

    private val supportedMarkets = listOf(
        "EURUSD", "GBPUSD", "AUDUSD", "GBPJPY", "USDJPY",
        "USDCAD", "NZDUSD", "EURJPY",
        "Gold", "Oil", "Silver", "NaturalGas",
        "Bitcoin", "Ethereum", "Litecoin", "Ripple",
        "SP500", "DAX", "FTSE100", "Nikkei", "OTC"
    )

    fun detectMarket(screenText: String): String {
        return try {
            for (market in supportedMarkets) {
                if (screenText.contains(market, ignoreCase = true)) {
                    Log.d(TAG, "Market detected: $market")
                    return market
                }
            }
            Log.d(TAG, "Market not detected, defaulting to UNKNOWN")
            "UNKNOWN"
        } catch (e: Exception) {
            Log.e(TAG, "Detection error: ${e.message}")
            "UNKNOWN"
        }
    }

    fun isValidMarket(market: String): Boolean {
        return market in supportedMarkets || market == "UNKNOWN"
    }
}
