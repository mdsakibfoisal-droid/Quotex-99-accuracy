package com.mdjibon.quotex99.engine

import android.content.Context
import com.mdjibon.quotex99.data.*
import com.mdjibon.quotex99.service.QuotexAccessibilityService
import com.mdjibon.quotex99.utils.Constants
import kotlinx.coroutines.*

class TradeExecutor(private val context: Context, private val accessibilityService: QuotexAccessibilityService) {
    private val executionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var activeTradeJob: Job? = null
    private var executionListener: ((TradeResult) -> Unit)? = null
    private var tradeAmountListener: (() -> Int)? = null

    fun executeSignal(signal: TradeSignal, uiState: QuotexUiState): TradeResult? {
        activeTradeJob?.cancel()
        val amount = tradeAmountListener?.invoke() ?: Constants.DEFAULT_TRADE_AMOUNT
        val trade = TradeResult(direction = signal.direction, entryPrice = uiState.currentPrice, amount = amount,
            pattern = signal.pattern, confidence = signal.confidence, status = TradeStatus.PENDING)
        activeTradeJob = executionScope.launch {
            try {
                val executed = when (signal.direction) {
                    TradeDirection.BUY -> executeBuyTrade(amount)
                    TradeDirection.SELL -> executeSellTrade(amount)
                    TradeDirection.HOLD -> false
                }
                executionListener?.invoke(trade.copy(status = if (executed) TradeStatus.EXECUTED else TradeStatus.CANCELLED))
            } catch (e: Exception) {
                e.printStackTrace()
                executionListener?.invoke(trade.copy(status = TradeStatus.CANCELLED))
            }
        }
        return trade
    }
    private suspend fun executeBuyTrade(amount: Int): Boolean = withContext(Dispatchers.Default) {
        try {
            accessibilityService.setText("trade_amount_input", amount.toString()); delay(500)
            val clicked = accessibilityService.performClick("buy_button"); if (clicked) delay(1000); clicked
        } catch (e: Exception) { e.printStackTrace(); false }
    }
    private suspend fun executeSellTrade(amount: Int): Boolean = withContext(Dispatchers.Default) {
        try {
            accessibilityService.setText("trade_amount_input", amount.toString()); delay(500)
            val clicked = accessibilityService.performClick("sell_button"); if (clicked) delay(1000); clicked
        } catch (e: Exception) { e.printStackTrace(); false }
    }
    fun setExecutionListener(listener: (TradeResult) -> Unit) { executionListener = listener }
    fun setTradeAmountListener(listener: () -> Int) { tradeAmountListener = listener }
    fun cancelActiveTradeIfRunning(): Boolean = if (activeTradeJob?.isActive == true) { activeTradeJob?.cancel(); true } else false
    fun cleanup() { activeTradeJob?.cancel(); executionScope.cancel() }
}