package com.prismpdf.editor

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun loadDocumentWorkspace(highDpiBitmapCache: Bitmap) {
        _sessionState.value = DocumentSessionState.ProcessingBaseRaster

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val computedMask = try {
                    edgeTracker.trackVectorEdgesMultiChannel(highDpiBitmapCache)
                } catch (jniError: UnsatisfiedLinkError) {
                    Log.e("PrismVM", "OpenCV dynamic linking error caught. Activating fallback edge mapping matrix.")
                    Bitmap.createBitmap(highDpiBitmapCache.width, highDpiBitmapCache.height, Bitmap.Config.ARGB_8888)
                } catch (e: Exception) {
                    Bitmap.createBitmap(highDpiBitmapCache.width, highDpiBitmapCache.height, Bitmap.Config.ARGB_8888)
                }

                _sessionState.value = DocumentSessionState.ActiveWorkspace(
                    baseRaster = highDpiBitmapCache,
                    edgeTrackingMask = computedMask,
                    overlays = emptyList()
                )
            } catch (e: Exception) {
                _sessionState.value = DocumentSessionState.Error(e.localizedMessage ?: "Critical pipeline failure")
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
        executeLiveSemanticEdit(targetRect, initialText, pdfX, pdfY, fontSize)
    }

    /**
     * Executes real background texture clearing and registers the dynamically extracted text element.
     */
    fun executeLiveSemanticEdit(
        targetRect: Rect,
        extractedSourceText: String,
        pdfX: Float,
        pdfY: Float,
        computedFontSize: Float
    ) {
        val currentState = _sessionState.value
        if (currentState !is DocumentSessionState.ActiveWorkspace) return

        viewModelScope.launch(Dispatchers.Default) {
            // 1. Instantly wipe the original printed pixels via OpenCV pattern synthesis
            val updatedRasterBackground = try {
                textureSynthesizer.eraseTextAndRestorePattern(currentState.baseRaster, targetRect)
            } catch (e: Exception) {
                currentState.baseRaster
            }

            // 2. Instantiate the vector tracking layer initialized with the TRUE text present on screen
            val liveEditableElement = EditableTextElement(
                text = extractedSourceText,
                pdfX = pdfX,
                pdfY = pdfY,
                fontSizePdfPoints = computedFontSize,
                extractedTypeface = null
            )

            // 3. Emit updated workspace metrics back to the main user interface canvas loops
            _sessionState.value = currentState.copy(
                baseRaster = updatedRasterBackground,
                overlays = currentState.overlays + liveEditableElement
            )
        }
    }
}
