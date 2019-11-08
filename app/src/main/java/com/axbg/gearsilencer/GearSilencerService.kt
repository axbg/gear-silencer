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
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class GearSilencerService : Service() {

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

        detectBluetooth()
        startForeground(SERVICE_ID, notification)
        return START_NOT_STICKY
    }

    private fun detectBluetooth() {
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        this.registerReceiver(createBluetoothListener(), filter)
    }

    private fun createBluetoothListener(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent!!.action!!
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!
                val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

                when (action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        if (device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.WEARABLE) {
                            am.ringerMode = AudioManager.RINGER_MODE_SILENT
                        }
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        if (device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.WEARABLE) {
                            am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                        }
                    }
                }
            }
        }
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
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
    }

}