package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppPrefs
import com.example.service.LiveUpdatesService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = AppPrefs(context)
            if (prefs.isTimerRunning) {
                LiveUpdatesService.start(
                    context = context,
                    label = prefs.timerLabel,
                    startWall = prefs.timerStartWall,
                    tab = prefs.timerTab
                )
            }
        }
    }
}
