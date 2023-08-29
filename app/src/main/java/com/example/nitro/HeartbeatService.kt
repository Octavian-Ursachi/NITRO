package com.example.nitro

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.util.Log

class HeartbeatService : Service() {

    private val HEARTBEAT_INTERVAL = 20L
    private lateinit var alarmManager: AlarmManager
    private lateinit var  pendingIntent: PendingIntent

    override fun onCreate() {
        super.onCreate()
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, HeartbeatReceiver::class.java) // Use HeartbeatReceiver
        intent.action = "HEARTBEAT_ACTION"
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        Log.v("HEARBEATSERVICE","HEARTBEATSERVICE")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Schedule the initial alarm and periodic repeating alarms
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            HEARTBEAT_INTERVAL, //miliseconds
            pendingIntent
        )
        return START_STICKY

    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}