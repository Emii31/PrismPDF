package com.prismpdf.editor.ui

import androidx.compose.ui.unit.sp


import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Renders the structural high-resolution canvas splash presentation animation for PrismPDF.
 */
@Composable
fun PrismPDFSplashAnimation(onAnimationComplete: () -> Unit) {
    val animationProgress = remember { Animatable(0f) }

    // Execute the linear animation sequences sequentially
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LinearOutSlowInEasing)
        )
        onAnimationComplete()
    }

    val progress = animationProgress.value

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = if (progress < 0.2f) progress * 5f else (1f - progress).coerceIn(0f, 1f)
            }
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f

        // Dynamic vector sizing logic mapping layout transformations scale values
        val boxWidth = 240f * progress
        val boxHeight = 320f * progress

        val left = centerX - (boxWidth / 2f)
        val top = centerY - (boxHeight / 2f)

        // Define the structural glassmorphic gradient path bounds
        val documentPath = Path().apply {
            moveTo(left, top)
            lineTo(left + (boxWidth * 0.7f), top)
            lineTo(left + boxWidth, top + (boxHeight * 0.22f)) // Isometric sheet cut corner
            lineTo(left + boxWidth, top + boxHeight)
            lineTo(left, top + boxHeight)
            close()
        }

        // Render the fluid geometric accent backdrop layer
        drawPath(
            path = documentPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0x8070A3FF), Color(0x1A94A3B8))
            )
        )

        // Draw the outer light-catching boundaries using high-precision strokes
        drawPath(
            path = documentPath,
            color = Color(0xCCFFFFFF),
            style = Stroke(width = 3f * progress)
        )

        // Draw standard simulated vector layout indicator items inside the sheet core
        val baselineStart = left + (boxWidth * 0.15f)
        val baselineEnd = left + (boxWidth * 0.85f)
        val firstLineY = top + (boxHeight * 0.4f)

        for (i in 0..2) {
            val currentLineY = firstLineY + (i * 35f * progress)
            drawLine(
                color = Color(0x66FFFFFF),
                start = androidx.compose.ui.geometry.Offset(baselineStart, currentLineY),
                end = androidx.compose.ui.geometry.Offset(baselineEnd, currentLineY),
                strokeWidth = 6f
            )
        }
    }
}
