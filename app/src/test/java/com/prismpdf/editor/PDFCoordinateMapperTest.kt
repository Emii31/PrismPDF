package com.prismpdf.editor

import android.graphics.PointF
import com.prismpdf.editor.graphics.PDFCoordinateMapper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PDFCoordinateMapperTest {

    private lateinit var mapper: PDFCoordinateMapper

    @Before
    fun setUp() {
        mapper = PDFCoordinateMapper()
    }

    @Test
    fun testSubPixelTransformationPrecision() {
        // Initialize mapper with 5x zoom magnification and a layout pixel pan offset
        val testScale = 5.0f
        val panX = 120.5f
        val panY = 340.25f
        mapper.updateMatrix(testScale, panX, panY)

        val physicalPdfX = 72.0f // Exactly 1 inch in PDF point coordinates
        val physicalPdfY = 144.0f // Exactly 2 inches in PDF point coordinates

        // Execute forward mapping layer calculation: PDF -> Screen Pixel Bounds
        val screenPos = mapper.pdfToScreen(physicalPdfX, physicalPdfY)

        val expectedScreenX = (physicalPdfX * testScale) + panX
        val expectedScreenY = (physicalPdfY * testScale) + panY

        assertEquals(expectedScreenX, screenPos.x, 0.001f)
        assertEquals(expectedScreenY, screenPos.y, 0.001f)

        // Execute inverse mapping layer verification: Screen Pixel Bounds -> PDF Point
        val restoredPdfPos = mapper.screenToPdf(screenPos.x, screenPos.y)

        assertEquals(physicalPdfX, restoredPdfPos.x, 0.001f)
        assertEquals(physicalPdfY, restoredPdfPos.y, 0.001f)
    }
}
