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

class ScreenCaptureEngine(private val context: Context) {
    private val tag = "ScreenCapture"
    private var projection: MediaProjection? = null
    private var worker: HandlerThread? = null
    private var reader: ImageReader? = null
    private var display: VirtualDisplay? = null
    private var projectionCallback: MediaProjection.Callback? = null
    @Synchronized fun initialize(mediaProjection: MediaProjection) {
        releaseResources(false)
        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels.coerceAtLeast(1); val height = metrics.heightPixels.coerceAtLeast(1)
        val density = metrics.densityDpi.coerceAtLeast(1)
        val thread = HandlerThread("QuotexScreenCapture").apply { start() }; worker = thread
        val handler = Handler(thread.looper)
        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 3); reader = imageReader
        projection = mediaProjection
        val callback = object : MediaProjection.Callback() {
            override fun onStop() { Log.i(tag, "MediaProjection stopped"); synchronized(this@ScreenCaptureEngine) { releaseResources(false) } }
        }
        projectionCallback = callback
        mediaProjection.registerCallback(callback, handler)
        display = mediaProjection.createVirtualDisplay("Quotex99ScreenCapture", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.surface, null, handler)
            ?: run { releaseResources(false); throw IllegalStateException("Could not create screen-capture virtual display") }
    }
    @Synchronized fun isCapturing(): Boolean = projection != null && display != null && reader != null
    @Synchronized fun captureFrame(): Bitmap? {
        val source = reader ?: return null
        if (projection == null || display == null) return null
        var image: Image? = null
        var paddedBitmap: Bitmap? = null
        return try {
            image = source.acquireLatestImage() ?: return null
            val currentImage = image ?: return null
            val plane = currentImage.planes.firstOrNull() ?: return null
            if (plane.pixelStride <= 0 || plane.rowStride <= 0) return null
            val paddedWidth = plane.rowStride / plane.pixelStride
            val padded = Bitmap.createBitmap(paddedWidth, currentImage.height, Bitmap.Config.ARGB_8888)
            paddedBitmap = padded
            plane.buffer.rewind(); padded.copyPixelsFromBuffer(plane.buffer)
            if (paddedWidth == currentImage.width) { paddedBitmap = null; padded }
            else Bitmap.createBitmap(padded, 0, 0, currentImage.width, currentImage.height)
        } catch (e: Exception) { Log.e(tag, "Screen frame capture failed", e); null }
        finally {
            val toRecycle = paddedBitmap
            if (toRecycle != null && !toRecycle.isRecycled) toRecycle.recycle()
            image?.close()
        }
    }
    @Synchronized fun release() = releaseResources(true)
    private fun releaseResources(stopProjection: Boolean) {
        val currentProjection = projection; val callback = projectionCallback
        projection = null; projectionCallback = null
        if (currentProjection != null && callback != null) runCatching { currentProjection.unregisterCallback(callback) }
        runCatching { display?.release() }; display = null
        runCatching { reader?.close() }; reader = null
        runCatching { worker?.quitSafely() }; worker = null
        if (stopProjection) runCatching { currentProjection?.stop() }
    }
}