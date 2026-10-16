package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPrefs
import com.example.model.ExpenseItem
import com.example.ui.theme.YirenjuTheme
import com.example.widget.TodayWidgetProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoneyScreen(
    prefs: AppPrefs,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = YirenjuTheme.colors

    var expenses by remember { mutableStateOf(prefs.getExpenses()) }

    var amountInput by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("餐饮") }
    var noteInput by remember { mutableStateOf("") }

    var incomeInput by remember { mutableStateOf(prefs.income) }
    var budgetInput by remember { mutableStateOf(prefs.budget) }

    val categories = remember { listOf("餐饮", "交通", "居家", "购物", "娱乐", "其他") }

    // Month stats
    val currentMonthKey = remember {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }
    val monthExpenses = remember(expenses) {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        expenses.filter { sdf.format(Date(it.ts)) == currentMonthKey }
    }
    val monthTotal = remember(monthExpenses) {
        monthExpenses.sumOf { it.amount }
    }

    val budgetVal = budgetInput.toDoubleOrNull() ?: 0.0
    val incomeVal = incomeInput.toDoubleOrNull() ?: 0.0

    val (rightKpiLabel, rightKpiValue) = remember(budgetVal, incomeVal, monthTotal) {
        when {
            budgetVal > 0 -> "预算剩" to "¥${(budgetVal - monthTotal).toInt().coerceAtLeast(0)}"
            incomeVal > 0 -> "结余" to "¥${(incomeVal - monthTotal).toInt()}"
            else -> "可花" to "--"
        }
    }

    // Category breakdown
    val catTotals = remember(monthExpenses) {
        val map = mutableMapOf<String, Double>()
        for (item in monthExpenses) {
            map[item.cat] = (map[item.cat] ?: 0.0) + item.amount
        }
        map
    }
    val maxCatTotal = (catTotals.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 2 KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "¥${String.format("%,d", monthTotal.toInt())}",
                        color = colors.text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "本月支出", color = colors.muted, fontSize = 13.sp)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = rightKpiValue,
                        color = colors.text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = rightKpiLabel, color = colors.muted, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 记一笔 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "记一笔", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                // Amount input
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    placeholder = { Text("金额", color = colors.muted, fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(10.dp))

                // Category chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCat == cat
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) colors.accent else colors.surface2)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, CircleShape)
                                .clickable { selectedCat = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else colors.text,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Note input
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("备注（可选）", color = colors.muted, fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface2,
                        unfocusedContainerColor = colors.surface2,
                        focusedBorderColor = colors.border,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            Toast.makeText(context, "请输入大于 0 的金额", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newItem = ExpenseItem(
                            id = System.currentTimeMillis(),
                            amount = amount,
                            cat = selectedCat,
                            note = noteInput.trim(),
                            ts = System.currentTimeMillis()
                        )
                        val updated = expenses.toMutableList()
                        updated.add(0, newItem)
                        expenses = updated
                        prefs.saveExpenses(updated)
                        TodayWidgetProvider.updateAll(context)
                        amountInput = ""
                        noteInput = ""
                        Toast.makeText(context, "已记录 ¥$amount", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("记一笔", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 分类占比（本月）Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "分类占比（本月）", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                val sortedCats = categories.filter { (catTotals[it] ?: 0.0) > 0 }
                if (sortedCats.isEmpty()) {
                    Text(text = "本月暂无支出记录", color = colors.muted, fontSize = 13.sp)
                } else {
                    sortedCats.forEach { cat ->
                        val amount = catTotals[cat] ?: 0.0
                        val ratio = (amount / maxCatTotal).toFloat().coerceIn(0.02f, 1f)

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "$cat　¥${amount.toInt()}",
                                color = colors.text,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            // Ratio track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.surface2)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(colors.accent)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 最近记录 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "最近记录", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                val timeFmt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                val displayList = expenses.take(20)

                displayList.forEachIndexed { index, exp ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${exp.cat}　¥${exp.amount.toInt()}${if (exp.note.isNotEmpty()) " · ${exp.note}" else ""}",
                                color = colors.text,
                                fontSize = 14.sp
                            )
                            Text(
                                text = timeFmt.format(Date(exp.ts)),
                                color = colors.muted,
                                fontSize = 12.sp
                            )
                        }

                        // Delete
                        Box(
                            modifier = Modifier
                                .size(44.dp, 48.dp)
                                .clickable {
                                    val updated = expenses.toMutableList()
                                    updated.removeAt(index)
                                    expenses = updated
                                    prefs.saveExpenses(updated)
                                    TodayWidgetProvider.updateAll(context)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "×", color = colors.muted, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 月度设置 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "月度设置", color = colors.muted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = incomeInput,
                        onValueChange = { incomeInput = it },
                        placeholder = { Text("月收入", color = colors.muted, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
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

                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        placeholder = { Text("月预算", color = colors.muted, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        prefs.income = incomeInput.trim()
                        prefs.budget = budgetInput.trim()
                        Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface2)
                ) {
                    Text("保存设置", color = colors.text, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
