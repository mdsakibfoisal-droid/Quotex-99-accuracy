package com.mdjibon.quotex99.engine

import com.mdjibon.quotex99.model.PatternMatchResult
import com.mdjibon.quotex99.model.PatternRecord

class PatternMatcher {

    fun matchPattern(current: PatternSignature, reference: PatternRecord): Float {
        var score = 0f

        if (current.candle1.color.equals(reference.candle1Color, ignoreCase = true)) score += 40f
        if (current.candle2.color.equals(reference.candle2Color, ignoreCase = true)) score += 40f
        if (current.candle1.bodySize.equals(reference.bodySize, ignoreCase = true)) score += 10f
        if (current.candle2.bodySize.equals(reference.bodySize, ignoreCase = true)) score += 10f

        return score.coerceIn(0f, 100f)
    }

    fun findBestMatch(current: PatternSignature, patternList: List<PatternRecord>): PatternMatchResult? {
        var best: PatternMatchResult? = null
        var highest = 0f

        for (pattern in patternList) {
            val score = matchPattern(current, pattern)
            if (score >= 90f && score > highest) {
                highest = score
                best = PatternMatchResult(pattern, score)
            }
        }

        return best
    }
}
