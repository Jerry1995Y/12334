package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DaySteps
import com.example.ui.theme.YirenjuTheme

@Composable
fun WeekStepsBarChart(
    days: List<DaySteps>,
    modifier: Modifier = Modifier
) {
    val colors = YirenjuTheme.colors
    val maxSteps = (days.maxOfOrNull { it.steps } ?: 8000L).coerceAtLeast(1000L).toFloat()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "本周步数",
            color = colors.muted,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Canvas Bars
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
        ) {
            val count = days.size.coerceAtLeast(1)
            val slotWidth = size.width / count
            val barWidth = slotWidth * 0.52f
            val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

            days.forEachIndexed { index, day ->
                val centerX = slotWidth * index + slotWidth / 2f
                val left = centerX - barWidth / 2f

                if (day.steps <= 0) {
                    // Empty bar placeholder (5dp height)
                    val h = 5.dp.toPx()
                    drawRoundRect(
                        color = colors.surface2,
                        topLeft = Offset(left, size.height - h),
                        size = Size(barWidth, h),
                        cornerRadius = cornerRadius
                    )
                } else {
                    val ratio = (day.steps / maxSteps).coerceIn(0.04f, 1f)
                    val barHeight = size.height * ratio
                    drawRoundRect(
                        color = colors.step,
                        topLeft = Offset(left, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Date labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Text(
                    text = day.dateLabel,
                    color = colors.muted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        val todaySteps = days.lastOrNull()?.steps ?: 0L
        Text(
            text = "今日合计 $todaySteps 步",
            color = colors.step,
            fontSize = 14.sp
        )
    }
}
