package com.prismpdf.editor.ui

import androidx.lifecycle.*
import com.prismpdf.editor.cv.BackgroundTextureSynthesizer
import com.prismpdf.editor.cv.OpenCVInitializer
import com.prismpdf.editor.graphics.PDFCoordinateMapper
import com.prismpdf.editor.graphics.PdfPageRenderer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PassportEditorViewModel : ViewModel() {

    private val synthesizer = BackgroundTextureSynthesizer()
    private val jobs = mutableSetOf<Job>()

    private val _uiState = MutableStateFlow<EditorState>(EditorState.Idle)
    val uiState: StateFlow<EditorState> = _uiState.asStateFlow()

    private val _elements = MutableStateFlow<List<OverlayElement>>(emptyList())
    val elements: StateFlow<List<OverlayElement>> = _elements.asStateFlow()

    fun initEditor(context: android.content.Context) {
        OpenCVInitializer.initialize(context)
        _uiState.value = EditorState.Ready
    }

    fun loadPage(pageResult: PdfPageRenderer.PageRenderResult,
                 mapper: PDFCoordinateMapper) {
        viewModelScope.launch {
            _uiState.value = EditorState.Loading
            _uiState.value = EditorState.Ready
        }
    }

    fun requestInpaint(bitmap: android.graphics.Bitmap, region: android.graphics.Rect,
                       onComplete: (android.graphics.Bitmap) -> Unit) {
        val job = viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = EditorState.Inpainting()
            try {
                val cleaned = synthesizer.eraseTextAndRestorePattern(bitmap, region)
                withContext(Dispatchers.Main) {
                    onComplete(cleaned)
                    _uiState.value = EditorState.Ready
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = EditorState.Error(e.message ?: "Inpainting failed")
                }
            }
        }
        jobs.add(job)
        job.invokeOnCompletion { jobs.remove(it) }
    }

    fun updateTextElement(id: String, newText: String) {
        val current = _elements.value
        _elements.value = current.map { el ->
            if (el is TextOverlayElement && el.id == id) {
                el.copy(element = el.element.copy(text = newText))
            } else el
        }
    }

    override fun onCleared() {
        jobs.forEach { it.cancel() }
        super.onCleared()
    }
}

sealed class EditorState {
    object Idle : EditorState()
    object Loading : EditorState()
    object Ready : EditorState()
    data class Inpainting(val progress: Float = 0f) : EditorState()
    data class Error(val message: String) : EditorState()
}
