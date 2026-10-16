package com.example.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.HealthSummary
import java.lang.reflect.Method

object Notifier {
    const val CH_LIVE = "yirenju_live"
    const val CH_OVERVIEW = "yirenju_overview"
    const val CH_ALERT = "yirenju_alert"

    const val ID_TIMER = 1001
    const val ID_OVERVIEW = 2001
    const val ID_ALERT = 3001

    // Reflection for API 36+ Live Updates / Fluid Cloud promotion
    private val setPromotedMethod: Method? by lazy {
        try {
            Notification.Builder::class.java.getMethod("setRequestPromotedOngoing", java.lang.Boolean.TYPE)
        } catch (_: Throwable) {
            null
        }
    }

    fun initChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // 1. Live activity channel (Silent, stopwatch for ColorOS Fluid Cloud capsule)
        val chLive = NotificationChannel(
            CH_LIVE,
            "进行中活动（流体云）",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "通勤打卡、日常与专注计时，支持 ColorOS 流体云状态栏胶囊与锁屏显示"
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }

        // 2. Health overview channel (Silent persistent overview)
        val chOverview = NotificationChannel(
            CH_OVERVIEW,
            "今日健康概览",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "每日步数、睡眠与心率统计常驻概览"
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }

        // 3. Health alert channel (Normal importance with sound/vibrate)
        val chAlert = NotificationChannel(
            CH_ALERT,
            "健康提醒",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "步数达标提醒与昨夜少睡提醒"
            setShowBadge(true)
            enableVibration(true)
        }

        nm.createNotificationChannel(chLive)
        nm.createNotificationChannel(chOverview)
        nm.createNotificationChannel(chAlert)
    }

    fun hasPostPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun canPostPromoted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 36) return false
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        return try {
            nm.canPostPromotedNotifications()
        } catch (_: Throwable) {
            false
        }
    }

    fun isFluidCloudSupported(context: Context): Boolean {
        return setPromotedMethod != null || Build.VERSION.SDK_INT >= 35
    }

    fun getFluidCloudStatusText(context: Context): String {
        return when {
            !hasPostPermission(context) ->
                "需要先允许通知权限，才能进入状态栏胶囊"
            Build.VERSION.SDK_INT < 35 && setPromotedMethod == null ->
                "本机系统版本低于 Android 15/16，自动降级为普通常驻通知"
            Build.VERSION.SDK_INT >= 36 && !canPostPromoted(context) ->
                "系统未开启「提升通知」，流体云暂未接管（可通过系统通知设置开启）"
            else ->
                "已就绪：ColorOS 16+ 的流体云会自动接管胶囊"
        }
    }

    // L1: Publish or update Ongoing Live Activity notification
    fun buildTimerNotification(
        context: Context,
        label: String,
        startWall: Long,
        targetTab: String = "today"
    ): Notification {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", targetTab)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            ID_TIMER,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = R.drawable.ic_launcher_foreground

        // We use Notification.Builder to support setRequestPromotedOngoing directly
        val builder = Notification.Builder(context, CH_LIVE)
            .setContentTitle(label)
            .setContentText("进行中 · 点按回到一人居")
            .setSubText("ColorOS 实时活动")
            .setSmallIcon(iconRes)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_STOPWATCH)
            .setWhen(startWall)
            .setUsesChronometer(true)

        // ColorOS 16/17 fluid cloud live updates request
        try {
            // Flag 0x00080000 represents Notification.FLAG_PROMOTED_ONGOING
            builder.setFlag(0x00080000, true)
        } catch (_: Throwable) {}

        try {
            setPromotedMethod?.invoke(builder, true)
        } catch (_: Throwable) {}

        return builder.build()
    }

    fun showTimerNotification(
        context: Context,
        label: String,
        startWall: Long,
        targetTab: String = "today"
    ) {
        if (!hasPostPermission(context)) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        try {
            val notif = buildTimerNotification(context, label, startWall, targetTab)
            nm.notify(ID_TIMER, notif)
        } catch (_: Throwable) {}
    }

    fun cancelTimerNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        try {
            nm.cancel(ID_TIMER)
        } catch (_: Throwable) {}
    }

    // L2: Health Overview Persistent Notification
    fun showOverviewNotification(context: Context, summary: HealthSummary) {
        if (!hasPostPermission(context)) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "health")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            ID_OVERVIEW,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sleepText = if (summary.sleepHours > 0) "${summary.sleepHours} 小时" else "暂无记录"
        val heartText = if (summary.heartRateBpm > 0) "${summary.heartRateBpm} bpm" else "暂无记录"

        val bigText = "今日步数 ${summary.steps} / 8000 步\n静息心率 $heartText\n昨夜睡眠 $sleepText\n点按 → 查看健康数据"

        val notif = NotificationCompat.Builder(context, CH_OVERVIEW)
            .setContentTitle("今日健康概览")
            .setContentText("今日步数 ${summary.steps} 步 · 睡眠 $sleepText")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        try {
            nm.notify(ID_OVERVIEW, notif)
        } catch (_: Throwable) {}
    }

    fun cancelOverviewNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        try {
            nm.cancel(ID_OVERVIEW)
        } catch (_: Throwable) {}
    }

    // L3: Alert Notification
    fun showAlertNotification(context: Context, title: String, content: String) {
        if (!hasPostPermission(context)) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "health")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            ID_ALERT,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CH_ALERT)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            nm.notify(ID_ALERT, notif)
        } catch (_: Throwable) {}
    }
}
