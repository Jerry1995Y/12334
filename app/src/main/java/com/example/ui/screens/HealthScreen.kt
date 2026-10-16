package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.example.data.AppPrefs
import com.example.health.HealthConnectRepo
import com.example.health.StepSensorController
import com.example.model.DaySteps
import com.example.model.HealthSummary
import com.example.notification.Notifier
import com.example.ui.components.HealthRingCard
import com.example.ui.components.KeepAliveDialog
import com.example.ui.components.WeekStepsBarChart
import com.example.ui.theme.YirenjuTheme
import kotlinx.coroutines.launch

@Composable
fun HealthScreen(
    prefs: AppPrefs,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = YirenjuTheme.colors
    val scope = rememberCoroutineScope()
    val hcRepo = remember { HealthConnectRepo(context) }
    val stepSensor = remember { StepSensorController(context) }

    var showKeepAliveDialog by remember { mutableStateOf(false) }

    // Health Data States
    var displaySteps by remember { mutableLongStateOf(if (prefs.manualSteps > 0) prefs.manualSteps else 6428L) }
    var displayDistance by remember { mutableDoubleStateOf(12.4) }
    var displayCalories by remember { mutableLongStateOf(486L) }
    var displaySleep by remember { mutableDoubleStateOf(if (prefs.manualSleep > 0) prefs.manualSleep else 7.2) }
    var displaySleepRange by remember { mutableStateOf("23:10 → 06:22") }
    var displayHeartRate by remember { mutableIntStateOf(68) }
    var displaySource by remember {
        mutableStateOf(if (prefs.manualSteps > 0) "手动录入（兜底优先）" else "Health Connect（系统健康数据）")
    }

    var weekSteps by remember {
        mutableStateOf(
            listOf(
                DaySteps("10/10", 3120L),
                DaySteps("10/11", 5200L),
                DaySteps("10/12", 6800L),
                DaySteps("10/13", 8100L),
                DaySteps("10/14", 4050L),
                DaySteps("10/15", 7300L),
                DaySteps("10/16", 6428L)
            )
        )
    }

    // Permission states
    var hasActivityPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasPostNotif by remember { mutableStateOf(Notifier.hasPostPermission(context)) }
    var hasHcPermission by remember { mutableStateOf(false) }
    var hcSdkAvailable by remember {
        mutableStateOf(HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE)
    }

    // Refresh function combining: 1. Manual -> 2. Health Connect -> 3. Step Sensor
    val refreshData = {
        scope.launch {
            hasHcPermission = hcRepo.hasAllPermissions()
            hasActivityPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
            hasPostNotif = Notifier.hasPostPermission(context)

            if (prefs.manualSteps > 0 || prefs.manualSleep > 0) {
                if (prefs.manualSteps > 0) displaySteps = prefs.manualSteps
                if (prefs.manualSleep > 0) displaySleep = prefs.manualSleep
                displaySource = "手动录入（兜底优先）"
            } else if (hasHcPermission) {
                val summary = hcRepo.readTodayHealth()
                if (summary != null) {
                    displaySteps = summary.steps
                    displayDistance = summary.distanceKm
                    displayCalories = summary.caloriesKcal
                    if (summary.sleepHours > 0) {
                        displaySleep = summary.sleepHours
                        displaySleepRange = "${summary.sleepStart} → ${summary.sleepEnd}"
                    }
                    if (summary.heartRateBpm > 0) {
                        displayHeartRate = summary.heartRateBpm
                    }
                    if (summary.weekSteps.isNotEmpty()) {
                        weekSteps = summary.weekSteps
                    }
                    displaySource = "Health Connect（系统健康数据）"
                }
            } else if (hasActivityPermission && stepSensor.currentSensorSteps > 0) {
                displaySteps = stepSensor.currentSensorSteps
                displayDistance = String.format("%.1f", displaySteps * 0.0007).toDoubleOrNull() ?: 0.0
                displayCalories = (displaySteps * 0.04).toLong()
                displaySource = "手机计步传感器（内置硬件）"
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
        if (hasActivityPermission) {
            stepSensor.startListening { sensorSteps ->
                if (prefs.manualSteps <= 0 && !hasHcPermission) {
                    displaySteps = sensorSteps
                    displayDistance = String.format("%.1f", sensorSteps * 0.0007).toDoubleOrNull() ?: 0.0
                    displayCalories = (sensorSteps * 0.04).toLong()
                    displaySource = "手机计步传感器（内置硬件）"
                }
            }
        }
    }

    // Health Connect Permission Launcher
    val hcPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        scope.launch {
            if (granted.containsAll(hcRepo.requiredPermissions)) {
                hasHcPermission = true
                Toast.makeText(context, "健康数据读取授权成功！", Toast.LENGTH_SHORT).show()
                refreshData()
                if (Notifier.hasPostPermission(context)) {
                    Notifier.showOverviewNotification(
                        context,
                        HealthSummary(
                            steps = displaySteps,
                            distanceKm = displayDistance,
                            caloriesKcal = displayCalories,
                            sleepHours = displaySleep,
                            sleepStart = "23:10",
                            sleepEnd = "06:22",
                            heartRateBpm = displayHeartRate,
                            weekSteps = weekSteps,
                            sourceLabel = "Health Connect"
                        )
                    )
                }
            } else {
                Toast.makeText(context, "部分权限未授予，可手动录入或前往设置授权", Toast.LENGTH_SHORT).show()
                refreshData()
            }
        }
    }

    val activityPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasActivityPermission = granted
        if (granted) {
            Toast.makeText(context, "计步传感器权限已授予", Toast.LENGTH_SHORT).show()
            stepSensor.startListening { sensorSteps ->
                if (prefs.manualSteps <= 0 && !hasHcPermission) {
                    displaySteps = sensorSteps
                    displaySource = "手机计步传感器（内置硬件）"
                }
            }
        } else {
            Toast.makeText(context, "未允许计步，可通过手动录入兜底", Toast.LENGTH_SHORT).show()
        }
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPostNotif = granted
        if (granted) {
            Toast.makeText(context, "通知权限已允许，流体云胶囊就绪", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "未允许通知，计时功能将仅在应用内可见", Toast.LENGTH_SHORT).show()
        }
    }

    var manualStepsInput by remember { mutableStateOf(if (prefs.manualSteps > 0) "${prefs.manualSteps}" else "") }
    var manualSleepInput by remember { mutableStateOf(if (prefs.manualSleep > 0) "${prefs.manualSleep}" else "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Title & Source State
        Text(
            text = "运动 · 睡眠",
            color = colors.text,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (hasHcPermission) "Health Connect 已授权，睡眠与心率同步中" else if (hcSdkAvailable) "Health Connect 可用，但尚未授权" else "Health Connect 本机未安装，计步传感器与手动兜底运行中",
            color = colors.muted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        // 今日步数 Card (HealthRingCard)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            HealthRingCard(
                steps = displaySteps,
                goal = prefs.stepGoal,
                distanceKm = displayDistance,
                caloriesKcal = displayCalories,
                sourceLabel = displaySource
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2 KPI Cards: 最近睡眠 & 心率
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Sleep Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "最近睡眠", color = colors.sleep, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${displaySleep} h",
                        color = colors.text,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = displaySleepRange, color = colors.muted, fontSize = 11.sp)
                }
            }

            // Heart Rate Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "心率", color = colors.heart, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$displayHeartRate",
                        color = colors.text,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "bpm", color = colors.muted, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 本周步数 Card (WeekStepsBarChart)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            WeekStepsBarChart(days = weekSteps)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 数据来源 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "数据来源", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        try {
                            hcPermissionLauncher.launch(hcRepo.requiredPermissions)
                        } catch (_: Exception) {
                            Toast.makeText(context, "未找到 Health Connect 授权服务", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("授权读取健康数据", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        openHealthSettings(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                ) {
                    Text("打开系统健康设置", color = colors.text, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 提醒与权限 Card (含诊断信息)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "提醒与权限", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "流体云状态：" + Notifier.getFluidCloudStatusText(context),
                    color = colors.muted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Row 1: 计步与活动识别
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "计步与活动识别", color = colors.text, fontSize = 14.sp)
                        Text(
                            text = if (hasActivityPermission) "已允许，可读取手机计步" else "未允许，步数只能手动录入",
                            color = if (hasActivityPermission) colors.ok else colors.muted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                    if (!hasActivityPermission) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    activityPermLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            Text("去授权", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Row 2: Health Connect 健康数据
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Health Connect 健康数据", color = colors.text, fontSize = 14.sp)
                        Text(
                            text = if (hasHcPermission) "已授权 5 类：步数·睡眠·心率·距离·卡路里" else "未授权，读不到睡眠与心率",
                            color = if (hasHcPermission) colors.ok else colors.muted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                    if (!hasHcPermission) {
                        Button(
                            onClick = {
                                try {
                                    hcPermissionLauncher.launch(hcRepo.requiredPermissions)
                                } catch (_: Exception) {
                                    openHealthSettings(context)
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            Text("去授权", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Row 3: 通知与实时活动
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "通知与实时活动", color = colors.text, fontSize = 14.sp)
                        Text(
                            text = if (hasPostNotif) "已就绪，提醒可进入流体云" else "未允许，任何提醒都不会出现",
                            color = if (hasPostNotif) colors.ok else colors.muted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                    if (!hasPostNotif) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifPermLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    openAppSettings(context)
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            Text("去开启", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Buttons
                Button(
                    onClick = {
                        val summary = HealthSummary(
                            steps = displaySteps,
                            distanceKm = displayDistance,
                            caloriesKcal = displayCalories,
                            sleepHours = displaySleep,
                            sleepStart = "23:10",
                            sleepEnd = "06:22",
                            heartRateBpm = displayHeartRate,
                            weekSteps = weekSteps,
                            sourceLabel = displaySource
                        )
                        Notifier.showOverviewNotification(context, summary)
                        Toast.makeText(context, "已在通知栏开启健康概览", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("开启健康概览通知", color = Color.White, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        Notifier.showAlertNotification(
                            context,
                            "今日步数达标提醒",
                            "已经走到 $displaySteps 步，继续保持健康好习惯！"
                        )
                        Toast.makeText(context, "已发送一条试用提醒", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                ) {
                    Text("试一条提醒", color = colors.text, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        openAppSettings(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                ) {
                    Text("打开本应用的系统设置", color = colors.text, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Diagnostic line
                val promotedText = if (Notifier.canPostPromoted(context)) "可用" else "不可用（标准通知降级）"
                val hcStatusStr = if (hasHcPermission) "已授权" else "未授权"
                val notifStatusStr = if (hasPostNotif) "系统已允许" else "未允许"
                Text(
                    text = "Android ${Build.VERSION.RELEASE}（API ${Build.VERSION.SDK_INT}）· 应用 targetSdk 36\n" +
                           "通知开关：$notifStatusStr · 提升通知：$promotedText\n" +
                           "健康连接：$hcStatusStr",
                    color = colors.muted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 后台保活与实时推送保障 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "OPPO 后台保活与实时推送保障", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "防止 ColorOS 省电策略清理计时与推送服务，保障流体云常驻与数据无延迟同步。",
                    color = colors.text,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showKeepAliveDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("配置后台保活 / 防杀设置", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 手动录入（兜底）Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "手动录入（兜底）", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Steps manual input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualStepsInput,
                        onValueChange = { manualStepsInput = it },
                        placeholder = { Text("步数", color = colors.muted, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface2,
                            unfocusedContainerColor = colors.surface2,
                            focusedBorderColor = colors.border,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .clickable {
                                val v = manualStepsInput.toLongOrNull()
                                if (v != null && v >= 0) {
                                    prefs.manualSteps = v
                                    displaySteps = v
                                    displayDistance = String.format("%.1f", v * 0.0007).toDoubleOrNull() ?: 0.0
                                    displayCalories = (v * 0.04).toLong()
                                    displaySource = "手动录入（兜底优先）"
                                    Toast.makeText(context, "步数已设定为 $v", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "设", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sleep manual input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualSleepInput,
                        onValueChange = { manualSleepInput = it },
                        placeholder = { Text("睡眠小时 如 7.5", color = colors.muted, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface2,
                            unfocusedContainerColor = colors.surface2,
                            focusedBorderColor = colors.border,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .clickable {
                                val v = manualSleepInput.toDoubleOrNull()
                                if (v != null && v >= 0) {
                                    prefs.manualSleep = v
                                    displaySleep = v
                                    displaySource = "手动录入（兜底优先）"
                                    Toast.makeText(context, "睡眠已设定为 $v 小时", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "设", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        prefs.manualSteps = 0L
                        prefs.manualSleep = 0.0
                        manualStepsInput = ""
                        manualSleepInput = ""
                        refreshData()
                        Toast.makeText(context, "已清除手动值，已切换为系统与传感器数据", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                ) {
                    Text("清除手动值", color = colors.text, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 刷新健康数据 Button
        Button(
            onClick = {
                refreshData()
                Toast.makeText(context, "健康数据已刷新", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
        ) {
            Text("刷新健康数据", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "数据来自安卓官方 Health Connect（手机与手表写入的数据都在这里）。不会上传，只在本机显示。",
            color = colors.muted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }

    if (showKeepAliveDialog) {
        KeepAliveDialog(onDismissRequest = { showKeepAliveDialog = false })
    }
}

private fun openHealthSettings(context: Context) {
    try {
        val intent = Intent("androidx.health.ACTION_HEALTH_HOME_SETTINGS")
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent2 = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.healthdata")
            if (intent2 != null) {
                context.startActivity(intent2)
            } else {
                openAppSettings(context)
            }
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
