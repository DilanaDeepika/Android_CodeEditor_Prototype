package com.example.codeeditor

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import com.example.codeeditor.ui.theme.CodeEditorTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val editorState = TextEditorState()
    private var currentFileName by mutableStateOf("Untitled.kt")

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val clipboardManager = LocalClipboardManager.current
            val scope = rememberCoroutineScope()

            var syntaxRules by remember { mutableStateOf(loadSyntaxRules(context, "kotlin.json")) }
            var showMiniToolbar by remember { mutableStateOf(false) }
            var showFindReplace by remember { mutableStateOf(false) }

            // Compiler dialog states
            var showCompileDialog by remember { mutableStateOf(false) }
            var compileOutput by remember { mutableStateOf("") }

            val drawerState = rememberDrawerState(DrawerValue.Closed)

            // ----------------------
            // Helpers for file name
            // ----------------------
            fun getFileNameFromUri(uri: Uri): String {
                var name = "Untitled.kt"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) name = cursor.getString(index)
                    }
                }
                return name
            }

            // ----------------------
            // External Open
            // ----------------------
            val openExternalFile = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader().use {
                        val loaded = it?.readText().orEmpty()
                        editorState.textField.value = TextFieldValue(loaded)
                        currentFileName = getFileNameFromUri(uri)
                        Toast.makeText(context, "File Opened", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // ----------------------
            // External Save
            // ----------------------
            val saveExternalFile = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("*/*")
            ) { uri ->
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(editorState.textField.value.text.toByteArray())
                    }
                    currentFileName = getFileNameFromUri(uri)
                    Toast.makeText(context, "File Saved", Toast.LENGTH_SHORT).show()
                }
            }

            // ----------------------
            // Auto-save draft in memory
            // ----------------------
            LaunchedEffect(editorState.textField.value) {
                snapshotFlow { editorState.textField.value }
                    .debounce(500)
                    .collect { editorState.commitChange() }
            }

            // ----------------------
            // UI
            // ----------------------
            CodeEditorTheme {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(
                            onNewFile = {
                                editorState.textField.value = TextFieldValue("")
                                currentFileName = "Untitled.kt"
                            },
                            onOpenFile = { openExternalFile.launch(arrayOf("*/*")) },
                            onSaveFile = {
                                val suggestedName =
                                    if (currentFileName.contains(".")) currentFileName else "$currentFileName.kt"
                                saveExternalFile.launch(suggestedName)
                            }
                        )
                    }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Code Editor - $currentFileName") },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { showMiniToolbar = !showMiniToolbar }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Mini Toolbar")
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            ModernBottomBar(
                                editorState = editorState,
                                showFindReplace = showFindReplace,
                                onToggleFindReplace = { showFindReplace = it },
                                currentFileName = currentFileName,
                                fileManager = FileManager(context),
                                onCompileOutput = { output ->
                                    compileOutput = output
                                    showCompileDialog = true
                                }
                            )
                        }
                    ) { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding)) {

                            if (showFindReplace) {
                                FindReplaceBar(editorState) { showFindReplace = false }
                            }

                            if (showMiniToolbar) {
                                MiniToolbar(
                                    onCut = {
                                        cutText(
                                            editorState.textField.value,
                                            { editorState.onTextChange(it) },
                                            clipboardManager
                                        )
                                    },
                                    onCopy = {
                                        copyText(editorState.textField.value, clipboardManager)
                                    },
                                    onPaste = {
                                        pasteText(
                                            editorState.textField.value,
                                            { editorState.onTextChange(it) },
                                            clipboardManager
                                        )
                                    }
                                )
                            }

                            // ----------------------
                            // Code Editor
                            // ----------------------
                            CodeEditor(
                                modifier = Modifier.weight(1f),
                                editorState = editorState,
                                syntaxRules = syntaxRules
                            )
                        }
                    }

                    // ---------------------------
                    // Show Compiler Dialog
                    // ---------------------------
                    if (showCompileDialog) {
                        CompilerInterface(
                            clipboardManager = clipboardManager,
                            compileOutput = compileOutput,
                            onClose = { showCompileDialog = false }
                        )
                    }
                }
            }
        }
    }
}
