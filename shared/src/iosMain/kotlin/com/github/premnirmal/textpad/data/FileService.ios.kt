package com.github.premnirmal.textpad.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UniformTypeIdentifiers.UTTypePlainText
import platform.darwin.NSObject
import kotlin.coroutines.resume

/** Keeps picker delegates alive while their controllers are presented. */
private val activeDelegates = mutableListOf<NSObject>()

@Composable
actual fun rememberFileService(): FileService = remember { IosFileService() }

private class IosFileService : FileService {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun openFile(): String? = suspendCancellableCoroutine { continuation ->
        val delegate = PickerDelegate { urls ->
            val url = urls.firstOrNull()
            if (url == null) {
                if (continuation.isActive) continuation.resume(null)
                return@PickerDelegate
            }
            val accessing = url.startAccessingSecurityScopedResource()
            val text = NSString.stringWithContentsOfURL(
                url = url,
                encoding = NSUTF8StringEncoding,
                error = null
            )
            if (accessing) url.stopAccessingSecurityScopedResource()
            if (continuation.isActive) continuation.resume(text)
        }
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypePlainText)
        )
        present(picker, delegate) { if (continuation.isActive) continuation.resume(null) }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun saveFile(suggestedName: String, content: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            val tempPath = (NSTemporaryDirectory() as NSString)
                .stringByAppendingPathComponent(suggestedName)
            val tempUrl = NSURL.fileURLWithPath(tempPath)
            (content as NSString).writeToURL(
                url = tempUrl,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )
            val delegate = PickerDelegate { urls ->
                if (continuation.isActive) continuation.resume(urls.isNotEmpty())
            }
            val picker = UIDocumentPickerViewController(forExportingURLs = listOf(tempUrl))
            present(picker, delegate) { if (continuation.isActive) continuation.resume(false) }
        }

    private fun present(
        picker: UIDocumentPickerViewController,
        delegate: PickerDelegate,
        onMissingRoot: () -> Unit
    ) {
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController
        if (root == null) {
            onMissingRoot()
            return
        }
        activeDelegates.add(delegate)
        picker.delegate = delegate
        delegate.onFinished = { activeDelegates.remove(delegate) }
        root.presentViewController(picker, animated = true, completion = null)
    }
}

private class PickerDelegate(
    private val onPicked: (List<NSURL>) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    var onFinished: (() -> Unit)? = null
    private var finished = false

    private fun finish(urls: List<NSURL>) {
        if (finished) return
        finished = true
        onPicked(urls)
        onFinished?.invoke()
    }

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        finish(didPickDocumentsAtURLs.filterIsInstance<NSURL>())
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        finish(emptyList())
    }
}
