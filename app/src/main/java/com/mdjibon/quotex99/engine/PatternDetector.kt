package com.mdjibon.quotex99.engine

class PatternDetector {
    fun detectPattern(): PatternSignature {
        val analyzer = CandleAnalyzer()
        val candles = analyzer.detectCandles()
        return analyzer.createPatternSignature(candles)
    }
}
