package com.mdjibon.quotex99

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mdjibon.quotex99.databinding.ActivityMainBinding
import com.mdjibon.quotex99.service.FloatingIconService
import com.mdjibon.quotex99.utils.PermissionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startButton.setOnClickListener {
            if (PermissionManager.hasOverlayPermission(this)) {
                startService(Intent(this, FloatingIconService::class.java))
                binding.statusText.text = "Scanning"
            } else {
                PermissionManager.requestOverlayPermission(this)
            }
        }

        binding.stopButton.setOnClickListener {
            stopService(Intent(this, FloatingIconService::class.java))
            binding.statusText.text = "Not Active"
        }

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.historyButton.setOnClickListener {
            startActivity(Intent(this, TradeHistoryActivity::class.java))
        }
    }
}
