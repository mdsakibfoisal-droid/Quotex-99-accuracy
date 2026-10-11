package com.mdjibon.quotex99

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mdjibon.quotex99.databinding.ActivitySettingsBinding
import com.mdjibon.quotex99.utils.Constants

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE)

        binding.tradeAmountInput.setText(prefs.getInt(Constants.KEY_TRADE_AMOUNT, 10).toString())
        binding.scanIntervalInput.setText(prefs.getFloat(Constants.KEY_SCAN_INTERVAL, 1f).toString())
        binding.notificationToggle.isChecked = prefs.getBoolean(Constants.KEY_NOTIFICATIONS, true)
        binding.vibrationToggle.isChecked = prefs.getBoolean(Constants.KEY_VIBRATION, true)

        binding.saveSettingsButton.setOnClickListener {
            prefs.edit()
                .putInt(Constants.KEY_TRADE_AMOUNT, binding.tradeAmountInput.text.toString().toIntOrNull() ?: 10)
                .putFloat(Constants.KEY_SCAN_INTERVAL, binding.scanIntervalInput.text.toString().toFloatOrNull() ?: 1f)
                .putBoolean(Constants.KEY_NOTIFICATIONS, binding.notificationToggle.isChecked)
                .putBoolean(Constants.KEY_VIBRATION, binding.vibrationToggle.isChecked)
                .apply()

            finish()
        }
    }
}
