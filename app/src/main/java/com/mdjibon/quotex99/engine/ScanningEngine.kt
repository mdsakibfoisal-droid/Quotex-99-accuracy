package com.mdjibon.quotex99.engine

class ScanningEngine {
    fun shouldTradeNow(timeRemainingMs: Long): Boolean {
        return timeRemainingMs in 1..5000L
    }
}
