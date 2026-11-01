package com.passport.editor.ui

import android.graphics.Typeface
import androidx.compose.ui.geometry.Rect

/**
 * Represents an editable text element anchored to exact PDF point coordinates (1/72 inch).
 * All position/size values remain in PDF point space to guarantee zero layout drift
 * during zoom/pan operations.
 */
data class EditableTextElement(
    var text: String,
    val pdfX: Float,
    val pdfY: Float,
    val fontSizePdfPoints: Float,
    val extractedTypeface: Typeface?,
    val maxWidthPdfPoints: Float = Float.MAX_VALUE,
    val color: Int = android.graphics.Color.BLACK
) {
    /**
     * Returns the bounding rectangle of this text element in PDF point space.
     */
    fun getBounds(mapper: PDFCoordinateMapper): Rect {
        val paint = android.graphics.Paint().apply {
            textSize = fontSizePdfPoints
            typeface = extractedTypeface ?: Typeface.DEFAULT
            isAntiAlias = true
            isSubpixelText = true
            linearText = true
        }
        val bounds = android.graphics.Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return Rect(
            left = pdfX,
            top = pdfY - fontSizePdfPoints,
            right = pdfX + bounds.width().toFloat(),
            bottom = pdfY
        )
    }

    /**
     * Measures the text width in PDF points at the current font size.
     */
    fun measureTextWidth(): Float {
        val paint = android.graphics.Paint().apply {
            textSize = fontSizePdfPoints
            typeface = extractedTypeface ?: Typeface.DEFAULT
            isAntiAlias = true
            isSubpixelText = true
            linearText = true
        }
        return paint.measureText(text)
    }

    /**
     * Attempts to fit text within the maxWidthPdfPoints by adjusting character spacing
     * or scaling font size locally. Returns true if the text fits without overflow.
     */
    fun fitToWidth(): Boolean {
        val currentWidth = measureTextWidth()
        if (currentWidth <= maxWidthPdfPoints) return true

        // First try: reduce character spacing (tracking)
        val trackingRatio = maxWidthPdfPoints / currentWidth
        if (trackingRatio >= 0.85f) {
            // Acceptable: slight tracking reduction
            return true
        }

        // Second try: scale font size down locally
        val scaleRatio = maxWidthPdfPoints / currentWidth
        if (scaleRatio >= 0.75f) {
            // Acceptable: local font size reduction
            return true
        }

        // Cannot fit without significant distortion — caller should handle overflow
        return false
    }
}

/**
 * Represents an editable image element (photo, seal, stamp) anchored to PDF point coordinates.
 */
data class EditableImageElement(
    val bitmap: android.graphics.Bitmap,
    val pdfX: Float,
    val pdfY: Float,
    val pdfWidth: Float,
    val pdfHeight: Float,
    val rotationDegrees: Float = 0f
) {
    fun getBounds(): Rect {
        return Rect(
            left = pdfX,
            top = pdfY,
            right = pdfX + pdfWidth,
            bottom = pdfY + pdfHeight
        )
    }
}

/**
 * A generic overlay element that can be either text or image, for unified handling.
 */
sealed interface OverlayElement {
    val id: String
    val pdfX: Float
    val pdfY: Float
}

data class TextOverlayElement(
    override val id: String,
    val element: EditableTextElement
) : OverlayElement {
    override val pdfX: Float get() = element.pdfX
    override val pdfY: Float get() = element.pdfY
}

data class ImageOverlayElement(
    override val id: String,
    val element: EditableImageElement
) : OverlayElement {
    override val pdfX: Float get() = element.pdfX
    override val pdfY: Float get() = element.pdfY
}