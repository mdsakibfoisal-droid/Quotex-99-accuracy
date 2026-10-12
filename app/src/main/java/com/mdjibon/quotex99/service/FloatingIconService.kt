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
import com.mdjibon.quotex99.engine.AdvancedPatternMatcher
import com.mdjibon.quotex99.engine.CandleAnalyzer
import com.mdjibon.quotex99.engine.PatternDatabase
import com.mdjibon.quotex99.engine.TimingEngine
import com.mdjibon.quotex99.utils.Constants

class FloatingIconService : Service() {

    private val TAG = "FloatingIcon"
    private var windowManager: WindowManager? = null
    private var floatingContainer: FrameLayout? = null
    private var signalIcon: TextView? = null
    private var statusText: TextView? = null
    private var isScanning = false
    private var currentSignal = "" // UP, DOWN, or empty
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
    private val patternDb = PatternDatabase

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createFloatingView()
    }

    private fun createFloatingView() {
        floatingContainer = FrameLayout(this).apply {
            // Main container params
            layoutParams = WindowManager.LayoutParams(
                180, 220,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 60
                y = 220
            }
            setBackgroundColor(Color.TRANSPARENT)
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
        }

        // Signal Icon (UP/DOWN/Icon)
        signalIcon = TextView(this).apply {
            text = "Q99"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(20, 15, 20, 15)
            setBackgroundColor(Color.parseColor("#1E88E5"))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP
            )
        }

        // Status/Countdown Text
        statusText = TextView(this).apply {
            text = "00:00"
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(10, 8, 10, 8)
            setBackgroundColor(Color.parseColor("#263238"))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
        }

        floatingContainer?.apply {
            addView(signalIcon)
            addView(statusText)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showFloatingIcon()
        startCountdownTimer()
        return START_STICKY
    }

    private fun showFloatingIcon() {
        if (floatingContainer?.parent == null) {
            windowManager?.addView(floatingContainer, floatingContainer?.layoutParams)
        }
    }

    private fun toggleScanning() {
        isScanning = !isScanning
        updateIconState()

        if (isScanning) {
            timingEngine.resetCandleTimer()
            currentSignal = ""
            startScanning()
            Log.d(TAG, "🟢 Scanning started")
        } else {
            stopScanning()
            Log.d(TAG, "🔴 Scanning stopped")
        }
    }

    private fun startScanning() {
        scanRunnable = object : Runnable {
            override fun run() {
                if (isScanning) {
                    try {
                        // Get live candles from chart
                        val liveCandles = analyzer.detectCandles()
                        
                        // Convert to pattern string format
                        val liveStringSequence = liveCandles.map { candle ->
                            val color = if (candle.color == "GREEN") "G" else "R"
                            val body = candle.bodySize.take(1)
                            val upper = candle.upperWick.take(1)
                            val lower = candle.lowerWick.take(1)
                            "$color-$body-$upper-$lower"
                        }

                        Log.d(TAG, "Live sequence: $liveStringSequence")
                        
                        // Try to match with database
                        val matchedPatternId = patternDb.findMatchingPattern(liveStringSequence)
                        
                        if (matchedPatternId != null) {
                            Log.d(TAG, "✅ Pattern matched: $matchedPatternId")
                            
                            // Determine signal (UP for BUY, DOWN for SELL)
                            val signal = if (matchedPatternId.toInt() % 2 == 1) "UP" else "DOWN"
                            currentSignal = signal
                            updateSignal(signal)
                            
                            // Check if in valid trading window
                            if (timingEngine.isValidTradingWindow()) {
                                Log.d(TAG, "🎯 Valid trading window - executing trade")
                                executeTrade(matchedPatternId, signal)
                            } else {
                                Log.d(TAG, "⏳ Outside trading window (${timingEngine.getTimeInCandle()}ms)")
                            }
                        }
                        
                        handler.postDelayed(this, (Constants.DEFAULT_SCAN_INTERVAL * 1000).toLong())
                    } catch (e: Exception) {
                        Log.e(TAG, "Scan error: ${e.message}")
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        }
        handler.post(scanRunnable!!)
    }

    private fun stopScanning() {
        scanRunnable?.let { handler.removeCallbacks(it) }
        currentSignal = ""
    }

    private fun startCountdownTimer() {
        countdownRunnable = object : Runnable {
            override fun run() {
                if (true) { // Always update timer
                    val timeRemaining = timingEngine.getTimeRemaining()
                    val seconds = (timeRemaining / 1000).toInt()
                    val millis = (timeRemaining % 1000).toInt()
                    
                    val display = String.format("%02d:%02d", seconds, millis / 10)
                    statusText?.text = display
                    
                    // Color coding for time windows
                    when {
                        timeRemaining <= 5000 && isScanning -> {
                            // Last 5 seconds - VALID TRADING WINDOW
                            statusText?.setBackgroundColor(Color.parseColor("#FF6B6B"))
                        }
                        timeRemaining <= 10000 && isScanning -> {
                            // Scanning phase
                            statusText?.setBackgroundColor(Color.parseColor("#FFA500"))
                        }
                        else -> {
                            statusText?.setBackgroundColor(Color.parseColor("#263238"))
                        }
                    }
                    
                    // Reset timer when candle completes
                    if (timeRemaining <= 0) {
                        timingEngine.resetCandleTimer()
                        currentSignal = ""
                        updateIconState()
                    }
                    
                    handler.postDelayed(this, 50)
                }
            }
        }
        handler.post(countdownRunnable!!)
    }

    private fun updateIconState() {
        signalIcon?.apply {
            when {
                !isScanning -> {
                    text = "Q99"
                    setBackgroundColor(Color.parseColor("#1E88E5"))
                }
                currentSignal == "UP" -> {
                    text = "🔼\nUP"
                    setBackgroundColor(Color.parseColor("#2ECC71"))
                }
                currentSignal == "DOWN" -> {
                    text = "🔽\nDOWN"
                    setBackgroundColor(Color.parseColor("#E74C3C"))
                }
                else -> {
                    text = "RUN"
                    setBackgroundColor(Color.parseColor("#3498DB"))
                }
            }
        }
    }

    private fun updateSignal(signal: String) {
        currentSignal = signal
        updateIconState()
        Log.d(TAG, "Signal updated: $signal")
    }

    private fun executeTrade(patternId: String, signal: String) {
        Log.d(TAG, "💰 TRADE EXECUTED: $signal on Pattern $patternId")
        
        // Log to database
        val tradeIntent = Intent(this, TradeExecutorService::class.java).apply {
            putExtra("pattern_id", patternId)
            putExtra("signal", signal)
            putExtra("action", if (signal == "UP") "BUY" else "SELL")
            putExtra("timestamp", System.currentTimeMillis())
        }
        startService(tradeIntent)
        
        // Notification
        val notificationService = com.mdjibon.quotex99.service.NotificationService(this)
        notificationService.showTradeNotification(
            "Chart",
            "$signal Trade - Pattern $patternId",
            95f
        )
        notificationService.vibratePhone()
    }

    override fun onDestroy() {
        countdownRunnable?.let { handler.removeCallbacks(it) }
        stopScanning()
        floatingContainer?.let { view ->
            runCatching { windowManager?.removeViewImmediate(view) }
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
