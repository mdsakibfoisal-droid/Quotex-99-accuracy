package com.mdjibon.quotex99.model

/**
 * Describes a catalog entry. These rows are placeholders, not validated trading signals.
 */
data class PatternRecord(
    val id: Int,
    val market: String,
    val action: String,
    val candle1Color: String,
    val candle2Color: String,
    val candle1Size: String,
    val candle2Size: String
)
