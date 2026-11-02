package com.prismpdf.editor.ui.screens

import android.graphics.PointF
import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prismpdf.editor.PDFCoordinateMapper
import com.prismpdf.editor.SubPixelSnappingEngine
import com.prismpdf.editor.PassportEditorCanvas
import com.prismpdf.editor.InteractiveTextOverlayWidget
import com.prismpdf.editor.MultiPageThumbnailCarousel
import com.prismpdf.editor.ui.liquidGlassCard
import com.prismpdf.editor.DocumentSessionState
import com.prismpdf.editor.DocumentSessionViewModel

enum class EditorToolMode {
    PAN_AND_ZOOM,
    CORNER_TEXT_INJECTOR
}

@Composable
fun PrismPDFEditorScreen(
    sessionWorkspace: DocumentSessionState.ActiveWorkspace,
    viewModel: DocumentSessionViewModel,
    onExitWorkspace: () -> Unit
) {
    val mapper = remember { PDFCoordinateMapper() }
    val snappingEngine = remember { SubPixelSnappingEngine() }
    var currentToolMode by remember { mutableStateOf(EditorToolMode.PAN_AND_ZOOM) }
    var activeInjectingElement by remember { mutableStateOf<com.prismpdf.editor.EditableTextElement?>(null) }

    LaunchedEffect(Unit) {
        mapper.updateMatrix(1f, 0f, 0f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Canvas Interaction Layer with strict safety bounds
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentToolMode) {
                    if (currentToolMode == EditorToolMode.CORNER_TEXT_INJECTOR) {
                        detectTapGestures { offset ->
                            // 1. Map touch pixel layout positions to exact physical PDF points
                            val clickPdfPoint = mapper.screenToPdf(offset.x, offset.y)

                            // 2. Query the spatial index mapper to find out exactly what text node is under the touch coordinate
                            val matchedNode = viewModel.spatialTextIndexer.findTextElementAt(clickPdfPoint)

                            if (matchedNode != null) {
                                // Apply precision snapping corrections to line vector targets
                                val snappedPdfPoint = try {
                                    snappingEngine.computeSnappedPdfCoordinates(clickPdfPoint, sessionWorkspace.edgeTrackingMask, mapper)
                                } catch (e: Exception) {
                                    clickPdfPoint
                                }

                                val pixelLocation = mapper.pdfToScreen(snappedPdfPoint.x, snappedPdfPoint.y)
                                val targetInpaintBox = Rect(
                                    (pixelLocation.x - 40).toInt().coerceAtLeast(0),
                                    (pixelLocation.y - 25).toInt().coerceAtLeast(0),
                                    (pixelLocation.x + 140).toInt().coerceAtMost(sessionWorkspace.baseRaster.width),
                                    (pixelLocation.y + 20).toInt().coerceAtMost(sessionWorkspace.baseRaster.height)
                                )

                                // 3. Hydrate the inline text overlay pre-filled with the TRUE string variable extracted from the index
                                activeInjectingElement = com.prismpdf.editor.EditableTextElement(
                                    text = matchedNode.text, // Pre-populates the input field with the text that was already there!
                                    pdfX = snappedPdfPoint.x,
                                    pdfY = snappedPdfPoint.y,
                                    fontSizePdfPoints = matchedNode.fontSizePoints,
                                    extractedTypeface = null
                                )

                                // 4. Fire background task to cleanly wipe old pixels and update vector overlays
                                viewModel.executeSemanticTextModification(
                                    targetRect = targetInpaintBox,
                                    matchedNode = matchedNode,
                                    updatedString = matchedNode.text,
                                    pdfX = snappedPdfPoint.x,
                                    pdfY = snappedPdfPoint.y
                                )
                            }
                        }
                    }
                }
        ) {
            PassportEditorCanvas(
                backgroundBitmap = sessionWorkspace.baseRaster,
                textElements = sessionWorkspace.overlays,
                imageElements = emptyList(),
                mapper = mapper
            )
        }

        // Live text input injector mapping focus loops
        activeInjectingElement?.let { element ->
            InteractiveTextOverlayWidget(
                textElement = element,
                mapper = mapper,
                onTextChangeCompleted = { text ->
                    element.text = text
                    activeInjectingElement = null
                }
            )
        }

        // Frosted Control Bar Layout Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, start = 16.dp, end = 16.dp)
                .liquidGlassCard()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExitWorkspace) { Text("❌", color = Color.Black) }
            Text("PRISMPDF WORKSPACE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70A3FF))) {
                Text("Save", color = Color.White)
            }
        }

        // Active Glassmorphic Floating Tool Controller
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MultiPageThumbnailCarousel(
                pageThumbnails = listOf(sessionWorkspace.baseRaster),
                currentPageIndex = 0,
                onPageSelected = {}
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Floating Navigation Panel supporting dynamic visual state highlights
            Row(
                modifier = Modifier
                    .liquidGlassCard()
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                FilledTonalButton(
                    onClick = { currentToolMode = EditorToolMode.PAN_AND_ZOOM },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (currentToolMode == EditorToolMode.PAN_AND_ZOOM) Color(0x3D70A3FF) else Color.Transparent
                    )
                ) {
                    Text("🔍 View", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                FilledTonalButton(
                    onClick = { currentToolMode = EditorToolMode.CORNER_TEXT_INJECTOR },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (currentToolMode == EditorToolMode.CORNER_TEXT_INJECTOR) Color(0x3D70A3FF) else Color.Transparent
                    )
                ) {
                    Text("✍️ Snap Edit", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
