package com.example

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.AppPrefs
import com.example.notification.Notifier
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TabIcon
import com.example.ui.screens.CommuteScreen
import com.example.ui.screens.HealthScreen
import com.example.ui.screens.ListScreen
import com.example.ui.screens.MoneyScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.YirenjuTheme
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var prefs: AppPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = AppPrefs(this)

        val targetTab = intent?.getStringExtra("target_tab") ?: "today"

        setContent {
            var themeMode by remember { mutableStateOf(prefs.themeMode) }

            YirenjuTheme(themeMode = themeMode) {
                MainAppContent(
                    prefs = prefs,
                    initialTab = targetTab,
                    currentThemeMode = themeMode,
                    onThemeModeChange = { newMode ->
                        themeMode = newMode
                        prefs.themeMode = newMode
                    },
                    onRestart = { recreate() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun MainAppContent(
    prefs: AppPrefs,
    initialTab: String,
    currentThemeMode: String,
    onThemeModeChange: (String) -> Unit,
    onRestart: () -> Unit
) {
    val context = LocalContext.current
    val colors = YirenjuTheme.colors
    var currentTab by remember { mutableStateOf(initialTab) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Crash Recovery check
    var crashMessage by remember { mutableStateOf(prefs.lastCrash) }

    if (crashMessage.isNotBlank()) {
        CrashRecoveryScreen(
            stackTrace = crashMessage,
            onClearAndRetry = {
                prefs.lastCrash = ""
                crashMessage = ""
                onRestart()
            }
        )
        return
    }

    // Permission Launchers
    val activityPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Activity recognition handled */ }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(context, "通知与流体云胶囊已准备就绪", Toast.LENGTH_SHORT).show()
        }
    }

    // SAF Backup Export Launcher
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    val json = prefs.exportBackupJson()
                    os.write(json.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "备份已成功导出到本地文件", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // SAF Backup Import Launcher
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val sb = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line = reader.readLine()
                        while (line != null) {
                            sb.append(line)
                            line = reader.readLine()
                        }
                    }
                }
                val success = prefs.importBackupJson(sb.toString())
                if (success) {
                    Toast.makeText(context, "数据恢复成功！正在刷新...", Toast.LENGTH_SHORT).show()
                    onRestart()
                } else {
                    Toast.makeText(context, "备份文件格式不正确", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "恢复失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Staggered permission requests on startup (Section 6.6)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                activityPermLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        delay(900L)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!Notifier.hasPostPermission(context) && !prefs.notifAsked) {
                prefs.notifAsked = true
                notifPermLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // BackHandler: return to today tab if on sub-tab
    BackHandler(enabled = currentTab != "today") {
        currentTab = "today"
    }

    val tabTitle = when (currentTab) {
        "today" -> "今天"
        "money" -> "记账"
        "list" -> "清单"
        "commute" -> "通勤"
        "health" -> "健康"
        else -> "今天"
    }

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            // Top Bar: 30sp Bold Title + Clean Settings Icon Button ⚙️
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tabTitle,
                    color = colors.text,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.weight(1f)
                )

                // Clean Settings Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.surface2)
                        .border(0.5.dp, colors.border, CircleShape)
                        .clickable { showSettingsDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚙️", fontSize = 16.sp)
                }
            }
        },
        bottomBar = {
            // Bottom Tab Bar: 5 Tabs with Custom Icons and Deep/Soft Blue Active Pill
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.navBg)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        Triple("today", "今天", "today"),
                        Triple("money", "记账", "money"),
                        Triple("list", "清单", "list"),
                        Triple("commute", "通勤", "commute"),
                        Triple("health", "健康", "health")
                    )

                    tabs.forEach { (id, label, iconKind) ->
                        val isSelected = currentTab == id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) colors.navPill else colors.navBg)
                                .clickable { currentTab = id }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                TabIcon(
                                    kind = iconKind,
                                    tint = if (isSelected) colors.accent else colors.muted
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) colors.accent else colors.muted,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "today" -> TodayScreen(prefs = prefs)
                "money" -> MoneyScreen(prefs = prefs)
                "list" -> ListScreen(prefs = prefs)
                "commute" -> CommuteScreen(prefs = prefs)
                "health" -> HealthScreen(prefs = prefs)
                else -> TodayScreen(prefs = prefs)
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            prefs = prefs,
            currentThemeMode = currentThemeMode,
            onThemeModeChange = onThemeModeChange,
            onExportBackup = {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                exportBackupLauncher.launch("yirenju-backup-$today.json")
            },
            onImportBackup = {
                importBackupLauncher.launch(arrayOf("application/json", "*/*"))
            },
            onDismissRequest = { showSettingsDialog = false }
        )
    }
}

@Composable
fun CrashRecoveryScreen(
    stackTrace: String,
    onClearAndRetry: () -> Unit
) {
    val colors = YirenjuTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(colors.bg)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "应用遇到问题（已被捕获，不再闪退）：",
            color = colors.text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(14.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stackTrace,
                color = colors.muted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClearAndRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
        ) {
            Text("清除并重试", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}
