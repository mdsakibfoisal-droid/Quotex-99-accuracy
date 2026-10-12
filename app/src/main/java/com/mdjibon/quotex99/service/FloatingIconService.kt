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
import android.widget.TextView
import android.util.Log
import com.mdjibon.quotex99.engine.AdvancedPatternMatcher
import com.mdjibon.quotex99.engine.CandleAnalyzer
import com.mdjibon.quotex99.engine.TimingEngine
import com.mdjibon.quotex99.utils.Constants

class FloatingIconService : Service() {

    private val TAG = "FloatingIcon"
    private var windowManager: WindowManager? = null
    private var floatingView: TextView? = null
    private var isScanning = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private val handler = Handler(Looper.getMainLooper())
    private val timingEngine = TimingEngine()
    private val analyzer = CandleAnalyzer()
    private val matcher = AdvancedPatternMatcher()
    private var scanRunnable: Runnable? = null
    private var countdownRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createFloatingView()
    }

    private fun createFloatingView() {
        floatingView = TextView(this).apply {
            text = "Q99\n00:00"
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(16, 20, 16, 20)
            setBackgroundColor(Color.parseColor("#1E88E5"))
            setOnClickListener { toggleScanning() }
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
        startCountdownTimer()
        return START_STICKY
    }

    private fun showFloatingIcon() {
        if (floatingView?.parent == null) {
            windowManager?.addView(floatingView, floatingView?.layoutParams)
        }
    }

    private fun toggleScanning() {
        isScanning = !isScanning
        updateIconState()

        if (isScanning) {
            timingEngine.resetCandleTimer()
            startScanning()
        } else {
            stopScanning()
        }
    }

    private fun startScanning() {
        Log.d(TAG, "Scanning started")
        scanRunnable = object : Runnable {
            override fun run() {
                if (isScanning) {
                    // Get live candles from chart analysis
                    val liveCandles = analyzer.detectCandles()
                    
                    // Try to match with patterns
                    val matchResult = matcher.matchSequence(liveCandles)
                    
                    if (matchResult != null && matchResult.confidence >= 90f) {
                        Log.d(TAG, "Pattern matched: ${matchResult.patternId}")
                        
                        // Check if in valid trading window (last 5 seconds)
                        if (timingEngine.isValidTradingWindow()) {
                            executeTrade(matchResult.patternId)
                        } else {
                            Log.d(TAG, "Outside trading window")
                        }
                    }
                    
                    handler.postDelayed(this, Constants.DEFAULT_SCAN_INTERVAL.toLong() * 1000)
                }
            }
        }
        handler.post(scanRunnable!!)
    }

    private fun stopScanning() {
        Log.d(TAG, "Scanning stopped")
        scanRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun startCountdownTimer() {
        countdownRunnable = object : Runnable {
            override fun run() {
                val timeRemaining = timingEngine.getTimeRemaining()
                val seconds = (timeRemaining / 1000).toInt()
                val millis = (timeRemaining % 1000).toInt()
                
                val display = String.format("%02d:%02d", seconds, millis / 10)
                floatingView?.text = if (isScanning) "RUN\n$display" else "Q99\n$display"
                
                // Highlight in last 5 seconds
                if (timeRemaining <= 5000 && isScanning) {
                    floatingView?.setBackgroundColor(Color.parseColor("#FF6B6B"))
                } else if (isScanning) {
                    floatingView?.setBackgroundColor(Color.parseColor("#2ECC71"))
                } else {
                    floatingView?.setBackgroundColor(Color.parseColor("#1E88E5"))
                }
                
                // Reset timer when candle completes
                if (timeRemaining <= 0) {
                    timingEngine.resetCandleTimer()
                }
                
                handler.postDelayed(this, 100)
            }
        }
        handler.post(countdownRunnable!!)
    }

    private fun updateIconState() {
        floatingView?.apply {
            val color = if (isScanning) Color.parseColor("#2ECC71") else Color.parseColor("#1E88E5")
            setBackgroundColor(color)
            text = if (isScanning) "RUN\n00:00" else "Q99\n00:00"
        }
    }

    private fun executeTrade(patternId: String) {
        Log.d(TAG, "EXECUTING TRADE: Pattern $patternId")
        
        // Send to accessibility service to click BUY/SELL
        val tradeIntent = Intent(this, TradeExecutorService::class.java).apply {
            putExtra("pattern_id", patternId)
            putExtra("timestamp", System.currentTimeMillis())
        }
        startService(tradeIntent)
        
        // Show notification
        val notificationService = NotificationService(this)
        notificationService.showTradeNotification("Chart Pattern", "Pattern $patternId matched!", 95f)
        notificationService.vibratePhone()
    }

    override fun onDestroy() {
        countdownRunnable?.let { handler.removeCallbacks(it) }
        stopScanning()
        floatingView?.let { view ->
            runCatching { windowManager?.removeViewImmediate(view) }
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
