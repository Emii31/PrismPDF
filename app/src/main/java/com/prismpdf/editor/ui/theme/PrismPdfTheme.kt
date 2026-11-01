package com.prismpdf.editor.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.prismpdf.editor.ui.components.liquidGlassCard

/**
 * Material 3 Liquid Glass theme for PrismPDF.
 *
 * Uses a frosted-glass color scheme with translucent surfaces, specular borders,
 * and under-glass radial glow. Surfaces respect the GlassBackground translucency
 * to maintain depth illusion.
 */
@Composable
fun PrismPdfTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = ColorScheme(
        primary = LightSpecularGlow,
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0x4D70A3FF),
        onPrimaryContainer = Color(0xFF1A1C1E),
        secondary = Color(0xFF94A3B8),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0x2A94A3B8),
        onSecondaryContainer = Color(0xFF1A1C1E),
        tertiary = Color(0xFFB0C4DE),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0x2AB0C4DE),
        onTertiaryContainer = Color(0xFF1A1C1E),
        background = Color(0x2AFFFFFF),
        onBackground = PrimaryText,
        surface = GlassBackground,
        onSurface = PrimaryText,
        surfaceVariant = DarkGlassBackground,
        onSurfaceVariant = Color(0xFFB0B8C4),
        surfaceTint = LightSpecularGlow,
        inverseSurface = DarkGlassBackground,
        inverseOnSurface = Color(0xFFE8E8F0),
        inversePrimary = Color(0xFF70A3FF),
        error = Color(0xFFFF6B6B),
        onError = Color(0xFF000000),
        errorContainer = Color(0x40FF6B6B),
        onErrorContainer = Color(0xFF1A1C1E),
        outline = GlassBorder,
        outlineVariant = DarkGlassBorder,
        scrim = Color(0x80000000)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = com.prismpdf.editor.ui.theme.PrismPdfTypography(),
        shapes = com.prismpdf.editor.ui.theme.PrismPdfShapes(),
        content = content
    )
}

@Composable
fun PrismPdfTypography() = MaterialTheme.typography

@Composable
fun PrismPdfShapes() = MaterialTheme.shapes

// Convenience wrapper for the liquid glass card
fun Modifier.liquidGlassCard(shape: androidx.compose.foundation.shape.RoundedCornerShape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)): Modifier =
    com.prismpdf.editor.ui.components.liquidGlassCard(shape)