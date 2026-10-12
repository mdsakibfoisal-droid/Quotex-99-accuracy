package com.mdjibon.quotex99.ui.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class ScanningOverlayView(context: Context) : View(context) {
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#00A8FF")
    }

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = 0x2600A8FF.toInt()
    }

    private var progress = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw semi-transparent blue overlay
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fillPaint)
        
        // Draw progress indicator on sides
        val progressHeight = (height * progress / 100f)
        paint.color = Color.parseColor("#00A8FF")
        canvas.drawRect(0f, height - progressHeight, 5f, height.toFloat(), paint)
        canvas.drawRect(width - 5f, height - progressHeight, width.toFloat(), height.toFloat(), paint)
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 100f)
        invalidate()
    }
}
