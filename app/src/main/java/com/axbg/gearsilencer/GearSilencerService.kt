package com.axbg.gearsilencer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat

class GearSilencerService : Service() {

    private val delay = 10000L

    private var am: AudioManager? = null
    private var wm: WifiManager? = null
    private var switchWifi = false
    private var recentlyDelayed = false
    private var listener: BroadcastReceiver? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, GearSilencer::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_gear_silencer)
            .setContentIntent(pendingIntent)
            .build()

        retrieveIntentExtra(intent!!)
        bindSystemControl()
        detectBluetooth()
        startForeground(SERVICE_ID, notification)
        return START_NOT_STICKY
    }

    private fun retrieveIntentExtra(intent: Intent) {
        switchWifi = intent.getBooleanExtra(SWITCH_WIFI_EXTRA, false)
    }

    private fun bindSystemControl() {
        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        wm = getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private fun detectBluetooth() {
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        this.registerReceiver(createBluetoothListener(), filter)
    }

    private fun createBluetoothListener(): BroadcastReceiver {
        listener = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent!!.action!!
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!

                when (action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        if (device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.WEARABLE
                            && !recentlyDelayed) {
                            am!!.ringerMode = AudioManager.RINGER_MODE_SILENT

                            if (switchWifi) {
                                wm!!.isWifiEnabled = false
                            }

                            recentlyDelayed = true
                            toggleOffRecentlyDelayed()
                        }
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        if (device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.WEARABLE
                            && !recentlyDelayed) {
                            am!!.ringerMode = AudioManager.RINGER_MODE_VIBRATE

                            if (switchWifi) {
                                wm!!.isWifiEnabled = true
                            }
                        }
                    }
                }
            }
        }

        return listener!!
    }

    private fun toggleOffRecentlyDelayed() {
        Handler().postDelayed({
                recentlyDelayed = false
            },
            delay
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChanel = NotificationChannel(
                CHANNEL_ID, "Service Silencer Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChanel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        am!!.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        this.unregisterReceiver(listener!!)
    }

}