package com.prismpdf.editor.ui

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.prismpdf.editor.graphics.PDFCoordinateMapper
import com.prismpdf.editor.graphics.PdfPageRenderer

/**
 * High-precision Jetpack Compose overlay canvas implementing the hybrid architecture:
 *
 *   LAYER 1: BASE RASTER ENGINE — high-density immutable Bitmap cache drawn via unified Matrix.
 *   LAYER 2: INPAINTING & PATCH MASK — background texture synthesis (OpenCV) over targeted regions.
 *   LAYER 3: COORDINATE-MAPPED INTERACTIVE VECTOR OVERLAY — vector bounding boxes, text, and image
 *           overlays anchored to exact PDF point coordinates.
 */
@Composable
fun PassportEditorCanvas(
    backgroundBitmap: Bitmap,
    textElements: List<EditableTextElement>,
    imageElements: List<EditableImageElement>,
    mapper: PDFCoordinateMapper,
    onBoundsChanged: ((RectF) -> Unit)? = null
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    LaunchedEffect(scale, offsetX, offsetY) {
        mapper.updateMatrix(scale, offsetX, offsetY)
    }

    val onBoundsChangedState = rememberUpdatedState(onBoundsChanged)
    LaunchedEffect(mapper) {
        onBoundsChangedState.value?.invoke(
            RectF(
                -offsetX / scale,
                -offsetY / scale,
                (backgroundBitmap.width - offsetX) / scale,
                (backgroundBitmap.height - offsetY) / scale
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 15.0f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.setMatrix(mapper.transformMatrix)
            drawContext.canvas.nativeCanvas.drawBitmap(backgroundBitmap, 0f, 0f, null)

            textElements.forEach { element ->
                val screenPos = mapper.pdfToScreen(element.pdfX, element.pdfY)
                val dynamicScale = mapper.getScale()
                drawContext.canvas.nativeCanvas.apply {
                    val paint = Paint().apply {
                        color = element.color
                        textSize = element.fontSizePdfPoints * dynamicScale
                        typeface = element.extractedTypeface ?: Typeface.DEFAULT
                        isAntiAlias = true
                        isSubpixelText = true
                        linearText = true
                    }
                    drawText(element.text, screenPos.x, screenPos.y, paint)
                }
            }

            imageElements.forEach { element ->
                val screenPos = mapper.pdfToScreen(element.pdfX, element.pdfY)
                val dynamicScale = mapper.getScale()
                val screenWidth = element.pdfWidth * dynamicScale
                val screenHeight = element.pdfHeight * dynamicScale

                drawContext.canvas.nativeCanvas.save()
                if (element.rotationDegrees != 0f) {
                    drawContext.canvas.nativeCanvas.rotate(
                        element.rotationDegrees,
                        screenPos.x,
                        screenPos.y
                    )
                }
                drawContext.canvas.nativeCanvas.drawImage(
                    element.bitmap,
                    screenPos.x,
                    screenPos.y,
                    screenPos.x + screenWidth,
                    screenPos.y + screenHeight,
                    null
                )
                drawContext.canvas.nativeCanvas.restore()
            }

            drawContext.canvas.nativeCanvas.restore()
        }
    }
}

@Composable
fun PassportEditorWithSelection(
    pageResult: PdfPageRenderer.PageRenderResult,
    textElements: List<EditableTextElement>,
    imageElements: List<EditableImageElement>,
    selectedBounds: android.graphics.Rect?,
    mapper: PDFCoordinateMapper,
    onBoundsChanged: ((RectF) -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize()) {
        PassportEditorCanvas(
            backgroundBitmap = pageResult.bitmap,
            textElements = textElements,
            imageElements = imageElements,
            mapper = mapper,
            onBoundsChanged = onBoundsChanged
        )

        selectedBounds?.let { sel ->
            Canvas(modifier = Modifier.fillMaxSize()) {
                val screenRect = mapper.pdfToScreen(
                    sel.left.toFloat(),
                    sel.top.toFloat()
                )
                val screenRight = mapper.pdfToScreen(
                    sel.right.toFloat(),
                    sel.top.toFloat()
                ).x
                val screenBottom = mapper.pdfToScreen(
                    sel.left.toFloat(),
                    sel.bottom.toFloat()
                ).y

                val rect = RectF(
                    screenRect.x,
                    screenRect.y,
                    screenRight,
                    screenBottom
                )

                drawRect(
                    color = android.graphics.Color.argb(100, 255, 0, 0),
                    topLeft = rect.topLeft,
                    size = rect.size
                )
            }
        }
    }
}
