package com.nateapps.smartparcelapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable function for the "More" screen.
 *
 * This screen is divided into two main sections:
 * 1. Top Section (30% of screen height):
 *    - Displays a title "เมนูเพิ่มเติม" (More Menu) centered within a background
 *      colored by `MaterialTheme.colorScheme.primary`.
 *    - The text color is `MaterialTheme.colorScheme.onPrimary`.
 * 2. Bottom Section (70% of screen height):
 *    - Fills the remaining space.
 *    - Has a background color of `MaterialTheme.colorScheme.background`.
 *    - Currently, this section is an empty Box, intended for future content.
 */
@Composable
fun MoreScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        // ส่วนบน 30%
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f) // 30% of the height
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text("เมนูเพิ่มเติม", fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimary)
        }
        // ส่วนล่าง 70%
        Box(
            modifier = Modifier
                .fillMaxSize() // Fills the remaining space (70%)
                .padding(50.dp)

                .padding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .padding(50.dp)
            ) {
                // Add your content for the bottom part here
            }
        }
    }
}