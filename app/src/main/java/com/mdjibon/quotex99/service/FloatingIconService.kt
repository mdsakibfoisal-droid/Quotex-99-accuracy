package com.mdjibon.quotex99.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.mdjibon.quotex99.engine.CandleAnalyzer
import com.mdjibon.quotex99.engine.ScreenCaptureEngine
import java.util.concurrent.Executors

/** User-approved screen capture and experimental candle-candidate display; never emits trade signals. */
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
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var scanTask: Runnable? = null
    private var captureEngine: ScreenCaptureEngine? = null
    private var projection: MediaProjection? = null
    private var analysisInProgress = false

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Chart scanning", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createFloatingView()
    }

    private fun enterForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Quotex chart scanning")
            .setContentText("Screen capture is active while scanning")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createFloatingView() {
        container = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP or Gravity.START; x = 60; y = 220 }
            setOnTouchListener { view, event ->
                val params = view.layoutParams as WindowManager.LayoutParams
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = params.x; startY = params.y; touchX = event.rawX; touchY = event.rawY; true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = startX + (event.rawX - touchX).toInt()
                        params.y = startY + (event.rawY - touchY).toInt()
                        windowManager?.updateViewLayout(view, params); true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (kotlin.math.abs(event.rawX - touchX) < 10 && kotlin.math.abs(event.rawY - touchY) < 10) toggleScanning()
                        true
                    }
                    else -> false
                }
            }
        }
        signalView = TextView(this).apply {
            text = "Q99"; textSize = 22f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            setPadding(18, 14, 18, 14); setBackgroundColor(Color.rgb(30, 136, 229))
        }
        statusView = TextView(this).apply {
            text = "Ready"; textSize = 12f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            setPadding(8, 7, 8, 7); setBackgroundColor(Color.rgb(38, 50, 56))
        }
        container?.addView(signalView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP))
        container?.addView(statusView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM))
    }

    @Suppress("DEPRECATION")
    private fun resultData(intent: Intent): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        else intent.getParcelableExtra(EXTRA_RESULT_DATA)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val data = intent?.let { resultData(it) }
        if (resultCode == 0 || data == null) {
            Log.e(tag, "Missing screen-capture consent result")
            stopSelf()
            return START_NOT_STICKY
        }
        try {
            enterForeground()
            val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projection?.let { runCatching { it.stop() } }
            projection = manager.getMediaProjection(resultCode, data)
            captureEngine = ScreenCaptureEngine(this).also { it.initialize(requireNotNull(projection)) }
        } catch (e: Exception) {
            Log.e(tag, "Screen capture initialization failed", e)
            stopSelf()
            return START_NOT_STICKY
        }
        if (container?.parent == null) windowManager?.addView(container, container?.layoutParams)
        statusView?.text = "Capture ready"
        return START_NOT_STICKY
    }

    private fun toggleScanning() {
        scanning = !scanning
        if (scanning) {
            signalView?.text = "SCANNING"
            signalView?.setBackgroundColor(Color.DKGRAY)
            statusView?.text = "Waiting for chart frame"
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
                // Never queue an unbounded backlog of full-screen bitmap analyses.
                if (!analysisInProgress) {
                    val frame = runCatching { captureEngine?.captureFrame() }.getOrNull()
                    if (frame == null) {
                        statusView?.text = "Waiting for chart frame"
                    } else {
                        analysisInProgress = true
                        try {
                            analysisExecutor.execute {
                                val count = try { analyzer.detectCandles(frame).size }
                                catch (e: Exception) { Log.e(tag, "Image analysis failed", e); 0 }
                                finally {
                                    if (!frame.isRecycled) frame.recycle()
                                    handler.post { analysisInProgress = false }
                                }
                                handler.post {
                                    if (scanning) {
                                        signalView?.text = "NO SIGNAL"
                                        signalView?.setBackgroundColor(Color.DKGRAY)
                                        statusView?.text = if (count >= 2) "Candidates: $count (unverified)" else "No verified candles"
                                    }
                                }
                            }
                        } catch (e: java.util.concurrent.RejectedExecutionException) {
                            if (!frame.isRecycled) frame.recycle()
                            analysisInProgress = false
                            Log.w(tag, "Frame skipped because analysis executor is shutting down")
                        }
                    }
                }
                handler.postDelayed(this, 1000L)
            }
        }
        handler.post(scanTask!!)
    }

    override fun onDestroy() {
        scanning = false
        scanTask?.let { handler.removeCallbacks(it) }
        runCatching { captureEngine?.release() }
        captureEngine = null
        projection = null
        analysisExecutor.shutdownNow()
        container?.let { runCatching { windowManager?.removeViewImmediate(it) } }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_RESULT_CODE = "screen_capture_result_code"
        const val EXTRA_RESULT_DATA = "screen_capture_result_data"
        private const val CHANNEL_ID = "quotex_chart_scanning"
        private const val NOTIFICATION_ID = 9901
    }
}
