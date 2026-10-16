package com.example

import android.app.Application
import com.example.data.AppPrefs
import com.example.notification.Notifier
import com.example.service.LiveUpdatesService

class YirenjuApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Uncaught exception handler fallback to prevent flash-crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val prefs = AppPrefs(this)
                prefs.lastCrash = throwable.stackTraceToString()
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // 2. Initialize notification channels
        Notifier.initChannels(this)

        // 3. Check ongoing live activity and ensure state persistence
        val prefs = AppPrefs(this)
        if (prefs.isTimerRunning) {
            LiveUpdatesService.start(
                context = this,
                label = prefs.timerLabel,
                startWall = prefs.timerStartWall,
                tab = prefs.timerTab
            )
        }
    }
}
