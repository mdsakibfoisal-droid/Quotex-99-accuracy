package com.mdjibon.quotex99

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mdjibon.quotex99.databinding.ActivityTradeHistoryBinding

class TradeHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTradeHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTradeHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.historyText.text = "History is ready for local trade logging.\n\nThis screen will show executed BUY/SELL trades by timestamp and result."
    }
}
