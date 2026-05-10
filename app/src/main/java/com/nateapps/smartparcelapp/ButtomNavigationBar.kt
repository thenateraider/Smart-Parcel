package com.nateapps.smartparcelapp

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nateapps.smartparcelapp.ui.theme.IBMPlexSansThai

@Composable
fun BottomNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf("home", "history")
    val labels = listOf("หน้าหลัก", "ประวัติ")
    val icons = listOf(Icons.Default.Home, Icons.Default.History)

    Surface(
        tonalElevation = 8.dp, // Adjust elevation as needed
        shadowElevation = 6.dp, // เพิ่มเงา
        shape = RoundedCornerShape(21.dp), // Adjust corner radius as needed
        modifier = Modifier
            .padding(horizontal = 28.dp)
            .navigationBarsPadding() // ใช้สำหรับรองรับ gesture nav bar// Add padding to float the navbar
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.clip(RoundedCornerShape(16.dp)) // Clip the content to the rounded shape
        ) {
            items.forEachIndexed { index, route ->
                NavigationBarItem(
                    icon = { Icon(icons[index], contentDescription = labels[index]) },
                    label = { Text(labels[index], fontFamily = IBMPlexSansThai) },
                    selected = selectedIndex == index,
                    onClick = { onTabSelected(index) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.surface // Optional: to make indicator same as background
                    )
                )
            }
        }
    }
}
