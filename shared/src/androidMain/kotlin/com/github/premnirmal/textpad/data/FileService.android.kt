package com.github.premnirmal.textpad.data

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

@Composable
actual fun rememberFileService(): FileService {
    val context = LocalContext.current.applicationContext

    val openDeferred = remember { mutableStateOf<CompletableDeferred<Uri?>?>(null) }
    val saveDeferred = remember { mutableStateOf<CompletableDeferred<Uri?>?>(null) }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        openDeferred.value?.complete(uri)
        openDeferred.value = null
    }
    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        saveDeferred.value?.complete(uri)
        saveDeferred.value = null
    }

    return remember(context) {
        object : FileService {
            override suspend fun openFile(): String? {
                val deferred = CompletableDeferred<Uri?>()
                openDeferred.value = deferred
                openLauncher.launch(arrayOf("text/plain"))
                val uri = deferred.await() ?: return null
                return withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        input.readBytes().decodeToString()
                    }
                }
            }

            override suspend fun saveFile(suggestedName: String, content: String): Boolean {
                val deferred = CompletableDeferred<Uri?>()
                saveDeferred.value = deferred
                saveLauncher.launch(suggestedName)
                val uri = deferred.await() ?: return false
                withContext(Dispatchers.IO) {
                    context.contentResolver.openFileDescriptor(uri, "rwt")?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { output ->
                            output.write(content.toByteArray())
                        }
                    }
                }
                return true
            }
        }
    }
}
