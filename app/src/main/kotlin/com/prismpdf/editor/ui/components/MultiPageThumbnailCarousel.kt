package com.prismpdf.editor.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders a lazy-loaded, horizontally scrolling thumbnail tray for page navigation.
 *
 * @param pageThumbnails The collection of pre-rendered page bitmap previews.
 * @param currentPageIndex The index pointer of the currently active editor page.
 * @param onPageSelected Callback triggered when a user selects a page card.
 */
@Composable
fun MultiPageThumbnailCarousel(
    pageThumbnails: List<Bitmap>,
    currentPageIndex: Int,
    onPageSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0x0D000000))
            .padding(vertical = 14.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "DOCUMENT PAGES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(pageThumbnails) { index, thumbnail ->
                val isSelected = index == currentPageIndex

                Box(
                    modifier = Modifier
                        .width(76.dp)
                        .height(104.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0x3D70A3FF) else Color(0x1F1E293B))
                        .liquidGlassCard(RoundedCornerShape(12.dp))
                        .clickable { onPageSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = thumbnail.asImageBitmap(),
                        contentDescription = "Page Preview Thumbnail ${index + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Page counter overlay tag badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(Color(0xCC0F172A), RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
