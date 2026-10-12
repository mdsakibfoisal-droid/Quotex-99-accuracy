package com.mdjibon.quotex99

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mdjibon.quotex99.database.Trade
import com.mdjibon.quotex99.database.TradeDatabase
import com.mdjibon.quotex99.databinding.ActivityTradeHistoryBinding
import kotlinx.coroutines.launch

class TradeHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTradeHistoryBinding
    private lateinit var database: TradeDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTradeHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = TradeDatabase.getInstance(this)

        lifecycleScope.launch {
            loadTradeHistory()
        }
    }

    private suspend fun loadTradeHistory() {
        val trades = database.tradeDao().getLastTrades()
        val totalCount = database.tradeDao().getTotalCount()
        val winCount = database.tradeDao().getWinCount()
        val winRate = if (totalCount > 0) (winCount * 100f / totalCount) else 0f

        val historyText = buildString {
            append("📊 TRADE HISTORY\n")
            append("===================\n")
            append("Total Trades: $totalCount\n")
            append("Wins: $winCount\n")
            append("Win Rate: ${"%2.1f".format(winRate)}%\n")
            append("===================\n\n")

            if (trades.isEmpty()) {
                append("No trades yet")
            } else {
                for (trade in trades) {
                    val time = java.text.SimpleDateFormat("HH:mm:ss").format(trade.timestamp)
                    append("[$time] ${
                        when (trade.action) {
                            "BUY" -> "📈"
                            "SELL" -> "📉"
                            else -> "●"
                        }
                    } ${trade.action} ${trade.market}\n")
                    append("    Amount: ${trade.amount} | Score: ${"%2.1f".format(trade.similarity)}%\n")
                    append("    Result: ${trade.result} | Status: ${trade.status}\n")
                    append("\n")
                }
            }
        }

        binding.historyText.text = historyText
    }
}
