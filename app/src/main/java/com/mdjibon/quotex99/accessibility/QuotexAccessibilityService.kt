package com.mdjibon.quotex99.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class QuotexAccessibilityService : AccessibilityService() {

    private val TAG = "QuotexAccessibility"
    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            Log.d(TAG, "Window content changed")
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    fun executeTrade(action: String) {
        handler.post {
            val rootNode = rootInActiveWindow ?: return@post
            performTradeAction(rootNode, action)
        }
    }

    private fun performTradeAction(node: AccessibilityNodeInfo, action: String) {
        val buyNode = findNodeByText(node, "BUY")
        val sellNode = findNodeByText(node, "SELL")

        when (action.uppercase()) {
            "BUY" -> {
                buyNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "BUY clicked")
            }
            "SELL" -> {
                sellNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "SELL clicked")
            }
        }
    }

    private fun findNodeByText(
        node: AccessibilityNodeInfo,
        text: String
    ): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeByText(child, text)
            if (found != null) return found
        }
        return null
    }
}
