package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YirenjuTheme

@Composable
fun HealthRingCard(
    steps: Long,
    goal: Long = 8000L,
    distanceKm: Double,
    caloriesKcal: Long,
    sourceLabel: String,
    modifier: Modifier = Modifier
) {
    val colors = YirenjuTheme.colors
    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "今日步数",
            color = colors.muted,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ring
            Box(
                modifier = Modifier.size(132.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    val arcSize = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    // Track
                    drawArc(
                        color = colors.surface2,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth)
                    )

                    // Progress
                    if (progress > 0f) {
                        drawArc(
                            color = colors.step,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%,d", steps),
                        color = colors.text,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "步 / 目标 $goal",
                        color = colors.muted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Right side stats: Distance & Calories
            Column {
                Text(
                    text = if (distanceKm > 0) String.format("%.1f", distanceKm) else "0.0",
                    color = colors.dist,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "距离 km",
                    color = colors.muted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (caloriesKcal > 0) "$caloriesKcal" else "0",
                    color = colors.kcal,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "消耗 kcal",
                    color = colors.muted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "来源：$sourceLabel",
            color = colors.muted,
            fontSize = 12.sp
        )
    }
}
