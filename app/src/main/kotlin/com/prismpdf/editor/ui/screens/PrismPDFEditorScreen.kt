package com.prismpdf.editor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.prismpdf.editor.DocumentSessionState
import com.prismpdf.editor.DocumentSessionViewModel
import com.prismpdf.editor.PDFCoordinateMapper
import com.prismpdf.editor.PassportEditorCanvas

@Composable
fun PrismPDFEditorScreen(
    sessionWorkspace: DocumentSessionState.ActiveWorkspace,
    viewModel: DocumentSessionViewModel,
    onExitWorkspace: () -> Unit
) {
    val mapper = remember { PDFCoordinateMapper() }

    Box(modifier = Modifier.fillMaxSize()) {
        PassportEditorCanvas(
            backgroundBitmap = sessionWorkspace.baseRaster,
            textElements = sessionWorkspace.overlays,
            imageElements = emptyList(),
            mapper = mapper
        )

        // Top bar with exit button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onExitWorkspace) {
                Text("Exit Workspace")
            }
        }
    }
}
