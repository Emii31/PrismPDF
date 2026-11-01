package com.prismpdf.editor.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.prismpdf.editor.ui.animation.PrismPDFSplashAnimation
import com.prismpdf.editor.ui.screens.PrismPDFHomeScreen

enum class ApplicationState {
    SPLASH_PRESENTATION,
    MAIN_WORKSPACE
}

/**
 * The master entry UI coordinator for the PrismPDF Application System.
 */
@Composable
fun PrismPDFApplicationCore() {
    var currentAppState by remember { mutableStateOf(ApplicationState.SPLASH_PRESENTATION) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F5F9), // Ice slate clear top surface base
                        Color(0xE6E2E8F0)  // Solidified dark slate canvas anchor
                    )
                )
            )
    ) {
        Crossfade(
            targetState = currentAppState,
            animationSpec = tween(durationMillis = 600)
        ) { state ->
            when (state) {
                ApplicationState.SPLASH_PRESENTATION -> {
                    PrismPDFSplashAnimation(
                        onAnimationComplete = {
                            currentAppState = ApplicationState.MAIN_WORKSPACE
                        }
                    )
                }
                ApplicationState.MAIN_WORKSPACE -> {
                    PrismPDFHomeScreen()
                }
            }
        }
    }
}