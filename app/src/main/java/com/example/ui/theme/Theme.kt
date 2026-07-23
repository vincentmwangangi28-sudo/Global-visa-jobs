package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CosmicColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    secondary = TealCyan,
    tertiary = AmberGold,
    background = NavyDark,
    surface = NavyMedium,
    onPrimary = NavyDark,
    onSecondary = NavyDark,
    onBackground = WhiteActive,
    onSurface = WhiteActive,
    error = CoralRed,
    onError = WhiteActive,
    outline = NavyLight
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicColorScheme,
        typography = Typography,
        content = content
    )
}
