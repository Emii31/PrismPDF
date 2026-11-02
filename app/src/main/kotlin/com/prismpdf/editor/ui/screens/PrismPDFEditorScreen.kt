package com.prismpdf.editor.ui.screens

import android.graphics.Rect
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prismpdf.editor.PDFCoordinateMapper
import com.prismpdf.editor.SubPixelSnappingEngine
import com.prismpdf.editor.EditableTextElement
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
    var activeInjectingElement by remember { mutableStateOf<EditableTextElement?>(null) }
    val standardFontScalePdfPoints by remember { mutableFloatStateOf(10f) }

    // Explicitly seed basic layout boundary properties to prevent initialization crashes
    LaunchedEffect(Unit) {
        mapper.updateMatrix(1f, 0f, 0f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                if (mapper.getScale() == 0f) {
                    mapper.updateMatrix(1f, 0f, 0f)
                }
            }
    ) {
        // LAYER 1: Core Rendering Framework
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentToolMode) {
                    if (currentToolMode == EditorToolMode.CORNER_TEXT_INJECTOR) {
                        detectTapGestures { offset ->
                            val clickPdfPoint = mapper.screenToPdf(offset.x, offset.y)

                            val snappedPdfPoint = snappingEngine.computeSnappedPdfCoordinates(
                                targetTouchPdfCoordinates = clickPdfPoint,
                                edgeMaskBitmap = sessionWorkspace.edgeTrackingMask,
                                mapper = mapper
                            )

                            val pixelLocation = mapper.pdfToScreen(snappedPdfPoint.x, snappedPdfPoint.y)

                            val inpaintWidth = 160
                            val inpaintHeight = 40
                            val targetInpaintBox = Rect(
                                (pixelLocation.x - (inpaintWidth / 4)).toInt().coerceAtLeast(0),
                                (pixelLocation.y - (inpaintHeight / 2)).toInt().coerceAtLeast(0),
                                (pixelLocation.x + (inpaintWidth * 3 / 4)).toInt().coerceAtMost(sessionWorkspace.baseRaster.width),
                                (pixelLocation.y + (inpaintHeight / 2)).toInt().coerceAtMost(sessionWorkspace.baseRaster.height)
                            )

                            activeInjectingElement = EditableTextElement(
                                text = "",
                                pdfX = snappedPdfPoint.x,
                                pdfY = snappedPdfPoint.y,
                                fontSizePdfPoints = standardFontScalePdfPoints,
                                extractedTypeface = null
                            )

                            viewModel.performTargetTextEdit(
                                targetRect = targetInpaintBox,
                                placeholderToken = "",
                                initialText = "",
                                pdfX = snappedPdfPoint.x,
                                pdfY = snappedPdfPoint.y,
                                fontSize = standardFontScalePdfPoints
                            )
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

        // LAYER 2: Soft Text Input Overlays
        activeInjectingElement?.let { targetOverlay ->
            InteractiveTextOverlayWidget(
                textElement = targetOverlay,
                mapper = mapper,
                onTextChangeCompleted = { finalizedString ->
                    targetOverlay.text = finalizedString
                    activeInjectingElement = null
                }
            )
        }

        // LAYER 3: Controls Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, start = 16.dp, end = 16.dp)
                .liquidGlassCard()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExitWorkspace) {
                Text(text = "❌", fontSize = 16.sp)
            }
            Text("PRISMPDF WORKSPACE WORKBENCH", fontSize = 12.sp, fontWeight = FontWeight.Black)
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70A3FF)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text("Save", color = Color.White, fontSize = 12.sp)
            }
        }

        // LAYER 4: Dynamic Bottom Navigation Preview and Navigation Panel
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

            Row(
                modifier = Modifier
                    .liquidGlassCard()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { currentToolMode = EditorToolMode.PAN_AND_ZOOM },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentToolMode == EditorToolMode.PAN_AND_ZOOM) Color(0x3370A3FF) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 20.sp)
                        Text(text = "Pan & Zoom", fontSize = 10.sp, color = Color.Black)
                    }
                }

                Button(
                    onClick = { currentToolMode = EditorToolMode.CORNER_TEXT_INJECTOR },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentToolMode == EditorToolMode.CORNER_TEXT_INJECTOR) Color(0x3370A3FF) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "✍️", fontSize = 20.sp)
                        Text(text = "Precision Edit", fontSize = 10.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}
