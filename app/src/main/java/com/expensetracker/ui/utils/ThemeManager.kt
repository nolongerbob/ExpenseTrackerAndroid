package com.expensetracker.ui.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

object AppColors {
    @Composable
    fun backgroundGradient(isLight: Boolean): Brush {
        return if (isLight) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF7F7F8),
                    Color(0xFFEBEBF0)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A1A26),
                    Color.Black
                )
            )
        }
    }
    
    @Composable
    fun primaryText(isLight: Boolean): Color {
        return if (isLight) Color.Black else Color.White
    }
    
    @Composable
    fun secondaryText(isLight: Boolean): Color {
        return if (isLight) Color(0xFF4D4D5A) else Color(0xFF999999)
    }
    
    @Composable
    fun cardBackground(isLight: Boolean): Color {
        return if (isLight) Color(0xFFD9D9DE) else Color.White.copy(alpha = 0.15f)
    }
    
    @Composable
    fun textFieldBackground(isLight: Boolean): Color {
        return if (isLight) Color(0xFFF5F5F7) else Color.White.copy(alpha = 0.1f)
    }
    
    @Composable
    fun textFieldText(isLight: Boolean): Color {
        return if (isLight) Color.Black else Color.White
    }
    
    @Composable
    fun cardBorder(isLight: Boolean): Color {
        return if (isLight) Color(0xFFD9D9E6) else Color.White.copy(alpha = 0.15f)
    }
}


