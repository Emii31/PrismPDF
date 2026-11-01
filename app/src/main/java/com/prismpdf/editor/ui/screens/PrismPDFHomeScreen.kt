package com.prismpdf.editor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prismpdf.editor.ui.components.liquidGlassCard
import com.prismpdf.editor.ui.panels.DeveloperProfilePanel
import com.prismpdf.editor.ui.theme.GlassBackground
import com.prismpdf.editor.ui.theme.PrimaryText
import com.prismpdf.editor.ui.theme.SecondaryText

@Composable
fun PrismPDFHomeScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // App Identity Header Block
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "PrismPDF",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryText,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Pixel-Perfect Document Engine",
                fontSize = 14.sp,
                color = SecondaryText,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Core Actions Grid Architecture
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Path A: Advanced Document Sandbox (High-DPI Security/Passport Engine Focus)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .liquidGlassCard()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "🛡️", fontSize = 28.sp)
                Column {
                    Text(text = "Advanced Sandbox", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    Text(text = "ID & Passport Corner Edit", fontSize = 11.sp, color = SecondaryText)
                }
            }

            // Path B: Standard Workspace Utility Tray (Everyday Forms, Text Manipulation, Merges)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .liquidGlassCard()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "📄", fontSize = 28.sp)
                Column {
                    Text(text = "Standard Toolkit", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    Text(text = "Forms, Signatures & Layouts", fontSize = 11.sp, color = SecondaryText)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Files Dashboard Section Component Placeholder
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .liquidGlassCard()
                .padding(20.dp)
        ) {
            Text(text = "Recent Projects", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                Text(text = "No recent documents edited", fontSize = 13.sp, color = SecondaryText)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dynamic System Integration Panel containing Dev Information & Infrastructure Feedback
        DeveloperProfilePanel()

        Spacer(modifier = Modifier.height(48.dp))
    }
}