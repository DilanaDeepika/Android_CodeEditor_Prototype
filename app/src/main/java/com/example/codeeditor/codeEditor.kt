package com.example.codeeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// -------------------------
// Code Editor Composable
// -------------------------
@Composable
fun CodeEditor(
    modifier: Modifier,
    editorState: TextEditorState,
    syntaxRules: SyntaxRules
) {
    val scrollState = rememberScrollState()
    val editorText = editorState.textField.value

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White) // Fixed background
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        Row {
            // Line numbers
            val lines = editorText.text.lines().ifEmpty { listOf("") }
            Column(modifier = Modifier.width(50.dp).padding(end = 4.dp)) {
                lines.forEachIndexed { i, _ ->
                    Text(
                        text = "${i + 1}.",
                        style = TextStyle(fontSize = 16.sp, color = Color.DarkGray),
                        modifier = Modifier
                            .height(24.dp)
                            .padding(vertical = 2.dp)
                            .background(Color.LightGray) // Fixed line number background
                    )
                }
            }

            // Highlighted editor
            BasicTextField(
                value = editorText,
                onValueChange = { editorState.onTextChange(it) },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color.Transparent // keep actual input invisible
                ),
                decorationBox = { innerTextField ->
                    Box {
                        // Highlighted text (fixed color)
                        Text(
                            text = highlightSyntax(editorText.text, syntaxRules),
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = Color.Black // force editor text color
                            )
                        )
                        // Editable overlay (invisible)
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
