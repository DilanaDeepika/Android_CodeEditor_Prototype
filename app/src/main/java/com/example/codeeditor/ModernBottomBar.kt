package com.example.codeeditor

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.codeeditor.network.CompilerClient

@Composable
fun ModernBottomBar(
    editorState: TextEditorState,
    showFindReplace: Boolean,
    onToggleFindReplace: (Boolean) -> Unit,
    context: Context,
    currentFileName: String,
    fileManager: FileManager,
    onCompileOutput: (String) -> Unit
) {
    // Compute word and character count
    val textValue = editorState.textField.value.text
    val wordCount = textValue.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
    val charCount = textValue.length

    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Word & Character Count Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Words: $wordCount",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Chars: $charCount",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo
                ModernIconButton(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "Undo",
                    onClick = { editorState.undo() }
                )

                // Redo
                ModernIconButton(
                    imageVector = Icons.Default.Redo,
                    contentDescription = "Redo",
                    onClick = { editorState.redo() }
                )

                // Find/Replace
                ModernIconButton(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Find",
                    onClick = { onToggleFindReplace(!showFindReplace) }
                )

                // Compile
                ModernIconButton(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = "Compile",
                    onClick = {
                        val compiler = CompilerClient(context)
                        val code = editorState.textField.value.text

                        // Launch coroutine to compile
                        CoroutineScope(Dispatchers.Main).launch {
                            // Optional: save file locally
                            compiler.saveCodeLocally(fileManager, currentFileName, code)

                            // Compile code on server
                            val result = compiler.compile(code)

                            // Show stdout if ok, else stderr
                            val output = if (result.ok) result.stdout else result.stderr

                            // Send output to screen
                            onCompileOutput(output)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ModernIconButton(
    contentDescription: String,
    painter: androidx.compose.ui.graphics.painter.Painter? = null,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .padding(4.dp)
    ) {
        if (painter != null) {
            Icon(painter = painter, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.primary)
        } else if (imageVector != null) {
            Icon(imageVector = imageVector, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
