package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CinemaDarkColorScheme = darkColorScheme(
    primary = OpticalCyan,
    onPrimary = OnOpticalCyan,
    primaryContainer = OpticalCyanDim,
    onPrimaryContainer = TextPrimary,
    secondary = TungstenAmber,
    onSecondary = OnTungstenAmber,
    secondaryContainer = TungstenAmberDim,
    onSecondaryContainer = TextPrimary,
    tertiary = SignalEmerald,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceHigh,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianOutline,
    error = AlertCoral
)

val CubeShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun CubeTunerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinemaDarkColorScheme,
        typography = Typography,
        shapes = CubeShapes,
        content = content
    )
}
