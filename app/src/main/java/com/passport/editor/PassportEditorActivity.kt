package com.passport.editor

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.appcompat.app.AppCompatActivity
import com.passport.editor.graphics.PDFCoordinateMapper
import com.passport.editor.graphics.PdfPageRenderer
import com.passport.editor.graphics.PDFEditorRepository
import com.passport.editor.ui.PassportEditorViewModel
import com.passport.editor.ui.OverlayModels

class PassportEditorActivity : AppCompatActivity() {

    private lateinit var mapper: PDFCoordinateMapper
    private var pageResult: com.passport.editor.graphics.PdfPageRenderer.PageRenderResult? = null
    private val viewModel by lazy { PassportEditorViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapper = PDFCoordinateMapper()

        // In a real app, you'd receive the result from a repository layer
        // For now, this is a placeholder initialization
        pageResult = initializePageResult()

        viewModel.initEditor(this)
        pageResult?.let { viewModel.loadPage(it, mapper) }

        setContent {
            val elements by viewModel.elements.collectAsState()

            val typedTextElements = elements.filterIsInstance<OverlayModels.TextOverlayElement>().map { it.element }
            val typedImageElements = elements.filterIsInstance<OverlayModels.ImageOverlayElement>().map { it.element }

            PassportEditorCanvas(
                backgroundBitmap = pageResult?.bitmap ?: android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888),
                textElements = typedTextElements,
                imageElements = typedImageElements,
                mapper = mapper
            )
        }
    }

    private fun initializePageResult(): com.passport.editor.graphics.PdfPageRenderer.PageRenderResult {
        // This is a placeholder to simulate a real page result
        val bitmap = android.graphics.Bitmap.createBitmap(1000, 1500, android.graphics.Bitmap.Config.ARGB_8888)
        return com.passport.editor.graphics.PdfPageRenderer.PageRenderResult(
            bitmap = bitmap,
            renderer = null as com.passport.editor.graphics.PdfPageRenderer?,
            pageIndex = 0,
            targetDpi = 300,
            pageWidthPoints = 5.5f,
            pageHeightPoints = 8.5f
        )
    }
}