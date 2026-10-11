package com.mdjibon.quotex99.ui.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

class BlueOverlayView(context: Context) : View(context) {
    private val paint = Paint().apply {
        color = 0x6600A8FF.toInt()
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}
