package com.prismpdf.editor

import com.prismpdf.editor.ui.*

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.graphics.Bitmap


import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.appcompat.app.AppCompatActivity
import com.prismpdf.editor.PDFCoordinateMapper
import com.prismpdf.editor.PdfPageRenderer
import com.prismpdf.editor.PDFEditorRepository
import com.prismpdf.editor.PassportEditorViewModel

class PassportEditorActivity : AppCompatActivity() {

    private lateinit var mapper: PDFCoordinateMapper
    private var pageResult: com.prismpdf.editor.PdfPageRenderer.PageRenderResult? = null
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

            val typedTextElements = elements.filterIsInstance<EditableTextElement>()
            val typedImageElements = elements.filterIsInstance<EditableImageElement>()

            PassportEditorCanvas(
                backgroundBitmap = pageResult?.bitmap ?: android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888),
                textElements = typedTextElements,
                imageElements = typedImageElements,
                mapper = mapper
            )
        }
    }

    private fun initializePageResult(): com.prismpdf.editor.PdfPageRenderer.PageRenderResult {
        // This is a placeholder to simulate a real page result
        val bitmap = android.graphics.Bitmap.createBitmap(1000, 1500, android.graphics.Bitmap.Config.ARGB_8888)
        return com.prismpdf.editor.PdfPageRenderer.PageRenderResult(
            bitmap = bitmap,
            renderer = null as android.graphics.pdf.PdfRenderer?,
            pageIndex = 0,
            targetDpi = 300,
            pageWidthPoints = 5.5f,
            pageHeightPoints = 8.5f
        )
    }
}
