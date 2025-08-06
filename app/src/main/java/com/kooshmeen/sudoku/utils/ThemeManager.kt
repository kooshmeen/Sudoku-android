/**
 * Theme manager for handling custom color schemes and theme persistence
 * Includes color contrast validation to prevent UI lockout scenarios
 */

package com.kooshmeen.sudoku.utils

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import com.kooshmeen.sudoku.ui.theme.DefaultColors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object ThemeManager {
    private const val PREFS_NAME = "SudokuTheme"
    private const val MIN_CONTRAST_RATIO = 3.0f // Minimum contrast ratio for accessibility

    // Keys for storing theme colors
    private const val KEY_LIGHT_PRIMARY = "light_primary"
    private const val KEY_LIGHT_SECONDARY = "light_secondary"
    private const val KEY_LIGHT_TERTIARY = "light_tertiary"
    private const val KEY_LIGHT_BACKGROUND = "light_background"
    private const val KEY_LIGHT_SURFACE = "light_surface"
    private const val KEY_LIGHT_ON_PRIMARY = "light_on_primary"
    private const val KEY_LIGHT_ON_SECONDARY = "light_on_secondary"
    private const val KEY_LIGHT_ON_TERTIARY = "light_on_tertiary"
    private const val KEY_LIGHT_ON_BACKGROUND = "light_on_background"
    private const val KEY_LIGHT_ON_SURFACE = "light_on_surface"

    private const val KEY_DARK_PRIMARY = "dark_primary"
    private const val KEY_DARK_SECONDARY = "dark_secondary"
    private const val KEY_DARK_TERTIARY = "dark_tertiary"
    private const val KEY_DARK_BACKGROUND = "dark_background"
    private const val KEY_DARK_SURFACE = "dark_surface"
    private const val KEY_DARK_ON_PRIMARY = "dark_on_primary"
    private const val KEY_DARK_ON_SECONDARY = "dark_on_secondary"
    private const val KEY_DARK_ON_TERTIARY = "dark_on_tertiary"
    private const val KEY_DARK_ON_BACKGROUND = "dark_on_background"
    private const val KEY_DARK_ON_SURFACE = "dark_on_surface"

    data class CustomTheme(
        // Light theme colors
        val lightPrimary: Color,
        val lightSecondary: Color,
        val lightTertiary: Color,
        val lightBackground: Color,
        val lightSurface: Color,
        val lightOnPrimary: Color,
        val lightOnSecondary: Color,
        val lightOnTertiary: Color,
        val lightOnBackground: Color,
        val lightOnSurface: Color,

        // Dark theme colors
        val darkPrimary: Color,
        val darkSecondary: Color,
        val darkTertiary: Color,
        val darkBackground: Color,
        val darkSurface: Color,
        val darkOnPrimary: Color,
        val darkOnSecondary: Color,
        val darkOnTertiary: Color,
        val darkOnBackground: Color,
        val darkOnSurface: Color
    )

    /**
     * Calculate contrast ratio between two colors
     */
    private fun calculateContrastRatio(color1: Color, color2: Color): Float {
        val luminance1 = color1.luminance()
        val luminance2 = color2.luminance()
        val lighter = max(luminance1, luminance2)
        val darker = min(luminance1, luminance2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    /**
     * Validate color combination to prevent UI lockout
     */
    fun validateColorCombination(backgroundColor: Color, onBackgroundColor: Color, surfaceColor: Color, onSurfaceColor: Color): ValidationResult {
        val backgroundContrast = calculateContrastRatio(backgroundColor, onBackgroundColor)
        val surfaceContrast = calculateContrastRatio(surfaceColor, onSurfaceColor)

        val issues = mutableListOf<String>()

        if (backgroundContrast < MIN_CONTRAST_RATIO) {
            issues.add("Background and text colors have insufficient contrast (${String.format("%.1f", backgroundContrast)}:1, minimum: ${MIN_CONTRAST_RATIO}:1)")
        }

        if (surfaceContrast < MIN_CONTRAST_RATIO) {
            issues.add("Surface and text colors have insufficient contrast (${String.format("%.1f", surfaceContrast)}:1, minimum: ${MIN_CONTRAST_RATIO}:1)")
        }

        // Additional check for very similar colors
        val backgroundSimilarity = abs(backgroundColor.luminance() - onBackgroundColor.luminance())
        if (backgroundSimilarity < 0.1f) {
            issues.add("Background and text colors are too similar")
        }

        return ValidationResult(
            isValid = issues.isEmpty(),
            issues = issues
        )
    }

    /**
     * Auto-adjust colors to ensure accessibility
     */
    fun ensureAccessibleColors(backgroundColor: Color, textColor: Color): Pair<Color, Color> {
        val contrast = calculateContrastRatio(backgroundColor, textColor)

        if (contrast >= MIN_CONTRAST_RATIO) {
            return Pair(backgroundColor, textColor)
        }

        // Adjust text color to ensure sufficient contrast
        val backgroundLuminance = backgroundColor.luminance()
        val adjustedTextColor = if (backgroundLuminance > 0.5f) {
            Color.Black // Use black text on light backgrounds
        } else {
            Color.White // Use white text on dark backgrounds
        }

        return Pair(backgroundColor, adjustedTextColor)
    }

    /**
     * Save custom theme to SharedPreferences
     */
    fun saveCustomTheme(context: Context, theme: CustomTheme) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            // Light theme colors
            putInt(KEY_LIGHT_PRIMARY, theme.lightPrimary.toArgb())
            putInt(KEY_LIGHT_SECONDARY, theme.lightSecondary.toArgb())
            putInt(KEY_LIGHT_TERTIARY, theme.lightTertiary.toArgb())
            putInt(KEY_LIGHT_BACKGROUND, theme.lightBackground.toArgb())
            putInt(KEY_LIGHT_SURFACE, theme.lightSurface.toArgb())
            putInt(KEY_LIGHT_ON_PRIMARY, theme.lightOnPrimary.toArgb())
            putInt(KEY_LIGHT_ON_SECONDARY, theme.lightOnSecondary.toArgb())
            putInt(KEY_LIGHT_ON_TERTIARY, theme.lightOnTertiary.toArgb())
            putInt(KEY_LIGHT_ON_BACKGROUND, theme.lightOnBackground.toArgb())
            putInt(KEY_LIGHT_ON_SURFACE, theme.lightOnSurface.toArgb())

            // Dark theme colors
            putInt(KEY_DARK_PRIMARY, theme.darkPrimary.toArgb())
            putInt(KEY_DARK_SECONDARY, theme.darkSecondary.toArgb())
            putInt(KEY_DARK_TERTIARY, theme.darkTertiary.toArgb())
            putInt(KEY_DARK_BACKGROUND, theme.darkBackground.toArgb())
            putInt(KEY_DARK_SURFACE, theme.darkSurface.toArgb())
            putInt(KEY_DARK_ON_PRIMARY, theme.darkOnPrimary.toArgb())
            putInt(KEY_DARK_ON_SECONDARY, theme.darkOnSecondary.toArgb())
            putInt(KEY_DARK_ON_TERTIARY, theme.darkOnTertiary.toArgb())
            putInt(KEY_DARK_ON_BACKGROUND, theme.darkOnBackground.toArgb())
            putInt(KEY_DARK_ON_SURFACE, theme.darkOnSurface.toArgb())
        }
    }

    /**
     * Load custom theme from SharedPreferences
     */
    fun loadCustomTheme(context: Context): CustomTheme {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        return CustomTheme(
            // Light theme colors
            lightPrimary = Color(prefs.getInt(KEY_LIGHT_PRIMARY, DefaultColors.LightPrimary.toArgb())),
            lightSecondary = Color(prefs.getInt(KEY_LIGHT_SECONDARY, DefaultColors.LightSecondary.toArgb())),
            lightTertiary = Color(prefs.getInt(KEY_LIGHT_TERTIARY, DefaultColors.LightTertiary.toArgb())),
            lightBackground = Color(prefs.getInt(KEY_LIGHT_BACKGROUND, DefaultColors.LightBackground.toArgb())),
            lightSurface = Color(prefs.getInt(KEY_LIGHT_SURFACE, DefaultColors.LightSurface.toArgb())),
            lightOnPrimary = Color(prefs.getInt(KEY_LIGHT_ON_PRIMARY, DefaultColors.LightOnPrimary.toArgb())),
            lightOnSecondary = Color(prefs.getInt(KEY_LIGHT_ON_SECONDARY, DefaultColors.LightOnSecondary.toArgb())),
            lightOnTertiary = Color(prefs.getInt(KEY_LIGHT_ON_TERTIARY, DefaultColors.LightOnTertiary.toArgb())),
            lightOnBackground = Color(prefs.getInt(KEY_LIGHT_ON_BACKGROUND, DefaultColors.LightOnBackground.toArgb())),
            lightOnSurface = Color(prefs.getInt(KEY_LIGHT_ON_SURFACE, DefaultColors.LightOnSurface.toArgb())),

            // Dark theme colors
            darkPrimary = Color(prefs.getInt(KEY_DARK_PRIMARY, DefaultColors.DarkPrimary.toArgb())),
            darkSecondary = Color(prefs.getInt(KEY_DARK_SECONDARY, DefaultColors.DarkSecondary.toArgb())),
            darkTertiary = Color(prefs.getInt(KEY_DARK_TERTIARY, DefaultColors.DarkTertiary.toArgb())),
            darkBackground = Color(prefs.getInt(KEY_DARK_BACKGROUND, DefaultColors.DarkBackground.toArgb())),
            darkSurface = Color(prefs.getInt(KEY_DARK_SURFACE, DefaultColors.DarkSurface.toArgb())),
            darkOnPrimary = Color(prefs.getInt(KEY_DARK_ON_PRIMARY, DefaultColors.DarkOnPrimary.toArgb())),
            darkOnSecondary = Color(prefs.getInt(KEY_DARK_ON_SECONDARY, DefaultColors.DarkOnSecondary.toArgb())),
            darkOnTertiary = Color(prefs.getInt(KEY_DARK_ON_TERTIARY, DefaultColors.DarkOnTertiary.toArgb())),
            darkOnBackground = Color(prefs.getInt(KEY_DARK_ON_BACKGROUND, DefaultColors.DarkOnBackground.toArgb())),
            darkOnSurface = Color(prefs.getInt(KEY_DARK_ON_SURFACE, DefaultColors.DarkOnSurface.toArgb()))
        )
    }

    /**
     * Reset theme to defaults
     */
    fun resetToDefaults(context: Context) {
        val defaultTheme = CustomTheme(
            lightPrimary = DefaultColors.LightPrimary,
            lightSecondary = DefaultColors.LightSecondary,
            lightTertiary = DefaultColors.LightTertiary,
            lightBackground = DefaultColors.LightBackground,
            lightSurface = DefaultColors.LightSurface,
            lightOnPrimary = DefaultColors.LightOnPrimary,
            lightOnSecondary = DefaultColors.LightOnSecondary,
            lightOnTertiary = DefaultColors.LightOnTertiary,
            lightOnBackground = DefaultColors.LightOnBackground,
            lightOnSurface = DefaultColors.LightOnSurface,

            darkPrimary = DefaultColors.DarkPrimary,
            darkSecondary = DefaultColors.DarkSecondary,
            darkTertiary = DefaultColors.DarkTertiary,
            darkBackground = DefaultColors.DarkBackground,
            darkSurface = DefaultColors.DarkSurface,
            darkOnPrimary = DefaultColors.DarkOnPrimary,
            darkOnSecondary = DefaultColors.DarkOnSecondary,
            darkOnTertiary = DefaultColors.DarkOnTertiary,
            darkOnBackground = DefaultColors.DarkOnBackground,
            darkOnSurface = DefaultColors.DarkOnSurface
        )
        saveCustomTheme(context, defaultTheme)
    }

    data class ValidationResult(
        val isValid: Boolean,
        val issues: List<String>
    )
}
