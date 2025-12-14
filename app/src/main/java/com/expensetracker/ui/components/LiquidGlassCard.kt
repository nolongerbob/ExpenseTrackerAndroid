package com.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.expensetracker.ui.utils.AppColors

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    isLight: Boolean = MaterialTheme.colorScheme.surface == Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.cardBackground(isLight))
            .border(
                width = if (isLight) 0.5.dp else 0.33.dp,
                brush = Brush.linearGradient(
                    colors = if (isLight) {
                        listOf(
                            Color(0xFFCCCCD9).copy(alpha = 0.6f),
                            Color(0xFFB3B3BF).copy(alpha = 0.4f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    }
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp),
        content = content
    )
}

