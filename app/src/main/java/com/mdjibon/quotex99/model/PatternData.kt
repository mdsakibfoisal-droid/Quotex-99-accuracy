package com.mdjibon.quotex99.model

import com.mdjibon.quotex99.model.PatternRecord

class PatternCatalog {
    val patterns: List<PatternRecord> = listOf(
        PatternRecord(1, "EURUSD", "BUY", "GREEN", "RED", "MEDIUM", "MEDIUM"),
        PatternRecord(2, "EURUSD", "SELL", "RED", "GREEN", "MEDIUM", "MEDIUM"),
        PatternRecord(3, "GBPUSD", "BUY", "GREEN", "RED", "SMALL", "SMALL"),
        PatternRecord(4, "GBPUSD", "SELL", "RED", "GREEN", "SMALL", "SMALL"),
        PatternRecord(5, "AUDUSD", "BUY", "GREEN", "RED", "MEDIUM", "SMALL"),
        PatternRecord(6, "AUDUSD", "SELL", "RED", "GREEN", "MEDIUM", "SMALL"),
        PatternRecord(7, "USDJPY", "BUY", "GREEN", "RED", "MEDIUM", "MEDIUM"),
        PatternRecord(8, "USDJPY", "SELL", "RED", "GREEN", "MEDIUM", "MEDIUM"),
        PatternRecord(9, "Gold", "BUY", "GREEN", "RED", "LARGE", "MEDIUM"),
        PatternRecord(10, "Gold", "SELL", "RED", "GREEN", "LARGE", "MEDIUM")
    )
}
