package com.mdjibon.quotex99.engine

class PatternMatcher {
    fun matchPattern(current: PatternSignature, reference: PatternSignature): Float {
        val sameColor = current.candle1.color == reference.candle1.color
        val sameSecondColor = current.candle2.color == reference.candle2.color
        var score = 0f
        if (sameColor) score += 40f
        if (sameSecondColor) score += 40f
        if (current.trend == reference.trend) score += 20f
        return score.coerceIn(0f, 100f)
    }
}
