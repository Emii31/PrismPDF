package com.passport.editor.ui

import androidx.lifecycle.*
import com.passport.editor.cv.BackgroundTextureSynthesizer
import com.passport.editor.cv.OpenCVInitializer
import com.passport.editor.graphics.PDFEditorRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PassportEditorViewModel : ViewModel() {

    private val repository = PDFEditorRepository()
    private val synthesizer = BackgroundTextureSynthesizer()
    private val jobs = MutableSet<Job>()

    private val _uiState = MutableStateFlow<EditorState>(EditorState.Idle)
    val uiState: StateFlow<EditorState> = _uiState.asStateFlow()

    private val _elements = MutableStateFlow<List<OverlayElement>>(emptyList())
    val elements: StateFlow<List<OverlayElement>> = _elements.asStateFlow()

    fun initEditor(context: android.content.Context) {
        OpenCVInitializer.initialize(context)
        _uiState.value = EditorState.Ready
    }

    fun loadPage(pageResult: com.passport.editor.graphics.PdfPageRenderer.PageRenderResult,
                 mapper: PDFCoordinateMapper) {
        viewModelScope.launch {
            _uiState.value = EditorState.Loading
            repository.cachePageRender(pageResult)
            _uiState.value = EditorState.Ready(mapper, pageResult)
        }
    }

    fun requestInpaint(bitmap: android.graphics.Bitmap, region: android.graphics.Rect,
                       onComplete: (android.graphics.Bitmap) -> Unit) {
        val job = viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = EditorState.Inpainting
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

/**
 * Repository holding cached page renders and managing the PDF renderer lifecycle.
 */
class PDFEditorRepository {

    private var pageCache: LruCache<String, android.graphics.Bitmap> =
        LruCache(maxSize = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt())

    fun cachePageRender(result: com.passport.editor.graphics.PdfPageRenderer.PageRenderResult) {
        val key = "page_${result.pageIndex}_${result.targetDpi}"
        pageCache.put(key, result.bitmap)
    }

    fun getCachedBitmap(pageIndex: Int, dpi: Int): android.graphics.Bitmap? {
        return pageCache.get("page_${pageIndex}_${dpi}")
    }

    fun evictCache() {
        pageCache.evictAll()
    }
}