package com.mdjibon.quotex99.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.mdjibon.quotex99.data.QuotexUiState
import com.mdjibon.quotex99.utils.QuotexUiParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class QuotexAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var uiParser: QuotexUiParser
    
    private var lastUiState: QuotexUiState? = null
    private var uiStateListener: ((QuotexUiState) -> Unit)? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        uiParser = QuotexUiParser(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Only process window content changed events for better performance
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            scope.launch {
                try {
                    val rootNode = rootInActiveWindow ?: return@launch
                    val uiState = uiParser.parseQuotexScreen(rootNode)
                    
                    if (uiState != null && uiState != lastUiState) {
                        lastUiState = uiState
                        uiStateListener?.invoke(uiState)
                    }
                    rootNode.recycle()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onInterrupt() {
        // Handle service interruption
    }

    fun setUiStateListener(listener: (QuotexUiState) -> Unit) {
        uiStateListener = listener
    }

    fun performClick(nodeId: String): Boolean {
        return try {
            val node = findNodeById(rootInActiveWindow, nodeId)
            node?.performAction(AccessibilityNodeInfo.ACTION_CLICK)?.also {
                node.recycle()
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun performLongClick(nodeId: String): Boolean {
        return try {
            val node = findNodeById(rootInActiveWindow, nodeId)
            node?.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)?.also {
                node.recycle()
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun setText(nodeId: String, text: String): Boolean {
        return try {
            val node = findNodeById(rootInActiveWindow, nodeId)
            if (node?.isEditable == true) {
                node.text = text
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                node.recycle()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun findNodeById(node: AccessibilityNodeInfo?, targetId: String): AccessibilityNodeInfo? {
        if (node == null) return null
        
        // Check if current node has viewIdResourceName matching
        if (node.viewIdResourceName?.contains(targetId) == true) {
            return AccessibilityNodeInfo.obtain(node)
        }

        // Search children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findNodeById(child, targetId)
            if (result != null) return result
        }

        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.launch {
            // Cleanup
        }
    }
}
