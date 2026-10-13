package com.mdjibon.quotex99.engine

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CandleAnalyzerInstrumentedTest {
    @Test
    fun emptyChartImageDoesNotInventCandidates() {
        val bitmap = Bitmap.createBitmap(240, 160, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.WHITE)
            assertTrue(CandleAnalyzer().detectCandles(bitmap).isEmpty())
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun unrelatedBlueAndGrayPixelsDoNotProduceCandidates() {
        val bitmap = Bitmap.createBitmap(240, 160, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.rgb(90, 110, 130))
            assertTrue(CandleAnalyzer().detectCandles(bitmap).isEmpty())
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun legacyNoFrameEntryPointReturnsNoCandidates() {
        assertTrue(CandleAnalyzer().detectCandles().isEmpty())
    }
}
