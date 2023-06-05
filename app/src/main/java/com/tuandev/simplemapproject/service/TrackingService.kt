package com.tuandev.simplemapproject.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.tuandev.simplemapproject.extension.log

class TrackingService : Service() {

    private val binder = TrackingServiceBinder()

    inner class TrackingServiceBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //start with Intent(this, LocalService::class.java).also { intent ->
        //    bindService(intent, connection, Context.BIND_AUTO_CREATE)
        //}
        log("TrackingService startCommand")
        return START_NOT_STICKY
    }

    override fun onCreate() {
        log("TrackingService create")
        super.onCreate()
    }

    override fun onDestroy() {
        log("TrackingService destroy")
        super.onDestroy()
    }
}