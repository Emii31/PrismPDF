package com.prismpdf.editor.ui

import androidx.compose.ui.unit.sp


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
// import com.prismpdf.editor.GlassBackground
// import com.prismpdf.editor.GlassBorder

/**
 * Applies a high-fidelity Liquid Glass container transformation with radial gloss styling and specular border framing.
 */
fun Modifier.liquidGlassCard(shape: RoundedCornerShape = RoundedCornerShape(24.dp)): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x3DFFFFFF), Color(0x14FFFFFF)),
            radius = 600f
        )
    )
    .background(GlassBackground)
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(GlassBorder, Color(0x00FFFFFF)) // Vertical gradient captures top-down ambient light
        ),
        shape = shape
    )
