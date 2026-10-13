package com.mdjibon.quotex99.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.util.Log
import com.mdjibon.quotex99.engine.CandleAnalyzer

/**
 * Floating status control. It intentionally shows NO SIGNAL until real screen
 * capture and candle extraction return verified data.
 */
class FloatingIconService : Service() {
    private val tag = "FloatingIcon"
    private var windowManager: WindowManager? = null
    private var container: FrameLayout? = null
    private var signalView: TextView? = null
    private var statusView: TextView? = null
    private var scanning = false
    private var startX = 0
    private var startY = 0
    private var touchX = 0f
    private var touchY = 0f
    private val handler = Handler(Looper.getMainLooper())
    private val analyzer = CandleAnalyzer()
    private var scanTask: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createFloatingView()
    }

    private fun createFloatingView() {
        container = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = WindowManager.LayoutParams(
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
            setOnTouchListener { view, event ->
                val params = view.layoutParams as WindowManager.LayoutParams
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = params.x
                        startY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = startX + (event.rawX - touchX).toInt()
                        params.y = startY + (event.rawY - touchY).toInt()
                        windowManager?.updateViewLayout(view, params)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (kotlin.math.abs(event.rawX - touchX) < 10 &&
                            kotlin.math.abs(event.rawY - touchY) < 10) toggleScanning()
                        true
                    }
                    else -> false
                }
            }
        }

        signalView = TextView(this).apply {
            text = "Q99"
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(18, 14, 18, 14)
            setBackgroundColor(Color.rgb(30, 136, 229))
        }
        statusView = TextView(this).apply {
            text = "Ready"
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(8, 7, 8, 7)
            setBackgroundColor(Color.rgb(38, 50, 56))
        }
        container?.addView(signalView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP
        ))
        container?.addView(statusView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
        ))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (container?.parent == null) windowManager?.addView(container, container?.layoutParams)
        return START_STICKY
    }

    private fun toggleScanning() {
        scanning = !scanning
        if (scanning) {
            signalView?.text = "NO SIGNAL"
            signalView?.setBackgroundColor(Color.DKGRAY)
            statusView?.text = "Chart capture unavailable"
            startScanLoop()
        } else {
            scanTask?.let { handler.removeCallbacks(it) }
            signalView?.text = "Q99"
            signalView?.setBackgroundColor(Color.rgb(30, 136, 229))
            statusView?.text = "Stopped"
        }
    }

    private fun startScanLoop() {
        scanTask = object : Runnable {
            override fun run() {
                if (!scanning) return
                // CandleAnalyzer currently fails closed until live image extraction exists.
                val candles = analyzer.detectCandles()
                signalView?.text = if (candles.size >= 2) "NO SIGNAL" else "NO SIGNAL"
                statusView?.text = "No verified chart data"
                Log.d(tag, "Scan attempted; verified candles=${candles.size}; no trade automation")
                handler.postDelayed(this, 1000L)
            }
        }
        handler.post(scanTask!!)
    }

    override fun onDestroy() {
        scanning = false
        scanTask?.let { handler.removeCallbacks(it) }
        container?.let { view -> runCatching { windowManager?.removeViewImmediate(view) } }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}