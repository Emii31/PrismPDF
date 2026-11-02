package com.prismpdf.editor

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prismpdf.editor.BackgroundTextureSynthesizer
import com.prismpdf.editor.MultiChannelEdgeTracker
import com.prismpdf.editor.EditableTextElement
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
                // Attempt to generate precision channel edge mapping masks
                val computedMask = try {
                    edgeTracker.trackVectorEdgesMultiChannel(highDpiBitmapCache)
                } catch (jniError: UnsatisfiedLinkError) {
                    Log.e("PrismVM", "OpenCV dynamic linking error caught. Activating fallback edge mapping matrix.")
                    // Generate a safe baseline fallback bitmap matching layout bounds exactly
                    Bitmap.createBitmap(highDpiBitmapCache.width, highDpiBitmapCache.height, Bitmap.Config.ARGB_8888)
                } catch (e: Exception) {
                    Log.e("PrismVM", "Exception during edge tracking. Activating fallback mask.")
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
        val currentState = _sessionState.value
        if (currentState !is DocumentSessionState.ActiveWorkspace) return

        viewModelScope.launch(Dispatchers.Default) {
            val updatedBitmap = try {
                textureSynthesizer.eraseTextAndRestorePattern(currentState.baseRaster, targetRect)
            } catch (e: Exception) {
                Log.e("PrismVM", "Texture synthesis fallback triggered due to memory safety bounds.")
                currentState.baseRaster // Keep original background layout if texture engine is locked
            }

            val newOverlay = EditableTextElement(
                text = initialText,
                pdfX = pdfX,
                pdfY = pdfY,
                fontSizePdfPoints = fontSize,
                extractedTypeface = null
            )

            _sessionState.value = currentState.copy(
                baseRaster = updatedBitmap,
                overlays = currentState.overlays + newOverlay
            )
        }
    }
}
