package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPrefs
import com.example.model.TaskItem
import com.example.service.LiveUpdatesService
import com.example.ui.theme.YirenjuTheme
import com.example.widget.TodayWidgetProvider
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    prefs: AppPrefs,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = YirenjuTheme.colors

    var tasks by remember { mutableStateOf(prefs.getTasks()) }
    var newTaskText by remember { mutableStateOf("") }
    val expenses = remember { prefs.getExpenses() }
    val commutePlans = remember { prefs.getCommutePlans() }

    // Live chronometer tick
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isRunning by remember { mutableStateOf(prefs.isTimerRunning) }
    var currentLabel by remember { mutableStateOf(prefs.timerLabel) }
    var startWall by remember { mutableLongStateOf(prefs.timerStartWall) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 6 -> "夜深了"
            hour < 11 -> "早上好"
            hour < 14 -> "中午好"
            hour < 18 -> "下午好"
            else -> "晚上好"
        }
    }

    val dateStr = remember {
        val sdf = SimpleDateFormat("M月d日 EEEE", Locale.CHINA)
        sdf.format(Date())
    }

    // Today expenses calculation
    val todayKey = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val todayExpensesTotal = remember(expenses) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        expenses.filter { sdf.format(Date(it.ts)) == todayKey }.sumOf { it.amount }
    }

    val pendingTasksCount = tasks.count { !it.done }
    val nextCommuteItem = commutePlans.firstOrNull { !it.done }?.text ?: "都安排好啦"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Dynamic Greeting & Date
        Text(
            text = greeting,
            color = colors.text,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = dateStr,
            color = colors.muted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        // 2 KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Today Spent
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "今日已花", color = colors.kcal, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "¥${todayExpensesTotal.toInt()}",
                        color = colors.text,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pending Tasks
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "待办剩", color = colors.accent, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$pendingTasksCount",
                        color = colors.text,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ongoing Live Timer Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "进行中", color = colors.muted, fontSize = 13.sp)

                val elapsedSec = if (isRunning) {
                    ((nowMillis - startWall).coerceAtLeast(0L) / 1000L)
                } else 0L

                val timeStr = String.format(
                    "%02d:%02d:%02d",
                    elapsedSec / 3600,
                    (elapsedSec % 3600) / 60,
                    elapsedSec % 60
                )

                if (isRunning) {
                    Text(
                        text = timeStr,
                        color = colors.ok,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "$currentLabel · 已提交到流体云",
                        color = colors.muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                prefs.isTimerRunning = false
                                isRunning = false
                                LiveUpdatesService.stop(context)
                                TodayWidgetProvider.updateAll(context)
                                Toast.makeText(context, "已结束计时：$timeStr", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            Text("结束计时", color = androidx.compose.ui.graphics.Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Text(
                        text = "现在没有正在进行的活动",
                        color = colors.muted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                val wall = System.currentTimeMillis()
                                prefs.isTimerRunning = true
                                prefs.timerLabel = "阅读计时"
                                prefs.timerStartWall = wall
                                isRunning = true
                                currentLabel = "阅读计时"
                                startWall = wall
                                LiveUpdatesService.start(context, "阅读计时", wall, "today")
                                TodayWidgetProvider.updateAll(context)
                                Toast.makeText(context, "阅读计时已开启并同步到流体云", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            Text("阅读计时", color = androidx.compose.ui.graphics.Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = {
                                val wall = System.currentTimeMillis()
                                prefs.isTimerRunning = true
                                prefs.timerLabel = "专注计时"
                                prefs.timerStartWall = wall
                                isRunning = true
                                currentLabel = "专注计时"
                                startWall = wall
                                LiveUpdatesService.start(context, "专注计时", wall, "today")
                                TodayWidgetProvider.updateAll(context)
                                Toast.makeText(context, "专注计时已开启并同步到流体云", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                        ) {
                            Text("专注计时", color = colors.text, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Today Tasks Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "今日任务", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                // Task list items
                tasks.forEachIndexed { index, task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tick checkmark (44x48 min touch target)
                        Box(
                            modifier = Modifier
                                .size(44.dp, 48.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Checkbox
                                ) {
                                    val updated = tasks.toMutableList()
                                    updated[index] = task.copy(done = !task.done)
                                    tasks = updated
                                    prefs.saveTasks(updated)
                                    TodayWidgetProvider.updateAll(context)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(
                                        1.5.dp,
                                        if (task.done) colors.ok else colors.border,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (task.done) {
                                    Text(text = "✓", color = colors.ok, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Text
                        Text(
                            text = task.text,
                            color = if (task.done) colors.muted else colors.text,
                            fontSize = 15.sp,
                            textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )

                        // Delete button
                        Box(
                            modifier = Modifier
                                .size(44.dp, 48.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    val updated = tasks.toMutableList()
                                    updated.removeAt(index)
                                    tasks = updated
                                    prefs.saveTasks(updated)
                                    TodayWidgetProvider.updateAll(context)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "×", color = colors.muted, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input row: Text + ＋ button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = { Text("加个待办，回车添加", color = colors.muted, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface2,
                            unfocusedContainerColor = colors.surface2,
                            focusedBorderColor = colors.border,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newTaskText.isNotBlank()) {
                                    val updated = tasks.toMutableList()
                                    updated.add(TaskItem(System.currentTimeMillis(), newTaskText.trim(), false))
                                    tasks = updated
                                    prefs.saveTasks(updated)
                                    TodayWidgetProvider.updateAll(context)
                                    newTaskText = ""
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .clickable {
                                if (newTaskText.isNotBlank()) {
                                    val updated = tasks.toMutableList()
                                    updated.add(TaskItem(System.currentTimeMillis(), newTaskText.trim(), false))
                                    tasks = updated
                                    prefs.saveTasks(updated)
                                    TodayWidgetProvider.updateAll(context)
                                    newTaskText = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "＋", color = androidx.compose.ui.graphics.Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Commute Next Step Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "通勤下一步", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = nextCommuteItem,
                    color = colors.step,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
