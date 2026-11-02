package com.prismpdf.editor.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prismpdf.editor.ui.PrismPDFSplashAnimation
import com.prismpdf.editor.ui.screens.PrismPDFHomeScreen
import com.prismpdf.editor.ui.screens.PrismPDFEditorScreen
import com.prismpdf.editor.DocumentSessionViewModel
import com.prismpdf.editor.DocumentSessionState
import com.prismpdf.editor.util.PdfPageRendererHelper

// Explicit static hook executing hardware JNI registration
private const val TAG = "PrismPDFInit"

enum class ApplicationState {
    SPLASH_PRESENTATION,
    MAIN_WORKSPACE,
    ACTIVE_EDITOR_SANDBOX
}

@Composable
fun PrismPDFApplicationCore() {
    // Force native initialization verification inside the main lifecycle anchor loop
    LaunchedEffect(Unit) {
        try {
            // Check if the embedded JNI wrappers link successfully
            if (org.opencv.android.OpenCVLoader.initDebug()) {
                Log.d(TAG, "OpenCV native JNI link state initialized successfully via wrapper check.")
            } else {
                Log.e(TAG, "OpenCV wrapper check reported false. Attempting fallback static system library load injection...")
                System.loadLibrary("opencv_java4")
            }
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Direct wrapper load assertion failed. Injecting explicit dynamic system library mapping link.", e)
            try {
                System.loadLibrary("opencv_java4") // Directly binds compile-time generated JNI binaries
            } catch (fatal: Exception) {
                Log.e(TAG, "CRITICAL: Native binary linkage pipeline completely blocked.", fatal)
            }
        }
    }

    var currentAppState by remember { mutableStateOf(ApplicationState.SPLASH_PRESENTATION) }
    val sessionViewModel: DocumentSessionViewModel = viewModel()
    val context = LocalContext.current
    val uiState by sessionViewModel.sessionState.collectAsState()

    // Intent launcher mapping native system file managers for loading PDFs
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { selectedUri: Uri? ->
        if (selectedUri != null) {
            currentAppState = ApplicationState.ACTIVE_EDITOR_SANDBOX
            // Process document stream in a background thread task
            val baseHighDpiBitmap = PdfPageRendererHelper.renderFirstPageToBitmap(context, selectedUri)
            if (baseHighDpiBitmap != null) {
                sessionViewModel.loadDocumentWorkspace(baseHighDpiBitmap)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF1F5F9), Color(0xE6E2E8F0))
                )
            )
    ) {
        Crossfade(targetState = currentAppState, animationSpec = tween(500)) { state ->
            when (state) {
                ApplicationState.SPLASH_PRESENTATION -> {
                    PrismPDFSplashAnimation(onAnimationComplete = {
                        currentAppState = ApplicationState.MAIN_WORKSPACE
                    })
                }
                ApplicationState.MAIN_WORKSPACE -> {
                    PrismPDFHomeScreen(onLaunchPicker = {
                        // Launch native SAF storage browser for PDF selection
                        pdfPickerLauncher.launch("application/pdf")
                    })
                }
                ApplicationState.ACTIVE_EDITOR_SANDBOX -> {
                    when (val currentSession = uiState) {
                        is DocumentSessionState.ActiveWorkspace -> {
                            PrismPDFEditorScreen(
                                sessionWorkspace = currentSession,
                                viewModel = sessionViewModel,
                                onExitWorkspace = { currentAppState = ApplicationState.MAIN_WORKSPACE }
                            )
                        }
                        is DocumentSessionState.ProcessingBaseRaster -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF70A3FF))
                            }
                        }
                        is DocumentSessionState.Error -> {
                            Text(
                                text = "Engine failure: ${currentSession.exceptionMessage}",
                                color = Color.Red,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}
