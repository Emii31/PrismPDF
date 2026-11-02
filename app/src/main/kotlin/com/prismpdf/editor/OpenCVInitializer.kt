package com.prismpdf.editor

import com.prismpdf.editor.ui.*

import android.content.Context
import android.util.Log
import org.opencv.android.OpenCVLoader

object OpenCVInitializer {
    private const val TAG = "OpenCVInitializer"
    private var isInitialized = false

    /**
     * Initializes OpenCV library. Must be called before any OpenCV operations.
     * Use OpenCVManager-based async init for production; this is the synchronous fallback.
     */
    @Synchronized
    fun initialize(context: Context): Boolean {
        if (isInitialized) return true

        // Try static initialization first (OpenCV Android SDK >= 4.5.0)
        if (OpenCVLoader.initDebug()) {
            isInitialized = true
            Log.i(TAG, "OpenCV loaded successfully via initDebug()")
            return true
        }

        // Note: For production, use async initialization via OpenCVManager:
        // OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, context, BaseLoaderCallback { ... })
        Log.e(TAG, "OpenCV initDebug() failed — ensure opencv-android SDK is bundled in gradle")
        return false
    }
}
