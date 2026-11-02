package com.prismpdf.editor.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prismpdf.editor.ui.liquidGlassCard
import com.prismpdf.editor.ui.DeveloperProfilePanel
import com.prismpdf.editor.ui.PrimaryText
import com.prismpdf.editor.ui.SecondaryText

@Composable
fun PrismPDFHomeScreen(onLaunchPicker: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Branding Title
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "PrismPDF", fontSize = 32.sp, fontWeight = FontWeight.Black, color = PrimaryText)
            Text(text = "Pixel-Perfect Document Engine", fontSize = 14.sp, color = SecondaryText)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Advanced Sandbox Card Panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .liquidGlassCard()
                    .clickable { onLaunchPicker() } // Direct interactive trigger mapping
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "🛡️", fontSize = 28.sp)
                Column {
                    Text(text = "Advanced Sandbox", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    Text(text = "ID & Passport Corner Edit", fontSize = 11.sp, color = SecondaryText)
                }
            }

            // Standard Toolkit Card Panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .liquidGlassCard()
                    .clickable { onLaunchPicker() } // Direct interactive trigger mapping
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "📄", fontSize = 28.sp)
                Column {
                    Text(text = "Standard Toolkit", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    Text(text = "Forms, Signatures & Layouts", fontSize = 11.sp, color = SecondaryText)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        DeveloperProfilePanel()
        Spacer(modifier = Modifier.height(48.dp))
    }
}
