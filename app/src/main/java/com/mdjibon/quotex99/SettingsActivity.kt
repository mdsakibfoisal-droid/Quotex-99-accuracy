package com.mdjibon.quotex99

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mdjibon.quotex99.database.TradeDatabase
import com.mdjibon.quotex99.databinding.ActivitySettingsBinding
import com.mdjibon.quotex99.utils.Constants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var database: TradeDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE)
        database = TradeDatabase.getInstance(this)

        // Load current settings
        binding.tradeAmountInput.setText(prefs.getInt(Constants.KEY_TRADE_AMOUNT, 10).toString())
        binding.scanIntervalInput.setText(prefs.getFloat(Constants.KEY_SCAN_INTERVAL, 1f).toString())
        binding.notificationToggle.isChecked = prefs.getBoolean(Constants.KEY_NOTIFICATIONS, true)
        binding.vibrationToggle.isChecked = prefs.getBoolean(Constants.KEY_VIBRATION, true)
        binding.autoTradeToggle.isChecked = prefs.getBoolean(Constants.KEY_AUTO_TRADE, true)

        // Save button
        binding.saveSettingsButton.setOnClickListener {
            saveSettings()
        }

        // Clear history button
        binding.clearHistoryButton?.setOnClickListener {
            clearTradeHistory()
        }
    }

    private fun saveSettings() {
        try {
            val tradeAmount = binding.tradeAmountInput.text.toString().toIntOrNull() ?: 10
            val scanInterval = binding.scanIntervalInput.text.toString().toFloatOrNull() ?: 1f

            if (tradeAmount < 1 || tradeAmount > 1000) {
                Toast.makeText(this, "Trade amount must be between 1-1000", Toast.LENGTH_SHORT).show()
                return
            }

            if (scanInterval < 0.5f || scanInterval > 5f) {
                Toast.makeText(this, "Scan interval must be between 0.5-5 seconds", Toast.LENGTH_SHORT).show()
                return
            }

            prefs.edit()
                .putInt(Constants.KEY_TRADE_AMOUNT, tradeAmount)
                .putFloat(Constants.KEY_SCAN_INTERVAL, scanInterval)
                .putBoolean(Constants.KEY_NOTIFICATIONS, binding.notificationToggle.isChecked)
                .putBoolean(Constants.KEY_VIBRATION, binding.vibrationToggle.isChecked)
                .putBoolean(Constants.KEY_AUTO_TRADE, binding.autoTradeToggle.isChecked)
                .apply()

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearTradeHistory() {
        GlobalScope.launch {
            database.tradeDao().clearAllTrades()
            runOnUiThread {
                Toast.makeText(this@SettingsActivity, "History cleared", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
