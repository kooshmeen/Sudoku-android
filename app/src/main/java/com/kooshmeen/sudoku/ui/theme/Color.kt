package com.kooshmeen.sudoku.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Default color palette
object DefaultColors {
    // Light theme defaults
    val LightPrimary = Purple40
    val LightSecondary = PurpleGrey40
    val LightTertiary = Pink40
    val LightBackground = Color(0xFFFFFBFE)
    val LightSurface = Color(0xFFFFFBFE)
    val LightOnPrimary = Color.White
    val LightOnSecondary = Color.White
    val LightOnTertiary = Color.White
    val LightOnBackground = Color(0xFF1C1B1F)
    val LightOnSurface = Color(0xFF1C1B1F)

    // Dark theme defaults
    val DarkPrimary = Purple80
    val DarkSecondary = PurpleGrey80
    val DarkTertiary = Pink80
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkOnPrimary = Color.Black
    val DarkOnSecondary = Color.Black
    val DarkOnTertiary = Color.Black
    val DarkOnBackground = Color.White
    val DarkOnSurface = Color.White
}

// Predefined color palettes for easy selection
object ColorPalettes {
    val Blue = mapOf(
        "primary" to Color(0xFF1976D2),
        "secondary" to Color(0xFF1565C0),
        "tertiary" to Color(0xFF0D47A1)
    )

    val Green = mapOf(
        "primary" to Color(0xFF388E3C),
        "secondary" to Color(0xFF2E7D32),
        "tertiary" to Color(0xFF1B5E20)
    )

    val Orange = mapOf(
        "primary" to Color(0xFFFF9800),
        "secondary" to Color(0xFFE65100),
        "tertiary" to Color(0xFFBF360C)
    )

    val Red = mapOf(
        "primary" to Color(0xFFD32F2F),
        "secondary" to Color(0xFFC62828),
        "tertiary" to Color(0xFFB71C1C)
    )

    val Teal = mapOf(
        "primary" to Color(0xFF00796B),
        "secondary" to Color(0xFF00695C),
        "tertiary" to Color(0xFF004D40)
    )
}
