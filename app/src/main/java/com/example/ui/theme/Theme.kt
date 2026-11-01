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
    onPrimary = NavyMedium,
    onSecondary = NavyMedium,
    onTertiary = NavyMedium,
    onBackground = WhiteActive,
    onSurface = WhiteActive,
    error = CoralRed,
    onError = NavyMedium,
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
