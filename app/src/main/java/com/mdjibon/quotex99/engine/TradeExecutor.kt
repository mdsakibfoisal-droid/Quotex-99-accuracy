package com.mdjibon.quotex99.engine

import android.content.Context
import com.mdjibon.quotex99.data.*
import com.mdjibon.quotex99.service.QuotexAccessibilityService
import com.mdjibon.quotex99.utils.Constants
import kotlinx.coroutines.*

class TradeExecutor(
    private val context: Context,
    private val accessibilityService: QuotexAccessibilityService
) {

    private val executionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var activeTradeJob: Job? = null
    private var executionListener: ((TradeResult) -> Unit)? = null
    private var tradeAmountListener: (() -> Int)? = null

    fun executeSignal(signal: TradeSignal, uiState: QuotexUiState): TradeResult? {
        // Cancel previous trade if still running
        activeTradeJob?.cancel()

        val tradeAmount = tradeAmountListener?.invoke() ?: Constants.DEFAULT_TRADE_AMOUNT
        val entryPrice = uiState.currentPrice

        val trade = TradeResult(
            direction = signal.direction,
            entryPrice = entryPrice,
            amount = tradeAmount,
            pattern = signal.pattern,
            confidence = signal.confidence,
            status = TradeStatus.PENDING
        )

        activeTradeJob = executionScope.launch {
            try {
                // Execute the trade based on direction
                val executed = when (signal.direction) {
                    TradeDirection.BUY -> executeBuyTrade(tradeAmount)
                    TradeDirection.SELL -> executeSellTrade(tradeAmount)
                    TradeDirection.HOLD -> {
                        trade.status = TradeStatus.CANCELLED
                        false
                    }
                }

                if (executed) {
                    // Actual settlement is not available yet; never infer a win/loss.
                    trade.status = TradeStatus.EXECUTED
                }

                executionListener?.invoke(trade)
            } catch (e: Exception) {
                e.printStackTrace()
                trade.status = TradeStatus.CANCELLED
                executionListener?.invoke(trade)
            }
        }

        return trade
    }

    private suspend fun executeBuyTrade(amount: Int): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                // Click on trade amount input
                accessibilityService.setText("trade_amount_input", amount.toString())
                delay(500)

                // Click BUY button
                val buyClicked = accessibilityService.performClick("buy_button")
                if (buyClicked) {
                    delay(1000)
                }
                
                buyClicked
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun executeSellTrade(amount: Int): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                // Click on trade amount input
                accessibilityService.setText("trade_amount_input", amount.toString())
                delay(500)

                // Click SELL button
                val sellClicked = accessibilityService.performClick("sell_button")
                if (sellClicked) {
                    delay(1000)
                }
                
                sellClicked
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun setExecutionListener(listener: (TradeResult) -> Unit) {
        executionListener = listener
    }

    fun setTradeAmountListener(listener: () -> Int) {
        tradeAmountListener = listener
    }

    fun cancelActiveTradeIfRunning(): Boolean {
        return if (activeTradeJob?.isActive == true) {
            activeTradeJob?.cancel()
            true
        } else {
            false
        }
    }

    fun cleanup() {
        activeTradeJob?.cancel()
        executionScope.cancel()
    }
}
