package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppPrefs
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = AppPrefs(context)
            val views = RemoteViews(context.packageName, R.layout.widget_today)

            // Greeting
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greeting = when {
                hour < 6 -> "夜深了"
                hour < 11 -> "早上好"
                hour < 14 -> "中午好"
                hour < 18 -> "下午好"
                else -> "晚上好"
            }
            views.setTextViewText(R.id.widget_greeting, "一人居 · $greeting")

            // Date
            val dateStr = SimpleDateFormat("M月d日", Locale.CHINA).format(Date())
            views.setTextViewText(R.id.widget_date, dateStr)

            // Today Expenses
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val expenses = prefs.getExpenses()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayTotal = expenses.filter { sdf.format(Date(it.ts)) == todayKey }.sumOf { it.amount }
            views.setTextViewText(R.id.widget_spent, "¥${todayTotal.toInt()}")

            // Pending Tasks
            val tasks = prefs.getTasks()
            val pendingCount = tasks.count { !it.done }
            views.setTextViewText(R.id.widget_pending_tasks, "$pendingCount")

            // Timer / Fluid cloud status
            if (prefs.isTimerRunning) {
                views.setTextViewText(R.id.widget_timer_text, "⏱ ${prefs.timerLabel}中")
            } else {
                views.setTextViewText(R.id.widget_timer_text, "⏱ 流体云就绪")
            }

            // Next step
            val nextCommute = prefs.getCommutePlans().firstOrNull { !it.done }?.text
            val nextTask = tasks.firstOrNull { !it.done }?.text
            val nextStep = nextCommute ?: nextTask ?: "今日事务均已完成"
            views.setTextViewText(R.id.widget_next_step, "▶ 下一步：$nextStep")

            // Launch MainActivity when clicked
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("target_tab", "today")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, TodayWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }
}
