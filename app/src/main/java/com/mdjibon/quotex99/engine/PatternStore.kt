package com.mdjibon.quotex99.engine

import com.mdjibon.quotex99.model.PatternRecord

/**
 * Pattern catalog intentionally starts empty.
 *
 * The available sketch screenshots have not yet been converted into validated,
 * machine-readable candle records. Do not manufacture BUY/SELL examples from
 * placeholder rows.
 */
class PatternStore {
    val patterns: List<PatternRecord> = emptyList()
}
