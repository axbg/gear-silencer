package com.axbg.gearsilencer

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class GearSilencer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gear_silencer)

        val btnStartService = findViewById<Button>(R.id.btn_start)
        val btnStopService = findViewById<Button>(R.id.btn_stop)

        btnStartService.setOnClickListener {
            startService()
        }

        btnStopService.setOnClickListener {
            stopService()
        }

        checkSilentPermission()
    }

    private fun checkSilentPermission() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            askForSilentPermission()
        }
    }

    private fun askForSilentPermission() {
        val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivityForResult(intent, 0)
    }

    private fun startService() {
        val serviceIntent = Intent(this, GearSilencerService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopService() {
        val serviceIntent = Intent(this, GearSilencerService::class.java)
        stopService(serviceIntent)
    }
}
