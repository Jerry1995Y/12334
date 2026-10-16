package com.example.ui.components

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import com.example.widget.TodayWidgetProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppPrefs
import com.example.notification.Notifier
import com.example.ui.theme.YirenjuTheme

@Composable
fun SettingsDialog(
    prefs: AppPrefs,
    currentThemeMode: String,
    onThemeModeChange: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val colors = YirenjuTheme.colors
    var keepAliveOn by remember { mutableStateOf(prefs.keepAliveEnabled) }

    val pm = remember { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
    val isIgnoringBattery = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm?.isIgnoringBatteryOptimizations(context.packageName) == true
        } else true
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "设置与偏好",
                        color = colors.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // Close button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.surface2)
                            .clickable(onClick = onDismissRequest),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", color = colors.muted, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: 外观与显示 (Appearance)
                Text(
                    text = "外观与显示",
                    color = colors.muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeOptions = listOf(
                        Triple("dark", "🌙 暗色", "OLED 纯黑"),
                        Triple("light", "☀️ 白天", "清爽通透"),
                        Triple("system", "⚙️ 跟随系统", "自适应")
                    )

                    themeOptions.forEach { (mode, title, desc) ->
                        val isSelected = currentThemeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) colors.navPill else colors.surface2)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.accent else colors.border,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    onThemeModeChange(mode)
                                    val msg = when (mode) {
                                        "dark" -> "已切换至暗色模式"
                                        "light" -> "已切换至白天模式"
                                        else -> "已切换至跟随系统"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = title,
                                    color = if (isSelected) colors.accent else colors.text,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    color = colors.muted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 2: 后台保活与实时推送 (Keep-Alive)
                Text(
                    text = "OPPO / ColorOS 后台保活与流体云",
                    color = colors.muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 电池优化状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "系统电池优化白名单",
                            color = colors.text,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isIgnoringBattery) "已在白名单内（无休眠，推荐）" else "未加白名单（可能被系统休眠）",
                            color = if (isIgnoringBattery) colors.ok else colors.muted,
                            fontSize = 12.sp
                        )
                    }

                    if (!isIgnoringBattery) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    try {
                                        val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                        context.startActivity(fallback)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "请在系统电池设置中将一人居设为无限制", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            Text("去加白", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 前台服务持续保活开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "前台服务持续保活机制",
                            color = colors.text,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "防止锁屏被杀，确保流体云与数据无延迟同步",
                            color = colors.muted,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = keepAliveOn,
                        onCheckedChange = { checked ->
                            keepAliveOn = checked
                            prefs.keepAliveEnabled = checked
                            Toast.makeText(context, if (checked) "已开启持续保活守护" else "已关闭持续保活", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.accent,
                            uncheckedTrackColor = colors.surface2
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // OPPO 手机管家与应用设置跳转
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { openOppoAutoStart(context) },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                    ) {
                        Text("自启动管理", color = colors.text, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { openAppSettings(context) },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                    ) {
                        Text("应用权限设置", color = colors.text, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 测试即时通知
                Button(
                    onClick = {
                        Notifier.showAlertNotification(
                            context,
                            "一人居 · 实时推送测试",
                            "通知实时通道畅通！ColorOS 后台同步状态正常无延迟。"
                        )
                        Toast.makeText(context, "已发送一条即时测试通知", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                ) {
                    Text("测试即时通知推送", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 3: 数据备份与恢复 (Data Backup & Restore)
                Text(
                    text = "数据备份与恢复（本地离线）",
                    color = colors.muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "数据存储于本机，使用系统安全框架导出/恢复 JSON 文件，卸载或换机不丢数据。",
                    color = colors.muted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onDismissRequest()
                            onExportBackup()
                        },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                    ) {
                        Text("导出备份文件", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            onDismissRequest()
                            onImportBackup()
                        },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                    ) {
                        Text("从备份恢复", color = colors.text, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION: 桌面小部件 (Home Screen Widgets)
                Text(
                    text = "桌面小部件（桌面生活卡片）",
                    color = colors.muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "一人居桌面小部件支持在手机主屏幕随时查看今日花销、待办数量、流体云计时与下一步规划，点按即可直达应用。",
                    color = colors.muted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val appWidgetManager = AppWidgetManager.getInstance(context)
                                val myProvider = ComponentName(context, TodayWidgetProvider::class.java)
                                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                    appWidgetManager.requestPinAppWidget(myProvider, null, null)
                                    Toast.makeText(context, "已发起添加小部件到桌面请求", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "请长按桌面空白处 -> 小部件 -> 选择一人居", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "请长按桌面空白处添加一人居小部件", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                    ) {
                        Text("添加到桌面", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            TodayWidgetProvider.updateAll(context)
                            Toast.makeText(context, "桌面小部件已刷新最新数据", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                    ) {
                        Text("刷新小部件", color = colors.text, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 4: 关于一人居 (About)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "一人居 · 原生 Android 生活 OS",
                        color = colors.text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "v1.4.1 (Build 7) · 本地纯净离线 · 适配 OPPO ColorOS 16/17",
                        color = colors.muted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

private fun openOppoAutoStart(context: Context) {
    try {
        val intent = Intent().apply {
            component = ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent2 = Intent().apply {
                component = ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                )
            }
            context.startActivity(intent2)
        } catch (_: Exception) {
            openAppSettings(context)
        }
    }
}

private fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "无法打开系统设置", Toast.LENGTH_SHORT).show()
    }
}
