package com.mdjibon.quotex99.engine

import android.content.Context
import android.graphics.Bitmap
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Display
import android.view.WindowManager
import kotlin.math.abs

class RealTimeScreenAnalyzer(private val context: Context, private val mediaProjection: MediaProjection?) {

    private val TAG = "ScreenAnalyzer"
    private val handler = Handler(HandlerThread("ScreenCapture").apply { start() }.looper)

    fun captureAndAnalyze(callback: (List<CandleInfo>?) -> Unit) {
        handler.post {
            try {
                val bitmap = captureScreen() ?: run {
                    callback(null)
                    return@post
                }
                
                val candles = analyzeChartImage(bitmap)
                callback(candles)
                bitmap.recycle()
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                callback(null)
            }
        }
    }

    private fun captureScreen(): Bitmap? {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val size = android.util.DisplayMetrics()
            display.getRealMetrics(size)
            
            // In production, use VirtualDisplay with MediaProjection
            // For now, return null - will be handled by accessibility
            null
        } catch (e: Exception) {
            Log.e(TAG, "Capture failed: ${e.message}")
            null
        }
    }

    private fun analyzeChartImage(bitmap: Bitmap): List<CandleInfo>? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val chartLeft = (width * 0.3).toInt()
            val chartRight = width
            val chartTop = (height * 0.15).toInt()
            val chartBottom = (height * 0.85).toInt()

            val candles = mutableListOf<CandleInfo>()
            
            // Extract last 2 candles from right side of chart
            val candleWidth = ((chartRight - chartLeft) / 2)
            
            for (candleIndex in 0..1) {
                val candleLeft = chartLeft + (candleIndex * candleWidth)
                val candleRight = candleLeft + candleWidth
                
                val candle = extractCandleInfo(
                    bitmap, 
                    candleLeft, candleRight, chartTop, chartBottom, candleIndex
                )
                candle?.let { candles.add(it) }
            }

            if (candles.size >= 2) candles else null
        } catch (e: Exception) {
            Log.e(TAG, "Analysis error: ${e.message}")
            null
        }
    }

    private fun extractCandleInfo(
        bitmap: Bitmap,
        left: Int, right: Int, top: Int, bottom: Int,
        index: Int
    ): CandleInfo? {
        return try {
            val color = detectCandleColor(bitmap, left, right, top, bottom)
            val bodySize = estimateBodySize(bitmap, left, right, top, bottom)
            val upperWick = estimateWickSize(bitmap, left, right, top, (top + bottom) / 2, true)
            val lowerWick = estimateWickSize(bitmap, left, right, (top + bottom) / 2, bottom, false)

            CandleInfo(
                index = index + 1,
                color = color,
                bodySize = bodySize,
                upperWick = upperWick,
                lowerWick = lowerWick,
                market = "EURUSD" // Will be detected separately
            )
        } catch (e: Exception) {
            Log.e(TAG, "Candle extraction error: ${e.message}")
            null
        }
    }

    private fun detectCandleColor(bitmap: Bitmap, left: Int, right: Int, top: Int, bottom: Int): String {
        var greenCount = 0
        var redCount = 0
        val step = 5 // Sample every 5th pixel for performance

        for (x in left..right step step) {
            for (y in top..bottom step step) {
                if (x < bitmap.width && y < bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)
                    val r = (pixel shr 16) and 0xff
                    val g = (pixel shr 8) and 0xff
                    val b = pixel and 0xff

                    // Green detection (high G, low R)
                    if (g > 100 && r < 100 && b < 100) {
                        greenCount++
                    }
                    // Red detection (high R, low G)
                    if (r > 100 && g < 100 && b < 100) {
                        redCount++
                    }
                }
            }
        }

        return if (greenCount > redCount) "GREEN" else "RED"
    }

    private fun estimateBodySize(bitmap: Bitmap, left: Int, right: Int, top: Int, bottom: Int): String {
        val height = bottom - top
        return when {
            height < 30 -> "SMALL"
            height < 60 -> "MEDIUM"
            else -> "LARGE"
        }
    }

    private fun estimateWickSize(bitmap: Bitmap, left: Int, right: Int, top: Int, bottom: Int, isUpper: Boolean): String {
        val height = abs(bottom - top)
        return when {
            height < 15 -> "SMALL"
            height < 30 -> "MEDIUM"
            else -> "LARGE"
        }
    }
}
