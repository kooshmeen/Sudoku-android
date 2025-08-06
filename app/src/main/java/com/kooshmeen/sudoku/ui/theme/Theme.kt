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
import com.kooshmeen.sudoku.utils.ThemeManager

@Composable
fun SudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val customTheme = ThemeManager.loadCustomTheme(context)

    val targetColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = customTheme.darkPrimary,
            secondary = customTheme.darkSecondary,
            tertiary = customTheme.darkTertiary,
            background = customTheme.darkBackground,
            surface = customTheme.darkSurface,
            onPrimary = customTheme.darkOnPrimary,
            onSecondary = customTheme.darkOnSecondary,
            onTertiary = customTheme.darkOnTertiary,
            onBackground = customTheme.darkOnBackground,
            onSurface = customTheme.darkOnSurface
        )
        else -> lightColorScheme(
            primary = customTheme.lightPrimary,
            secondary = customTheme.lightSecondary,
            tertiary = customTheme.lightTertiary,
            background = customTheme.lightBackground,
            surface = customTheme.lightSurface,
            onPrimary = customTheme.lightOnPrimary,
            onSecondary = customTheme.lightOnSecondary,
            onTertiary = customTheme.lightOnTertiary,
            onBackground = customTheme.lightOnBackground,
            onSurface = customTheme.lightOnSurface
        )
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