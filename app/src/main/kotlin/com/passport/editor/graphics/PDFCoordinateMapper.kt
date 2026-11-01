package com.passport.editor.graphics

import android.graphics.Matrix
import android.graphics.PointF

class PDFCoordinateMapper {
    // Master transformation matrix tracking panning and scaling variables
    val transformMatrix = Matrix()
    private val inverseMatrix = Matrix()
    private val floatArrayBuffer = FloatArray(9)

    fun updateMatrix(scale: Float, translateX: Float, translateY: Float) {
        transformMatrix.reset()
        transformMatrix.postScale(scale, scale)
        transformMatrix.postTranslate(translateX, translateY)
        transformMatrix.invert(inverseMatrix)
    }

    /**
     * Converts a raw PDF point coordinate (1/72 inch units) into an exact screen pixel coordinate.
     */
    fun pdfToScreen(pdfX: Float, pdfY: Float): PointF {
        val points = floatArrayOf(pdfX, pdfY)
        transformMatrix.mapPoints(points)
        return PointF(points[0], points[1])
    }

    /**
     * Converts a user screen touch pixel location back into the exact physical PDF layout point.
     */
    fun screenToPdf(screenX: Float, screenY: Float): PointF {
        val points = floatArrayOf(screenX, screenY)
        inverseMatrix.mapPoints(points)
        return PointF(points[0], points[1])
    }

    fun getScale(): Float {
        transformMatrix.getValues(floatArrayBuffer)
        return floatArrayBuffer[Matrix.MSCALE_X]
    }
}