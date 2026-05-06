package com.github.premnirmal.textpad

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.premnirmal.textpad.theme.AppTheme
import com.github.premnirmal.textpad.theme.AppTypography
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(lightScrim = Color.Transparent.toArgb(), darkScrim = Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.auto(lightScrim = Color.Transparent.toArgb(), darkScrim = Color.Transparent.toArgb()),
        )
        Injector.appComponent.inject(this)

        setContent {
            AppTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                val text by viewModel.note.collectAsStateWithLifecycle()
                val textFieldState = rememberTextFieldState()
                LaunchedEffect(text) {
                    if (textFieldState.text.toString() != text) {
                        textFieldState.edit {
                            replace(0, length, text)
                            selection = TextRange(text.length)
                        }
                    }
                }
                val message by viewModel.messageState.collectAsStateWithLifecycle(null)
                LaunchedEffect(message) {
                    message?.let {
                        snackbarHostState.showSnackbar(it)
                    }
                }

                DisposableEffect(textFieldState) {
                    onDispose {
                        viewModel.updateCache(textFieldState.text.toString())
                    }
                }
                LaunchedEffect(textFieldState) {
                    snapshotFlow { textFieldState.text.toString() }
                        .debounce(CACHE_WRITE_DEBOUNCE_MS)
                        .distinctUntilChanged()
                        .collect { viewModel.updateCache(it) }
                }

                val dashShortcutTransformation = remember {
                    DashLineTransformation()
                }

                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                Scaffold(
                    modifier = Modifier.imePadding(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "TextPad",
                                    modifier = Modifier,
                                    style = AppTypography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            },
                            navigationIcon = {
                                Image(
                                    modifier = Modifier.size(42.dp),
                                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                                    contentDescription = null,
                                )
                            },
                        )
                    },
                    floatingActionButton = {
                        var showPopup by remember { mutableStateOf(false) }
                        DropdownMenu(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            expanded = showPopup,
                            onDismissRequest = {
                                showPopup = false
                                focusRequester.requestFocus()
                            }
                        ) {
                            val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
                                if (it != null) {
                                    viewModel.open(this@MainActivity, it)
                                }
                            }
                            val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
                                if (it != null) {
                                    viewModel.save(this@MainActivity, it)
                                }
                            }
                            DropdownMenuItem(
                                text = { Text("Clear") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_clear),
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                },
                                onClick = {
                                    viewModel.clearCache()
                                    textFieldState.edit {
                                        replace(0, length, "")
                                        selection = TextRange(0)
                                    }
                                })
                            DropdownMenuItem(
                                text = { Text("Open") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_file_open),
                                        contentDescription = "Open",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                },
                                onClick = { openLauncher.launch(arrayOf("text/plain")) }
                            )
                            DropdownMenuItem(
                                text = { Text("Save") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_save),
                                        contentDescription = "Save",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                },
                                onClick = { saveLauncher.launch("File.txt") }
                            )
                        }
                        SmallFloatingActionButton(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            onClick = {
                                showPopup = true
                            },
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.ic_more_horizontal),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentDescription = "More",
                            )
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    ) {
                        val scrollState = rememberScrollState()
                        val editorText = textFieldState.text.toString()
                        LaunchedEffect(scrollState.maxValue, editorText, textFieldState.selection) {
                            if (textFieldState.selection == TextRange(editorText.length)) {
                                scrollState.scrollTo(scrollState.maxValue)
                            }
                        }

                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .focusRequester(focusRequester),
                            state = textFieldState,
                            textStyle = AppTypography.bodyMedium,
                            inputTransformation = dashShortcutTransformation,
                            lineLimits = TextFieldLineLimits.MultiLine(),
                            colors = TextFieldDefaults.colors().copy(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                showKeyboardOnFocus = true,
                                imeAction = ImeAction.None,
                                keyboardType = KeyboardType.Text,
                                autoCorrectEnabled = true,
                                capitalization = KeyboardCapitalization.Sentences,
                            ),
                        )
                    }
                }
            }
        }
    }

    class DashLineTransformation : InputTransformation {
        private var lastUpdateTimeMillis = 0L

        override fun TextFieldBuffer.transformInput() {
            val now = SystemClock.uptimeMillis()
            if (now - lastUpdateTimeMillis <= DASH_SHORTCUT_WINDOW_MS) {
                val selectionEnd = selection.end
                val currentText = asCharSequence()
                val hadDashBefore = selectionEnd >= 4 && currentText[selectionEnd - 4] == '-'
                val hasTripleDash = selectionEnd >= 3 && currentText.subSequence(selectionEnd - 3, selectionEnd).toString() == "---"
                if (!hadDashBefore && hasTripleDash) {
                    replace(selectionEnd, selectionEnd, DASH_SEPARATOR)
                    selection = TextRange(selectionEnd + DASH_SEPARATOR.length)
                }
            }
            lastUpdateTimeMillis = now
        }

        companion object {
            private const val DASH_SHORTCUT_WINDOW_MS = 800L
        }
    }

    companion object {
        private const val CACHE_WRITE_DEBOUNCE_MS = 300L
        private const val DASH_SEPARATOR = "----------------------------\n"
    }
}
