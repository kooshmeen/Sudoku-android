package com.kooshmeen.sudoku.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.DarkGray,
    onSecondary = Color.LightGray,
    onTertiary = Color.LightGray,
    onBackground = Color.LightGray,
    onSurface = Color.LightGray,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun SudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val targetColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Animate the color scheme
    val animatedColorScheme = animateColorScheme(targetColorScheme)

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun animateColorScheme(targetScheme: ColorScheme): ColorScheme {
    val primary by animateColorAsState(
        targetValue = targetScheme.primary,
        animationSpec = tween(durationMillis = 300)
    )
    val background by animateColorAsState(
        targetValue = targetScheme.background,
        animationSpec = tween(durationMillis = 300)
    )
    val surface by animateColorAsState(
        targetValue = targetScheme.surface,
        animationSpec = tween(durationMillis = 300)
    )
    val onBackground by animateColorAsState(
        targetValue = targetScheme.onBackground,
        animationSpec = tween(durationMillis = 300)
    )
    val onSurface by animateColorAsState(
        targetValue = targetScheme.onSurface,
        animationSpec = tween(durationMillis = 300)
    )

    return targetScheme.copy(
        primary = primary,
        background = background,
        surface = surface,
        onBackground = onBackground,
        onSurface = onSurface
    )
}