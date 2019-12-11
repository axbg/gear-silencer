package com.axbg.gearsilencer

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_gear_silencer.*

class GearSilencer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gear_silencer)

        val btnStartService = findViewById<Button>(R.id.btn_start)
        val btnStopService = findViewById<Button>(R.id.btn_stop)
        val wifiSwitch = findViewById<Switch>(R.id.wifiSwitch)

        btnStartService.setOnClickListener {
            startService()
        }

        btnStopService.setOnClickListener {
            stopService()
        }

        wifiSwitch.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b) {
                writePreferences(true)
            } else {
                writePreferences(false)
            }
        }

        loadPreferences()
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

    private fun loadPreferences() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        wifiSwitch.isChecked = sharedPref.getBoolean(SWITCH_WIFI_PREFERENCE, false)
    }

    private fun writePreferences(switchWifi: Boolean) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean(SWITCH_WIFI_PREFERENCE, switchWifi)
            commit()
        }
    }

    private fun startService() {
        val serviceIntent = Intent(this, GearSilencerService::class.java)
        serviceIntent.putExtra(SWITCH_WIFI_EXTRA, wifiSwitch.isChecked)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopService() {
        val serviceIntent = Intent(this, GearSilencerService::class.java)
        stopService(serviceIntent)
    }

}
