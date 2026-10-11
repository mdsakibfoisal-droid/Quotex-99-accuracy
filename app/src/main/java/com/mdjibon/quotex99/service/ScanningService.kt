package com.mdjibon.quotex99.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class FloatingIconService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: TextView? = null
    private var isScanning = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatingView = TextView(this).apply {
            text = "Q99"
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(18, 14, 18, 14)
            setBackgroundColor(Color.parseColor("#1E88E5"))
            setOnClickListener {
                toggleScanning()
            }
            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = (layoutParams as WindowManager.LayoutParams).x
                        initialY = (layoutParams as WindowManager.LayoutParams).y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val params = layoutParams as WindowManager.LayoutParams
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(this, params)
                    }
                }
                false
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 60
                y = 220
            }
            layoutParams = params
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showFloatingIcon()
        return START_STICKY
    }

    private fun showFloatingIcon() {
        if (floatingView?.parent == null) {
            windowManager?.addView(floatingView, floatingView?.layoutParams)
        }
        updateIconState()
    }

    private fun toggleScanning() {
        isScanning = !isScanning
        updateIconState()
        if (isScanning) {
            val scanIntent = Intent(this, ScanningService::class.java)
            startService(scanIntent)
        } else {
            stopService(Intent(this, ScanningService::class.java))
        }
    }

    private fun updateIconState() {
        floatingView?.apply {
            val color = if (isScanning) Color.parseColor("#2ECC71") else Color.parseColor("#1E88E5")
            setBackgroundColor(color)
            text = if (isScanning) "RUN" else "Q99"
        }
    }

    override fun onDestroy() {
        floatingView?.let { view ->
            runCatching { windowManager?.removeViewImmediate(view) }
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
