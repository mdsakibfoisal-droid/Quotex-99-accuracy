package com.mdjibon.quotex99

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mdjibon.quotex99.data.TradeResult
import com.mdjibon.quotex99.data.TradeStatus
import java.text.SimpleDateFormat
import java.util.*

class TradeHistoryAdapter : ListAdapter<TradeResult, TradeHistoryAdapter.TradeViewHolder>(TradeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trade, parent, false)
        return TradeViewHolder(view as android.view.View)
    }

    override fun onBindViewHolder(holder: TradeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TradeViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val directionText: TextView = itemView.findViewById(R.id.directionText)
        private val patternText: TextView = itemView.findViewById(R.id.patternText)
        private val resultText: TextView = itemView.findViewById(R.id.resultText)
        private val profitText: TextView = itemView.findViewById(R.id.profitText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val confidenceText: TextView = itemView.findViewById(R.id.confidenceText)

        fun bind(trade: TradeResult) {
            directionText.text = "${trade.direction.name} - ${'$'}${trade.amount}"
            patternText.text = trade.pattern
            
            val statusEmoji = when (trade.status) {
                TradeStatus.WON -> "✅"
                TradeStatus.LOST -> "❌"
                TradeStatus.PENDING -> "⏳"
                TradeStatus.EXECUTED -> "▶️"
                TradeStatus.CANCELLED -> "⚠️"
            }
            
            resultText.text = "$statusEmoji ${trade.status.name}"
            resultText.setTextColor(
                when (trade.status) {
                    TradeStatus.WON -> android.graphics.Color.GREEN
                    TradeStatus.LOST -> android.graphics.Color.RED
                    else -> android.graphics.Color.GRAY
                }
            )

            val profitFormatted = String.format("%.2f", trade.profit)
            profitText.text = if (trade.profit > 0) "+$${profitFormatted}" else "-$${profitFormatted}"
            profitText.setTextColor(
                if (trade.profit > 0) android.graphics.Color.GREEN else android.graphics.Color.RED
            )

            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            timeText.text = dateFormat.format(Date(trade.createdAt))
            
            confidenceText.text = "${trade.confidence.toInt()}%"
        }
    }

    private class TradeDiffCallback : DiffUtil.ItemCallback<TradeResult>() {
        override fun areItemsTheSame(oldItem: TradeResult, newItem: TradeResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TradeResult, newItem: TradeResult): Boolean {
            return oldItem == newItem
        }
    }
}
