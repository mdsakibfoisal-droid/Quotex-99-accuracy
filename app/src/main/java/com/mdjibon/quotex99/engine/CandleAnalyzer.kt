package com.mdjibon.quotex99.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import kotlin.math.max

data class CandleInfo(
    val index: Int,
    val color: String,
    val bodySize: String,
    val upperWick: String,
    val lowerWick: String,
    val market: String = "EURUSD"
)

data class PatternSignature(
    val candle1: CandleInfo,
    val candle2: CandleInfo,
    val trend: String,
    val patterns: List<String>
)

/**
 * Experimental screenshot-based candle-candidate detector.
 * It does not yet calibrate chart bounds or validate trade signals.
 */
class CandleAnalyzer {
    private val tag = "CandleAnalyzer"

    /** Legacy service has no frame source yet; do not fabricate data. */
    fun detectCandles(): List<CandleInfo> {
        Log.w(tag, "No live chart frame supplied; no candles returned")
        return emptyList()
    }

    /**
     * Scans the central image area for narrow vertical groups of red/green
     * pixels. These are candidates only: chart controls and overlays can cause
     * false detections, so callers must not convert them directly to signals.
     */
    fun detectCandles(bitmap: Bitmap): List<CandleInfo> {
        if (bitmap.width < 20 || bitmap.height < 20) return emptyList()
        val left = bitmap.width / 10
        val right = bitmap.width * 9 / 10
        val top = bitmap.height / 10
        val bottom = bitmap.height * 9 / 10
        val scanHeight = bottom - top
        if (right <= left || scanHeight <= 0) return emptyList()

        // Sample rows to reduce CPU work on full-screen phone captures.
        val yStep = max(1, scanHeight / 240)
        val sampledRows = ((scanHeight - 1) / yStep) + 1
        val columns = BooleanArray(right - left)
        for (x in left until right) {
            var count = 0
            var y = top
            while (y < bottom) {
                if (isCandleColor(bitmap.getPixel(x, y))) count++
                y += yStep
            }
            columns[x - left] = count >= max(2, sampledRows / 180)
        }

        var i = 0
        while (i < columns.size) {
            if (columns[i]) { i++; continue }
            val gapStart = i
            while (i < columns.size && !columns[i]) i++
            if (gapStart > 0 && i < columns.size && i - gapStart <= 2 &&
                columns[gapStart - 1] && columns[i]) {
                for (j in gapStart until i) columns[j] = true
            }
        }

        val ranges = mutableListOf<IntRange>()
        i = 0
        while (i < columns.size) {
            if (!columns[i]) { i++; continue }
            val start = i
            while (i < columns.size && columns[i]) i++
            val end = i - 1
            if (end - start + 1 <= max(30, bitmap.width / 12)) ranges += start..end
        }

        val candidates = ranges.mapNotNull { range ->
            var red = 0
            var green = 0
            var minY = bottom
            var maxY = top
            for (offset in range) {
                val x = left + offset
                var y = top
                while (y < bottom) {
                    val pixel = bitmap.getPixel(x, y)
                    if (isCandleColor(pixel)) {
                        if (isRed(pixel)) red++ else green++
                        minY = minOf(minY, y)
                        maxY = maxOf(maxY, y)
                    }
                    y += yStep
                }
            }
            if (red + green < 3 || maxY < minY) return@mapNotNull null
            val ratio = (maxY - minY + 1).toFloat() / scanHeight
            val size = when { ratio < 0.025f -> "SMALL"; ratio < 0.07f -> "MEDIUM"; else -> "LARGE" }
            CandleInfo(0, if (green > red) "GREEN_CANDIDATE" else "RED_CANDIDATE", size, "UNKNOWN", "UNKNOWN")
        }
        val result = candidates.takeLast(120).mapIndexed { index, candle -> candle.copy(index = index) }
        Log.i(tag, "Found ${result.size} experimental candle-like groups")
        return result
    }

    fun createPatternSignature(candles: List<CandleInfo>): PatternSignature {
        require(candles.size >= 2) { "At least two detected candle candidates are required" }
        return PatternSignature(candles[candles.size - 2], candles[candles.size - 1], "UNKNOWN", emptyList())
    }

    fun analyzeChartImage(chartBytes: ByteArray?): List<CandleInfo>? {
        if (chartBytes.isNullOrEmpty()) {
            Log.w(tag, "No chart image supplied")
            return null
        }
        val bitmap = runCatching { BitmapFactory.decodeByteArray(chartBytes, 0, chartBytes.size) }.getOrNull()
        if (bitmap == null) {
            Log.w(tag, "Supplied bytes are not a decodable bitmap")
            return null
        }
        return try { detectCandles(bitmap) } catch (e: Exception) {
            Log.e(tag, "Experimental image analysis failed", e)
            null
        } finally { bitmap.recycle() }
    }

    private fun isCandleColor(pixel: Int): Boolean = isRed(pixel) || isGreen(pixel)
    private fun isRed(pixel: Int): Boolean {
        val r = Color.red(pixel); val g = Color.green(pixel); val b = Color.blue(pixel)
        return r > 100 && r > g * 1.25 && r > b * 1.15
    }
    private fun isGreen(pixel: Int): Boolean {
        val r = Color.red(pixel); val g = Color.green(pixel); val b = Color.blue(pixel)
        return g > 90 && g > r * 1.15 && g > b * 1.05
    }
}
