package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun TabIcon(
    kind: String,
    tint: Color,
    modifier: Modifier = Modifier.size(22.dp)
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(
            width = 1.9.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val w = size.width
        val h = size.height

        when (kind) {
            "today" -> {
                // Calendar rect
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.14f, h * 0.22f),
                    size = Size(w * 0.72f, h * 0.65f),
                    cornerRadius = CornerRadius(w * 0.12f, h * 0.12f),
                    style = stroke
                )
                // Top hooks
                drawLine(
                    color = tint,
                    start = Offset(w * 0.33f, h * 0.12f),
                    end = Offset(w * 0.33f, h * 0.28f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.67f, h * 0.12f),
                    end = Offset(w * 0.67f, h * 0.28f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                // Divider line
                drawLine(
                    color = tint,
                    start = Offset(w * 0.14f, h * 0.45f),
                    end = Offset(w * 0.86f, h * 0.45f),
                    strokeWidth = stroke.width
                )
            }
            "money" -> {
                // Circle
                drawCircle(
                    color = tint,
                    radius = w * 0.35f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = stroke
                )
                // Vertical line
                drawLine(
                    color = tint,
                    start = Offset(w * 0.5f, h * 0.32f),
                    end = Offset(w * 0.5f, h * 0.68f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                // Diagonal cross
                drawLine(
                    color = tint,
                    start = Offset(w * 0.38f, h * 0.42f),
                    end = Offset(w * 0.62f, h * 0.58f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }
            "list" -> {
                // 3 horizontal lines with left dots
                val y1 = h * 0.32f
                val y2 = h * 0.50f
                val y3 = h * 0.68f

                drawLine(color = tint, start = Offset(w * 0.35f, y1), end = Offset(w * 0.82f, y1), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(w * 0.35f, y2), end = Offset(w * 0.82f, y2), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(w * 0.35f, y3), end = Offset(w * 0.82f, y3), strokeWidth = stroke.width, cap = StrokeCap.Round)

                drawCircle(color = tint, radius = 1.5.dp.toPx(), center = Offset(w * 0.20f, y1))
                drawCircle(color = tint, radius = 1.5.dp.toPx(), center = Offset(w * 0.20f, y2))
                drawCircle(color = tint, radius = 1.5.dp.toPx(), center = Offset(w * 0.20f, y3))
            }
            "commute" -> {
                // Commute: Direction line + arrow + user circle
                drawLine(color = tint, start = Offset(w * 0.18f, h * 0.72f), end = Offset(w * 0.82f, h * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(w * 0.65f, h * 0.56f), end = Offset(w * 0.82f, h * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(w * 0.65f, h * 0.88f), end = Offset(w * 0.82f, h * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(color = tint, radius = w * 0.15f, center = Offset(w * 0.32f, h * 0.32f), style = stroke)
            }
            "health" -> {
                // Heart shape using bezier curves
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.78f)
                    cubicTo(w * 0.18f, h * 0.60f, w * 0.15f, h * 0.38f, w * 0.32f, h * 0.28f)
                    cubicTo(w * 0.44f, h * 0.21f, w * 0.5f, h * 0.32f, w * 0.5f, h * 0.38f)
                    cubicTo(w * 0.5f, h * 0.32f, w * 0.56f, h * 0.21f, w * 0.68f, h * 0.28f)
                    cubicTo(w * 0.85f, h * 0.38f, w * 0.82f, h * 0.60f, w * 0.5f, h * 0.78f)
                    close()
                }
                drawPath(path = path, color = tint, style = stroke)
            }
        }
    }
}
