package com.prismpdf.editor.ui

import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.prismpdf.editor.ui.PrismPdfTheme

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
