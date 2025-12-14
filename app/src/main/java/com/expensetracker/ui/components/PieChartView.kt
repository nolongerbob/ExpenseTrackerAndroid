package com.expensetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.data.models.Category
import com.expensetracker.ui.utils.AppColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min

data class PieChartData(
    val category: Category,
    val total: Double,
    val percentage: Double
)

@Composable
fun PieChartView(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    isLight: Boolean? = null
) {
    if (data.isEmpty()) return
    
    val isLightValue = isLight ?: (MaterialTheme.colorScheme.surface == Color.White) // Вычисляем внутри @Composable
    val surfaceColor = MaterialTheme.colorScheme.surface // Вычисляем заранее, чтобы использовать в Canvas
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = min(size.width, size.height) / 2f - 20.dp.toPx()
            val innerRadius = radius * 0.75f
            val center = Offset(size.width / 2f, size.height / 2f)
            
            var currentAngle = -90f // Начинаем сверху
            
            data.forEach { item ->
                val sweepAngle = (item.percentage.toFloat() / 100f) * 360f
                
                // Рисуем сегмент
                val path = Path().apply {
                    val startX = center.x + innerRadius * cos(Math.toRadians(currentAngle.toDouble())).toFloat()
                    val startY = center.y + innerRadius * sin(Math.toRadians(currentAngle.toDouble())).toFloat()
                    moveTo(startX, startY)
                    
                    // Внутренняя дуга
                    arcTo(
                        rect = Rect(
                            center = center,
                            radius = innerRadius
                        ),
                        startAngleDegrees = currentAngle,
                        sweepAngleDegrees = sweepAngle,
                        forceMoveTo = false
                    )
                    
                    // Линия к внешней дуге
                    val endX = center.x + radius * cos(Math.toRadians((currentAngle + sweepAngle).toDouble())).toFloat()
                    val endY = center.y + radius * sin(Math.toRadians((currentAngle + sweepAngle).toDouble())).toFloat()
                    lineTo(endX, endY)
                    
                    // Внешняя дуга (обратно)
                    arcTo(
                        rect = Rect(
                            center = center,
                            radius = radius
                        ),
                        startAngleDegrees = currentAngle + sweepAngle,
                        sweepAngleDegrees = -sweepAngle,
                        forceMoveTo = false
                    )
                    
                    close()
                }
                
                drawPath(
                    path = path,
                    color = item.category.getColor()
                )
                
                // Обводка
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.15f),
                    style = Stroke(width = 3.dp.toPx())
                )
                
                currentAngle += sweepAngle
            }
            
            // Центральный круг
            drawCircle(
                color = surfaceColor.copy(alpha = 0.9f),
                radius = innerRadius
            )
            
            // Текст в центре
            val total = data.sumOf { it.total }
            val totalText = formatCurrency(total)
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.apply {
                    save()
                    translate(center.x, center.y)
                    val textPaint = android.graphics.Paint().apply {
                        color = if (isLightValue) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                        textSize = 14.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    drawText("Всего", 0f, -8.dp.toPx(), textPaint)
                    textPaint.textSize = 20.dp.toPx()
                    drawText(totalText, 0f, 16.dp.toPx(), textPaint)
                    restore()
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("ru", "RU")).apply {
        currency = java.util.Currency.getInstance("RUB")
        maximumFractionDigits = 0
    }
    return formatter.format(amount)
}

