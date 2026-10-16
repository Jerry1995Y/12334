package com.example.ui.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.data.AppPrefs
import com.example.notification.Notifier
import com.example.ui.theme.YirenjuTheme

@Composable
fun KeepAliveDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val colors = YirenjuTheme.colors
    val prefs = remember { AppPrefs(context) }
    var keepAliveOn by remember { mutableStateOf(prefs.keepAliveEnabled) }

    val pm = remember { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
    val isIgnoringBattery = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm?.isIgnoringBatteryOptimizations(context.packageName) == true
        } else true
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(22.dp))
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "OPPO / ColorOS 后台保活设置",
                    color = colors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "针对 OPPO ColorOS 16/17 深度优化，确保流体云胶囊常驻、通知即时送达与健康数据后台零延迟同步。",
                color = colors.muted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = colors.border, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // 1. 电池优化白名单状态
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "系统电池优化白名单",
                        color = colors.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isIgnoringBattery) "已加入白名单（无休眠，推荐）" else "未忽略电池优化（可能被系统挂起）",
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
                                    Toast.makeText(context, "请在系统电池设置中允许一人居无限制运行", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("去加白", fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = colors.border, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // 2. 前台服务持续保活开关
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "前台服务持续保活机制",
                        color = colors.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "激活前台保活心跳与唤醒锁，防止锁屏被杀",
                        color = colors.muted,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = keepAliveOn,
                    onCheckedChange = { checked ->
                        keepAliveOn = checked
                        prefs.keepAliveEnabled = checked
                        Toast.makeText(context, if (checked) "已开启前台保活保障" else "已关闭持续保活", Toast.LENGTH_SHORT).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.accent,
                        uncheckedTrackColor = colors.surface2
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = colors.border, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // 3. OPPO 快捷直达
            Text(
                text = "OPPO / ColorOS 专属自启与权限直达",
                color = colors.muted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    openOppoAutoStart(context)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
            ) {
                Text("打开 OPPO 手机管家 · 自启动管理", color = colors.text, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    openAppSettings(context)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
            ) {
                Text("打开本应用的系统权限与耗电详情", color = colors.text, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = colors.border, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // 4. ColorOS 3 步设置图文要点
            Text(
                text = "ColorOS 16/17 防杀必看三步：",
                color = colors.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "1. 多任务卡片加锁：上划呼出后台，长按或下拉一人居，点击小锁 🔒 图标锁定。\n" +
                       "2. 允许自启动：应用信息 -> 耗电管理 -> 开启「允许完全后台行为」与「允许自启动」。\n" +
                       "3. 流体云权限：通知管理 -> 开启通知并允许「提升通知 / 实时活动胶囊」。",
                color = colors.muted,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Test alert button
            Button(
                onClick = {
                    Notifier.showAlertNotification(
                        context,
                        "推送与保活测试",
                        "通知实时推送正常！当前 ColorOS 后台心跳与数据通道无延迟。"
                    )
                    Toast.makeText(context, "已发送一条即时测试通知", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("测试即时通知推送", color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
            ) {
                Text("关闭", color = colors.text, fontSize = 14.sp)
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
