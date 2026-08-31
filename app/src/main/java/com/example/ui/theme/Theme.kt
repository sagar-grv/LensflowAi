package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LensFlowStandardDarkScheme = darkColorScheme(
    primary = PrimaryAmber,
    onPrimary = OnPrimaryAmber,
    primaryContainer = PrimaryContainerAmber,
    onPrimaryContainer = OnPrimaryContainerAmber,
    secondary = SecondaryTeal,
    onSecondary = OnSecondaryTeal,
    secondaryContainer = SecondaryContainerTeal,
    onSecondaryContainer = OnSecondaryContainerTeal,
    tertiary = TertiaryCyan,
    onTertiary = OnTertiaryCyan,
    tertiaryContainer = TertiaryContainerCyan,
    onTertiaryContainer = OnTertiaryContainerCyan,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LensFlowRedLightDarkScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = RedOnPrimary,
    primaryContainer = RedPrimaryContainer,
    onPrimaryContainer = RedOnPrimaryContainer,
    secondary = RedSecondary,
    onSecondary = RedOnSecondary,
    secondaryContainer = RedSecondaryContainer,
    onSecondaryContainer = RedOnSecondaryContainer,
    tertiary = RedPrimary,
    onTertiary = RedOnPrimary,
    background = RedBackground,
    onBackground = RedOnSurface,
    surface = RedSurface,
    onSurface = RedOnSurface,
    surfaceVariant = Color(0xFF401818),
    onSurfaceVariant = Color(0xFFE0AAAA),
    surfaceContainer = RedSurfaceContainer,
    surfaceContainerHigh = Color(0xFF361414),
    surfaceContainerHighest = Color(0xFF441919),
    outline = RedOutline,
    outlineVariant = Color(0xFF5A2525)
)

val LensFlowShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun LensFlowTheme(
    isRedLight: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isRedLight) LensFlowRedLightDarkScheme else LensFlowStandardDarkScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = LensFlowShapes,
        typography = Typography(),
        content = content
    )
}
