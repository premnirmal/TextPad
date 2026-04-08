package com.github.premnirmal.textpad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.premnirmal.textpad.ui.AppTheme
import com.github.premnirmal.textpad.ui.AppTypography
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Injector.appComponent.inject(this)

        setContent {
            AppTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                val text by viewModel.note.collectAsStateWithLifecycle()
                var updatedText by remember(text) {
                    mutableStateOf(
                        TextFieldValue(
                            text = text,
                            selection = TextRange(text.length),
                        )
                    )
                }
                val message by viewModel.messageState.collectAsStateWithLifecycle(null)
                LaunchedEffect(message) {
                    message?.let {
                        snackbarHostState.showSnackbar(it)
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        viewModel.updateCache(updatedText.text)
                    }
                }

                LaunchedEffect(Unit) {
                    while(true) {
                        delay(500L)
                        viewModel.updateCache(updatedText.text)
                    }
                }

                Scaffold(
                    modifier = Modifier.safeDrawingPadding(),
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
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    floatingActionButton = {
                        Row(
                            modifier = Modifier.imePadding(),
                        ) {
                            SmallFloatingActionButton(
                                onClick = {
                                    updatedText = TextFieldValue("")
                                    viewModel.updateCache("")
                                },
                            ) {
                                Image(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_clear),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                    contentDescription = "Clear",
                                )
                            }

                            val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
                                if (it != null) {
                                    viewModel.open(this@MainActivity, it)
                                }
                            }

                            SmallFloatingActionButton(
                                onClick = {
                                    openLauncher.launch(arrayOf("text/plain"))
                                },
                            ) {
                                Image(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_file_open),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                    contentDescription = "Open file",
                                )
                            }

                            val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
                                if (it != null) {
                                    viewModel.save(this@MainActivity, it)
                                }
                            }
                            SmallFloatingActionButton(
                                onClick = {
                                    saveLauncher.launch("file.txt")
                                },
                            ) {
                                Image(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_save),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                    contentDescription = "Save",
                                )
                            }
                        }
                    },
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        val focusRequester = remember { FocusRequester() }
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                        val scrollState = rememberScrollState()
                        LaunchedEffect(scrollState.maxValue, updatedText.text, updatedText.selection) {
                            if (updatedText.selection == TextRange(updatedText.text.length)) {
                                scrollState.scrollTo(scrollState.maxValue)
                            }
                        }

                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .imePadding()
                                .focusRequester(focusRequester),
                            value = updatedText,
                            onValueChange = {
                                updatedText = it
                            },
                            singleLine = false,
                            colors = TextFieldDefaults.colors().copy(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            ),
                        )
                    }
                }
            }
        }
    }
}
