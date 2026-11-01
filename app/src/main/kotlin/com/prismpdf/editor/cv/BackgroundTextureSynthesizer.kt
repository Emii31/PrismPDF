package com.prismpdf.editor.cv

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.photo.Photo

class BackgroundTextureSynthesizer {

    /**
     * Inpaints the specified bounding region to erase text while restoring the background pattern.
     * @param sourceBitmap The high-resolution original page raster bitmap.
     * @param targetBoundingBox The exact rectangular coordinates of the text to be removed.
     */
    fun eraseTextAndRestorePattern(sourceBitmap: Bitmap, targetBoundingBox: android.graphics.Rect): Bitmap {
        val srcMat = Mat()
        Utils.bitmapToMat(sourceBitmap, srcMat)

        // Generate a matching single-channel binary mask initialized to black (0)
        val maskMat = Mat.zeros(srcMat.size(), CvType.CV_8UC1)

        // Define the target bounding region inside the mask and set it to white (255)
        val roi = Rect(targetBoundingBox.left, targetBoundingBox.top, targetBoundingBox.width(), targetBoundingBox.height())
        val maskRoi = Mat(maskMat, roi)
        maskRoi.setTo(Scalar(255.0))

        val destinationMat = Mat()

        // Execute Navier-Stokes or Telea based low-level structural inpainting over the mask
        Photo.inpaint(srcMat, maskMat, destinationMat, 3.0, Photo.INPAINT_TELEA)

        // Convert the updated native structural Mat layout back to a safe Android Bitmap layer
        val outputBitmap = Bitmap.createBitmap(sourceBitmap.width, sourceBitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(destinationMat, outputBitmap)

        // Clean native memory resources immediately
        srcMat.release()
        maskMat.release()
        maskRoi.release()
        destinationMat.release()

        return outputBitmap
    }
}
