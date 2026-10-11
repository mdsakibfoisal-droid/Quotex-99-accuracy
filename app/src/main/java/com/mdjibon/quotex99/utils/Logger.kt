package com.mdjibon.quotex99.utils

object Logger {
    fun log(tag: String, message: String) {
        android.util.Log.d(tag, message)
    }
}
