package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPrefs
import com.example.model.CommutePlanItem
import com.example.service.LiveUpdatesService
import com.example.ui.theme.YirenjuTheme
import com.example.widget.TodayWidgetProvider
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun CommuteScreen(
    prefs: AppPrefs,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = YirenjuTheme.colors

    var commutePlans by remember { mutableStateOf(prefs.getCommutePlans()) }
    var newPlanText by remember { mutableStateOf("") }
    var commuteLogs by remember { mutableStateOf(prefs.getCommuteLogs()) }

    var isRunning by remember {
        mutableStateOf(prefs.isTimerRunning && prefs.timerLabel == "通勤打卡")
    }
    var startWall by remember { mutableLongStateOf(prefs.timerStartWall) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    // Calculate this week's commute count
    val weekCount = remember(commuteLogs) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = (dayOfWeek + 5) % 7
            add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        }
        val startOfWeek = cal.timeInMillis
        commuteLogs.count { it >= startOfWeek }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 我的通勤 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "我的通勤", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "家 · 光明（6号线） → 公司 · 雅宝（10号线）",
                    color = colors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "换乘 1 次 · 单程约 50–70 分钟",
                    color = colors.muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (isRunning) {
                            // Stop commute timer
                            prefs.isTimerRunning = false
                            isRunning = false
                            LiveUpdatesService.stop(context)
                            TodayWidgetProvider.updateAll(context)
                            Toast.makeText(context, "已结束通勤计时", Toast.LENGTH_SHORT).show()
                        } else {
                            // Start commute timer
                            val wall = System.currentTimeMillis()
                            prefs.isTimerRunning = true
                            prefs.timerLabel = "通勤打卡"
                            prefs.timerStartWall = wall
                            prefs.timerTab = "commute"
                            prefs.addCommuteLog(wall)
                            commuteLogs = prefs.getCommuteLogs()
                            isRunning = true
                            startWall = wall
                            LiveUpdatesService.start(context, "通勤打卡", wall, "commute")
                            TodayWidgetProvider.updateAll(context)
                            Toast.makeText(context, "已开始通勤计时，流体云胶囊已启动", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text(
                        text = if (isRunning) "结束通勤计时" else "开始通勤计时",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isRunning) {
                    val elapsedSec = ((nowMillis - startWall).coerceAtLeast(0L) / 1000L)
                    val timeStr = String.format(
                        "%02d:%02d:%02d",
                        elapsedSec / 3600,
                        (elapsedSec % 3600) / 60,
                        elapsedSec % 60
                    )
                    Text(
                        text = timeStr,
                        color = colors.step,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "已提交到流体云 · 状态栏与锁屏可查看",
                        color = colors.muted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Text(
                    text = "本周已利用通勤 $weekCount 次",
                    color = colors.muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 车上做点啥（计划清单）Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "车上做点啥（计划清单）", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                commutePlans.forEachIndexed { index, plan ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tick checkmark
                        Box(
                            modifier = Modifier
                                .size(44.dp, 48.dp)
                                .clickable {
                                    val updated = commutePlans.toMutableList()
                                    updated[index] = plan.copy(done = !plan.done)
                                    commutePlans = updated
                                    prefs.saveCommutePlans(updated)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(
                                        1.5.dp,
                                        if (plan.done) colors.ok else colors.border,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (plan.done) {
                                    Text(text = "✓", color = colors.ok, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Text
                        Text(
                            text = plan.text,
                            color = if (plan.done) colors.muted else colors.text,
                            fontSize = 15.sp,
                            textDecoration = if (plan.done) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )

                        // Delete
                        Box(
                            modifier = Modifier
                                .size(44.dp, 48.dp)
                                .clickable {
                                    val updated = commutePlans.toMutableList()
                                    updated.removeAt(index)
                                    commutePlans = updated
                                    prefs.saveCommutePlans(updated)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "×", color = colors.muted, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input + ＋ button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPlanText,
                        onValueChange = { newPlanText = it },
                        placeholder = { Text("如：听一期播客", color = colors.muted, fontSize = 14.sp) },
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
                                if (newPlanText.isNotBlank()) {
                                    val updated = commutePlans.toMutableList()
                                    updated.add(CommutePlanItem(System.currentTimeMillis(), newPlanText.trim(), false))
                                    commutePlans = updated
                                    prefs.saveCommutePlans(updated)
                                    newPlanText = ""
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
                                if (newPlanText.isNotBlank()) {
                                    val updated = commutePlans.toMutableList()
                                    updated.add(CommutePlanItem(System.currentTimeMillis(), newPlanText.trim(), false))
                                    commutePlans = updated
                                    prefs.saveCommutePlans(updated)
                                    newPlanText = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "＋", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
