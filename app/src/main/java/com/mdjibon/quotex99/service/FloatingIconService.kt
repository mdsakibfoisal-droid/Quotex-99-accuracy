package com.mdjibon.quotex99.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class FloatingIconService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: TextView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatingView = TextView(this).apply {
            text = "Q99"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#1E88E5"))
            setPadding(18, 18, 18, 18)
            layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT
            )
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
    }

    override fun onDestroy() {
        floatingView?.let { view ->
            runCatching { windowManager?.removeViewImmediate(view) }
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
