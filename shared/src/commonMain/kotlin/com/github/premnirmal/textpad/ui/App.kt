package com.github.premnirmal.textpad.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.premnirmal.textpad.MainViewModel
import com.github.premnirmal.textpad.data.Cache
import com.github.premnirmal.textpad.data.rememberFileService
import com.github.premnirmal.textpad.resources.Res
import com.github.premnirmal.textpad.resources.app_icon
import com.github.premnirmal.textpad.resources.ic_clear
import com.github.premnirmal.textpad.resources.ic_file_open
import com.github.premnirmal.textpad.resources.ic_more_horizontal
import com.github.premnirmal.textpad.resources.ic_save
import com.github.premnirmal.textpad.theme.AppTheme
import com.github.premnirmal.textpad.theme.AppTypography
import com.russhwolf.settings.Settings
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.painterResource
import kotlin.time.TimeSource

private const val CACHE_WRITE_DEBOUNCE_MS = 300L

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, FlowPreview::class)
@Composable
fun App() {
    AppTheme {
        val viewModel: MainViewModel = viewModel { MainViewModel(Cache(Settings())) }
        val fileService = rememberFileService()
        val snackbarHostState = remember { SnackbarHostState() }

        val textFieldState = rememberTextFieldState()
        LaunchedEffect(textFieldState) {
            viewModel.editorContent.collect { content ->
                if (textFieldState.text.toString() != content) {
                    textFieldState.edit {
                        replace(0, length, content)
                        selection = TextRange(content.length)
                    }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Scaffold(
                containerColor = Color.Transparent,
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
                                painter = painterResource(Res.drawable.app_icon),
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
                        DropdownMenuItem(
                            text = { Text("Clear") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_clear),
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
                                    painter = painterResource(Res.drawable.ic_file_open),
                                    contentDescription = "Open",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            },
                            onClick = {
                                showPopup = false
                                viewModel.open(fileService)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Save") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_save),
                                    contentDescription = "Save",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            },
                            onClick = {
                                showPopup = false
                                viewModel.save(fileService)
                            }
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
                            painter = painterResource(Res.drawable.ic_more_horizontal),
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

private class DashLineTransformation : InputTransformation {
    private val timeSource = TimeSource.Monotonic
    private var lastUpdateMark: TimeSource.Monotonic.ValueTimeMark? = null

    override fun TextFieldBuffer.transformInput() {
        val now = timeSource.markNow()
        val previousMark = lastUpdateMark
        if (previousMark != null && (now - previousMark).inWholeMilliseconds <= DASH_SHORTCUT_WINDOW_MS) {
            val selectionEnd = selection.end
            val currentText = asCharSequence()
            // iOS "smart punctuation" rewrites consecutive hyphens into typographic
            // en/em dashes, so three typed hyphens may arrive as e.g. "—-". Sum the
            // hyphen-equivalent weight of the trailing dash run instead of matching "---".
            var index = selectionEnd
            var totalWeight = 0
            var separatorChar = '-'
            var maxWeight = 0
            while (index > 0) {
                val char = currentText[index - 1]
                val weight = dashWeight(char)
                if (weight == 0) break
                if (weight > maxWeight) {
                    maxWeight = weight
                    separatorChar = char
                }
                totalWeight += weight
                index--
            }
            if (totalWeight == DASH_SHORTCUT_LENGTH) {
                // Build the separator from the same dash glyph that was typed (a hyphen
                // on Android, an en/em dash on iOS), scaling the count by glyph width so
                // the rule stays a consistent visual length across platforms.
                val separator = separatorChar.toString()
                    .repeat(DASH_SEPARATOR_WIDTH / maxWeight) + "\n"
                replace(index, selectionEnd, separator)
                selection = TextRange(index + separator.length)
            }
        }
        lastUpdateMark = now
    }

    private fun dashWeight(char: Char): Int = when (char) {
        '-' -> 1
        '\u2013', '\u2014' -> 2 // en dash, em dash
        else -> 0
    }

    companion object {
        private const val DASH_SHORTCUT_WINDOW_MS = 800L
        private const val DASH_SHORTCUT_LENGTH = 3
        private const val DASH_SEPARATOR_WIDTH = 28
    }
}
