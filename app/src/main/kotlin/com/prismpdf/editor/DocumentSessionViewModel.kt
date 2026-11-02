package com.prismpdf.editor

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.PointF
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prismpdf.editor.core.PDFSpatialTextIndexer
import com.prismpdf.editor.core.PDFTextNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DocumentSessionState {
    object Idle : DocumentSessionState
    object ProcessingBaseRaster : DocumentSessionState
    data class ActiveWorkspace(
        val baseRaster: Bitmap,
        val edgeTrackingMask: Bitmap,
        val overlays: List<EditableTextElement>
    ) : DocumentSessionState
    data class Error(val exceptionMessage: String) : DocumentSessionState
}

class DocumentSessionViewModel : ViewModel() {
    private val _sessionState = MutableStateFlow<DocumentSessionState>(DocumentSessionState.Idle)
    val sessionState: StateFlow<DocumentSessionState> = _sessionState.asStateFlow()

    private val edgeTracker = MultiChannelEdgeTracker()
    private val textureSynthesizer = BackgroundTextureSynthesizer()

    // Instantiate the spatial index tracking repository
    val spatialTextIndexer = PDFSpatialTextIndexer()

    fun loadDocumentWorkspace(highDpiBitmapCache: Bitmap) {
        _sessionState.value = DocumentSessionState.ProcessingBaseRaster

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val computedMask = try {
                    edgeTracker.trackVectorEdgesMultiChannel(highDpiBitmapCache)
                } catch (e: UnsatisfiedLinkError) {
                    Bitmap.createBitmap(highDpiBitmapCache.width, highDpiBitmapCache.height, Bitmap.Config.ARGB_8888)
                } catch (e: Exception) {
                    Bitmap.createBitmap(highDpiBitmapCache.width, highDpiBitmapCache.height, Bitmap.Config.ARGB_8888)
                }

                // Parse and index the structural text stream elements at load time
                spatialTextIndexer.indexPageTextStream(mockElementsEnabled = true)

                _sessionState.value = DocumentSessionState.ActiveWorkspace(
                    baseRaster = highDpiBitmapCache,
                    edgeTrackingMask = computedMask,
                    overlays = emptyList()
                )
            } catch (e: Exception) {
                _sessionState.value = DocumentSessionState.Error(e.localizedMessage ?: "Initialization failure")
            }
        }
    }

    fun performTargetTextEdit(
        targetRect: Rect,
        placeholderToken: String,
        initialText: String,
        pdfX: Float,
        pdfY: Float,
        fontSize: Float
    ) {
        // Fallback simple edit implementation
        executeSemanticTextModification(
            targetRect = targetRect,
            matchedNode = PDFTextNode(initialText, android.graphics.RectF(pdfX, pdfY - fontSize, pdfX + 100f, pdfY), fontSize, null),
            updatedString = initialText,
            pdfX = pdfX,
            pdfY = pdfY
        )
    }

    fun executeSemanticTextModification(
        targetRect: Rect,
        matchedNode: PDFTextNode,
        updatedString: String,
        pdfX: Float,
        pdfY: Float
    ) {
        val currentState = _sessionState.value
        if (currentState !is DocumentSessionState.ActiveWorkspace) return

        viewModelScope.launch(Dispatchers.Default) {
            val patchedBitmap = try {
                textureSynthesizer.eraseTextAndRestorePattern(currentState.baseRaster, targetRect)
            } catch (e: Exception) {
                currentState.baseRaster
            }

            val semanticOverlay = EditableTextElement(
                text = updatedString,
                pdfX = pdfX,
                pdfY = pdfY,
                fontSizePdfPoints = matchedNode.fontSizePoints,
                extractedTypeface = null
            )

            _sessionState.value = currentState.copy(
                baseRaster = patchedBitmap,
                overlays = currentState.overlays + semanticOverlay
            )
        }
    }
}
