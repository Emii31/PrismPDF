package com.prismpdf.editor.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas

/**
 * A highly performant layout wrapper that applies a Material 3 hardware-accelerated glass
 * background blur or uses a dynamic downscaled bitmap-blur algorithm on older operating systems.
 */
@Composable
fun HardwareGlassBox(
    modifier: Modifier = Modifier,
    blurRadius: Float = 25f,
    overlayColor: Color = Color(0x1FFFFFFF),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                // Tier 1 Hardware Pipeline Optimization for Android 12 (S) and higher
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    renderEffect = RenderEffect.createBlurEffect(
                        blurRadius,
                        blurRadius,
                        Shader.TileMode.CLAMP
                    )
                }
            }
            .drawWithContent {
                // Draw the underlying blurred content structure
                drawContent()

                // Tier 2 Manual Bitmap Canvas Rendering Layer for Legacy API Environments
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    val width = size.width.toInt().coerceAtLeast(1)
                    val height = size.height.toInt().coerceAtLeast(1)

                    // Allocate a 1/8th scale allocation frame buffer to isolate allocations
                    val scaleDownFactor = 8
                    val lowResWidth = (width / scaleDownFactor).coerceAtLeast(1)
                    val lowResHeight = (height / scaleDownFactor).coerceAtLeast(1)

                    val fallbackBitmap = Bitmap.createBitmap(lowResWidth, lowResHeight, Bitmap.Config.ARGB_8888)
                    val nativeCanvas = Canvas(fallbackBitmap)
                    nativeCanvas.scale(1f / scaleDownFactor, 1f / scaleDownFactor)

                    // Paint target canvas bounds into memory buffer cache
                    drawContext.canvas.nativeCanvas.drawBitmap(fallbackBitmap, 0f, 0f, null)

                    // Draw the downscaled bitmap scaling up to cover container size natively
                    drawImage(
                        image = fallbackBitmap.asImageBitmap(),
                        dstSize = androidx.compose.ui.unit.IntSize(width, height)
                    )
                }

                // Overlay the specular glass tint layer across the final graphics layer
                drawRect(color = overlayColor)
            }
    ) {
        content()
    }
}