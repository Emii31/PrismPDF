package com.prismpdf.editor.ui.panels

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prismpdf.editor.ui.components.liquidGlassCard
import com.prismpdf.editor.ui.theme.PrimaryText
import com.prismpdf.editor.ui.theme.SecondaryText

/**
 * Constructs the integrated Developer Profile & Application Feedback subsystem.
 */
@Composable
fun DeveloperProfilePanel() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DEVELOPER PROFILE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SecondaryText,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Emam Hossain (Emii31)",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryText
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Command Action A: External Browser Navigation to Developer Workspace
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Emii31"))
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = "View GitHub Profile", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Command Action B: Communication Navigation Intent to Feedback Endpoint
        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Emii31"))
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = "Send Feedback via Telegram", fontWeight = FontWeight.SemiBold)
        }
    }
}