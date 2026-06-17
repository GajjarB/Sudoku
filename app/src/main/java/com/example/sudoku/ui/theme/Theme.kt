package com.example.sudoku.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

private val EInkColorScheme = lightColorScheme(
    background = EInkBackground,
    surface = EInkSurface,
    onBackground = EInkText,
    onSurface = EInkText,
    primary = EInkAccent,
    error = EInkError
)

private val NordicColorScheme = lightColorScheme(
    background = NordicBackground,
    surface = NordicSurface,
    onBackground = NordicText,
    onSurface = NordicText,
    primary = NordicAccent,
    error = NordicError
)

private val CyberColorScheme = darkColorScheme(
    background = CyberBackground,
    surface = CyberSurface,
    onBackground = CyberText,
    onSurface = CyberText,
    primary = CyberAccent,
    error = CyberError
)

private val ChocoColorScheme = lightColorScheme(
    background = ChocoBackground,
    surface = ChocoSurface,
    onBackground = ChocoText,
    onSurface = ChocoText,
    primary = ChocoAccent,
    error = ChocoError
)

@Composable
fun SudokuTheme(
    themeName: String = "E-Ink",
    content: @Composable () -> Unit
) {
    val targetColors = when (themeName) {
        "Nordic" -> NordicColorScheme
        "Cyberpunk" -> CyberColorScheme
        "Chocolatier" -> ChocoColorScheme
        else -> EInkColorScheme
    }

    val background by animateColorAsState(targetValue = targetColors.background, animationSpec = tween(500), label = "background")
    val surface by animateColorAsState(targetValue = targetColors.surface, animationSpec = tween(500), label = "surface")
    val onBackground by animateColorAsState(targetValue = targetColors.onBackground, animationSpec = tween(500), label = "onBackground")
    val onSurface by animateColorAsState(targetValue = targetColors.onSurface, animationSpec = tween(500), label = "onSurface")
    val primary by animateColorAsState(targetValue = targetColors.primary, animationSpec = tween(500), label = "primary")
    val error by animateColorAsState(targetValue = targetColors.error, animationSpec = tween(500), label = "error")

    val animatedColors = targetColors.copy(
        background = background,
        surface = surface,
        onBackground = onBackground,
        onSurface = onSurface,
        primary = primary,
        error = error
    )

    MaterialTheme(
        colorScheme = animatedColors,
        content = content
    )
}
