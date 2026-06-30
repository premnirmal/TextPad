package com.github.premnirmal.textpad

import androidx.compose.ui.window.ComposeUIViewController
import com.github.premnirmal.textpad.ui.App
import platform.UIKit.UIViewController

// PascalCase factory name is the iOS entry point referenced from Swift (MainViewControllerKt).
@Suppress("FunctionNaming")
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
