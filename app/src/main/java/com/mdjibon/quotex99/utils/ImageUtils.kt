package com.mdjibon.quotex99.utils

import android.graphics.Bitmap

object ImageUtils {
    fun bitmapToRgb(bitmap: Bitmap): Bitmap = bitmap
    fun isDarkColor(bitmap: Bitmap): Boolean = bitmap.width > 0 && bitmap.height > 0
}
