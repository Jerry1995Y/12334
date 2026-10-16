package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CommutePlanItem
import com.example.model.ExpenseItem
import com.example.model.ShoppingItem
import com.example.model.TaskItem
import org.json.JSONArray
import org.json.JSONObject

class AppPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("yirenju_prefs", Context.MODE_PRIVATE)

    // Today Tasks
    fun getTasks(): List<TaskItem> {
        val raw = prefs.getString("tasks", null) ?: return getInitialTasks()
        val list = mutableListOf<TaskItem>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    TaskItem(
                        id = obj.optLong("id", System.nanoTime()),
                        text = obj.optString("text", ""),
                        done = obj.optBoolean("done", false)
                    )
                )
            }
        } catch (_: Exception) {
            return getInitialTasks()
        }
        return list
    }

    private fun getInitialTasks(): List<TaskItem> {
        val initial = listOf(
            TaskItem(1L, "取快递", done = true),
            TaskItem(2L, "整理本周读书笔记", done = false),
            TaskItem(3L, "预约周日体检", done = false)
        )
        saveTasks(initial)
        return initial
    }

    fun saveTasks(tasks: List<TaskItem>) {
        val arr = JSONArray()
        for (item in tasks) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("done", item.done)
            arr.put(obj)
        }
        prefs.edit().putString("tasks", arr.toString()).apply()
    }

    // Expenses
    fun getExpenses(): List<ExpenseItem> {
        val raw = prefs.getString("expenses", null) ?: return getInitialExpenses()
        val list = mutableListOf<ExpenseItem>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ExpenseItem(
                        id = obj.optLong("id", System.nanoTime()),
                        amount = obj.optDouble("amount", 0.0),
                        cat = obj.optString("cat", "其他"),
                        note = obj.optString("note", ""),
                        ts = obj.optLong("ts", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            return getInitialExpenses()
        }
        return list
    }

    private fun getInitialExpenses(): List<ExpenseItem> {
        val now = System.currentTimeMillis()
        val initial = listOf(
            ExpenseItem(1L, 38.0, "餐饮", "午饭", now - 4 * 3600 * 1000),
            ExpenseItem(2L, 6.0, "交通", "地铁", now - 8 * 3600 * 1000),
            ExpenseItem(3L, 142.0, "餐饮", "超市买菜", now - 28 * 3600 * 1000)
        )
        saveExpenses(initial)
        return initial
    }

    fun saveExpenses(expenses: List<ExpenseItem>) {
        val arr = JSONArray()
        for (item in expenses) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("amount", item.amount)
            obj.put("cat", item.cat)
            obj.put("note", item.note)
            obj.put("ts", item.ts)
            arr.put(obj)
        }
        prefs.edit().putString("expenses", arr.toString()).apply()
    }

    // Monthly Settings
    var income: String
        get() = prefs.getString("income", "12000") ?: "12000"
        set(value) = prefs.edit().putString("income", value).apply()

    var budget: String
        get() = prefs.getString("budget", "5000") ?: "5000"
        set(value) = prefs.edit().putString("budget", value).apply()

    // Shopping List
    fun getShopping(): List<ShoppingItem> {
        val raw = prefs.getString("shopping", null) ?: return getInitialShopping()
        val list = mutableListOf<ShoppingItem>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ShoppingItem(
                        id = obj.optLong("id", System.nanoTime()),
                        text = obj.optString("text", ""),
                        group = obj.optString("group", "食材"),
                        done = obj.optBoolean("done", false)
                    )
                )
            }
        } catch (_: Exception) {
            return getInitialShopping()
        }
        return list
    }

    private fun getInitialShopping(): List<ShoppingItem> {
        val initial = listOf(
            ShoppingItem(1L, "鸡蛋", "食材", done = true),
            ShoppingItem(2L, "牛奶", "食材", done = false),
            ShoppingItem(3L, "洗衣液", "日用品", done = false),
            ShoppingItem(4L, "垃圾袋", "日用品", done = false)
        )
        saveShopping(initial)
        return initial
    }

    fun saveShopping(items: List<ShoppingItem>) {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("group", item.group)
            obj.put("done", item.done)
            arr.put(obj)
        }
        prefs.edit().putString("shopping", arr.toString()).apply()
    }

    // Commute Plans
    fun getCommutePlans(): List<CommutePlanItem> {
        val raw = prefs.getString("commute_plans", null) ?: return getInitialCommutePlans()
        val list = mutableListOf<CommutePlanItem>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    CommutePlanItem(
                        id = obj.optLong("id", System.nanoTime()),
                        text = obj.optString("text", ""),
                        done = obj.optBoolean("done", false)
                    )
                )
            }
        } catch (_: Exception) {
            return getInitialCommutePlans()
        }
        return list
    }

    private fun getInitialCommutePlans(): List<CommutePlanItem> {
        val initial = listOf(
            CommutePlanItem(1L, "听播客 / 有声书", done = false),
            CommutePlanItem(2L, "刷科技资讯 / 阅读灵感", done = false),
            CommutePlanItem(3L, "学英语 15 分钟", done = true),
            CommutePlanItem(4L, "闭目养神", done = false)
        )
        saveCommutePlans(initial)
        return initial
    }

    fun saveCommutePlans(items: List<CommutePlanItem>) {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("done", item.done)
            arr.put(obj)
        }
        prefs.edit().putString("commute_plans", arr.toString()).apply()
    }

    // Commute Log Timestamps
    fun getCommuteLogs(): List<Long> {
        val raw = prefs.getString("commute_logs", null) ?: return listOf(
            System.currentTimeMillis() - 86400000L * 3,
            System.currentTimeMillis() - 86400000L * 2,
            System.currentTimeMillis() - 86400000L * 1,
            System.currentTimeMillis() - 3600000L * 2
        )
        val list = mutableListOf<Long>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                list.add(arr.getLong(i))
            }
        } catch (_: Exception) {}
        return list
    }

    fun addCommuteLog(ts: Long) {
        val list = getCommuteLogs().toMutableList()
        list.add(ts)
        val arr = JSONArray()
        for (item in list) {
            arr.put(item)
        }
        prefs.edit().putString("commute_logs", arr.toString()).apply()
    }

    // Timer Running State
    var isTimerRunning: Boolean
        get() = prefs.getBoolean("timer_running", false)
        set(value) = prefs.edit().putBoolean("timer_running", value).apply()

    var timerLabel: String
        get() = prefs.getString("timer_label", "通勤打卡") ?: "通勤打卡"
        set(value) = prefs.edit().putString("timer_label", value).apply()

    var timerStartWall: Long
        get() = prefs.getLong("timer_start_wall", System.currentTimeMillis())
        set(value) = prefs.edit().putLong("timer_start_wall", value).apply()

    var timerTab: String
        get() = prefs.getString("timer_tab", "today") ?: "today"
        set(value) = prefs.edit().putString("timer_tab", value).apply()

    // Health Manual Values
    var manualSteps: Long
        get() = prefs.getLong("manual_steps", 0L)
        set(value) = prefs.edit().putLong("manual_steps", value).apply()

    var manualSleep: Double
        get() = prefs.getFloat("manual_sleep", 0f).toDouble()
        set(value) = prefs.edit().putFloat("manual_sleep", value.toFloat()).apply()

    var stepGoal: Long
        get() = prefs.getLong("step_goal", 8000L)
        set(value) = prefs.edit().putLong("step_goal", value).apply()

    var stepSensorBase: Long
        get() = prefs.getLong("step_sensor_base", -1L)
        set(value) = prefs.edit().putLong("step_sensor_base", value).apply()

    var stepSensorDate: String
        get() = prefs.getString("step_sensor_date", "") ?: ""
        set(value) = prefs.edit().putString("step_sensor_date", value).apply()

    var overviewNotificationOn: Boolean
        get() = prefs.getBoolean("overview_notification_on", false)
        set(value) = prefs.edit().putBoolean("overview_notification_on", value).apply()

    var notifAsked: Boolean
        get() = prefs.getBoolean("notif_asked", false)
        set(value) = prefs.edit().putBoolean("notif_asked", value).apply()

    var hcAutoAsked: Boolean
        get() = prefs.getBoolean("hc_auto_asked", false)
        set(value) = prefs.edit().putBoolean("hc_auto_asked", value).apply()

    var keepAliveEnabled: Boolean
        get() = prefs.getBoolean("keep_alive_enabled", true)
        set(value) = prefs.edit().putBoolean("keep_alive_enabled", value).apply()

    var lastCrash: String
        get() = prefs.getString("last_crash", "") ?: ""
        set(value) = prefs.edit().putString("last_crash", value).apply()

    var themeMode: String
        get() = prefs.getString("theme_mode", "dark") ?: "dark"
        set(value) = prefs.edit().putString("theme_mode", value).apply()

    // Export all preferences to JSON String
    fun exportBackupJson(): String {
        val root = JSONObject()
        val all = prefs.all
        for ((key, value) in all) {
            root.put(key, value)
        }
        return root.toString(2)
    }

    // Import from JSON String
    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val editor = prefs.edit()
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                when (val value = root.get(key)) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                    is String -> editor.putString(key, value)
                    else -> editor.putString(key, value.toString())
                }
            }
            editor.apply()
            true
        } catch (_: Exception) {
            false
        }
    }
}
