package com.prismpdf.editor.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.prismpdf.editor.ui.theme.PrismPdfTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrismPdfTheme {
                // Use the master application core coordinator with splash animation & home screen
                PrismPDFApplicationCore()
            }
        }
    }
}