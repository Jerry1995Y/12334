package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.example.data.AppPrefs
import com.example.notification.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LiveUpdatesService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var wakeLock: PowerManager.WakeLock? = null
    private var heartbeatJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Notifier.initChannels(this)
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Yirenju:LiveUpdatesWakeLock")?.apply {
                setReferenceCounted(false)
            }
        } catch (_: Exception) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = AppPrefs(this)
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_STOP -> {
                stopForegroundService()
                return START_NOT_STICKY
            }
            ACTION_START, ACTION_UPDATE -> {
                val label = intent?.getStringExtra(EXTRA_LABEL) ?: prefs.timerLabel
                val startWall = intent?.getLongExtra(EXTRA_START_WALL, prefs.timerStartWall) ?: prefs.timerStartWall
                val tab = intent?.getStringExtra(EXTRA_TAB) ?: prefs.timerTab

                val notification = Notifier.buildTimerNotification(this, label, startWall, tab)

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (Build.VERSION.SDK_INT >= 34) {
                            startForeground(
                                Notifier.ID_TIMER,
                                notification,
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                            )
                        } else {
                            startForeground(Notifier.ID_TIMER, notification)
                        }
                    } else {
                        startForeground(Notifier.ID_TIMER, notification)
                    }
                } catch (_: Exception) {
                    try {
                        startForeground(Notifier.ID_TIMER, notification)
                    } catch (_: Exception) {}
                }

                // Acquire wakelock during active timing to prevent ColorOS CPU sleep killing
                try {
                    if (wakeLock?.isHeld == false) {
                        wakeLock?.acquire(3 * 60 * 60 * 1000L) // 3 hours max
                    }
                } catch (_: Exception) {}

                startHeartbeat()
            }
        }

        return START_STICKY
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            val prefs = AppPrefs(this@LiveUpdatesService)
            while (isActive) {
                delay(30_000L) // 30s heartbeat to ensure data sync and notification integrity
                if (!prefs.isTimerRunning && !prefs.keepAliveEnabled) {
                    stopForegroundService()
                    break
                }
            }
        }
    }

    private fun stopForegroundService() {
        heartbeatJob?.cancel()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        heartbeatJob?.cancel()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.example.yirenju.action.START_LIVE"
        const val ACTION_UPDATE = "com.example.yirenju.action.UPDATE_LIVE"
        const val ACTION_STOP = "com.example.yirenju.action.STOP_LIVE"

        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_START_WALL = "extra_start_wall"
        const val EXTRA_TAB = "extra_tab"

        fun start(context: Context, label: String, startWall: Long, tab: String = "today") {
            val intent = Intent(context, LiveUpdatesService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_LABEL, label)
                putExtra(EXTRA_START_WALL, startWall)
                putExtra(EXTRA_TAB, tab)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {
                // Fallback to direct Notifier call if background start restriction triggers
                Notifier.showTimerNotification(context, label, startWall, tab)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LiveUpdatesService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
            Notifier.cancelTimerNotification(context)
        }
    }
}
