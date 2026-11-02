package com.prismpdf.editor.ui

import androidx.compose.ui.graphics.Color

// Material 3 Liquid Glass Core Tokens
val GlassBackground = Color(0x2AFFFFFF)       // 15% Translucent Pure White Base
val GlassBorder = Color(0x54FFFFFF)           // 33% Translucent White for Light-Catching Edges
val LightSpecularGlow = Color(0x3D70A3FF)     // Refractive Ice Blue under-glass radial glow
val PrimaryText = Color(0xFF1A1C1E)
val SecondaryText = Color(0xFF41474D)

// Dark / Deep Acrylic System Alternates
val DarkGlassBackground = Color(0x1F0F172A)   // 12% Translucent Deep Slate Base
val DarkGlassBorder = Color(0x3394A3B8)       // Translucent Slate border frame

@androidx.compose.runtime.Composable
fun PrismPdfTypography() = androidx.compose.material3.MaterialTheme.typography

@androidx.compose.runtime.Composable
fun PrismPdfShapes() = androidx.compose.material3.MaterialTheme.shapes
