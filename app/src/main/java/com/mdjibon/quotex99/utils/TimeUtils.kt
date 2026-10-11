package com.mdjibon.quotex99.utils

object TimeUtils {
    fun isValidTradingWindow(timeRemainingMs: Long): Boolean {
        return timeRemainingMs in 1..5000L
    }
}
