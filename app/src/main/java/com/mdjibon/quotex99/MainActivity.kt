package com.mdjibon.quotex99

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mdjibon.quotex99.accessibility.QuotexAccessibilityService
import com.mdjibon.quotex99.database.Trade
import com.mdjibon.quotex99.database.TradeDatabase
import com.mdjibon.quotex99.databinding.ActivityMainBinding
import com.mdjibon.quotex99.service.FloatingIconService
import com.mdjibon.quotex99.utils.Constants
import com.mdjibon.quotex99.utils.PermissionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: TradeDatabase
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = TradeDatabase.getInstance(this)

        requestPermissions()
        setupUI()
        updateStats()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>(
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.FOREGROUND_SERVICE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSION_REQUEST_CODE)
        }
    }

    private fun setupUI() {
        binding.startButton.setOnClickListener {
            if (PermissionManager.hasOverlayPermission(this)) {
                startFloatingService()
                binding.statusText.text = "✅ Scanning Active"
                binding.startButton.isEnabled = false
                binding.stopButton.isEnabled = true
                isServiceRunning = true
            } else {
                PermissionManager.requestOverlayPermission(this)
                Toast.makeText(this, "Please enable overlay permission", Toast.LENGTH_SHORT).show()
            }
        }

        binding.stopButton.setOnClickListener {
            stopFloatingService()
            binding.statusText.text = "⏹️ Scanning Stopped"
            binding.startButton.isEnabled = true
            binding.stopButton.isEnabled = false
            isServiceRunning = false
        }

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.historyButton.setOnClickListener {
            startActivity(Intent(this, TradeHistoryActivity::class.java))
        }

        binding.stopButton.isEnabled = false
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingIconService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFloatingService() {
        stopService(Intent(this, FloatingIconService::class.java))
    }

    private fun updateStats() {
        lifecycleScope.launch {
            val totalCount = database.tradeDao().getTotalCount()
            val winCount = database.tradeDao().getWinCount()
            val winRate = if (totalCount > 0) (winCount * 100f / totalCount) else 0f
            val lastTrades = database.tradeDao().getLastTrades()

            binding.tradeCount.text = totalCount.toString()
            binding.winRate.text = "%2.1f%%".format(winRate)

            if (lastTrades.isNotEmpty()) {
                val last = lastTrades[0]
                binding.lastTradeText.text = "${last.action} on ${last.market} - ${last.result}"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStats()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
