package com.example.codeeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DrawerContent(
    onNewFile: () -> Unit,      // clear editor
    onOpenFile: () -> Unit,     // external open
    onSaveFile: () -> Unit      // external save
) {

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.7f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 24.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Code Editor",
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // New File
        SidebarItem(icon = Icons.Default.Add, label = "New File") {
            onNewFile()
        }

        // Open from Phone Storage
        SidebarItem(icon = Icons.Default.FolderOpen, label = "Open File") {
            onOpenFile()
        }

        // Save to Phone Storage
        SidebarItem(icon = Icons.Default.Save, label = "Save File") {
            onSaveFile()
        }
    }
}

@Composable
fun SidebarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .background(Color.Transparent, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
