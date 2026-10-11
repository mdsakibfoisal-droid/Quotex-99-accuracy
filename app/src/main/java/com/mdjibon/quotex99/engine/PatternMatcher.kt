package com.mdjibon.quotex99.model

data class PatternRecord(
    val id: Int,
    val market: String,
    val action: String,
    val candle1Color: String,
    val candle2Color: String,
    val bodySize: String,
    val wickSize: String
)

data class PatternMatchResult(
    val pattern: PatternRecord,
    val score: Float
)
