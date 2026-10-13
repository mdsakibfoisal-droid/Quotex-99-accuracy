package com.mdjibon.quotex99.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.util.Log

/** Captures actual display frames after the user grants Android MediaProjection consent. */
class ScreenCaptureEngine(private val context: Context) {
    private val tag = "ScreenCapture"
    private var projection: MediaProjection? = null
    private var worker: HandlerThread? = null
    private var reader: ImageReader? = null
    private var display: VirtualDisplay? = null
    private var projectionCallback: MediaProjection.Callback? = null

    @Synchronized
    fun initialize(mediaProjection: MediaProjection) {
        releaseResources(stopProjection = false)
        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels.coerceAtLeast(1)
        val height = metrics.heightPixels.coerceAtLeast(1)
        val density = metrics.densityDpi.coerceAtLeast(1)
        val thread = HandlerThread("QuotexScreenCapture").apply { start() }
        worker = thread
        val handler = Handler(thread.looper)
        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 3)
        reader = imageReader
        projection = mediaProjection
        val callback = object : MediaProjection.Callback() {
            override fun onStop() {
                Log.i(tag, "MediaProjection stopped by system or user")
                synchronized(this@ScreenCaptureEngine) { releaseResources(stopProjection = false) }
            }
        }
        projectionCallback = callback
        mediaProjection.registerCallback(callback, handler)
        display = mediaProjection.createVirtualDisplay(
            "Quotex99ScreenCapture", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, handler
        ) ?: run {
            releaseResources(stopProjection = false)
            throw IllegalStateException("Could not create screen-capture virtual display")
        }
        Log.i(tag, "Capture initialized: ${width}x${height}")
    }

    /** Returns a caller-owned bitmap for the newest available frame, or null if none is ready. */
    @Synchronized
    fun captureFrame(): Bitmap? {
        val source = reader ?: return null
        if (projection == null || display == null) return null
        var image: Image? = null
        var paddedBitmap: Bitmap? = null
        return try {
            image = source.acquireLatestImage() ?: return null
            val plane = image.planes.firstOrNull() ?: return null
            if (plane.pixelStride <= 0 || plane.rowStride <= 0) return null
            val paddedWidth = plane.rowStride / plane.pixelStride
            paddedBitmap = Bitmap.createBitmap(paddedWidth, image.height, Bitmap.Config.ARGB_8888)
            plane.buffer.rewind()
            paddedBitmap.copyPixelsFromBuffer(plane.buffer)
            if (paddedWidth == image.width) {
                paddedBitmap.also { paddedBitmap = null }
            } else {
                Bitmap.createBitmap(paddedBitmap, 0, 0, image.width, image.height)
            }
        } catch (e: Exception) {
            Log.e(tag, "Screen frame capture failed", e)
            null
        } finally {
            paddedBitmap?.let { if (!it.isRecycled) it.recycle() }
            image?.close()
        }
    }

    @Synchronized
    fun release() = releaseResources(stopProjection = true)

    private fun releaseResources(stopProjection: Boolean) {
        val currentProjection = projection
        val callback = projectionCallback
        projection = null
        projectionCallback = null
        if (currentProjection != null && callback != null) {
            runCatching { currentProjection.unregisterCallback(callback) }
        }
        runCatching { display?.release() }
        display = null
        runCatching { reader?.close() }
        reader = null
        runCatching { worker?.quitSafely() }
        worker = null
        if (stopProjection) runCatching { currentProjection?.stop() }
    }
}
