package com.mdjibon.quotex99.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo

class QuotexAccessibilityService : AccessibilityService() {
    fun executeTrade(action: String) {
        val rootNode = rootInActiveWindow ?: return
        val target = findNodeByText(rootNode, action)
        target?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun findNodeByText(node: AccessibilityNodeInfo, action: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.equals(action, ignoreCase = true) == true) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val match = child?.let { findNodeByText(it, action) }
            if (match != null) return match
        }
        return null
    }
}
