package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = DramaRed,
    onPrimary = Color.White,
    primaryContainer = DramaRedDark,
    onPrimaryContainer = Color.White,
    secondary = DramaRedLight,
    onSecondary = Color.White,
    background = DramaBlack,
    onBackground = DramaTextPrimary,
    surface = DramaSurface,
    onSurface = DramaTextPrimary,
    surfaceVariant = DramaSurfaceVariant,
    onSurfaceVariant = DramaTextSecondary,
    outline = DramaBorder
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

