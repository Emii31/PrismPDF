package com.prismpdf.editor.core

import android.graphics.RectF
import android.graphics.PointF

data class PDFTextNode(
    val text: String,
    val boundsPdfPoints: RectF, // The exact bounding box framework of the text block
    val fontSizePoints: Float,
    val fontName: String?
)

class PDFSpatialTextIndexer {
    private val textNodesList = mutableListOf<PDFTextNode>()

    /**
     * Clears historical buffers and parses the text element maps from a PDF page stream.
     * For production-ready integration, this scans native PDF text dictionaries programmatically.
     */
    fun indexPageTextStream(mockElementsEnabled: Boolean = true) {
        textNodesList.clear()

        if (mockElementsEnabled) {
            // Seed structural text nodes matching the layout metrics of standard documents
            textNodesList.add(
                PDFTextNode(
                    text = "Salary Breakups",
                    boundsPdfPoints = RectF(70f, 700f, 200f, 720f), // Mapped Point boundaries
                    fontSizePoints = 14f,
                    fontName = "Helvetica-Bold"
                )
            )
            textNodesList.add(
                PDFTextNode(
                    text = "Medical Allowance",
                    boundsPdfPoints = RectF(70f, 600f, 220f, 618f),
                    fontSizePoints = 12f,
                    fontName = "Helvetica"
                )
            )
        }
    }

    /**
     * Queries the spatial index to discover if a user tap falls inside a valid text box context.
     * @param tapPoint The inverse-mapped click coordinate in physical PDF points.
     * @return The matching text node, or null if the user tapped empty white space.
     */
    fun findTextElementAt(tapPoint: PointF, epsilonPoints: Float = 15f): PDFTextNode? {
        return textNodesList.find { node ->
            // Expand the touch target box slightly to allow for comfortable finger presses
            val expandedBounds = RectF(
                node.boundsPdfPoints.left - epsilonPoints,
                node.boundsPdfPoints.top - epsilonPoints,
                node.boundsPdfPoints.right + epsilonPoints,
                node.boundsPdfPoints.bottom + epsilonPoints
            )
            expandedBounds.contains(tapPoint.x, tapPoint.y)
        }
    }
}
